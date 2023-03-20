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

import java.nio.file.Path;
import java.util.Objects;

/**
 * It's parameters for an add dependency request
 */
class AddDependency {

    final Path pom;
    final Dependency dependency;

    /**
     * Creates an AddDependencyRequest instance
     *
     * @param pom        path to the target POM. By default,: pom.xml
     * @param dependency a {@link Dependency} instance
     * @throws IllegalArgumentException when pom or dependency is null
     * @throws IllegalArgumentException when pom or dependency is null
     */
    AddDependency(Path pom, Dependency dependency) {
        Objects.requireNonNull(pom, "pom path is required");
        Objects.requireNonNull(dependency, "dependency is required");
        this.pom = pom;
        this.dependency = dependency;
    }

}
