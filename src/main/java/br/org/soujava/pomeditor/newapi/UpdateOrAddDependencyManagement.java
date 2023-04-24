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
import java.util.Objects;
import java.util.Set;

public class UpdateOrAddDependencyManagement implements ChangePom, ChangeFromProfile {

    public static UpdateOrAddDependencyManagement updateOrAdd(Set<ChangeFromDependencyManagement> changes) {
        return new UpdateOrAddDependencyManagement(null, changes);
    }

    final UpdateOrAddProfile profile;
    final Set<ChangeFromDependencyManagement> changes;

    private UpdateOrAddDependencyManagement(UpdateOrAddProfile profile, Set<ChangeFromDependencyManagement> changes) {
        this.profile = profile;
        this.changes = changes;
    }

    private UpdateOrAddDependencyManagement withProfile(UpdateOrAddProfile profile) {
        return new UpdateOrAddDependencyManagement(profile, this.changes);
    }

    @Override
    public void execute(Pom pom) {
        // TODO test is missing
        new PomTransformer(
                pom.pomFile,
                StandardCharsets.UTF_8,
                PomTransformer.SimpleElementWhitespace.AUTODETECT_PREFER_SPACE)
                .transform(addOrUpdateDependencyManagementIfNeededDirectToPom());

        changes.stream().filter(Objects::nonNull)
                .forEach(change -> change.execute(pom, null, this));
    }

    private PomTransformer.Transformation addOrUpdateDependencyManagementIfNeededDirectToPom() {
        return (document, context) -> {
            context.getOrAddContainerElements("dependencyManagement");
        };
    }

    @Override
    public void execute(Pom pom, UpdateOrAddProfile profile) {
        // TODO test is missing
        new PomTransformer(
                pom.pomFile,
                StandardCharsets.UTF_8,
                PomTransformer.SimpleElementWhitespace.AUTODETECT_PREFER_SPACE)
                .transform(addOrUpdateDependencyManagementIntoProfile(profile));

        changes.stream().filter(Objects::nonNull)
                .forEach(change -> change.execute(pom, profile, this));
    }

    private PomTransformer.Transformation addOrUpdateDependencyManagementIntoProfile(UpdateOrAddProfile profile) {
        return (document, context) -> {
            context.getProfile(profile.id).ifPresent(profileElement->{
                profileElement.getOrAddChildContainerElement("dependencyManagement");
            });
        };
    }
}
