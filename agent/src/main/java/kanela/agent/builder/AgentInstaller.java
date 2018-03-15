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
import kanela.agent.util.conf.KanelaConfiguration;

import lombok.Value;
import lombok.val;

import java.lang.instrument.Instrumentation;

@Value
public class AgentInstaller {
    KanelaAgentBuilder agentBuilder;
    KanelaConfiguration.ModuleConfiguration moduleDescription;
    Instrumentation instrumentation;

    private AgentInstaller(KanelaConfiguration config, KanelaConfiguration.ModuleConfiguration moduleDescription, Instrumentation instrumentation) {
        this.moduleDescription = moduleDescription;
        this.agentBuilder = KanelaAgentBuilder.from(config, moduleDescription, instrumentation);
        this.instrumentation = instrumentation;
    }

    public static AgentInstaller from(KanelaConfiguration config, KanelaConfiguration.ModuleConfiguration moduleDescription, Instrumentation instrumentation) {
        return new AgentInstaller(config, moduleDescription, instrumentation);
    }

    public KanelaFileTransformer install() {
        val agentBuilder = this.agentBuilder.build();
        val classFileTransformer = agentBuilder.installOn(instrumentation);
        return KanelaFileTransformer.from(agentBuilder, classFileTransformer, moduleDescription.isStoppable());
    }

    public AgentInstaller addTypeTransformation(TypeTransformation typeTransformation) {
        agentBuilder.addTypeTransformation(typeTransformation);
        return this;
    }
}