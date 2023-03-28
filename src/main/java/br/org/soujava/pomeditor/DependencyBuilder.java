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

package br.org.soujava.pomeditor;

/**
 * Builder for Dependency
 */
public class DependencyBuilder {

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
    public DependencyBuilder setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    /**
     * @param artifactId artifactId
     * @return the same {@link DependencyBuilder} instance
     */
    public DependencyBuilder setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    /**
     * @param version version
     * @return the same {@link DependencyBuilder} instance
     */
    public DependencyBuilder setVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * @param type type
     * @return the same {@link DependencyBuilder} instance
     */
    public DependencyBuilder setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * @param classifier classifier
     * @return the same {@link DependencyBuilder} instance
     */
    public DependencyBuilder setClassifier(String classifier) {
        this.classifier = classifier;
        return this;
    }

    /**
     * @param scope scope
     * @return the same {@link DependencyBuilder} instance
     */
    public DependencyBuilder setScope(String scope) {
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