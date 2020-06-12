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

package com.openexchange.file.storage.webdav.utils;

import static com.openexchange.java.Autoboxing.I;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.webdav.AbstractWebDAVFileStorageService;
import com.openexchange.file.storage.webdav.exception.WebdavExceptionCodes;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 * {@link WebDAVEndpointUtils}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.4
 */
public class WebDAVEndpointUtils {

    /**
     * Verifies provided url by doing multiple checks (configuration, scheme, loopback, ...)
     *
     * @param session The session used for configuration retrieval
     * @param service The service used for configuration retrieval
     * @param url The URL to check
     * @throws OXException
     */
    public static void verifyURI(Session session, AbstractWebDAVFileStorageService service, String url) throws OXException {
        if (Strings.isEmpty(url)) {
            throw FileStorageExceptionCodes.INVALID_URL.create("not provided", "empty");
        }
        try {
            URI uri = new URI(url);
            check(uri);
            boolean denied = isDenied(session, service, uri);

            if (denied || !isValid(uri)) {
                throw WebdavExceptionCodes.URL_NOT_ALLOWED.create(url);
            }
        } catch (URISyntaxException e) {
            throw WebdavExceptionCodes.BAD_URL.create(e, url);
        }
    }

    private static void check(URI uri) throws OXException {
        if (Strings.containsSurrogatePairs(uri.toString())) {
            throw WebdavExceptionCodes.BAD_URL.create(uri.toString());
        }
    }

    /**
     * Verifies that the given URL is allowed to be used by the session
     *
     * @param url The URL to verify
     * @throws OXException if the given URI is not allowed to be used
     */
    private static boolean isDenied(Session session, AbstractWebDAVFileStorageService service, URI url) throws OXException {
        if (isBlacklisted(session, service, url) || !verifyPort(session, service, url) || !verifyScheme(url)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the host specified in given URL is blacklisted for a session
     *
     * @param session The session to check
     * @param url The URL to check
     * @return <code>true</code>, if the uri's host is blacklisted, <code>false</code> otherwise
     * @throws OXException
     */
    private static boolean isBlacklisted(Session session, AbstractWebDAVFileStorageService service, URI url) throws OXException {
        if (url != null) {
            return isBlacklisted(session, service, url.getHost());
        }
        return false;
    }

    /**
     * Checks if the given host is blacklisted
     *
     * @param session The session to get the black listed hosts for
     * @param host The host to check
     * @return <code>true</code> , if the given host is blacklisted, <code>false</code> otherwise
     * @throws OXException
     */
    private static boolean isBlacklisted(Session session, AbstractWebDAVFileStorageService service, String host) throws OXException {
        if (service != null) {
            return service.getBlackListedHosts(session).contains(host);
        }
        return false;
    }

    /**
     * Verifies that the scheme (if set) is http or https
     *
     * @param url The URL to verify
     * @return <code>true</code> if the scheme is allowed or <code>null</code>, <code>false</code> otherwise
     * @throws OXException
     */
    private static boolean verifyScheme(URI url) {
        if (url != null) {
            String protocol = url.getScheme();
            if (Strings.isNotEmpty(protocol)) {
                if (protocol.equalsIgnoreCase("http") || protocol.equalsIgnoreCase("https")) {
                    return true;
                }
            } else {
                // no scheme provided. Will be set to 'https' later on.
                return true;
            }
        }
        return false;
    }

    /**
     * Verifies that the port of the given URI is allowed
     *
     * @param session The session
     * @param url The URL to verify
     * @return <code>true</code> if the URI's port is allowed <code>false</code> otherwise
     * @throws OXException
     */
    private static boolean verifyPort(Session session, AbstractWebDAVFileStorageService service, URI url) throws OXException {
        if (url != null) {
            return isAllowed(session, service, url.getPort());
        }
        return false;
    }

    /**
     * Checks if the given port is allowed
     *
     * @param session The session to check
     * @param port The port to check
     * @return <code>true</code> if the given port is allowed <code>false</code> otherwise
     * @throws OXException
     */
    private static boolean isAllowed(Session session, AbstractWebDAVFileStorageService service, int port) throws OXException {
        if (port < 0) {
            // port not set; always allow
            return true;
        }

        if (port > 65535) {
            // invalid port
            return false;
        }
        if (service != null) {
            Optional<Set<Integer>> optAllowedPorts = service.getAllowedPorts(session);
            return optAllowedPorts.isPresent() ? optAllowedPorts.get().contains(I(port)) : true;
        }
        return true;
    }

    private static boolean isValid(URI uri) {
        try {
            InetAddress inetAddress = InetAddress.getByName(uri.getHost());
            if (inetAddress.isAnyLocalAddress() || inetAddress.isSiteLocalAddress() || inetAddress.isLoopbackAddress() || inetAddress.isLinkLocalAddress()) {
                org.slf4j.LoggerFactory.getLogger(WebDAVEndpointUtils.class).debug("Given URL \"{}\" with destination IP {} appears not to be valid.", uri.toString(), inetAddress.getHostAddress());
                return false;
            }
        } catch (UnknownHostException e) {
            org.slf4j.LoggerFactory.getLogger(WebDAVEndpointUtils.class).debug("Given URL \"{}\" appears not to be valid.", uri.toString(), e);
            return false;
        }
        return true;
    }
}
