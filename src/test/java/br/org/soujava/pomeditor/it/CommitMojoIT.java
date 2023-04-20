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

package br.org.soujava.pomeditor.it;

import com.soebes.itf.jupiter.extension.MavenGoal;
import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;
import com.soebes.itf.jupiter.maven.MavenProjectResult;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.Path;
import java.nio.file.Paths;

import static br.org.soujava.pomeditor.CheckSum.checksum;
import static com.soebes.itf.extension.assertj.MavenExecutionResultAssert.assertThat;

@MavenJupiterExtension
@MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:commit")
class PomCommitMojoIT {

    String expectedChecksum;

    @BeforeEach
    void beforeEach(MavenProjectResult project) throws Exception {
        Path pom = project.getTargetProjectDirectory().resolve("pom.xml");
        expectedChecksum = checksum(pom);
    }


    @MavenTest
    @DisplayName("committing the changes - it will remove the backup file")
    void project_with_backup(MavenExecutionResult result) throws Exception {
        assertThat(result).isSuccessful();
        Path baseDirectory = result.getMavenProjectResult().getTargetProjectDirectory();
        Path pom = Path.of(baseDirectory.toString(), "pom.xml");
        Path backupPom = Paths.get(baseDirectory.toString(), "pom.xml.backup");
        Assertions.assertThat(backupPom).doesNotExist();
        Assertions.assertThat(checksum(pom)).isEqualTo(expectedChecksum);
    }
}
