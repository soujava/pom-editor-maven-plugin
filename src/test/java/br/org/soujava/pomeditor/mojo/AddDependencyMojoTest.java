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


import br.org.soujava.pomeditor.InvalidGroupIdArtifactIdArgs;
import br.org.soujava.pomeditor.api.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AddDependencyMojoTest extends EditingMojoTest {

    @Mock
    BiConsumer<Path, Dependency> addDependencyCommand;

    @Captor
    ArgumentCaptor<Dependency> dependencyToBeAdded;

    @DisplayName("should return error when")
    @ParameterizedTest(name = "groupId={0}, artifactId={1}")
    @ArgumentsSource(InvalidGroupIdArtifactIdArgs.class)
    void shouldReturnErrorsForInvalidRequiredParameters(final String groupId,
                                                        final String artifactId) {
        Assertions.assertThrows(MojoExecutionException.class, () -> {
            AddDependencyMojo mojo = newMojo();
            mojo.gav = Arrays.stream(new String[]{groupId, artifactId})
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(":"));
            mojo.execute();
        });

        verify(backupFunction, never()).apply(any());
        verify(rollbackFunction, never()).accept(any());
    }

    @Test
    void shouldAddDependencyProperly() throws MojoExecutionException, MojoFailureException {

        //Given
        String expectedGroupId = "groupId";
        String expectedArtifactId = "artifactId";
        String expectedVersion = "222";

        AddDependencyMojo mojo = newMojo();
        mojo.gav = expectedGroupId + ":" + expectedArtifactId + ":" + expectedVersion;
        mojo.type = "jar";
        mojo.classifier = "classified";
        mojo.scope = "compile";


        //When
        mojo.execute();

        //Then
        verify(addDependencyCommand, atLeastOnce()).accept(targetPom.capture(), dependencyToBeAdded.capture());
        verify(backupFunction, atLeastOnce()).apply(any(Path.class));
        verify(rollbackFunction, never()).accept(any());
        verify(log, atLeastOnce()).info(anyString());

        var addDependency = this.dependencyToBeAdded.getValue();
        assertThat(targetPom.getValue()).isEqualTo(Path.of(mojo.pom));
        assertThat(addDependency.getGroupId()).isEqualTo(expectedGroupId);
        assertThat(addDependency.getArtifactId()).isEqualTo(expectedArtifactId);
        assertThat(addDependency.getVersion()).isEqualTo(expectedVersion);
        assertThat(addDependency.getType()).isEqualTo(mojo.type);
        assertThat(addDependency.getClassifier()).isEqualTo(mojo.classifier);
        assertThat(addDependency.getScope()).isEqualTo(mojo.scope);

    }

    private AddDependencyMojo newMojo() {
        AddDependencyMojo mojo = new AddDependencyMojo();
        mojo.pom = this.pom.toString();
        mojo.setLog(log);
        mojo.backupFunction = backupFunction;
        mojo.rollbackFunction = rollbackFunction;
        mojo.addDependencyCommand =
                (path, dep) -> addDependencyCommand.accept(path, dep);
        return mojo;
    }

}
