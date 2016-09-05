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

package com.openexchange.websockets.grizzly.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.glassfish.grizzly.http.Cookies;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.server.util.Globals;
import org.glassfish.grizzly.http.server.util.SimpleDateFormats;
import org.glassfish.grizzly.http.server.util.StringParser;
import org.glassfish.grizzly.http.util.FastHttpDateFormat;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.http.util.Parameters;
import org.glassfish.grizzly.servlet.CookieWrapper;
import org.slf4j.Logger;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.tools.servlet.DelegateServletInputStream;


/**
 * {@link GrizzlyWebSocketHttpServletRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class GrizzlyWebSocketHttpServletRequest implements HttpServletRequest {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(GrizzlyWebSocketHttpServletRequest.class);

    private final HttpRequestPacket requestPacket;
    private final Cookies cookies;
    private final Parameters parameters;

    /**
     * Initializes a new {@link GrizzlyWebSocketHttpServletRequest}.
     *
     * @param requestPacket The associated HTTP request packet
     * @param cookies The cookies
     * @param parameters The parsed parameters
     */
    public GrizzlyWebSocketHttpServletRequest(HttpRequestPacket requestPacket, Cookies cookies, Parameters parameters) {
        super();
        this.requestPacket = requestPacket;
        this.cookies = cookies;
        this.parameters = parameters;
    }

    @Override
    public Object getAttribute(String name) {
        return requestPacket.getAttribute(name);
    }

    @Override
    public Enumeration getAttributeNames() {
        final Iterator<String> attributeNames = requestPacket.getAttributeNames().iterator();
        return new Enumeration<String>() {

            @Override
            public boolean hasMoreElements() {
                return attributeNames.hasNext();
            }

            @Override
            public String nextElement() {
                return attributeNames.next();
            }
        };
    }

    @Override
    public String getCharacterEncoding() {
        return requestPacket.getCharacterEncoding();
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        requestPacket.setCharacterEncoding(env);
    }

    @Override
    public int getContentLength() {
        return (int) requestPacket.getContentLength();
    }

    @Override
    public String getContentType() {
        return requestPacket.getContentType();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new DelegateServletInputStream(Streams.EMPTY_INPUT_STREAM);
    }

    @Override
    public String getParameter(String name) {
        return parameters.getParameter(name);
    }

    @Override
    public Enumeration getParameterNames() {
        final Iterator<String> iterator = parameters.getParameterNames().iterator();
        return new Enumeration<String>() {

            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public String nextElement() {
                return iterator.next();
            }
        };
    }

    @Override
    public String[] getParameterValues(String name) {
        return parameters.getParameterValues(name);
    }

    @Override
    public Map getParameterMap() {
        Set<String> parameterNames = parameters.getParameterNames();
        Map<String, String[]> parameterMap = new LinkedHashMap<>(parameterNames.size());
        for (String name : parameterNames) {
            String[] values = getParameterValues(name);
            parameterMap.put(name, values);
        }
        return parameterMap;
    }

    @Override
    public String getProtocol() {
        return requestPacket.getProtocolString();
    }

    @Override
    public String getScheme() {
        return requestPacket.isSecure() ? "https" : "http";
    }

    @Override
    public String getServerName() {
        return requestPacket.serverName().toString();
    }

    @Override
    public int getServerPort() {
        return requestPacket.getServerPort();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), Charsets.UTF_8));
    }

    @Override
    public String getRemoteAddr() {
        return requestPacket.getRemoteAddress();
    }

    @Override
    public String getRemoteHost() {
        return requestPacket.getRemoteHost();
    }

    @Override
    public void setAttribute(String name, Object o) {
        requestPacket.setAttribute(name, o);
    }

    @Override
    public void removeAttribute(String name) {
        requestPacket.removeAttribute(name);
    }

    @Override
    public Locale getLocale() {
        Iterable<String> values = requestPacket.getHeaders().values(Header.AcceptLanguage);

        List<Locale> locales = new LinkedList<>();
        for (String value : values) {
            locales.addAll(parseLocalesHeader(value));
        }
        return locales.get(0);
    }

    @Override
    public Enumeration getLocales() {
        Iterable<String> values = requestPacket.getHeaders().values(Header.AcceptLanguage);

        List<Locale> locales = new LinkedList<>();
        for (String value : values) {
            locales.addAll(parseLocalesHeader(value));
        }
        final Iterator<Locale> iter = locales.iterator();
        return new Enumeration<Locale>() {

            @Override
            public boolean hasMoreElements() {
                return iter.hasNext();
            }

            @Override
            public Locale nextElement() {
                return iter.next();
            }
        };
    }

    @Override
    public boolean isSecure() {
        return requestPacket.isSecure();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public String getRealPath(String path) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return requestPacket.getRemotePort();
    }

    @Override
    public String getLocalName() {
        return requestPacket.getLocalName();
    }

    @Override
    public String getLocalAddr() {
        return requestPacket.getLocalAddress();
    }

    @Override
    public int getLocalPort() {
        return requestPacket.getLocalPort();
    }

    @Override
    public String getAuthType() {
        return requestPacket.authType().toString();
    }

    @Override
    public Cookie[] getCookies() {
        org.glassfish.grizzly.http.Cookie[] internalCookies = this.cookies.get();
        if (internalCookies == null) {
            return null;
        }

        List<javax.servlet.http.Cookie> cookieList = new ArrayList<javax.servlet.http.Cookie>();
        for (int i = 0; i < internalCookies.length; i++) {
            final org.glassfish.grizzly.http.Cookie cook = internalCookies[i];
            if (cook instanceof CookieWrapper) {
                cookieList.add(((CookieWrapper) cook).getWrappedCookie());
            } else {
                try {
                    javax.servlet.http.Cookie currentCookie = new javax.servlet.http.Cookie(cook.getName(), cook.getValue());
                    currentCookie.setComment(cook.getComment());
                    if (cook.getDomain() != null) {
                        currentCookie.setDomain(cook.getDomain());
                    }
                    currentCookie.setMaxAge(cook.getMaxAge());
                    currentCookie.setPath(cook.getPath());
                    currentCookie.setSecure(cook.isSecure());
                    currentCookie.setVersion(cook.getVersion());
                    cookieList.add(currentCookie);
                } catch (IllegalArgumentException iae) {
                    LOG.warn("Failed to create cookie {}", cook.getName(), iae);
                }
            }
        }
        return cookieList.toArray(new javax.servlet.http.Cookie[cookieList.size()]);
    }

    @Override
    public long getDateHeader(String name) {
        String value = getHeader(name);
        if (value == null) {
            return (-1L);
        }

        final SimpleDateFormats formats = SimpleDateFormats.create();
        try {
            // Attempt to convert the date header in a variety of formats
            long result = FastHttpDateFormat.parseDate(value, formats.getFormats());
            if (result != (-1L)) {
                return result;
            }
            throw new IllegalArgumentException(value);
        } finally {
            formats.recycle();
        }
    }

    @Override
    public String getHeader(String name) {
        return requestPacket.getHeader(name);
    }

    @Override
    public Enumeration getHeaders(String name) {
        final Iterator<String> iter = requestPacket.getHeaders().values(name).iterator();
        return new Enumeration<String>() {

            @Override
            public boolean hasMoreElements() {
                return iter.hasNext();
            }

            @Override
            public String nextElement() {
                return iter.next();
            }
        };
    }

    @Override
    public Enumeration getHeaderNames() {
        final Iterator<String> iter = requestPacket.getHeaders().names().iterator();
        return new Enumeration<String>() {

            @Override
            public boolean hasMoreElements() {
                return iter.hasNext();
            }

            @Override
            public String nextElement() {
                return iter.next();
            }
        };
    }

    @Override
    public int getIntHeader(String name) {
        String value = getHeader(name);
        return value == null ? -1 : Integer.parseInt(value);
    }

    @Override
    public String getMethod() {
        return requestPacket.getMethod().getMethodString();
    }

    @Override
    public String getPathInfo() {
        return null;
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getContextPath() {
        return "";
    }

    @Override
    public String getQueryString() {
        return requestPacket.getQueryString();
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public String getRequestURI() {
        return requestPacket.getRequestURI();
    }

    @Override
    public StringBuffer getRequestURL() {
        StringBuffer buffer = new StringBuffer(32);
        String scheme = getScheme();
        int port = getServerPort();
        if (port < 0) {
            port = 80; // Work around java.net.URL bug
        }

        buffer.append(scheme);
        buffer.append("://");
        buffer.append(getServerName());
        if ((scheme.equals("http") && (port != 80)) || (scheme.equals("https") && (port != 443))) {
            buffer.append(':');
            buffer.append(port);
        }
        buffer.append(getRequestURI());
        return buffer;
    }

    @Override
    public String getServletPath() {
        return "";
    }

    @Override
    public HttpSession getSession(boolean create) {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        org.glassfish.grizzly.http.Cookie cookie = this.cookies.findByName(Globals.SESSION_COOKIE_NAME);
        return null != cookie;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    // ----------------------------------------------------------------------------------------

    /**
     * Configures the given JSESSIONID cookie.
     *
     * @param cookie The JSESSIONID cookie to be configured
     */
    protected void configureSessionCookie(final Cookie cookie) {
        cookie.setMaxAge(-1);
        cookie.setPath("/");

        if (isSecure()) {
            cookie.setSecure(true);
        }
    }

    /**
     * Parse accept-language header value.
     */
    protected List<Locale> parseLocalesHeader(String value) {

        // Store the accumulated languages that have been requested in
        // a local collection, sorted by the quality value (so we can
        // add Locales in descending order).  The values will be ArrayLists
        // containing the corresponding Locales to be added
        TreeMap<Double,List<Locale>> localLocalesMap = new TreeMap<Double,List<Locale>>();

        // Preprocess the value to remove all whitespace
        int white = value.indexOf(' ');
        if (white < 0) {
            white = value.indexOf('\t');
        }
        if (white >= 0) {
            StringBuilder sb = new StringBuilder();
            int len = value.length();
            for (int i = 0; i < len; i++) {
                char ch = value.charAt(i);
                if ((ch != ' ') && (ch != '\t')) {
                    sb.append(ch);
                }
            }
            value = sb.toString();
        }

        // Process each comma-delimited language specification
        StringParser parser = new StringParser();
        parser.setString(value);        // ASSERT: parser is available to us
        int length = parser.getLength();
        while (true) {

            // Extract the next comma-delimited entry
            int start = parser.getIndex();
            if (start >= length) {
                break;
            }
            int end = parser.findChar(',');
            String entry = parser.extract(start, end).trim();
            parser.advance();   // For the following entry

            // Extract the quality factor for this entry
            double quality = 1.0;
            int semi = entry.indexOf(";q=");
            if (semi >= 0) {
                final String qvalue = entry.substring(semi + 3);
                // qvalues, according to the RFC, may not contain more
                // than three values after the decimal.
                if (qvalue.length() <= 5) {
                    try {
                        quality = Double.parseDouble(qvalue);
                    } catch (NumberFormatException e) {
                        quality = 0.0;
                    }
                } else {
                    quality = 0.0;
                }
                entry = entry.substring(0, semi);
            }

            // Skip entries we are not going to keep track of
            if (quality < 0.00005)
             {
                continue;       // Zero (or effectively zero) quality factors
            }
            if ("*".equals(entry))
             {
                continue;       // FIXME - "*" entries are not handled
            }

            // Extract the language and country for this entry
            String language;
            String country;
            String variant;
            int dash = entry.indexOf('-');
            if (dash < 0) {
                language = entry;
                country = "";
                variant = "";
            } else {
                language = entry.substring(0, dash);
                country = entry.substring(dash + 1);
                int vDash = country.indexOf('-');
                if (vDash > 0) {
                    String cTemp = country.substring(0, vDash);
                    variant = country.substring(vDash + 1);
                    country = cTemp;
                } else {
                    variant = "";
                }
            }

            if (!isAlpha(language) || !isAlpha(country) || !isAlpha(variant)) {
                 continue;
            }


            // Add a new Locale to the list of Locales for this quality level
            Locale locale = new Locale(language, country, variant);
            Double key = -quality;  // Reverse the order
            List<Locale> values = localLocalesMap.get(key);
            if (values == null) {
                values = new ArrayList<Locale>();
                localLocalesMap.put(key, values);
            }
            values.add(locale);

        }

        // Process the quality values in highest->lowest order (due to
        // negating the Double value when creating the key)
        List<Locale> locales = new ArrayList<Locale>(localLocalesMap.size());
        for (List<Locale> localLocales: localLocalesMap.values()) {
            for (Locale locale : localLocales) {
                locales.add(locale);
            }
        }
        return locales;
    }

    /*
     * @return <code>true</code> if the given string is composed of
     *  upper- or lowercase letters only, <code>false</code> otherwise.
     */
    static boolean isAlpha(String value) {

        if (value == null) {
            return false;
        }

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) {
                return false;
            }
        }

        return true;
    }

}
