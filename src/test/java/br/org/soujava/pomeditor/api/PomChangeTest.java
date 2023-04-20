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

package br.org.soujava.pomeditor.transaction;

import br.org.soujava.pomeditor.control.PomChange;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import static br.org.soujava.pomeditor.CheckSum.checksum;

class PomChangeTransactionTest {

    @Nested
    class WhenBackFileDoesNotExists {

        Path backupPom;

        @BeforeEach
        void setup() throws IOException {
            createTempDirAndPom();
            this.backupPom = PomChange.backupFileOf(pom);
            if (this.backupPom.toFile().exists())
                this.backupPom.toFile().delete();
        }

        @Test
        void shouldCreateBackup() throws Throwable {
            var expectedCheckSum = checksum(pom);

            getPomChangeTransactionBuilder().build()
                    .execute(() -> {
                        // do something here
                    });

            verify(log, atLeastOnce()).info(anyString());
            assertTrue(backupPom.toFile().exists());
            assertEquals(expectedCheckSum, checksum(backupPom));
        }

        @Test
        void shouldRollbackOnErrors() throws IOException, NoSuchAlgorithmException {
            var expectedCheckSum = checksum(pom);

            assertThrows(Throwable.class, () -> {
                getPomChangeTransactionBuilder()
                        .build().execute(() -> {
                            modifyPomRandomly();
                            throw new RuntimeException("forced error");
                        });
            });

            verify(log, atLeastOnce()).info(anyString());
            assertFalse(backupPom.toFile().exists());
            assertEquals(expectedCheckSum, checksum(pom));
        }
    }


    @Nested
    class WhenBackFileAlreadyExists {

        Path backupPom;

        @BeforeEach
        void setup() throws IOException {
            createTempDirAndPom();
            backupPom = newDummyBackup(pom);
        }

        @Test
        void shouldNotCreateBackup() throws Throwable {

            var expectedCheckSum = checksum(backupPom);

            getPomChangeTransactionBuilder()
                    .build()
                    .execute(() -> {
                        modifyPomRandomly();
                    });

            verify(log, never()).info(anyString());
            assertNotEquals(checksum(pom), checksum(backupPom));
            assertEquals(expectedCheckSum, checksum(backupPom));
        }

        @Test
        void shouldNotRollbackOnErrors() throws IOException, NoSuchAlgorithmException {
            var expectedCheckSum = checksum(pom);

            assertThrows(Throwable.class, () -> {
                getPomChangeTransactionBuilder()
                        .build().execute(() -> {
                            modifyPomRandomly();
                            throw new RuntimeException("forced error");
                        });
            });

            verify(log, never()).info(anyString());
            assertTrue(backupPom.toFile().exists());
            assertNotEquals(expectedCheckSum, checksum(pom));
        }

    }

    private Log log;
    private Path pom;

    @Test
    void shouldCommit() throws IOException, NoSuchAlgorithmException {


        Path backup = newDummyBackup(pom);

        String expectedCheckSum = checksum(pom);
        assertNotEquals(expectedCheckSum, checksum(backup));

        PomChange.commit(log, pom);
        verify(log, atMost(2)).info(anyString());

        assertTrue(pom.toFile().exists());
        assertFalse(backup.toFile().exists());

        assertEquals(expectedCheckSum, checksum(pom));

    }

    @Test
    void shouldRollback() throws IOException, NoSuchAlgorithmException {

        var log = mock(Log.class);
        Path pom = newDummyPom();
        Path backup = newDummyBackup(pom);

        String expectedCheckSum = checksum(backup);
        assertNotEquals(expectedCheckSum, checksum(pom));

        PomChange.rollback(log, pom);
        verify(log, atMost(2)).info(anyString());

        assertTrue(pom.toFile().exists());
        assertFalse(backup.toFile().exists());

        assertEquals(expectedCheckSum, checksum(pom));

    }

    private PomChange.PomChangeBuilder getPomChangeTransactionBuilder() {
        return PomChange.builder()
                .withLog(log)
                .withPom(pom);
    }

    // utility methods

    private void modifyPomRandomly() throws IOException {
        modifyPom(UUID.randomUUID().toString());
    }

    private void modifyPom(String data) throws IOException {
        Files.writeString(pom, data, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private Path newDummyPom() throws IOException {
        Path pom = Files.createTempFile(tempDir, "pom", ".xml");
        Files.writeString(pom, UUID.randomUUID().toString(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        return pom;
    }

    private Path newDummyBackup(Path pom) throws IOException {
        Path backupFile = PomChange.backupFileOf(pom);
        Files.writeString(backupFile, UUID.randomUUID().toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        return backupFile;
    }

    protected Path tempDir;

    @BeforeEach
    void createTempDirAndPom() throws IOException {
        this.tempDir = Files.createTempDirectory(null);
        this.log = mock(Log.class);
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