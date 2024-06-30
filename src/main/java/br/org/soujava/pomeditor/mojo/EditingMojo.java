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

import br.org.soujava.pomeditor.api.PomChange;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Base class for {@link org.apache.maven.plugin.Mojo} functionality,
 * containing common code for editing POM files, and unit testing.
 */
public abstract class EditingMojo extends AbstractMojo {

    @Parameter(property = "pom", defaultValue = "pom.xml")
    String pom = "pom.xml";


    protected Function<Path, Boolean> backupFunction;
    protected Consumer<Path> rollbackFunction;


    protected PomChange change(Path pomFile) {
        return PomChange
                .builder()
                .withLogger(getLog()::info)
                .withPom(pomFile)
                .withBackupFunction(backupFunction)
                .withRollbackFunction(rollbackFunction)
                .build();
    }

}
