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

package com.openexchange.drive.impl;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link DriveClientInfoStrings}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.0
 */
public class DriveClientInfoStrings implements LocalizableStrings {

    // %1$s %2$s on %3$s %4$s
    // E.g. Drive Client 2.4.0 on Windows 10
    public static final String DRIVE_CLIENT_INFO_WITH_PLATFORM_VERSION = "%1$s %2$s on %3$s %4$s";

    // %1$s %2$s on %3$s
    // E.g. Drive Client 2.4.0 on Windows
    public static final String DRIVE_CLIENT_INFO_WITH_PLATFORM = "%1$s %2$s on %3$s";

    // %1$s on %2$s
    // E.g. Drive Client on Windows
    public static final String DRIVE_CLIENT_INFO_WITHOUT_VERSION = "%1$s on %2$s";

    // %1$s %2$s
    // E.g. Drive Client 2.4.0
    public static final String DRIVE_CLIENT_INFO_WITH_VERSION = "%1$s %2$s";

    // %1$s
    // E.g. Drive Client
    public static final String DRIVE_CLIENT = "%1$s";

    private DriveClientInfoStrings() {
        super();
    }

}
