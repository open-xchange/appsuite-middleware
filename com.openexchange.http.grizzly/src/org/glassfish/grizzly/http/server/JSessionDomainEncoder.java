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

package org.glassfish.grizzly.http.server;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * {@link JSessionDomainEncoder} - Generates an <code>application/x-www-form-urlencoded</code> versions of domain strings that additionally have the safe
 * character "." and "-" replaced by the according URL-Encoding.
 *
 * @author <a href="mailto:marc	.arens@open-xchange.com">Marc Arens</a>
 */
public class JSessionDomainEncoder {

    /**
     * Initializes a new {@link JSessionDomainEncoder}.
     */
    private JSessionDomainEncoder() {
        super();
    }

    /**
     * Generates an application/x-www-form-urlencoded version of specified domain string that additionally has the safe character "." and
     * "-" replaced by the according URL-Encoding.
     *
     * @param domain The domain to encode
     * @return The URL-encoded text that additionally has dots and dashes replaced by their URLEncodings
     * @throws IllegalStateException if the running Java platform doesn't support the iso-8859-1 encoding which is mandatory since 1.4.2
     */
    public static String urlEncode(final String domain) {
        try {
            return replaceDotsAndDashes(URLEncoder.encode(domain, "iso-8859-1"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Every implementation of the Java platform is required to support the iso-8859-1 encoding", e);
        }
    }

    private static String replaceDotsAndDashes(String str) {
        if (null == str) {
            return null;
        }

        int length = str.length();
        if (length == 0) {
            return str;
        }

        StringBuilder sb = null;
        for (int i = 0, k = length; k-- > 0; i++) {
            char ch = str.charAt(i);
            if (ch == '.') {
                if (null == sb) {
                    sb = new StringBuilder(length);
                    if (i > 0) {
                        sb.append(str, 0, i);
                    }
                }
                sb.append("%2E");
            } else if (ch == '-') {
                if (null == sb) {
                    sb = new StringBuilder(length);
                    if (i > 0) {
                        sb.append(str, 0, i);
                    }
                }
                sb.append("%2D");
            } else {
                // Allowed character
                if (null != sb) {
                    sb.append(ch);
                }
            }
        }

        return null == sb ? str : sb.toString();
    }

}
