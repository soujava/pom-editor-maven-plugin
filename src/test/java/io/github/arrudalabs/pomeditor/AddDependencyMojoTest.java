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


import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AddDependencyMojoTest {

    @Mock
    PomEditor pomEditor;

    @Mock
    Log log;

    @Mock
    BiConsumer<Log, Path> backupFunction;

    @Mock
    BiConsumer<Log, Path> rollbackFunction;

    @Captor
    ArgumentCaptor<AddDependency> addDependency;

    @DisplayName("should return error when")
    @ParameterizedTest(name = "groupId={0}, artifactId={1}")
    @MethodSource("invalidParameters")
    void shouldReturnErrorsForInvalidRequiredParameters(final String groupId,
                                                        final String artifactId) {
        Assertions.assertThrows(MojoExecutionException.class, () -> {
            AddDependencyMojo mojo = new AddDependencyMojo();
            mojo.backupFunction = backupFunction;
            mojo.rollbackFunction = rollbackFunction;
            mojo.groupId = groupId;
            mojo.artifactId = artifactId;
            mojo.execute();
        });

        verify(backupFunction, atLeastOnce()).accept(any(), any());
        verify(rollbackFunction, atLeastOnce()).accept(any(), any());
    }

    static Stream<Arguments> invalidParameters() {
        return Stream.of(
                arguments(
                        null, // groupId,
                        null // artifactId,
                ),
                arguments(
                        null, // groupId,
                        "artifactId" // artifactId,
                ),
                arguments(
                        "groupId", // groupId,
                        null // artifactId,
                ),
                arguments(
                        "", // groupId,
                        "" // artifactId,
                ),
                arguments(
                        "", // groupId,
                        "artifactId" // artifactId,
                ),
                arguments(
                        "groupId", // groupId,
                        "" // artifactId,
                )
        );
    }


    @Test
    void shouldAddDependencyProperly() throws MojoExecutionException, MojoFailureException {

        //Given
        AddDependencyMojo mojo = newMojo();
        mojo.groupId = "groupId";
        mojo.artifactId = "artifactId";
        mojo.version = "222";
        mojo.type = "jar";
        mojo.classifier = "classified";
        mojo.scope = "compile";
        mojo.pomEditor = pomEditor;


        //When
        mojo.execute();

        //Then
        verify(pomEditor, atLeastOnce()).execute(addDependency.capture());
        verify(backupFunction, atLeastOnce()).accept(any(), any());
        verify(rollbackFunction, never()).accept(any(), any());
        verify(log, atLeastOnce()).info(anyString());


        var addDependency = this.addDependency.getValue();
        assertThat(addDependency.pom).isEqualTo(Path.of(mojo.pom));
        assertThat(addDependency.dependency.getGroupId()).isEqualTo(mojo.groupId);
        assertThat(addDependency.dependency.getArtifactId()).isEqualTo(mojo.artifactId);
        assertThat(addDependency.dependency.getVersion()).isEqualTo(mojo.version);
        assertThat(addDependency.dependency.getType()).isEqualTo(mojo.type);
        assertThat(addDependency.dependency.getClassifier()).isEqualTo(mojo.classifier);
        assertThat(addDependency.dependency.getScope()).isEqualTo(mojo.scope);

    }

    private AddDependencyMojo newMojo() {
        AddDependencyMojo mojo = new AddDependencyMojo();
        mojo.setLog(log);
        mojo.backupFunction = backupFunction;
        mojo.rollbackFunction = rollbackFunction;
        return mojo;
    }

}
