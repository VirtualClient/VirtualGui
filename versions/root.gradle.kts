import gg.essential.gradle.util.*

plugins {
    id("gg.essential.multi-version.root")
    id("gg.essential.multi-version.api-validation")
}

version = versionFromBuildIdAndBranch()

preprocess {
    val version11900 = createNode("1.19-fabric", 11900, "yarn")
    val fabric11802 = createNode("1.18.2-fabric", 11802, "yarn")
    val fabric11701 = createNode("1.17.1-fabric", 11701, "yarn")
    val fabric11605 = createNode("1.16.4-fabric", 11604, "yarn")

    version11900.link(fabric11802)
    fabric11802.link(fabric11701)
    fabric11701.link(fabric11605)
}

apiValidation {
    ignoredProjects.addAll(subprojects.map { it.name })
    ignoredPackages.add("com.example")
    nonPublicMarkers.add("org.jetbrains.annotations.ApiStatus\$Internal")
}
