
lazy val root = project
  .in(file("."))
  .settings(
    skip in publish := true
  )
  .aggregate(js, jvm)

val sonatypePublish = Seq(
  releaseCrossBuild := true,
  publishMavenStyle := true,
  publishArtifact.in(Test) := false,
  pomIncludeRepository := Function.const(false),
  homepage := Some(url("https://github.com/lihaoyi/scala.rx")),
  sonatypeProfileName := "io.github.voltir",
  licenses += ("MIT license", url(
    "http://www.opensource.org/licenses/mit-license.php")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/Voltir/scala.rx"),
      "scm:git:git@github.com:Voltir/scala.rx.git"
    )),
  developers := List(
    Developer(
      "lihaoyi",
      "Li Haoyi",
      "",
      url("https://github.com/lihaoyi")
    ),
    Developer(
      "voltir",
      "Nick Childers",
      "voltir42@gmail.com",
      url("https://github.com/Voltir")
    )
  ),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }
)

lazy val scalarx = crossProject.settings(
  organization := "io.github.voltir",
  organizationName := "Nick Childers",
  name := "scalarx",
  crossScalaVersions := Seq("2.11.12", "2.12.4"),
  version := "0.3.3-SNAPSHOT",
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
    "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
    "com.lihaoyi" %%% "utest" % "0.6.0" % "test",
    "com.lihaoyi" %% "acyclic" % "0.1.7" % "provided"
  ) ++ (
    CrossVersion.partialVersion(scalaVersion.value) match {
      // if scala 2.11+ is used, quasiquotes are merged into scala-reflect
      case Some((2, scalaMajor)) if scalaMajor >= 11 =>
        Nil
      // in Scala 2.10, quasiquotes are provided by macro paradise
      case Some((2, 10)) =>
        Seq(
          compilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full),
          "org.scalamacros" %% "quasiquotes" % "2.0.0" cross CrossVersion.binary)
    }
  ),
  addCompilerPlugin("com.lihaoyi" %% "acyclic" % "0.1.7"),
  testFrameworks += new TestFramework("utest.runner.Framework"),
  autoCompilerPlugins := true,

  scalacOptions ++=
    "-encoding" :: "UTF-8" ::
    "-unchecked" ::
    "-deprecation" ::
    "-explaintypes" ::
    "-feature" ::
    "-language:_" ::
    "-Xcheckinit" ::
    "-Xfuture" ::
    "-Xlint" ::
    "-Ypartial-unification" ::
    "-Yno-adapted-args" ::
    "-Ywarn-infer-any" ::
    "-Ywarn-nullary-override" ::
    "-Ywarn-nullary-unit" ::
    Nil,

  // Sonatype
  publishTo := Some("releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"),
  pomExtra :=
    <url>https://github.com/lihaoyi/scalatags</url>
      <licenses>
        <license>
          <name>MIT license</name>
          <url>http://www.opensource.org/licenses/mit-license.php</url>
        </license>
      </licenses>
      <scm>
        <url>git://github.com/lihaoyi/scalatags.git</url>
        <connection>scm:git://github.com/lihaoyi/scalatags.git</connection>
      </scm>
      <developers>
        <developer>
          <id>lihaoyi</id>
          <name>Li Haoyi</name>
          <url>https://github.com/lihaoyi</url>
        </developer>
      </developers>
).jsSettings(sonatypePublish)
 .jsSettings(
  scalaJSStage in Test := FullOptStage,
  scalacOptions ++= (if (isSnapshot.value) Seq.empty else Seq({
    val a = baseDirectory.value.toURI.toString.replaceFirst("[^/]+/?$", "")
    val g = "https://raw.githubusercontent.com/lihaoyi/scala.rx"
    s"-P:scalajs:mapSourceURI:$a->$g/v${version.value}/"
  }))
 )
 .jvmSettings(sonatypePublish)

lazy val js = scalarx.js
lazy val jvm = scalarx.jvm
