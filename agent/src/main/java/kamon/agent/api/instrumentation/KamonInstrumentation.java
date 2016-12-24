/*
 * =========================================================================================
 * Copyright © 2013-2016 the kamon project <http://kamon.io/>
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

package kamon.agent.api.instrumentation;

import javaslang.Function1;
import kamon.agent.api.advisor.AdvisorDescription;
import kamon.agent.api.instrumentation.mixin.MixinDescription;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.ByteCodeElement;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.pool.TypePool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.bytebuddy.matcher.ElementMatchers.*;

public abstract class KamonInstrumentation {
    private final List<InstrumentationDescription> instrumentationDescriptions = new ArrayList<>();

    private final TypePool typePool = TypePool.Default.ofClassPath();
    protected final ElementMatcher.Junction<ByteCodeElement> NotDeclaredByObject = not(isDeclaredBy(Object.class));
    protected final ElementMatcher.Junction<MethodDescription> TakesArguments = not(takesArguments(0));

    public List<TypeTransformation> collectTransformations() {
        return instrumentationDescriptions.stream()
                .map(this::makeTransformations)
                .collect(Collectors.toList());
    }

    private TypeTransformation makeTransformations(InstrumentationDescription instrumentationDescription) {
        final Set<AgentBuilder.Transformer> mixins = toTransformers(instrumentationDescription.mixins(), MixinDescription::makeTransformer);
        final Set<AgentBuilder.Transformer> advisors = toTransformers(instrumentationDescription.interceptors(), AdvisorDescription::makeTransformer);
        final Set<AgentBuilder.Transformer> transformers = toTransformers(instrumentationDescription.transformers(), Function.identity());

        return TypeTransformation.of(instrumentationDescription.elementMatcher(), mixins, advisors, transformers);
    }

    private <T> Set<AgentBuilder.Transformer> toTransformers(List<T> transformerList, Function<T, AgentBuilder.Transformer> f) {
        return transformerList.stream()
                .map(f)
                .collect(Collectors.toSet());
    }

    public void forTargetType(Supplier<String> f, Function1<InstrumentationDescription.Builder, InstrumentationDescription> instrumentationFunction) {
        InstrumentationDescription.Builder builder = new InstrumentationDescription.Builder();
        builder.addElementMatcher(() -> defaultTypeMatcher().and(named(f.get())));
        instrumentationDescriptions.add(instrumentationFunction.apply(builder));
    }

    public void forSubtypeOf(Supplier<String> f, Function1<InstrumentationDescription.Builder, InstrumentationDescription> instrumentationFunction) {
        InstrumentationDescription.Builder builder = new InstrumentationDescription.Builder();
        builder.addElementMatcher(() -> defaultTypeMatcher().and(isSubTypeOf(typePool.describe(f.get()).resolve())));
        instrumentationDescriptions.add(instrumentationFunction.apply(builder));
    }

    private ElementMatcher.Junction<TypeDescription> defaultTypeMatcher() {
        return  failSafe(not(isInterface()).and(not(isSynthetic())));
    }

    public boolean isActive() {
        return true;
    }

    public int order() {
        return 1;
    }
}
