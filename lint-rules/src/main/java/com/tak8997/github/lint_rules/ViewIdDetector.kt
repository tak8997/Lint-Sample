package com.tak8997.github.lint_rules

import com.android.tools.lint.detector.api.*
import org.w3c.dom.Attr

class ViewIdDetector : LayoutDetector() {

    override fun getApplicableAttributes(): Collection<String>? {
        return listOf("id")
    }

    override fun visitAttribute(context: XmlContext, attribute: Attr) {
        val viewName = attribute.ownerElement.tagName
        val viewRawId = attribute.value
        val findView = View.findView(viewName, viewRawId)

        if (!findView) {
            context.report(
                ISSUE,
                attribute,
                context.getLocation(attribute),
                "Naming convention is not correct."
            )
        }
    }

    /**
     * Put the desired view and id prefix
     */
    enum class View(val prefix: String) {
        Button("btn"),
        ImageView("img"),
        TextView("txt"),
        LinearLayout("lyt"),
        ConstraintLayout("lyt")
        ;

        companion object {
            fun findView(viewName: String, viewRawId: String): Boolean {
                return values().firstOrNull { it.name == viewName }
                    ?.let {
                        val ids = viewRawId.split("/")
                        if (ids.size < 2) {
                            return false
                        }

                        val idName = ids[1]
                        idName.startsWith(it.prefix)
                    } ?: false
            }
        }
    }

    companion object {
        val ISSUE = Issue.create(
            id = "ViewIdDetector",
            briefDescription = "View id must be written in the correct format.",
            explanation = "View id must be written in the correct format. It must be written according to the established rules",
            category = Category.CORRECTNESS,
            priority = 10,
            severity = Severity.ERROR,
            implementation = Implementation(ViewIdDetector::class.java, Scope.RESOURCE_FILE_SCOPE)
        )
    }
}