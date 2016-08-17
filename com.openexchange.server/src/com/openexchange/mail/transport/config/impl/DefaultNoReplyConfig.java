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

package com.openexchange.mail.transport.config.impl;

import javax.mail.internet.InternetAddress;
import com.openexchange.mail.transport.config.NoReplyConfig;

/**
 * {@link NoReplyConfig} - The configuration for the no-reply account used for system-initiated messages.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
final class DefaultNoReplyConfig implements NoReplyConfig {

    private InternetAddress address;

    private String login;

    private String password;

    private String server;

    private int port;

    private SecureMode secureMode;

    /**
     * Initializes a new {@link NoReplyConfig}.
     */
    DefaultNoReplyConfig() {
        super();
    }

    /**
     * Checks if this no-reply configuration is valid
     *
     * @return <code>true</code> if valid; otherwise <code>false</code>
     */
    public boolean isValid() {
        return (null != address) && (null != server) && (port > 0) && (port < 65536) && (null != secureMode);
    }

    /**
     * Gets the address
     *
     * @return The address
     */
    @Override
    public InternetAddress getAddress() {
        return address;
    }

    /**
     * Sets the address
     *
     * @param address The address to set
     */
    public void setAddress(InternetAddress address) {
        this.address = address;
    }

    /**
     * Gets the login
     *
     * @return The login
     */
    @Override
    public String getLogin() {
        return login;
    }

    /**
     * Sets the login
     *
     * @param login The login to set
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * Gets the password
     *
     * @return The password
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password
     *
     * @param password The password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the server
     *
     * @return The server
     */
    @Override
    public String getServer() {
        return server;
    }

    /**
     * Sets the server
     *
     * @param server The server to set
     */
    public void setServer(String server) {
        this.server = server;
    }

    /**
     * Gets the port
     *
     * @return The port
     */
    @Override
    public int getPort() {
        return port;
    }

    /**
     * Sets the port
     *
     * @param port The port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the secure mode
     *
     * @return The secure mode
     */
    @Override
    public SecureMode getSecureMode() {
        return secureMode;
    }

    /**
     * Sets the secureMode
     *
     * @param secureMode The secureMode to set
     */
    public void setSecureMode(SecureMode secureMode) {
        this.secureMode = secureMode;
    }

}
