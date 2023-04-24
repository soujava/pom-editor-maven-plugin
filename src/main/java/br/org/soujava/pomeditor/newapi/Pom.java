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

package br.org.soujava.pomeditor.newapi;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class Pom {

    public static Pom setup(PomChange.PomChangeBuilder builder) {
        return new Pom(null, builder);
    }

    public static Pom from(Path pomFile) {
        return new Pom(pomFile, null);
    }

    final Path pomFile;
    private final PomChange.PomChangeBuilder pomChangeBuilder;

    private Pom(Path pomFile, PomChange.PomChangeBuilder pomChangeBuilder) {
        this.pomFile = pomFile;
        this.pomChangeBuilder = Optional.ofNullable(pomChangeBuilder).orElseGet(PomChange::builder);
    }

    public Pom withPom(Path pomFile) {
        return new Pom(pomFile, this.pomChangeBuilder);
    }

    public void updateOrAdd(ChangePom... changes) throws Throwable {
        this.pomChangeBuilder
                .withPom(this.pomFile)
                .build()
                .execute(() -> {
                    Arrays.stream(changes)
                            .filter(Objects::nonNull)
                            .forEach(change -> change.execute(this));
                });
    }
}