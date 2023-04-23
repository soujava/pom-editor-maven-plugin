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

package br.org.soujava.pomeditor.api;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

class AddDependencyTest {


    @Test
    void test() {

        Path pom = Path.of("pom.xml");

        Pom.from(pom)
                .updateOrAdd(
                        DependencyManagement
                                .updateOrAdd(Dependency
                                        .updateOrAdd()
                                        .withGroupId("someGroupId")
                                        .withArtifactId("someArtifactId")
                                        .withVersion("1.0.0")
                                        .build()),
                        Dependency
                                .updateOrAdd()
                                .withGroupId("someGroupId")
                                .withArtifactId("someArtifactId")
                                .withVersion("1.0.0")
                                .build(),
                        Profile.updateOrAdd("1")
                                .withMode(Mode.UPDATE_OR_ADD)
                                .updateOrAdd(DependencyManagement
                                        .updateOrAdd(Dependency
                                                .updateOrAdd()
                                                .withGroupId("someGroupId")
                                                .withArtifactId("someArtifactId")
                                                .withVersion("1.0.0")
                                                .build()))
                                .updateOrAdd(Dependency
                                        .updateOrAdd()
                                        .withGroupId("someGroupId")
                                        .withArtifactId("someArtifactId")),
                        Profile.updateOrAdd("2")
                                .updateOrAdd(DependencyManagement
                                        .updateOrAdd(Dependency
                                                .updateOrAdd()
                                                .withGroupId("someGroupId")
                                                .withArtifactId("someArtifactId")
                                                .withVersion("1.0.0")
                                                .build()))
                                .updateOrAdd(Dependency
                                        .updateOrAdd()
                                        .withGroupId("someGroupId")
                                        .withArtifactId("someArtifactId"))
                );


    }


    static class Pom {

        static Pom from(Path pomFile) {
            return new Pom(pomFile);
        }

        public final Path pomFile;

        public Pom(Path pomFile) {
            this.pomFile = pomFile;
        }

        public void updateOrAdd(ChangePom... changes) {
            Arrays.stream(changes)
                    .filter(Objects::nonNull)
                    .forEach(change -> change.execute(this));

        }
    }

    static interface ChangePom {
        void execute(Pom target);
    }

    static class DependencyManagement {

        static UpdateOrAddDependencyManagement updateOrAdd(ChangeFromDependencyManagement... changes) {
            return UpdateOrAddDependencyManagement.updateOrAdd(Arrays.stream(changes).collect(Collectors.toCollection(LinkedHashSet::new)));
        }

    }

    static class Profile {

        static UpdateOrAddProfile updateOrAdd(String id) {
            return UpdateOrAddProfile.withId(id);
        }

    }

    static class Dependency {
        public static UpdateOrAddDependency updateOrAdd() {
            return UpdateOrAddDependency.build();
        }
    }

    static enum Mode {
        UPDATE,
        UPDATE_OR_ADD;

        static Mode of(Boolean forceAdd) {
            return forceAdd ? UPDATE_OR_ADD : UPDATE;
        }

        boolean shouldAdd() {
            return UPDATE_OR_ADD.equals(this);
        }
    }

    static interface ChangeFromProfile {
        void execute(UpdateOrAddProfile profile);
    }

    static class UpdateOrAddProfile implements ChangePom {

        public final Pom pom;
        public final String id;
        public final Mode mode;
        public final UpdateOrAddDependencyManagement dependencyManagement;
        public final Set<UpdateOrAddDependency> dependencies;

        public UpdateOrAddProfile(Pom pom,
                                  Mode mode,
                                  String id,
                                  UpdateOrAddDependencyManagement dependencyManagement,
                                  Set<UpdateOrAddDependency> dependencies) {
            this.pom = pom;
            this.mode = Optional.ofNullable(mode).orElse(Mode.UPDATE);
            this.id = id;
            this.dependencyManagement = dependencyManagement;
            this.dependencies = dependencies;
        }

        public static UpdateOrAddProfile withId(String id) {
            return new UpdateOrAddProfile(null, null, id, null, Collections.emptySet());
        }

        public UpdateOrAddProfile withMode(Mode mode) {
            return new UpdateOrAddProfile(this.pom, mode, this.id, this.dependencyManagement, this.dependencies);
        }

