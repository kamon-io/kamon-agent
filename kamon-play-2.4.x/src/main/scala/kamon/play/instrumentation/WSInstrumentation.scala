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
import kamon.play.instrumentation.interceptor.WSInterceptor

class WSInstrumentation extends KamonInstrumentation {

  val ExecuteMethod = named("execute").and(takesArguments(0))

  forSubtypeOf("play.api.libs.ws.WSRequest") { builder =>
    builder
      .withTransformationFor(ExecuteMethod, classOf[WSInterceptor])
      .build()
  }
}
