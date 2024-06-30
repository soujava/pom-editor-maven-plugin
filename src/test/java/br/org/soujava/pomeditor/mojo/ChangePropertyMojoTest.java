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


import br.org.soujava.pomeditor.InvalidPropertyArgs;
import br.org.soujava.pomeditor.api.Property;
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
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChangePropertyMojoTest extends EditingMojoTest {

    @Mock
    BiConsumer<Path, Property> changePropertyCommand;

    @Captor
    ArgumentCaptor<Property> propertyToBeAdded;

    @DisplayName("should return error when")
    @ParameterizedTest(name = "property={0}, value={1}")
    @ArgumentsSource(InvalidPropertyArgs.class)
    void shouldReturnErrorsForInvalidRequiredParameters(final String property,
                                                        final String value) {
        Assertions.assertThrows(MojoExecutionException.class, () -> {

            ChangePropertyMojo mojo = newMojo();
            mojo.property = property;
            mojo.value = value;
            mojo.execute();
        });

        verify(backupFunction, never()).apply(any());
        verify(rollbackFunction, never()).accept(any());
    }

    @Test
    void shouldSetPropertyProperly() throws MojoExecutionException, MojoFailureException {

        //Given
        String expectedProp = "vanilla";
        String expectedVal = "sky";

        ChangePropertyMojo mojo = newMojo();
        mojo.property = expectedProp;
        mojo.value = expectedVal;

        //When
        mojo.execute();

        //Then
        verify(changePropertyCommand, atLeastOnce()).accept(targetPom.capture(), propertyToBeAdded.capture());
        verify(backupFunction, atLeastOnce()).apply(any(Path.class));
        verify(rollbackFunction, never()).accept(any());
        verify(log, atLeastOnce()).info(anyString());

        var addedProperty = this.propertyToBeAdded.getValue();
        assertThat(targetPom.getValue()).isEqualTo(Path.of(mojo.pom));
        assertThat(addedProperty.getName()).isEqualTo(expectedProp);
        assertThat(addedProperty.getValue()).isEqualTo(expectedVal);

    }

    private ChangePropertyMojo newMojo() {
        ChangePropertyMojo mojo = new ChangePropertyMojo();
        mojo.pom = this.pom.toString();
        mojo.setLog(log);
        mojo.backupFunction = backupFunction;
        mojo.rollbackFunction = rollbackFunction;
        mojo.changePropertyCommand =
                (path, dep) -> changePropertyCommand.accept(path, dep);
        return mojo;
    }

}
