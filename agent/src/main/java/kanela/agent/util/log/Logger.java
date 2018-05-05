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

package kanela.agent.util.log;

import io.vavr.control.Try;
import kanela.agent.util.conf.KanelaConfiguration;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.labelers.TimestampLabeler;
import org.pmw.tinylog.policies.SizePolicy;
import org.pmw.tinylog.policies.StartupPolicy;
import org.pmw.tinylog.writers.RollingFileWriter;

import java.util.function.Supplier;

/**
 * Lazy Logger implementing {@link org.pmw.tinylog.Logger}, which supports lazy evaluation of messages.<br>
 * The message to be logged must be inside a {@link Supplier} which will be evaluated only if the level of debug is enabled.
 */
public class Logger {

    static {
        Try.run(() -> Configurator
                .fromResource("kanela-log.properties")
                .writingThread("main")
                .level(KanelaConfiguration.instance().getLogLevel())
                .addWriter(new RollingFileWriter("kanela-agent.log", 2, true, new TimestampLabeler(), new StartupPolicy(), new SizePolicy(10 * 1024)))
                .activate())
                .getOrElseThrow((error) -> new RuntimeException("Error when trying to load configuration: " + error.getMessage()));
    }

    private Logger(){}

    public static void debug(final Supplier<String> msg) { org.pmw.tinylog.Logger.debug(msg.get());}

    public static void trace(final Supplier<String> msg) { org.pmw.tinylog.Logger.trace(msg.get());}

    public static void info(final Supplier<String> msg) { org.pmw.tinylog.Logger.info(msg.get()); }

    public static void info(final Supplier<String> msg, final Throwable t) { org.pmw.tinylog.Logger.info(msg.get(),t);}

    public static void warn(final Supplier<String> msg) { org.pmw.tinylog.Logger.warn(msg.get());}

    public static void warn(final Supplier<String> msg, final Throwable t) { org.pmw.tinylog.Logger.warn(msg.get(), t);}

    public static void error(final Supplier<String> msg) { org.pmw.tinylog.Logger.error(msg.get()); }

    public static void error(final Supplier<String> msg, final Throwable t) { org.pmw.tinylog.Logger.warn(msg.get(),t);}
}
