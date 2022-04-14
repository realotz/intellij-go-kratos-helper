package com.github.realotz.kratosHelper.annotators

import com.goide.GoTypes
import com.goide.psi.GoFunctionDeclaration
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.elementType
import org.intellij.sdk.language.AutoWireQuickFix

class AutoWireAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element.elementType!= GoTypes.IDENTIFIER){
            return
        }

        if (element.parent.elementType != GoTypes.FUNCTION_DECLARATION){
            return
        }
        println(element.parent.text)
        holder.newAnnotation(HighlightSeverity.INFORMATION, "Auto wire")
//                .range(psiMethod.getNameIdentifier())
                .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                .withFix(AutoWireQuickFix(element.text))
                .create();
    }
}