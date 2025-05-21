ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.5"

lazy val root = (project in file("."))
  .settings(
    name := "ai-protocol",
    libraryDependencies ++= Seq(
      "com.github.wangzaixiang" %% "wjson-core" % "0.5.0-RC2"
    )
  )
