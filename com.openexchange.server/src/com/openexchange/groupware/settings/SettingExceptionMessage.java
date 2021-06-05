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

package com.openexchange.groupware.settings;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link SettingExceptionMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class SettingExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link SettingExceptionMessage}.
     */
    private SettingExceptionMessage() {
        super();
    }

    /** Writing the setting %1$s is not permitted. */
    public final static String NO_WRITE_MSG = "Writing the setting %1$s is not permitted.";

    /** Invalid value %s written to setting %s. */
    public final static String INVALID_VALUE_MSG = "Invalid value %s written to setting %s.";

}
