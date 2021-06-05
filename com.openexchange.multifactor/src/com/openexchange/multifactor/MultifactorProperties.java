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

package com.openexchange.multifactor;

import com.openexchange.config.lean.Property;

/**
 * {@link MultifactorProperties}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public enum MultifactorProperties implements Property {

    /**
     * Allow multiple primary multifactor devices
     */
    allowMultiple(Boolean.TRUE),

    /**
     * WARNING: This puts the multifactor framework into demo mode.
     * This is for testing only!
     * DO NOT SET TO TRUE IN A PRODUCTIVE ENVIRONMENT!
     */
    demo(Boolean.FALSE),

    /**
     * List of urls that require a recent multifactor authentication. This means that the use must have
     * authenticated within the recentAuthenticationTime.
     */
    recentAuthRequired("multifactor/device?action=delete, multifactor/device?action=startRegistration"),

    /**
     * The time, in minutes, that a multifactor authentication is considered "recent".
     *
     * Some actions (defined in {@link MultifactorProperties#recentAuthRequired}) require that the client performed
     * multifactor authentication recently. If the multifactor authentication happened prior the configured amount of
     * minutes, the requests defined in recentAuthRequired will be denied.
     */
    recentAuthenticationTime(Integer.valueOf(10));

    public static final String PREFIX = "com.openexchange.multifactor.";
    private Object             defaultValue;

    MultifactorProperties(Object defaultValue) {
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
