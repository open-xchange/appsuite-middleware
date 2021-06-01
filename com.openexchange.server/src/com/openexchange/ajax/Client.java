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

package com.openexchange.ajax;

import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 * {@link Client} - An enumeration for known clients accessing the AJAX HTTP-API.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public enum Client {

    /**
     * The client for OX6 UI: <code>"com.openexchange.ox.gui.dhtml"</code>
     */
    OX6_UI("com.openexchange.ox.gui.dhtml", "The client identifier for OX6 UI"),
    /**
     * The client for App Suite UI: <code>"open-xchange-appsuite"</code>
     */
    APPSUITE_UI("open-xchange-appsuite", "The client identifier for App Suite UI"),
    /**
     * The client for Mobile Mail App: <code>"open-xchange-mailapp"</code>
     */
    MOBILE_APP("open-xchange-mailapp", "The client identifier for Mail App"),
    /**
     * The client for USM/EAS: <code>"USM-EAS"</code>
     */
    USM_EAS("USM-EAS", "The client identifier for Microsoft Active-Sync"),
    /**
     * The client for USM/JSON (OLOX): <code>"USM-JSON"</code>
     */
    USM_JSON("USM-JSON", "The client identifier for Outlook Connector (OLOX)"),
    /**
     * The client for Outlook OXtender2 AddIn: <code>"OpenXchange.HTTPClient.OXAddIn"</code>
     */
    @Deprecated
    OUTLOOK_OXTENDER2_ADDIN("OpenXchange.HTTPClient.OXAddIn", "The client identifier for Outlook OXtender2 AddIn"),
    /**
     * The client for OX Notifier: <code>"OpenXchange.HTTPClient.OXNotifier"</code>
     */
    @Deprecated
    OXNOTIFIER("OpenXchange.HTTPClient.OXNotifier", "The client identifier for OX Notifier"),
    /**
     * The client for Outlook Updater 1: <code>"com.open-xchange.updater.olox1"</code>
     */
    @Deprecated
    OUTLOOK_UPDATER1("com.open-xchange.updater.olox1", "The client identifier for Outlook Updater v1"),
    /**
     * The client for Outlook Updater 2: <code>"com.open-xchange.updater.olox2"</code>
     */
    @Deprecated
    OUTLOOK_UPDATER2("com.open-xchange.updater.olox2", "The client identifier for Outlook Updater v2"),
    /**
     * The client for CardDAV: <code>"CARDDAV"</code>
     */
    CARDDAV("CARDDAV", "The client identifier for CardDAV"),
    /**
     * The client for CalDAV: <code>"CALDAV"</code>
     */
    CALDAV("CALDAV", "The client identifier for CalDAV"),
    /**
     * The client for WebDAV iCal: <code>"WEBDAV_ICAL"</code>
     */
    WEBDAV_ICAL("WEBDAV_ICAL", "The client identifier for WebDAV iCal"),
    /**
     * The client for WebDav InfoStore: <code>"WEBDAV_INFOSTORE"</code>
     */
    WEBDAV_INFOSTORE("WEBDAV_INFOSTORE", "The client identifier for WebDav InfoStore"),
    /**
     * The client for WebDav vCard: <code>"WEBDAV_VCARD"</code>
     */
    WEBDAV_VCARD("WEBDAV_VCARD", "The client identifier for WebDav vCard"),
    /**
     * The client identifier for Windows Drive client: <code>"OpenXchange.HTTPClient.OXDrive"</code>
     */
    DRIVE_WINDOWS("OpenXchange.HTTPClient.OXDrive", "The client identifier for Windows Drive client"),
    /**
     * The client identifier for Windows Drive client: <code>"OSX.OXDrive"</code>
     */
    DRIVE_OSX("OSX.OXDrive", "The client identifier for OSX Drive client"),
    /**
     * The client identifier for Windows Drive client: <code>"OpenXchange.iosClient.OXDrive"</code>
     */
    DRIVE_IOS("OpenXchange.iosClient.OXDrive", "The client identifier for iOS Drive client"),
    /**
     * The client identifier for Windows Drive client: <code>"OpenXchange.Android.OXDrive"</code>
     */
    DRIVE_ANDROID("OpenXchange.Android.OXDrive", "The client identifier for Android Drive client"),
    /**
     * The client identifier for Mail App: <code>"open-xchange-mailapp"</code>
     */
    @Deprecated
    MAIL_APP("open-xchange-mailapp", "The client identifier for Mail App"),
    /**
     * The client identifier for plain Mobile API Facade: <code>"open-xchange-mobile-api-facade"</code>
     */
    MOBILE_API_FACADE("open-xchange-mobile-api-facade", "The client identifier for plain Mobile API Facade");


    private final String clientId;
    private final String description;

    /**
     * Initializes a new {@link Client}.
     */
    private Client(String clientId, String description) {
        this.clientId = clientId;
        this.description = description;
    }

    /**
     * Gets the client identifier
     *
     * @return The client identifier
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Gets the description
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return getClientId();
    }

    /**
     * Gets the Client with the given clientID
     *
     * @param clientID
     * @return the Client
     */
    public static Client getClientByID(String clientID) {
        Client[] clients = Client.values();
        for (Client clt : clients)
        {
            if (clt.clientId.contentEquals(clientID))
            {
                return clt;
            }
        }
        return null;
    }

    // ----------------------------------------------------------------------------------------------------------------------

    /**
     * Checks if given session is associated with App Suite UI client.
     *
     * @param session The session to examine
     * @return <code>true</code> if given session is associated with App Suite UI client; otherwise <code>false</code>
     */
    public static boolean isAppSuiteUI(Session session) {
        return isAppSuiteUI(session.getClient());
    }

    /**
     * Checks if given client identifier denotes App Suite UI client.
     *
     * @param clientId The client identifier to examine
     * @return <code>true</code> if given client identifier denotes App Suite UI client; otherwise <code>false</code>
     */
    public static boolean isAppSuiteUI(String clientId) {
        if (Strings.isEmpty(clientId)) {
            return false;
        }

        String uc = Strings.asciiLowerCase(clientId);
        return APPSUITE_UI.clientId.equals(uc);
    }

    // ----------------------------------------------------------------------------------------------------------------------

    /**
     * Checks if given session is associated with an USM client.
     *
     * @param session The session to examine
     * @return <code>true</code> if given session is associated with an USM client; otherwise <code>false</code>
     */
    public static boolean isUsmEas(Session session) {
        return isUsmEas(session.getClient());
    }

    /**
     * Checks if given client identifier denotes an USM client.
     *
     * @param clientId The client identifier to examine
     * @return <code>true</code> if given client identifier denotes an USM client; otherwise <code>false</code>
     */
    public static boolean isUsmEas(String clientId) {
        if (Strings.isEmpty(clientId)) {
            return false;
        }

        String uc = Strings.toUpperCase(clientId);
        return uc.startsWith(USM_EAS.clientId) || uc.startsWith(USM_JSON.clientId);
    }

}
