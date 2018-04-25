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

package kanela.agent.util;

import io.vavr.control.Try;
import kanela.agent.Kanela;
import lombok.Value;
import lombok.val;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

@Value
public class Jar {
    public static Try<JarFile> getEmbeddedJar(String jarName) {
        return Try.of(() -> {
            val tempFile = File.createTempFile(jarName, ".jar");
            val resourceAsStream = Kanela.class.getResourceAsStream(jarName + ".jar");
            Files.copy(resourceAsStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return new JarFile(tempFile);
        });
    }

    public static Try<List<ExtensionJar>> fromString(String arguments) {
        return stringToMap(arguments)
                .mapTry(map -> map.entrySet()
                        .stream()
                        .map(k -> ExtensionJar.from(k.getKey(), k.getValue()))
                        .collect(Collectors.toList()));
    }

    private static Try<Map<String,String>> stringToMap(String value) {
        return Try.of(() -> Arrays.stream(value.split(";"))
                .map(s -> s.split(":"))
                .collect(Collectors.toMap(k -> k[0], v -> v[1])));
    }

    @Value(staticConstructor = "from")
    static class ExtensionJar {
        String agentLocation;
        String classLoader;
    }
}
