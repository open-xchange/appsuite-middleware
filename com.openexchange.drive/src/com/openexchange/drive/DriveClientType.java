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

package com.openexchange.drive;

import com.openexchange.java.Strings;

/**
 * {@link DriveClientType}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public enum DriveClientType {

    /**
     * An unknown client
     */
    UNKNOWN(null, true),

    /**
     * The windows desktop client
     */
    WINDOWS("OpenXchange.HTTPClient.OXDrive", true),

    /**
     * The Mac OS desktop client
     */
    MAC_OS("OSX.OXDrive", true),

    /**
     * The iOS mobile client
     */
    IOS("OpenXchange.iosClient.OXDrive", false),

    /**
     * The iOS Drive 3.0 client
     */
    IOS_DRIVE3("iOS.Drive", false),

    /**
     * The Android mobile client
     */
    ANDROID("OpenXchange.Android.OXDrive", false),

    /**
     * The .NET test drive client
     */
    TEST_DRIVE("OpenXchange.HTTPClient.TestDrive", true),
    ;

    private String clientString;
    private boolean desktop;

    private DriveClientType(String clientString, boolean desktop) {
        this.clientString = clientString;
        this.desktop = desktop;
    }

    /**
     * Gets the client string identifier as set by the client, e.g. "OpenXchange.HTTPClient.OXDrive".
     *
     * @return The client string
     */
    public String getClientString() {
        return clientString;
    }

    /**
     * Gets a value indicating whether the client denotes a "desktop" client or not.
     *
     * @return <code>true</code> for a "desktop" client, <code>false</code>, otherwise
     */
    public boolean isDesktop() {
        return desktop;
    }

    /**
     * Gets the drive client type matching the supplied client string.
     *
     * @param clientString The client string to parse
     * @return The drive client type, or {@link #UNKNOWN} if unknown.
     */
    public static DriveClientType parse(String clientString) {
        if (Strings.isNotEmpty(clientString)) {
            for (DriveClientType driveClient : DriveClientType.values()) {
                if (clientString.equals(driveClient.clientString)) {
                    return driveClient;
                }
            }
        }
        return UNKNOWN;
    }

}
