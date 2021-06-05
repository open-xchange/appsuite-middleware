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

package com.openexchange.config.lean.internal;

import static com.openexchange.java.Autoboxing.I;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.PropertyFilter;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.config.lean.PropertyValueParser;
import com.openexchange.config.lean.internal.parser.BooleanPropertyValueParser;
import com.openexchange.config.lean.internal.parser.FloatPropertyValueParser;
import com.openexchange.config.lean.internal.parser.IntegerPropertyValueParser;
import com.openexchange.config.lean.internal.parser.LongPropertyValueParser;
import com.openexchange.config.lean.internal.parser.StringPropertyValueParser;
import com.openexchange.exception.OXException;

/**
 * {@link LeanConfigurationServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class LeanConfigurationServiceImpl implements LeanConfigurationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeanConfigurationServiceImpl.class);

    private final ConfigurationService configService;
    private final ConfigViewFactory viewFactory;
    private final Map<Class<?>, PropertyValueParser<?>> valueParsers;

    /**
     * Initializes a new {@link LeanConfigurationServiceImpl}.
     */
    public LeanConfigurationServiceImpl(ConfigurationService configService, ConfigViewFactory viewFactory) {
        super();
        this.configService = configService;
        this.viewFactory = viewFactory;

        // Load parsers
        ImmutableMap.Builder<Class<?>, PropertyValueParser<?>> vps = ImmutableMap.builder();
        vps.put(Integer.class, new IntegerPropertyValueParser());
        vps.put(Long.class, new LongPropertyValueParser());
        vps.put(Float.class, new FloatPropertyValueParser());
        vps.put(Boolean.class, new BooleanPropertyValueParser());
        vps.put(String.class, new StringPropertyValueParser());
        valueParsers = vps.build();
    }

    @Override
    public String getProperty(Property property) {
        return getProperty(property, String.class, Collections.emptyMap());
    }

    @Override
    public int getIntProperty(Property property) {
        return getProperty(property, Integer.class, Collections.emptyMap()).intValue();
    }

    @Override
    public boolean getBooleanProperty(Property property) {
        return getProperty(property, Boolean.class, Collections.emptyMap()).booleanValue();
    }

    @Override
    public float getFloatProperty(Property property) {
        return getProperty(property, Float.class, Collections.emptyMap()).floatValue();
    }

    @Override
    public long getLongProperty(Property property) {
        return getProperty(property, Long.class, Collections.emptyMap()).longValue();
    }

    @Override
    public String getProperty(int userId, int contextId, Property property) {
        return getProperty(property, userId, contextId, String.class, Collections.emptyMap());
    }

    @Override
    public int getIntProperty(int userId, int contextId, Property property) {
        return getProperty(property, userId, contextId, Integer.class, Collections.emptyMap()).intValue();
    }

    @Override
    public boolean getBooleanProperty(int userId, int contextId, Property property) {
        return getProperty(property, userId, contextId, Boolean.class, Collections.emptyMap()).booleanValue();
    }

    @Override
    public float getFloatProperty(int userId, int contextId, Property property) {
        return getProperty(property, userId, contextId, Float.class, Collections.emptyMap()).floatValue();
    }

    @Override
    public long getLongProperty(int userId, int contextId, Property property) {
        return getProperty(property, userId, contextId, Long.class, Collections.emptyMap()).longValue();
    }

    @Override
    public String getProperty(Property property, Map<String, String> optionals) {
        return getProperty(property, String.class, optionals);
    }

    @Override
    public int getIntProperty(Property property, Map<String, String> optionals) {
        return getProperty(property, Integer.class, optionals).intValue();
    }

    @Override
    public boolean getBooleanProperty(Property property, Map<String, String> optionals) {
        return getProperty(property, Boolean.class, optionals).booleanValue();
    }

    @Override
    public float getFloatProperty(Property property, Map<String, String> optionals) {
        return getProperty(property, Float.class, optionals).floatValue();
    }

    @Override
    public long getLongProperty(Property property, Map<String, String> optionals) {
        return getProperty(property, Long.class, optionals).longValue();
    }

    @Override
    public String getProperty(int userId, int contextId, Property property, Map<String, String> optionals) {
        return getProperty(property, userId, contextId, String.class, optionals);
    }

    @Override
    public int getIntProperty(int userId, int contextId, Property property, Map<String, String> optionals) {
        return getProperty(property, userId, contextId, Integer.class, optionals).intValue();
    }

    @Override
    public boolean getBooleanProperty(int userId, int contextId, Property property, Map<String, String> optionals) {
        return getProperty(property, userId, contextId, Boolean.class, optionals).booleanValue();
    }

    @Override
    public float getFloatProperty(int userId, int contextId, Property property, Map<String, String> optionals) {
        return getProperty(property, userId, contextId, Float.class, optionals).floatValue();
    }

    @Override
    public long getLongProperty(int userId, int contextId, Property property, Map<String, String> optionals) {
        return getProperty(property, userId, contextId, Long.class, optionals).longValue();
    }

    @Override
    public String getProperty(int userId, int contextId, Property property, List<String> scopes, Map<String, String> optionals) {
        return getProperty(property, userId, contextId, scopes, String.class, optionals);
    }

    @Override
    public int getIntProperty(int userId, int contextId, Property property, List<String> scopes, Map<String, String> optionals) {
        return getProperty(property, userId, contextId, scopes, Integer.class, optionals).intValue();
    }

    @Override
    public boolean getBooleanProperty(int userId, int contextId, Property property, List<String> scopes, Map<String, String> optionals) {
        return getProperty(property, userId, contextId, scopes, Boolean.class, optionals).booleanValue();
    }

    @Override
    public float getFloatProperty(int userId, int contextId, Property property, List<String> scopes, Map<String, String> optionals) {
        return getProperty(property, userId, contextId, scopes, Float.class, optionals).floatValue();
    }

    @Override
    public long getLongProperty(int userId, int contextId, Property property, List<String> scopes, Map<String, String> optionals) {
        return getProperty(property, userId, contextId, scopes, Long.class, optionals).longValue();
    }

    @Override
    public Map<String, String> getProperties(PropertyFilter propertyFilter) {
        try {
            ConfigurationService configService = this.configService;
            return configService.getProperties(propertyFilter);
        } catch (OXException e) {
            LOGGER.error("", e);
            return ImmutableMap.of();
        }
    }

    ////////////////////////////////////////HELPERS ///////////////////////////////////////

    /**
     * Get the value T of specified property and coerce it to the specified type T
     *
     * @param property The {@link Property}
     * @param coerceTo The type T to coerce the value of the property
     * @return The value T of the property from the {@link ConfigurationService} or the default value
     * @throws IllegalArgumentException If given coercion type is not applicable
     */
    @SuppressWarnings("unchecked")
    private <T> T getProperty(Property property, Class<T> coerceTo, Map<String, String> optionals) {
        boolean tryToRecover = true;
        String value = null;
        try {
            ConfigurationService configService = this.configService;
            value = configService.getProperty(property.getFQPropertyName(optionals));
            if (value == null) {
                T defaultValue = property.getDefaultValue(coerceTo);
                LOGGER.debug("The value of property '{}' was 'null'. Returning default value '{}' instead.", property.getFQPropertyName(optionals), defaultValue);
                return defaultValue;
            }

            PropertyValueParser<?> propertyValueParser = valueParsers.get(coerceTo);
            if (propertyValueParser == null) {
                T defaultValue = property.getDefaultValue(coerceTo);
                LOGGER.debug("Cannot find an appropriate value parser for '{}'. Returning default value '{}' instead.", coerceTo, defaultValue);
                return defaultValue;
            }

            try {
                return (T) propertyValueParser.parse(value);
            } catch (ClassCastException x) {
                if (String.class.equals(coerceTo)) {
                    return (T) value;
                }
                // Otherwise impossible to recover from wrong coercion type
                tryToRecover = false;
                throw new IllegalArgumentException("The value '" + value + "' of property '" + property.getFQPropertyName(optionals) + "' cannot be converted to the specified type '" + coerceTo.getCanonicalName() + "'", x);
            }
        } catch (Exception e) {
            if (!tryToRecover) {
                throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
            }
            T defaultValue = property.getDefaultValue(coerceTo);
            LOGGER.warn("The value '{}' of property '{}' cannot be cast as '{}'. Returning default value '{}' instead.", value, property.getFQPropertyName(optionals), coerceTo.getSimpleName(), defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * Get the value T of specified property for the specified user in the specified context and coerce it to the specified type T
     *
     * @param property The {@link Property}
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param coerceTo The type T to coerce the value of the property
     * @return The value T of the property from the config cascade or the default value
     */
    private <T> T getProperty(Property property, int userId, int contextId, Class<T> coerceTo, Map<String, String> optionals) {
        try {
            ConfigViewFactory viewFactory = this.viewFactory;
            ConfigView view = viewFactory.getView(userId, contextId);

            ComposedConfigProperty<T> p = view.property(property.getFQPropertyName(optionals), coerceTo);
            return p.isDefined() ? p.get() : property.getDefaultValue(coerceTo);
        } catch (Exception e) {
            T defaultValue = property.getDefaultValue(coerceTo);
            LOGGER.error("Error getting '{}' property for user '{}' in context '{}'. Returning the default value of '{}'", property.getFQPropertyName(), I(userId), I(contextId), defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * Get the value T of specified property for the specified user in the specified context and coerce it to the specified type T
     *
     * @param property The {@link Property}
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param scopes The list of scopes that should be considered,
     *            with first element being the most specific and last element being the least specific scope
     * @param coerceTo The type T to coerce the value of the property
     * @return The value T of the property from the config cascade or the default value
     */
    private <T> T getProperty(Property property, int userId, int contextId, List<String> scopes, Class<T> coerceTo, Map<String, String> optionals) {
        try {
            ConfigViewFactory viewFactory = this.viewFactory;
            ConfigView view = viewFactory.getView(userId, contextId);

            for (String scope : scopes) {
                ConfigProperty<T> p = view.property(scope, property.getFQPropertyName(optionals), coerceTo);
                if (false == p.isDefined()) {
                    continue;
                }
                return p.get();
            }
            return property.getDefaultValue(coerceTo);
        } catch (Exception e) {
            T defaultValue = property.getDefaultValue(coerceTo);
            LOGGER.error("Error getting '{}' property for user '{}' in context '{}'  for scope(s) '{}'. Returning the default value of '{}'", property.getFQPropertyName(), I(userId), I(contextId), scopes, defaultValue, e);
            return defaultValue;
        }
    }
}
