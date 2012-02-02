package com.openexchange.calendar.osgi;

import org.osgi.framework.BundleActivator;

import com.openexchange.osgi.CompositeBundleActivator;

public class CalendarActivator extends CompositeBundleActivator {

	@Override
	protected BundleActivator[] getActivators() {
		return new BundleActivator[]{
				new CoreCalendarActivator(),
				new ITipActivator()
		};
	}

}
