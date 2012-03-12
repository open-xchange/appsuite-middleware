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

package com.openexchange.mail.smal.impl.json;

/**
 * {@link JSONServerSetting}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JSONServerSetting {

    private String host;

    private boolean secure;

    private int port;

    private String login;

    private String password;

    /**
     * Initializes a new {@link JSONServerSetting}.
     */
    public JSONServerSetting() {
        super();
    }

    /**
     * Sets the host
     *
     * @param host The host to set
     */
    public void setHost(final String host) {
        this.host = host;
    }

    /**
     * Gets the host
     *
     * @return The host
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the secure
     *
     * @param secure The secure to set
     */
    public void setSecure(final boolean secure) {
        this.secure = secure;
    }

    /**
     * Gets the secure
     *
     * @return The secure
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * Sets the port
     *
     * @param port The port to set
     */
    public void setPort(final int port) {
        this.port = port;
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
     * Sets the login
     *
     * @param login The login to set
     */
    public void setLogin(final String login) {
        this.login = login;
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
     * Sets the password
     *
     * @param password The password to set
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Gets the password
     *
     * @return The password
     */
    public String getPassword() {
        return password;
    }
}
