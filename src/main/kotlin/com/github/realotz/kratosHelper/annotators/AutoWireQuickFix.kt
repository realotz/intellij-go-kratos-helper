package org.intellij.sdk.language

import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.util.IncorrectOperationException

internal class AutoWireQuickFix(private val key: String) : BaseIntentionAction() {
    override fun getText(): String {
        return "Auto Wire Set '$key'"
    }

    override fun getFamilyName(): String {
        return "Auto wire set"
    }

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        return true
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {

    }

    private fun createProperty(project: Project, file: VirtualFile) {
        WriteCommandAction.writeCommandAction(project).run<RuntimeException> {
//            val simpleFile: SimpleFile? = PsiManager.getInstance(project).findFile(file) as SimpleFile?
//            val lastChildNode: ASTNode = simpleFile.getNode().getLastChildNode()
//            // TODO: Add another check for CRLF
//            if (lastChildNode != null /* && !lastChildNode.getElementType().equals(SimpleTypes.CRLF)*/) {
//                simpleFile.getNode().addChild(SimpleElementFactory.createCRLF(project).getNode())
//            }
//            // IMPORTANT: change spaces to escaped spaces or the new node will only have the first word for the key
//            val property: SimpleProperty = SimpleElementFactory.createProperty(project, key.replace(" ".toRegex(), "\\\\ "), "")
//            simpleFile.getNode().addChild(property.getNode())
//            (property.getLastChild().getNavigationElement() as Navigatable).navigate(true)
//            FileEditorManager.getInstance(project).selectedTextEditor!!.caretModel.moveCaretRelatively(2, 0, false, false, false)
        }
    }
}