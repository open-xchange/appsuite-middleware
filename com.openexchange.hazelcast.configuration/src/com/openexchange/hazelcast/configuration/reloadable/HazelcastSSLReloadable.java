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
import org.slf4j.LoggerFactory;
import com.hazelcast.config.Config;
import com.hazelcast.instance.BuildInfoProvider;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadables;
import com.openexchange.hazelcast.configuration.internal.HazelcastConfigurationServiceImpl;
import com.openexchange.hazelcast.configuration.ssl.HazelcastSSLFactory;

/**
 * {@link HazelcastSSLReloadable}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class HazelcastSSLReloadable implements ForcedReloadable {

    private final static Logger LOGGER = LoggerFactory.getLogger(HazelcastSSLReloadable.class);

    private final HazelcastConfigurationServiceImpl hzConfiguration;

    /**
     * Initializes a new {@link HazelcastSSLReloadable}.
     *
     */
    public HazelcastSSLReloadable(HazelcastConfigurationServiceImpl hzConfiguration) {
        super();
        this.hzConfiguration = hzConfiguration;
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        if (false == BuildInfoProvider.getBuildInfo().isEnterprise()) {
            return;
        }
        Config config = hzConfiguration.getConfigDirect();
        if (null == config) {
            // Hazelcast has not yet been initialized
            return;
        }

        Object factory = config.getNetworkConfig().getSSLConfig().getFactoryImplementation();
        if (null != factory && HazelcastSSLFactory.class.isAssignableFrom(factory.getClass())) {
            HazelcastSSLFactory hazelcastSSLFactory = (HazelcastSSLFactory) factory;
            try {
                hazelcastSSLFactory.init(HazelcastSSLFactory.getPropertiesFromService(configService));
            } catch (Exception e) {
                LOGGER.error("Unable to reload {}.", HazelcastSSLFactory.class.getSimpleName(), e);
            }
        }
    }

    @Override
    public Interests getInterests() {
        // Force reloadable for key stores
        return Reloadables.getInterestsForAll();
    }
}
