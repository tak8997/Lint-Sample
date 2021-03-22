package com.tak8997.github.lint_rules

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.android.tools.lint.detector.api.getMethodName
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UBlockExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UReturnExpression
import org.jetbrains.uast.asRecursiveLogString
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.isUastChildOf
import org.jetbrains.uast.visitor.AbstractUastVisitor

class LifecycleOwnerDetector : Detector(), SourceCodeScanner {

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        println(context.uastFile?.asRecursiveLogString())
        return super.createUastHandler(context)
    }

    override fun getApplicableMethodNames(): List<String>? {
        return listOf("setContentView")
    }

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (!context.evaluator.isMemberInClass(method, "androidx.databinding.DataBindingUtil")) {
            return
        }

        val surroundingDeclaration: UElement = node.getParentOfType(
            true,
            UMethod::class.java,
            UBlockExpression::class.java,
            ULambdaExpression::class.java
        ) ?: return

        val parent: UElement? = node.uastParent
        if (parent?.uastParent is UCallExpression)
            return

        val finder = LifecycleOwnerFinder(node)
        surroundingDeclaration.accept(finder)
        if (!finder.isLifecycleOwnerCalled()) {
            context.report(
                ISSUE,
                node,
                context.getCallLocation(
                    node,
                    includeReceiver = true,
                    includeArguments = true
                ),
                "DataBindingUtil.setContentView created but setLifecycleOwner() is not called. " +
                        "Did you forget to call 'setLifecycleOwner()'?"
            )
        }
    }

    private class LifecycleOwnerFinder(val target: UCallExpression) : AbstractUastVisitor() {

        private var found = false
        private var seenTarget = false

        override fun visitCallExpression(node: UCallExpression): Boolean {
            if (node == target || node.psi != null && node.psi == target.psi) {
                seenTarget = true
            } else {
                if ((seenTarget || target.equals(node.receiver))) {
                    if ("lifecycleOwner" == getMethodName(node)
                        || "setLifecycleOwner" == getMethodName(node)
                    ) {
                        found = true
                    }
                }
            }

            return super.visitCallExpression(node)
        }

        override fun visitReturnExpression(node: UReturnExpression): Boolean {
            if (target.isUastChildOf(node.returnExpression, true)) {
                found = true
            }

            return super.visitReturnExpression(node)
        }

        fun isLifecycleOwnerCalled(): Boolean {
            return found
        }
    }

    companion object {
        val ISSUE = Issue.create(
            id = "DataBindingDetector",
            briefDescription = "LifecycleOwner must be set.",
            explanation = "If you use DataBinding with LiveData, you must set LifecycleOwner",
            category = Category.CORRECTNESS,
            priority = 8,
            severity = Severity.WARNING,
            implementation = Implementation(LifecycleOwnerDetector::class.java, Scope.JAVA_FILE_SCOPE)
        )
    }
}