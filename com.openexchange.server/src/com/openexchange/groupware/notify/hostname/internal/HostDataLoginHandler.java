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

package com.openexchange.groupware.notify.hostname.internal;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.login.BlockingLoginHandlerService;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.session.Session;
import com.openexchange.systemname.SystemNameService;
import com.openexchange.version.Version;

/**
 * Adds the host data to every session.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class HostDataLoginHandler implements BlockingLoginHandlerService {

    private final AtomicReference<HostnameService> hostnameService;
    private final AtomicReference<SystemNameService> systemNameService;

    public HostDataLoginHandler() {
        super();
        hostnameService = new AtomicReference<HostnameService>();
        systemNameService = new AtomicReference<SystemNameService>();
    }

    @Override
    public void handleLogin(LoginResult login) {
        LoginRequest request = login.getRequest();
        Session session = login.getSession();
        HostDataImpl hostData = new HostDataImpl(
            request.isSecure(),
            determineHost(request, session.getContextId(), session.getUserId()),
            request.getServerPort(),
            determineHttpSessionId(request.getHttpSessionID()));
        session.setParameter(HostnameService.PARAM_HOST_DATA, hostData);
    }

    private String determineHost(LoginRequest request, int contextId, int userId) {
        String host = request.getServerName();
        final HostnameService hostnameService = this.hostnameService.get();
        if (null != hostnameService) {
            String tmp = hostnameService.getHostname(userId, contextId);
            if (null != tmp) {
                host = tmp;
            }
        }
        return host;
    }

    private String determineHttpSessionId(String httpSessionId) {
        final String retval;
        if (null == httpSessionId) {
            final SystemNameService systemNameService = this.systemNameService.get();
            retval = "0123456789." + (null == systemNameService ? Version.NAME : systemNameService.getSystemName());
        } else {
            if (httpSessionId.indexOf('.') > 0) {
                retval = httpSessionId;
            } else {
                final SystemNameService systemNameService = this.systemNameService.get();
                retval = httpSessionId + '.' + (null == systemNameService ? Version.NAME : systemNameService.getSystemName());
            }
        }
        return retval;
    }

    @Override
    public void handleLogout(LoginResult logout) {
        // Nothing to do.
    }

    public void setHostnameService(HostnameService hostnameService) {
        this.hostnameService.set(hostnameService);
    }

    public void setSystemNameService(SystemNameService systemNameService) {
        this.systemNameService.set(systemNameService);
    }
}
