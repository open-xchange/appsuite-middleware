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

package com.openexchange.authentication.application.impl;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link AppPasswordStrings}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public class AppPasswordStrings implements LocalizableStrings {

    // The display name in context of the maximum allowed number of app-specific passwords a user may create.
    public static final String QUOTA_MODULE_NAME = "App-specific passwords";

    // The display name used for a Drive Sync App / id "drive"
    public static final String DISPLAY_NAME_DRIVE = "Drive Sync App";

    // The display name used for a Calendar Client (CalDAV) / id "caldav"
    public static final String DISPLAY_NAME_CALDAV = "Calendar Client (CalDAV)";

    // The display name used for a Addressbook Client (CardDAV) / id "carddav"
    public static final String DISPLAY_NAME_CARDDAV = "Addressbook Client (CardDAV)";

    // The display name used for a Calendar and Addressbook Client (CalDAV/CardDAV) / id "calcarddav"
    public static final String DISPLAY_NAME_CALCARDDAV = "Calendar and Addressbook Client (CalDAV/CardDAV)";

    // The display name used for a WebDAV Client / id "webdav"
    public static final String DISPLAY_NAME_WEBDAV = "WebDAV Client";

    /**
     * Initializes a new {@link AppPasswordStrings}.
     */
    private AppPasswordStrings() {
        super();
    }

}
