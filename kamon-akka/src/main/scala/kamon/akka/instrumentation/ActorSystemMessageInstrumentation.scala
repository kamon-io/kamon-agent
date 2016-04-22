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

package akka.kamon.instrumentation

import akka.kamon.instrumentation.advisor.{InvokeAllMethodAdvisor, PointMethodAdvisor}
import kamon.agent.libs.net.bytebuddy.description.method.MethodDescription
import kamon.agent.libs.net.bytebuddy.matcher.ElementMatcher.Junction
import kamon.agent.libs.net.bytebuddy.matcher.ElementMatchers._
import kamon.agent.scala.KamonInstrumentation
import kamon.akka.instrumentation.mixin.TraceContextMixin

class ActorSystemMessageInstrumentation extends KamonInstrumentation {

  /**
    * Mix:
    *
    * akka.dispatch.sysmsg.SystemMessage with kamon.trace.TraceContextAware
    *
    */
  forSubtypeOf("akka.dispatch.sysmsg.SystemMessage") { builder ⇒
    builder
      .withMixin(classOf[TraceContextMixin])
      .build()
  }


  /**
   * Instrument:
   *
   * akka.actor.RepointableActorRef::point
   *
   * Mix:
   *
   * akka.actor.RepointableActorRef with kamon.trace.TraceContextAware
   *
   */

  val PointMethod: Junction[MethodDescription] = named("point")

  forTargetType("akka.actor.RepointableActorRef") { builder ⇒
    builder
      .withMixin(classOf[TraceContextMixin])
      .withAdvisorFor(PointMethod, classOf[PointMethodAdvisor])
      .build()
  }

  /**
   * Instrument:
   *
   * akka.actor.ActorCell::invokeAll
   *
   */

  val InvokeAllMethod: Junction[MethodDescription] = named("invokeAll$1")

  forTargetType("akka.actor.ActorCell") { builder ⇒
    builder
      .withAdvisorFor(InvokeAllMethod, classOf[InvokeAllMethodAdvisor])
      .build()
  }
}
