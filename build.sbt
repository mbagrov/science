import sbtassembly.MergeStrategy

enablePlugins(JavaAppPackaging, SbtTwirl)

name         := """electronic-queue"""
organization := "com.theiterators"
version      := "0.1"
scalaVersion := "2.11.7"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV       = "2.3.12"
  val akkaStreamV = "2.0.1"
  val scalaTestV  = "2.2.5"
  val jodatime = "joda-time" % "joda-time" % "2.9.3"
  val posgresDriver = "postgresql" % "postgresql" % "9.1-901.jdbc4"
  val squerylLib = "org.squeryl" %% "squeryl" % "0.9.5-7"
  val guice = Seq(("com.google.inject.extensions" % "guice-multibindings" % "3.0"),
    "com.tzavellas" % "sse-guice" % "0.7.0")
  val liquibase = "org.liquibase" % "liquibase-core" % "3.4.2"
  val mustache = "com.github.spullara.mustache.java" % "compiler" % "0.8.12"

  Seq(
    "com.typesafe.akka" %% "akka-actor"                           % akkaV,
    "com.typesafe.akka" %% "akka-stream-experimental"             % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-core-experimental"          % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-experimental"               % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental"    % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-testkit-experimental"       % akkaStreamV,
    "org.scalatest"     %% "scalatest"                            % scalaTestV % "test",
    "org.apache.commons" % "commons-csv" % "1.1",
    "commons-validator" % "commons-validator" % "1.4.0",
    "com.typesafe.akka" %% "akka-remote"                          % akkaV,
    "com.typesafe.akka" % "akka-slf4j_2.11" % akkaV,
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "commons-io" % "commons-io" % "2.4",
    "org.apache.commons" % "commons-lang3" % "3.1",
    "org.firebirdsql.jdbc" % "jaybird-jdk17" % "2.2.8",
    "net.sf.ehcache" % "ehcache" %  "2.10.1",
    squerylLib,
    posgresDriver,
    jodatime,
    liquibase,
    mustache
  ) ++ guice ++ Seq("org.apache.commons" % "commons-email" % "1.3.1",
    "org.jsoup" % "jsoup" % "1.7.1")
}

lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

compileScalastyle := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value

(compile in Compile) <<= (compile in Compile) dependsOn compileScalastyle

assemblyMergeStrategy in assembly := {
    case x if Assembly.isConfigFile(x) =>
        MergeStrategy.concat
    case PathList(ps @ _*) if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
        MergeStrategy.rename
    case PathList("net", "sf", "cglib", xs @ _*) => MergeStrategy.last
    case PathList("META-INF", xs @ _*) =>
      (xs map {_.toLowerCase}) match {
        case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
          MergeStrategy.discard
        case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
          MergeStrategy.discard
        case "plexus" :: xs =>
          MergeStrategy.discard
        case "services" :: xs =>
          MergeStrategy.filterDistinctLines
        case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
          MergeStrategy.filterDistinctLines
        case _ => MergeStrategy.deduplicate
      }
    case _ => MergeStrategy.deduplicate
}

Revolver.settings
