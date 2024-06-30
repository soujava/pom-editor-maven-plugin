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

package br.org.soujava.pomeditor.api;

import br.org.soujava.pomeditor.InvalidPropertyArgs;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

class PropertiesTest {

    @DisplayName("should return error when")
    @ParameterizedTest(name = "property={0}, value={1}")
    @ArgumentsSource(InvalidPropertyArgs.class)
    void shouldReturnErrors (final String property, final String value) {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                Property.of(property, value).build());

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                Property.builder()
                        .withName(property)
                        .withValue(value).build());
    }

}