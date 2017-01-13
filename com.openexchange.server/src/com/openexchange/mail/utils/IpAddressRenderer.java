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

package com.openexchange.mail.utils;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.java.Strings;

/**
 * {@link IpAddressRenderer} - Renders an IP address.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public abstract class IpAddressRenderer {

    /**
     * Gets the simple IP address renderer.
     * <p>
     * Given IP address is returned as-is.
     *
     * @return The simple IP address renderer
     */
    public static IpAddressRenderer simpleRenderer() {
        return new SimpleIpAddressRenderer();
    }

    /**
     * Creates the IP address renderer for specified pattern.
     * <p>
     * Pattern is supposed to be a BNF-alike string; e.g.:
     * <pre>
     *   "[" + IP + "]"
     * </pre>
     *
     * @param pattern The pattern implying how IP address is supposed to be rendered
     * @return The parsed renderer instance
     * @throws IllegalArgumentException If pattern is invalid
     */
    public static IpAddressRenderer createRendererFor(String pattern) {
        if (Strings.isEmpty(pattern)) {
            return new SimpleIpAddressRenderer();
        }

        // "[" + IP + "]"
        List<String> sTokens = Strings.splitAndTrim(pattern, "\\+");

        int size = sTokens.size();
        if (size == 0) {
            return new SimpleIpAddressRenderer();
        }

        if (size == 1) {
            if (false == "IP".equals(sTokens.get(0))) {
                throw new IllegalArgumentException("Invalid IP address pattern: " + pattern);
            }
            return new SimpleIpAddressRenderer();
        }

        List<Token> tokens = new ArrayList<>(size);
        for (String sToken : sTokens) {
            if ("IP".equals(sToken)) {
                tokens.add(new AddressToken());
            } else {
                if (!sToken.startsWith("\"") || !sToken.endsWith("\"") || (sToken.length() <= 2)) {
                    throw new IllegalArgumentException("Invalid IP address pattern: " + pattern);
                }

                tokens.add(new StaticToken(sToken.substring(1, sToken.length() - 1)));
            }
        }
        return new TokenBackedIpAddressRenderer(tokens);
    }

    // -------------------------------------------------------------------------------

    /**
     * Initializes a new {@link IpAddressRenderer}.
     */
    protected IpAddressRenderer() {
        super();
    }

    /**
     * Renders the specified IP address
     *
     * @param ipAddress The IP address
     * @return The rendered IP address
     */
    public abstract String render(String ipAddress);

    // -------------------------------------------------------------------------------

    private static class SimpleIpAddressRenderer extends IpAddressRenderer {

        SimpleIpAddressRenderer() {
            super();
        }

        @Override
        public String render(String ipAddress) {
            return ipAddress;
        }

        @Override
        public String toString() {
            return "${IP}";
        }
    }

    // -------------------------------------------------------------------------------

    private static class TokenBackedIpAddressRenderer extends IpAddressRenderer {

        private final List<Token> tokens;

        TokenBackedIpAddressRenderer(List<Token> tokens) {
            super();
            this.tokens = tokens;
        }

        @Override
        public String render(String ipAddress) {
            StringBuilder sb = new StringBuilder(16);
            for (Token token : tokens) {
                sb.append(token.getToken(ipAddress));
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(16);
            boolean first = true;
            for (Token token : tokens) {
                if (first) {
                    first = false;
                } else {
                    sb.append(" + ");
                }
                sb.append(token.toString());
            }
            return sb.toString();
        }
    }

    // -------------------------------------------------------------------------------

    private static interface Token {

        String getToken(String ipAddress);
    }

    private static class StaticToken implements Token {

        private final String token;

        StaticToken(String token) {
            super();
            this.token = token;
        }

        @Override
        public String getToken(String ipAddress) {
            return token;
        }

        @Override
        public String toString() {
            return "\"" + token + "\"";
        }
    }

    private static class AddressToken implements Token {

        AddressToken() {
            super();
        }

        @Override
        public String getToken(String ipAddress) {
            return ipAddress;
        }

        @Override
        public String toString() {
            return "${IP}";
        }
    }

}
