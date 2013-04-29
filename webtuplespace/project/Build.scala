import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "webtuplespace"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "org.mongodb" %% "casbah" % "2.5.0",
    "org.json4s" %% "json4s-native" % "3.2.4"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
