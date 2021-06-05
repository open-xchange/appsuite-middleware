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

package com.openexchange.hostname.ldap.configuration;

import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.ServiceRegistry;


/**
 * Class for property handling
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class LDAPHostnameProperties {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LDAPHostnameProperties.class);

    /**
     * Fetches the property (convenience method)
     *
     * @param <T>
     * @param configuration
     * @param prop
     * @return
     * @throws ConfigurationException
     */
    @SuppressWarnings("unchecked")
    public static <T extends Object> T getProperty(final ServiceRegistry registry, final PropertyInterface prop) throws ConfigurationException {
        final ConfigurationService configuration = registry.getService(ConfigurationService.class);
        if (null == configuration) {
            throw new ConfigurationException("No configuration service found");
        }
        // Copy because otherwise compiler complains... :-(
        final Class<? extends Object> clazz = prop.getClazz();
        final String completePropertyName = prop.getName();
        final String property = configuration.getProperty(completePropertyName);
        if (null != property && property.length() != 0) {
            if (String.class.equals(clazz)) {
                // no conversion done just output
                return (T) clazz.cast(property);
            } else if (Integer.class.equals(clazz)) {
                try {
                    return (T) clazz.cast(Integer.valueOf(property));
                } catch (NumberFormatException e) {
                    throw new ConfigurationException("The value given in the property " + completePropertyName + " is no integer value");
                }
            } else if (Boolean.class.equals(clazz)) {
                return (T) clazz.cast(Boolean.valueOf(property));
            } else if (Enum.class.equals(clazz.getSuperclass())) {
                try {
                    final Enum<?> valueOf = Enum.valueOf(clazz.asSubclass(Enum.class), property);
                    return (T) valueOf;
                } catch (IllegalArgumentException e) {
                    return null;
                }
            } else {
                return null;
            }
        }
        return null;

    }

    /**
     * Fetches the property
     *
     * @param <T>
     * @param configuration
     * @param prop
     * @return
     * @throws ConfigurationException
     */
    @SuppressWarnings("unchecked")
    public static <T extends Object> T getProperty(final ConfigurationService configuration, final PropertyInterface prop) throws OXException {
        final Class<? extends Object> clazz = prop.getClazz();
        final String completePropertyName = prop.getName();
        final String property = configuration.getProperty(completePropertyName);
        if (null == property || property.length() == 0) {
            return null;
        }
        if (String.class.equals(clazz)) {
            // no conversion done just output
            return (T) clazz.cast(property);
        } else if (Integer.class.equals(clazz)) {
            try {
                return (T) clazz.cast(Integer.valueOf(property));
            } catch (NumberFormatException e) {
                throw new OXException().setLogMessage("The value given in the property " + completePropertyName + " is no integer value");
            }
        } else if (Boolean.class.equals(clazz)) {
            return (T) clazz.cast(Boolean.valueOf(property));
        } else if (Enum.class.equals(clazz.getSuperclass())) {
            try {
                final Enum<?> valueOf = Enum.valueOf(clazz.asSubclass(Enum.class), property);
                return (T) valueOf;
            } catch (IllegalArgumentException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Checks if all required properties are set and throws an exception if not. Also prints out the settings values
     *
     * @param configService The {@link ConfigurationService} from which the properties are read
     * @param props An array of props which should be checked
     * @param bundlename The bundle name (needed for output of the properties)
     * @throws OXException
     */
    public static void check(ConfigurationService configService, PropertyInterface[] props, String bundlename) throws OXException {
        if (null == configService) {
            throw new OXException().setLogMessage("No configuration service found");
        }

        StringBuilder sb = new StringBuilder(256);
        sb.append("\nLoading global " + bundlename + " properties...\n");
        for (PropertyInterface prop : props) {
            Object property = getProperty(configService, prop);
            sb.append('\t');
            sb.append(prop.getName());
            sb.append(": ");
            sb.append(property);
            sb.append('\n');
            if (Required.Value.TRUE.equals(prop.getRequired().getValue()) && null == property) {
                throw new OXException().setLogMessage("Property " + prop.getName() + " not set but required.");
            }
            if (Required.Value.CONDITION.equals(prop.getRequired().getValue())) {
                final Condition[] condition = prop.getRequired().getCondition();
                if (null == condition || condition.length == 0) {
                    throw new OXException().setLogMessage("Property " + prop.getName() + " claims to have condition but condition not set.");
                }
                for (final Condition cond : condition) {
                    final PropertyInterface property3 = cond.getProperty();
                    final Object property2 = getProperty(configService, property3);
                    final Object value = cond.getValue();
                    if (value.equals(property2) && null == property) {
                        throw new OXException().setLogMessage("Property " + prop.getName() + " must be set if " + property3.getName() + " is set to " + value);
                    }
                }
            }
        }
        LOG.info(sb.toString());
    }

}
