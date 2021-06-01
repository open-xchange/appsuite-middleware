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
    IOS_REMINDERS("iOS Reminders (CalDAV)"),
    THUNDERBIRD_LIGHTNING("Mozilla Thunderbird / Lightning (CalDAV)"),
    EM_CLIENT("eM Client (CalDAV/CardDAV)"),
    EM_CLIENT_FOR_APPSUITE("eM Client for OX App Suite (CalDAV/CardDAV)"),
    OX_SYNC("OX Sync on Android (CalDAV/CardDAV)"),
    CALDAV_SYNC("CalDAV-Sync on Android (CalDAV)"),
    CARDDAV_SYNC("CardDAV-Sync on Android (CardDAV)"),
    SMOOTH_SYNC("SmoothSync on Android (CalDAV/CardDAV)"),
    DAVDROID("DAVdroid on Android (CalDAV/CardDAV)"),
    DAVX5("DAVx\u2075 on Android (CalDAV/CardDAV)"),
    OUTLOOK_CALDAV_SYNCHRONIZER("Outlook CalDav Synchronizer (CalDAV/CardDAV)"),
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
            if (userAgent.contains("DAVx5")) {
                return DAVUserAgent.DAVX5;
            }
            if (userAgent.contains("DAVdroid")) {
                return DAVUserAgent.DAVDROID;
            }
            if (userAgent.contains("Thunderbird") && userAgent.contains("Mozilla")) {
                return THUNDERBIRD_LIGHTNING;
            }
            if (userAgent.contains("iOS") && userAgent.contains("remindd") && false == userAgent.contains("Android")) {
                return IOS_REMINDERS;
            }
            if (userAgent.contains("iOS") && userAgent.contains("dataaccessd") && false == userAgent.contains("Android")) {
                return IOS;
            }
            if (userAgent.contains("CalDavSynchronizer")) {
                return OUTLOOK_CALDAV_SYNCHRONIZER;
            }
            if (userAgent.startsWith("MSFT-WP")) {
                return WINDOWS_PHONE;
            }
            if (userAgent.startsWith("MSFT-WIN")) {
                return WINDOWS;
            }
            if ((userAgent.contains("Mac OS") || userAgent.contains("Mac+OS") || userAgent.contains("macOS")) &&
                (userAgent.contains("CalendarStore") || (userAgent.contains("CalendarAgent")))) {
                return MAC_CALENDAR;
            }
            if ((userAgent.contains("Mac OS") || userAgent.contains("Mac_OS") || userAgent.contains("macOS")) &&
                (userAgent.contains("AddressBook"))) {
                return MAC_CONTACTS;
            }
            if (userAgent.contains("eM Client for OX App Suite")) {
                return EM_CLIENT_FOR_APPSUITE;
            }
            if (userAgent.contains("eM Client") || userAgent.contains("eMClient")) {
                return EM_CLIENT;
            }
        }
        return UNKNOWN;
    }

}
