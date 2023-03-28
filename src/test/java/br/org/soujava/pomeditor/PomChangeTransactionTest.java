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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PomChangeTransactionTest {

    @Test
    void shouldCreateBackup() throws IOException, NoSuchAlgorithmException {

        var log = mock(Log.class);

        Path pom = newDummyPom();
        Path backupFile = PomChangeTransaction.backupFileOf(pom);

        PomChangeTransaction.backup(log, pom);
        verify(log, atLeastOnce()).info(anyString());

        String expectedChecksum = checksum(pom);
        String actualChecksum = checksum(backupFile);

        assertEquals(expectedChecksum, actualChecksum);

    }

    @Test
    void shouldCommit() throws IOException, NoSuchAlgorithmException {

        var log = mock(Log.class);
        Path pom = newDummyPom();
        Path backup = newDummyBackup(pom);

        String expectedCheckSum = checksum(pom);
        assertNotEquals(expectedCheckSum, checksum(backup));

        PomChangeTransaction.commit(log, pom);
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

        PomChangeTransaction.rollback(log, pom);
        verify(log, atMost(2)).info(anyString());

        assertTrue(pom.toFile().exists());
        assertFalse(backup.toFile().exists());

        assertEquals(expectedCheckSum, checksum(pom));

    }

    private Path newDummyPom() throws IOException {
        Path pom = Files.createTempFile(tempDir, "pom", ".xml");
        Files.writeString(pom, UUID.randomUUID().toString(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        return pom;
    }

    private Path newDummyBackup(Path pom) throws IOException {
        Path backupFile = PomChangeTransaction.backupFileOf(pom);
        Files.writeString(backupFile, UUID.randomUUID().toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        return backupFile;
    }


    protected Path tempDir;

    @BeforeEach
    void before() throws IOException {
        tempDir = Files.createTempDirectory(null);
    }

    @AfterEach
    void after() {
        Optional.ofNullable(tempDir).ifPresent(tempDir -> delete(tempDir.toFile()));
    }

    String checksum(Path file) throws IOException, NoSuchAlgorithmException {
        byte[] data = Files.readAllBytes(file);
        byte[] hash = MessageDigest.getInstance("MD5").digest(data);
        String checksum = new BigInteger(1, hash).toString(16);
        return checksum;
    }

    void delete(File file) {
        if (file.isDirectory()) {
            Arrays.stream(file.listFiles()).forEach(this::delete);
        }
        file.delete();
    }

}