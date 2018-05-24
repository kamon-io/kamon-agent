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

package kanela.agent.util.classloader;

import io.vavr.control.Try;
import kanela.agent.util.BuiltInModuleLoader;
import kanela.agent.util.Lang;
import kanela.agent.util.log.Logger;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.val;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.jar.JarFile;

@Value
@EqualsAndHashCode(callSuper = false)
public class KanelaClassLoader {

    ChildFirstURLClassLoader contextClassLoader;

    private KanelaClassLoader(Instrumentation instrumentation) {
        val modules = BuiltInModuleLoader.getUrlModules();
        val classpath = modules.toArray(new URL[modules.size()]);
        appendToSystemClassloader(instrumentation, classpath);

        contextClassLoader = new ChildFirstURLClassLoader(classpath, getParentClassLoader());
    }

    public static KanelaClassLoader from(Instrumentation instrumentation) {
        return new KanelaClassLoader(instrumentation);
    }

    public void use(Consumer<ClassLoader> thunk) {
        val oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
            thunk.accept(contextClassLoader);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    private ClassLoader getParentClassLoader() {
        return Try.of(() -> {
            val javaVersion = Lang.getRunningJavaVersion();
            if (javaVersion.startsWith("1.7") || javaVersion.startsWith("1.8")) return null;
            //platform classloader is parent of system in java >= 9
            val method = ClassLoader.class.getDeclaredMethod("getPlatformClassLoader");
            return (ClassLoader) method.invoke(null);
        }).getOrElse(() -> null);
    }

    private void appendToSystemClassloader(final Instrumentation instrumentation, final URL[] urls) {
        Arrays.asList(urls).forEach(url -> Try
                .run(() -> instrumentation.appendToSystemClassLoaderSearch(new JarFile(new File(url.toURI()))))
                .onFailure((cause) -> Logger.warn(() -> "Error trying to append module to the System Class Loader", cause)));
    }
}
