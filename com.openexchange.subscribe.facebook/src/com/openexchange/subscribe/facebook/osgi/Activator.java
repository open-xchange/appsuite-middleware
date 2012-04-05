package com.openexchange.subscribe.facebook.osgi;

import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import com.openexchange.context.ContextService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.facebook.FacebookService;
import com.openexchange.osgi.HousekeepingActivator;

public class Activator extends HousekeepingActivator {

    public Activator() {
        super();
    }

    @Override
    public void startBundle() throws Exception {
        final Filter filter = context.createFilter("(|(" + Constants.OBJECTCLASS + '=' + OAuthServiceMetaData.class.getName() + ")(" + Constants.OBJECTCLASS + '=' + FacebookService.class.getName() + ")(" + Constants.OBJECTCLASS + '=' + ContextService.class.getName() + "))");
        track(filter, new FacebookRegisterer(context));
        openTrackers();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

}
