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
