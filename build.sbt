scalaVersion in ThisBuild := "2.11.8"
scalaOrganization in ThisBuild := "org.typelevel"

val http4sVersion = "0.16.0-cats-SNAPSHOT"
val circeVersion = "0.7.0"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats" % "0.9.0",
  "org.atnos" %% "eff" % "4.0.0",
  "org.atnos" %% "eff-fs2" % "4.0.0",
  "eu.timepit" %% "refined" % "0.8.0",
  "eu.timepit" %% "refined-pureconfig" % "0.8.0",
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "org.tpolecat" %% "doobie-core-cats" % "0.4.1",
  "ch.qos.logback" %  "logback-classic" % "1.2.1"
)

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3")

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",       // yes, this is 2 args
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",        // N.B. doesn't work well with the ??? hole
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Ypartial-unification"
)

fork in run := true
