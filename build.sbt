name := """hs-social"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "ws.securesocial" %% "securesocial" % "2.1.3",
  "org.jsoup" % "jsoup" % "1.7.3"
)


play.Project.playScalaSettings