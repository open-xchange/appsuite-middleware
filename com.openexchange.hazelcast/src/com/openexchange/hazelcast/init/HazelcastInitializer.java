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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.hazelcast.init;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.osgi.framework.BundleException;
import com.hazelcast.config.Config;
import com.hazelcast.config.Join;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Hazelcasts;
import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleEvent.LifecycleState;
import com.hazelcast.core.LifecycleListener;
import com.hazelcast.impl.GroupProperties;
import com.openexchange.config.ConfigurationService;
import com.openexchange.hazelcast.HazelcastMBean;
import com.openexchange.hazelcast.init.HazelcastInitializer.InitMode;
import com.openexchange.hazelcast.osgi.HazelcastActivator;
import com.openexchange.tools.strings.TimeSpanParser;

/**
 * {@link HazelcastInitializer}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HazelcastInitializer {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(HazelcastInitializer.class);

    /**
     * Hazelcast initialization result.
     */
    public static enum InitMode {
        INITIALIZED, RE_INITIALIZED, NONE;
    }

    /**
     * The {@code AtomicReference} for {@code HazelcastInstance}.
     */
    public static final AtomicReference<HazelcastInstance> REF_HAZELCAST_INSTANCE = new AtomicReference<HazelcastInstance>();

    /**
     * The activator.
     */
    private final HazelcastActivator activator;

    /**
     * Initializes a new {@link HazelcastInitializer}.
     */
    public HazelcastInitializer(final HazelcastActivator activator) {
        super();
        this.activator = activator;
    }

    /**
     * Re-initializes the {@link HazelcastInstance} by removing the supplied nodes from the list of known members.
     * 
     * @param nodes The nodes that should be removed
     * @param force <code>true</code> to enforce a restart of a running {@code HazelcastInstance}; otherwise <code>false</code>
     * @param logger The logger instance
     * @return {@link InitMode#RE_INITIALIZED} if <tt>HazelcastInstance</tt> has been re-initialized by this call; otherwise
     *         {@link InitMode.NONE} if no configuration changes were necessary
     */
    public InitMode remove(final List<InetAddress> nodes, final boolean force, final Log logger) {
        if (null == nodes || nodes.isEmpty()) {
            return InitMode.NONE;
        }
        synchronized (this) {
            final HazelcastInstance hazelcastInstance = REF_HAZELCAST_INSTANCE.get();
            if (null == hazelcastInstance) {
                return InitMode.NONE;
            }
            final Config config = hazelcastInstance.getConfig();
            /*
             * Remove from existing network configuration
             */
            final Set<String> members = resolve2Members(nodes);
            if (members.isEmpty()) {
                return InitMode.NONE;
            }
            final Join join = config.getNetworkConfig().getJoin();
            final TcpIpConfig tcpIpConfig = join.getTcpIpConfig();
            final Set<String> cur = new LinkedHashSet<String>(tcpIpConfig.getMembers());
            if (!cur.removeAll(members)) {
                return InitMode.NONE;
            }
            if (logger.isInfoEnabled()) {
                logger.info("\nHazelcast:\n\tRe-initializing Hazelcast instance:\n\tExisting members: " + cur + 
                    "\n\tDisappeared members: " + members + "\n");
            }
            final long st = System.currentTimeMillis();
            tcpIpConfig.clear();
            tcpIpConfig.setMembers(new ArrayList<String>(cur));
            if (false == force) {
                logger.info("\nHazelcast:\n\tRe-initialized without restart in " + (System.currentTimeMillis() - st) + "msec.\n");
                return InitMode.RE_INITIALIZED;
            }
            hazelcastInstance.getLifecycleService().restart();
            if (logger.isInfoEnabled()) {
                logger.info("\nHazelcast:\n\tRe-started in " + (System.currentTimeMillis() - st) + "msec.\n");
            }
            return InitMode.RE_INITIALIZED;
        }
    }

    /**
     * (Re-)Initializes the {@link HazelcastInstance} by adding the supplied nodes to the list of known members.
     * 
     * @param nodes The pre-known nodes
     * @param force <code>true</code> to enforce a restart of a running {@code HazelcastInstance}; otherwise <code>false</code>
     * @param stamp The start-up time stamp
     * @param logger The logger instance
     * @return {@link InitMode#RE_INITIALIZED} if <tt>HazelcastInstance</tt> has been (re-)initialized by this call; otherwise
     *         {@link InitMode.NONE} if no configuration changes were necessary
     */
    public InitMode init(final List<InetAddress> nodes, final boolean force, final long stamp, final Log logger) {
        synchronized (this) {
            final HazelcastInstance prevHazelcastInstance = REF_HAZELCAST_INSTANCE.get();
            if (null != prevHazelcastInstance) {
                final long st = System.currentTimeMillis();
                final Config config = prevHazelcastInstance.getConfig();
                final InitMode configMode = configureNetworkJoin(nodes, true, config, logger);
                if (null != configMode && InitMode.NONE.equals(configMode)) {
                    return InitMode.NONE;
                }
                if (false == force) {
                    logger.info("\nHazelcast:\n\tRe-initialized without restart in " + (System.currentTimeMillis() - st) + "msec.\n");
                    return InitMode.RE_INITIALIZED;
                }
                prevHazelcastInstance.getLifecycleService().restart();
                if (logger.isInfoEnabled()) {
                    logger.info("\nHazelcast:\n\tRe-started in " + (System.currentTimeMillis() - st) + "msec.\n");
                }
                return InitMode.RE_INITIALIZED;
            }
            /*
             * Create Hazelcast configuration from properties
             */
            Config config = createConfig();
            configureNetworkJoin(nodes, false, config, logger);
            /*
             * Create appropriate Hazelcast instance from configuration
             */
            LOG.debug("Creating new hazelcast instance...");
            long hzStart = System.currentTimeMillis(); 
            final HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
            LOG.info("New hazelcast instance successfully created in " + (System.currentTimeMillis() - hzStart) + "msec.\n");
            hazelcastInstance.getLifecycleService().addLifecycleListener(new LifecycleListener() {

                @Override
                public void stateChanged(final LifecycleEvent event) {
                    final LifecycleState state = event.getState();
                    if (LifecycleState.RESTARTING.equals(state)) {
                        Hazelcasts.setPaused(true);
                    } else if (LifecycleState.RESTARTED.equals(state)) {
                        Hazelcasts.setPaused(false);
                    }
                }
            });
            activator.registerService(HazelcastInstance.class, hazelcastInstance);
            REF_HAZELCAST_INSTANCE.set(hazelcastInstance);
            if (logger.isInfoEnabled()) {
                logger.info("\nHazelcast:\n\tStarted in " + (System.currentTimeMillis() - stamp) + "msec.\n");
            }
            /*
             * Create dummy map for JMX-based communication
             */
            final MapConfig mapConfig = new MapConfig();
            mapConfig.setName(HazelcastMBean.MAP_NAME);
            mapConfig.setBackupCount(MapConfig.DEFAULT_BACKUP_COUNT);
            mapConfig.setEvictionPercentage(MapConfig.DEFAULT_EVICTION_PERCENTAGE);
            mapConfig.setEvictionPolicy("LRU");
            mapConfig.setMaxIdleSeconds(60);
            mapConfig.setTimeToLiveSeconds(300);
            hazelcastInstance.getConfig().addMapConfig(mapConfig);
            return InitMode.INITIALIZED;
        }
    }

    private InitMode configureNetworkJoin(final List<InetAddress> nodes, final boolean append, final Config config, final Log logger) {
        /*
         * Get reference to network join
         */
        final Join join = config.getNetworkConfig().getJoin();
        if (append) {
            /*
             * Append to existing network configuration
             */
            final Set<String> members = resolve2Members(nodes);
            if (members.isEmpty()) {
                if (logger.isInfoEnabled()) {
                    logger.info("\nHazelcast:\n\tAbort re-configuration of Hazelcast instance:\n\tNo additional members\n");
                }
                return InitMode.NONE;
            }
            final TcpIpConfig tcpIpConfig = join.getTcpIpConfig();
            final Set<String> cur = new LinkedHashSet<String>(tcpIpConfig.getMembers());
            if (cur.containsAll(members)) {
                // Already contained...
                if (logger.isInfoEnabled()) {
                    logger.info("\nHazelcast:\n\tAbort re-configuration of Hazelcast instance:\n\tNo additional members\n");
                }
                return InitMode.NONE;
            }
            if (logger.isInfoEnabled()) {
                logger.info("\nHazelcast:\n\tRe-configuring Hazelcast instance:\n\tExisting members: " + cur + "\n\tNew members: " + 
                    members + "\n");
            }
            if (!cur.addAll(members)) {
                if (logger.isInfoEnabled()) {
                    logger.info("\nHazelcast:\n\tAbort re-configuration of Hazelcast instance:\n\tNo additional members\n");
                }
                return InitMode.NONE;
            }
            tcpIpConfig.clear();
            tcpIpConfig.setMembers(new ArrayList<String>(cur));
            return InitMode.RE_INITIALIZED;
        } else {
            final Set<String> members = resolve2Members(nodes);
            if (!members.isEmpty()) {
                if (logger.isInfoEnabled()) {
                    logger.info("\nHazelcast:\n\tConfiguring Hazelcast instance:\n\tInitial members: " + members + "\n");
                }
                join.getTcpIpConfig().setMembers(new ArrayList<String>(members));
                return InitMode.INITIALIZED;
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("\nHazelcast:\n\tConfiguring Hazelcast instance:\n\tNo initial members\n");
                }
                join.getTcpIpConfig().clear();
                return InitMode.INITIALIZED;
            }
        }
    }

    /**
     * Creates a new hazelcast configuration, setting the relevant properties as read by the configuration service.
     * 
     * @return A new pre-configured hazelcast config object
     */
    private Config createConfig() {
        Config config = new Config();
        ConfigurationService configService = activator.getService(ConfigurationService.class);
        if (null == configService) {
            throw new IllegalStateException(new BundleException("Unable to access configuration service.", 
                BundleException.ACTIVATOR_ERROR));
        }
        /*
         * cluster group name
         */
        String groupName = configService.getProperty("com.openexchange.cluster.name");
        if (isEmpty(groupName)) {
            throw new IllegalStateException(new BundleException(
                "Cluster name is mandatory. Please set a valid identifier through property \"com.openexchange.cluster.name\".", 
                BundleException.ACTIVATOR_ERROR));
        } else if ("ox".equalsIgnoreCase(groupName)) {
            LOG.warn("\n\tThe configuration value for \"com.openexchange.cluster.name\" has not been changed from it's default value "
                + "\"ox\". Please do so to make this warning disappear.\n");
        }
        config.getGroupConfig().setName(groupName).setPassword("YXV0b2JhaG4=");
        /*
         * JMX
         */
        if (configService.getBoolProperty("com.openexchange.hazelcast.jmx", true)) {
            config.setProperty(GroupProperties.PROP_ENABLE_JMX, "true").setProperty(GroupProperties.PROP_ENABLE_JMX_DETAILED, "true");
        }
        /*
         * limit number of redos
         */
        config.setProperty(GroupProperties.PROP_REDO_GIVE_UP_THRESHOLD, "10");
        /*
         * configure merge run intervals 
         */
        String mergeFirstRunDelay = configService.getProperty("com.openexchange.hazelcast.mergeFirstRunDelay", "120s");
        config.setProperty(GroupProperties.PROP_MERGE_FIRST_RUN_DELAY_SECONDS, 
            String.valueOf(TimeSpanParser.parseTimespan(mergeFirstRunDelay).longValue() / 1000));
        String mergeRunDelay = configService.getProperty("com.openexchange.hazelcast.mergeRunDelay", "120s");
        config.setProperty(GroupProperties.PROP_MERGE_NEXT_RUN_DELAY_SECONDS, 
            String.valueOf(TimeSpanParser.parseTimespan(mergeRunDelay).longValue() / 1000));
        /*
         * set interfaces
         */
        String interfaces = configService.getProperty("com.openexchange.hazelcast.interfaces");
        if (false == isEmpty(interfaces)) {
            String[] ips = interfaces.split(" *, *");
            if (null != ips && 0 < ips.length) {
                config.getNetworkConfig().getInterfaces().setInterfaces(Arrays.asList(ips)).setEnabled(true);
            }
        }
        /*
         * Disable: Multicast, AWS and ...
         */
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(false);
        /*-
         * ... enable: TCP-IP
         * 
         * http://code.google.com/p/hazelcast/wiki/ConfigFullTcpIp
         */
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true).setConnectionTimeoutSeconds(10);
        return config;
    }

    protected static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    private static final Pattern SPLIT = Pattern.compile("\\%");

    private static Set<String> resolve2Members(final List<InetAddress> nodes) {
        Set<String> localHost = getLocalHost(); 
        final Set<String> members = new LinkedHashSet<String>(nodes.size());
        for (final InetAddress inetAddress : nodes) {
            final String[] addressArgs = SPLIT.split(inetAddress.getHostAddress(), 0);
            for (final String address : addressArgs) {
                if (false == localHost.contains(address)) {
                    members.add(address);
                }
            }
        }
        return members;
    }

    private static Set<String> getLocalHost() {
        try {
            final Set<String> set = new HashSet<String>(2);
            final InetAddress inetAddress = InetAddress.getLocalHost();
            set.add(inetAddress.getCanonicalHostName());
            set.add(inetAddress.getHostAddress());
            return set;
        } catch (final UnknownHostException e) {
            return Collections.emptySet();
        }
    }

}
