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

import java.util.Locale;
import com.openexchange.clientinfo.ClientInfo;
import com.openexchange.clientinfo.ClientInfoType;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;


/**
 * {@link MailAppClientInfo}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.0
 */
public class MailAppClientInfo implements ClientInfo {

    private final String app;
    private final String appVersion;
    private final String platformFamily;
    private final String platformVersion;


    public MailAppClientInfo(String app) {
        this(app, null, null, null);
    }

    public MailAppClientInfo(String app, String appVersion, String platformFamily, String platformVersion) {
        super();
        this.app = app;
        this.appVersion = appVersion;
        this.platformFamily = platformFamily;
        this.platformVersion = platformVersion;
    }

    @Override
    public ClientInfoType getType() {
        return ClientInfoType.OXAPP;
    }

    @Override
    public String getDisplayName(Locale locale) {
        StringHelper helper = StringHelper.valueOf(locale);
        String out;
        if (Strings.isNotEmpty(platformFamily)) {
            if (Strings.isNotEmpty(platformVersion)) {
                if (Strings.isNotEmpty(appVersion)) {
                    out = helper.getString(ClientInfoStrings.MAILAPP_WITH_VERSION_AND_PLATFORM_AND_PLATFORMVERSION);
                    return String.format(out, app, appVersion, platformFamily, platformVersion);
                } else {
                    out = helper.getString(ClientInfoStrings.MAILAPP_WITH_PLATFORM_AND_PLATFORMVERSION);
                    return String.format(out, app, platformFamily, platformVersion);
                }
            } else {
                if (Strings.isNotEmpty(appVersion)) {
                    out = helper.getString(ClientInfoStrings.MAILAPP_WITH_VERSION_AND_PLATFORM);
                    return String.format(out, app, appVersion, platformFamily);
                } else {
                    out = helper.getString(ClientInfoStrings.MAILAPP_WITH_PLATFORM);
                    return String.format(out, app, platformFamily);
                }
            }
        }
        return app;
    }

    @Override
    public String getOSFamily() {
        if (Strings.isNotEmpty(platformFamily)) {
            return platformFamily.toLowerCase();
        }
        return null;
    }

    @Override
    public String getOSVersion() {
        return platformVersion;
    }

    @Override
    public String getClientName() {
        if (Strings.isNotEmpty(app)) {
            return app.toLowerCase();
        }
        return null;
    }

    @Override
    public String getClientVersion() {
        return appVersion;
    }

    @Override
    public String getClientFamily() {
        return "oxmailapp";
    }

}
