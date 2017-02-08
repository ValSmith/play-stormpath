name := "PlayStormpath"

version := "1.0"

PlayKeys.devSettings := Seq("play.server.http.port" -> "disabled", "play.server.https.port" -> "9000")

lazy val PlayStormpath = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.webjars" % "angularjs" % "1.5.8",
  "org.webjars" % "angular-ui-router" % "0.2.10",
  "org.webjars" % "angular-ui-bootstrap" % "1.3.3",
  "org.webjars" % "bootstrap" % "3.3.7-1",
  "com.stormpath.sdk" % "stormpath-sdk-api" % "1.1.1",
  "com.stormpath.sdk" % "stormpath-sdk-httpclient" % "1.1.1",
  "org.webjars" % "jquery" % "3.1.1",
  "org.webjars" % "lodash" % "4.15.0",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.3",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.8.3"
)

