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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.config.lean.internal;

import static com.openexchange.java.Autoboxing.I;
import java.util.Collections;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ComposedConfigProperty;
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

    ////////////////////////////////////////HELPERS ///////////////////////////////////////

    /**
     * Get the value T of specified property and coerce it to the specified type T
     *
     * @param property The {@link Property}
     * @param coerceTo The type T to coerce the value of the property
     * @return The value T of the property from the {@link ConfigurationService} or the default value
     */
    @SuppressWarnings("unchecked")
    private <T> T getProperty(Property property, Class<T> coerceTo, Map<String, String> optionals) {
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

            return (T) propertyValueParser.parse(value);
        } catch (Exception e) {
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


}
