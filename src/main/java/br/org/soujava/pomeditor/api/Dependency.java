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

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a dependency to be added
 */
public final class Dependency {

    /**
     * Returns a {@link DependencyBuilder} instance
     * @return a {@link DependencyBuilder}
     */
    public static DependencyBuilder builder() {
        return new DependencyBuilder();
    }

    /**
     * Returns a {@link DependencyBuilder} based on a GAV - groupId:artifactId:version - parameter
     *
     * @param gav a GAV - groupId:artifactId:version - parameter
     * @return a {@link DependencyBuilder}
     */
    public static DependencyBuilder ofGav(String gav) {
        DependencyBuilder builder = builder();
        var gavValues = Arrays.stream(gav.split(":"))
                .filter(Objects::nonNull)
                .filter(item -> !item.isBlank())
                .map(String::trim)
                .collect(Collectors.toList());
        if (gavValues.size() >= 1)
            builder.withGroupId(gavValues.get(0));
        if (gavValues.size() >= 2)
            builder.withArtifactId(gavValues.get(1));
        if (gavValues.size() >= 3)
            builder.withVersion(gavValues.get(2));
        return builder;
    }

    /**
     * Dependency builder
     */
    public static class DependencyBuilder {

        private String groupId;
        private String artifactId;
        private String version;
        private String type;
        private String classifier;
        private String scope;

        /**
         * @param groupId groupId
         * @return the same {@link DependencyBuilder} instance
         */
        public DependencyBuilder withGroupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        /**
         * @param artifactId artifactId
         * @return the same {@link DependencyBuilder} instance
         */
        public DependencyBuilder withArtifactId(String artifactId) {
            this.artifactId = artifactId;
            return this;
        }

        /**
         * @param version version
         * @return the same {@link DependencyBuilder} instance
         */
        public DependencyBuilder withVersion(String version) {
            this.version = version;
            return this;
        }

        /**
         * @param type type
         * @return the same {@link DependencyBuilder} instance
         */
        public DependencyBuilder withType(String type) {
            this.type = type;
            return this;
        }

        /**
         * @param classifier classifier
         * @return the same {@link DependencyBuilder} instance
         */
        public DependencyBuilder withClassifier(String classifier) {
            this.classifier = classifier;
            return this;
        }

        /**
         * @param scope scope
         * @return the same {@link DependencyBuilder} instance
         */
        public DependencyBuilder withScope(String scope) {
            this.scope = scope;
            return this;
        }

        /**
         * @return a {@link Dependency} instance
         */
        public Dependency build() {
            return new Dependency(groupId, artifactId, version, type, classifier, scope);
        }
    }

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String type;
    private final String classifier;
    private final String scope;

    private Dependency(String groupId, String artifactId, String version, String type, String classifier, String scope) {
        if (groupId == null || groupId.isBlank())
            throw new IllegalArgumentException("groupId must be provided");

        if (artifactId == null || artifactId.isBlank())
            throw new IllegalArgumentException("artifactId must be provided");

        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.type = type;
        this.classifier = classifier;
        this.scope = scope;
    }

    /**
     * @return the groupId
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @return artifactId
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * @return version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * @return classifier
     */
    public String getClassifier() {
        return classifier;
    }

    /**
     * @return scope
     */
    public String getScope() {
        return scope;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dependency that = (Dependency) o;
        return Objects.equals(groupId, that.groupId)
                && Objects.equals(artifactId, that.artifactId)
                && Objects.equals(version, that.version)
                && Objects.equals(type, that.type)
                && Objects.equals(classifier, that.classifier)
                && Objects.equals(scope, that.scope);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("groupId='" + groupId + '\'');
        sb.append(", artifactId='" + artifactId + '\'');
        if (version != null)
            sb.append(", version='" + version + '\'');
        if (type != null)
            sb.append(", type='" + type + '\'');
        if (classifier != null)
            sb.append(", classifier='" + classifier + '\'');
        if (scope != null)
            sb.append(", scope='" + scope + '\'');
        sb.append("}");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version, type, classifier, scope);
    }
}
