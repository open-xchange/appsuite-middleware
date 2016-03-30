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

package com.openexchange.groupware.notify.hostname.internal;

import com.openexchange.groupware.notify.hostname.HostData;

/**
 * {@link HostDataImpl} - The {@link HostData} implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HostDataImpl implements HostData {

    private String httpSessionID;
    private String host;
    private String route;
    private int port;
    private boolean secure;
    private final String dispatcherPrefix;

    public HostDataImpl(boolean secure, String host, int port, String httpSessionID, String route, String dispatcherPrefix) {
        super();
        this.secure = secure;
        this.host = host;
        this.port = port;
        this.httpSessionID = httpSessionID;
        this.route = route;
        this.dispatcherPrefix = dispatcherPrefix;
    }

    @Override
    public String getHTTPSession() {
        return httpSessionID;
    }

    @Override
    public String getRoute() {
        return route;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port < 0 ? (secure ? 443 : 80) : port;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public String getDispatcherPrefix() {
        return dispatcherPrefix;
    }

    /**
     * Sets the HTTP session ID.
     *
     * @param httpSessionID The HTTP session ID to set
     */
    public void setHTTPSession(String httpSessionID) {
        this.httpSessionID = httpSessionID;
    }

    /**
     * Sets the route: &lt;http-session-id&gt; + <code>"." </code>+ &lt;route&gt;
     *
     * @param route The route to set
     */
    public void setRoute(final String route) {
        this.route = route;
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
     * Sets the secure
     *
     * @param secure The secure to set
     */
    public void setSecure(final boolean secure) {
        this.secure = secure;
    }

}
