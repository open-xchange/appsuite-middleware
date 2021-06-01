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

package com.openexchange.drive.client.windows.service.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.drive.client.windows.service.Constants;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.java.Charsets;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

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

    public static String getUserName(ServerSession session) {
        return getUserName(session, session.getUser());
    }

    public static String getUserName(Session session, User user) {
        String login = session.getLogin();
        if (login != null) {
            return login;
        }
        if (user != null) {
            // Fallback. session.getLogin() may be null in some special cases (usage of custom plugins like 1und1 UAS).
            login = user.getLoginInfo();
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
        return compileUrl(baseUrl, segments, Collections.<String, String> emptyMap());
    }

    public static String compileUrl(String baseUrl, String[] pathSegments, Map<String, String> queryParams) {
        String path = buildPathSegments(pathSegments);
        URIBuilder uri = new URIBuilder(URI.create(baseUrl)).setPath(path);
        buildURIParameters(queryParams, uri);
        return uri.toString();
    }

    public static String convertToBase64(File icon) throws IOException {
        try (InputStream is = new Base64InputStream(new FileInputStream(icon), true)) {
            return IOUtils.toString(is, Charsets.US_ASCII);
        }
    }

    private static void buildURIParameters(Map<String, String> queryParams, URIBuilder uri) {
        if (queryParams == null || queryParams.size() == 0) {
            return;
        }
        queryParams.entrySet().stream().forEach((entry) -> uri.addParameter(entry.getKey(), entry.getValue()));
    }

    private static String buildPathSegments(String[] pathSegments) {
        if (pathSegments == null || pathSegments.length == 0) {
            return "";
        }
        StringBuilder path = new StringBuilder();
        for (String segment : pathSegments) {
            if (segment == null || segment.length() == 0) {
                continue;
            }
            if (segment.charAt(0) != '/') {
                path.append('/');
            }
            if (segment.charAt(segment.length() - 1) == '/') {
                path.append(segment.substring(0, segment.length() - 1));
            } else {
                path.append(segment);
            }
        }
        return path.toString();
    }
}
