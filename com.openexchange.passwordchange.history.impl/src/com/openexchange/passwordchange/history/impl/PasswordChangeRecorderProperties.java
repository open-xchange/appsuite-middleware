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

package com.openexchange.passwordchange.history.impl;

import com.openexchange.config.lean.Property;
import com.openexchange.passwordchange.history.PasswordChangeRecorder;

/**
 * {@link PasswordChangeRecorderProperties}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public enum PasswordChangeRecorderProperties implements Property {

    /**
     * The property that indicates if a password change should be recorded into a history.
     * <p>
     * If set to <code>true</code> a history is saved.<br>
     * If set to <code>false</code> no history is saved.
     * <p>
     * Default is <code>true</code>.
     */
    ENABLE("enabled", Boolean.TRUE),

    /**
     * The recorder that takes care of the password change history.<br>
     * It has to be registered as {@link PasswordChangeRecorder} before usage.
     * <p>
     * Default is <code>"default"</code> for the shipped version.
     */
    RECORDER("recorder", "default"),

    /**
     * The number of entries to be saved within the {@link PasswordChangeRecorder} for a certain user.
     * <p>
     * Default is <code>10</code>.
     */
    LIMIT("limit", Integer.valueOf(10))

    ;

    private final String fqn;
    private final Object defaultValue;

    private PasswordChangeRecorderProperties(String name, Object defaultValue) {
        this.defaultValue = defaultValue;
        this.fqn = "com.openexchange.passwordchange.history." + name;
    }

    @Override
    public String getFQPropertyName() {
        return fqn;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }
}
