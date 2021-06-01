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

package com.openexchange.hazelcast.upgrade381.osgi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.legacy.DynamicPortableFactory;
import com.openexchange.legacy.DynamicPortableFactoryImpl;
import com.openexchange.legacy.PortableContextInvalidationCallableFactory;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link HazelcastUpgradeActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class HazelcastUpgradeActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HazelcastUpgradeActivator.class);

    private UpgradedCacheListener cacheListener;

    /**
     * Initializes a new {@link HazelcastUpgradeActivator}.
     */
    public HazelcastUpgradeActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { com.openexchange.caching.events.CacheEventService.class, ConfigurationService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        LOG.info("starting bundle: \"com.openexchange.hazelcast.upgrade381\"");
        ClientConfig clientConfig = getConfig(getService(ConfigurationService.class));
        if (null != clientConfig) {
            UpgradedCacheListener cacheListener = new UpgradedCacheListener(clientConfig);
            String region = UpgradedCacheListener.CACHE_REGION;
            LOG.warn("Listening to events for cache region \"{}\". " +
                "Please remember to uninstall this package once all nodes in the cluster have been upgraded.", region);
            getService(com.openexchange.caching.events.CacheEventService.class).addListener(region, cacheListener);
            this.cacheListener = cacheListener;
        }
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        org.slf4j.LoggerFactory.getLogger(HazelcastUpgradeActivator.class).info("stopping bundle: \"com.openexchange.hazelcast.upgrade381\"");
        UpgradedCacheListener cacheListener = this.cacheListener;
        if (null != cacheListener) {
            CacheEventService cacheEventService = getService(com.openexchange.caching.events.CacheEventService.class);
            if (null != cacheEventService) {
                cacheEventService.removeListener(cacheListener);
            }
            this.cacheListener = null;
        }
        super.stopBundle();
    }

    /**
     * Gets a suitable client configuration to connect to a legacy Hazelcast cluster.
     *
     * @param configService The configuration service
     * @return The
     * @throws OXException
     */
    private ClientConfig getConfig(ConfigurationService configService) throws OXException {
        /*
         * Check if enabled
         */
        if (false == configService.getBoolProperty("com.openexchange.hazelcast.enabled", true)) {
            LOG.info("Hazelcast is disabled by configuration, aborting initialization.");
            return null;
        }
        String join = configService.getProperty("com.openexchange.hazelcast.network.join", "empty");
        if ("empty".equalsIgnoreCase(join)) {
            LOG.info("Hazelcast cluster discovery is \"empty\", aborting initialization.");
            return null;
        }
        if (configService.getBoolProperty("com.openexchange.hazelcast.network.symmetricEncryption", false)) {
            LOG.warn("Can't connect to Hazelcast cluster with symmetric encryption enabled, aborting initialization.");
            return null;
        }
        /*
         * Network config
         */
        ClientConfig config = new ClientConfig();
        String[] nodeProperties = { "com.openexchange.hazelcast.network.join.static.nodes", "com.openexchange.hazelcast.network.client.nodes" };
        LOG.info("Hazelcast cluster discovery is \"{}\", looking for possible addresses at {}...", join, Arrays.toString(nodeProperties));
        for (String property : nodeProperties) {
            String[] members = Strings.splitByComma(configService.getProperty(property));
            if (null != members && 0 < members.length) {
                for (String member : members) {
                    if (Strings.isNotEmpty(member)) {
                        try {
                            config.getNetworkConfig().addAddress(InetAddress.getByName(member).getHostAddress());
                        } catch (UnknownHostException e) {
                            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, property);
                        }
                    }
                }
            }
        }
        if (config.getNetworkConfig().getAddresses().isEmpty()) {
            LOG.info("No possible members to connect to found at {}, aborting initialization.", Arrays.toString(nodeProperties));
            return null;
        }
        /*
         * Group name & password
         */
        String groupName = configService.getProperty("com.openexchange.hazelcast.group.name");
        if (Strings.isEmpty(groupName)) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create("com.openexchange.hazelcast.group.name");
        }
        config.getGroupConfig().setName(groupName);
        String groupPassword = configService.getProperty("com.openexchange.hazelcast.group.password");
        if (Strings.isNotEmpty(groupPassword)) {
            config.getGroupConfig().setPassword(groupPassword);
        }
        /*
         * Serialization config
         */
        DynamicPortableFactoryImpl dynamicPortableFactory = new DynamicPortableFactoryImpl();
        dynamicPortableFactory.register(new PortableContextInvalidationCallableFactory());
        config.getSerializationConfig().addPortableFactory(DynamicPortableFactory.FACTORY_ID, dynamicPortableFactory);
        for (ClassDefinition classDefinition : dynamicPortableFactory.getClassDefinitions()) {
            config.getSerializationConfig().addClassDefinition(classDefinition);
        }
        return config;
    }

}
