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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.ajp13.coyote.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import javax.servlet.http.Cookie;
import com.openexchange.ajp13.exception.AJPv13Exception;
import com.openexchange.tools.regex.RFC2616Regex;


/**
 * {@link CookieParser}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CookieParser {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(CookieParser.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

    /**
     * Initializes a new {@link CookieParser}.
     */
    private CookieParser() {
        super();
    }

    private static final Set<String> COOKIE_PARAMS = new HashSet<String>(Arrays.asList("$Path", "$Domain", "$Port"));

    /**
     * Parses specified cookie value.
     *
     * @param headerValue The cookie value
     * @return The parsed cookie(s) from passed header value
     * @throws AJPv13Exception If parsing fails
     */
    public static Cookie[] parseCookieHeader(final String headerValue) throws AJPv13Exception {
        final Matcher m = RFC2616Regex.COOKIE.matcher(headerValue);
        final List<Cookie> cookieList = new ArrayList<Cookie>();
        final StringBuilder valueBuilder = new StringBuilder(128);
        int prevEnd = -1;
        while (m.find()) {
            // offset + 1 -> complete single cookie
            int version = -1;
            {
                final String versionStr = m.group(2);
                if (versionStr == null) {
                    version = 0;
                } else {
                    try {
                        version = Integer.parseInt(versionStr);
                    } catch (final NumberFormatException e) {
                        version = 0;
                        if (DEBUG) {
                            LOG.debug("Version set to 0. No number value in $Version cookie: " + versionStr);
                        }
                    }
                }
            }
            final String name = m.group(3);
            if (null == name) {
                // Regex has always at minimum 2 cookies as groups.
                continue;
            }
            if ((name.length() > 0) && (name.charAt(0) == '$')) {
                if (COOKIE_PARAMS.contains(name)) {
                    /*
                     * A wrongly parsed cookie name which is actually a cookie parameter. Force re-parse of previous cookie.
                     */
                    continue;
                }
                if (LOG.isInfoEnabled()) {
                    LOG.info(new StringBuilder(32).append("Special cookie ").append(name).append(" not handled, yet!"));
                }
            }
            if (prevEnd != -1) {
                final int start = m.start();
                if (start > prevEnd) {
                    // Last cookie skipped some characters
                    final String skipped = prepare(headerValue.substring(prevEnd, start));
                    if (skipped.length() > 0) {
                        reparsePrevCookie(headerValue, skipped, valueBuilder, cookieList);
                    }
                }
            }
            prevEnd = m.end();
            final Cookie c;
            try {
                c = new Cookie(name, prepare(m.group(4)));
            } catch (final RuntimeException e) {
                /*
                 * Cookie name contains illegal characters (for example, a comma, space, or semicolon) or it is one of the tokens reserved
                 * for use by the cookie protocol
                 */
                if (DEBUG) {
                    LOG.debug("Invalid cookie name detected. Ignoring...", e);
                }
                continue;
            }
            c.setVersion(version);
            String attr = m.group(5);
            if (attr != null) {
                /*
                 * Set $Path parameter
                 */
                c.setPath(attr);
            }
            attr = m.group(6);
            if (attr != null) {
                /*
                 * Set $Domain parameter
                 */
                c.setDomain(attr);
            }
            /*
             * Ignore $Port, apply version, and add to list
             */
            cookieList.add(c);
        }
        final int len = headerValue.length();
        if (len > prevEnd && prevEnd >= 0) {
            // Last cookie skipped some characters
            final String skipped = prepare(headerValue.substring(prevEnd, len));
            if (skipped.length() > 0) {
                reparsePrevCookie(headerValue, skipped, valueBuilder, cookieList);
            }
        }
        if ((headerValue.length() > 0) && cookieList.isEmpty()) {
            throw new AJPv13Exception(AJPv13Exception.AJPCode.INVALID_COOKIE_HEADER, true, headerValue);
        }
        if (DEBUG) {
            final StringBuilder sb = new StringBuilder(256).append("Parsed Cookies:\n");
            for (final Cookie cookie : cookieList) {
                sb.append('\'').append(cookie.getName()).append("'='").append(cookie.getValue()).append("'\n");
            }
            LOG.debug(sb.toString());
        }
        return cookieList.toArray(new Cookie[cookieList.size()]);
    }

    /**
     * Re-Parse previous cookie in list
     *
     * @param headerValue The complete cookie header value
     * @param skipped The skipped string
     * @param valueBuilder A string builder needed to compose proper cookie value
     * @param cookieList The cookie list
     */
    private static void reparsePrevCookie(final String headerValue, final String skipped, final StringBuilder valueBuilder, final List<Cookie> cookieList) {
        final String prevValue;
        final Cookie prevCookie = cookieList.get(cookieList.size() - 1);
        valueBuilder.append(prevCookie.getValue());
        String prevAttr = prevCookie.getPath();
        if (null != prevAttr) {
            valueBuilder.append("; $Path=").append(prevAttr);
        }
        prevAttr = prevCookie.getDomain();
        if (null != prevAttr) {
            valueBuilder.append("; $Domain=").append(prevAttr);
        }
        valueBuilder.append(skipped);
        final String complVal = valueBuilder.toString();
        valueBuilder.setLength(0);
        int paramsStart = -1;
        /*
         * Check for parameters except $Port
         */
        Matcher paramMatcher = RFC2616Regex.COOKIE_PARAM_PATH.matcher(complVal);
        if (paramMatcher.find()) {
            paramsStart = paramMatcher.start();
            prevCookie.setPath(paramMatcher.group(1));
        }
        paramMatcher = RFC2616Regex.COOKIE_PARAM_DOMAIN.matcher(complVal);
        if (paramMatcher.find()) {
            paramsStart = Math.min(paramMatcher.start(), paramsStart);
            prevCookie.setDomain(paramMatcher.group(1));
        }
        if (paramsStart == -1) {
            prevValue = complVal;
        } else {
            prevValue = complVal.substring(0, paramsStart);
        }
        prevCookie.setValue(prevValue);
    }

    /**
     * Prepares passed cookie value.
     *
     * @param cookieValue The cookie value to prepare
     * @return The prepared cookie value.
     */
    private static String prepare(final String cookieValue) {
        if (null == cookieValue || cookieValue.length() == 0) {
            return cookieValue;
        }
        String cv = cookieValue;
        int mlen = cv.length() - 1;
        if (cv.charAt(mlen) == ';') {
            if (mlen == 0) {
                return "";
            }
            do {
                mlen--;
            } while ((mlen > 0) && (cv.charAt(mlen) == ';'));
            cv = cv.substring(0, mlen);
        }
        if ((mlen > 0) && (cv.charAt(0) == '"') && (cv.charAt(mlen) == '"')) {
            cv = cv.substring(1, mlen);
        }
        return cv;
    }

}
