val scala211 = "2.11.8"
val scala212 = "2.12.1"

val scalazVersion = "7.2.10"
val circeVersion = "0.7.0"

scalaVersion := scala212

import scalariform.formatter.preferences._

lazy val commonSettings = Seq(
  scalaVersion := scala212,
  crossScalaVersions := Seq(scala211, scala212),
  organization := "org.danielnixon.progressive",
  licenses := Seq("GNU General Public License, Version 3" -> url("http://www.gnu.org/licenses/gpl-3.0.html")),
  version := "0.16.0-SNAPSHOT",
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
    "-Xlint:_",
    "-Xfatal-warnings",
    "-Xfuture",
    "-Ywarn-adapted-args",
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
    Wart.TraversableOps,
    Wart.TryPartial,
    Wart.Var,
    Wart.While,
    ExtraWart.DateFormatPartial,
    ExtraWart.EnumerationPartial,
    ExtraWart.FutureObject,
    ExtraWart.GenMapLikePartial,
    ExtraWart.GenTraversableLikeOps,
    ExtraWart.GenTraversableOnceOps,
    ExtraWart.LegacyDateTimeCode,
    ExtraWart.ScalaGlobalExecutionContext,
    ExtraWart.StringOpsPartial,
    ExtraWart.TraversableOnceOps,
    ExtraWart.UntypedEquality)
)

lazy val server = (project in file("server")).
  settings(commonSettings: _*).
  settings(
    name := "progressive-server",
    libraryDependencies ++= Seq(
      "org.scalaz" %%% "scalaz-core" % scalazVersion,
      "org.joda" % "joda-convert" % "1.8.1" // TODO: Compilation fails without this...
    )
  ).
  disablePlugins(ScalaJSPlugin, ScalaJSWarts, PlayWarts).
  dependsOn(sharedJvm)

lazy val serverPlay = (project in file("server-play")).
  settings(commonSettings: _*).
  settings(
    name := "progressive-server-play",
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play" % "2.6.0-M2"
    ),
    wartremoverErrors ++= Seq(
      PlayWart.AssetsObject,
      PlayWart.CookiesPartial,
      PlayWart.FlashPartial,
      PlayWart.FormPartial,
      PlayWart.HeadersPartial,
      PlayWart.JavaApi,
      PlayWart.JsLookupResultPartial,
      PlayWart.JsReadablePartial,
      PlayWart.LangObject,
      PlayWart.MessagesObject,
      PlayWart.PlayGlobalExecutionContext,
      PlayWart.SessionPartial)
  ).
  enablePlugins(PlayWarts).
  disablePlugins(ScalaJSPlugin, ScalaJSWarts).
  dependsOn(sharedJvm, server)

lazy val client = (project in file("client")).
  settings(commonSettings: _*).
  settings(
    name := "progressive-client",
    libraryDependencies ++= Seq(
      "org.danielnixon" %%% "safer-dom" % "0.3.0",
      "org.scalaz" %%% "scalaz-core" % scalazVersion,
      "org.scalatest" %%% "scalatest" % "3.0.1" % Test
    ),
    jsDependencies ++= Seq(
      "org.webjars.npm" % "virtual-dom" % "2.1.1" / "virtual-dom.js",
      "org.webjars.npm" % "vdom-parser" % "1.3.4" / "dist.js",
      RuntimeDOM % Test
    ),
    jsEnv := JSDOMNodeJSEnv().value,
    wartremoverErrors ++= Seq(
      ScalaJSWart.ArrayPartial,
      ScalaJSWart.UndefOrOpsPartial
    )
  ).
  enablePlugins(ScalaJSPlugin).
  disablePlugins(ScoverageSbtPlugin, PlayWarts). // TODO https://github.com/scoverage/sbt-scoverage/issues/101
  dependsOn(sharedJs)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
  settings(commonSettings: _*).
  settings(
    name := "progressive-shared",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "scalatags" % "0.6.3",
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion
    )
  ).
  disablePlugins(ScalaJSWarts, PlayWarts).
  disablePlugins(ScoverageSbtPlugin) // TODO https://github.com/scoverage/sbt-scoverage/issues/101

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js