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
import java.util.Collection;
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
import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleEvent.LifecycleState;
import com.hazelcast.core.LifecycleListener;
import com.openexchange.cluster.discovery.ClusterMember;
import com.openexchange.exception.OXException;
import com.openexchange.hazelcast.Hazelcasts;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.hazelcast.init.HazelcastInitializer.InitMode;
import com.openexchange.hazelcast.mbean.HazelcastMBean;
import com.openexchange.hazelcast.osgi.HazelcastActivator;

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
    public InitMode remove(final List<ClusterMember> nodes, final boolean force, final Log logger) {
        if (null == nodes || nodes.isEmpty()) {
            return InitMode.NONE;
        }
        final HazelcastInstance hazelcastInstance = REF_HAZELCAST_INSTANCE.get();
        if (null == hazelcastInstance) {
            return InitMode.NONE;
        }
        synchronized (this) {
            final Config config = hazelcastInstance.getConfig();
            /*
             * Remove from existing network configuration
             */
            final Set<String> members = resolve2Members(nodes, config.getNetworkConfig().getInterfaces().getInterfaces());
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
    public InitMode init(final List<ClusterMember> nodes, final boolean force, final long stamp, final Log logger) {
        synchronized (this) {
            final HazelcastInstance prevHazelcastInstance = REF_HAZELCAST_INSTANCE.get();
            final boolean infoEnabled = logger.isInfoEnabled();
            if (null != prevHazelcastInstance) {
                final long st = System.currentTimeMillis();
                final Config config = prevHazelcastInstance.getConfig();
                final InitMode configMode = configureNetworkJoin(nodes, true, config, logger);
                if (null != configMode && InitMode.NONE.equals(configMode)) {
                    return InitMode.NONE;
                }
                if (false == force) {
                    if (infoEnabled) {
                        logger.info("\nHazelcast:\n\tRe-initialized without restart in " + (System.currentTimeMillis() - st) + "msec.\n");
                    }
                    return InitMode.RE_INITIALIZED;
                }
                prevHazelcastInstance.getLifecycleService().restart();
                if (infoEnabled) {
                    logger.info("\nHazelcast:\n\tRe-started in " + (System.currentTimeMillis() - st) + "msec.\n");
                }
                return InitMode.RE_INITIALIZED;
            }
            /*
             * Create Hazelcast configuration from properties
             */
            HazelcastConfigurationService configService = activator.getService(HazelcastConfigurationService.class);
            if (null == configService) {
                final BundleException bundleException = new BundleException("Unable to access configuration service.", BundleException.ACTIVATOR_ERROR);
                throw wrap(bundleException);
            }
            Config config = null;
            try {
                config = configService.getConfig();
            } catch (OXException e) {
                final BundleException bundleException = new BundleException("Unable to get hazelcast configuration: " + e.getPlainLogMessage(), BundleException.ACTIVATOR_ERROR, e);
                throw wrap(bundleException);
            }
            configureNetworkJoin(nodes, false, config, logger);
            /*
             * Create appropriate Hazelcast instance from configuration
             */
            if (infoEnabled) {
                LOG.info("\nHazelcast:\n\tCreating new hazelcast instance...");
            }
            long hzStart = infoEnabled ? System.currentTimeMillis() : 0L;
            final HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
            if (infoEnabled) {
                LOG.info("\nHazelcast:\n\tNew hazelcast instance successfully created in " + (System.currentTimeMillis() - hzStart) + "msec.\n");
            }
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
            if (infoEnabled) {
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

    private InitMode configureNetworkJoin(final List<ClusterMember> nodes, final boolean append, final Config config, final Log logger) {
        /*
         * Get reference to network join
         */
        final Join join = config.getNetworkConfig().getJoin();
        if (append) {
            /*
             * Append to existing network configuration
             */
            final Set<String> members = resolve2Members(nodes, config.getNetworkConfig().getInterfaces().getInterfaces());
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
        }
        // Don't append
        final Set<String> members = resolve2Members(nodes, config.getNetworkConfig().getInterfaces().getInterfaces());
        if (members.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("\nHazelcast:\n\tConfiguring Hazelcast instance:\n\tNo initial members\n");
            }
            join.getTcpIpConfig().clear();
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("\nHazelcast:\n\tConfiguring Hazelcast instance:\n\tInitial members: " + members + "\n");
            }
            join.getTcpIpConfig().setMembers(new ArrayList<String>(members));
        }
        return InitMode.INITIALIZED;
    }

    private static final Pattern SPLIT = Pattern.compile("\\%");

    private static Set<String> resolve2Members(final List<ClusterMember> nodes, Collection<String> excludedHosts) {
        Set<String> excluded = new HashSet<String>(excludedHosts);
        excluded.addAll(getLocalHost());
        final Set<String> members = new LinkedHashSet<String>(nodes.size());
        final StringBuilder sb = new StringBuilder(32);
        for (final ClusterMember clusterMember : nodes) {
            final int port = clusterMember.getPort();
            final String[] addressArgs = SPLIT.split(clusterMember.getInetAddress().getHostAddress(), 0);
            for (final String address : addressArgs) {
                if (false == excluded.contains(address)) {
                    sb.setLength(0);
                    members.add(port > 0 ? sb.append(address).append(':').append(port).toString() : address);
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

    private RuntimeException wrap(final BundleException bundleException) {
        return new IllegalStateException(bundleException);
    }

}
