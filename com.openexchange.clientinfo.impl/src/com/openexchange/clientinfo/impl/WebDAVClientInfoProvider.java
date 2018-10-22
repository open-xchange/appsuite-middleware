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

import com.openexchange.ajax.Client;
import com.openexchange.clientinfo.ClientInfo;
import com.openexchange.clientinfo.ClientInfoProvider;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;


/**
 * {@link WebDAVClientInfoProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.1
 */
public class WebDAVClientInfoProvider implements ClientInfoProvider {

    private static final String OS_FAMILY_WINDOWS = "windows";
    private static final String OS_FAMILY_MACOS = "macos";
    private static final String OS_FAMILY_LINUX = "linux";
    private static final String OS_FAMILY_ANDROID = "android";
    private static final String OS_FAMILY_IOS = "ios";

    /**
     * Initializes a new {@link WebDAVClientInfoProvider}.
     */
    public WebDAVClientInfoProvider() {
        super();
    }

    @Override
    public ClientInfo getClientInfo(Session session) {
        if (null == session) {
            return null;
        }
        String userAgent = (String) session.getParameter(Session.PARAM_USER_AGENT);
        if (Strings.isEmpty(userAgent)) {
            return getClientInfo(session.getClient());
        }

        // UserAgentParser is useless here, parse manually...

        // Windows Explorer example
        // Microsoft-WebDAV-MiniRedir/6.1.7601
        if (userAgent.contains("Microsoft-WebDAV")) {
            return new WebDAVClientInfo("Windows Explorer (WebDAV)", OS_FAMILY_WINDOWS);
        }

        // mac OS Finder example
        // WebDAVFS/3.0.1 (03018000) Darwin/13.4.0
        if (userAgent.contains("WebDAVFS") && userAgent.contains("Darwin")) {
            return new WebDAVClientInfo("macOS Finder (WebDAV)", OS_FAMILY_MACOS);
        }

        // Dolphin example
        // Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/534.34 (KHTML, like Gecko) dolphin/18.08.2 Safari/534.34
        if (userAgent.contains("dolphin")) {
            return new WebDAVClientInfo("Dolphin (WebDAV)", OS_FAMILY_LINUX);
        }

        // Konqueror example
        // Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/534.34 (KHTML, like Gecko) konqueror/5.0.97 Safari/534.34
        if (userAgent.contains("konqueror")) {
            return new WebDAVClientInfo("Konqueror (WebDAV)", OS_FAMILY_LINUX);
        }

        // Nautilus/Nemo/Thunar/Caja example
        // gvfs/1.36.1
        if (userAgent.contains("gvfs")) {
            return new WebDAVClientInfo("Nautilus/Nemo/Thunar/Caja (WebDAV)", OS_FAMILY_LINUX);
        }

        // DavFS client
        // davfs2/1.5.4 neon/0.30.2
        if (userAgent.contains("davfs2") || userAgent.contains("neon")) {
            return new WebDAVClientInfo("DavFS2 (WebDAV)", OS_FAMILY_LINUX);
        }

        // Default iOS
        if (userAgent.contains("iOS") || userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            return new WebDAVClientInfo(ClientInfoStrings.WEBDAV_INFOSTORE, OS_FAMILY_IOS);
        }

        // Default Android
        if (userAgent.contains("Android") || userAgent.contains("android")) {
            return new WebDAVClientInfo(ClientInfoStrings.WEBDAV_INFOSTORE, OS_FAMILY_ANDROID);
        }

        return new WebDAVClientInfo(ClientInfoStrings.WEBDAV_INFOSTORE, null);
    }

    @Override
    public ClientInfo getClientInfo(String clientId) {
        if (Strings.isNotEmpty(clientId)) {
            if (Client.WEBDAV_INFOSTORE.getClientId().equals(clientId)) {
                return new WebDAVClientInfo(ClientInfoStrings.WEBDAV_INFOSTORE, null);
            }
            if (Client.WEBDAV_VCARD.getClientId().equals(clientId)) {
                return new WebDAVClientInfo(ClientInfoStrings.WEBDAV_VCARD, null);
            }
            if (Client.WEBDAV_ICAL.getClientId().equals(clientId)) {
                return new WebDAVClientInfo(ClientInfoStrings.WEBDAV_ICAL, null);
            }
        }
        return null;
    }

}
