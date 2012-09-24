
package com.openexchange.hazelcast.osgi;

import java.net.InetAddress;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.logging.Log;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryXmlConfig;
import com.hazelcast.config.Join;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.cluster.discovery.ClusterDiscoveryService;
import com.openexchange.cluster.discovery.ClusterListener;
import com.openexchange.config.ConfigurationService;
import com.openexchange.hazelcast.ClassLoaderAwareHazelcastInstance;
import com.openexchange.osgi.HousekeepingActivator;
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
        return new Class[] { ConfigurationService.class, TimerService.class };
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
                                if (init(Collections.<InetAddress> emptyList(), st, logger)) {
                                    if (infoEnabled) {
                                        logger.info("\nHazelcast:\n\tInitialized Hazelcast instance via delayed one-shot task after " + delay + "msec.\n");
                                    }
                                }
                            }
                        };
                        getService(TimerService.class).schedule(task, delay);
                    }
                } else {
                    /*
                     * We already have at least one node at start-up time
                     */
                    if (init(nodes, st, logger)) {
                        if (infoEnabled) {
                            logger.info("\nHazelcast:\n\tInitialized Hazelcast instance via initially available Open-Xchange nodes.\n");
                        }
                    }
                }
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
     * Initializes and registers a {@link HazelcastInstance} for a full TCP/IP cluster.
     * 
     * @param nodes The pre-known nodes
     * @param stamp The start-up time stamp
     * @param logger The logger instance
     * @return <code>true</code> if <tt>HazelcastInstance</tt> has been initialized by this call; otherwise <code>false</code> if already
     *         done by another call
     */
    boolean init(final List<InetAddress> nodes, final long stamp, final Log logger) {
        synchronized (this) {
            if (null != REF_HAZELCAST_INSTANCE.get()) {
                // Already initialized
                return false;
            }
            /*
             * Create configuration from XML data
             */
            final String xml = getService(ConfigurationService.class).getText("hazelcast.xml");
            final Config config = new InMemoryXmlConfig(xml);
            /*
             * Get reference to network join
             */
            final Join join = config.getNetworkConfig().getJoin();
            /*
             * Disable: multicast, AWS, and Enable: TCP-IP
             */
            join.getMulticastConfig().setEnabled(false);
            join.getAwsConfig().setEnabled(false);
            final TcpIpConfig tcpIpConfig = join.getTcpIpConfig();
            tcpIpConfig.setEnabled(true);
            tcpIpConfig.setConnectionTimeoutSeconds(10);
            {
                final List<String> members = new LinkedList<String>();
                for (final InetAddress inetAddress : nodes) {
                    final String[] addressArgs = inetAddress.getHostAddress().split("\\%");
                    for (final String address : addressArgs) {
                        members.add(address);
                    }
                }
                if (!members.isEmpty()) {
                    tcpIpConfig.setMembers(members);
                }
            }
            // for (final InetAddress address : nodes) {
            // tcpIpConfig.addAddress(new Address(address, config.getNetworkConfig().getPort()));
            // }
            /*
             * Create appropriate Hazelcast instance from configuration
             */
            final HazelcastInstance hazelcastInstance =
                new ClassLoaderAwareHazelcastInstance(Hazelcast.newHazelcastInstance(config), false);
            registerService(HazelcastInstance.class, hazelcastInstance);
            REF_HAZELCAST_INSTANCE.set(hazelcastInstance);
            if (logger.isInfoEnabled()) {
                logger.info("\nHazelcast:\n\tStarted in " + (System.currentTimeMillis() - stamp) + "msec.\n");
            }
            return true;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
    }

}
