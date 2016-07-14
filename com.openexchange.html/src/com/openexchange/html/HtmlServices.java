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

package com.openexchange.html;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import com.openexchange.config.ConfigurationService;
import com.openexchange.html.internal.WhitelistedSchemes;
import com.openexchange.html.osgi.Services;

/**
 * {@link HtmlServices} - A utility class for {@link HtmlService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HtmlServices {

    /**
     * Initializes a new {@link HtmlServices}.
     */
    private HtmlServices() {
        super();
    }

    /**
     * Checks if specified URL String is safe or not.
     *
     * @param val The URL String to check
     * @return <code>true</code> if safe; otherwise <code>false</code>
     */
    public static boolean isNonJavaScriptURL(final String val) {
        return isNonJavaScriptURL(val, new String[0]);
    }

    /**
     * Checks if specified URL String is safe or not.
     *
     * @param val The URL String to check
     * @param more More tokens to look for
     * @return <code>true</code> if safe; otherwise <code>false</code>
     */
    public static boolean isNonJavaScriptURL(final String val, final String... more) {
        if (null == val) {
            return false;
        }
        String lc = asciiLowerCase(val.trim());
        if (lc.indexOf("javascript:") >= 0 || lc.indexOf("vbscript:") >= 0 || lc.indexOf("<script") >= 0) {
            return false;
        }
        if (null != more && more.length > 0) {
            for (final String token : more) {
                if (lc.indexOf(asciiLowerCase(token)) >= 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if specified URL String is unsafe or not.
     *
     * @param val The URL String to check
     * @return <code>true</code> if unsafe; otherwise <code>false</code>
     */
    public static boolean isJavaScriptURL(final String val) {
        return !isNonJavaScriptURL(val);
    }

    // ---------------------------------------------------------------------------------------------------------------------------- //

    /**
     * Checks given possible URI it is safe being appended/inserted to HTML content.
     *
     * @param possibleUrl The possible URI to check
     * @return <code>true</code> if safe; otherwise <code>false</code>
     */
    public static boolean isSafe(String possibleUrl) {
        if (null == possibleUrl) {
            return true;
        }

        // Check for possible URI
        URI uri;
        try {
            uri = new URI(possibleUrl.trim());
        } catch (URISyntaxException x) {
            // At least check for common attach vectors
            return isNonJavaScriptURL(possibleUrl);
        }

        // Get URI's scheme and compare against possible whitelist
        String scheme = uri.getScheme();
        if (null == scheme) {
            // No scheme...
            return true;
        }

        List<String> schemes = WhitelistedSchemes.getWhitelistedSchemes();
        if (schemes.isEmpty()) {
            // No allowed schemes specified
            return isNonJavaScriptURL(possibleUrl);
        }

        String lc = asciiLowerCase(scheme);
        for (String s : schemes) {
            if (lc.equals(s)) {
                // Matches an allowed scheme
                return isNonJavaScriptURL(possibleUrl);
            }
        }

        // Not allowed...
        return false;
    }

    // ---------------------------------------------------------------------------------------------------------------------------- //

    /** Volatile cache variable for HTML size threshold */
    private static volatile Integer maxLength;

    /**
     * Gets the HTML size threshold (<code>"<i>com.openexchange.html.maxLength</i>"</code> property).
     *
     * @return The HTML size threshold
     */
    public static int htmlThreshold() {
        Integer i = maxLength;
        if (null == maxLength) {
            synchronized (HtmlServices.class) {
                i = maxLength;
                if (null == maxLength) {
                    // Default is 1MB
                    ConfigurationService service = Services.optService(ConfigurationService.class);
                    int defaultMaxLength = 1048576;
                    if (null == service) {
                        return defaultMaxLength;
                    }
                    i = Integer.valueOf(service.getIntProperty("com.openexchange.html.maxLength", defaultMaxLength));
                    maxLength = i;
                }
            }
        }
        return i.intValue();
    }

    // ------------------------------------------------------------------------------------------------------------------------ //

    private static char[] lowercases = {
        '\000', '\001', '\002', '\003', '\004', '\005', '\006', '\007', '\010', '\011', '\012', '\013', '\014', '\015', '\016', '\017',
        '\020', '\021', '\022', '\023', '\024', '\025', '\026', '\027', '\030', '\031', '\032', '\033', '\034', '\035', '\036', '\037',
        '\040', '\041', '\042', '\043', '\044', '\045', '\046', '\047', '\050', '\051', '\052', '\053', '\054', '\055', '\056', '\057',
        '\060', '\061', '\062', '\063', '\064', '\065', '\066', '\067', '\070', '\071', '\072', '\073', '\074', '\075', '\076', '\077',
        '\100', '\141', '\142', '\143', '\144', '\145', '\146', '\147', '\150', '\151', '\152', '\153', '\154', '\155', '\156', '\157',
        '\160', '\161', '\162', '\163', '\164', '\165', '\166', '\167', '\170', '\171', '\172', '\133', '\134', '\135', '\136', '\137',
        '\140', '\141', '\142', '\143', '\144', '\145', '\146', '\147', '\150', '\151', '\152', '\153', '\154', '\155', '\156', '\157',
        '\160', '\161', '\162', '\163', '\164', '\165', '\166', '\167', '\170', '\171', '\172', '\173', '\174', '\175', '\176', '\177' };

    /**
     * Fast lower-case conversion.
     *
     * @param s The string
     * @return The lower-case string
     */
    public static String asciiLowerCase(String s) {
        if (null == s) {
            return null;
        }

        char[] c = null;
        int i = s.length();

        // look for first conversion
        while (i-- > 0) {
            char c1 = s.charAt(i);
            if (c1 <= 127) {
                char c2 = lowercases[c1];
                if (c1 != c2) {
                    c = s.toCharArray();
                    c[i] = c2;
                    break;
                }
            }
        }

        while (i-- > 0) {
            if (c[i] <= 127) {
                c[i] = lowercases[c[i]];
            }
        }

        return c == null ? s : new String(c);
    }

}
