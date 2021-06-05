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

package com.openexchange.framework.request;

import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.session.UserContextSession;

/**
 * Default implementation of {@link RequestContext}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class DefaultRequestContext implements RequestContext {

    private HostData hostData;
    private String userAgent;
    private UserContextSession session;

    /**
     * Initializes a new {@link DefaultRequestContext}.
     */
    public DefaultRequestContext() {
        super();
    }

    @Override
    public HostData getHostData() {
        return hostData;
    }

    /**
     * Sets the host data
     *
     * @param hostData The host data to set
     */
    public void setHostData(HostData hostData) {
        this.hostData = hostData;
    }

    @Override
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Sets the user agent.
     *
     * @param userAgent The user agent to set
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public UserContextSession getSession() {
        return session;
    }

    /**
     * Sets the session
     *
     * @param session The session to set
     */
    public void setSession(UserContextSession session) {
        this.session = session;
    }

}
