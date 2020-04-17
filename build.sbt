
// Building both for JVM and JavaScript runtimes.

// To convince SBT not to publish any root level artifacts, I had a look at how scala-java-time does it.
// See https://github.com/cquiroz/scala-java-time/blob/master/build.sbt as a "template" for this build file.


// shadow sbt-scalajs' crossProject and CrossType from Scala.js 0.6.x

import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val scalaVer = "2.13.1"
val crossScalaVer = Seq(scalaVer)

lazy val commonSettings = Seq(
  name         := "tqa2",
  description  := "XBRL taxonomy query API, using a locator-free taxonomy model",
  organization := "eu.cdevreeze.tqa2",
  version      := "0.2.0-SNAPSHOT",

  scalaVersion       := scalaVer,
  crossScalaVersions := crossScalaVer,

  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings", "-Xlint"),

  Test / publishArtifact := false,
  publishMavenStyle := true,

  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },

  pomExtra := pomData,
  pomIncludeRepository := { _ => false },

  libraryDependencies += "eu.cdevreeze.yaidom2" %%% "yaidom2" % "0.10.0",

  libraryDependencies += "eu.cdevreeze.xpathparser" %%% "xpathparser" % "0.6.1",

  libraryDependencies += "org.scala-lang.modules" %%% "scala-xml" % "2.0.0-M1",

  libraryDependencies += "org.scala-lang.modules" %% "scala-parallel-collections" % "0.2.0",

  libraryDependencies += "org.scalatest" %%% "scalatest" % "3.1.1" % "test",

  libraryDependencies += "org.scalatestplus" %%% "scalacheck-1-14" % "3.1.1.1" % "test"
)

lazy val root = project.in(file("."))
  .aggregate(tqa2JVM, tqa2JS)
  .settings(commonSettings: _*)
  .settings(
    name                 := "tqa2",
    // Thanks, scala-java-time, for showing us how to prevent any publishing of root level artifacts:
    // No, SBT, we don't want any artifacts for root. No, not even an empty jar.
    publish              := {},
    publishLocal         := {},
    publishArtifact      := false,
    Keys.`package`       := file(""))

lazy val tqa2 = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("."))
  .settings(commonSettings: _*)
  .jvmSettings(
    // By all means, override this version of Saxon if needed, possibly with a Saxon-EE release!

    libraryDependencies += "net.sf.saxon" % "Saxon-HE" % "9.9.1-7",

    libraryDependencies += "com.github.ben-manes.caffeine" % "caffeine" % "2.8.1",

    libraryDependencies += "org.scalacheck" %%% "scalacheck" % "1.14.3" % "test"
  )
  .jsSettings(
    // Do we need this jsEnv?
    jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv(),

    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.0.0",

    Test / parallelExecution := false
  )

lazy val tqa2JVM = tqa2.jvm

lazy val tqa2JS = tqa2.js

lazy val pomData =
  <url>https://github.com/dvreeze/tqa2</url>
  <licenses>
    <license>
      <name>Apache License, Version 2.0</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      <distribution>repo</distribution>
      <comments>TQA2 is licensed under Apache License, Version 2.0</comments>
    </license>
  </licenses>
  <scm>
    <connection>scm:git:git@github.com:dvreeze/tqa2.git</connection>
    <url>https://github.com/dvreeze/tqa2.git</url>
    <developerConnection>scm:git:git@github.com:dvreeze/tqa2.git</developerConnection>
  </scm>
  <developers>
    <developer>
      <id>dvreeze</id>
      <name>Chris de Vreeze</name>
      <email>chris.de.vreeze@caiway.net</email>
    </developer>
  </developers>
