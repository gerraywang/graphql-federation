/*
 * This file was generated by the Gradle 'init' task.
 *
 * The settings file is used to specify which projects to include in your build.
 * For more detailed information on multi-project builds, please refer to https://docs.gradle.org/8.13/userguide/multi_project_builds.html in the Gradle documentation.
 * This project uses @Incubating APIs which are subject to change.
 */

rootProject.name = "graphql-services"

include(":dynamic_graphql:subgraphs:common")
include(":dynamic_graphql:subgraphs:mst")
include(":dynamic_graphql:subgraphs:tran")
include(":dynamic_graphql:api")

include(":static_graphql:subgraphs:common")
include(":static_graphql:subgraphs:mst")
include(":static_graphql:subgraphs:tran")
include(":static_graphql:api")
