/*
 * =========================================================================================
 * Copyright © 2013-2017 the kamon project <http://kamon.io/>
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

package akka.kamon.instrumentation

import akka.kamon.instrumentation.advisor.AskMethodAdvisor
import kamon.agent.scala.KamonInstrumentation

class AskPatternInstrumentation extends KamonInstrumentation {

  /**
   * Instrument:
   *
   * akka.pattern.AskableActorRef::$qmark$extension
   *
   */
  forTargetType("akka.pattern.AskableActorRef$") { builder ⇒
    builder
      .withAdvisorFor(named("$qmark$extension"), classOf[AskMethodAdvisor])
      .build()
  }
}
