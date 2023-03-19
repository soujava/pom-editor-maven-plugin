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
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AddDependencyMojoTest {


    @DisplayName("should return error when")
    @ParameterizedTest(name = "{index} groupId={0}, artifactId={1}, version={2}")
    @MethodSource("invalidParameters")
    void shouldReturnErrorsForInvalidRequiredParameters(final String groupId,
                                                        final String artifactId) {

        AddDependencyMojo mojo = new AddDependencyMojo();
        mojo.groupId = groupId;
        mojo.artifactId = artifactId;

        Assertions.assertThrows(MojoExecutionException.class, () -> {
            mojo.validate();
        });
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

    @Mock
    BiConsumer<Path, AddDependencyMojo> pomTransformer;

    @Captor
    ArgumentCaptor<Path> pomPath;

    @Captor
    ArgumentCaptor<AddDependencyMojo> params;

    @Test
    void shouldAddDependencyProperly() throws MojoExecutionException, MojoFailureException {

        //Given
        AddDependencyMojo mojo = new AddDependencyMojo();
        mojo.groupId = "groupId";
        mojo.artifactId = "artifactId";
        mojo.pomTransformer = pomTransformer;

        //When
        mojo.execute();

        //Then
        verify(pomTransformer, atLeastOnce()).accept(pomPath.capture(), params.capture());
        assertThat(Path.of("pom.xml")).isEqualTo(pomPath.getValue());
        assertThat(mojo).isSameAs(params.getValue());

    }

}
