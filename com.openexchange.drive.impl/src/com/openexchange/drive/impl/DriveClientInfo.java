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

package com.openexchange.drive.impl;

import java.util.Locale;
import com.openexchange.clientinfo.ClientInfo;
import com.openexchange.clientinfo.ClientInfoType;
import com.openexchange.drive.impl.management.DriveConfig;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;

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

    public DriveClientInfo(String platform, String platformVersion, String appVersion, String osFamily) {
        super();
        this.platform = platform;
        this.platformVersion = platformVersion;
        this.appVersion = appVersion;
        this.osFamily = osFamily;
    }

    @Override
    public ClientInfoType getType() {
        return ClientInfoType.OXAPP;
    }

    @Override
    public String getDisplayName(Locale locale) {
        String app = DriveConfig.getInstance().getShortProductName();
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
        return DriveConfig.getInstance().getShortProductName().toLowerCase();
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
