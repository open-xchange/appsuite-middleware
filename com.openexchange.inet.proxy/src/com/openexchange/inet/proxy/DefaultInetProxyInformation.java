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

package com.openexchange.inet.proxy;

import java.util.List;

/**
 * {@link DefaultInetProxyInformation}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DefaultInetProxyInformation implements InetProxyInformation {

    private boolean secure;
    private String host;
    private int port;
    private String userName;
    private String password;
    private List<String> nonProxyHosts;

    /**
     * Initializes a new {@link DefaultInetProxyInformation}.
     */
    public DefaultInetProxyInformation() {
        super();
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public List<String> getNonProxyHosts() {
        return nonProxyHosts;
    }

    /**
     * Sets the secure flag
     * 
     * @param secure The secure flag to set
     */
    public void setSecure(final boolean secure) {
        this.secure = secure;
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
     * Sets the port
     * 
     * @param port The port to set
     */
    public void setPort(final int port) {
        this.port = port;
    }

    /**
     * Sets the user name
     * 
     * @param userName The user name to set
     */
    public void setUserName(final String userName) {
        this.userName = userName;
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
     * Sets the non-proxy hosts
     * 
     * @param nonProxyHosts The non-proxy hosts to set
     */
    public void setNonProxyHosts(List<String> nonProxyHosts) {
        this.nonProxyHosts = nonProxyHosts;
    }

}
