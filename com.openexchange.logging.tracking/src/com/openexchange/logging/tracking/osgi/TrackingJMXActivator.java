package com.openexchange.logging.tracking.osgi;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.LogFactory;

import com.openexchange.exception.OXException;
import com.openexchange.logging.tracking.TrackingConfiguration;
import com.openexchange.management.ManagementService;
import com.openexchange.osgi.HousekeepingActivator;

public class TrackingJMXActivator extends HousekeepingActivator {

	private static ObjectName OBJECT_NAME = null;
	private TrackingConfiguration config;
	private boolean registered;
	
	static {
		try {
			OBJECT_NAME = new ObjectName("com.openexchange.logging.tracking.TrackingConfiguration", "name", "TrackingConfiguration");
		} catch (MalformedObjectNameException e) {
			LogFactory.getLog(TrackingJMXActivator.class).error(e.getMessage(), e);
		} catch (NullPointerException e) {
			LogFactory.getLog(TrackingJMXActivator.class).error(e.getMessage(), e);
		}
	}
	
	public void setConfig(TrackingConfiguration config) {
		this.config = config;
		tryRegistering();
	}
	
	@Override
	protected Class<?>[] getNeededServices() {
		return new Class<?>[]{ManagementService.class};
	}

	@Override
	protected void startBundle() throws Exception {
		tryRegistering();
	}

	private void tryRegistering() {
		if (registered) {
			return;
		}
		if (config == null) {
			return;
		}
		
		ManagementService managementService = getService(ManagementService.class);
		if (managementService == null) {
			return;
		}
		
		try {
			managementService.registerMBean(OBJECT_NAME, config);
			registered = true;
		} catch (OXException e) {
			LogFactory.getLog(TrackingJMXActivator.class).error(e.getMessage(), e);
		}
	}
	
	@Override
	protected void stopBundle() throws Exception {
		if (registered) {
			registered = false;
			ManagementService managementService = getService(ManagementService.class);
			if (managementService != null) {
				managementService.unregisterMBean(OBJECT_NAME);
			}
		}
		super.stopBundle();
	}

}
