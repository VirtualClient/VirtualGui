import gg.virtualclient.gradle.util.*

plugins {
    id("gg.virtualclient.multi-version.root")
    id("gg.virtualclient.multi-version.api-validation")
}

version = versionFromBuildIdAndBranch()

preprocess {
    val version12000 = createNode("1.20.0-fabric", 12000, "yarn")
    val version11904 = createNode("1.19.4-fabric", 11904, "yarn")
    val version11903 = createNode("1.19.3-fabric", 11903, "yarn")
    val version11900 = createNode("1.19-fabric", 11900, "yarn")
    val fabric11802 = createNode("1.18.2-fabric", 11802, "yarn")
    val fabric11701 = createNode("1.17.1-fabric", 11701, "yarn")
    val fabric11605 = createNode("1.16.5-fabric", 11605, "yarn")

    version12000.link(version11904)
    version11904.link(version11903)
    version11903.link(version11900, file("1.19.3-1.19.2.txt"))
    version11900.link(fabric11802)
    fabric11802.link(fabric11701)
    fabric11701.link(fabric11605)
}

apiValidation {
    ignoredProjects.addAll(subprojects.map { it.name })
    ignoredPackages.add("com.example")
    nonPublicMarkers.add("org.jetbrains.annotations.ApiStatus\$Internal")
}
