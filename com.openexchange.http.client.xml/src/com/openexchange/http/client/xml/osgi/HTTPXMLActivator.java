package com.openexchange.http.client.xml.osgi;

import org.osgi.framework.BundleActivator;

import com.openexchange.osgi.CompositeBundleActivator;

public class HTTPXMLActivator extends CompositeBundleActivator {

	@Override
	protected BundleActivator[] getActivators() {
		return new BundleActivator[]{
				new DOMBasedProcessorsActivator(),
				new JDomBasedProcessorActivator()
		};
	}


}
