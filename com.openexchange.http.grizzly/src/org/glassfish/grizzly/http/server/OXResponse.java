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
 *    trademarks of the OX Software GmbH. group of companies.
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

package org.glassfish.grizzly.http.server;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Locale;
import org.glassfish.grizzly.http.Cookie;
import org.glassfish.grizzly.http.util.CharChunk;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.http.util.HttpRequestURIDecoder;
import org.glassfish.grizzly.http.util.HttpStatus;
import com.openexchange.http.grizzly.GrizzlyConfig;

/**
 * {@link OXResponse} - Open-Xchange specific additions to the Grizzly Response like
 * altered cookie handling and absolute/relative redirects respecting forced https.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class OXResponse extends Response {

    /** The Grizzly configuration */
    private final GrizzlyConfig grizzlyConfig = GrizzlyConfig.getInstance();

    /**
     * Initializes a new {@link OXResponse}.
     */
    public OXResponse() {
        super();
    }

    @Override
    public void addCookie(Cookie cookie) {
        // Prevent unwanted access to cookies by only allowing access via HTTP methods
        cookie.setHttpOnly(GrizzlyConfig.getInstance().isCookieHttpOnly());

        super.addCookie(cookie);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        if (isCommitted()) {
            throw new IllegalStateException("Illegal attempt to redirect the response as the response has been committed.");
        }

        // Clear any data content that has been buffered
        resetBuffer();

        // Generate a temporary redirect to the specified location
        try {
            String redirectLocation = location;
            if (grizzlyConfig.isAbsoluteRedirect()) {
                redirectLocation = toAbsolute(location, true);
            }

            // END RIMOD 4642650
            setStatus(HttpStatus.FOUND_302);
            setHeader(Header.Location, redirectLocation);

            // According to RFC2616 section 10.3.3 302 Found,
            // the response SHOULD contain a short hypertext note with
            // a hyperlink to the new URI.
            setContentType("text/html");
            setLocale(Locale.getDefault());

            String filteredMsg = filter(redirectLocation);
            StringBuilder sb = new StringBuilder(150 + redirectLocation.length());

            sb.append("<html>\r\n");
            sb.append("<head><title>Document moved</title></head>\r\n");
            sb.append("<body><h1>Document moved</h1>\r\n");
            sb.append("This document has moved <a href=\"");
            sb.append(filteredMsg);
            sb.append("\">here</a>.<p>\r\n");
            sb.append("</body>\r\n");
            sb.append("</html>\r\n");

            try {
                getWriter().write(sb.toString());
                getWriter().flush();
            } catch (IllegalStateException ise1) {
                try {
                   getOutputStream().write(sb.toString().getBytes(
                           org.glassfish.grizzly.http.util.Constants.DEFAULT_HTTP_CHARSET));
                } catch (IllegalStateException ise2) {
                   // ignore; the RFC says "SHOULD" so it is acceptable
                   // to omit the body in case of an error
                }
            }
        } catch (IllegalArgumentException e) {
            sendError(404);
        }

        finish();
    }

    @Override
    protected String toAbsolute(String location, boolean normalize) {
        if (location == null) {
            return location;
        }

        final boolean leadingSlash = location.startsWith("/");

        if (leadingSlash || (!location.contains("://"))) {

            String scheme = request.getScheme();

            String name = request.getServerName();
            int port = request.getServerPort();
            if (grizzlyConfig.isForceHttps()) {
                scheme = "https";
                port = grizzlyConfig.getHttpsProtoPort();
            }

            redirectURLCC.recycle();
            final CharChunk cc = redirectURLCC;

            try {
                cc.append(scheme, 0, scheme.length());
                cc.append("://", 0, 3);
                cc.append(name, 0, name.length());
                if ((scheme.equals("http") && port != 80)
                        || (scheme.equals("https") && port != 443)) {
                    cc.append(':');
                    String portS = port + "";
                    cc.append(portS, 0, portS.length());
                }
                if (!leadingSlash) {
                    String relativePath = request.getDecodedRequestURI();
                    final int pos = relativePath.lastIndexOf('/');
                    relativePath = relativePath.substring(0, pos);

                    final String encodedURI;
                    if (System.getSecurityManager() != null) {
                        try {
                            final String frelativePath = relativePath;
                            encodedURI = AccessController.doPrivileged(
                                    new PrivilegedExceptionAction<String>() {
                                        @Override
                                        public String run() throws IOException {
                                            return urlEncoder.encodeURL(frelativePath);
                                        }
                                    });
                        } catch (PrivilegedActionException pae) {
                            throw new IllegalArgumentException(location, pae.getCause());
                        }
                    } else {
                        encodedURI = urlEncoder.encodeURL(relativePath);
                    }

                    cc.append(encodedURI, 0, encodedURI.length());
                    cc.append('/');
                }
                cc.append(location, 0, location.length());
            } catch (IOException e) {
                throw new IllegalArgumentException(location, e);
            }

            if (normalize){
                HttpRequestURIDecoder.normalizeChars(cc);
            }

            return cc.toString();

        } else {
            return location;
        }
    }

}
