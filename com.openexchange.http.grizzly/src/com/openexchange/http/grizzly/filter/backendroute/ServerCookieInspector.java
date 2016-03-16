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

package com.openexchange.http.grizzly.filter.backendroute;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.glassfish.grizzly.http.Cookie;
import org.glassfish.grizzly.http.Cookies;
import org.glassfish.grizzly.http.HttpResponsePacket;
import org.glassfish.grizzly.http.util.CookieParserUtils;
import org.glassfish.grizzly.http.util.CookieSerializerUtils;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.http.util.MimeHeaders;
import org.glassfish.grizzly.memory.ByteBufferWrapper;
import com.openexchange.config.ConfigurationService;
import com.openexchange.http.grizzly.osgi.Services;


/**
 * {@link ServerCookieInspector}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class ServerCookieInspector extends AbstractCookieInspector {

    private final HttpResponsePacket httpResponsePacket;

    /**
     * Initializes a new {@link ServerCookieInspector}.
     * @param headerLine the server Set-Cookie: header, must not be null
     * @param backendRoute the currently configured backendRoute, must not be null
     * @throws IllegalArgumentException for null Parameters
     */
    public ServerCookieInspector(HttpResponsePacket httpResponsePacket, String backendRoute) {
        if(httpResponsePacket == null || backendRoute == null) {
            throw new IllegalArgumentException();
        }
        this.backendRoute=backendRoute;
        this.httpResponsePacket = httpResponsePacket;

        MimeHeaders responseMimeHeaders = httpResponsePacket.getHeaders();
        Iterable<String> headerLines = responseMimeHeaders.values(Header.SetCookie);
        this.cookieMap = getCookieMapFromHeaderLines(headerLines);
    }

    /**
     * Convert the server Set-Cookie: header line into a map<name, cookie> of cookies.
     * @param headerLine the Set-Cookie: header line from the client http request
     * @return
     */
    private Map<String, Cookie> getCookieMapFromHeaderLines(Iterable<String> headerLines) {
        Map<String, Cookie> cookieMap = new HashMap<String, Cookie>();
        Cookies cookies = new Cookies();
        for (String headerLine : headerLines) {
            CookieParserUtils.parseServerCookies(cookies, headerLine, true);
        }
        for (Cookie cookie : cookies.get()) {
            cookieMap.put(cookie.getName(), cookie);
        }
        return cookieMap;
    }

    /**
     * Set the value of the JSESSIONID cookie to a new fixed value.
     * The value was fixed outside of this ServerCookieInspector e.g. in the ClientCookieInspector.
     * If no existing JSESSIONID cookie can be found in the Set-Cookie: header a new one is created.
     * @param fixedJessionId the fixed value of the JSessionId
     */
    public void setJSessionIDCookieValue(String fixedJessionId) {
        Cookie jSessionIdCookie = cookieMap.get("JSESSIONID");
        if (jSessionIdCookie == null) {
            jSessionIdCookie = new Cookie("JSESSIONID", fixedJessionId);
            configureSessionCookie(jSessionIdCookie);
            cookieMap.put("JSESSIONID", jSessionIdCookie);
        }
    }

    /**
     * Configure the JSESSIONID cookie
     * @param cookie the cookie to configure
     */
    private void configureSessionCookie(final Cookie cookie) {
        cookie.setMaxAge(-1);
        cookie.setPath("/");
        if (isSecure()) {
            cookie.setSecure(true);
        }
    }

    /**
     * Check if either the request is over a secure connection or the server enforces https.
     * See server.properties for more details.
     * @return true if either the request is over a secure connection or the server enforces https.
     */
    private boolean isSecure() {
        final ConfigurationService configService = Services.optService(ConfigurationService.class);
        final boolean forceHttps = null == configService || configService.getBoolProperty("com.openexchange.forceHTTPS", true);
        if (forceHttps && !com.openexchange.tools.servlet.http.Cookies.isLocalLan(httpResponsePacket.getRequest().getRemoteHost())) {
            // Speak HTTPS with all non-local LAN endpoints
            return true;
        }
        return httpResponsePacket.getRequest().isSecure();
    }

    /**
     * Get a list of ByteBufferWrappers containing the single Set-Cookie header values.
     * @return a list of ByteBufferWrappers containing the single header values
     */
    public List<ByteBufferWrapper> getSetCookieHeaders() {
        List<ByteBufferWrapper> headers = new ArrayList<ByteBufferWrapper>();
        for (Cookie cookie : cookieMap.values()) {
            StringBuilder sb = new StringBuilder();
            CookieSerializerUtils.serializeServerCookie(sb, cookie);
            headers.add(new ByteBufferWrapper(ByteBuffer.wrap(sb.toString().getBytes())));
        }
        return headers;
    }
    /**
     * Get the Set-Cookie header to set the JSESSIONID.
     * @return a ByteBufferWrapper containing the Set-Cookie header to set the JSESSIONID.
     */
    public ByteBufferWrapper getSetJSessionIdCookieHeader() {
        Cookie jSessionIDCookie = cookieMap.get("JSESSIONID");
        StringBuilder sb = new StringBuilder();
        CookieSerializerUtils.serializeServerCookie(sb, jSessionIDCookie);
        return new ByteBufferWrapper(ByteBuffer.wrap(sb.toString().getBytes()));
    }

}
