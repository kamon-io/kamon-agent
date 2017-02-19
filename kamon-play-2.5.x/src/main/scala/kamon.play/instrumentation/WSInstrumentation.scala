/*
 * =========================================================================================
 * Copyright © 2013-2017 the kamon project <http://kamon.io/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * =========================================================================================
 */

package kamon.play.instrumentation

import kamon.agent.scala.KamonInstrumentation
import kamon.metric.instrument.InstrumentFactory
import kamon.metric.{EntityRecorderFactory, GenericEntityRecorder}
import kamon.play.instrumentation.interceptor.WSInterceptor

class WSInstrumentation extends KamonInstrumentation {

  val ExecuteMethod = named("execute").and(takesArguments(0))

  forSubtypeOf("play.api.libs.ws.WSRequest") { builder =>
    builder
      .withTransformationFor(ExecuteMethod, classOf[WSInterceptor])
      .build()
  }
}

/**
  *  Counts HTTP response status codes into per status code and per trace name + status counters. If recording a HTTP
  *  response with status 500 for the trace "http://localhost:9000/badoutside_200", the counter with name "500" as
  *  well as the counter with name "http://localhost:9000/badoutside_500" will be incremented.
  */
class HttpClientMetrics(instrumentFactory: InstrumentFactory) extends GenericEntityRecorder(instrumentFactory) {

  def recordResponse(statusCode: String): Unit = {
    counter(statusCode).increment()
  }

  def recordResponse(traceName: String, statusCode: String): Unit = {
    recordResponse(statusCode)
    counter(traceName + "_" + statusCode).increment()
  }
}

object HttpClientMetrics extends EntityRecorderFactory[HttpClientMetrics] {
  def category: String = "http-client"
  def createRecorder(instrumentFactory: InstrumentFactory): HttpClientMetrics = new HttpClientMetrics(instrumentFactory)
}
