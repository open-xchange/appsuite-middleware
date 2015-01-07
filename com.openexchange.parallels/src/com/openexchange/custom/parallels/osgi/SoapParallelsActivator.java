
package com.openexchange.custom.parallels.osgi;

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.admin.rmi.OXLoginInterface;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.custom.parallels.impl.ParallelsHostnameService;
import com.openexchange.custom.parallels.impl.ParallelsMailMappingService;
import com.openexchange.custom.parallels.impl.ParallelsOXAuthentication;
import com.openexchange.custom.parallels.impl.ParallelsOptions;
import com.openexchange.custom.parallels.soap.OXServerServicePortType;
import com.openexchange.custom.parallels.soap.OXServerServicePortTypeImpl;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.mailmapping.MailResolver;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.tools.servlet.http.HTTPServletRegistration;
import com.openexchange.user.UserService;

public class SoapParallelsActivator extends HousekeepingActivator {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SoapParallelsActivator.class);

    public SoapParallelsActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ContextService.class, UserService.class, ConfigurationService.class, HttpService.class, DatabaseService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        // try to load all the needed services like config service and hostnameservice
        Services.setServiceLookup(this);

        // register the http info/sso servlet
        LOG.debug("Trying to register POA info servlet");
        {
            String alias = getFromConfig(ParallelsOptions.PROPERTY_SSO_INFO_SERVLET);
            if (null == alias) {
                throw new BundleException("Missing property \"" + ParallelsOptions.PROPERTY_SSO_INFO_SERVLET + "\".");
            }
            rememberTracker(new HTTPServletRegistration(context, new com.openexchange.custom.parallels.impl.ParallelsInfoServlet(), alias));
        }
        {
            String alias = getFromConfig(ParallelsOptions.PROPERTY_OPENAPI_SERVLET);
            if (null == alias) {
                throw new BundleException("Missing property \"" + ParallelsOptions.PROPERTY_SSO_INFO_SERVLET + "\".");
            }
            rememberTracker(new HTTPServletRegistration(context, new com.openexchange.custom.parallels.impl.ParallelsOpenApiServlet(), alias));
        }
        final BundleContext context = this.context;
        final ServiceTrackerCustomizer<Remote, Remote> trackerCustomizer = new ServiceTrackerCustomizer<Remote, Remote>() {

            @Override
            public void removedService(final ServiceReference<Remote> reference, final Remote service) {
                if (null != service) {
                    OXServerServicePortTypeImpl.RMI_REFERENCE.set(null);
                    context.ungetService(reference);
                }
            }

            @Override
            public void modifiedService(final ServiceReference<Remote> reference, final Remote service) {
                // Ignore
            }

            @Override
            public Remote addingService(final ServiceReference<Remote> reference) {
                final Remote service = context.getService(reference);
                if (service instanceof OXLoginInterface) {
                    OXServerServicePortTypeImpl.RMI_REFERENCE.set((OXLoginInterface) service);
                    return service;
                }
                context.ungetService(reference);
                return null;
            }
        };
        track(Remote.class, trackerCustomizer);
        openTrackers();
        LOG.debug("Successfully registered POA info servlet");

        // register auth plugin
        LOG.debug("Trying to register POA authentication plugin");
        registerService(AuthenticationService.class.getName(), new ParallelsOXAuthentication(), null);
        LOG.debug("Successfully registered POA authentication plugin");
        // regitser hostname service to modify hostnames in directlinks
        LOG.debug("Trying to register POA hostname/directlinks plugin");
        registerService(HostnameService.class.getName(), new ParallelsHostnameService(), null);
        LOG.debug("Successfully registered POA hostname/directlinks plugin");
        Dictionary<String, Object> props = new Hashtable<String, Object>(2);
        props.put(Constants.SERVICE_RANKING, Integer.MAX_VALUE);
        registerService(MailResolver.class, new ParallelsMailMappingService(this), props);
        LOG.debug("Successfully registered POA Mailmappings plugin");
        // Register SOAP service
        final OXServerServicePortTypeImpl soapService = new OXServerServicePortTypeImpl();
        registerService(OXServerServicePortType.class, soapService);
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        Services.setServiceLookup(null);
    }

    private String getFromConfig(final String key) throws OXException {
        return getService(ConfigurationService.class).getProperty(key);
    }
}
