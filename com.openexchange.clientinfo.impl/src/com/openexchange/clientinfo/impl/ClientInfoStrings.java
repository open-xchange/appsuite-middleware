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

package com.openexchange.clientinfo.impl;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link ClientInfoStrings}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.0
 */
public class ClientInfoStrings implements LocalizableStrings {

    // %1$s, %2$s on %3$s %4$s
    // E.g. App Suite UI, Chrome 61 on Windows 10
    public final static String WEB_WITH_CLIENT_CLIENTVERSION_PLATFORM_FLATFORMVERSION = "%1$s, %2$s %3$s on %4$s %5$s";

    // %1$s, %2$s %3$s on %4$s"
    // E.g. App Suite UI, Chrome 64 on Linux
    public final static String WEB_WITH_CLIENT_CLIENTVERSION_PLATFORM = "%1$s, %2$s %3$s on %4$s";

    // %1$s, %2$s %3$s
    // E.g. App Suite UI, Chrome 64
    public final static String WEB_WITH_CLIENT_CLIENTVERSION = "%1$s, %2$s %3$s";

    // %1$s, %2$s on %3$s %4$s"
    // E.g. App Suite UI, Edge on Windows 10
    public final static String WEB_WITH_CLIENT_PLATFORM_FLATFORMVERSION = "%1$s, %2$s on %3$s %4$s";

    // %1$s, %2$s on %3$s
    // E.g. App Suite UI, Edge on Windows
    public final static String WEB_WITH_CLIENT_PLATFORM = "%1$s, %2$s on %3$s";

    // %1$s on %2$s %3$s
    // E.g. App Suite UI on Windows 10
    public final static String CLIENT_BROWSER_INFO_MESSAGE = "%1$s on %2$s %3$s";

    // Microsoft Exchange ActiveSync Client
    public final static String USM_EAS_CLIENT = "Microsoft Exchange ActiveSync Client";

    // WebDAV InfoStore
    public final static String WEBDAV_INFOSTORE = "WebDAV InfoStore";

    // WebDAV VCard
    public final static String WEBDAV_VCARD = "WebDAV VCard";

    // WebDAV iCal
    public final static String WEBDAV_ICAL = "WebDAV iCal";

    // %1$s %2$s on %3$s %4$s
    // E.g. OX Mail App 1.10 on Android 8.0
    public final static String MAILAPP_WITH_VERSION_AND_PLATFORM_AND_PLATFORMVERSION = "%1$s %2$s on %3$s %4$s";

    // %1$s on %2$s %3$s
    // E.g. OX Mail App on Android 8.0
    public final static String MAILAPP_WITH_PLATFORM_AND_PLATFORMVERSION = "%1$s on %2$s %3$s";

    // %1$s %2$s on %3$s
    // E.g. OX Mail App 1.10 on Android
    public final static String MAILAPP_WITH_VERSION_AND_PLATFORM = "%1$s %2$s on %3$s";

    // %1$s on %2$s
    // E.g. OX Mail App on Android
    public final static String MAILAPP_WITH_PLATFORM = "%1$s on %2$s";

    // Unknown client
    public final static String UNKNOWN_CLIENT = "Unknown client";

    private ClientInfoStrings() {
        super();
    }

}
