package com.openexchange.cluster.discovery.empty.osgi;

import java.net.InetAddress;
import java.util.Collections;
import java.util.List;

import com.openexchange.cluster.discovery.ClusterDiscoveryService;
import com.openexchange.cluster.discovery.ClusterListener;
import com.openexchange.osgi.HousekeepingActivator;

public class EmptyClusterDiscoveryActivator extends HousekeepingActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return null;
	}

	@Override
	protected void startBundle() throws Exception {
		registerService(ClusterDiscoveryService.class, new ClusterDiscoveryService() {
			
			public List<InetAddress> getNodes() {
				return Collections.emptyList();
			}
			
			public void addListener(ClusterListener listener) {
				
			}
		});
	}


}
