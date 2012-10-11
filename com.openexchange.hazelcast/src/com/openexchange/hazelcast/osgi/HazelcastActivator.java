
package com.openexchange.hazelcast.osgi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.logging.Log;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryXmlConfig;
import com.hazelcast.config.Interfaces;
import com.hazelcast.config.Join;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.Address;
import com.openexchange.cluster.discovery.ClusterDiscoveryService;
import com.openexchange.cluster.discovery.ClusterListener;
import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.strings.TimeSpanParser;

/**
 * {@link HazelcastActivator} - The activator for Hazelcast bundle (registers a {@link HazelcastInstance} for ths JVM)
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
    
    private static final Log LOG = com.openexchange.log.Log.loggerFor(HazelcastActivator.class);

    public static final AtomicReference<HazelcastInstance> REF_HAZELCAST_INSTANCE = new AtomicReference<HazelcastInstance>();

    volatile ClusterListener clusterListener;

    /**
     * Initializes a new {@link HazelcastActivator}.
     */
    public HazelcastActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ConfigurationService.class, TimerService.class, ThreadPoolService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final Log logger = com.openexchange.log.Log.loggerFor(HazelcastActivator.class);
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
        track(ClusterDiscoveryService.class, new ServiceTrackerCustomizer<ClusterDiscoveryService, ClusterDiscoveryService>() {

            @Override
            public ClusterDiscoveryService addingService(final ServiceReference<ClusterDiscoveryService> reference) {
                final ClusterDiscoveryService discovery = context.getService(reference);
                final long st = System.currentTimeMillis();
                final List<InetAddress> nodes = discovery.getNodes();
                final Runnable task;
                if (infoEnabled) {
                    final long et = System.currentTimeMillis();
                    logger.info("\nHazelcast\n\tAvailable cluster nodes received in "+(et - st)+"msec from "+ClusterDiscoveryService.class.getSimpleName()+":\n\t"+nodes+"\n");
                }
                if (nodes.isEmpty()) {
                    /*-
                     * Wait for at least one via ClusterListener
                     * 
                     * Add cluster listener to manage appearing/disappearing nodes
                     */
                    task = new Runnable() {

                        @Override
                        public void run() {
                            final HazelcastActivator activator = HazelcastActivator.this;
                    final ClusterListener clusterListener = new HazelcastInitializingClusterListener(activator, st, logger);
                            discovery.addListener(clusterListener);
                            activator.clusterListener = clusterListener;
                            /*
                             * Timeout before we assume we are either the first or alone in the cluster
                             */
                            final long delay = getDelay();
                            if (delay >= 0) {
                                final Runnable task = new Runnable() {

                                    @Override
                                    public void run() {
                                if (init(Collections.<InetAddress> emptyList(), false, st, logger)) {
                                    if (infoEnabled) {
                                        logger.info("\nHazelcast:\n\tInitialized Hazelcast instance via delayed one-shot task after " + delay + "msec.\n");
                                    }
                                        }
                                    }
                                };
                                getService(TimerService.class).schedule(task, delay);
                            }
                        }
                    };
                } else {
                    task = new Runnable() {

                        @Override
                        public void run() {
                            /*
                             * We already have at least one node at start-up time
                             */
                    if (init(nodes, false, st, logger)) {
                        if (infoEnabled) {
                            logger.info("\nHazelcast:\n\tInitialized Hazelcast instance via initially available Open-Xchange nodes.\n");
                        }
                            }
                        }
                    };
                }
                //getService(ThreadPoolService.class).submit(ThreadPools.task(task));
                new Thread(task).run();
                return discovery;
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
                final ClusterListener clusterListener = HazelcastActivator.this.clusterListener;
                if (null != clusterListener) {
                    service.removeListener(clusterListener);
                    HazelcastActivator.this.clusterListener = null;
                }
                final HazelcastInstance hazelcastInstance = REF_HAZELCAST_INSTANCE.get();
                if (null != hazelcastInstance) {
                    hazelcastInstance.getLifecycleService().shutdown();
                    REF_HAZELCAST_INSTANCE.set(null);
                }
                Hazelcast.shutdownAll();
                context.ungetService(reference);
            }
        });
        openTrackers();
        
        registerService(CommandProvider.class, new UtilCommandProvider(), null);
    }

    /**
     * Gets the delay in milliseconds.
     * 
     * @return The delay milliseconds
     */
    long getDelay() {
        final String delay = getService(ConfigurationService.class).getProperty("com.openexchange.hazelcast.startupDelay", "60000");
        return TimeSpanParser.parseTimespan(delay).longValue();
    }

    /**
     * Initializes and registers a {@code HazelcastInstance} for a full TCP/IP cluster.
     * 
     * @param nodes The pre-known nodes
     * @param force <code>true</code> to enforce (re-)initialization of {@code HazelcastInstance}; otherwise <code>false</code>
     * @param stamp The start-up time stamp
     * @param logger The logger instance
     * @return <code>true</code> if <tt>HazelcastInstance</tt> has been initialized by this call; otherwise <code>false</code> if already
     *         done by another call
     */
    boolean init(final List<InetAddress> nodes, final boolean force, final long stamp, final Log logger) {
        synchronized (this) {
            final HazelcastInstance prevHazelcastInstance = REF_HAZELCAST_INSTANCE.get();
            if (null != prevHazelcastInstance) {
                // Already initialized
                if (!force) {
                    return false;                    
                }
                final Config config = prevHazelcastInstance.getConfig();
                configureNetworkJoin(nodes, true, config);
                prevHazelcastInstance.getLifecycleService().restart();
                if (logger.isInfoEnabled()) {
                    logger.info("\nHazelcast:\n\tRe-Started in " + (System.currentTimeMillis() - stamp) + "msec.\n");
                }
                return true;
            }
            /*
             * Create configuration from XML data
             */
            final String xml = getService(ConfigurationService.class).getText("hazelcast.xml");
            final Config config = new InMemoryXmlConfig(xml);
            configureNetworkJoin(nodes, false, config);
            // for (final InetAddress address : nodes) {
            // tcpIpConfig.addAddress(new Address(address, config.getNetworkConfig().getPort()));
            // }
            /*
             * Create appropriate Hazelcast instance from configuration
             */
//            final HazelcastInstance hazelcastInstance = new ClassLoaderAwareHazelcastInstance(Hazelcast.newHazelcastInstance(config), false);
            final HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
            registerService(HazelcastInstance.class, hazelcastInstance);
            REF_HAZELCAST_INSTANCE.set(hazelcastInstance);
            if (logger.isInfoEnabled()) {
                logger.info("\nHazelcast:\n\tStarted in " + (System.currentTimeMillis() - stamp) + "msec.\n");
            }
            return true;
        }
    }
    
    public static final class UtilCommandProvider implements CommandProvider {

        @Override
        public String getHelp() {
            StringBuilder help = new StringBuilder();
            help.append("\taddnode - Add a solr node.\n");
            return help.toString();
        }
        
        public void _addnode(CommandInterpreter commandInterpreter) {
            String ip = commandInterpreter.nextArgument();
            HazelcastInstance hazelcast = REF_HAZELCAST_INSTANCE.get();
            Join join = hazelcast.getConfig().getNetworkConfig().getJoin();
            TcpIpConfig tcpIpConfig = join.getTcpIpConfig();
            try {
                tcpIpConfig.addAddress(new Address(InetAddress.getByName(ip), hazelcast.getConfig().getNetworkConfig().getPort()));
                hazelcast.getLifecycleService().restart();
            } catch (UnknownHostException e) {
                LOG.error("Could not register node.", e);
            }
        }
    }

    private void configureNetworkJoin(final List<InetAddress> nodes, final boolean append, final Config config) {
        /*
         * Get reference to network join
         */
        final Join join = config.getNetworkConfig().getJoin();
        /*
         * Disable: Multicast, AWS and ...
         */
        join.getMulticastConfig().setEnabled(false);
        join.getAwsConfig().setEnabled(false);
        /*-
         * ... enable: TCP-IP
         * 
         * http://code.google.com/p/hazelcast/wiki/ConfigFullTcpIp
         */
        final TcpIpConfig tcpIpConfig = join.getTcpIpConfig();
        tcpIpConfig.setEnabled(true).setConnectionTimeoutSeconds(10);
        if (!append) {
            tcpIpConfig.clear();
        }
        {
            final List<String> members = new LinkedList<String>();
            for (final InetAddress inetAddress : nodes) {
                final String[] addressArgs = inetAddress.getHostAddress().split("\\%");
                for (final String address : addressArgs) {
                    members.add(address);
                }
            }
            if (!members.isEmpty()) {
                if (append) {
                    List<String> cur = new ArrayList<String>(tcpIpConfig.getMembers());
                    for (final String candidate : members) {
                        if (!cur.contains(candidate)) {
                            cur.add(candidate);
                        }
                    }
                    tcpIpConfig.setMembers(cur);
                    // Set interfaces, too
                    final Interfaces interfaces = config.getNetworkConfig().getInterfaces();
                    cur = new ArrayList<String>(interfaces.getInterfaces());
                    for (final String candidate : members) {
                        if (!cur.contains(candidate)) {
                            cur.add(candidate);
                        }
                    }
                    interfaces.setInterfaces(cur);
                } else {
                    tcpIpConfig.setMembers(members);
                    // Set interfaces, too
                    final Interfaces interfaces = config.getNetworkConfig().getInterfaces();
                    interfaces.setInterfaces(members);
                }
            }
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
    }

}
