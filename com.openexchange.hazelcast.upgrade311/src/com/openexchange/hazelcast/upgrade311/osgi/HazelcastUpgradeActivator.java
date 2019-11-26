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

package com.openexchange.hazelcast.upgrade311.osgi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.osgi.framework.Version;
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

    /** The version string of the 'legacy' hazelcast version that the old cluster is using */
    private static final Version LEGACY_HAZELCAST_VERSION = new Version(3, 11, 1);

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
        LOG.info("starting bundle {}", context.getBundle());
        List<ClientConfig> clientConfigs = getConfigs(getService(ConfigurationService.class));
        if (null != clientConfigs && 0 < clientConfigs.size()) {
            UpgradedCacheListener cacheListener = new UpgradedCacheListener(clientConfigs);
            String region = UpgradedCacheListener.CACHE_REGION;
            LOG.warn("Listening to events for cache region \"{}\". " +
                "Please remember to uninstall this package once all nodes in the cluster have been upgraded.", region);
            getService(com.openexchange.caching.events.CacheEventService.class).addListener(region, cacheListener);
            this.cacheListener = cacheListener;
        }
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        LOG.info("stopping bundle {}", context.getBundle());
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
     * Gets suitable client configurations to connect to a legacy Hazelcast cluster.
     *
     * @param configService The configuration service
     * @return A non-empty list of possible client configurations, or <code>null</code> if no client configuration is possible or needed
     */
    private List<ClientConfig> getConfigs(ConfigurationService configService) throws OXException {
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
        ClientConfig alternativeConfig = new ClientConfig();
        String[] nodeProperties = { "com.openexchange.hazelcast.network.join.static.nodes", "com.openexchange.hazelcast.network.client.nodes" };
        LOG.info("Hazelcast cluster discovery is \"{}\", looking for possible addresses at {}...", join, Arrays.toString(nodeProperties));
        for (String property : nodeProperties) {
            String[] members = Strings.splitByComma(configService.getProperty(property));
            if (null != members && 0 < members.length) {
                for (String member : members) {
                    if (Strings.isNotEmpty(member)) {
                        String hostAddress;
                        try {
                            hostAddress = InetAddress.getByName(member).getHostAddress();
                        } catch (UnknownHostException e) {
                            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, property);
                        }
                        config.getNetworkConfig().addAddress(hostAddress);
                        alternativeConfig.getNetworkConfig().addAddress(hostAddress);
                    }
                }
            }
        }
        if (config.getNetworkConfig().getAddresses().isEmpty()) {
            LOG.info("No possible members to connect to found at {}, aborting initialization.", Arrays.toString(nodeProperties));
            return null;
        }
        /*
         * Group name & versioned group name
         */
        String groupName = configService.getProperty("com.openexchange.hazelcast.group.name");
        if (Strings.isEmpty(groupName)) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create("com.openexchange.hazelcast.group.name");
        }
        config.getGroupConfig().setName(groupName);
        boolean enterprise = Strings.isNotEmpty(configService.getProperty("com.openexchange.hazelcast.licenseKey"));
        alternativeConfig.getGroupConfig().setName(buildGroupName(groupName, LEGACY_HAZELCAST_VERSION, enterprise));
        /*
         * Serialization config
         */
        DynamicPortableFactoryImpl dynamicPortableFactory = new DynamicPortableFactoryImpl();
        dynamicPortableFactory.register(new PortableContextInvalidationCallableFactory());
        config.getSerializationConfig().addPortableFactory(DynamicPortableFactory.FACTORY_ID, dynamicPortableFactory);
        for (ClassDefinition classDefinition : dynamicPortableFactory.getClassDefinitions()) {
            config.getSerializationConfig().addClassDefinition(classDefinition);
            alternativeConfig.getSerializationConfig().addClassDefinition(classDefinition);
        }
        List<ClientConfig> clientConfigs = new ArrayList<ClientConfig>(2);
        clientConfigs.add(config);
        if (false == groupName.equals(alternativeConfig.getGroupConfig().getName())) {
            clientConfigs.add(alternativeConfig);
        }
        return clientConfigs;
    }

    /**
     * Constructs the effective group name to use in the cluster group configuration for Hazelcast. The full group name is constructed
     * based on the configured group name prefix, optionally appended with a version identifier string of the underlying Hazelcast library.
     * <p/>
     * This needs to be done to form separate Hazelcast clusters during rolling upgrades of the nodes with incompatible Hazelcast
     * libraries.
     *
     * @param groupName The configured cluster group name
     * @param version The version of the Hazelcast library
     * @param enterprise <code>true</code> if enterprise features are available for rolling upgrades, <code>false</code>, otherwise
     * @return The full cluster group name
     */
    private static String buildGroupName(String groupName, Version version, boolean enterprise) {
        if (enterprise) {
            return groupName;
        }
        return new StringBuilder(20).append(groupName).append('-').append(version.getMajor()).append('.').append(version.getMinor()).toString();
    }

}
