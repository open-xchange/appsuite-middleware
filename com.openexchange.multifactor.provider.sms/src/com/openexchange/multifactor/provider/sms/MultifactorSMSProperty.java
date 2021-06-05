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

package com.openexchange.multifactor.provider.sms;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.config.lean.Property;
import com.openexchange.multifactor.MultifactorProperties;

/**
 * {@link MultifactorSMSProperty}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public enum MultifactorSMSProperty implements Property {

    /**
     * Defines if the SMS provider is enabled.
     *
     * Only providers which are "enabled" can be used by a user.
     */
    enabled(Boolean.FALSE),

    /**
     * Defines if the SMS provider can also be used as "backup provider".
     * I.E. This defines if it's possible for a user to register SMS based devices as backup device.
     */
    backup(Boolean.TRUE),

    /**
     * Defines the length of the SMS token
     */
    tokenLength(I(8)),

    /**
     * Defines the lifetime (in seconds) of the SMS token in minutes before it expires and cannot be used for authentication anymore.
     *
     */
    tokenLifetime(I(120)),

    /**
     * The maximum amount of active tokens a user is allowed to own at a point in time
     */
    maxTokenAmount(I(5));

    private static final String PREFIX = MultifactorProperties.PREFIX + "sms.";
    private Object defaultValue;

    MultifactorSMSProperty(Object defaultValue){
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
