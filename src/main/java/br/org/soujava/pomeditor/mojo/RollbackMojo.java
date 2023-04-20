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

import br.org.soujava.pomeditor.control.PomChange;
import br.org.soujava.pomeditor.control.Rollback;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Restore the target POM file with the backup POM file
 */
@Mojo(name = "rollback")
public class RollbackMojo extends AbstractMojo {

    @Parameter(property = "pom", defaultValue = "pom.xml")
    String pom = "pom.xml";

    Consumer<Path> rollbackFunction;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Path targetPom = Path.of(this.pom);
        try {
            getLog().info(String.format("trying to revert the \"%s\" file...", targetPom));
            Optional.ofNullable(rollbackFunction)
                    .orElse(Rollback::execute)
                    .accept(targetPom);
            getLog().info(String.format("\"%s\" file has been recovered. Backup file was deleted successfully.", targetPom));
        } catch (RuntimeException ex) {
            throw new MojoFailureException(
                    String.format("cannot recover the \"%s\" file: %s", targetPom,ex.getMessage()), ex);
        }
    }
}
