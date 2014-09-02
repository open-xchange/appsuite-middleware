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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.http.grizzly.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.glassfish.grizzly.http.server.Request;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
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

    // -------------------------------------------------------------------------------------------------------------------------------------- //

    private static volatile String driveUri;
    private static String driveUri() {
        String tmp = driveUri;
        if (null == tmp) {
            synchronized (RequestTools.class) {
                tmp = driveUri;
                if (null == tmp) {
                    String defaultPrefix = DispatcherPrefixService.DEFAULT_PREFIX;
                    DispatcherPrefixService service = Services.optService(DispatcherPrefixService.class);
                    if (null == service) {
                        return defaultPrefix;
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

    // -------------------------------------------------------------------------------------------------------------------------------------- //

    private final static String EAS_URI_DEFAULT = "/Microsoft-Server-ActiveSync";
    private final static String USM_URI_DEFAULT = "/usm-json";

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
     * Checks if given request is an EAS request
     *
     * @param request The request to check
     * @return <code>true</code> in case of an EAS request; otherwise <code>false</code>
     */
    public static boolean isEasRequest(Request request) {
        return easUri().equals(request.getRequestURI());
    }

    /**
     * Checks if given request is a USM-JSON request
     *
     * @param request The request to check
     * @return <code>true</code> in case of a USM-JSON request; otherwise <code>false</code>
     */
    public static boolean isUsmJsonRequest(HttpServletRequest request) {
        return usmJsonUri().equals(request.getRequestURI());
    }

    /**
     * Checks if given request is a USM-JSON request
     *
     * @param request The request to check
     * @return <code>true</code> in case of a USM-JSON request; otherwise <code>false</code>
     */
    public static boolean isUsmJsonRequest(Request request) {
        return usmJsonUri().equals(request.getRequestURI());
    }

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

    private static final String EAS_CMD = "Cmd";
    private static final String EAS_PING = "Ping";
    private static final int EAS_COMMAND_CODE_PING = 18;

    /**
     * Checks if given requests signals to be an EAS Ping request
     *
     * @param request The request to check
     * @return <code>true</code> if given requests signals to be an EAS Ping request; otherwise <code>false</code>
     */
    public static boolean isEasPingRequest(final HttpServletRequest request) {
        if (easUri().equals(request.getRequestURI())) {
            if (EAS_PING.equals(request.getParameter(EAS_CMD))) {
                return true;
            }

            /*-
             * Check for possibly EAS base64-encoded query string
             *
             * Second byte reflects EAS command
             */
            final byte[] bytes = getBase64Bytes(request.getQueryString());
            if (null != bytes && bytes.length > 2 && EAS_COMMAND_CODE_PING == bytes[1]) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if given requests signals to be an EAS Ping request
     *
     * @param request The request to check
     * @return <code>true</code> if given requests signals to be an EAS Ping request; otherwise <code>false</code>
     */
    public static boolean isEasPingRequest(final Request request) {
        if (easUri().equals(request.getRequestURI())) {
            if (EAS_PING.equals(request.getParameter(EAS_CMD))) {
                return true;
            }

            /*-
             * Check for possibly EAS base64-encoded query string
             *
             * Second byte reflects EAS command
             */
            final byte[] bytes = getBase64Bytes(request.getQueryString());
            if (null != bytes && bytes.length > 2 && EAS_COMMAND_CODE_PING == bytes[1]) {
                return true;
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
        } catch (final Exception e) {
            LOG.warn("Could not check for EAS base64-encoded query string", e);
        }

        return null;
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
