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

package com.openexchange.mail.transport.config;

import javax.mail.internet.InternetAddress;



/**
 * {@link NoReplyConfig}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public interface NoReplyConfig {

    /**
     * The secure mode enumeration.
     */
    public static enum SecureMode {

        /**
         * The plain connection mode
         */
        PLAIN("plain"),
        /**
         * The SSL secure mode
         */
        SSL("SSL"),
        /**
         * The TLS secure mode
         */
        TLS("TLS"), ;

        private final String identifier;

        private SecureMode(String identifier) {
            this.identifier = identifier;
        }

        /**
         * Gets the identifier
         *
         * @return The identifier
         */
        public String getIdentifier() {
            return identifier;
        }

        /**
         * Gets the secure mode for given identifier.
         *
         * @param identifier The identifier
         * @return The associated secure mode or <code>null</code>
         */
        public static SecureMode secureModeFor(String identifier) {
            if (null == identifier) {
                return null;
            }

            for (SecureMode sm : SecureMode.values()) {
                if (identifier.equalsIgnoreCase(sm.identifier)) {
                    return sm;
                }
            }
            return null;
        }
    }

    /**
     * Gets the address
     *
     * @return The address
     */
    InternetAddress getAddress();

    /**
     * Gets the login
     *
     * @return The login
     */
    String getLogin();

    /**
     * Gets the password
     *
     * @return The password
     */
    String getPassword();

    /**
     * Gets the server
     *
     * @return The server
     */
    String getServer();

    /**
     * Gets the port
     *
     * @return The port
     */
    int getPort();

    /**
     * Gets the secure mode
     *
     * @return The secure mode
     */
    SecureMode getSecureMode();

}
