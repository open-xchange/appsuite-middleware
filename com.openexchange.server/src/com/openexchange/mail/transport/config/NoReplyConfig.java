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

package com.openexchange.mail.transport.config;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.slf4j.Logger;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link NoReplyConfig} - The configuration for the no-reply account used for system-initiated messages.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public final class NoReplyConfig {

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

    // --------------------------------------------------------------------------------------------------------------------------------- //

    /**
     * Gets the no-reply configuration.
     *
     * @return The no-reply configuration
     * @throws OXException If no-reply configuration cannot be return
     */
    public static NoReplyConfig getInstance(Session session) throws OXException {
        NoReplyConfig retval = (NoReplyConfig) session.getParameter("__transport.noreply");
        if (null == retval) {
            retval = getInstance(session.getUserId(), session.getContextId());
            session.setParameter("__transport.noreply", retval);
        }
        return retval;
    }

    /**
     * Gets the no-reply configuration.
     *
     * @return The no-reply configuration
     * @throws OXException If no-reply configuration cannot be return
     */
    public static NoReplyConfig getInstance(int userId, int contextId) throws OXException {
        return new NoReplyConfig(userId, contextId);
    }

    // --------------------------------------------------------------------------------------------------------------------------------- //

    private final InternetAddress address;
    private final String login;
    private final String password;
    private final String server;
    private final int port;
    private final SecureMode secureMode;

    /**
     * Initializes a new {@link NoReplyConfig}.
     *
     * @throws OXException If initialization fails
     */
    private NoReplyConfig(int userId, int contextId) throws OXException {
        super();

        ConfigViewFactory factory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
        ConfigView view = factory.getView(userId, contextId);

        Logger logger = org.slf4j.LoggerFactory.getLogger(NoReplyConfig.class);

        {
            String sAddress = view.get("com.openexchange.noreply.address", String.class);
            InternetAddress address;
            if (Strings.isEmpty(sAddress)) {
                String msg = "Missing no-reply address";
                logger.error(msg, new Throwable(msg));
                address = null;
            } else {
                try {
                    address = new QuotedInternetAddress(sAddress, false);
                } catch (AddressException e) {
                    logger.error("Invalid no-reply address", e);
                    address = null;
                }
            }

            this.address = address;
        }

        {
            String str = view.get("com.openexchange.noreply.login", String.class);
            if (Strings.isEmpty(str)) {
                String msg = "Missing no-reply login";
                logger.error(msg, new Throwable(msg));
                login = null;
            } else {
                login = str.trim();
            }
        }

        {
            String str = view.get("com.openexchange.noreply.password", String.class);
            if (Strings.isEmpty(str)) {
                String msg = "Missing no-reply password";
                logger.error(msg, new Throwable(msg));
                password = null;
            } else {
                password = str.trim();
            }
        }

        {
            String str = view.get("com.openexchange.noreply.server", String.class);
            if (Strings.isEmpty(str)) {
                String msg = "Missing no-reply server";
                logger.error(msg, new Throwable(msg));
                server = null;
            } else {
                server = str.trim();
            }
        }

        {
            String str = view.get("com.openexchange.noreply.port", String.class);
            if (Strings.isEmpty(str)) {
                logger.info("Missing no-reply port. Using 25 as fall-back value.");
                port = 25;
            } else {
                int p = Strings.parseInt(str.trim());
                if (p < 0) {
                    logger.warn("Invalid no-reply port: {}. Using 25 as fall-back value.", str);
                    port = 25;
                } else {
                    port = p;
                }
            }
        }

        {
            String str = view.get("com.openexchange.noreply.secureMode", String.class);
            if (Strings.isEmpty(str)) {
                logger.info("Missing no-reply secure mode. Using \"plain\" as fall-back value.");
                secureMode = SecureMode.PLAIN;
            } else {
                SecureMode tmp = SecureMode.secureModeFor(str.trim());
                if (null == tmp) {
                    logger.warn("Invalid no-reply secure mode: {}. Using \"plain\" as fall-back value.", str);
                    secureMode = SecureMode.PLAIN;
                } else {
                    secureMode = tmp;
                }
            }
        }
    }

    /**
     * Gets the address
     *
     * @return The address
     */
    public InternetAddress getAddress() {
        return address;
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

    /**
     * Gets the server
     *
     * @return The server
     */
    public String getServer() {
        return server;
    }

    /**
     * Gets the port
     *
     * @return The port
     */
    public int getPort() {
        return port;
    }

    /**
     * Gets the secure mode
     *
     * @return The secure mode
     */
    public SecureMode getSecureMode() {
        return secureMode;
    }

}
