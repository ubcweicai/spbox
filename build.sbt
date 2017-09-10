import com.github.play2war.plugin._

name := """WeTicket"""

version := "1.0-BETA"

Play2WarPlugin.play2WarSettings

Play2WarKeys.servletVersion := "3.1"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "mysql" % "mysql-connector-java" % "5.1.27",
  "com.google.zxing" % "core" % "3.2.0",
  "com.stripe" % "stripe-java" % "1.31.0",
  "com.typesafe.play" %% "play-mailer" % "2.4.1"
)


PlayKeys.playWatchService := play.sbtplugin.run.PlayWatchService.sbt(pollInterval.value)
