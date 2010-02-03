package com.openexchange.messaging.rss.osgi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.rss.RSSMessagingService;

public class Activator implements BundleActivator {

    private static final Log LOG = LogFactory.getLog(Activator.class);
    
	public void start(BundleContext context) throws Exception {
	    try {
	        context.registerService(MessagingService.class.getName(), new RSSMessagingService(), null);
	    } catch (Exception x) {
	        LOG.error(x.getMessage(), x);
	        throw x;
	    }
	}

	public void stop(BundleContext context) throws Exception {
	    // Services are deregistered automatically by the framework
	}

}
