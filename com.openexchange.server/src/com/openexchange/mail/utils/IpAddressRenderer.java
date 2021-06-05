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
