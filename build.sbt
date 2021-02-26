name := "Programarea in timp real"

version := "1.0"

scalaVersion := "2.13.1"

lazy val akkaVersion = "2.6.11"
val AkkaHttpVersion = "10.2.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.1.0" % Test,

  "com.lightbend.akka" %% "akka-stream-alpakka-sse" % "2.0.2",
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.lihaoyi" %% "upickle" % "0.9.5"
)
libraryDependencies += "com.typesafe.akka" %% "akka-http-jackson" % AkkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion
