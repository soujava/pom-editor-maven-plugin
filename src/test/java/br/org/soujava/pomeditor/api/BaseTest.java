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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public abstract class BaseTest {

    protected Path pom;
    protected Path tempDir;

    PomChange.PomChangeBuilder newPomChangeBuilder() {
        return PomChange.builder()
                .withLogger((string)->{})
                .withPom(pom);
    }

    // utility methods

    void modifyPomRandomly() throws IOException {
        modifyPom(UUID.randomUUID().toString());
    }

    void modifyPom(String data) throws IOException {
        Files.writeString(pom, data, StandardOpenOption.TRUNCATE_EXISTING);
    }

    Path newDummyPom() throws IOException {
        Path pom = Files.createTempFile(tempDir, "pom", ".xml");
        Files.writeString(pom, UUID.randomUUID().toString(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        return pom;
    }

    Path newDummyBackup(Path pom) throws IOException {
        Path backupFile = PomChange.backupFileOf(pom);
        Files.writeString(backupFile, UUID.randomUUID().toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        return backupFile;
    }

    @BeforeEach
    void createTempDirAndPom() throws IOException {
        this.tempDir = Files.createTempDirectory(null);
        this.pom = newDummyPom();
    }

    @AfterEach
    void destroyTempDir() {
        Optional.ofNullable(tempDir).ifPresent(tempDir -> delete(tempDir.toFile()));
    }


    void delete(File file) {
        if (file.isDirectory()) {
            Arrays.stream(file.listFiles()).forEach(this::delete);
        }
        file.delete();
    }

}
