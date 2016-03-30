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
import java.util.HashMap;
import java.util.Map;
import org.glassfish.grizzly.http.Cookie;
import org.glassfish.grizzly.http.Cookies;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.util.CookieParserUtils;
import org.glassfish.grizzly.http.util.CookieSerializerUtils;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.http.util.MimeHeaders;
import org.glassfish.grizzly.memory.ByteBufferWrapper;


/**
 * {@link ClientCookieInspector} to inspect and fix client cookies.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class ClientCookieInspector extends AbstractCookieInspector {

    /**
     * Initializes a new {@link ClientCookieInspector}.
     * @param httpRequestPacket the incoming request packet, must not be null
     * @param backendRoute the currently configured backendRoute, must not be null
     * @throws IllegalArgumentException for null parameters
     */
    public ClientCookieInspector(HttpRequestPacket httpRequestPacket, String backendRoute) {
        if( httpRequestPacket == null || backendRoute == null) {
            throw new IllegalArgumentException();
        }
        MimeHeaders requestMimeHeaders = httpRequestPacket.getHeaders();
        String cookieHeader = requestMimeHeaders.getHeader(Header.Cookie);
        this.cookieMap = getCookieMapFromHeaderLine(cookieHeader == null ? "" : cookieHeader);
        this.backendRoute = backendRoute;
    }

    /**
     * Convert the client header line into a map<name, cookie> of cookies.
     * @param headerLine the header line from the client http request
     * @throws IllegalArgumentException for null parameters
     */
    private Map<String, Cookie> getCookieMapFromHeaderLine(String headerLine) {
        if(headerLine == null) {
            throw new IllegalArgumentException();
        }
        HashMap<String, Cookie> cookieMap = new HashMap<String, Cookie>();
        Cookies cookies = new Cookies();
        CookieParserUtils.parseClientCookies(cookies, headerLine, true);
      for (Cookie cookie : cookies.get()) {
            cookieMap.put(cookie.getName(), cookie);
        }
        return cookieMap;
    }

    /**
     * Construct a http <code>Cookie:</code> header line from the cookies currently stored in the inspector.
     * @return a Cookie: header line
     */
    public ByteBufferWrapper getCookieHeaderLine() {
        StringBuilder sb = new StringBuilder();
        CookieSerializerUtils.serializeClientCookies(sb, false, cookieMap.values().toArray(new Cookie[0]));
        return new ByteBufferWrapper(ByteBuffer.wrap(sb.toString().getBytes()));
    }

}
