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

import java.nio.charset.UnsupportedCharsetException;
import com.openexchange.tools.encoding.Base64;

/**
 * {@link Authorization}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Authorization {

    private static final String BASIC_AUTH = "basic";

    private static final String SPNEGO_AUTH = "negotiate";

    /**
     * Digest type for authorization.
     */
    private static final String DIGEST_AUTH = "digest";

    private Authorization() {
        super();
    }

    public static boolean checkForAuthorizationHeader(String authHeader) {
        final String authScheme = extractAuthScheme(authHeader);
        if (null == authScheme) {
            return false;
        }
        if (!(authScheme.equalsIgnoreCase(BASIC_AUTH) || authScheme.equalsIgnoreCase(SPNEGO_AUTH))) {
            return false;
        }
        return true;
    }

    /**
     * Checks if the client sends a correct digest authorization header.
     *
     * @param auth Authorization header.
     * @return <code>true</code> if the client sent a correct authorization header.
     */
    public static boolean checkForDigestAuthorization(final String auth) {
        if (null == auth) {
            return false;
        }
        if (auth.length() <= DIGEST_AUTH.length()) {
            return false;
        }
        if (!auth.substring(0, DIGEST_AUTH.length()).equalsIgnoreCase(DIGEST_AUTH)) {
            return false;
        }
        return true;
    }

    public static String extractAuthScheme(String authHeader) {
        if (com.openexchange.java.Strings.isEmpty(authHeader)) {
            return null;
        }
        final int spacePos = authHeader.indexOf(' ');
        if (-1 == spacePos) {
            return null;
        }
        return authHeader.substring(0, spacePos);
    }

    /**
     * Checks if the client sends a correct basic authorization header.
     *
     * @param authHeader Authorization header.
     * @return <code>true</code> if the client sent a correct authorization header.
     */
    public static boolean checkForBasicAuthorization(final String authHeader) {
        final String authScheme = extractAuthScheme(authHeader);
        if (null == authScheme) {
            return false;
        }
        return authScheme.equalsIgnoreCase(BASIC_AUTH);
    }

    /**
     * Checks if the client sends a correct kerberos authorization header.
     *
     * @param authHeader Authorization header.
     * @return <code>true</code> if the client sent a correct authorization header.
     */
    public static boolean checkForKerberosAuthorization(final String authHeader) {
        final String authScheme = extractAuthScheme(authHeader);
        if (null == authScheme) {
            return false;
        }
        if (!authScheme.equalsIgnoreCase(SPNEGO_AUTH)) {
            return false;
        }
        return true;
    }

    /**
     * The credentials providing login and password.
     */
    public static class Credentials {

        private final String login;
        private final String password;

        /**
         * Initializes a new {@link Credentials}.
         *
         * @param login The login
         * @param password The password
         */
        public Credentials(final String login, final String password) {
            super();
            this.login = login;
            this.password = password;
        }

        /**
         * Gets the login
         *
         * @return The login
         */
        public String getLogin() {
            return login;
        }

        /**
         * Gets the password
         *
         * @return The password
         */
        public String getPassword() {
            return password;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(24);
            builder.append("Credentials [");
            if (login != null) {
                builder.append("login=").append(login).append(", ");
            }
            if (password != null) {
                builder.append("password=").append(password);
            }
            builder.append("]");
            return builder.toString();
        }

    }

    /**
     * The unknown character: <code>'&#65533;'</code>
     */
    private static final char UNKNOWN = '\ufffd';

    public static Credentials decode(final String auth) throws UnsupportedCharsetException {
        final byte[] decoded = Base64.decode(auth.substring(BASIC_AUTH.length() + 1));
        String userpass = new String(decoded, com.openexchange.java.Charsets.UTF_8).trim();
        if (userpass.indexOf(UNKNOWN) >= 0) {
            userpass = new String(decoded, com.openexchange.java.Charsets.ISO_8859_1).trim();
        }
        final int delimiter = userpass.indexOf(':');
        String login = "";
        String pass = "";
        if (-1 != delimiter) {
            login = userpass.substring(0, delimiter);
            pass = userpass.substring(delimiter + 1);
        }
        return new Credentials(login, pass);
    }

    /**
     * Checks if the login contains only valid values.
     *
     * @param pass password of the user
     * @return false if the login contains illegal values.
     */
    public static boolean checkLogin(final String pass) {
        // check if the user wants to login without password.
        // ldap bind doesn't fail with empty password. so check it here.
        return (pass != null && !com.openexchange.java.Strings.isEmpty(pass));
    }
}
