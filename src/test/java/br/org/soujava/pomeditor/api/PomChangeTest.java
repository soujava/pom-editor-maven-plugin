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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

import static br.org.soujava.pomeditor.CheckSum.checksum;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PomChangeTest extends BaseTest {

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

            newPomChangeBuilder().build()
                    .execute(() -> {
                        // do something here
                    });
            assertTrue(backupPom.toFile().exists());
            assertEquals(expectedCheckSum, checksum(backupPom));
        }

        @Test
        void shouldRollbackOnErrors() throws IOException, NoSuchAlgorithmException {
            var expectedCheckSum = checksum(pom);

            assertThrows(Throwable.class, () -> {
                newPomChangeBuilder()
                        .build().execute(() -> {
                            modifyPomRandomly();
                            throw new RuntimeException("forced error");
                        });
            });
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

            newPomChangeBuilder()
                    .build()
                    .execute(() -> {
                        modifyPomRandomly();
                    });

            assertNotEquals(checksum(pom), checksum(backupPom));
            assertEquals(expectedCheckSum, checksum(backupPom));
        }

        @Test
        void shouldNotRollbackOnErrors() throws IOException, NoSuchAlgorithmException {
            var expectedCheckSum = checksum(pom);

            assertThrows(Throwable.class, () -> {
                newPomChangeBuilder()
                        .build().execute(() -> {
                            modifyPomRandomly();
                            throw new RuntimeException("forced error");
                        });
            });

            assertTrue(backupPom.toFile().exists());
            assertNotEquals(expectedCheckSum, checksum(pom));
        }

    }


}