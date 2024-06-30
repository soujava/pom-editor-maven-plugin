/*
 * Copyright 2024  the original author or authors.
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

package br.org.soujava.pomeditor.mojo;


import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
abstract class EditingMojoTest {

    @Mock
    Log log;

    @Mock
    Function<Path, Boolean> backupFunction;

    @Mock
    Consumer<Path> rollbackFunction;

    @Captor
    ArgumentCaptor<Path> targetPom;

    Path tempDir;
    Path pom;

    @BeforeEach
    void createTempDirAndPom() throws IOException {
        this.tempDir = Files.createTempDirectory(null);
        this.log = mock(Log.class);
        this.pom = newDummyPom();
    }

    private Path newDummyPom() throws IOException {
        Path pom = Files.createTempFile(tempDir, "pom", ".xml");
        Files.copy(Path.of("pom.xml"), pom, StandardCopyOption.REPLACE_EXISTING);
        return pom;
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