        public UpdateOrAddProfile updateOrAdd(UpdateOrAddDependency... dependencies) {
            return new UpdateOrAddProfile(
                    this.pom,
                    this.mode,
                    this.id,
                    this.dependencyManagement,
                    Arrays.stream(dependencies)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toCollection(LinkedHashSet::new)));
        }

        public UpdateOrAddProfile updateOrAdd(UpdateOrAddDependencyManagement dependencyManagement) {
            return new UpdateOrAddProfile(
                    this.pom,
                    this.mode,
                    this.id,
                    dependencyManagement,
                    Collections.emptySet());
        }

        public UpdateOrAddProfile executeOn(Pom pom) {
            return new UpdateOrAddProfile(pom,
                    this.mode,
                    this.id,
                    this.dependencyManagement,
                    this.dependencies);
        }


        @Override
        public void execute(Pom target) {
            // TODO do something direct to the target pom
            System.out.println(getClass().getSimpleName() + " -> doing something direct to the target pom");
            UpdateOrAddProfile updateOrAddProfile = this.executeOn(target);
            Optional.ofNullable(this.dependencyManagement).ifPresent(this::execute);
        }

        private void execute(UpdateOrAddDependencyManagement dependencyManagement) {
            dependencyManagement.execute(this);
        }
    }

    static interface ChangeFromDependencyManagement {
        void execute(UpdateOrAddDependencyManagement dependencyManagement);
    }

    static class UpdateOrAddDependencyManagement implements ChangePom, ChangeFromProfile {

        static UpdateOrAddDependencyManagement updateOrAdd(Set<ChangeFromDependencyManagement> changes) {
            return new UpdateOrAddDependencyManagement(null, changes);
        }

        private final UpdateOrAddProfile profile;
        private final Set<ChangeFromDependencyManagement> changes;

        private UpdateOrAddDependencyManagement(UpdateOrAddProfile profile, Set<ChangeFromDependencyManagement> changes) {
            this.profile = profile;
            this.changes = changes;
        }

        private UpdateOrAddDependencyManagement withProfile(UpdateOrAddProfile profile) {
            return new UpdateOrAddDependencyManagement(profile, this.changes);
        }

        @Override
        public void execute(Pom pom) {
            // TODO do something direct to the target pom
            System.out.println(getClass().getSimpleName() + " -> doing something direct to the target pom");
            changes.stream().filter(Objects::nonNull)
                    .forEach(change -> change.execute(this));
        }

        @Override
        public void execute(UpdateOrAddProfile updateOrAddProfile) {
            // TODO do something direct to the target profile
            var dependencyManagement = this.withProfile(updateOrAddProfile);
            System.out.println(getClass().getSimpleName() + " -> doing something direct from the profile " + updateOrAddProfile.id);
            changes.stream().filter(Objects::nonNull)
                    .forEach(change -> change.execute(dependencyManagement));
        }
    }

    static class UpdateOrAddDependency implements ChangePom, ChangeFromProfile, ChangeFromDependencyManagement {

        public static UpdateOrAddDependency build() {
            return new UpdateOrAddDependency(null, null, null);
        }

        final String groupId;
        final String artifactId;
        final String version;

        public UpdateOrAddDependency(String groupId, String artifactId, String version) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }

        public UpdateOrAddDependency withGroupId(String groupId) {
            return new UpdateOrAddDependency(groupId, this.artifactId, this.version);
        }

        public UpdateOrAddDependency withArtifactId(String artifactId) {
            return new UpdateOrAddDependency(this.groupId, artifactId, this.version);
        }

        public UpdateOrAddDependency withVersion(String version) {
            return new UpdateOrAddDependency(this.groupId, this.artifactId, version);
        }

        @Override
        public void execute(Pom target) {
            // TODO should update or add this dependency direct to the target pom
            System.out.println(getClass().getSimpleName() + " -> doing something direct to the target pom");
        }

        @Override
        public void execute(UpdateOrAddProfile profile) {
            // TODO should update or add this dependency to the target profile
            System.out.println(getClass().getSimpleName() + " -> doing something direct to target profile " + profile.id);
        }

        @Override
        public void execute(UpdateOrAddDependencyManagement dependencyManagement) {
            // TODO should update or add this dependency to the target dependencyManagement
            System.out.println(getClass().getSimpleName() + " -> doing something direct to target dependencyManagement in " + Optional.ofNullable(dependencyManagement.profile).map(p -> p.id).orElse("default") + " profile");
        }
    }

}
