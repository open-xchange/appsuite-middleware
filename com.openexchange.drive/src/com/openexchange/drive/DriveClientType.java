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
        if (false == Strings.isEmpty(clientString)) {
            for (DriveClientType driveClient : DriveClientType.values()) {
                if (clientString.equals(driveClient.clientString)) {
                    return driveClient;
                }
            }
        }
        return UNKNOWN;
    }

}
