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

package com.openexchange.tools.servlet.http;

import static com.openexchange.net.IPAddressUtil.textToNumericFormatV4;
import static com.openexchange.net.IPAddressUtil.textToNumericFormatV6;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import com.google.common.net.InternetDomainName;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.config.ConfigurationService;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link Cookies} - Utility class for {@link Cookie}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Cookies {

    /**
     * Initializes a new {@link Cookies}.
     */
    private Cookies() {
        super();
    }

    private static final Set<String> LOCALS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("localhost", "127.0.0.1", "::1")));

    /**
     * Checks if specified request's server name is considered as part of local LAN.
     *
     * @param request The request
     * @return <code>true</code> if considered as part of local LAN; otherwise <code>false</code>
     */
    public static boolean isLocalLan(final HttpServletRequest request) {
        return isLocalLan(request.getServerName());
    }

    /**
     * Checks if specified server name is considered as part of local LAN.
     *
     * @param serverName The server name
     * @return <code>true</code> if considered as part of local LAN; otherwise <code>false</code>
     */
    public static boolean isLocalLan(final String serverName) {
        if (com.openexchange.java.Strings.isEmpty(serverName)) {
            return false;
        }
        return LOCALS.contains(serverName.toLowerCase(Locale.US));
    }

    private static volatile Boolean domainEnabled;

    /**
     * Checks whether domain parameter is enabled
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public static boolean domainEnabled() {
        Boolean tmp = domainEnabled;
        if (null == tmp) {
            synchronized (LoginServlet.class) {
                tmp = domainEnabled;
                if (null == tmp) {
                    final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    tmp = Boolean.valueOf(null != service && service.getBoolProperty("com.openexchange.cookie.domain.enabled", false));
                    domainEnabled = tmp;
                }
            }
        }
        return tmp.booleanValue();
    }

    private static volatile Boolean prefixWithDot;

    /**
     * Checks whether domain parameter should start with a dot (<code>'.'</code>) character
     *
     * @return <code>true</code> for starting dot; otherwise <code>false</code>
     */
    public static boolean prefixWithDot() {
        Boolean tmp = prefixWithDot;
        if (null == tmp) {
            synchronized (LoginServlet.class) {
                tmp = prefixWithDot;
                if (null == tmp) {
                    final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    tmp = Boolean.valueOf(null == service || service.getBoolProperty("com.openexchange.cookie.domain.prefixWithDot", true));
                    prefixWithDot = tmp;
                }
            }
        }
        return tmp.booleanValue();
    }

    private static volatile String configuredDomain;

    /**
     * Gets the configured domain or <code>null</code>
     *
     * @return The configured domain or <code>null</code>
     */
    public static String configuredDomain() {
        String tmp = configuredDomain;
        if (null == tmp) {
            synchronized (LoginServlet.class) {
                tmp = configuredDomain;
                if (null == tmp) {
                    final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    tmp = null == service ? "null" : service.getProperty("com.openexchange.cookie.domain", "null");
                    configuredDomain = tmp;
                }
            }
        }
        return "null".equalsIgnoreCase(tmp) ? null : tmp;
    }

    /**
     * Gets the domain parameter for specified server name with configured default behavior whether to prefix domain with a dot (
     * <code>'.'</code>) character.
     *
     * @param serverName The server name
     * @return The domain parameter or <code>null</code>
     * @see #prefixWithDot()
     * @see #configuredDomain()
     */
    public static String getDomainValue(final String serverName) {
        if (!domainEnabled()) {
            return null;
        }
        final String configuredDomain = configuredDomain();
        if (null != configuredDomain) {
            return configuredDomain;
        }
        return getDomainValue(serverName, prefixWithDot(), null, true);
    }

    /**
     * Gets the domain parameter for specified server name.
     *
     * @param serverName The server name
     * @param prefixWithDot Whether to prefix domain with a dot (<code>'.'</code>) character
     * @param configuredDomain The pre-configured domain name for this host
     * @param domainEnabled Whether to write a domain parameter at all (<code>null</code> is immediately returned)
     * @return The domain parameter or <code>null</code>
     */
    public static String getDomainValue(final String serverName, final boolean prefixWithDot, final String configuredDomain, final boolean domainEnabled) {
        if (!domainEnabled) {
            return null;
        }
        if (null != configuredDomain) {
            return configuredDomain;
        }
        // Try by best-guessed attempt
        if (null == serverName) {
            return null;
        }
        if (prefixWithDot) { // Follow RFC 2109 syntax for domain name
            if (serverName.startsWith("www.")) {
                return serverName.substring(3);
            } else if ("localhost".equalsIgnoreCase(serverName)) {
                return null;
            } else {
                if (null == textToNumericFormatV4(serverName) && (null == textToNumericFormatV6(serverName))) {
                    // Not an IP address
                    final int fpos = serverName.indexOf('.');
                    if (fpos < 0) {
                        return null; // Equal to server name
                    }
                    final int pos = serverName.indexOf('.', fpos + 1);
                    if (pos < 0) {
                        return null; // Equal to server name
                    }
                    final String domain = serverName.substring(fpos);
                    final InternetDomainName tmp = InternetDomainName.from(domain);
                    if (tmp.isPublicSuffix()) {
                        return null; // Equal to server name
                    }
                    return domain;
                }
            }
        } else {
            if (!"localhost".equalsIgnoreCase(serverName) && (null == textToNumericFormatV4(serverName)) && (null == textToNumericFormatV6(serverName))) {
                return serverName.toLowerCase(Locale.US).startsWith("www.") ? serverName.substring(4) : serverName;
            }
        }
        return null;
    }

    /**
     * Extracts domain parameter out of specified (JSESSIONID) cookie value.
     *
     * @param id The cookie value
     * @return The domain parameter or <code>null</code>
     */
    public static String extractDomainValue(final String id) {
        if (null == id) {
            return null;
        }
        final int start = id.indexOf('-');
        if (start > 0) {
            final int end = id.lastIndexOf('.');
            if (end > start) {
                return urlDecode(id.substring(start + 1, end));
            }
        }
        return null;
    }

    private static String urlDecode(final String text) {
        try {
            return URLDecoder.decode(text, "iso-8859-1");
        } catch (final UnsupportedEncodingException e) {
            return text;
        }
    }

    /**
     * Pretty-prints given cookies.
     *
     * @param cookies The cookies
     * @return The string representation
     */
    public static String prettyPrint(final Cookie[] cookies) {
        if (null == cookies) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(cookies.length << 4);
        final String sep = System.getProperty("line.separator");
        for (int i = 0; i < cookies.length; i++) {
            final Cookie cookie = cookies[i];
            sb.append(i + 1).append(": ").append(cookie.getName());
            sb.append('=').append(cookie.getValue());
            sb.append("; version=").append(cookie.getVersion());
            final int maxAge = cookie.getMaxAge();
            if (maxAge >= 0) {
                sb.append("; max-age=").append(maxAge);
            }
            final String path = cookie.getPath();
            if (null != path) {
                sb.append("; path=").append(path);
            }
            final String domain = cookie.getDomain();
            if (null != domain) {
                sb.append("; domain=").append(domain);
            }
            final boolean secure = cookie.getSecure();
            if (secure) {
                sb.append("; secure");
            }
            sb.append(sep);
        }
        return sb.toString();
    }

    /**
     * Creates a cookie map for given HTTP request.
     *
     * @param req The HTTP request
     * @return The cookie map or {@link java.util.Collections#emptyMap()}
     */
    public static Map<String, Cookie> cookieMapFor(final HttpServletRequest req) {
        if (null == req) {
            return Collections.emptyMap();
        }
        @SuppressWarnings("unchecked") Map<String, Cookie> m = (Map<String, Cookie>) req.getAttribute("__cookie.map");
        if (null != m) {
            return m;
        }
        final Cookie[] cookies = req.getCookies();
        if (null == cookies) {
            return Collections.emptyMap();
        }
        final int length = cookies.length;
        m = new LinkedHashMap<String, Cookie>(length);
        for (final Cookie cookie : cookies) {
            m.put(cookie.getName(), cookie);
        }
        req.setAttribute("__cookie.map", m);
        return m;
    }
}
