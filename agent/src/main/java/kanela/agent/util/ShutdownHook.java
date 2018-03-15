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

package kanela.agent.util;

import lombok.Value;
import lombok.EqualsAndHashCode;

import java.util.concurrent.ExecutorService;

@Value
@EqualsAndHashCode(callSuper = false)
public class ShutdownHook extends Thread {
    ExecutorService executor;

    public static void register(ExecutorService executor) {
        new ShutdownHook(executor);
    }

    private ShutdownHook(ExecutorService executor) {
        this.executor = executor;
        Runtime.getRuntime().addShutdownHook(this);
    }

    @Override
    public void run() {
        executor.shutdown();
    }
}