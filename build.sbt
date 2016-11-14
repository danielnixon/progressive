val scala211 = "2.11.8"
val scala212 = "2.12.0"

val scalazVersion = "7.2.7"
val circeVersion = "0.6.0"

import scalariform.formatter.preferences._

lazy val commonSettings = Seq(
  organization := "org.danielnixon.progressive",
  licenses := Seq("GNU General Public License, Version 3" -> url("http://www.gnu.org/licenses/gpl-3.0.html")),
  version := "0.7.0",
  publishMavenStyle := true,
  publishArtifact in Test := false,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value) {
      Some("snapshots" at nexus + "content/repositories/snapshots")
    } else {
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
    }
  },
  homepage := Some(url("https://github.com/danielnixon/progressive")),
  pomExtra := {
    <scm>
      <url>git@github.com:danielnixon/progressive.git</url>
      <connection>scm:git:git@github.com:danielnixon/progressive.git</connection>
    </scm>
      <developers>
        <developer>
          <id>danielnixon</id>
          <name>Daniel Nixon</name>
          <url>https://danielnixon.org/</url>
        </developer>
      </developers>
  },
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-feature",
    "-language:postfixOps",
    "-language:implicitConversions",
    "-Xlint:_",
    "-Xfatal-warnings",
    "-Ywarn-inaccessible",
    "-Ywarn-unused",
    "-Ywarn-unused-import",
    "-Ywarn-numeric-widen",
    "-Ywarn-nullary-override"),
  // scalariform
  scalariformPreferences := scalariformPreferences.value
    .setPreference(DoubleIndentClassDeclaration, true)
    .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, true),
  // wartremover
  wartremoverErrors ++= Seq(
    Wart.Any,
    Wart.AsInstanceOf,
    Wart.IsInstanceOf,
    Wart.EitherProjectionPartial,
    Wart.Enumeration,
    Wart.Equals,
    Wart.ExplicitImplicitTypes,
    Wart.FinalCaseClass,
    Wart.JavaConversions,
    Wart.LeakingSealed,
    Wart.ListOps,
    Wart.MutableDataStructures,
    Wart.Nothing,
    Wart.Null,
    Wart.OptionPartial,
    Wart.Product,
    Wart.Return,
    Wart.Serializable,
    Wart.StringPlusAny,
    Wart.Throw,
    Wart.ToString,
    Wart.TryPartial,
    Wart.Var,
    Wart.While)
)

lazy val root = Project(
  id = "root",
  base = file("."),
  aggregate = Seq(server, serverPlay, client, sharedJs, sharedJvm)
).settings(commonSettings: _*).settings(publishArtifact := false)

lazy val server = (project in file("server")).
  settings(commonSettings: _*).
  settings(
    scalaVersion := scala211,
    crossScalaVersions := Seq(scala211, scala212),
    name := "progressive-server",
    libraryDependencies ++= Seq(
      "org.scalaz" %%% "scalaz-core" % scalazVersion
    )
  ).
  dependsOn(sharedJvm)

lazy val serverPlay = (project in file("server-play")).
  settings(commonSettings: _*).
  settings(
    scalaVersion := scala211,
    crossScalaVersions := Seq(scala211),
    name := "progressive-server-play",
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play" % "2.5.9",
      "com.typesafe.play" %% "twirl-api" % "1.3.0"
    ),
    dependencyOverrides += "com.typesafe.play" %% "twirl-api" % "1.3.0"
  ).
  dependsOn(sharedJvm, server)

lazy val client = (project in file("client")).
  settings(commonSettings: _*).
  settings(
    scalaVersion := scala211,
    crossScalaVersions := Seq(scala211, scala212),
    name := "progressive-client",
    persistLauncher := true,
    persistLauncher in Test := false,
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.1",
      "org.scalaz" %%% "scalaz-core" % scalazVersion,
      "org.scalatest" %%% "scalatest" % "3.0.1" % Test
    ),
    jsDependencies ++= Seq(
      "org.webjars.npm" % "virtual-dom" % "2.1.1" / "virtual-dom.js",
      "org.webjars.npm" % "vdom-parser" % "1.3.4" / "dist.js",
      RuntimeDOM % Test
    ),
    jsEnv := JSDOMNodeJSEnv().value
  ).
  enablePlugins(ScalaJSPlugin).
  disablePlugins(ScoverageSbtPlugin). // TODO https://github.com/scoverage/sbt-scoverage/issues/101
  dependsOn(sharedJs)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
  settings(commonSettings: _*).
  settings(
    scalaVersion := scala211,
    crossScalaVersions := Seq(scala211, scala212),
    name := "progressive-shared",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "scalatags" % "0.6.2",
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion
    )
  ).
  disablePlugins(ScoverageSbtPlugin) // TODO https://github.com/scoverage/sbt-scoverage/issues/101

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js