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

package br.org.soujava.pomeditor.newapi;

import org.l2x6.pom.tuner.PomTransformer;
import org.l2x6.pom.tuner.model.Gavtcs;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class UpdateOrAddDependency implements ChangePom, ChangeFromProfile, ChangeFromDependencyManagement {

    public static UpdateOrAddDependency build() {
        return new UpdateOrAddDependency(null, null, null, null, null, null);
    }

    public UpdateOrAddDependency withGav(String gav) {
        var gavValues = Arrays.stream(gav.split(":"))
                .filter(Objects::nonNull)
                .filter(item -> !item.isBlank())
                .map(String::trim)
                .collect(Collectors.toList());
        var builder = UpdateOrAddDependency.build()
                .withGroupId(this.groupId)
                .withArtifactId(this.artifactId)
                .withVersion(this.version)
                .withType(this.type)
                .withClassifier(this.classifier)
                .withScope(this.scope);
        if (gavValues.size() >= 1)
            builder = builder.withGroupId(gavValues.get(0));
        if (gavValues.size() >= 2)
            builder = builder.withArtifactId(gavValues.get(1));
        if (gavValues.size() >= 3)
            builder = builder.withVersion(gavValues.get(2));
        return builder;
    }

    final String groupId;
    final String artifactId;
    final String version;
    final String type;
    final String classifier;
    final String scope;

    private UpdateOrAddDependency(
            String groupId,
            String artifactId,
            String version,
            String type,
            String classifier,
            String scope) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.type = type;
        this.classifier = classifier;
        this.scope = scope;
    }

    public UpdateOrAddDependency withGroupId(String groupId) {
        return new UpdateOrAddDependency(groupId, this.artifactId, this.version, this.type, this.classifier, this.scope);
    }

    public UpdateOrAddDependency withArtifactId(String artifactId) {
        return new UpdateOrAddDependency(this.groupId, artifactId, this.version, this.type, this.classifier, this.scope);
    }

    public UpdateOrAddDependency withVersion(String version) {
        return new UpdateOrAddDependency(this.groupId, this.artifactId, version, this.type, this.classifier, this.scope);
    }

    public UpdateOrAddDependency withType(String type) {
        return new UpdateOrAddDependency(this.groupId, this.artifactId, this.version, type, this.classifier, this.scope);
    }

    public UpdateOrAddDependency withClassifier(String classifier) {
        return new UpdateOrAddDependency(this.groupId, this.artifactId, this.version, this.type, classifier, this.scope);
    }

    public UpdateOrAddDependency withScope(String scope) {
        return new UpdateOrAddDependency(this.groupId, this.artifactId, this.version, this.type, this.classifier, scope);
    }

    @Override
    public void execute(Pom target) {
        validate();
        // TODO test is missing
        new PomTransformer(
                target.pomFile,
                StandardCharsets.UTF_8,
                PomTransformer.SimpleElementWhitespace.AUTODETECT_PREFER_SPACE)
                .transform(addOrUpdateDependencyIfNeededDirectToPom());
    }

    private void validate() {
        if (this.groupId == null || this.groupId.isBlank())
            throw new IllegalArgumentException("groupId must be provided");

        if (this.artifactId == null || this.artifactId.isBlank())
            throw new IllegalArgumentException("artifactId must be provided");
    }

    private PomTransformer.Transformation addOrUpdateDependencyIfNeededDirectToPom() {
        return (document, context) -> {

            Gavtcs dependency = toGavtcs();

            var dependencies = context.getDependencies()
                    .stream()
                    .filter(nodeGavtcs -> nodeGavtcs.getGroupId().equals(dependency.getGroupId()))
                    .filter(nodeGavtcs -> nodeGavtcs.getArtifactId().equals(dependency.getArtifactId()))
                    .collect(Collectors.toList());

            dependencies.forEach(nodeGavtcs -> {

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

    private Gavtcs toGavtcs() {
        return new Gavtcs(
                this.groupId,
                this.artifactId,
                this.version,
                this.type,
                this.classifier,
                this.scope
        );
    }

    private BiConsumer<String, String> nodeChanger(PomTransformer.NodeGavtcs nodeGavtcs) {
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


    @Override
    public void execute(Pom pom, UpdateOrAddProfile profile) {
        // TODO test is missing
        validate();
        new PomTransformer(
                pom.pomFile,
                StandardCharsets.UTF_8,
                PomTransformer.SimpleElementWhitespace.AUTODETECT_PREFER_SPACE)
                .transform(addOrUpdateDependencyIfNeededIntoProfile(profile));

    }

    private PomTransformer.Transformation addOrUpdateDependencyIfNeededIntoProfile(UpdateOrAddProfile profile) {
        return (document, context) -> {

            Gavtcs dependency = toGavtcs();

            context.getProfile(profile.id)
                    .ifPresent(profileElement -> {
                        var dependenciesNode = profileElement.getOrAddChildContainerElement("dependencies");
                        updateOrAddDependencyIntoDependencyManagement(dependenciesNode, dependency);
                    });

        };

    }

    private void updateOrAddDependencyIntoDependencyManagement(PomTransformer.ContainerElement dependenciesNode, Gavtcs dependency) {
        var dependencies = dependenciesNode.childElementsStream()
                .map(PomTransformer.ContainerElement::asGavtcs)
                .filter(nodeGavtcs -> nodeGavtcs.getGroupId().equals(dependency.getGroupId()))
                .filter(nodeGavtcs -> nodeGavtcs.getArtifactId().equals(dependency.getArtifactId()))
                .collect(Collectors.toList());

        dependencies.forEach(nodeGavtcs -> {

            BiConsumer<String, String> setValue = nodeChanger(nodeGavtcs);
            setValue.accept("version", dependency.getVersion());
            setValue.accept("type", dependency.getType());
            setValue.accept("classifier", dependency.getClassifier());
            setValue.accept("scope", dependency.getScope());

        });

        if (dependencies.isEmpty()) {
            dependenciesNode.addGavtcsIfNeeded(dependency, Gavtcs.scopeAndTypeFirstComparator());
        }
    }

    @Override
    public void execute(Pom pom, UpdateOrAddProfile profile, UpdateOrAddDependencyManagement dependencyManagement) {
        // TODO test is missing
        validate();
        new PomTransformer(
                pom.pomFile,
                StandardCharsets.UTF_8,
                PomTransformer.SimpleElementWhitespace.AUTODETECT_PREFER_SPACE)
                .transform(addOrUpdateDependencyIfNeededIntoDependencyManagementFromProfile(profile, dependencyManagement));
    }

    private PomTransformer.Transformation addOrUpdateDependencyIfNeededIntoDependencyManagementFromProfile
            (UpdateOrAddProfile profile, UpdateOrAddDependencyManagement dependencyManagement) {
        if (profile == null) {
            return addOrUpdateDependencyIfNeededIntoDependencyManagementFromDefaultProfile(dependencyManagement);
        }
        return (document, context) -> {
            // TODO test is missing
            context.getProfile(profile.id)
                    .ifPresent(profileElement -> {
                        Gavtcs dependency = toGavtcs();
                        var dependencyManagementElement = profileElement.getOrAddChildContainerElement("dependencyManagement");
                        updateOrAddDependencyIntoDependencyManagement(dependencyManagementElement, dependency);
                    });
        };
    }

    private PomTransformer.Transformation addOrUpdateDependencyIfNeededIntoDependencyManagementFromDefaultProfile(UpdateOrAddDependencyManagement dependencyManagement) {
        return (document, context) -> {
            // TODO test is missing
            Gavtcs dependency = toGavtcs();
            var dependencyManagementElement = context.getOrAddContainerElement("dependencyManagement");
            updateOrAddDependencyIntoDependencyManagement(dependencyManagementElement, dependency);
        };
    }

}