
package com.openexchange.subscribe.linkedin.osgi;

import java.util.ArrayList;
import java.util.Stack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.linkedin.LinkedInService;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.linkedin.LinkedInSubscribeService;

public class Activator implements BundleActivator {

    private static final Log LOG = LogFactory.getLog(Activator.class);

    private BundleContext bundleContext;

    private final Stack<ServiceTracker> trackers = new Stack<ServiceTracker>();

    private ArrayList<ServiceRegistration> services;

    private OAuthServiceMetaData oAuthServiceMetadata;

    private LinkedInService linkedInService;

    public void start(BundleContext context) throws Exception {
        bundleContext = context;
        services = new ArrayList<ServiceRegistration>();
        // react dynamically to the appearance/disappearance of LinkedinService
        trackers.push(new ServiceTracker(context, LinkedInService.class.getName(), new LinkedInServiceRegisterer(context, this)));

        // react dynamically to the appearance/disappearance of LinkedinService
        trackers.push(new ServiceTracker(context, OAuthServiceMetaData.class.getName(), new OAuthServiceMetaDataRegisterer(context, this)));

        for (final ServiceTracker tracker : trackers) {
            tracker.open();
        }
    }

    public void stop(BundleContext context) throws Exception {
        while (!trackers.isEmpty()) {
            trackers.pop().close();
        }
    }

    public void registerServices() {
        if (null != oAuthServiceMetadata && null != linkedInService){
            final LinkedInSubscribeService linkedInSubscribeService = new LinkedInSubscribeService(this);
            final ServiceRegistration serviceRegistration = bundleContext.registerService(
                SubscribeService.class.getName(),
                linkedInSubscribeService,
                null);
            services.add(serviceRegistration);
            LOG.info("LinkedInSubscribeService was started.");
        }
    }

    public void unregisterServices() {
        for (final ServiceRegistration serviceRegistration : services) {
            serviceRegistration.unregister();
        }
    }

    public OAuthServiceMetaData getOAuthServiceMetadata() {
        return oAuthServiceMetadata;
    }

    public void setOAuthServiceMetadata(OAuthServiceMetaData authServiceMetadata) {
        oAuthServiceMetadata = authServiceMetadata;
    }

    public LinkedInService getLinkedInService() {
        return linkedInService;
    }

    public void setLinkedInService(LinkedInService linkedInService) {
        this.linkedInService = linkedInService;
    }

}
