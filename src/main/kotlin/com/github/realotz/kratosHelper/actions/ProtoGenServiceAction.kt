package com.github.realotz.kratosHelper.actions

import com.github.realotz.kratosHelper.services.StorageService
import com.goide.GoTypes
import com.goide.psi.GoFile
import com.goide.psi.GoMethodDeclaration
import com.goide.psi.GoTypeSpec
import com.goide.psi.impl.GoElementFactory
import com.goide.psi.impl.GoMethodDeclarationImpl
import com.goide.psi.impl.GoTypeDeclarationImpl
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.ide.fileTemplates.impl.CustomFileTemplate
import com.intellij.internal.psiView.PsiViewerDialog
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.util.IncorrectOperationException
import com.intellij.util.ResourceUtil
import com.squareup.wire.schema.Location
import com.squareup.wire.schema.internal.parser.OptionElement
import com.squareup.wire.schema.internal.parser.ProtoParser
import com.squareup.wire.schema.internal.parser.RpcElement
import com.squareup.wire.schema.internal.parser.ServiceElement
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*


class ProtoGenServiceAction : AnAction() {
    // 点击操作
    override fun actionPerformed(e: AnActionEvent) {
        val mProject = e.getData(PlatformDataKeys.PROJECT) ?: return
        val settings = mProject.service<StorageService>()
        //获取选中的文件
        val file: VirtualFile? = CommonDataKeys.VIRTUAL_FILE.getData(e.dataContext)
        val view = LangDataKeys.IDE_VIEW.getData(e.dataContext)!!
        val state = settings.state
        if (file != null) {
            if (file.extension == null && state.protoFile != null) {
                this.genService(state.protoFile!!, file.path, view.orChooseDirectory!!)
            }
        }
    }

    fun getGoPackage(ops: List<OptionElement>): String {
        var goPackageStr = "pb"
        ops.forEach { item: OptionElement ->
            if (item.name == "go_package") {
                val pks = item.value.toString().split(";")
                goPackageStr = pks[pks.size - 1]
            }
        }
        return goPackageStr
    }

    fun getFilePackage(path: String): String {
        val pks = path.split("/")
        return pks[pks.size - 1]
    }

    val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()

    // String extensions
    fun camelToSnakeCase(str: String): String {
        return camelRegex.replace(str) {
            "_${it.value}"
        }.toLowerCase()
    }

    // 合并service.go
    fun mergeService(psiTree: GoFile, params: Properties, project: Project) {
        val list = params["RPCS"] as List<TreeMap<String, String>>
        val funcMap: HashMap<String, GoMethodDeclarationImpl> = HashMap<String, GoMethodDeclarationImpl>()
        var serviceTypeEl: PsiElement = psiTree.children[psiTree.children.size - 1]
        psiTree.children.forEach { item: PsiElement ->
            if (item.elementType == GoTypes.METHOD_DECLARATION) {
                val goFunc = GoMethodDeclarationImpl(item.node)
                funcMap[goFunc.qualifiedName.toString()] = goFunc
            }
            if (item.elementType == GoTypes.TYPE_DECLARATION) {
                val goType = GoTypeDeclarationImpl(item.node)
                goType.typeSpecList.forEach { ts: GoTypeSpec ->
                    if (ts.qualifiedName == params["GO_PACKAGE_NAME"] as String + "." + params["SERVICE"]) {
                        serviceTypeEl = item
                    }
                }
            }
        }
        val addElementList = mutableListOf<PsiElement>()
        list.forEach { item: TreeMap<String, String> ->
            val key = params["SERVICE"] as String + "." + item["NAME"]
            var flag = false
            val goFunc = funcMap[key]
            if (goFunc != null) {
                val requestList = goFunc.signature?.parameters?.parameterDeclarationList
                val response = goFunc.result?.parameters?.parameterDeclarationList
                if (requestList?.size != 2) {
                    flag = true
                } else {
                    if (requestList[1].type?.text != ("*" + item["REQUEST_TYPE"])) {
                        flag = true
                        println(requestList[1].text)
                        println(requestList[1].type?.text)
                        println(item["REQUEST_TYPE"])
                    }
                }
                if (response?.size != 2) {
                    flag = true
                } else {
                    if (response[0].type?.text != ("*" + item["RESPONSE_TYPE"])) {
                        flag = true
                        println(response[0].text)
                        println(item["RESPONSE_TYPE"])
                    }
                }
            } else {
                flag = true
            }
            if (flag) {
                val funStr = """package ${params["GO_PACKAGE_NAME"]}  
type ${params["SERVICE"]} struct {}
// ${item["NAME"]} ${item["DOCUMENTATION"]}
func (u ${params["SERVICE"]}) ${item["NAME"]}(ctx context.Context, req *${item["REQUEST_TYPE"]}) (*${item["RESPONSE_TYPE"]}, error) {
    return &${item["RESPONSE_TYPE"]}{}, nil
}"""
                val goFile = GoElementFactory.createFileFromText(project, funStr)
                goFile.methods.forEach{m: GoMethodDeclaration ->
                    addElementList.add(GoElementFactory.createNewLine(project))
                    addElementList.add(m)
                    addElementList.add(GoElementFactory.createNewLine(project))
                    addElementList.add(GoElementFactory.createLineCommentFromText(project,"${item["NAME"]} ${item["DOCUMENTATION"]}"))
                }
            }
        }
        addElementList.add(GoElementFactory.createNewLine(project,3))
        WriteCommandAction.runWriteCommandAction(project) {
            addElementList.forEach { item: PsiElement ->
                psiTree.addAfter(item,serviceTypeEl)
            }
        }
    }


