package com.openexchange.subscribe.facebook.osgi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.facebook.FacebookService;

public class Activator implements BundleActivator {

    private static final Log LOG = LogFactory.getLog(Activator.class);

    private ServiceTracker tracker;

    public Activator() {
        super();
    }

    public void start(final BundleContext context) throws Exception {
        final Filter filter = context.createFilter("(|(" + Constants.OBJECTCLASS + '=' + OAuthServiceMetaData.class.getName() + ")(" + Constants.OBJECTCLASS + '=' + FacebookService.class.getName() + "))");
        tracker = new ServiceTracker(context, filter, new FacebookRegisterer(context));
        tracker.open();
    }

    /**
     * {@inheritDoc}
     */
    public void stop(final BundleContext context) throws Exception {
        tracker.close();
    }

}
