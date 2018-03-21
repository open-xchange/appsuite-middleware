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
