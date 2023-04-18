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

package br.org.soujava.pomeditor;

import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Interface that provides functions for the pom change transactions
 */
public class PomChangeTransaction {

    public static Path backupFileOf(Path pom) {
        Objects.requireNonNull(pom, "pom cannot be null");
        return Path.of(pom.toString() + ".backup");
    }

    public static void rollback(Log log, Path pom) {
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

    public static void commit(Log log, Path pom) {
        Objects.requireNonNull(log, "log cannot be null");
        Objects.requireNonNull(pom, "pom cannot be null");
        Path backupFile = backupFileOf(pom);
        if (backupFile.toFile().exists()) {
            log.info("committing changes...");
            backupFile.toFile().delete();
            log.info("backup file was deleted");
        }
    }

    private final Log log;
    private final Path pom;
    private final BiFunction<Log, Path, Boolean> backupFunction;
    private final BiConsumer<Log, Path> rollbackFunction;

    private PomChangeTransaction(Log log,
                                 Path pom,
                                 BiFunction<Log, Path, Boolean> backupFunction,
                                 BiConsumer<Log, Path> rollbackFunction) {
        this.log = log;
        this.pom = pom;
        this.backupFunction = Optional.ofNullable(backupFunction).orElse(this::backup);
        this.rollbackFunction = Optional.ofNullable(rollbackFunction).orElse(PomChangeTransaction::rollback);
    }

    public static PomChangeTransactionBuilder builder() {
        return new PomChangeTransactionBuilder();
    }

    static class PomChangeTransactionBuilder {

        private Log log;
        private Path pom;
        private BiFunction<Log, Path, Boolean> backupFunction;
        private BiConsumer<Log, Path> rollbackFunction;

        public PomChangeTransactionBuilder withLog(Log log) {
            this.log = log;
            return this;
        }

        public PomChangeTransactionBuilder withPom(Path pom) {
            this.pom = pom;
            return this;
        }

        public PomChangeTransactionBuilder withBackupFunction(BiFunction<Log, Path, Boolean> backupFunction) {
            this.backupFunction = backupFunction;
            return this;
        }


        public PomChangeTransactionBuilder withRollbackFunction(BiConsumer<Log, Path> rollbackFunction) {
            this.rollbackFunction = rollbackFunction;
            return this;
        }

        public PomChangeTransaction build() {
            return new PomChangeTransaction(this.log, this.pom, this.backupFunction, this.rollbackFunction);
        }

    }

    @FunctionalInterface
    public static interface Executable {
        void execute() throws Throwable;
    }

    public void execute(Executable executable) throws Throwable {
        boolean createdBackupFile = false;
        try {
            createdBackupFile = createBackupFileIfNeeded();
            executable.execute();
        } catch (Throwable ex) {
            rollback(createdBackupFile);
            throw ex;
        }
    }

    private boolean createBackupFileIfNeeded() {
        return Optional.ofNullable(
                        Optional.ofNullable(this.backupFunction).orElse(this::backup).apply(this.log, this.pom))
                .orElse(Boolean.FALSE);
    }

    private void rollback(boolean isBackupOwner) {
        if (isBackupOwner) {
            Optional.ofNullable(this.rollbackFunction)
                    .orElse(PomChangeTransaction::rollback)
                    .accept(this.log, this.pom);
        }
    }

    private Boolean backup(Log log, Path pom) {
        Objects.requireNonNull(log, "log cannot be null");
        Objects.requireNonNull(pom, "pom cannot be null");
        Path backupFile = backupFileOf(pom);
        if (!backupFile.toFile().exists()) {
            try {
                Files.copy(pom, backupFile);
                log.info(String.format("Backup for '%s' created: '%s'",
                        pom.toAbsolutePath(),
                        backupFile.toAbsolutePath()));
                return true;
            } catch (IOException e) {
                throw new RuntimeException("failure during backup process:" + e.getMessage(), e);
            }
        }
        return false;
    }

}
