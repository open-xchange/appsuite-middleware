package com.openexchange.preview.thirdwing.osgi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class ThirdwingPreviewActivator implements BundleActivator {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ThirdwingPreviewActivator.class));

    /*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
	    LOG.info("Starting bundle com.openexchange.preview.thirdwing.");
	    Tester tester = new Tester();
	    tester.perform();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		LOG.info("Stopping bundle com.openexchange.preview.thirdwing.");
	}
}
