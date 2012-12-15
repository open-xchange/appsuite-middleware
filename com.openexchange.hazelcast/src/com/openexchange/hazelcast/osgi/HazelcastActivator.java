
package com.openexchange.hazelcast.osgi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.logging.Log;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.cluster.discovery.ClusterDiscoveryService;
import com.openexchange.cluster.discovery.ClusterListener;
import com.openexchange.config.ConfigurationService;
import com.openexchange.hazelcast.init.HazelcastInitializer;
import com.openexchange.management.ManagementService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceContainer;
import com.openexchange.timer.TimerService;

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
     * The {@code AtomicReference} for {@code ClusterDiscoveryService}.
     */
    protected final AtomicReference<ClusterDiscoveryService> clusterDiscoveryServiceReference;

    /**
     * The Hazelcast initializer.
     */
    protected final HazelcastInitializer initializer;

    /**
     * Initializes a new {@link HazelcastActivator}.
     */
    public HazelcastActivator() {
        super();
        clusterDiscoveryServiceReference = new AtomicReference<ClusterDiscoveryService>();
        initializer = new HazelcastInitializer(this);
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
            logger.info("\nHazelcast\n\tStartup of bundle disabled: com.openexchange.hazelcast");
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
        final ClusterListener clusterListener = new HazelcastClusterListener(initializer, System.currentTimeMillis(), logger);
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
                    if (HazelcastInitializer.InitMode.INITIALIZED == initializer.init(nodes, false, st, logger)) {
                        if (infoEnabled) {
                            logger.info("\nHazelcast:\n\tInitialized Hazelcast instance with empty Open-Xchange nodes.\n");
                        }
                    }
                } else {
                    /*
                     * We already have at least one node at start-up time
                     */
                    if (HazelcastInitializer.InitMode.INITIALIZED == initializer.init(nodes, false, st, logger)) {
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
                final AtomicReference<HazelcastInstance> reference = HazelcastInitializer.REF_HAZELCAST_INSTANCE;
                final HazelcastInstance hazelcastInstance = reference.get();
                if (null != hazelcastInstance) {
                    hazelcastInstance.getLifecycleService().shutdown();
                    reference.set(null);
                }
                Hazelcast.shutdownAll();
            }
        });
        openTrackers();
        // CommandProvider for OSGi console
        registerService(CommandProvider.class, new UtilCommandProvider(initializer), null);
    }

    @Override
    public <S> void registerService(Class<S> clazz, S service) {
        super.registerService(clazz, service);
    }

    /** Simple command provider */
    public static final class UtilCommandProvider implements CommandProvider {

        private final HazelcastInitializer initializer;

        public UtilCommandProvider(final HazelcastInitializer initializer) {
            super();
            this.initializer = initializer;
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
                initializer.init(Collections.singletonList(InetAddress.getByName(ip)), false, System.currentTimeMillis(), LOG);
                commandInterpreter.println("Added node to Hazelcast cluster: " + ip);
            } catch (final UnknownHostException e) {
                LOG.error("Could not register node.", e);
                commandInterpreter.println("Couldn't resolve IP: " + ip);
            }
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
    }

    /** Checks for empty string */
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
}
