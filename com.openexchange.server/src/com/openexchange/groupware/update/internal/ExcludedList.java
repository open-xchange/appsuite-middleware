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

package com.openexchange.groupware.update.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ConfigurationServices;
import com.openexchange.config.lean.DefaultProperty;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.java.Strings;

/**
 * This class contains the list of excluded update tasks. The configuration can be done by the configuration file
 * excludedupdatetasks.properties.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ExcludedList implements UpdateTaskList<String> {

    private static final Logger LOG = LoggerFactory.getLogger(ExcludedList.class);

    private static final String PROPERTY_DEFAULT = "";
    private static final Property PROPERTY = DefaultProperty.valueOf("com.openexchange.groupware.update.excludedUpdateTasks", PROPERTY_DEFAULT);
    private static final ExcludedList SINGLETON = new ExcludedList();

    private static final String CONFIG_FILE_NAME = "excludedupdatetasks.properties";

    private final List<String> taskList = new ArrayList<String>();
    private Set<String> excludedNamespaces = new HashSet<>();

    private ExcludedList() {
        super();
    }

    public static ExcludedList getInstance() {
        return SINGLETON;
    }

    public void configure(ConfigurationService configService) {
        taskList.clear();
        Properties props = loadProperties(configService);
        for (Entry<Object, Object> entry : props.entrySet()) {
            String className = entry.getKey().toString().trim();
            taskList.add(className);
        }
        UpdateTaskCollection.getInstance().dirtyVersion();
    }

    /**
     * Loads the properties
     * 
     * @param configService The {@link ConfigurationService}
     * @return The {@link Properties} found in {@value #CONFIG_FILE_NAME} or an empty {@link Properties} set
     */
    private Properties loadProperties(ConfigurationService configService) {
        try {
            return ConfigurationServices.loadPropertiesFrom(configService.getFileByName(CONFIG_FILE_NAME));
        } catch (IOException e) {
            LOG.warn("No '{}' file found in configuration folder with excluded update tasks.", CONFIG_FILE_NAME);
            return new Properties();
        }
    }

    /**
     * Loads the property <code>com.openexchange.groupware.update.excludedUpdateTasks</code>
     * 
     * @param leanConfig The {@link LeanConfigurationService} to load the property
     */
    public void loadExcludedNamespaces(LeanConfigurationService leanConfig) {
        String namespaces = leanConfig.getProperty(PROPERTY);
        String[] split = Strings.splitByComma(namespaces);
        if (split == null) {
            return;
        }
        Set<String> en = new HashSet<>();
        for (String s : split) {
            excludedNamespaces.add(s);
        }
        excludedNamespaces = Collections.unmodifiableSet(en);
    }

    /**
     * Returns an unmodifiable {@link Set} with all excluded update task namespaces
     * 
     * @return an unmodifiable {@link Set} with all excluded update task namespaces
     */
    public Set<String> getExcludedNamespaces() {
        return excludedNamespaces;
    }

    @Override
    public List<String> getTaskList() {
        return taskList;
    }
}
