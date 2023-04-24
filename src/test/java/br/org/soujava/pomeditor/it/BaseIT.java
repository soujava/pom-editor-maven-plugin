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

package br.org.soujava.pomeditor.it;

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

public abstract class BaseIT {

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

    static String xmlPathToGetDependencies(String groupId, String artifactId, String version) {
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

    static NodeList findByXPath(Document doc, String xpathExpression) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList dependencies = (NodeList) xPath.compile(xpathExpression).evaluate(doc, XPathConstants.NODESET);
        return dependencies;
    }
}
