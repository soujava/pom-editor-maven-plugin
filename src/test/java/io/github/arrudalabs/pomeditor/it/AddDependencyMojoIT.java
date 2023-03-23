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
import com.soebes.itf.jupiter.extension.MavenOption;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.extension.SystemProperty;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.soebes.itf.extension.assertj.MavenExecutionResultAssert.assertThat;

@MavenJupiterExtension
@MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:add-dep")
@SystemProperty(value = "gav", content = AddDependencyMojoIT.orgJunitJupiterGav)
public class AddDependencyMojoIT {

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


    static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    static {
        try {
            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbf.setIgnoringComments(false);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    static Document getDoc(Path path) {
        // parse XML file
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(path.toFile());
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    private static String xmlPathToGetDependencies(String groupId, String artifactId, String version) {
        StringBuilder xpathExpression = new StringBuilder("/project/dependencies/dependency");
        List<String> attributes = new ArrayList<>();
        if (groupId != null)
            attributes.add("groupId=\"" + groupId + "\"");
        if (artifactId != null)
            attributes.add("artifactId=\"" + artifactId + "\"");
        if (version != null)
            attributes.add("version=\"" + version + "\"");
        if (!attributes.isEmpty()) {
            xpathExpression.append("[");
            xpathExpression.append(attributes.stream().collect(Collectors.joining(" and ")));
            xpathExpression.append("]");
        }
        return xpathExpression.toString();
    }

    private static NodeList findByXPath(Document doc, String xpathExpression) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList dependencies = (NodeList) xPath.compile(xpathExpression).evaluate(doc, XPathConstants.NODESET);
        return dependencies;
    }

}
