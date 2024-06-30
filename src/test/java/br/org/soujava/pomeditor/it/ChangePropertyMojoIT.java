/*
 * Copyright 2024  the original author or authors.
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

import static com.soebes.itf.extension.assertj.MavenExecutionResultAssert.assertThat;

@MavenJupiterExtension
@MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:change-prop")
// @MavenOption("-X") // for debugging
@SystemProperty(value = "property", content = ChangePropertyMojoIT.propName)
@SystemProperty(value = "value", content = ChangePropertyMojoIT.value)
public class ChangePropertyMojoIT extends EditingMojoIT {

    static final String propName = "uncertainty";
    static final String value = "principal";

    @MavenTest
    @DisplayName("property added when there were no properties")
    void had_no_properties(MavenExecutionResult result) throws Exception {
        validateProperty(result, 1);
    }

    @MavenTest
    @DisplayName("property added when there was no property")
    void had_no_property(MavenExecutionResult result) throws Exception {
        validateProperty(result, 2);
    }

    @MavenTest
    @DisplayName("property unchanged when it was set to the same value")
    void had_same_property(MavenExecutionResult result) throws Exception {
        validateProperty(result, 2);
    }

    @MavenTest
    @DisplayName("property changed when it was set to a different value")
    void had_different_property(MavenExecutionResult result) throws Exception {
        validateProperty(result, 2);
    }

    private static void validateProperty(MavenExecutionResult result, int totalProps) throws XPathExpressionException {
        assertThat(result)
                .isSuccessful();
        Path baseDir = result.getMavenProjectResult().getTargetProjectDirectory();
        shouldHaveGeneratedBackFile(baseDir);
        shouldHaveProperty(baseDir, totalProps, propName, value);
    }

    private static void shouldHaveProperty(Path baseDir, int totalProps, String name, String value) throws XPathExpressionException {

        Path pom = Path.of(baseDir.toString(), "pom.xml");

        Document doc = getDoc(pom);

        NodeList nlp = findByXPath(doc, "/project/properties/*");
        Assertions.assertThat(nlp.getLength()).isEqualTo(totalProps);

        NodeList nl = findByXPath(doc, "/project/properties/"+name);
        Assertions.assertThat(nl.getLength()).isEqualTo(1);

        Assertions.assertThat(nl.item(0).getTextContent()).isEqualTo(value);
    }

}
