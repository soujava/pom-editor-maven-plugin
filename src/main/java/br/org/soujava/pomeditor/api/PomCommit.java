/*
 * Copyright 2023  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package br.org.soujava.pomeditor.api;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Service component responsible for the commit logic
 */
public interface PomCommit {

    /**
     * Confirms the changes of a given POM and deletes the backup POM file
     *
     * @param pom the target POM file
     * @throws RuntimeException on any issue occurs during the rollback processing
     */
    public static void execute(final Path pom) {
        Objects.requireNonNull(pom, "pom cannot be null");
        Path backupFile = PomChange.backupFileOf(pom);
        if (backupFile.toFile().exists()) {
            backupFile.toFile().delete();
        }
    }

}
