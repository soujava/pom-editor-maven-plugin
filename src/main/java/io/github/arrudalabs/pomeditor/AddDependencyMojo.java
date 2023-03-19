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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.l2x6.pom.tuner.PomTransformer;
import org.l2x6.pom.tuner.PomTransformer.Transformation;
import org.l2x6.pom.tuner.model.Gavtcs;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiConsumer;

/**
 * add a given dependency to a target POM
 */
@Mojo(name = "add-dep")
public class AddDependencyMojo extends AbstractMojo {

    private static BiConsumer<Path, AddDependencyMojo> defaultPomTransformer = (pomPath, params) -> {
        new PomTransformer(pomPath, StandardCharsets.UTF_8,
                PomTransformer.SimpleElementWhitespace.AUTODETECT_PREFER_EMPTY)
                .transform(
                        Transformation.addDependencyIfNeeded(
                                new Gavtcs(params.groupId,
                                        params.artifactId,
                                        params.version,
                                        params.type,
                                        params.classifier,
                                        params.scope),
                                Gavtcs.scopeAndTypeFirstComparator())
                );
    };

    @Parameter(property = "groupId")
    String groupId;
    @Parameter(property = "artifactId")
    String artifactId;
    @Parameter(property = "version", defaultValue = "")
    String version = "";
    @Parameter(property = "type", defaultValue = "")
    String type = "";
    @Parameter(property = "classifier", defaultValue = "")
    String classifier = "";
    @Parameter(property = "scope", defaultValue = "")
    String scope = "";
    @Parameter(property = "pom", defaultValue = "pom.xml")
    String pom = "pom.xml";

    BiConsumer<Path, AddDependencyMojo> pomTransformer;

    BiConsumer<Path, AddDependencyMojo> getPomTransformer() {
        if (this.pomTransformer == null) {
            return defaultPomTransformer;
        }
        return this.pomTransformer;
    }

    void validate() throws MojoExecutionException {
        validateGroupId();
        validateArtifactIt();
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        validate();
        try {
            getPomTransformer().accept(Paths.get(pom), this);
        } catch (RuntimeException ex) {
            throw new MojoExecutionException(ex);
        }
    }

    private boolean isNullOrBlank(String artifactId) {
        return artifactId == null || artifactId.isBlank();
    }

    private void validateArtifactIt() throws MojoExecutionException {
        if (isNullOrBlank(artifactId)) {
            throw new MojoExecutionException("artifactId cannot be null or empty");
        }
    }

    private void validateGroupId() throws MojoExecutionException {
        if (isNullOrBlank(groupId)) {
            throw new MojoExecutionException("groupId cannot be null or empty");
        }
    }

}
