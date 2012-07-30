
package com.openexchange.hazelcast.osgi;

import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryXmlConfig;
import com.hazelcast.config.Join;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.Address;
import com.openexchange.cluster.discovery.ClusterDiscoveryService;
import com.openexchange.cluster.discovery.ClusterListener;
import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link HazelcastActivator}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HazelcastActivator extends HousekeepingActivator {

    private volatile HazelcastInstance hazelcastInstance;
    private volatile ClusterListener clusterListener;

    /**
     * Initializes a new {@link HazelcastActivator}.
     */
    public HazelcastActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ClusterDiscoveryService.class, ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        /*-
         * Look-up discovery service & obtain its addresses of known nodes in a cluster
         * 
         * Configuring Hazelcast for full TCP/IP cluster
         * 
         * If multicast is not prefered way of discovery for your environment, then you can configure Hazelcast for full TCP/IP cluster.
         * As configuration below shows, while enable attribute of multicast is set to false, tcp-ip has to be set to true.
         * For the none-multicast option, all or subset of cluster members' hostnames and/or ip addreses must be listed.
         * 
         * Note that all of the cluster members don't have to be listed there but at least one of them has to be active in cluster when a
         * new member joins.
         */
        final ClusterDiscoveryService discovery = getService(ClusterDiscoveryService.class);
        final List<InetAddress> nodes = discovery.getNodes();
        if (nodes.isEmpty()) {
            /*-
             * Wait for at least one via ClusterListener
             * 
             * Add cluster listener to manage appearing/disappearing nodes
             */
            final ClusterListener clusterListener = new ClusterListener() {
                
                @Override
                public void removed(InetAddress address) {
                    init(Collections.<InetAddress> singletonList(address));
                }
                
                @Override
                public void added(InetAddress address) {
                    // Nothing
                }
            };
            discovery.addListener(clusterListener);
            this.clusterListener = clusterListener;
        } else {
            /*
             * We already have at least one node at start-up time
             */
            init(nodes);
        }
    }

    protected void init(final List<InetAddress> nodes) {
        /*
         * Create config from XML data
         */
        String xml = getService(ConfigurationService.class).getText("hazelcast.xml");
        final Config config = new InMemoryXmlConfig(xml);
        /*
         * Get reference to network join
         */
        final Join join = config.getNetworkConfig().getJoin();
        /*
         * Enable: multicase, AWS, and TCP-IP
         */
        join.getMulticastConfig().setEnabled(false);
        join.getAwsConfig().setEnabled(false);
        final TcpIpConfig tcpIpConfig = join.getTcpIpConfig();
        tcpIpConfig.setEnabled(true);
        for (final InetAddress address : nodes) {
            tcpIpConfig.addAddress(new Address(address, config.getPort()));
        }
        /*
         * Create appropriate Hazelcast instance from configuration
         */
        final HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        registerService(HazelcastInstance.class, hazelcastInstance);
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    protected void stopBundle() throws Exception {
        final ClusterListener clusterListener = this.clusterListener;
        if (null != clusterListener) {
            getService(ClusterDiscoveryService.class).removeListener(clusterListener);
            this.clusterListener = null;
        }
        final HazelcastInstance hazelcastInstance = this.hazelcastInstance;
        if (null != hazelcastInstance) {
            hazelcastInstance.getLifecycleService().shutdown();
            this.hazelcastInstance = null;
        }
        Hazelcast.shutdownAll();
        super.stopBundle();
    }

}
