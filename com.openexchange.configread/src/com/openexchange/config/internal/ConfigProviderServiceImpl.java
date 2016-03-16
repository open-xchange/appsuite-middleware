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

package com.openexchange.config.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigProviderService;
import com.openexchange.config.cascade.ReinitializableConfigProviderService;
import com.openexchange.exception.OXException;

/**
 * {@link ConfigProviderServiceImpl} - The implementation of ConfigProviderService for the scope <code>"server"</code>.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ConfigProviderServiceImpl implements ReinitializableConfigProviderService {

    private static final String META = "meta";

    private static final String SETTINGS = "settings";

    private static final String PREFRENCE_PATH = "preferencePath";

    private static final String VALUE = "value";

    private static final String PROTECTED = "protected";

    private static final String TRUE = "true";

    // -------------------------------------------------------------------------------------------------------------------

    private final ConfigurationService configService;
    private final ConcurrentMap<String, ServerProperty> properties = new ConcurrentHashMap<String, ServerProperty>();

    /**
     * Initializes a new {@link ConfigProviderServiceImpl}.
     *
     * @param configService The associated configuration service
     * @throws OXException If initialization fails
     */
    public ConfigProviderServiceImpl(final ConfigurationService configService) throws OXException {
        super();
        this.configService = configService;
        init();
    }

    @Override
    public ServerProperty get(final String property, final int contextId, final int userId) throws OXException {
        if (null == property) {
            return null;
        }
        final ServerProperty basicProperty = properties.get(property);
        if (basicProperty != null) {
            return basicProperty;
        }
        final ServerProperty retval = new ServerProperty();
        final String value = configService.getProperty(property);
        retval.setDefined(value != null);
        retval.set(value);

        final ServerProperty alreadyDefined = properties.putIfAbsent(property, retval);
        if(alreadyDefined != null) {
            return alreadyDefined;
        }
        return retval;
    }

    @Override
    public String getScope() {
    	return "server";
    }

    @Override
    public Collection<String> getAllPropertyNames(final int contextId, final int userId) throws OXException {
        final Iterator<String> propertyNames = configService.propertyNames();
        final Set<String> retval = new HashSet<String>();
        while (propertyNames.hasNext()) {
            retval.add(propertyNames.next());
        }
        retval.addAll(properties.keySet());
        return retval;
    }

    private void init() throws OXException {
        initSettings(configService);
        initStructuredObjects(configService);
        initMetadata(configService);
    }

    private void initSettings(final ConfigurationService config) throws OXException {
        Properties propertiesInFolder = config.getPropertiesInFolder(SETTINGS);
        for (Object propKey : propertiesInFolder.keySet()) {
            String propName = propKey.toString();
            ServerProperty serverProperty = get(propName, -1, -1);
            serverProperty.set(PREFRENCE_PATH, propName);
            if (serverProperty.get(PROTECTED) == null) {
                serverProperty.set(PROTECTED, TRUE);
            }

        }

    }

    private void initStructuredObjects(final ConfigurationService config) throws OXException {
        Map<String, Object> yamlInFolder = config.getYamlInFolder(SETTINGS);
        for (Object yamlContent : yamlInFolder.values()) {
            if (yamlContent instanceof Map) {
                Map<String, Object> entries = (Map<String, Object>) yamlContent;
                for (Map.Entry<String, Object> entry : entries.entrySet()) {
                    String namespace = entry.getKey();
                    Object subkeys = entry.getValue();
                    recursivelyInitStructuredObjects(namespace, subkeys);
                }
            }
        }
    }

    private void recursivelyInitStructuredObjects(String namespace, Object subkeys) throws OXException {
        if (subkeys instanceof Map) {
            Map<String, Object> entries = (Map<String, Object>) subkeys;
            for (Map.Entry<String, Object> entry : entries.entrySet()) {
                recursivelyInitStructuredObjects(namespace + "/" + entry.getKey(), entry.getValue());
            }
        } else {
            // We found a leaf
            final ServerProperty serverProperty = get(namespace, -1, -1);
            serverProperty.set(PREFRENCE_PATH, namespace);
            serverProperty.set(subkeys.toString());
            serverProperty.setDefined(true);
            if (serverProperty.get(PROTECTED) == null) {
                serverProperty.set(PROTECTED, TRUE);
            }
        }
    }

    private void initMetadata(final ConfigurationService config) throws OXException {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ConfigProviderService.class);
        final Map<String, Object> yamlInFolder = config.getYamlInFolder(META);
        for (final Object o : yamlInFolder.values()) {
            if (false == checkMap(o, logger)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> metadataDef = (Map<String, Object>) o;
            for (final Map.Entry<String, Object> entry : metadataDef.entrySet()) {
                final String propertyName = entry.getKey();
                final Object value2 = entry.getValue();
                if (false == checkMap(value2, logger)) {
                    continue;
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> metadata = (Map<String, Object>) value2;
                final ServerProperty basicProperty = get(propertyName, -1, -1);
                for (final Map.Entry<String, Object> metadataProp : metadata.entrySet()) {
                    if (metadataProp.getValue() != null) {
                        basicProperty.set(metadataProp.getKey(), metadataProp.getValue().toString());
                    }
                }

                String value = basicProperty.get(VALUE);
                if (value == null) {
                    value = config.getProperty(propertyName);
                }
                basicProperty.set(value);
                basicProperty.setDefined(value != null);
            }
        }
    }

    private boolean checkMap(Object o, org.slf4j.Logger logger) {
        if (! Map.class.isInstance(o)) {
            final StringBuilder b = new StringBuilder("One of the .yml files in the meta configuration directory is improperly formatted\n");
            b.append("Please make sure they are formatted in this fashion:\n");
            b.append("ui/somepath:\n");
            b.append("\tprotected: false\n\n");
            b.append("ui/someOtherpath:\n");
            b.append("\tprotected: false\n\n");
            logger.error(b.toString(), new IllegalArgumentException("Invalid .yml file"));
            return false;
        }
        return true;
    }

    /**
     * Re-initializes this configuration provider.
     *
     * @throws OXException If operation fails
     */
    @Override
    public void reinit() throws OXException {
        properties.clear();
        init();
    }

}
