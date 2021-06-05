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

package com.openexchange.oidc.state.impl;

import com.openexchange.oidc.state.LogoutRequestInfo;

/**
 * {@link DefaultLogoutRequestInfo} Default implementation of {@link LogoutRequestInfo}, used by the core implementation
 * of the OpenID feature.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public class DefaultLogoutRequestInfo implements LogoutRequestInfo {

    private final String state;
    private final String domainName;
    private final String sessionId;
    private final String requestURI;

    public DefaultLogoutRequestInfo(String state, String domainName, String sessionId, String requestURI) {
        super();
        this.state = state;
        this.domainName = domainName;
        this.sessionId = sessionId;
        this.requestURI = requestURI;
    }

    @Override
    public String getState() {
        return this.state;
    }

    @Override
    public String getDomainName() {
        return this.domainName;
    }

    @Override
    public String getSessionId() {
        return this.sessionId;
    }

    @Override
    public String getRequestURI() {
        return this.requestURI;
    }

}
