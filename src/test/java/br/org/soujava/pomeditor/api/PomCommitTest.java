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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

import static br.org.soujava.pomeditor.CheckSum.checksum;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PomCommitTest extends BaseTest {

    @Test
    void shouldCommit() throws IOException, NoSuchAlgorithmException {

        Path backup = newDummyBackup(pom);

        String expectedCheckSum = checksum(pom);
        assertNotEquals(expectedCheckSum, checksum(backup));

        PomCommit.execute(pom);

        assertTrue(pom.toFile().exists());
        assertFalse(backup.toFile().exists());

        assertEquals(expectedCheckSum, checksum(pom));

    }

}
