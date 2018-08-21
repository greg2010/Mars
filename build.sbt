name := "Mars"

version := "0.1"

scalaVersion := "2.12.6"

scalacOptions := Seq("-unchecked", "-deprecation")


/*
 * ASSEMBLY PLUGIN
 */

assemblyJarName in assembly := "mars.jar"
mainClass in assembly := Some("org.kys.mars.Processor")
val meta = """.*(RSA|DSA)$""".r
// Hax to get .jar to execute
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) =>
    xs.map(_.toLowerCase) match {
      case ("manifest.mf" :: Nil) |
           ("index.list" :: Nil) |
           ("dependencies" :: Nil) |
           ("bckey.dsa" :: Nil) => MergeStrategy.discard
      case _ => MergeStrategy.first
    }
  case PathList("reference.conf") | PathList("application.conf") => MergeStrategy.concat
  case PathList(_*) => MergeStrategy.first
}


resolvers += "Spring-plugins" at "http://repo.spring.io/plugins-release/"

val http4sVersion = "0.18.11"
val circeVersion = "0.9.3"
val enumeratumVersion = "1.5.13"
val ackcordVersion = "0.10.0"
libraryDependencies ++= Seq(
  "com.github.pureconfig" %% "pureconfig" % "0.9.1",
  "com.github.pureconfig" %% "pureconfig-enumeratum" % "0.9.1",
  "org.rogach" %% "scallop" % "3.1.2",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.github.pukkaone" % "logback-gelf" % "1.1.10",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-literal" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-literal" % circeVersion,
  "io.circe" %% "circe-java8" % circeVersion,
  "io.circe" %% "circe-yaml" % "0.8.0",
  "com.beachape" %% "enumeratum" % enumeratumVersion,
  "com.beachape" %% "enumeratum-circe" % enumeratumVersion,
  "io.monix" %% "monix" % "3.0.0-RC1",
  "com.softwaremill.sttp" %% "async-http-client-backend-monix" % "1.1.14",
  "com.softwaremill.sttp" %% "core" % "1.1.14",
  "com.softwaremill.sttp" %% "circe" % "1.1.14",
  "net.katsstuff" %% "ackcord" % ackcordVersion,
  "net.katsstuff" %% "ackcord-core" % ackcordVersion,
  "net.katsstuff" %% "ackcord-commands-core" % ackcordVersion,
  "com.softwaremill.retry" %% "retry" % "0.3.0",
  "com.github.etaty" %% "rediscala" % "1.8.0")

addCompilerPlugin(
  "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full
)