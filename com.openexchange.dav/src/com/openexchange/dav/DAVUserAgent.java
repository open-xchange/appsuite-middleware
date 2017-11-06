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

package com.openexchange.dav;

/**
 * {@link DAVUserAgent}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public enum DAVUserAgent {

    MAC_CALENDAR("Mac OS Calendar (CalDAV)"),
    MAC_CONTACTS("Mac OS Addressbook (CardDAV)"),
    IOS("iOS Addressbook and Calendar (CalDAV/CardDAV)"),
    THUNDERBIRD_LIGHTNING("Mozilla Thunderbird / Lightning (CalDAV)"),
    EM_CLIENT("eM Client (CalDAV/CardDAV)"),
    EM_CLIENT_FOR_APPSUITE("eM Client for OX App Suite (CalDAV/CardDAV)"),
    OX_SYNC("OX Sync on Android (CalDAV/CardDAV)"),
    CALDAV_SYNC("CalDAV-Sync on Android (CalDAV)"),
    CARDDAV_SYNC("CardDAV-Sync on Android (CardDAV)"),
    SMOOTH_SYNC("SmoothSync on Android (CalDAV/CardDAV)"),
    DAVDROID("DAVdroid on Android (CalDAV/CardDAV)"),
    WINDOWS_PHONE("Windows Phone Contacts and Calendar (CalDAV/CardDAV)"),
    WINDOWS("Windows Contacts and Calendar (CalDAV/CardDAV)"),
    GENERIC_CALDAV("Sync Client (CalDAV)"),
    GENERIC_CARDDAV("Sync Client (CardDAV)"),
    UNKNOWN("CalDAV/CardDAV")
    ;

    private final String readableName;

    private DAVUserAgent(String readableName) {
        this.readableName = readableName;
    }

    /**
     * Gets a readable name for the user agent.
     *
     * @return The readable name
     */
    public String getReadableName() {
        return readableName;
    }

    /**
     * Parses a user-agent string from a HTTP request and tries to map it to a known *DAV client.
     *
     * @param userAgent The user agent string to parse
     * @return The parsed DAV client, or {@link DAVUserAgent#UNKNOWN} if not recognized or unknown
     */
    public static DAVUserAgent parse(String userAgent) {
        if (null != userAgent) {
            if (userAgent.contains("com.openexchange.mobile.syncapp.enterprise")) {
                return OX_SYNC;
            }
            if (userAgent.contains("org.dmfs.carddav.sync")) {
                return CARDDAV_SYNC;
            }
            if (userAgent.contains("org.dmfs.caldav.sync")) {
                return CALDAV_SYNC;
            }
            if (userAgent.contains("SmoothSync")) {
                return SMOOTH_SYNC;
            }
            if (userAgent.contains("DAVdroid")) {
                return DAVUserAgent.DAVDROID;
            }
            if (userAgent.contains("Lightning") && userAgent.contains("Thunderbird") && userAgent.contains("Mozilla")) {
                return THUNDERBIRD_LIGHTNING;
            }
            if (userAgent.contains("iOS") && userAgent.contains("dataaccessd") && false == userAgent.contains("Android")) {
                return IOS;
            }
            if (userAgent.startsWith("MSFT-WP")) {
                return WINDOWS_PHONE;
            }
            if (userAgent.startsWith("MSFT-WIN")) {
                return WINDOWS;
            }
            if ((userAgent.contains("Mac OS") || userAgent.contains("Mac+OS")) &&
                (userAgent.contains("CalendarStore") || (userAgent.contains("CalendarAgent")))) {
                return MAC_CALENDAR;
            }
            if ((userAgent.contains("Mac OS") || userAgent.contains("Mac_OS")) &&
                (userAgent.contains("AddressBook"))) {
                return MAC_CONTACTS;
            }
            if (userAgent.contains("eM Client for OX App Suite")) {
                return EM_CLIENT_FOR_APPSUITE;
            }
            if (userAgent.contains("eM Client")) {
                return EM_CLIENT;
            }
        }
        return UNKNOWN;
    }

}
