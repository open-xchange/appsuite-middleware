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
 * {@link WebClientInfo}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.0
 */
public class WebClientInfo implements ClientInfo {

    private final String client;
    private final String platform;
    private final String platformFamily;
    private final String platformVersion;
    private final String browser;
    private final String browserVersion;
    private final String browserFamily;

    public WebClientInfo(String client, String platform, String platformFamily, String platformVersion, String browser, String browserVersion, String browserFamily) {
        this.client = client;
        this.platform = platform;
        this.platformFamily = platformFamily;
        this.platformVersion = platformVersion;
        this.browser = browser;
        this.browserVersion = browserVersion;
        this.browserFamily = browserFamily;
    }

    @Override
    public String getDisplayName(Locale locale) {
        StringHelper helper = StringHelper.valueOf(locale);
        String out;

        if (Strings.isNotEmpty(browser)) {
            if (Strings.isNotEmpty(platform)) {
                if (Strings.isNotEmpty(browserVersion)) {
                    out = helper.getString(ClientInfoStrings.WEB_WITH_CLIENT_CLIENTVERSION_PLATFORM);
                    return String.format(out, client, browser, browserVersion, platform);
                } else {
                    out = helper.getString(ClientInfoStrings.WEB_WITH_CLIENT_PLATFORM);
                    return String.format(out, client, browser, platform);
                }
            } else {
                if (Strings.isNotEmpty(browserVersion)) {
                    out = helper.getString(ClientInfoStrings.WEB_WITH_CLIENT_CLIENTVERSION);
                    return String.format(out, client, browser, browserVersion);
                } else {
                    StringBuilder sb = new StringBuilder(client).append(", ").append(browser);
                    return sb.toString();
                }
            }
        }
        return client;
    }

    @Override
    public ClientInfoType getType() {
        return ClientInfoType.BROWSER;
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
        if (Strings.isNotEmpty(browser)) {
            return browser.toLowerCase();
        }
        return null;
    }

    @Override
    public String getClientVersion() {
        return browserVersion;
    }

    @Override
    public String getClientFamily() {
        return browserFamily;
    }

}
