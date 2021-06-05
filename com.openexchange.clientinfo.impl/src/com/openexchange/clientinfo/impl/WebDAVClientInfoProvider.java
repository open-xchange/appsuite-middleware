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

import com.openexchange.ajax.Client;
import com.openexchange.clientinfo.ClientInfo;
import com.openexchange.clientinfo.ClientInfoProvider;
import com.openexchange.java.Strings;
import com.openexchange.session.Origin;
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

        if (!isWebDAV(session)) {
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

        return new WebDAVClientInfo("Generic WebDAV", "unknown");
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

    private boolean isWebDAV(Session session) {
        Origin origin = session.getOrigin();
        if (null != origin) {
            return Origin.WEBDAV_ICAL.equals(origin) || Origin.WEBDAV_INFOSTORE.equals(origin) || Origin.WEBDAV_VCARD.equals(origin);
        }
        return false;
    }

}
