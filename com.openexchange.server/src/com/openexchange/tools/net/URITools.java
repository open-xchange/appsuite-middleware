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

package com.openexchange.tools.net;

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import com.google.common.collect.ImmutableSet;
import com.openexchange.exception.OXException;

/**
 * {@link URITools}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class URITools {

    private URITools() {
        super();
    }

    public static final URI changeHost(final URI uri, final String newHost) throws URISyntaxException {
        return new URI(uri.getScheme(), uri.getUserInfo(), newHost, uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
    }

    public static final URI generateURI(final String protocol, final String host, final int port) throws URISyntaxException {
        return new URI(protocol, null, host, port, null, null, null);
    }

    public static final String getHost(final URI uri) {
        String retval = uri.getHost();
        if (null == retval || retval.length() == 0) {
            return retval;
        }
        if (retval.indexOf(':') > 0 && (retval.length() > 0 && retval.charAt(0) == '[') && retval.endsWith("]")) {
            retval = retval.substring(1, retval.length() -1);
        }
        return retval;
    }

    private static final Set<Integer> REDIRECT_RESPONSE_CODES = ImmutableSet.of(I(HttpURLConnection.HTTP_MOVED_PERM), I(HttpURLConnection.HTTP_MOVED_TEMP), I(HttpURLConnection.HTTP_SEE_OTHER), I(HttpURLConnection.HTTP_USE_PROXY));

    private static final String LOCATION_HEADER = "Location";

    /**
     * Returns the final URL which might be different due to HTTP(S) redirects.
     *
     * @param url The URL to resolve
     * @param validator An optional validation of the any of the redirect hops, which returns an optional OXException if validation fails
     * @return The final URL
     * @throws OXException If an OPen-Xchange error occurs
     * @throws IOException If an I/O error occurs
     */
    public static String getFinalURL(String url, Optional<Function<URL, Optional<OXException>>> validator) throws IOException, OXException {
        URL u = new URL(url);
        if (validator.isPresent()) {
            Optional<OXException> exception = validator.get().apply(u);
            if (exception.isPresent()) {
                throw exception.get();
            }
        }

        URLConnection urlConnnection = u.openConnection();
        urlConnnection.setConnectTimeout(2500);
        urlConnnection.setReadTimeout(2500);

        if (urlConnnection instanceof HttpURLConnection) {
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnnection;
            httpURLConnection.setInstanceFollowRedirects(false);
            httpURLConnection.connect();

            if (REDIRECT_RESPONSE_CODES.contains(I(httpURLConnection.getResponseCode()))) {
                String redirectUrl = httpURLConnection.getHeaderField(LOCATION_HEADER);
                httpURLConnection.disconnect();
                return getFinalURL(redirectUrl, validator);
            }
            httpURLConnection.disconnect();
        }

        return url;
    }

    /**
     * Returns an URL connection for the final URL, depending on HTTP redirects.
     *
     * @param url The URL to connect to
     * @param validator An optional validation of the any of the redirect hops, which returns an optional OXException if validation fails
     * @param decorator An optional decorator for the probing URL connection instance
     * @return The terminal <b>connected</b> URL connection
     * @throws IOException If an I/O error occurs
     * @throws OXException If an OPen-Xchange error occurs
     */
    public static URLConnection getTerminalConnection(String url, Optional<Function<URL, Optional<OXException>>> validator, Optional<Function<URLConnection, Optional<OXException>>> decorator) throws IOException, OXException {
        // Initialize URL instance
        URL u = new URL(url);

        // Validate
        if (validator.isPresent()) {
            Optional<OXException> exception = validator.get().apply(u);
            if (exception.isPresent()) {
                throw exception.get();
            }
        }

        // Get connection
        URLConnection urlConnnection = u.openConnection();

        // Decorate
        if (decorator.isPresent()) {
            Optional<OXException> exception = decorator.get().apply(urlConnnection);
            if (exception.isPresent()) {
                throw exception.get();
            }
        } else {
            urlConnnection.setConnectTimeout(2500);
            urlConnnection.setReadTimeout(2500);
        }

        // Connect
        if (urlConnnection instanceof HttpURLConnection) {
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnnection;
            httpURLConnection.setInstanceFollowRedirects(false);
            httpURLConnection.connect();

            if (REDIRECT_RESPONSE_CODES.contains(I(httpURLConnection.getResponseCode()))) {
                String redirectUrl = httpURLConnection.getHeaderField(LOCATION_HEADER);
                httpURLConnection.disconnect();
                return getTerminalConnection(redirectUrl, validator, decorator);
            }
        } else {
            urlConnnection.connect();
        }
        return urlConnnection;
    }
}
