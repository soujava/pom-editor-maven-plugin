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

package br.org.soujava.pomeditor;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Mojo responsible to add a given dependency to a target POM
 * if such dependency is not declared
 * or the given dependency's version is greater than the existent at target POM
 */
@Mojo(name = "add-dep")
public class AddDependencyMojo extends AbstractMojo {

    @Parameter(property = "gav")
    String gav;
    @Parameter(property = "type")
    String type;
    @Parameter(property = "classifier")
    String classifier;
    @Parameter(property = "scope")
    String scope;
    @Parameter(property = "pom", defaultValue = "pom.xml")
    String pom = "pom.xml";

    PomEditor pomEditor;

    BiConsumer<Log, Path> backupFunction;

    BiConsumer<Log, Path> rollbackFunction;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        Path pomFile = Paths.get(pom);
        Dependency dependency = buildDependency();
        boolean doRollback = false;
        try {
            Optional.ofNullable(backupFunction)
                    .orElse(PomChangeTransaction::backup)
                    .accept(getLog(), pomFile);

            Optional.ofNullable(pomEditor)
                    .orElseGet(PomEditor::new)
                    .execute(pomFile, dependency);

            getLog().info(String.format("added the dependency: %s to the pom: %s ", dependency, pomFile));
        } catch (RuntimeException ex) {
            doRollback = true;
            throw new MojoExecutionException(ex);
        } finally {
            if (doRollback) {
                Optional.ofNullable(rollbackFunction)
                        .orElse(PomChangeTransaction::rollback)
                        .accept(getLog(), pomFile);
            }
        }
    }

    private Dependency buildDependency() throws MojoExecutionException {
        try {
            return Dependency.ofGav(gav)
                    .setType(type)
                    .setClassifier(classifier)
                    .setScope(scope)
                    .build();
        } catch (RuntimeException ex) {
            throw new MojoExecutionException(ex);
        }
    }
}
