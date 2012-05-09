package com.openexchange.http.client.xml.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.openexchange.server.osgiservice.CompositeBundleActivator;

public class HTTPXMLActivator extends CompositeBundleActivator {

	@Override
	protected BundleActivator[] getActivators() {
		return new BundleActivator[]{
				new DOMBasedProcessorsActivator(),
				new JDomBasedProcessorActivator()
		};
	}


}
