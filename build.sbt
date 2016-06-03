
val baseSettings = Seq(
  organization := "com.ruiandrebatista",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.11.8",
  scalacOptions in Compile ++= Seq(
    "-encoding", "UTF-8",
    "-deprecation",
    "-unchecked",
    "-feature",
    "-Xlint",
//    "-Ywarn-unused-import",
    "-language:implicitConversions",
    "-language:postfixOps",
    "-Xmax-classfile-name", "255" //due to pickling macros
  ),
licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")))

lazy val root = (project in file("."))
  .settings(baseSettings:_*)
  .settings(name := "dbio-cats",
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats" % "0.6.0",
    "com.typesafe.slick" %% "slick" % "3.1.1",
    compilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1"),
    "org.scalatest" %% "scalatest" % "2.2.6" % "test",
    "com.h2database" % "h2" % "1.4.192" % "test"))

