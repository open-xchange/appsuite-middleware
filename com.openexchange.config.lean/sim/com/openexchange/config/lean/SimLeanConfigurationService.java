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

package com.openexchange.config.lean;

import static com.openexchange.java.Autoboxing.b;
import static com.openexchange.java.Autoboxing.i;
import java.util.List;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.config.PropertyFilter;
import com.openexchange.config.SimConfigurationService;
import com.openexchange.exception.OXException;

/**
 * {@link SimLeanConfigurationService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SimLeanConfigurationService implements LeanConfigurationService {

    public SimConfigurationService delegateConfigurationService;

    /**
     * Initialises a new {@link SimMailFilterConfigurationService}.
     */
    public SimLeanConfigurationService(SimConfigurationService simConfigurationService) {
        super();
        this.delegateConfigurationService = simConfigurationService;
    }

    @Override
    public String getProperty(Property property) {
        return delegateConfigurationService.getProperty(property.getFQPropertyName());
    }

    @Override
    public int getIntProperty(Property property) {
        return delegateConfigurationService.getIntProperty(property.getFQPropertyName(), i(property.getDefaultValue(Integer.class)));
    }

    @Override
    public boolean getBooleanProperty(Property property) {
        return delegateConfigurationService.getBoolProperty(property.getFQPropertyName(), b(property.getDefaultValue(Boolean.class)));
    }

    @Override
    public float getFloatProperty(Property property) {
        return Float.parseFloat(delegateConfigurationService.getProperty(property.getFQPropertyName(), property.getDefaultValue(String.class)));
    }

    @Override
    public long getLongProperty(Property property) {
        return Long.parseLong(delegateConfigurationService.getProperty(property.getFQPropertyName(), property.getDefaultValue(String.class)));
    }

    @Override
    public String getProperty(int userId, int contextId, Property property) {
        return delegateConfigurationService.getProperty(property.getFQPropertyName());
    }

    @Override
    public int getIntProperty(int userId, int contextId, Property property) {
        return delegateConfigurationService.getIntProperty(property.getFQPropertyName(), i(property.getDefaultValue(Integer.class)));
    }

    @Override
    public boolean getBooleanProperty(int userId, int contextId, Property property) {
        return delegateConfigurationService.getBoolProperty(property.getFQPropertyName(), b(property.getDefaultValue(Boolean.class)));
    }

    @Override
    public float getFloatProperty(int userId, int contextId, Property property) {
        return Float.parseFloat(delegateConfigurationService.getProperty(property.getFQPropertyName(), property.getDefaultValue(String.class)));
    }

    @Override
    public long getLongProperty(int userId, int contextId, Property property) {
        return Long.parseLong(delegateConfigurationService.getProperty(property.getFQPropertyName(), property.getDefaultValue(String.class)));
    }

    @Override
    public String getProperty(Property property, Map<String, String> optionals) {
        return delegateConfigurationService.getProperty(property.getFQPropertyName(), property.getDefaultValue(String.class));
    }

    @Override
    public int getIntProperty(Property property, Map<String, String> optionals) {
        return delegateConfigurationService.getIntProperty(property.getFQPropertyName(), property.getDefaultValue(Integer.class).intValue());
    }

    @Override
    public boolean getBooleanProperty(Property property, Map<String, String> optionals) {
        return delegateConfigurationService.getBoolProperty(property.getFQPropertyName(), property.getDefaultValue(Boolean.class).booleanValue());
    }

    @Override
    public float getFloatProperty(Property property, Map<String, String> optionals) {
        return Float.parseFloat(delegateConfigurationService.getProperty(property.getFQPropertyName(), property.getDefaultValue(String.class)));
    }

    @Override
    public long getLongProperty(Property property, Map<String, String> optionals) {
        return Long.parseLong(delegateConfigurationService.getProperty(property.getFQPropertyName(), property.getDefaultValue(String.class)));
    }

    @Override
    public String getProperty(int userId, int contextId, Property property, Map<String, String> optionals) {
        return delegateConfigurationService.getProperty(property.getFQPropertyName(), property.getDefaultValue(String.class));
    }

    @Override
    public int getIntProperty(int userId, int contextId, Property property, Map<String, String> optionals) {
        return delegateConfigurationService.getIntProperty(property.getFQPropertyName(), property.getDefaultValue(Integer.class).intValue());
    }

    @Override
    public boolean getBooleanProperty(int userId, int contextId, Property property, Map<String, String> optionals) {
        return delegateConfigurationService.getBoolProperty(property.getFQPropertyName(), property.getDefaultValue(Boolean.class).booleanValue());
    }

    @Override
    public float getFloatProperty(int userId, int contextId, Property property, Map<String, String> optionals) {
        return Float.parseFloat(delegateConfigurationService.getProperty(property.getFQPropertyName(), property.getDefaultValue(String.class)));
    }

    @Override
    public long getLongProperty(int userId, int contextId, Property property, Map<String, String> optionals) {
        return Long.parseLong(delegateConfigurationService.getProperty(property.getFQPropertyName(), property.getDefaultValue(String.class)));
    }

    @Override
    public String getProperty(int userId, int contextId, Property property, List<String> scopes, Map<String, String> optionals) {
        return delegateConfigurationService.getProperty(property.getFQPropertyName(), property.getDefaultValue(String.class));
    }

    @Override
    public int getIntProperty(int userId, int contextId, Property property, List<String> scopes, Map<String, String> optionals) {
        return delegateConfigurationService.getIntProperty(property.getFQPropertyName(), property.getDefaultValue(Integer.class).intValue());
    }

    @Override
    public boolean getBooleanProperty(int userId, int contextId, Property property, List<String> scopes, Map<String, String> optionals) {
        return delegateConfigurationService.getBoolProperty(property.getFQPropertyName(), property.getDefaultValue(Boolean.class).booleanValue());
    }

    @Override
    public float getFloatProperty(int userId, int contextId, Property property, List<String> scopes, Map<String, String> optionals) {
        return Float.parseFloat(delegateConfigurationService.getProperty(property.getFQPropertyName(), property.getDefaultValue(String.class)));
    }

    @Override
    public long getLongProperty(int userId, int contextId, Property property, List<String> scopes, Map<String, String> optionals) {
        return Long.parseLong(delegateConfigurationService.getProperty(property.getFQPropertyName(), property.getDefaultValue(String.class)));
    }

    @Override
    public Map<String, String> getProperties(PropertyFilter propertyFilter) {
        try {
            return delegateConfigurationService.getProperties(propertyFilter);
        } catch (OXException e) {
            return ImmutableMap.of();
        }
    }

}
