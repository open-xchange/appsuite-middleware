package com.openexchange.server;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private Starter oxStarter = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		System.out.println("Starting OX6...");
		try {
			(oxStarter = new Starter()).start();
			System.out.println("OX6 started");
		} catch (final Throwable e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		System.out.println("Stopping OX6...");
		try {
			oxStarter.stop();
			System.out.println("OX6 stopped");
		} catch (final Throwable e) {
			e.printStackTrace();
		}
	}

}
