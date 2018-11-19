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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import com.openexchange.clientinfo.ClientInfo;
import com.openexchange.clientinfo.ClientInfoProvider;
import com.openexchange.clientinfo.ClientInfoService;
import com.openexchange.clientinfo.ClientInfoType;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.session.Session;


/**
 * {@link ClientInfoServiceImpl}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class ClientInfoServiceImpl implements ClientInfoService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ClientInfoServiceImpl.class);

    private final ServiceListing<ClientInfoProvider> providers;
    private final ClientInfo defaultClientInfo;

    /**
     * Initializes a new {@link ClientInfoServiceImpl}.
     *
     * @param providers The tracked providers
     */
    public ClientInfoServiceImpl(ServiceListing<ClientInfoProvider> providers) {
        super();
        this.providers = providers;
        defaultClientInfo = new DefaultClientInfo();
    }

    @Override
    public ClientInfo getClientInfo(Session session) {
        if (null != session) {
            for (ClientInfoProvider provider : providers) {
                ClientInfo info = provider.getClientInfo(session);
                if (null != info) {
                    return info;
                }
            }
            LOG.debug("Unknown client found. Client identifier: {} User-Agent: {}", session.getClient(), session.getParameter(Session.PARAM_USER_AGENT));
        }
        return defaultClientInfo;
    }

    @Override
    public ClientInfo getClientInfo(String clientId) {
        if (null != clientId) {
            for (ClientInfoProvider provider : providers) {
                ClientInfo info = provider.getClientInfo(clientId);
                if (null != info) {
                    return info;
                }
            }
            LOG.debug("Unknown client found. Client identifier: {}", clientId);
        }
        return defaultClientInfo;
    }

    @Override
    public Map<String, ClientInfo> getClientInfos(List<Session> sessions) {
        Map<String, ClientInfo> map = new HashMap<>(sessions.size());
        for (Session session : sessions) {
            ClientInfo info = getClientInfo(session);
            if (null != info) {
                map.put(session.getSessionID(), info);
            }
        }
        return map;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class DefaultClientInfo implements ClientInfo {

        private final String unknown = "unknown";

        /**
         * Initializes a new {@link ClientInfoServiceImpl.DefaultClientInfo}.
         */
        DefaultClientInfo() {
            super();
        }

        @Override
        public ClientInfoType getType() {
            return ClientInfoType.OTHER;
        }

        @Override
        public String getDisplayName(Locale locale) {
            return StringHelper.valueOf(locale).getString(ClientInfoStrings.UNKNOWN_CLIENT);
        }

        @Override
        public String getOSFamily() {
            return unknown;
        }

        @Override
        public String getOSVersion() {
            return unknown;
        }

        @Override
        public String getClientName() {
            return unknown;
        }

        @Override
        public String getClientVersion() {
            return unknown;
        }

        @Override
        public String getClientFamily() {
            return unknown;
        }
    } // End of class DefaultClientInfo

}
