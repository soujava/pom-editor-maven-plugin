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

package br.org.soujava.pomeditor.mojo;

import br.org.soujava.pomeditor.newapi.Dependency;
import br.org.soujava.pomeditor.newapi.DependencyManagement;
import br.org.soujava.pomeditor.newapi.Pom;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Mojo responsible to add a given dependency to a dependencyManagement node of a given  POM
 * if such dependency is not declared
 * or the given dependency's version is greater than the existent at target POM
 */
@Mojo(name = "add-mdep")
public class AddManagedDependencyMojo extends AbstractMojo {

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

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        Path pomFile = Paths.get(pom);
        try {
            getLog().info(String.format("trying to add the dependency: %s to the \"%s\" as managed dependency into the file...", toString(), pomFile));
            Pom.from(pomFile)
                    .updateOrAdd(
                            DependencyManagement
                                    .updateOrAdd(Dependency
                                            .updateOrAdd()
                                            .withGav(gav)
                                    ));

            getLog().info(String.format("added the dependency: %s as managed dependency into the \"%s\" file.", toString(), pomFile));
        } catch (Throwable ex) {
            throw new MojoFailureException(String.format("cannot add the dependency: %s as managed dependency into the \"%s\" file: %s",
                    toString(),
                    pomFile,
                    ex.getMessage()), ex);
        }
    }


    @Override
    public String toString() {
        return "{" +
                "gav='" + gav + '\'' +
                ", type='" + type + '\'' +
                ", classifier='" + classifier + '\'' +
                ", scope='" + scope + '\'' +
                ", pom='" + pom + '\'' +
                '}';
    }
}
