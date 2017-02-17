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

package kamon.akka.instrumentation.mixin

import akka.kamon.instrumentation.RouterMonitor

/**
 * Mixin for akka.routing.RoutedActorCell
 */
class RoutedActorCellInstrumentationMixin extends RouterInstrumentationAware {
  @volatile private var _ri: RouterMonitor = _

  def setRouterInstrumentation(ai: RouterMonitor): Unit = _ri = ai
  def routerInstrumentation: RouterMonitor = _ri
}

trait RouterInstrumentationAware {
  def routerInstrumentation: RouterMonitor
  def setRouterInstrumentation(ai: RouterMonitor): Unit
}
