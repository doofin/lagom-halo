organization in ThisBuild := "com.example"
version in ThisBuild := "1.0-SNAPSHOT"
name := "lagom-helo"
scalaVersion in ThisBuild := "2.12.4"
val akkaV = "2.5.14"
val akkaHttpV = "10.1.2"
val deps = Seq("com.typesafe.akka" %% "akka-http" % akkaHttpV,
               "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided",
               "org.scalatest" %% "scalatest" % "3.0.4" % Test)

val akkaTypedDeps = Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaV,
  "com.typesafe.akka" %% "akka-cluster-typed" % akkaV,
  "com.typesafe.akka" %% "akka-stream-typed" % akkaV,
  "com.typesafe.akka" %% "akka-cluster-sharding-typed" % akkaV
)

lazy val `hello` = (project in file("."))
  .aggregate(`hello-api`, `hello-impl`, hellostream_api, hello_stream)

lazy val `hello-api` = (project in file("hello-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `hello-impl` = (project in file("hello-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit
    ) ++ deps
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`hello-api`)

lazy val hellostream_api = (project in file("hello-stream-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val hello_stream = (project in file("hello-stream-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslTestKit
    ) ++ deps ++ akkaTypedDeps
  )
  .dependsOn(hellostream_api, `hello-api`)

//https://github.com/lagom/online-auction-scala/blob/master/build.sbt
lazy val http = (project in file("http"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslServer
    ) ++ deps ++ akkaTypedDeps
  )
  .dependsOn(hellostream_api, `hello-api`)
