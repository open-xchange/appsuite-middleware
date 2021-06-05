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

package com.openexchange.http.grizzly.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.glassfish.grizzly.http.server.Request;
import org.slf4j.Logger;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.http.grizzly.eas.EASCommandCodes;
import com.openexchange.http.grizzly.osgi.Services;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;

/**
 * {@link RequestTools}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public final class RequestTools {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(RequestTools.class);

    /**
     * Initializes a new {@link RequestTools}.
     */
    private RequestTools() {
        super();
    }

    // --------------------------------------------------------------------------------------------------------------------------------------

    private static volatile String driveUri;

    private static String driveUri() {
        String tmp = driveUri;
        if (null == tmp) {
            synchronized (RequestTools.class) {
                tmp = driveUri;
                if (null == tmp) {
                    DispatcherPrefixService service = Services.optService(DispatcherPrefixService.class);
                    if (null == service) {
                        return new StringBuilder(DispatcherPrefixService.DEFAULT_PREFIX).append("drive").toString();
                    }
                    tmp = new StringBuilder(service.getPrefix()).append("drive").toString();
                    driveUri = tmp;
                }
            }
        }
        return tmp;
    }

    /**
     * Checks if given request is a drive request
     *
     * @param request The request to check
     * @return <code>true</code> in case of drive request; otherwise <code>false</code>
     */
    public static boolean isDriveRequest(HttpServletRequest request) {
        return driveUri().equals(request.getRequestURI());
    }

    /**
     * Checks if given request is a drive request
     *
     * @param request The request to check
     * @return <code>true</code> in case of drive request; otherwise <code>false</code>
     */
    public static boolean isDriveRequest(Request request) {
        return driveUri().equals(request.getRequestURI());
    }

    // --------------------------------------------------------------------------------------------------------------------------------------

    protected final static String EAS_URI_DEFAULT = "/Microsoft-Server-ActiveSync";

    protected static final String EAS_CMD = "Cmd";

    private static volatile String easUri;

    private static String easUri() {
        String tmp = easUri;
        if (null == tmp) {
            synchronized (RequestTools.class) {
                tmp = easUri;
                if (null == tmp) {
                    String defaultUri = EAS_URI_DEFAULT;
                    ConfigurationService service = Services.optService(ConfigurationService.class);
                    if (null == service) {
                        return defaultUri;
                    }
                    tmp = service.getProperty("com.openexchange.usm.eas.alias", defaultUri);
                    easUri = tmp;
                }
            }
        }
        return tmp;
    }

    /**
     * Checks if given request is an EAS request
     *
     * @param request The request to check
     * @return <code>true</code> in case of an EAS request; otherwise <code>false</code>
     */
    public static boolean isEasRequest(HttpServletRequest request) {
        return easUri().equals(request.getRequestURI());
    }

    /**
     * Returns if the given request is defined as to ignore
     *
     * @param request The request to check
     * @param ignoredEasCommands - the commands that should be ignored
     * @return <code>true</code> if given requests is configured to be ignored; otherwise <code>false</code>
     */
    public static boolean isIgnoredEasRequest(HttpServletRequest request, Set<String> ignoredEasCommands) {
        if (isEasRequest(request)) {
            String cmd = request.getParameter(EAS_CMD);
            if ((cmd != null) && (ignoredEasCommands.contains(cmd.toLowerCase()))) {
                return true;
            }

            /*-
             * Check for possibly EAS base64-encoded query string;
             * see http://download.microsoft.com/download/5/D/D/5DD33FDF-91F5-496D-9884-0A0B0EE698BB/[MS-ASHTTP].pdf
             *
             * Second byte reflects EAS command
             */
            byte[] bytes = getBase64Bytes(request.getQueryString());
            if (null != bytes && bytes.length > 2) {
                Set<EASCommandCodes> set = EASCommandCodes.get(ignoredEasCommands);
                byte code = bytes[1];
                for (EASCommandCodes command : set) {
                    if (command.getByte() == code) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static byte[] getBase64Bytes(final String queryString) {
        if (Strings.isEmpty(queryString)) {
            return null;
        }

        try {
            final byte[] encodedBytes = Charsets.toAsciiBytes(queryString);
            if (Base64.isBase64(encodedBytes)) {
                return Base64.decodeBase64(encodedBytes);
            }
        } catch (Exception e) {
            LOG.warn("Could not check for EAS base64-encoded query string", e);
        }

        return null;
    }

    // --------------------------------------------------------------------------------------------------------------------------------------

    protected final static String USM_URI_DEFAULT = "/usm-json";

    protected static final Cache<String, Boolean> USM_PATH_CACHE = CacheBuilder.newBuilder().maximumSize(20).expireAfterWrite(2, TimeUnit.HOURS).build();

    private static volatile String usmJsonUri;

    private static String usmJsonUri() {
        String tmp = usmJsonUri;
        if (null == tmp) {
            synchronized (RequestTools.class) {
                tmp = usmJsonUri;
                if (null == tmp) {
                    String defaultUri = USM_URI_DEFAULT;
                    ConfigurationService service = Services.optService(ConfigurationService.class);
                    if (null == service) {
                        return defaultUri;
                    }
                    tmp = service.getProperty("com.openexchange.usm.json.alias", defaultUri);
                    usmJsonUri = tmp;
                }
            }
        }
        return tmp;
    }

    protected static boolean isUsmRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith(usmJsonUri());
    }

    /**
     * Returns if the given request is defined as to ignore
     *
     * @param request The request to check
     * @param ignoredUsmCommands - the commands that should be ignored
     * @return <code>true</code> if given requests is configured to be ignored; otherwise <code>false</code>
     */
    public static boolean isIgnoredUsmRequest(HttpServletRequest request, Set<String> ignoredUsmCommands) {
        String pathInfo = request.getPathInfo();
        if ((pathInfo != null) && isUsmRequest(request)) {
            pathInfo = pathInfo.toLowerCase();

            Boolean result = USM_PATH_CACHE.getIfPresent(pathInfo);
            if (null != result) {
                return result.booleanValue();
            }

            boolean isIgnored = false;
            if (ignoredUsmCommands.contains(pathInfo)) {
                isIgnored = true;
            }
            USM_PATH_CACHE.put(pathInfo, Boolean.valueOf(isIgnored));
            return isIgnored;

        }
        return false;
    }

    // ------------------------------------------------------------------------------------------------------------------------- //

    /**
     * Checks if given request is either a USM-JSON or an EAS request
     *
     * @param request The request to check
     * @return <code>true</code> in case of either a USM-JSON or an EAS request; otherwise <code>false</code>
     */
    public static boolean isUsmJsonOrEasRequest(Request request) {
        String requestUri = request.getRequestURI();
        return null != requestUri && (usmJsonUri().equals(requestUri) || easUri().equals(requestUri));
    }

    // ------------------------------------------------------------------------------------------------------------------------- //

    /**
     * Checks if HTTP request is from the same host (localhost).
     *
     * @param req The servlet request
     * @return <code>true</code> if HTTP request is from the same host (localhost); otherwise <code>false</code>
     */
    public static boolean isLocalhost(final Request req) {
        return req.getLocalAddr().equals(req.getRemoteAddr());
    }

    /**
     * Checks for a local address
     *
     * @param domain The address to check
     * @return <code>true</code> if a local address; otherwise <code>false</code>
     */
    public static boolean isLocalAddress(String domain) {
        try {
            InetAddress address = InetAddress.getByName(domain);
            return address.isAnyLocalAddress() || address.isLoopbackAddress() || NetworkInterface.getByInetAddress(address) != null;
        } catch (UnknownHostException e) {
            // ignore
        } catch (SocketException e) {
            // ignore
        }
        return false;
    }

}
