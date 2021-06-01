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

import java.util.Locale;
import com.openexchange.clientinfo.ClientInfo;
import com.openexchange.clientinfo.ClientInfoType;
import com.openexchange.drive.impl.management.DriveConfig;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 * {@link DriveClientInfo}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.0
 */
public class DriveClientInfo implements ClientInfo {

    private final String platform;
    private final String platformVersion;
    private final String appVersion;
    private final String osFamily;
    private final int    contextId;
    private final int    userId;

    public DriveClientInfo(String platform, String platformVersion, String appVersion, String osFamily, Session session) {
        super();
        this.platform = platform;
        this.platformVersion = platformVersion;
        this.appVersion = appVersion;
        this.osFamily = osFamily;
        this.contextId = session.getContextId();
        this.userId = session.getUserId();
    }

    public DriveClientInfo(String platform, String platformVersion, String appVersion, String osFamily) {
        super();
        this.platform = platform;
        this.platformVersion = platformVersion;
        this.appVersion = appVersion;
        this.osFamily = osFamily;
        this.contextId = -1;
        this.userId = -1;
    }

    @Override
    public ClientInfoType getType() {
        return ClientInfoType.OXAPP;
    }

    @Override
    public String getDisplayName(Locale locale) {
        String app = new DriveConfig(contextId, userId).getShortProductName();
        StringHelper helper = StringHelper.valueOf(locale);
        if (Strings.isNotEmpty(appVersion) && Strings.isNotEmpty(platform) && Strings.isNotEmpty(platformVersion)) {
            return String.format(helper.getString(DriveClientInfoStrings.DRIVE_CLIENT_INFO_WITH_PLATFORM_VERSION), app, appVersion, platform, platformVersion);
        }
        if (Strings.isNotEmpty(appVersion) && Strings.isNotEmpty(platform)) {
            return String.format(helper.getString(DriveClientInfoStrings.DRIVE_CLIENT_INFO_WITH_PLATFORM), app, appVersion, platform);
        }
        if (Strings.isEmpty(appVersion) && Strings.isNotEmpty(platform)) {
            return String.format(helper.getString(DriveClientInfoStrings.DRIVE_CLIENT_INFO_WITHOUT_VERSION), app, platform);
        }
        if (Strings.isNotEmpty(appVersion) && Strings.isEmpty(platform)) {
            return String.format(helper.getString(DriveClientInfoStrings.DRIVE_CLIENT_INFO_WITH_VERSION), app, appVersion);
        }
        if (Strings.isEmpty(platform)) {
            return String.format(helper.getString(DriveClientInfoStrings.DRIVE_CLIENT), app);
        }
        return app;
    }

    @Override
    public String getOSFamily() {
        return osFamily;
    }

    @Override
    public String getOSVersion() {
        return null;
    }

    @Override
    public String getClientName() {
        return new DriveConfig(contextId, userId).getShortProductName().toLowerCase();
    }

    @Override
    public String getClientVersion() {
        return null;
    }

    @Override
    public String getClientFamily() {
        return "oxdriveapp";
    }

}
