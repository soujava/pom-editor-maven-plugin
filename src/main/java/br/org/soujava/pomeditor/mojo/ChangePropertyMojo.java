/*
 * Copyright 2024  the original author or authors.
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

import br.org.soujava.pomeditor.api.ChangeProperty;
import br.org.soujava.pomeditor.api.Property;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Mojo responsible for setting (potentially overwriting), or removing a property value.
 */
@Mojo(name = "change-prop")
public class ChangePropertyMojo extends EditingMojo {

    @Parameter(property = "property", required = true)
    String property;
    @Parameter(property = "value")
    String value;

    BiConsumer<Path, Property> changePropertyCommand;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        Path pomFile = Paths.get(pom);
        Property property = buildProperty();

        try {

            getLog().info(String.format("Changing property %s on \"%s\" POM file...", property, pomFile));

            change(pomFile).execute(() -> changePropertyCommand().accept(pomFile, property));

            getLog().info(String.format("Handled property %s on \"%s\" POM file.", property, pomFile));

        } catch (Throwable ex) {
            throw new MojoFailureException(String.format("Failed to handle property %s on \"%s\" POM file: %s",
                    property,
                    pomFile,
                    ex.getMessage()), ex);
        }
    }

    private BiConsumer<Path, Property> changePropertyCommand() {
        return Optional
                .ofNullable(this.changePropertyCommand)
                .orElse((path,prop)->ChangeProperty.execute(getLog(), path, prop));
    }

    private Property buildProperty() throws MojoExecutionException {
        try {
            return Property.of(property, value).build();
        } catch (RuntimeException ex) {
            throw new MojoExecutionException(ex.getMessage(), ex);
        }
    }


}
