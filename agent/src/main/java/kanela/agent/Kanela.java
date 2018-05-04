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

package kanela.agent;

import java.lang.instrument.Instrumentation;

public final class Kanela {

    /**
     * JVM hook to statically load the javaagent at startup.
     *
     * After the Java Virtual Machine (JVM) has initialized, the premain method
     * will be called. Then the real application main method will be called.
     *
     * @param args Agent argument list
     * @param instrumentation {@link Instrumentation}
     */
    public static void premain(final String args, final Instrumentation instrumentation) throws Exception {
        KanelaEntryPoint.premain(args, instrumentation);
    }

    /**
     * JVM hook to dynamically load javaagent at runtime.
     *
     * The agent class may have an agentmain method for use when the agent is
     * started after VM startup.
     *
     * @param args Agent argument list
     * @param instrumentation {@link Instrumentation}
     */
    public static void agentmain(final String args, final Instrumentation instrumentation) throws Exception {
        KanelaEntryPoint.agentmain(args, instrumentation);
    }
}
