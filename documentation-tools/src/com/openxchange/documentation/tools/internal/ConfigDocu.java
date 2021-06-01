/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openxchange.documentation.tools.internal;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * {@link ConfigDocu}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class ConfigDocu {

    private List<YamlFile> data;

    /**
     * Initializes a new {@link ConfigDocu}.
     *
     * @param yamlFolder the folder containing the YAML files
     * @throws FileNotFoundException
     */
    public ConfigDocu(File yamlFolder) throws FileNotFoundException {
        Constructor constructor = new Constructor(YamlFile.class);
        TypeDescription propertyDescription = new TypeDescription(YamlFile.class);
        propertyDescription.addPropertyParameters("data", Property.class);
        constructor.addTypeDescription(propertyDescription);
        Yaml yaml = new Yaml(constructor);
        data = new LinkedList<>();
        FileFilter filter = file -> !file.isDirectory() && file.getName().endsWith(".yml") && !file.getName().equals("template.yml");
        File[] files = yamlFolder.listFiles(filter);
        Arrays.sort(files);
        for (File file : files) {
            YamlFile tmpData = (YamlFile) yaml.load(new FileInputStream(file));
            data.add(tmpData);
        }
    }

    /**
     * Returns all the properties
     *
     * @return A list with all properties
     */
    public List<Property> getProperties() {
        return data.stream().map(YamlFile::getProperties).flatMap(List::stream).collect(Collectors.toList());
    }

    /**
     * Returns all properties with the given tag
     * 
     * @param tag The tag
     * @return A list of properties
     */
    public List<Property> getProperties(String tag) {
        return data.stream().map(YamlFile::getProperties).flatMap(List::stream).filter(prop -> prop.getTags().contains(tag)).collect(Collectors.toList());
    }

    /**
     * Returns a list of available tags
     *
     * @return the list of tags
     */
    public Set<String> getTags() {
        return data.stream().map(YamlFile::getProperties).flatMap(List::stream).map(Property::getTags).flatMap(List::stream).collect(Collectors.toSet());
    }
}
