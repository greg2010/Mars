name := "Mars"

version := "0.1"

scalaVersion := "2.12.6"

val http4sVersion = "0.18.11"
libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.1",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.github.pukkaone" % "logback-gelf" % "1.1.10",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "io.circe" %% "circe-generic" % "0.9.3",
  "io.circe" %% "circe-literal" % "0.9.3",
  "com.beachape" %% "enumeratum" % "1.5.13",
  "com.beachape" %% "enumeratum-circe" % "1.5.17",
  "io.monix" %% "monix" % "3.0.0-RC1",
  "com.softwaremill.sttp" %% "async-http-client-backend-monix" % "1.1.14",
  "com.softwaremill.sttp" %% "core" % "1.1.14",
  "com.softwaremill.sttp" %% "circe" % "1.1.14")