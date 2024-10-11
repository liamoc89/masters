name := """masters-2025"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.15"

libraryDependencies += guice



libraryDependencies ++= Seq(
    "org.reactivemongo" %% "play2-reactivemongo" % "0.20.13-play27",
    "org.reactivemongo" %% "reactivemongo-play-json" % "0.20.13-play27",
    "org.mindrot" % "jbcrypt" % "0.4",
    "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test
)


// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
