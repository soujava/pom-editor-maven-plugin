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

package br.org.soujava.pomeditor.control;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Component responsible for POM changes with backup support
 */
public final class PomChange {

    public static Path backupFileOf(Path pom) {
        Objects.requireNonNull(pom, "pom cannot be null");
        return Path.of(pom.toString() + ".backup");
    }

    private final Path pom;
    private final Consumer<String> logger;
    private final Function<Path, Boolean> backupFunction;
    private final Consumer<Path> rollbackFunction;

    private PomChange(Path pom,
                      Consumer<String> logger,
                      Function<Path, Boolean> backupFunction,
                      Consumer<Path> rollbackFunction) {
        Objects.requireNonNull(pom, "pom cannot be null");
        this.pom = pom;
        this.logger = Optional.ofNullable(logger).orElse(System.out::println);
        this.backupFunction = Optional.ofNullable(backupFunction).orElse(this::backup);
        this.rollbackFunction = Optional.ofNullable(rollbackFunction).orElse(Rollback::execute);
    }

    public static PomChangeBuilder builder() {
        return new PomChangeBuilder();
    }

    public static class PomChangeBuilder {

        private Path pom;
        private Consumer<String> logger = System.out::println;
        private Function<Path, Boolean> backupFunction;
        private Consumer<Path> rollbackFunction;

        public PomChangeBuilder withLogger(Consumer<String> logger) {
            this.logger = logger;
            return this;
        }

        public PomChangeBuilder withPom(Path pom) {
            this.pom = pom;
            return this;
        }

        public PomChangeBuilder withBackupFunction(Function<Path, Boolean> backupFunction) {
            this.backupFunction = backupFunction;
            return this;
        }

        public PomChangeBuilder withRollbackFunction(Consumer<Path> rollbackFunction) {
            this.rollbackFunction = rollbackFunction;
            return this;
        }

        public PomChange build() {
            return new PomChange(this.pom, this.logger, this.backupFunction, this.rollbackFunction);
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
        return Optional.ofNullable(this.backupFunction.apply(this.pom))
                .orElse(Boolean.FALSE);
    }

    private void rollback(boolean isBackupOwner) {
        if (isBackupOwner) {
            this.rollbackFunction.accept(this.pom);
        }
    }

    private Boolean backup(Path pom) {
        Path backupFile = backupFileOf(pom);
        if (!backupFile.toFile().exists()) {
            try {
                Files.copy(pom, backupFile);
                logger.accept(String.format("Backup for '%s' created: '%s'",
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
