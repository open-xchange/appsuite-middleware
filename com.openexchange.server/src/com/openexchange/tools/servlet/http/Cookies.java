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

package com.openexchange.tools.servlet.http;

import static com.openexchange.sessiond.impl.IPAddressUtil.textToNumericFormatV4;
import static com.openexchange.sessiond.impl.IPAddressUtil.textToNumericFormatV6;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Locale;
import javax.servlet.http.Cookie;
import com.openexchange.ajax.Login;
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

    private static volatile Boolean prefixWithDot;

    /**
     * Checks whether domain parameter should start with a dot (<code>'.'</code>) character
     * 
     * @return <code>true</code> for starting dot; otherwise <code>false</code>
     */
    public static boolean prefixWithDot() {
        Boolean tmp = prefixWithDot;
        if (null == tmp) {
            synchronized (Login.class) {
                tmp = prefixWithDot;
                if (null == tmp) {
                    final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    tmp = Boolean.valueOf(null != service && service.getBoolProperty("com.openexchange.cookie.domain.prefixWithDot", false));
                    prefixWithDot = tmp;
                }
            }
        }
        return tmp.booleanValue();
    }

    /**
     * Gets the domain parameter for specified server name with configured default behavior whether to prefix domain with a dot (
     * <code>'.'</code>) character.
     * 
     * @param serverName The server name
     * @return The domain parameter or <code>null</code>
     * @see #prefixWithDot()
     */
    public static String getDomainValue(final String serverName) {
        return getDomainValue(serverName, prefixWithDot());
    }

    /**
     * Gets the domain parameter for specified server name.
     * 
     * @param serverName The server name
     * @param prefixWithDot Whether to prefix domain with a dot (<code>'.'</code>) character
     * @return The domain parameter or <code>null</code>
     */
    public static String getDomainValue(final String serverName, final boolean prefixWithDot) {
        if (null == serverName) {
            return null;
        }
        if (prefixWithDot) {
            if (serverName.startsWith("www.")) {
                return serverName.substring(3);
            } else if ("localhost".equalsIgnoreCase(serverName)) {
                return null;
            } else {
                // Not an IP address
                if (null == textToNumericFormatV4(serverName) && (null == textToNumericFormatV6(serverName))) {
                    return new StringBuilder(serverName.length() + 1).append('.').append(serverName).toString();
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
                return urlDecode(id.substring(start+1, end));
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

}
