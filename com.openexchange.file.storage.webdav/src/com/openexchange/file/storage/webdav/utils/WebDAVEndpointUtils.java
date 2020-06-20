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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.validator.routines.UrlValidator;
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

    private static final String[] ALLOWED_PROTOCOLS = new String[] { "http", "https" };

    /**
     * Verifies provided url by doing multiple checks (configuration, protocol, ...)
     *
     * @param session The session used for configuration retrieval
     * @param service The service used for configuration retrieval
     * @param urlString The URL to check
     * @throws OXException
     */
    public static void verifyURL(Session session, AbstractWebDAVFileStorageService service, String urlString) throws OXException {
        if (Strings.isEmpty(urlString)) {
            throw FileStorageExceptionCodes.INVALID_URL.create("not provided", "empty");
        }
        try {
            check(urlString);
            URL url = new URL(urlString);
            if (isDenied(session, service, url)) {
                throw WebdavExceptionCodes.URL_NOT_ALLOWED.create(urlString);
            }
        } catch (MalformedURLException e) {
            throw WebdavExceptionCodes.BAD_URL.create(e, urlString);
        }
    }

    private static void check(String url) throws OXException {
        UrlValidator validator = new UrlValidator(ALLOWED_PROTOCOLS, UrlValidator.ALLOW_LOCAL_URLS);
        if (!validator.isValid(url) || Strings.containsSurrogatePairs(url)) {
            throw WebdavExceptionCodes.BAD_URL.create(url);
        }
    }

    /**
     * Verifies that the given URL is allowed to be used by the session
     *
     * @param url The URL to verify
     * @throws OXException if the given URL is not allowed to be used
     */
    private static boolean isDenied(Session session, AbstractWebDAVFileStorageService service, URL url) throws OXException {
        if (isBlacklisted(session, service, url) || !verifyPort(session, service, url)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the host specified in given URL is blacklisted for a session
     *
     * @param session The session to check
     * @param url The URL to check
     * @return <code>true</code>, if the url's host is blacklisted, <code>false</code> otherwise
     * @throws OXException
     */
    private static boolean isBlacklisted(Session session, AbstractWebDAVFileStorageService service, URL url) throws OXException {
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
     * Verifies that the port of the given URL is allowed
     *
     * @param session The session
     * @param url The URL to verify
     * @return <code>true</code> if the URL's port is allowed <code>false</code> otherwise
     * @throws OXException
     */
    private static boolean verifyPort(Session session, AbstractWebDAVFileStorageService service, URL url) throws OXException {
        if (url == null) {
            return false;
        }
        int port = url.getPort();
        if (port < 0) {
            String protocol = Strings.asciiLowerCase(url.getProtocol());
            if (Strings.isEmpty(protocol)) {
                // Assume HTTP as default
                port = 80;
            } else {
                protocol = protocol.trim();
                if ("http".equals(protocol)) {
                    port = 80;
                } else if ("https".equals(protocol)) {
                    port = 443;
                }  else {
                    port = 80;
                }
            }
        }
        return isAllowed(session, service, port);
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

}
