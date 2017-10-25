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

package kamon.agent;

import io.vavr.collection.List;
import io.vavr.control.Try;
import kamon.agent.api.instrumentation.KamonInstrumentation;
import kamon.agent.builder.Agents;
import kamon.agent.builder.KamonAgentFileTransformer;
import kamon.agent.util.conf.AgentConfiguration;
import kamon.agent.util.conf.AgentConfiguration.AgentModuleDescription;
import kamon.agent.util.log.LazyLogger;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassInjector;

import java.lang.instrument.Instrumentation;
import java.util.Collections;

import static java.text.MessageFormat.format;

public class InstrumentationLoader {

    /**
     * Load from the current classpath all defined instrumentations {@link KamonInstrumentation}.
     *
     * @param instrumentation {@link Instrumentation}
     * @param configuration {@link AgentConfiguration}
     * @return a list of {@link KamonAgentFileTransformer}
     */
    public static List<KamonAgentFileTransformer> load(Instrumentation instrumentation, AgentConfiguration configuration) {
        return configuration.getAgentModules().map((moduleDescription) -> {
            LazyLogger.infoColor(() -> format("Loading {0} ",  moduleDescription.getName()));
            return moduleDescription.getInstrumentations()
                                    .map(instrumentationClassName -> loadInstrumentation(instrumentationClassName, configuration))
                                    .map(kamonInstrumentation -> injectInBootstrapClassloader(kamonInstrumentation , moduleDescription, instrumentation))
                                    .filter(KamonInstrumentation::isActive)
                                    .sortBy(KamonInstrumentation::order)
                                    .flatMap(KamonInstrumentation::collectTransformations)
                                    .foldLeft(Agents.from(configuration, moduleDescription, instrumentation), Agents::addTypeTransformation)
                                    .install();
        });
    }

    private static KamonInstrumentation loadInstrumentation(String instrumentationClassName, AgentConfiguration configuration) {
        LazyLogger.infoColor(() -> format(" ==> Loading {0} ", instrumentationClassName));
        return Try.of(() -> (KamonInstrumentation) Class.forName(instrumentationClassName, true, getClassLoader(InstrumentationLoader.class)).newInstance())
                  .getOrElseThrow((cause) -> new RuntimeException(format("Error trying to load Instrumentation {0}", instrumentationClassName), cause));
    }


    private static KamonInstrumentation injectInBootstrapClassloader(KamonInstrumentation kamonInstrumentation, AgentModuleDescription moduleDescription, Instrumentation instrumentation) {
        if (moduleDescription.shouldInjectInBootstrap()) {
            ClassInjector.UsingInstrumentation.of(moduleDescription.getTempDir(), ClassInjector.UsingInstrumentation.Target.BOOTSTRAP, instrumentation).inject(Collections.singletonMap(
                    new TypeDescription.ForLoadedType(kamonInstrumentation.getClass()),
                    ClassFileLocator.ForClassLoader.read(kamonInstrumentation.getClass()).resolve()));
        }
        return kamonInstrumentation;
    }

    private static ClassLoader getClassLoader(Class<?> clazz) {
      return clazz.getClassLoader() == null ? ClassLoader.getSystemClassLoader() : clazz.getClassLoader();
    }
}
