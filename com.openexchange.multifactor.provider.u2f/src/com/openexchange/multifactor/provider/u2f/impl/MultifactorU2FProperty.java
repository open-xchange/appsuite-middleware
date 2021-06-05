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

package com.openexchange.multifactor.provider.u2f.impl;

import com.openexchange.config.lean.Property;
import com.openexchange.multifactor.MultifactorProperties;

/**
 * {@link MultifactorU2FProperty} configuration properties for U2F multifactor authentication
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public enum MultifactorU2FProperty implements Property {

    /**
     * Defines if the TOTP provider is enabled.
     *
     * Only providers which are "enabled" can be used by a user.
     */
    enabled(Boolean.FALSE),

    /**
     * Defines the U2f APP ID to propagate to the client. Empty to use the host-name
     */
    appId(""),

    /**
     * Defines the lifetime of a U2F challenge token for authentication; i.e this is the time the client needs to resolve the challenge.
     */
    tokenLifetime(Integer.valueOf(60));

    private static final String PREFIX = MultifactorProperties.PREFIX + "u2f.";
    private Object              defaultValue;

    /**
     * Initializes a new {@link MultifactorU2FProperty}.
     *
     * @param defaultValue The default value for the property
     */
    MultifactorU2FProperty(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String getFQPropertyName() {
        return PREFIX + name();
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

}
