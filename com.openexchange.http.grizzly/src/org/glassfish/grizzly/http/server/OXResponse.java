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
    private final GrizzlyConfig grizzlyConfig;

    /**
     * Initializes a new {@link OXResponse}.
     */
    public OXResponse(GrizzlyConfig grizzlyConfig) {
        super();
        this.grizzlyConfig = grizzlyConfig;
    }

    @Override
    public void addCookie(Cookie cookie) {
        // Prevent unwanted access to cookies by only allowing access via HTTP methods
        cookie.setHttpOnly(grizzlyConfig.isCookieHttpOnly());

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

                    //unable to encode the request URI
                    if (encodedURI == null) {
                        return null;
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
