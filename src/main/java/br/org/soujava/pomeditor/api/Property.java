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

import java.util.Objects;

/**
 * Represents a property to be changed
 */
public final class Property {

    /**
     * Returns a {@link PropertyBuilder} instance
     * @return a {@link PropertyBuilder}
     */
    public static PropertyBuilder builder() {
        return new PropertyBuilder();
    }

    /**
     * Returns a {@link PropertyBuilder} based on the specified property name and value.
     * @param name property name
     * @param value property value, or {@code null} to request removal
     * @return a {@link PropertyBuilder}
     */
    public static PropertyBuilder of(String name, String value) {

        return builder().withName(name).withValue(value);

    }

    /**
     * Dependency builder
     */
    public static class PropertyBuilder {

        private String name;
        private String value;

        /**
         * @param name property name
         * @return {@code this} {@link PropertyBuilder} instance
         */
        public PropertyBuilder withName(String name) {
            this.name = name;
            return this;
        }

        /**
         * @param value property value
         * @return {@code this} {@link PropertyBuilder} instance
         */
        public PropertyBuilder withValue(String value) {
            this.value = value;
            return this;
        }

        /**
         * @return a new {@link Property} instance
         */
        public Property build() {
            return new Property(name, value);
        }
    }

    private final String name;
    private final String value;

    private Property(String name, String value) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must be provided");
        }

        this.name = name;
        this.value = value;

    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Property that = (Property) o;
        return Objects.equals(name, that.name)
                && Objects.equals(value, that.value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("name='").append(name).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append("}");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }
}
