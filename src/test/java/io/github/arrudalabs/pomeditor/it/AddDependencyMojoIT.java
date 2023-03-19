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

package io.github.arrudalabs.pomeditor.it;

import com.soebes.itf.jupiter.extension.MavenGoal;
import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.extension.SystemProperty;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;
import org.assertj.core.api.Assertions;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.nio.file.Path;

import static com.soebes.itf.extension.assertj.MavenExecutionResultAssert.assertThat;

@MavenJupiterExtension
public class AddDependencyMojoIT {

    @MavenTest
    @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:add-dep")
    @SystemProperty(value = "groupId", content = "myGroupId")
    @SystemProperty(value = "artifactId", content = "myArtifactId")
    @SystemProperty(value = "version", content = "1.1.1")
    void add_dep_project_without_dependencies(MavenExecutionResult result) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        assertThat(result)
                .isSuccessful();

        Path baseDir = result.getMavenProjectResult().getTargetProjectDirectory();

        Path pom = Path.of(baseDir.toString(), "pom.xml");

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        // optional, but recommended
        // process XML securely, avoid attacks like XML External Entities (XXE)
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setIgnoringComments(false);

        // parse XML file
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document doc = db.parse(pom.toFile());

        XPath xPath = XPathFactory.newInstance().newXPath();
        String expression = String.format(
                "/project/dependencies/dependency[groupId=\"%s\" and artifactId=\"%s\" and version=\"%s\"]",
                "myGroupId", "myArtifactId", "1.1.1");
        NodeList dependencies = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

        Assertions.assertThat(dependencies.getLength()).isEqualTo(1);

    }

}
