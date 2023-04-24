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

import org.l2x6.pom.tuner.PomTransformer;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class UpdateOrAddProfile implements ChangePom {

    public final String id;
    public final Mode mode;
    public final UpdateOrAddDependencyManagement dependencyManagement;
    public final Set<UpdateOrAddDependency> dependencies;

    private UpdateOrAddProfile(Mode mode,
                               String id,
                               UpdateOrAddDependencyManagement dependencyManagement,
                               Set<UpdateOrAddDependency> dependencies) {
        this.mode = Optional.ofNullable(mode).orElse(Mode.UPDATE);
        this.id = id;
        this.dependencyManagement = dependencyManagement;
        this.dependencies = dependencies;
    }

    public static UpdateOrAddProfile withId(String id) {
        return new UpdateOrAddProfile(null, id, null, Collections.emptySet());
    }

    public UpdateOrAddProfile withMode(Mode mode) {
        return new UpdateOrAddProfile(mode, this.id, this.dependencyManagement, this.dependencies);
    }

    public UpdateOrAddProfile updateOrAdd(UpdateOrAddDependency... dependencies) {
        return new UpdateOrAddProfile(
                this.mode,
                this.id,
                this.dependencyManagement,
                Arrays.stream(dependencies)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    public UpdateOrAddProfile updateOrAdd(UpdateOrAddDependencyManagement dependencyManagement) {
        return new UpdateOrAddProfile(
                this.mode,
                this.id,
                dependencyManagement,
                Collections.emptySet());
    }

    @Override
    public void execute(Pom pom) {

        // TODO test is missing

        new PomTransformer(
                pom.pomFile,
                StandardCharsets.UTF_8,
                PomTransformer.SimpleElementWhitespace.AUTODETECT_PREFER_SPACE)
                .transform(addOrUpdateProfileDirectToPom());


        Optional.ofNullable(this.dependencyManagement)
                .ifPresent(dependencyManagement -> dependencyManagement.execute(pom, this));
        dependencies.forEach(updateOrAddDependency -> updateOrAddDependency.execute(pom, this));
    }

    private PomTransformer.Transformation addOrUpdateProfileDirectToPom() {
        return (document, context) -> {
            if (context.getProfile(this.id).isEmpty() && this.mode.shouldAdd()) {
                context.getOrAddProfile(this.id);
            }
        };
    }

}
