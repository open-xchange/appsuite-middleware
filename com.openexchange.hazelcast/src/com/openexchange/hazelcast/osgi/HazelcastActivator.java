
package com.openexchange.hazelcast.osgi;

import java.net.InetAddress;
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
        TcpIpConfig tcpIpConfig = join.getTcpIpConfig();
        tcpIpConfig.setEnabled(true);
        /*
         * Look-up discovery service & obtain its addresses of known nodes in a cluster
         */
        ClusterDiscoveryService discovery = getService(ClusterDiscoveryService.class);
        for (InetAddress address : discovery.getNodes()) {
            tcpIpConfig.addAddress(new Address(address, config.getPort()));
        }
        /*
         * Create appropriate Hazelcast instance
         */
        final HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        registerService(HazelcastInstance.class, hazelcastInstance);
        this.hazelcastInstance = hazelcastInstance;
        /*
         * Add cluster listener to manage appearing/disappearing nodes
         */
        final ClusterListener clusterListener = new ClusterListener() {
            
            @Override
            public void removed(InetAddress address) {
                hazelcastInstance.getConfig().getNetworkConfig().getJoin().getTcpIpConfig().getAddresses().remove(address);
                hazelcastInstance.getLifecycleService().restart();
            }
            
            @Override
            public void added(InetAddress address) {
                hazelcastInstance.getConfig().getNetworkConfig().getJoin().getTcpIpConfig().addAddress(new Address(address, config.getPort()));
                hazelcastInstance.getLifecycleService().restart();
            }
        };
        discovery.addListener(clusterListener);
        this.clusterListener = clusterListener;
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
