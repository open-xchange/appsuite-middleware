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

package com.openexchange.groupware.settings.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.ReadOnlyValue;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.jslob.ConfigTreeEquivalent;
import com.openexchange.log.LogProperties;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * {@link ShardingSubdomains}
 * 
 * Provide an array of sharding subdomains determined for each host via as-config.yml.
 * Attention, to use cookies correctly, the following properties have to be set also
 * for the host: <br><br>
 * com.openexchange.cookie.domain.enabled<br>
 * com.openexchange.cookie.domain<br>
 * com.openexchange.cookie.domain.prefixWithDot<br>
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.8.4
 */
public class ShardingSubdomains implements PreferencesItemService, ConfigTreeEquivalent {

    private static final String SHARDING_SUBDOMAINS = "shardingSubdomains";

    @Override
    public String getConfigTreePath() {
        return SHARDING_SUBDOMAINS;
    }

    @Override
    public String getJslobPath() {
        return "io.ox/core//shardingSubdomains";
    }

    @Override
    public String[] getPath() {
        return new String[] { SHARDING_SUBDOMAINS };
    }

    @Override
    public IValueHandler getSharedValue() {
        return new ReadOnlyValue() {

            private List<String> shardingSubdomains;

            @Override
            public boolean isAvailable(UserConfiguration userConfig) {
                List<Map<String, Object>> customHostConfigurations = new ArrayList<>();
                shardingSubdomains = new ArrayList<>();
                ServerConfigService serverConfigService = ServerServiceRegistry.getInstance().getService(ServerConfigService.class);
                if (serverConfigService != null) {
                    try {
                        customHostConfigurations = serverConfigService.getCustomHostConfigurations(LogProperties.getHostName(), userConfig.getUserId(), userConfig.getContext().getContextId());
                    } catch (OXException e) {
                        LoggerFactory.getLogger(ShardingSubdomains.class).error("Unable to retrieve sharding subdomains.", e);
                    }
                }
                return areShardingHostsAvailable(customHostConfigurations);
            }

            @SuppressWarnings("unchecked")
            private boolean areShardingHostsAvailable(List<Map<String, Object>> customHostConfigurations) {
                for (Map<String, Object> map : customHostConfigurations) {
                    if (map.containsKey(SHARDING_SUBDOMAINS)) {
                        shardingSubdomains = (List<String>) map.get(SHARDING_SUBDOMAINS);
                    }
                }
                return shardingSubdomains.isEmpty() ? false : true;
            }

            @Override
            public void getValue(Session session, Context ctx, User user, UserConfiguration userConfig, Setting setting) throws OXException {
                for (String host : shardingSubdomains) {
                    setting.addMultiValue(host);
                }
            }
        };
    }
}
