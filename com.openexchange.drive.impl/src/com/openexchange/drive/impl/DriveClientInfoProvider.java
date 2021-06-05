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

import com.openexchange.clientinfo.ClientInfo;
import com.openexchange.clientinfo.ClientInfoProvider;
import com.openexchange.drive.DriveClientType;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;


/**
 * {@link DriveClientInfoProvider}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.0
 */
public class DriveClientInfoProvider implements ClientInfoProvider {

    /**
     * Initializes a new {@link DriveClientInfoProvider}.
     */
    public DriveClientInfoProvider() {
        super();
    }

    @Override
    public ClientInfo getClientInfo(Session session) {
        if (null == session) {
            return null;
        }
        String clientId = session.getClient();
        if (Strings.isNotEmpty(clientId)) {
            DriveClientType type = DriveClientType.parse(clientId);
            switch (type) {
                case ANDROID:
                    return new DriveClientInfo("Android", null, null, "android", session);
                case IOS:
                    return new DriveClientInfo("iOS", null, null, "ios", session);
                case IOS_DRIVE3:
                    return new DriveClientInfo("iOS", null, "Files", "ios", session);
                case MAC_OS:
                    return new DriveClientInfo("Mac OS", null, null, "macos", session);
                case WINDOWS:
                    return new DriveClientInfo("Windows", null, null, "windows", session);
                default:
                    if ("OXDrive".equals(clientId)) {
                        return new DriveClientInfo(null, null, null, null, session);
                    }
                    return null;
            }
        }
        return null;
    }

    @Override
    public ClientInfo getClientInfo(String clientId) {
        if (Strings.isNotEmpty(clientId)) {
            DriveClientType type = DriveClientType.parse(clientId);
            switch (type) {
                case ANDROID:
                    return new DriveClientInfo("Android", null, null, "android");
                case IOS:
                    return new DriveClientInfo("iOS", null, null, "ios");
                case IOS_DRIVE3:
                    return new DriveClientInfo("iOS", null, "Files", "ios");
                case MAC_OS:
                    return new DriveClientInfo("Mac OS", null, null, "macos");
                case WINDOWS:
                    return new DriveClientInfo("Windows", null, null, "windows");
                default:
                    if ("OXDrive".equals(clientId)) {
                        return new DriveClientInfo(null, null, null, null);
                    }
                    return null;
            }
        }
        return null;
    }

}