    // 创建service.go
    fun genService(protoPath: String, toPath: String, dir: PsiDirectory) {
        val location = Location.get(protoPath)
        val data = String(Files.readAllBytes(Paths.get(protoPath)))
        val protoElement = ProtoParser.parse(location, data)
        val protoPackage = this.getGoPackage(protoElement.options)
        val goPackage = this.getFilePackage(toPath)
        val text = ResourceUtil.loadText(ResourceUtil.getResourceAsStream(this.javaClass.classLoader, "template", "service.vm"))
        val template = CustomFileTemplate("go", "go")
        template.text = text
        val props = FileTemplateManager.getInstance(dir.project).defaultProperties
        protoElement.services.forEach { item: ServiceElement ->
            //把数据填入上下文
            val className = this.camelToSnakeCase(item.name)
            props["PROTO_PACKAGE"] = protoPackage
            props["GO_PACKAGE_NAME"] = goPackage
            if (item.name.indexOf("Service", 0, false) == -1) {
                props["SERVICE"] = item.name + "Service"
            } else {
                props["SERVICE"] = item.name
            }
            props["SERVICE_DESC"] = item.documentation
            val rpcs = mutableListOf<TreeMap<String, String>>()
            item.rpcs.forEach { rpc: RpcElement ->
                val map = TreeMap<String, String>()
                map["NAME"] = rpc.name
                map["DOCUMENTATION"] = rpc.documentation
                map["RESPONSE_TYPE"] = rpc.responseType
                map["REQUEST_TYPE"] = rpc.requestType
                if (rpc.requestType.indexOf(".", 0, false) == -1) {
                    map["REQUEST_TYPE"] = protoPackage + "." + rpc.requestType
                }
                if (rpc.responseType.indexOf(".", 0, false) == -1) {
                    map["RESPONSE_TYPE"] = protoPackage + "." + rpc.responseType
                }
                rpcs.add(map)
            }
            props["RPCS"] = rpcs
            val file = dir.findFile("$className.go")
            if (file != null) {
                val goFile = file as? GoFile ?: return
                this.mergeService(goFile, props, dir.project)
            } else {
                try {
                    FileTemplateUtil.createFromTemplate(template, className, props, dir)
                } catch (e: IncorrectOperationException) {
                    val notify = NotificationGroupManager.getInstance().getNotificationGroup("Plugins updates")
                    notify.createNotification(className + ".go 文件已存在，请勿重复操作", NotificationType.INFORMATION).notify(dir.project);
                }
            }

        }
    }

    // 更新可见性
    override fun update(event: AnActionEvent) {
        val mProject = event.getData(PlatformDataKeys.PROJECT) ?: return
        val settings = mProject.service<StorageService>()
        val state = settings.state
        val extension = getFileExtension(event.dataContext)
        if (null == extension && state.protoFile != null) {
            event.presentation.isEnabledAndVisible = true
            event.presentation.isEnabledAndVisible = true
        } else {
            event.presentation.isEnabledAndVisible = false
            event.presentation.isEnabledAndVisible = false
        }
    }

    // 获取文件后缀
    fun getFileExtension(dataContext: DataContext): String? {
        return CommonDataKeys.VIRTUAL_FILE.getData(dataContext)?.extension
    }
}
