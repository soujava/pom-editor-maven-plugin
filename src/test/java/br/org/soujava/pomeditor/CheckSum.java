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

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Component to be used to get M5 checksum data from files
 */
public interface CheckSum {

    /**
     * Return the MD5 checksum from given a file
     * @param file file to be used in the MD5 calculation
     * @return a string with the MD5 checksum
     * @throws IOException {@see Files.readAllBytes(Path)}
     * @throws NoSuchAlgorithmException {@see MessageDigest.getInstance(String)}
     */
    static String checksum(Path file) throws IOException, NoSuchAlgorithmException {
        byte[] data = Files.readAllBytes(file);
        byte[] hash = MessageDigest.getInstance("MD5").digest(data);
        String checksum = new BigInteger(1, hash).toString(16);
        return checksum;
    }
}
