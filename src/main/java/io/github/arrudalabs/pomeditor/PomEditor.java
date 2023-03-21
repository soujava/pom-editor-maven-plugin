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

package io.github.arrudalabs.pomeditor;

import org.l2x6.pom.tuner.PomTransformer;
import org.l2x6.pom.tuner.model.Gavtcs;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * The POM editor instance
 */
class PomEditor {
    /**
     * Add a dependency informed by the {@link Dependency} instance into the target POM xml
     * @param pom it's the target POM xml
     * @param dependency it's an {@link Dependency} instance
     */
    void execute(Path pom, Dependency dependency) {
        new PomTransformer(
                pom,
                StandardCharsets.UTF_8,
                PomTransformer.SimpleElementWhitespace.AUTODETECT_PREFER_SPACE)
                .transform(addOrUpdateDependencyIfNeeded(dependency));
    }

    private PomTransformer.Transformation addOrUpdateDependencyIfNeeded(Dependency dependencyToBeAdded) {
        return (document, context) -> {

            Gavtcs dependency = toGavtcs(dependencyToBeAdded);

            List<PomTransformer.NodeGavtcs> dependencies = context.getDependencies()
                    .stream()
                    .filter(nodeGavtcs -> nodeGavtcs.getGroupId().equals(dependency.getGroupId()))
                    .filter(nodeGavtcs -> nodeGavtcs.getArtifactId().equals(dependency.getArtifactId()))
                    .collect(Collectors.toList());

            dependencies.stream().forEach(
                    nodeGavtcs -> {

                        BiConsumer<String, String> setValue = nodeChanger(nodeGavtcs);
                        setValue.accept("version", dependency.getVersion());
                        setValue.accept("type", dependency.getType());
                        setValue.accept("classifier", dependency.getClassifier());
                        setValue.accept("scope", dependency.getScope());

                    });

            if (dependencies.isEmpty()) {
                context.addDependencyIfNeeded(dependency, Gavtcs.scopeAndTypeFirstComparator());
            }
        };
    }

    private static Gavtcs toGavtcs(Dependency dependency) {
        return new Gavtcs(
                dependency.getGroupId(),
                dependency.getArtifactId(),
                dependency.getVersion(),
                dependency.getType(),
                dependency.getClassifier(),
                dependency.getScope()
        );
    }

    private static BiConsumer<String, String> nodeChanger(PomTransformer.NodeGavtcs nodeGavtcs) {
        final PomTransformer.ContainerElement node = nodeGavtcs.getNode();

        return (name, value) -> {
            Optional<PomTransformer.ContainerElement> targetNode = node.childElementsStream()
                    .filter(ch -> name.equals(ch.getNode().getLocalName()))
                    .findFirst();
            if (!targetNode.isPresent() && value == null) {
                /* nothing to do */
            } else if (!targetNode.isPresent()) {
                node.addChildTextElement(name, value);
            } else if (value == null) {
                targetNode.get().remove(true, true);
            } else {
                targetNode.get().getNode().setTextContent(value);
            }
        };
    }
}
