/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openxchange.documentation.tools.internal;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    private ArrayList<YamlFile> data;

    /**
     * Initializes a new {@link ConfigDocu}.
     *
     * @throws FileNotFoundException
     */
    public ConfigDocu(File yamlFolder) throws FileNotFoundException {
        Constructor constructor = new Constructor(YamlFile.class);
        TypeDescription propertyDescription = new TypeDescription(YamlFile.class);
        propertyDescription.addPropertyParameters("data", Property.class);
        constructor.addTypeDescription(propertyDescription);
        Yaml yaml = new Yaml(constructor);
        data = new ArrayList<>();
        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File file) {
                return !file.isDirectory() && file.getName().endsWith(".yml") && !file.getName().equals("template.yml");
            }
        };
        File[] files = yamlFolder.listFiles(filter);
        Arrays.sort(files);
        for (File file : files) {
            YamlFile tmpData = (YamlFile) yaml.load(new FileInputStream(file));
            data.add(tmpData);
        }
    }

    public List<Property> getProperties(){
        List<Property> result = new ArrayList<>(data.size() * 5);
        for(YamlFile file: data) {
            result.addAll(file.getProperties());
        }
        return result;
    }

    /**
     * Returns all properties with the given tag
     * @param tag The tag
     * @return A list of properties
     */
    public List<Property> getProperties(String tag){
        List<Property> result = new ArrayList<>(20);
        for(YamlFile file: data) {
            for(Property prop: file.getProperties()) {
                if(prop.getTags().contains(tag)) {
                    result.add(prop);
                }
            }
        }
        return result;
    }

    /**
     * Returns a list of available tags
     *
     * @return the list of tags
     */
    public Set<String> getTags() {
        HashSet<String> result = new HashSet<>();
        for(YamlFile file: data) {
            for(Property prop: file.getProperties()) {
                result.addAll(prop.getTags());
            }
        }
        return result;
    }


}
