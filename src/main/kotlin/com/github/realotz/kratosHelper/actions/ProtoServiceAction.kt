package com.github.realotz.kratosHelper.actions

import com.github.realotz.kratosHelper.services.StorageService
import com.goide.GoTypes
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile

class ProtoServiceAction : AnAction() {

    // 点击操作
    override fun actionPerformed(e: AnActionEvent) {
        val mProject = e.getData(PlatformDataKeys.PROJECT) ?: return

        val settings = mProject.service<StorageService>()
        //获取选中的文件
        val file: VirtualFile? = CommonDataKeys.VIRTUAL_FILE.getData(e.dataContext)
        val notify = NotificationGroupManager.getInstance().getNotificationGroup("Plugins updates")
        val state = settings.state
        if (file != null) {
            if (file.extension == "proto") {
                state.protoFile = file.path
                settings.loadState(state)
                notify.createNotification("已解析 " + file.path, NotificationType.INFORMATION).notify(mProject);
            }else{
                Messages.showMessageDialog(mProject, "请先选择一个proto文件！", "Kratos通知", Messages.getInformationIcon())
            }
        }
    }

    // 获取文件后缀
    fun getFileExtension(dataContext: DataContext): String? {
        return CommonDataKeys.VIRTUAL_FILE.getData(dataContext)?.extension
    }

    // 更新可见性
    override fun update(event: AnActionEvent) {
        val extension = getFileExtension(event.dataContext)
        if ("proto" == extension) {
            event.presentation.isEnabledAndVisible = true
            event.presentation.isEnabledAndVisible = true
        } else {
            event.presentation.isEnabledAndVisible = false
            event.presentation.isEnabledAndVisible = false
        }
    }
}
