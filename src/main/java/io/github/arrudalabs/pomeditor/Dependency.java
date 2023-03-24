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

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a dependency to be added
 */
public final class Dependency {

    /**
     * @return a {@link DependencyBuilder}
     */
    public static DependencyBuilder builder() {
        return new DependencyBuilder();
    }

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String type;
    private final String classifier;
    private final String scope;

    Dependency(String groupId, String artifactId, String version, String type, String classifier, String scope) {
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

    public static DependencyBuilder ofGav(String gav) {
        DependencyBuilder builder = new DependencyBuilder();
        var gavValues = Arrays.stream(gav.split(":"))
                .filter(Objects::nonNull)
                .filter(item -> !item.isBlank())
                .map(String::trim)
                .collect(Collectors.toList());
        if (gavValues.size() >= 1)
            builder.setGroupId(gavValues.get(0));
        if (gavValues.size() >= 2)
            builder.setArtifactId(gavValues.get(1));
        if (gavValues.size() >= 3)
            builder.setVersion(gavValues.get(2));
        return builder;
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
