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

package kamon.play.instrumentation.advisor;

import kamon.agent.libs.net.bytebuddy.asm.Advice.FieldValue;
import kamon.agent.libs.net.bytebuddy.asm.Advice.OnMethodExit;
import kamon.play.KamonFilter;
import kamon.play.utils.SeqUtils;
import play.api.mvc.EssentialFilter;
import scala.collection.Seq;

/**
 * Advisor for play.api.http.DefaultHttpRequestHandler::new
 */
public class FiltersFieldAdvisor {

  @OnMethodExit
  public static void onExit(@FieldValue(value = "filters", readOnly = false) Seq<EssentialFilter> filters) {
    filters = SeqUtils.append(filters, KamonFilter.asJava());
  }
}
