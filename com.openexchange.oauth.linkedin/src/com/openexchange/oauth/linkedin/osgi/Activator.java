
package com.openexchange.oauth.linkedin.osgi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.config.ConfigurationService;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.linkedin.LinkedInConnectionsService;
import com.openexchange.oauth.linkedin.OAuthServiceMetaDataLinkedInImpl;

public class Activator implements BundleActivator {

    private static final Log LOG = LogFactory.getLog(Activator.class);

    private BundleContext bundleContext;

    private final Stack<ServiceTracker> trackers = new Stack<ServiceTracker>();

    private ArrayList<ServiceRegistration> services;

    private Map<String, ServiceRegistration> activeServices = new HashMap<String, ServiceRegistration>();
    
    private OAuthService oauthService;
    
    private OAuthServiceMetaData linkedInMetadata;
    
    public Activator(){
        
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        bundleContext = context;
        services = new ArrayList<ServiceRegistration>();
        
        // react dynamically to the appearance/disappearance of ConfigurationService, OAuthServiceMetadata
        trackers.push(new ServiceTracker(
            context,
            ConfigurationService.class.getName(),
            new ConfigurationServiceRegisterer(context, this)));
        trackers.push(new ServiceTracker(
            context,
            OAuthServiceMetaData.class.getName(),
            new LinkedInMetadataServiceRegisterer(context, this)));
        trackers.push(new ServiceTracker(
            context,
            OAuthService.class.getName(),
            new OAuthServiceRegisterer(context, this)));
        
        for (final ServiceTracker tracker : trackers) {
            tracker.open();
        }
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
    }

    public void registerServices(ConfigurationService config) {
        if (config != null) {

            final OAuthServiceMetaDataLinkedInImpl linkedInMetaDataService = new OAuthServiceMetaDataLinkedInImpl();
            final ServiceRegistration serviceRegistration = bundleContext.registerService(
                OAuthServiceMetaData.class.getName(),
                linkedInMetaDataService,
                null);
            services.add(serviceRegistration);
            activeServices.put("LinkedIn", serviceRegistration);
            LOG.info("OAuthServiceMetaData for LinkedIn was started");
            
            final LinkedInConnectionsService linkedinConnectionsService = new LinkedInConnectionsService(this);
            final ServiceRegistration serviceRegistration2 = bundleContext.registerService(
                LinkedInConnectionsService.class.getName(),
                linkedinConnectionsService,
                null);
            services.add(serviceRegistration2);
            activeServices.put("linkedin-connections", serviceRegistration2);
            LOG.info("LinkedInConnectionsService was started.");                      

        }
    }

    public void unregisterServices() {
        for (final ServiceRegistration serviceRegistration : services) {
            serviceRegistration.unregister();
        }
    }

    
    public OAuthService getOauthService() {
        return oauthService;
    }

    
    public void setOauthService(OAuthService oauthService) {
        this.oauthService = oauthService;
    }

    
    public OAuthServiceMetaData getLinkedInMetadata() {
        return linkedInMetadata;
    }

    
    public void setLinkedInMetadata(OAuthServiceMetaData linkedInMetadata) {
        this.linkedInMetadata = linkedInMetadata;
    }
    
    
}
