/*
 * =========================================================================================
 * Copyright © 2013-2018 the kamon project <http://kamon.io/>
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

package kanela.agent.builder;

import kanela.agent.api.instrumentation.TypeTransformation;
import kanela.agent.api.instrumentation.listener.DebugInstrumentationListener;
import kanela.agent.api.instrumentation.listener.DefaultInstrumentationListener;
import kanela.agent.api.instrumentation.listener.dumper.ClassDumperListener;
import kanela.agent.cache.PoolStrategyCache;
import kanela.agent.resubmitter.PeriodicResubmitter;
import kanela.agent.util.ListBuilder;
import kanela.agent.util.conf.KanelaConfiguration;
import kanela.agent.util.log.Logger;
import lombok.Value;
import lombok.experimental.var;
import lombok.val;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.dynamic.scaffold.MethodGraph;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;

import static net.bytebuddy.matcher.ElementMatchers.*;

@Value(staticConstructor = "from")
class KanelaAgentBuilder {

    KanelaConfiguration config;
    KanelaConfiguration.ModuleConfiguration moduleDescription;
    Instrumentation instrumentation;

    private static final PoolStrategyCache poolStrategyCache = PoolStrategyCache.instance();
    final ListBuilder<TypeTransformation> typeTransformations = ListBuilder.builder();

    public void addTypeTransformation(TypeTransformation typeTransformation) {
        if (typeTransformation.isActive()) {
            typeTransformations.add(typeTransformation);
        }
    }

    AgentBuilder build() {
        return typeTransformations.build().foldLeft(newAgentBuilder(), (agent, typeTransformation) -> {
            val transformers = new ArrayList<AgentBuilder.Transformer>();
            transformers.addAll(typeTransformation.getBridges().toJavaList());
            transformers.addAll(typeTransformation.getMixins().toJavaList());
            transformers.addAll(typeTransformation.getTransformations().toJavaList());
            return agent.type(typeTransformation.getElementMatcher().get())
                    .transform(new AgentBuilder.Transformer.Compound(transformers));
        });
    }

    private AgentBuilder newAgentBuilder() {
        val byteBuddy = new ByteBuddy()
            .with(TypeValidation.of(config.isDebugMode()))
            .with(MethodGraph.Compiler.ForDeclaredMethods.INSTANCE);

        var agentBuilder = new AgentBuilder.Default(byteBuddy)
            .with(poolStrategyCache);

        agentBuilder = withRetransformationForRuntime(agentBuilder);
        agentBuilder = withBootstrapAttaching(agentBuilder);
        agentBuilder = withIgnore(agentBuilder);

        return agentBuilder
                .with(DefaultInstrumentationListener.instance())
                .with(additionalListeners());
}

    private AgentBuilder withRetransformationForRuntime(AgentBuilder agentBuilder) {
        if (config.isAttachedInRuntime() || moduleDescription.isStoppable() || moduleDescription.shouldInjectInBootstrap()) {
            Logger.info(() -> "Retransformation Strategy activated for: " + moduleDescription.getName());
            agentBuilder = agentBuilder.disableClassFormatChanges()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .withResubmission(PeriodicResubmitter.instance());
        }
        return agentBuilder;
    }

    private AgentBuilder withBootstrapAttaching(AgentBuilder agentBuilder) {
        if(moduleDescription.shouldInjectInBootstrap()){
            Logger.info(() -> "Bootstrap Injection activated.");
            agentBuilder = agentBuilder.enableBootstrapInjection(instrumentation, moduleDescription.getTempDir());
        }
        return agentBuilder;
    }

    private AgentBuilder withIgnore(AgentBuilder agentBuilder) {
        val builder = agentBuilder.ignore(ignoreMatches());
        if (moduleDescription.shouldInjectInBootstrap()) return builder;
        return builder
                .or(any(), isBootstrapClassLoader())
                .or(any(), isExtensionClassLoader());
    }

    private AgentBuilder.Listener additionalListeners() {
        val listeners = new ArrayList<AgentBuilder.Listener>();
        if (config.getDump().isDumpEnabled()) listeners.add(ClassDumperListener.instance());
        if (config.getDebugMode()) listeners.add(DebugInstrumentationListener.instance());
        return new AgentBuilder.Listener.Compound(listeners);
    }

    private ElementMatcher.Junction<NamedElement> ignoreMatches() {
        return not(nameMatches(moduleDescription.getWithinPackage()));
    }
}
