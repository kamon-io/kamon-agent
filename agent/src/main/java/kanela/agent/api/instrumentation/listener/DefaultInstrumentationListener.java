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

package kanela.agent.api.instrumentation.listener;

import kanela.agent.util.log.Logger;
import net.bytebuddy.agent.builder.AgentBuilder.Listener;
import net.bytebuddy.utility.JavaModule;

import static java.text.MessageFormat.format;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = false)
public class DefaultInstrumentationListener extends Listener.Adapter {

    private static final DefaultInstrumentationListener Instance = new DefaultInstrumentationListener();

    @Override
    public void onError(String error, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {
        Logger.info(() -> format("Error => {0} with message {1}. Class loader: {2}", error, throwable.getMessage(), (classLoader == null) ? "Bootstrap class loader" : classLoader.getClass().getName()));
    }

    public static DefaultInstrumentationListener instance() {
        return Instance;
    }
}
