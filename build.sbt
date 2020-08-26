name := "http-tls-client"

version := "0.1"

scalaVersion := "2.12.11"

ThisBuild / resolvers ++= Seq(
  "Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/"
)

libraryDependencies ++= Seq (
  "org.apache.httpcomponents" % "httpcore" % "4.4.13",
  "org.apache.httpcomponents" % "httpclient" % "4.5.12",
  "org.json4s" %% "json4s-native" % "3.6.1",
  "org.scalaj" %% "scalaj-http" % "2.4.2",
  "com.typesafe" % "config" % "1.3.3",
  "org.json4s" %% "json4s-native" % "3.6.1",
  "org.slf4j" % "slf4j-log4j12" % "1.7.15"
)
