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
//@MavenOption("-X") // for debugging
@SystemProperty(value = "property", content = RemovePropertyMojoIT.propName)
//@SystemProperty(value = "value") // or else it's set to "true"
public class RemovePropertyMojoIT extends EditingMojoIT {

    static final String propName = "disappear";

    @MavenTest
    @DisplayName("no changes to pom if no <properties>")
    void had_no_properties(MavenExecutionResult result) throws Exception {
        validateProperty(result, false, 0);
    }

    @MavenTest
    @DisplayName("no changes to pom if no specific property")
    void had_no_property(MavenExecutionResult result) throws Exception {
        validateProperty(result, true, 1);
    }

    @MavenTest
    @DisplayName("property removed")
    void property_removed(MavenExecutionResult result) throws Exception {
        validateProperty(result, true, 1);
    }

    @MavenTest
    @DisplayName("the only property removed")
    void the_only_property_removed(MavenExecutionResult result) throws Exception {
        validateProperty(result, true, 0);
    }

    private static void validateProperty(MavenExecutionResult result, boolean propertiesNode, int propsLeft) throws XPathExpressionException {
        assertThat(result)
                .isSuccessful();
        Path baseDir = result.getMavenProjectResult().getTargetProjectDirectory();
        shouldHaveGeneratedBackFile(baseDir);
        shouldNotHaveProperty(baseDir, propName, propertiesNode, propsLeft);
    }

    private static void shouldNotHaveProperty(Path baseDir, String name, boolean propertiesNode, int propsLeft) throws XPathExpressionException {

        Path pom = Path.of(baseDir.toString(), "pom.xml");

        Document doc = getDoc(pom);

        if (!propertiesNode) {
            NodeList properties = findByXPath(doc, "/project/properties");
            Assertions.assertThat(properties.getLength()).isEqualTo(0);
            return;
        }

        NodeList nlp = findByXPath(doc, "/project/properties/*");
        Assertions.assertThat(nlp.getLength()).isEqualTo(propsLeft);

        NodeList nl = findByXPath(doc, "/project/properties/"+name);
        Assertions.assertThat(nl.getLength()).isEqualTo(0);

    }

}
