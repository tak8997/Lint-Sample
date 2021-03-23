# Lint-Sample

This is a lint check on the parts that our team or I often make mistakes in writing code.



## Lint Rules

* [`LifecycleOwnerDetector`](https://github.com/tak8997/Lint-Sample/blob/master/lint-rules/src/main/java/com/tak8997/github/lint_rules/LifecycleOwnerDetector.kt) - If DataBinding and LiveData are used together, make sure to put the LifecycleOwner.
* [`ViewIdDetector`](https://github.com/tak8997/Lint-Sample/blob/master/lint-rules/src/main/java/com/tak8997/github/lint_rules/ViewIdDetector.kt) - Detect if id naming in the view complies with certain rules.

You can see examples in MainActivity.kt and activity_main.xml.



