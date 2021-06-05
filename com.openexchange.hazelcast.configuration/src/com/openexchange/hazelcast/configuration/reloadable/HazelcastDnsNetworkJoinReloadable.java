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

import org.slf4j.Logger;
import com.hazelcast.config.Config;
import com.hazelcast.config.TcpIpConfig;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.hazelcast.configuration.KnownNetworkJoin;
import com.openexchange.hazelcast.configuration.internal.HazelcastConfigurationServiceImpl;

/**
 * {@link HazelcastDnsNetworkJoinReloadable}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class HazelcastDnsNetworkJoinReloadable implements Reloadable {

    private final HazelcastConfigurationServiceImpl hzConfiguration;

    /**
     * Initializes a new {@link HazelcastDnsNetworkJoinReloadable}.
     */
    public HazelcastDnsNetworkJoinReloadable(HazelcastConfigurationServiceImpl hzConfiguration) {
        super();
        this.hzConfiguration = hzConfiguration;
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
            if (join != KnownNetworkJoin.DNS) {
                // DNS network join is not configured; leave...
            }
        }

        TcpIpConfig tcpIpConfig = config.getNetworkConfig().getJoin().getTcpIpConfig();
        if (false == tcpIpConfig.isEnabled()) {
            // Static network join is not enabled; leave...
            return;
        }

        Logger logger = org.slf4j.LoggerFactory.getLogger(HazelcastDnsNetworkJoinReloadable.class);

        try {
            hzConfiguration.reinitializeDnsLookUp(config, configService);
            logger.info("Applied changed DNS look-up configuration to Hazelcast.");
        } catch (Exception e) {
            logger.error("Failed to apply changed DNS look-up configuration to Hazelcast.", e);
        }
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties(
            "com.openexchange.hazelcast.network.join.dns.domainNames",
            "com.openexchange.hazelcast.network.join.dns.resolverHost",
            "com.openexchange.hazelcast.network.join.dns.resolverPort",
            "com.openexchange.hazelcast.network.join.dns.refreshMillis");
    }

}
