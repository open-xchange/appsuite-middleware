
package com.openexchange.custom.parallels.osgi;

import java.rmi.Remote;
import org.apache.commons.logging.Log;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.admin.rmi.OXLoginInterface;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.custom.parallels.impl.ParallelsHostnameService;
import com.openexchange.custom.parallels.impl.ParallelsOXAuthentication;
import com.openexchange.custom.parallels.soap.OXServerServicePortType;
import com.openexchange.custom.parallels.soap.OXServerServicePortTypeImpl;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.tools.servlet.http.HTTPServletRegistration;
import com.openexchange.user.UserService;

public class SoapParallelsActivator extends HousekeepingActivator {

    private static transient final Log LOG = com.openexchange.log.Log.loggerFor(SoapParallelsActivator.class);

    public SoapParallelsActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ContextService.class, UserService.class, ConfigurationService.class, HttpService.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        if (LOG.isWarnEnabled()) {
            LOG.warn("Absent service: " + clazz.getName());
        }

        ParallelsServiceRegistry.getServiceRegistry().addService(clazz, getService(clazz));
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Re-available service: " + clazz.getName());
        }
        ParallelsServiceRegistry.getServiceRegistry().removeService(clazz);

    }

    @Override
    protected void startBundle() throws Exception {
        // try to load all the needed services like config service and hostnameservice
        {
            final ServiceRegistry registry = ParallelsServiceRegistry.getServiceRegistry();
            registry.clearRegistry();
            final Class<?>[] classes = getNeededServices();
            for (final Class<?> classe : classes) {
                final Object service = getService(classe);
                if (null != service) {
                    registry.addService(classe, service);
                }
            }
        }

        // register the http info/sso servlet
        if (LOG.isDebugEnabled()) {
            LOG.debug("Trying to register POA info servlet");
        }
        rememberTracker(new HTTPServletRegistration(
            context,
            new com.openexchange.custom.parallels.impl.ParallelsInfoServlet(),
            getFromConfig("com.openexchange.custom.parallels.sso_info_servlet")));
        rememberTracker(new HTTPServletRegistration(
            context,
            new com.openexchange.custom.parallels.impl.ParallelsOpenApiServlet(),
            getFromConfig("com.openexchange.custom.parallels.openapi_servlet")));
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("Successfully registered POA info servlet");
        }

        // register auth plugin
        if (LOG.isDebugEnabled()) {
            LOG.debug("Trying to register POA authentication plugin");
        }
        registerService(AuthenticationService.class.getName(), new ParallelsOXAuthentication(), null);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Successfully registered POA authentication plugin");
        }
        // regitser hostname service to modify hostnames in directlinks
        if (LOG.isDebugEnabled()) {
            LOG.debug("Trying to register POA hostname/directlinks plugin");
        }
        registerService(HostnameService.class.getName(), new ParallelsHostnameService(), null);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Successfully registered POA hostname/directlinks plugin");
        }
        // Register SOAP service
        final OXServerServicePortTypeImpl soapService = new OXServerServicePortTypeImpl();
        registerService(OXServerServicePortType.class, soapService);
    }

    @Override
    protected void stopBundle() throws Exception {
        cleanUp();
        ParallelsServiceRegistry.getServiceRegistry().clearRegistry();
    }

    private String getFromConfig(final String key) throws OXException {
        final ConfigurationService configservice = ParallelsServiceRegistry.getServiceRegistry().getService(
            ConfigurationService.class,
            true);
        return configservice.getProperty(key);
    }

}
