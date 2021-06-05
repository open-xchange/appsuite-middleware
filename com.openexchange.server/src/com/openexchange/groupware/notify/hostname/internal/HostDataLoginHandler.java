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

import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.login.BlockingLoginHandlerService;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.http.Tools;

/**
 * Adds the host data to every session.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class HostDataLoginHandler implements BlockingLoginHandlerService {

    // private final SystemNameService systemNameService;
    private final DispatcherPrefixService dispatcherPrefixService;
    private final ServiceLookup services;

    public HostDataLoginHandler(ServiceLookup services) {
        super();
        this.services = services;
        // this.systemNameService = services.getService(SystemNameService.class);
        this.dispatcherPrefixService = services.getService(DispatcherPrefixService.class);
    }

    @Override
    public void handleLogin(LoginResult login) {
        LoginRequest request = login.getRequest();
        Session session = login.getSession();
        HostDataImpl hostData = new HostDataImpl(
            request.isSecure(),
            determineHost(login, session.getContextId(), session.getUserId()),
            request.getServerPort(),
            request.getHttpSessionID(),
            Tools.extractRoute(request.getHttpSessionID()),
            dispatcherPrefixService.getPrefix());
        session.setParameter(HostnameService.PARAM_HOST_DATA, hostData);
    }

    private String determineHost(LoginResult loginResult, int contextId, int userId) {
        String host = loginResult.getRequest().getServerName();
        final HostnameService hostnameService = services.getOptionalService(HostnameService.class);
        if (null != hostnameService) {
            String tmp;
            if (loginResult.getUser().isGuest()) {
                tmp = hostnameService.getGuestHostname(userId, contextId);
            } else {
                tmp = hostnameService.getHostname(userId, contextId);
            }
            if (null != tmp) {
                host = tmp;
            }
        }
        return host;
    }

    /*-
     *
    private String determineRoute(String httpSessionId) {
        final String retval;
        if (null == httpSessionId) {
            retval = "0123456789." + systemNameService.getSystemName();
        } else {
            if (httpSessionId.indexOf('.') > 0) {
                retval = httpSessionId;
            } else {
                retval = httpSessionId + '.' + systemNameService.getSystemName();
            }
        }
        return retval;
    }
     *
     */

    @Override
    public void handleLogout(LoginResult logout) {
        // Nothing to do.
    }

}
