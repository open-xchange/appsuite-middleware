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

package com.openexchange.multifactor.provider.backupString.impl;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.config.lean.Property;
import com.openexchange.multifactor.MultifactorProperties;

/**
 * {@link MultifactorBackupStringProperty}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public enum MultifactorBackupStringProperty implements Property {

    /**
     * Defines if the BackupString provider is enabled.
     *
     * Only providers which are "enabled" can be used by a user.
     */
    enabled(Boolean.TRUE),

    /**
     * Defines the length of the string generated
     */
    stringLength(I(32));

    private static final String PREFIX = MultifactorProperties.PREFIX + "backupString.";
    private Object defaultValue;

    MultifactorBackupStringProperty(Object defaultValue){
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
