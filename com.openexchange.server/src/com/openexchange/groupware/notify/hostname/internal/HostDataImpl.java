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
