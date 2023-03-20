/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.arrudalabs.pomeditor;

import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * Interface that provides functions for the pom change transactions
 */
interface PomChangeTransaction {

    static Path backupFileOf(Path pom) {
        Objects.requireNonNull(pom, "pom cannot be null");
        return Path.of(pom.toString() + ".backup");
    }

    static void backup(Log log, Path pom) {
        Objects.requireNonNull(log, "log cannot be null");
        Objects.requireNonNull(pom, "pom cannot be null");
        Path backupFile = backupFileOf(pom);
        if (!backupFile.toFile().exists()) {
            try {
                Files.copy(pom, backupFile);
                log.info(String.format("Backup for '%s' created: '%s'",
                        pom.toAbsolutePath(),
                        backupFile.toAbsolutePath()));
            } catch (IOException e) {
                throw new RuntimeException("failure during backup process:" + e.getMessage(), e);
            }
        }
    }

    static void rollback(Log log, Path pom) {
        Objects.requireNonNull(log, "log cannot be null");
        Objects.requireNonNull(pom, "pom cannot be null");
        Path backupFile = backupFileOf(pom);
        if (backupFile.toFile().exists()) {
            try {
                log.info("reverting to original POM file");
                Files.copy(backupFile, pom, StandardCopyOption.REPLACE_EXISTING);
                backupFile.toFile().delete();
                log.info("backup file was deleted");
            } catch (IOException e) {
                log.error("error during rollback process: " + e.getMessage(), e);
            }
        }
    }

    static void commit(Log log, Path pom) {
        Objects.requireNonNull(log, "log cannot be null");
        Objects.requireNonNull(pom, "pom cannot be null");
        Path backupFile = backupFileOf(pom);
        if (backupFile.toFile().exists()) {
            log.info("committing changes...");
            backupFile.toFile().delete();
            log.info("backup file was deleted");
        }
    }
}
