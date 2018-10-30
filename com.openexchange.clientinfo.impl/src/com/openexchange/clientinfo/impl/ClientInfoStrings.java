/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
