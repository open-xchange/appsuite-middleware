package com.openexchange.hazelcast.osgi;

import java.net.InetAddress;

import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryXmlConfig;
import com.hazelcast.config.Join;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.Address;
import com.openexchange.config.ConfigurationService;
import com.openexchange.cluster.discovery.ClusterDiscoveryService;
import com.openexchange.osgi.HousekeepingActivator;

public class HazelcastActivator extends HousekeepingActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class[]{ClusterDiscoveryService.class, ConfigurationService.class};
	}

	@Override
	protected void startBundle() throws Exception {
		
		String xml = getService(ConfigurationService.class).getText("hazelcast.xml");
		
		Config config = new InMemoryXmlConfig(xml);
		
		Join join = config.getNetworkConfig().getJoin();
		
		join.getMulticastConfig().setEnabled(false);
		join.getAwsConfig().setEnabled(false);
		
		TcpIpConfig tcpIpConfig = join.getTcpIpConfig();
		tcpIpConfig.setEnabled(true);
		
		ClusterDiscoveryService discovery = getService(ClusterDiscoveryService.class);
		for(InetAddress address: discovery.getNodes()) {
			tcpIpConfig.addAddress(new Address(address, config.getPort()));
		}
		
		registerService(HazelcastInstance.class, Hazelcast.newHazelcastInstance(config));
		
	}


}
