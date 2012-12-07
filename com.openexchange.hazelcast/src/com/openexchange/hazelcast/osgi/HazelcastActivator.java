
package com.openexchange.hazelcast.osgi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
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
import com.openexchange.cluster.discovery.ClusterDiscoveryService;
import com.openexchange.cluster.discovery.ClusterListener;
import com.openexchange.config.ConfigurationService;
import com.openexchange.hazelcast.HazelcastMBean;
import com.openexchange.hazelcast.osgi.HazelcastActivator.InitMode;
import com.openexchange.management.ManagementService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceContainer;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.strings.TimeSpanParser;

/**
 * {@link HazelcastActivator} - The activator for Hazelcast bundle (registers a {@link HazelcastInstance} for this JVM)
 * <p>
 * When should you add node?<br>
 * 1. You reached the limits of your CPU or RAM.<br>
 * 2. You reached the limits of GC. You started seeing full-GC
 * <p>
 * When should you stop adding nodes? Should you have 10, 30, 50, 100, or 1000 nodes?<br>
 * 1. You reached the limits of your network. Your switch is not able to handle the amount of data passed around.<br>
 * 2. You reached the limits of the way application utilizing Hazelcast.<br>
 * Adding node is not increasing your total throughput and not reducing the latency.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HazelcastActivator extends HousekeepingActivator {

    /**
     * The logger for HazelcastActivator.
     */
    protected static final Log LOG = com.openexchange.log.Log.loggerFor(HazelcastActivator.class);

    /**
     * The {@code AtomicReference} for {@code HazelcastInstance}.
     */
    public static final AtomicReference<HazelcastInstance> REF_HAZELCAST_INSTANCE = new AtomicReference<HazelcastInstance>();

    /**
     * The {@code AtomicReference} for {@code ClusterDiscoveryService}.
     */
    protected final AtomicReference<ClusterDiscoveryService> clusterDiscoveryServiceReference;

    /**
     * Initializes a new {@link HazelcastActivator}.
     */
    public HazelcastActivator() {
        super();
        clusterDiscoveryServiceReference = new AtomicReference<ClusterDiscoveryService>();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ConfigurationService.class, TimerService.class };
    }

    /** Gets the ranking */
    protected static int getRanking(final ServiceReference<?> reference) {
        final Object property = reference.getProperty(Constants.SERVICE_RANKING);
        if (null == property) {
            return 0;
        }
        return ((Integer) property).intValue();
    }

    @Override
    protected void startBundle() throws Exception {
        final Log logger = com.openexchange.log.Log.loggerFor(HazelcastActivator.class);
        final ConfigurationService service = getService(ConfigurationService.class);
        if (null != service && !service.getBoolProperty("com.openexchange.hazelcast.enabled", true)) {
            logger.info("Startup of bundle disabled: com.openexchange.hazelcast");
            return;
        }
        final boolean infoEnabled = logger.isInfoEnabled();
        /*-
         * Look-up discovery service & obtain its addresses of known nodes in a cluster
         * 
         * Configure Hazelcast for full TCP/IP cluster
         * (see http://www.hazelcast.com/documentation.jsp#Config)
         * 
         * If multicast is not preferred way of discovery for your environment, then you can configure Hazelcast for full TCP/IP cluster.
         * As configuration below shows, while enable attribute of multicast is set to false, TCP/IP has to be set to true.
         * For the none-multicast option, all or subset of cluster members' host names and/or IP addresses must be listed.
         * 
         * Note that all of the cluster members don't have to be listed there but at least one of them has to be active in cluster when a
         * new member joins.
         */
        final BundleContext context = this.context;
        /*-
         * Wait for at least one via ClusterListener
         * 
         * Add cluster listener to manage appearing/disappearing nodes
         */
        final ClusterListener clusterListener = new HazelcastClusterListener(this, System.currentTimeMillis(), logger);
        registerService(ClusterListener.class, clusterListener);
        // Trackers
        track(ManagementService.class, new ManagementRegisterer(context));
        track(ClusterDiscoveryService.class, new ServiceTrackerCustomizer<ClusterDiscoveryService, ClusterDiscoveryService>() {

            private final boolean isSingleton = true;

            private final LinkedList<ServiceContainer<ClusterDiscoveryService>> deactivated = 
                new LinkedList<ServiceContainer<ClusterDiscoveryService>>();

            private int clusterDiscoveryServiceRanking = 0;

            private ClusterDiscoveryService clusterDiscoveryService = null;

            @Override
            public ClusterDiscoveryService addingService(final ServiceReference<ClusterDiscoveryService> reference) {
                if (isSingleton) {
                    final ClusterDiscoveryService discovery = context.getService(reference);
                    if (!clusterDiscoveryServiceReference.compareAndSet(null, discovery)) {
                        final com.openexchange.java.StringAllocator msg = new com.openexchange.java.StringAllocator();
                        msg.append("\n\t").append(ClusterDiscoveryService.class.getName()).append(" is a singleton service!");
                        msg.append("\n\tThis service is already tracked as \"").append(
                            clusterDiscoveryServiceReference.get().getClass().getName()).append("\".");
                        msg.append("\n\tDenying \"").append(discovery.getClass().getName()).append("\".");
                        final BundleException be = new BundleException(msg.toString(), BundleException.ACTIVATOR_ERROR);
                        throw new IllegalStateException(msg.toString(), be);
                    }
                    startupIfHigherRanked(discovery, getRanking(reference));
                    return discovery;
                }
                synchronized (deactivated) {
                    final ClusterDiscoveryService discovery = context.getService(reference);
                    startupIfHigherRanked(discovery, getRanking(reference));
                    return discovery;
                }
            }

            private void startupIfHigherRanked(final ClusterDiscoveryService discovery, final int ranking) {
                if (null != clusterDiscoveryService) {
                    if (clusterDiscoveryServiceRanking >= ranking) {
                        deactivated.addFirst(new ServiceContainer<ClusterDiscoveryService>(discovery, ranking));
                        return;
                    }
                    shutdown();
                    deactivated.addFirst(new ServiceContainer<ClusterDiscoveryService>(
                        clusterDiscoveryService,
                        clusterDiscoveryServiceRanking));
                }
                clusterDiscoveryService = discovery;
                clusterDiscoveryServiceRanking = ranking;
                /*
                 * Do start-up
                 */
                final long st = System.currentTimeMillis();
                final List<InetAddress> nodes = discovery.getNodes();
                if (infoEnabled) {
                    final long et = System.currentTimeMillis();
                    logger.info("\nHazelcast\n\tAvailable cluster nodes received in " + (et - st) + "msec from " + 
                        ClusterDiscoveryService.class.getSimpleName() + ":\n\t" + nodes + "\n");
                }
                /*-
                 * Check initially available nodes
                 */
                if (nodes.isEmpty()) {
                    if (InitMode.INITIALIZED.equals(init(nodes, false, st, logger))) {
                        if (infoEnabled) {
                            logger.info("\nHazelcast:\n\tInitialized Hazelcast instance with empty Open-Xchange nodes.\n");
                        }
                    }
                } else {
                    /*
                     * We already have at least one node at start-up time
                     */
                    if (InitMode.INITIALIZED.equals(init(nodes, false, st, logger))) {
                        if (infoEnabled) {
                            logger.info("\nHazelcast:\n\tInitialized Hazelcast instance via initially available Open-Xchange nodes.\n");
                        }
                    }
                }
            }

            @Override
            public void modifiedService(final ServiceReference<ClusterDiscoveryService> reference, final ClusterDiscoveryService service) {
                // nope
            }

            @Override
            public void removedService(final ServiceReference<ClusterDiscoveryService> reference, final ClusterDiscoveryService service) {
                if (null == service) {
                    return;
                }
                if (isSingleton) {
                    shutdown();
                    context.ungetService(reference);
                    clusterDiscoveryServiceReference.set(null);
                    return;
                }
                synchronized (deactivated) {
                    for (final Iterator<ServiceContainer<ClusterDiscoveryService>> iterator = deactivated.iterator(); iterator.hasNext();) {
                        final ServiceContainer<ClusterDiscoveryService> box = iterator.next();
                        if (box.getService() == service) {
                            // Wasn't active
                            iterator.remove();
                            context.ungetService(reference);
                            return;
                        }
                    }
                    if (service == clusterDiscoveryService) {
                        shutdown();
                        context.ungetService(reference);
                        clusterDiscoveryService = null;
                        clusterDiscoveryServiceRanking = 0;
                        if (!deactivated.isEmpty()) {
                            final ServiceContainer<ClusterDiscoveryService> box = deactivated.removeFirst();
                            startupIfHigherRanked(box.getService(), box.getRanking());
                        }
                        return;
                    }
                }
                // Eh...
                context.ungetService(reference);
            }

            private void shutdown() {
                final HazelcastInstance hazelcastInstance = REF_HAZELCAST_INSTANCE.get();
                if (null != hazelcastInstance) {
                    hazelcastInstance.getLifecycleService().shutdown();
                    REF_HAZELCAST_INSTANCE.set(null);
                }
                Hazelcast.shutdownAll();
            }
        });
        openTrackers();
        // CommandProvider for OSGi console
        registerService(CommandProvider.class, new UtilCommandProvider(this), null);
    }

    public static final class UtilCommandProvider implements CommandProvider {

        private final HazelcastActivator activator;

        public UtilCommandProvider(final HazelcastActivator activator) {
            super();
            this.activator = activator;
        }

        @Override
        public String getHelp() {
            final StringBuilder help = new StringBuilder();
            help.append("\taddnode - Add a hazelcast node.\n");
            return help.toString();
        }

        public void _addnode(final CommandInterpreter commandInterpreter) {
            final String ip = commandInterpreter.nextArgument();
            if (isEmpty(ip)) {
                commandInterpreter.println("Couldn't resolve IP: " + ip);
                return;
            }
            try {
                activator.init(Collections.singletonList(InetAddress.getByName(ip)), false, System.currentTimeMillis(), LOG);
                commandInterpreter.println("Added node to Hazelcast cluster: " + ip);
            } catch (final UnknownHostException e) {
                LOG.error("Could not register node.", e);
                commandInterpreter.println("Couldn't resolve IP: " + ip);
            }
        }
    }

    /**
     * Hazelcast initialization result.
     */
    public static enum InitMode {
        INITIALIZED, RE_INITIALIZED, NONE;
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
    InitMode remove(final List<InetAddress> nodes, final boolean force, final Log logger) {
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
    InitMode init(final List<InetAddress> nodes, final boolean force, final long stamp, final Log logger) {
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
            final HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
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
            registerService(HazelcastInstance.class, hazelcastInstance);
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

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
    }

    /**
     * Creates a new hazelcast configuration, setting the relevant properties as read by the configuration service.
     * 
     * @return A new pre-configured hazelcast config object
     */
    private Config createConfig() {
        Config config = new Config();
        ConfigurationService configService = getService(ConfigurationService.class);
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

    static boolean isEmpty(final String string) {
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
