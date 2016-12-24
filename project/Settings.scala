/*
 * =========================================================================================
 * Copyright © 2013-2016 the kamon project <http://kamon.io/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * =========================================================================================
 */

import sbt._
import Keys._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._
import Publish.{settings => publishSettings}
import Release.{settings => releaseSettings }
import sbt.Tests.{SubProcess, Group}

object Settings {

  val JavaVersion = "1.8"
  val ScalaVersion = "2.12.1"

  lazy val basicSettings = Seq(
    scalaVersion := ScalaVersion,
    resolvers ++= Dependencies.resolutionRepos,
    fork in run := true,
    parallelExecution in Global := false,
    javacOptions   := Seq(
      "-Xlint:-options",
      "-source", JavaVersion, "-target", JavaVersion),
    scalacOptions  := Seq(
      "-encoding",
      "utf8",
      "-g:vars",
      "-feature",
      "-unchecked",
      "-optimise",
      "-deprecation",
      "-language:postfixOps",
      "-language:implicitConversions",
      "-Xlog-reflective-calls"
    )
  ) ++ publishSettings ++ releaseSettings

  def singleTestPerJvm(tests: Seq[TestDefinition], jvmSettings: Seq[String]): Seq[Group] =
    tests map { test =>
      Group(
        name = test.name,
        tests = Seq(test),
        runPolicy = SubProcess(ForkOptions(runJVMOptions = jvmSettings)))
    }

  lazy val formatSettings = SbtScalariform.scalariformSettings ++ Seq(
    ScalariformKeys.preferences in Compile := formattingPreferences,
    ScalariformKeys.preferences in Test := formattingPreferences
  )

  lazy val assemblySettings = Assembly.settings
  lazy val notAggregateInAssembly = Assembly.notAggregateInAssembly
  lazy val excludeScalaLib = Assembly.excludeScalaLib
  lazy val agentSettings = Agent.settings

  def formattingPreferences =
    FormattingPreferences()
      .setPreference(RewriteArrowSymbols, true)
      .setPreference(AlignParameters, false)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(DoubleIndentClassDeclaration, true)
}
