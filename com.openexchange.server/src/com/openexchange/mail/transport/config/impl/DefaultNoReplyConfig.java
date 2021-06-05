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
