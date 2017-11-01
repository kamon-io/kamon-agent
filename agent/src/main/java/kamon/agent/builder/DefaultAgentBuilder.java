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

package kamon.agent.builder;

import kamon.agent.api.instrumentation.TypeTransformation;
import kamon.agent.api.instrumentation.listener.DebugInstrumentationListener;
import kamon.agent.api.instrumentation.listener.DefaultInstrumentationListener;
import kamon.agent.api.instrumentation.listener.dumper.ClassDumperListener;
import kamon.agent.util.conf.AgentConfiguration;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.val;
import net.bytebuddy.agent.builder.AgentBuilder;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;

@Value(staticConstructor = "instance")
@EqualsAndHashCode(callSuper = false)
class DefaultAgentBuilder extends KamonAgentBuilder {

    String name;

    public AgentBuilder newAgentBuilder(AgentConfiguration config, AgentConfiguration.ModuleConfiguration moduleDescription, Instrumentation instrumentation) {
        return from(config, moduleDescription, instrumentation)
                .with(DefaultInstrumentationListener.instance())
                .with(additionalListeners(config));
    }

    public void addTypeTransformation(TypeTransformation typeTransformation) {
      if (typeTransformation.isActive()) {
          typeTransformations.add(typeTransformation);
      }
    }

    private AgentBuilder.Listener additionalListeners(AgentConfiguration config) {
        val listeners = new ArrayList<AgentBuilder.Listener>();
        if (config.getDump().isDumpEnabled()) listeners.add(ClassDumperListener.instance());
        if (config.getDebugMode()) listeners.add(DebugInstrumentationListener.instance());
        return new AgentBuilder.Listener.Compound(listeners);
    }

    @Override
    protected String agentName() {
        return this.getName();
    }
}
