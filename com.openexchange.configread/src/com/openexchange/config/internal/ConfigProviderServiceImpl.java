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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigProviderService;
import com.openexchange.exception.OXException;

/**
 * {@link ConfigProviderServiceImpl}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ConfigProviderServiceImpl implements ConfigProviderService {

    private static final String META = "meta";

    private static final String SETTINGS = "settings";

    private static final String PREFRENCE_PATH = "preferencePath";

    private static final String VALUE = "value";

    private static final String PROTECTED = "protected";

    private static final String TRUE = "true";

    private ConfigurationService configService;

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ConfigProviderService.class));

    private final ConcurrentMap<String, ServerProperty> properties = new ConcurrentHashMap<String, ServerProperty>();

    public ConfigProviderServiceImpl(final ConfigurationService configService) throws OXException {
        setConfigService(configService);
    }

    @Override
    public ServerProperty get(final String property, final int context, final int user) throws OXException {
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
    public Collection<String> getAllPropertyNames(final int context, final int user) throws OXException {
        final Iterator<String> propertyNames = configService.propertyNames();
        final Set<String> retval = new HashSet<String>();
        while (propertyNames.hasNext()) {
            retval.add(propertyNames.next());
        }
        retval.addAll(properties.keySet());
        return retval;
    }

    public void setConfigService(final ConfigurationService configService) throws OXException {
        this.configService = configService;
        initSettings(configService);
        initMetadata(configService);
    }

    private void initSettings(final ConfigurationService config) throws OXException {
        final Properties propertiesInFolder = config.getPropertiesInFolder(SETTINGS);
        for(final Object propName : propertiesInFolder.keySet()) {
            final ServerProperty serverProperty = get((String) propName, -1, -1);
            serverProperty.set(PREFRENCE_PATH, (String) propName);
            if(serverProperty.get(PROTECTED) == null) {
                serverProperty.set(PROTECTED, TRUE);
            }

        }

    }

    private void initMetadata(final ConfigurationService config) throws OXException {
        final Map<String, Object> yamlInFolder = config.getYamlInFolder(META);
        for(final Object o : yamlInFolder.values()) {
            if (! checkMap(o)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            final Map<String, Object> metadataDef = (Map<String, Object>) o;
            for(final Map.Entry<String, Object> entry : metadataDef.entrySet()) {
                final String propertyName = entry.getKey();
                final Object value2 = entry.getValue();
                if (! checkMap(value2)) {
                    continue;
                }
                @SuppressWarnings("unchecked")
                final Map<String, Object> metadata = (Map<String, Object>) value2;
                final ServerProperty basicProperty = get(propertyName, -1, -1);
                for(final Map.Entry<String, Object> metadataProp : metadata.entrySet()) {
                    if(metadataProp.getValue() != null) {
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

    private boolean checkMap(final Object o) {
        if (! Map.class.isInstance(o)) {
            final StringBuilder b = new StringBuilder("One of the .yml files in the meta configuration directory is improperly formatted\n");
            b.append("Please make sure they are formatted in this fashion:\n");
            b.append("ui/somepath:\n");
            b.append("\tprotected: false\n\n");
            b.append("ui/someOtherpath:\n");
            b.append("\tprotected: false\n\n");
            LOG.error(b.toString(), new IllegalArgumentException("Invalid .yml file"));
            return false;
        }
        return true;
    }

}
