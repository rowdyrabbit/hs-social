name := """hs-social"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  filters,
  "org.jsoup" % "jsoup" % "1.7.3"
)


play.Project.playScalaSettings