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

package kamon.agent.api.instrumentation.listener.dumper;

import javaslang.control.Try;
import kamon.agent.util.conf.AgentConfiguration;
import kamon.agent.util.log.LazyLogger;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.bytebuddy.agent.builder.AgentBuilder.Listener;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import java.io.File;

@Value
@EqualsAndHashCode(callSuper = false)
public class ClassDumperListener extends Listener.Adapter {

    private static final ClassDumperListener Instance = new ClassDumperListener();

    File dumpDir;
    File jarFile;
    AgentConfiguration.DumpConfig config;

    private ClassDumperListener(){
        this.config = AgentConfiguration.instance().getDump();
        this.dumpDir = new File(config.getDumpDir());
        this.jarFile = new File(config.getDumpDir() + File.separator + config.getJarName() + ".jar");
    }

    public static ClassDumperListener instance() {
        return Instance;
    }

    @Override
    public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded, DynamicType dynamicType) {
        addClassToDump(dynamicType);
    }

    private void addClassToDump(DynamicType dynamicType) {
        if(!dumpDir.exists()){
            runSafe(dumpDir::mkdirs, "Error creating directory...");
        }

        if(config.getCreateJar()) {
            if(!jarFile.exists()) {
                runSafe(jarFile::createNewFile, "Error creating a new file...");
                runSafe(() -> dynamicType.toJar(jarFile), "Error trying to add transformed class to a new jar...");
            } else {
                runSafe( () -> dynamicType.inject(jarFile), "Error trying to add transformed class to existing jar...");
            }
        } else {
            runSafe(() -> dynamicType.saveIn(dumpDir), "Error trying to save transformed class into directory...");
        }
    }

    private <R> void runSafe(Try.CheckedSupplier<R> thunk, String msg) {
        Try.of(thunk).onFailure((cause) -> LazyLogger.errorColor(() -> msg, cause));
    }
}
