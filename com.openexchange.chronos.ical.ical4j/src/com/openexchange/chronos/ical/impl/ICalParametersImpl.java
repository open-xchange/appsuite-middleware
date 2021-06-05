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

package com.openexchange.chronos.ical.impl;

import static com.openexchange.java.Autoboxing.I;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.osgi.Services;
import com.openexchange.config.ConfigurationService;
import net.fortuna.ical4j.extensions.caldav.parameter.CalendarServerDtStamp;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryImpl;

/**
 * {@link ICalParametersImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ICalParametersImpl implements ICalParameters {

    /**
     * {@link TimeZoneRegistry}
     * <p/>
     * Holds a reference to the underlying timezone registry.
     */
    public static final String TIMEZONE_REGISTRY = TimeZoneRegistry.class.getName();

    /** The default prefiy to use for when initializing the timezone registries */
    private static final String DEFAULT_TIMEZONE_RESOURCE_PREFIX = "zoneinfo-outlook/";

    private final Map<String, Object> parameters;

    /**
     * Initializes a new, empty {@link ICalParametersImpl}.
     */
    public ICalParametersImpl() {
        super();
        this.parameters = new HashMap<String, Object>();
        applyDefaults();
    }

    private void applyDefaults() {
        set(TIMEZONE_REGISTRY, new TimeZoneRegistryImpl(DEFAULT_TIMEZONE_RESOURCE_PREFIX));
        ConfigurationService configService = Services.optService(ConfigurationService.class);
        if (null != configService) {
            set(IMPORT_LIMIT, I(configService.getIntProperty("com.openexchange.import.ical.limit", -1)));
        }
        set(IGNORED_PROPERTY_PARAMETERS, new String[] { ICalUtils.preparePrameterToRemove(Property.ATTENDEE, CalendarServerDtStamp.PARAMETER_NAME) });
    }

    @Override
    public <T> T get(String parameter, Class<T> clazz) {
        return get(parameter, clazz, null);
    }

    @Override
    public <T> T get(String parameter, Class<T> clazz, T defaultValue) {
        Object value = parameters.get(parameter);
        return null == value ? defaultValue : clazz.cast(value);
    }

    @Override
    public <T> ICalParameters set(String name, T value) {
        if (null != value) {
            parameters.put(name, value);
        } else {
            parameters.remove(name);
        }
        return this;
    }

}
