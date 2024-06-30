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

import org.apache.maven.plugin.logging.Log;
import org.l2x6.pom.tuner.PomTransformer;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Command responsible for changing a property within a given pom
 */
public interface ChangeProperty {

    /**
     * Change a property based on the {@link Property} instance into the target POM xml
     *
     * @param pom        it's the target POM xml
     * @param property   it's a {@link Property} instance
     */
    static void execute(Log log, Path pom, Property property) {
        new PomTransformer(
                pom,
                StandardCharsets.UTF_8,
                PomTransformer.SimpleElementWhitespace.AUTODETECT_PREFER_SPACE)
                .transform(changePropertyIfNeeded(log, property));
    }

    private static PomTransformer.Transformation changePropertyIfNeeded(Log log, Property propertyToChange) {
        return (document, context) -> {

            String name = propertyToChange.getName();
            String value = propertyToChange.getValue();
            boolean removing = value == null;

            log.debug("name: "+name);
            log.debug("value: "+value);
            log.debug("removing: "+removing);

            // the container API is a bit weird, we can't set a text value on a node, only on a sub-node.

            Optional<PomTransformer.ContainerElement> propsWrap = context.getContainerElement("project", "properties");
            PomTransformer.ContainerElement props;
            if (propsWrap.isEmpty()) {
                log.debug("properties node not found");
                if (removing) {
                    log.debug("nothing to do, exiting");
                    return;
                }
                props = context.getOrAddContainerElement("properties");
                log.debug("added properties:"+props);
            } else {
                props = propsWrap.get();
                log.debug("existing properties:"+props);
            }

            Optional<PomTransformer.ContainerElement> propWrap = props.getChildContainerElement(name);
            if (propWrap.isEmpty()) {
                log.debug("No property "+name+" found");
                if (removing) {
                    return;
                }
            }

            if (removing) {
                PomTransformer.ContainerElement prop = propWrap.get();
                log.debug("Removing prop "+prop);
                prop.remove(false, false);
                return;
            }

            log.debug("Adding to properties");
            props.addOrSetChildTextElement(name, value);

        };
    }

}
