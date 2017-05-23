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

package com.openexchange.groupware.settings.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
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

/**
 * {@link ShardingSubdomains}
 * 
 * Provide an array of sharding subdomains determined for each host via as-config.yml.
 * Attention, to use cookies correctly, the following properties have to be set also
 * for the host: <br><br>
    com.openexchange.cookie.domain.enabled<br>
    com.openexchange.cookie.domain<br>
    com.openexchange.cookie.domain.prefixWithDot<br>
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
                        e.printStackTrace();
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
