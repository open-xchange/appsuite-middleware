package com.openexchange.logging.tracking.osgi;

import org.osgi.framework.BundleActivator;

import com.openexchange.osgi.CompositeBundleActivator;

public class CompositeTrackingActivator extends CompositeBundleActivator {

	@Override
	protected BundleActivator[] getActivators() {
		TrackingJMXActivator trackingJMXActivator = new TrackingJMXActivator();
		TrackingActivator trackingActivator = new TrackingActivator(trackingJMXActivator);
		
		return new BundleActivator[]{trackingActivator, trackingJMXActivator};
	}

}
