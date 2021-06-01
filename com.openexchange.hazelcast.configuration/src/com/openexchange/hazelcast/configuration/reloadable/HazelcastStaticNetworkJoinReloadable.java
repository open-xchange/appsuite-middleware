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

package com.openexchange.hazelcast.configuration.reloadable;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import com.hazelcast.config.Config;
import com.hazelcast.config.TcpIpConfig;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.hazelcast.configuration.KnownNetworkJoin;
import com.openexchange.hazelcast.configuration.internal.HazelcastConfigurationServiceImpl;
import com.openexchange.java.Strings;

/**
 * {@link HazelcastStaticNetworkJoinReloadable}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class HazelcastStaticNetworkJoinReloadable implements Reloadable {

    private final HazelcastConfigurationServiceImpl hzConfiguration;
    private final String propertyName;

    /**
     * Initializes a new {@link HazelcastStaticNetworkJoinReloadable}.
     */
    public HazelcastStaticNetworkJoinReloadable(HazelcastConfigurationServiceImpl hzConfiguration) {
        super();
        this.hzConfiguration = hzConfiguration;
        propertyName = "com.openexchange.hazelcast.network.join.static.nodes";
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        Config config = hzConfiguration.getConfigDirect();
        if (null == config) {
            // Hazelcast has not yet been initialized
            return;
        }

        {
            String sJoin = configService.getProperty("com.openexchange.hazelcast.network.join", KnownNetworkJoin.EMPTY.getIdentifier()).trim();
            KnownNetworkJoin join = KnownNetworkJoin.networkJoinFor(sJoin);
            if (join != KnownNetworkJoin.STATIC) {
                // Static network join is not configured; leave...
            }
        }

        TcpIpConfig tcpIpConfig = config.getNetworkConfig().getJoin().getTcpIpConfig();
        if (false == tcpIpConfig.isEnabled()) {
            // Static network join is not enabled; leave...
            return;
        }

        // Read & parse members
        String[] members = Strings.splitByComma(configService.getProperty(propertyName));

        // Apply if there are any
        Logger logger = org.slf4j.LoggerFactory.getLogger(HazelcastStaticNetworkJoinReloadable.class);
        if (null == members || 0 >= members.length) {
            logger.warn("Empty value for property \"{}\". Hazelcast TCP/IP network configuration will not be changed.", propertyName);
            return;
        }

        // Clear TCP/IP network configuration & add the members one-by-one
        tcpIpConfig.clear();
        for (String member : members) {
            if (Strings.isNotEmpty(member)) {
                try {
                    tcpIpConfig.addMember(InetAddress.getByName(member).getHostAddress());
                } catch (UnknownHostException e) {
                    logger.error("Failed to add member {}", member, e);
                }
            }
        }
        logger.info("Applied changed static members to Hazelcast TCP/IP network configuration.");
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties(propertyName);
    }

}
