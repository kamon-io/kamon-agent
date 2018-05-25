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

import lombok.Value;
import net.bytebuddy.matcher.ElementMatcher;

@Value
public class ClassLoaderNameMatcher extends ElementMatcher.Junction.AbstractBase<ClassLoader> {

    String name;

    private ClassLoaderNameMatcher(String name) {
        this.name = name;
    }

    public static ElementMatcher.Junction.AbstractBase<ClassLoader> withName(String name) {
        return new ClassLoaderNameMatcher(name);

    }

    public static ElementMatcher.Junction.AbstractBase<ClassLoader> isReflectionClassLoader() {
        return new ClassLoaderNameMatcher("sun.reflect.DelegatingClassLoader");
    }

    public static ElementMatcher.Junction.AbstractBase<ClassLoader> isGroovyClassLoader() {
        return new ClassLoaderNameMatcher("org.codehaus.groovy.runtime.callsite.CallSiteClassLoader");
    }

    public static ElementMatcher.Junction.AbstractBase<ClassLoader> isKanelaClassLoader() {
        return new ClassLoaderNameMatcher(ChildFirstURLClassLoader.class.getName());
    }

    @Override
    public boolean matches(ClassLoader target) {
        return target != null && name.equals(target.getClass().getName());
    }
}