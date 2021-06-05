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

package com.openexchange.clientinfo.impl;

import java.util.LinkedHashMap;
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
        Map<String, ClientInfo> map = new LinkedHashMap<>(sessions.size());
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

        private static final String UNKNOWN = "unknown";

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
            return UNKNOWN;
        }

        @Override
        public String getOSVersion() {
            return UNKNOWN;
        }

        @Override
        public String getClientName() {
            return UNKNOWN;
        }

        @Override
        public String getClientVersion() {
            return UNKNOWN;
        }

        @Override
        public String getClientFamily() {
            return UNKNOWN;
        }
    } // End of class DefaultClientInfo

}
