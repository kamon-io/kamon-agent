/*
 * =========================================================================================
 * Copyright © 2013-2019 the kamon project <http://kamon.io/>
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

package kanela.agent.api.advisor;

import io.vavr.control.Option;
import lombok.Value;
import lombok.val;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

@Value
public class AdvisorDescription {
    private final ElementMatcher<? super MethodDescription> methodMatcher;
    private final Class<?> advisorClass;
    private final String advisorClassName;

    public static AdvisorDescription of(ElementMatcher.Junction<MethodDescription> methodMatcher, String advisorClassName) {
        return new AdvisorDescription(methodMatcher, null, advisorClassName);
    }

    public static AdvisorDescription of(ElementMatcher.Junction<MethodDescription> methodMatcher, Class<?> advisorClass) {
        return new AdvisorDescription(methodMatcher, advisorClass, null);
    }

    public AgentBuilder.Transformer makeTransformer() {
        val name = Option.of(advisorClassName).getOrElse(() -> advisorClass.getName());
        return new AgentBuilder.Transformer.ForAdvice()
                .advice(this.methodMatcher, name)
                .include(Thread.currentThread().getContextClassLoader())
                .withExceptionHandler(AdviceExceptionHandler.instance());
    }
}
