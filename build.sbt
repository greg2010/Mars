name := "Mars"

version := "0.1"

scalaVersion := "2.12.6"

val http4sVersion = "0.18.11"
val circeVersion = "0.9.3"
val enumeratumVersion = "1.5.13"
libraryDependencies ++= Seq(
  "com.github.pureconfig" %% "pureconfig" % "0.9.1",
  "org.rogach" %% "scallop" % "3.1.2",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.github.pukkaone" % "logback-gelf" % "1.1.10",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-literal" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-literal" % circeVersion,
  "com.beachape" %% "enumeratum" % enumeratumVersion,
  "com.beachape" %% "enumeratum-circe" % enumeratumVersion,
  "com.beachape" %% "enumeratum" % "1.5.13",
  "com.beachape" %% "enumeratum-circe" % "1.5.17",
  "io.monix" %% "monix" % "3.0.0-RC1",
  "com.softwaremill.sttp" %% "async-http-client-backend-monix" % "1.1.14",
  "com.softwaremill.sttp" %% "core" % "1.1.14",
  "com.softwaremill.sttp" %% "circe" % "1.1.14")

addCompilerPlugin(
  "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full
)