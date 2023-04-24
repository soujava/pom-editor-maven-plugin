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
import com.soebes.itf.jupiter.extension.SystemProperty;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpressionException;
import java.nio.file.Path;

import static br.org.soujava.pomeditor.it.BaseIT.findByXPath;
import static br.org.soujava.pomeditor.it.BaseIT.getDoc;
import static br.org.soujava.pomeditor.it.BaseIT.xmlPathToGetDependencies;
import static com.soebes.itf.extension.assertj.MavenExecutionResultAssert.assertThat;

@MavenJupiterExtension
@MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:add-dep2")
@SystemProperty(value = "gav", content = V2AddDependencyMojoIT.orgJunitJupiterGav)
public class V2AddDependencyMojoIT {

    static final String orgJunitJupiterGroupId = "org.junit.jupiter";
    static final String junitJupiterArtifactId = "junit-jupiter";
    static final String version5_9_0 = "5.9.0";
    static final String orgJunitJupiterGav = orgJunitJupiterGroupId + ":" + junitJupiterArtifactId + ":" + version5_9_0;

    @MavenTest
    @DisplayName("adding given a dependency into a POM without dependencies")
    void no_dependencies_declarated(MavenExecutionResult result) throws Exception {
        shouldAddDependency(result);
    }


    @MavenTest
    @DisplayName("adding given a dependency into a POM with other dependencies")
    void with_other_dependencies(MavenExecutionResult result) throws Exception {
        shouldAddDependency(result);
    }

    @MavenTest
    @DisplayName("adding given a dependency into a POM that has the dependency already")
    void has_dependency_already(MavenExecutionResult result) throws Exception {
        shouldAddDependency(result);
    }

    @MavenTest
    @DisplayName("adding given a dependency into a POM that has the dependency already but with version higher")
    void has_dependency_but_with_version_higher(MavenExecutionResult result) throws Exception {
        shouldAddDependency(result);
    }

    @MavenTest
    @DisplayName("adding given a dependency into a POM that has the dependency already but with version higher")
    void has_dependency_but_with_version_lower(MavenExecutionResult result) throws Exception {
        shouldAddDependency(result);
    }

    private static void shouldAddDependency(MavenExecutionResult result) throws XPathExpressionException {
        assertThat(result)
                .isSuccessful();
        Path baseDir = result.getMavenProjectResult().getTargetProjectDirectory();
        shouldHaveGeneratedBackFile(baseDir);
        shouldAddDependency(baseDir, orgJunitJupiterGroupId, junitJupiterArtifactId, version5_9_0);
    }

    private static void shouldHaveGeneratedBackFile(Path baseDir) {
        Path backup = Path.of(baseDir.toString(), "pom.xml.backup");
        Assertions.assertThat(backup).exists();
    }

    private static void shouldAddDependency(Path baseDir, String groupId, String artifactId, String version) throws XPathExpressionException {

        Path pom = Path.of(baseDir.toString(), "pom.xml");

        Document doc = getDoc(pom);

        String expression = xmlPathToGetDependencies(groupId, artifactId, version);

        NodeList dependencies = findByXPath(doc, expression);

        Assertions.assertThat(dependencies.getLength()).isEqualTo(1);
    }


}
