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

package com.openexchange.clientinfo.impl;

import com.openexchange.ajax.Client;
import com.openexchange.clientinfo.ClientInfo;
import com.openexchange.clientinfo.ClientInfoProvider;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.serverconfig.ServerConfig;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.session.Session;
import com.openexchange.uadetector.UserAgentParser;
import net.sf.uadetector.OperatingSystem;
import net.sf.uadetector.ReadableUserAgent;

/**
 * {@link WebClientInfoProvider}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.0
 */
public class WebClientInfoProvider implements ClientInfoProvider {

    private final String APPSUITE = "OX App Suite";
    private final String OX6 = "OX6 UI";
    private final ServiceLookup services;

    /**
     * Initializes a new {@link WebClientInfoProvider}.
     *
     * @param services The service look-up
     */
    public WebClientInfoProvider(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public ClientInfo getClientInfo(Session session) {
        if (null != session) {
            String client = "";
            if (Client.APPSUITE_UI.getClientId().equals(session.getClient())) {
                ServerConfigService serverConfigService = services.getService(ServerConfigService.class);
                if (null != serverConfigService) {
                    try {
                        String hostname = (String) session.getParameter(Session.PARAM_HOST_NAME);
                        ServerConfig config = serverConfigService.getServerConfig(hostname, session);
                        client = config.getProductName();
                    } catch (OXException e) {
                        client = APPSUITE;
                    }
                }
            }
            if (Client.OX6_UI.getClientId().equals(session.getClient())) {
                client = OX6;
            }
            if (Strings.isEmpty(client)) {
                return null;
            }
            UserAgentParser parser = services.getService(UserAgentParser.class);
            String userAgent = (String) session.getParameter(Session.PARAM_USER_AGENT);
            if (null != parser && Strings.isNotEmpty(userAgent)) {
                ReadableUserAgent info = parser.parse(userAgent);
                OperatingSystem operatingSystem = info.getOperatingSystem();
                String os = null;
                String osVersion = null;
                if (null != operatingSystem) {
                    os = operatingSystem.getFamilyName();
                    StringBuilder sb = new StringBuilder().append(operatingSystem.getVersionNumber().getMajor()).append(".").append(operatingSystem.getVersionNumber().getMinor());
                    osVersion = sb.toString();
                }
                String browser = info.getName();
                String browserVersion = info.getVersionNumber().getMajor();

                return new WebClientInfo(client, os, osVersion, browser, browserVersion);
            }
        }

        return null;
    }

    @Override
    public ClientInfo getClientInfo(String clientId) {
        String client = "";
        if (Client.APPSUITE_UI.getClientId().equals(clientId)) {
            client = APPSUITE;
        } else if (Client.OX6_UI.getClientId().equals(clientId)) {
            client = OX6;
        }
        return new WebClientInfo(client, null, null, null, null);
    }

}
