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

package com.openexchange.drive.client.windows.service.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.drive.client.windows.service.Constants;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link Utils}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class Utils {

    public static boolean hasPermissions(UserConfiguration userConfig, String[] necessaryPermission) {
        for (String cap : necessaryPermission) {
            if (!userConfig.hasPermission(cap)) {
                return false;
            }
        }
        return true;
    }

    public static String getServerUrl(final HttpServletRequest req, final Session session) {
        String host = null;
        HostnameService hostnameService = Services.optService(HostnameService.class);
        if (hostnameService != null) {
            host = hostnameService.getHostname(session.getUserId(), session.getContextId());
        }

        if (host == null) {
            /*
             * See org.glassfish.grizzly.http.HttpServerFilter.parseHost(DataChunk, HttpRequestPacket, HttpResponsePacket, ProcessingState)
             */
            host = req.getServerName();
        }

        return Tools.getProtocol(req) + host;
    }

    public static String getServletPrefix() {
        DispatcherPrefixService prefixService = Services.getService(DispatcherPrefixService.class);
        if (prefixService == null) {
            return DispatcherPrefixService.DEFAULT_PREFIX;
        }

        return prefixService.getPrefix();
    }

    public static String getUserName(ServerSession session) throws OXException {
        return getUserName(session, session.getUser());
    }

    public static String getUserName(Session session, User user) throws OXException {
        String login = session.getLogin();
        if (login == null) {
            if (user != null) {
                // Fallback. session.getLogin() may be null in some special cases (usage of custom plugins like 1und1 UAS).
                login = user.getLoginInfo();
            }
        }

        if (login == null) {
            // Double check. May also be null in some custom implementations.
            // In this case the user is forced to type in his login name manually.
            login = "";
        }

        return login;
    }

    public static String getFileUrl(String serverUrl, String fileName) {
        return compileUrl(serverUrl, getServletPrefix(), Constants.DOWNLOAD_SERVLET, fileName);
    }

    public static String getUpdateXMLUrl(String serverUrl) {
        return compileUrl(serverUrl, getServletPrefix(), Constants.UPDATE_SERVLET);
    }

    public static String getInfostoreWebDavUrl(String serverUrl) {
        return compileUrl(serverUrl, "servlet/webdav.infostore");
    }

    public static String getInfostoreUrl(String serverUrl) {
        return compileUrl(serverUrl, getServletPrefix(), "infostore");
    }

    public static String compileUrl(String baseUrl, String... segments) {
        return compileUrl(baseUrl, segments, Collections.<String, String>emptyMap());
    }

    public static String compileUrl(String baseUrl, String[] pathSegments, Map<String, String> queryParams) {
        StringBuilder path = new StringBuilder();
        if (pathSegments != null && pathSegments.length > 0) {
            for (String segment : pathSegments) {
                if (segment != null && segment.length() > 0) {
                    if (segment.charAt(0) != '/') {
                        path.append('/');
                    }
                    if (segment.charAt(segment.length() - 1) == '/') {
                        path.append(segment.substring(0, segment.length() - 1));
                    } else {
                        path.append(segment);
                    }
                }
            }
        }

        URIBuilder uri = new URIBuilder(URI.create(baseUrl)).setPath(path.toString());
        if (queryParams != null && queryParams.size() > 0) {
            for (Entry<String, String> entry : queryParams.entrySet()) {
                uri.addParameter(entry.getKey(), entry.getValue());
            }
        }
        return uri.toString();
    }

    public static String convertToBase64(File icon) throws IOException {
        InputStream is = null;
        try {
            is = new Base64InputStream(new FileInputStream(icon), true);
            return IOUtils.toString(is);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

}
