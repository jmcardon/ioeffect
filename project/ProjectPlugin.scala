import sbt._
import sbt.Keys._

import fommil.SensiblePlugin.autoImport._
import fommil.SonatypePlugin.autoImport._
import sbtdynver.DynVerPlugin.autoImport._
import org.scalafmt.sbt.ScalafmtPlugin, ScalafmtPlugin.autoImport._
import scalafix.sbt.ScalafixPlugin, ScalafixPlugin.autoImport._

object ProjectKeys {
  def KindProjector =
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6")

  def MonadicFor =
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.1.0")

  private val silencerVersion = "0.6"
  def Silencer =
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" %% "silencer-plugin" % silencerVersion),
      "com.github.ghik" %% "silencer-lib" % silencerVersion % "provided"
    )

  def extraScalacOptions(scalaVersion: String) =
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 12)) =>
        Seq(
          "-Ywarn-unused:explicits,patvars,imports,privates,locals,implicits",
          "-opt:l:method,inline",
          "-opt-inline-from:scalaz.**",
          "-opt-inline-from:xmlformat.**"
        )
      case _ =>
        Seq("-Xexperimental")
    }
}

object ProjectPlugin extends AutoPlugin {

  override def requires =
    fommil.SensiblePlugin && fommil.SonatypePlugin && ScalafmtPlugin && ScalafixPlugin
  override def trigger = allRequirements

  val autoImport = ProjectKeys
  import autoImport._

  override def buildSettings =
    Seq(
      organization := "org.scalaz",
      crossScalaVersions := Seq("2.12.4", "2.11.12"),
      scalaVersion := crossScalaVersions.value.head,
      sonatypeGithost := (Github, "scalaz", "effect"),
      sonatypeDevelopers := List("John de Goes"),
      licenses := Seq("BSD3" -> url("https://opensource.org/licenses/BSD-3-Clause")),
      startYear := Some(2017),
      scalafmtConfig := Some(file("project/scalafmt.conf")),
      scalafixConfig := Some(file("project/scalafix.conf"))
    )

  override def projectSettings =
    Seq(
      MonadicFor,
      Silencer,
      scalacOptions ++= Seq(
        "-language:_",
        "-unchecked",
        "-explaintypes",
        "-Ywarn-value-discard",
        "-Ywarn-numeric-widen",
        "-Ywarn-dead-code",
        "-Ypartial-unification",
        "-Xlog-free-terms",
        "-Xlog-free-types",
        "-Xlog-reflective-calls",
        "-Yrangepos",
        "-Xexperimental" // SAM types in 2.11
      ),
      scalacOptions ++= extraScalacOptions(scalaVersion.value)
    )
}