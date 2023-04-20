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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * Service component responsible for the rollback logic
 */
public interface PomRollback {

    /**
     * Recovers the backup POM file replacing the given POM file
     * @param pom target POM file
     * @throws RuntimeException on any issue occurs during the rollback processing
     */
    public static void execute(Path pom) {
        Objects.requireNonNull(pom, "pom cannot be null");
        Path backupFile = PomChange.backupFileOf(pom);
        if (backupFile.toFile().exists()) {
            try {
                Files.copy(backupFile, pom, StandardCopyOption.REPLACE_EXISTING);
                backupFile.toFile().delete();
            } catch (IOException e) {
                throw new RuntimeException("error during rollback process: " + e.getMessage(), e);
            }
        }
    }

}
