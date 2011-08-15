package com.openexchange.subscribe.facebook.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.context.ContextService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.facebook.FacebookService;

public class Activator implements BundleActivator {

    private ServiceTracker<Object, Object> tracker;

    public Activator() {
        super();
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        final Filter filter = context.createFilter("(|(" + Constants.OBJECTCLASS + '=' + OAuthServiceMetaData.class.getName() + ")(" + Constants.OBJECTCLASS + '=' + FacebookService.class.getName() + ")(" + Constants.OBJECTCLASS + '=' + ContextService.class.getName() + "))");
        tracker = new ServiceTracker<Object,Object>(context, filter, new FacebookRegisterer(context));
        tracker.open();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(final BundleContext context) throws Exception {
        tracker.close();
    }

}
