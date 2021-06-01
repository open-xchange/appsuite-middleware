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

package com.openexchange.chronos.schedjoules.impl;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.util.concurrent.TimeUnit;
import com.openexchange.config.lean.Property;

/**
 * {@link SchedJoulesProperty}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum SchedJoulesProperty implements Property {
    /**
     * The API key that gives access to the complete API of SchedJoules.
     */
    apiKey,
    /**
     * The refresh interval of subscriptions in milliseconds.
     * Defaults to 86400000 (1 day)
     */
    refreshInterval(L(TimeUnit.DAYS.toMillis(1))),
    /**
     * The host to access schedjoules.
     * Defaults to 'api.schedjoules.com'<br>
     * <b>Note:</b> this property should explicitly not be published as the endpoint is fix
     */
    host("api.schedjoules.com"),
    /**
     * The scheme used to contact the SchedJoules API.
     * Defaults to 'https'<br>
     * <b>Note:</b> this property should explicitly not be published as the endpoint is fix
     */
    scheme("https"),
    /**
     * Defines the connection timeout (in msec) of a connection to the SchedJoules servers.
     * Defaults to 30000
     */
    connectionTimeout(I(30000)),
    /**
     * Defines a comma separated blacklist for itemIds of SchedJoules calendars and pages that should be hidden from
     * the end user.
     * Default: empty
     */
    itemBlacklist;

    private final String fqn;
    private final Object defaultValue;
    private static final String PREFIX = "com.openexchange.calendar.schedjoules.";

    /**
     * Initialises a new {@link UserFeedbackMailProperty}.
     *
     * @param defaultValue The default value of the property
     * @param optional Whether the property is optional
     */
    private SchedJoulesProperty() {
        this("", PREFIX);
    }

    /**
     * Initialises a new {@link UserFeedbackMailProperty}.
     *
     * @param defaultValue The default value of the property
     * @param optional Whether the property is optional
     */
    private SchedJoulesProperty(Object defaultValue) {
        this(defaultValue, PREFIX);
    }

    /**
     * Initialises a new {@link UserFeedbackMailProperty}.
     *
     * @param defaultValue The default value of the property
     * @param optional Whether the property is optional
     */
    private SchedJoulesProperty(Object defaultValue, String fqn) {
        this.defaultValue = defaultValue;
        this.fqn = fqn;
    }

    @Override
    public String getFQPropertyName() {
        return fqn + name();
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }
}
