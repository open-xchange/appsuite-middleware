

package com.openexchange.custom.parallels.osgi;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.custom.parallels.impl.ParallelsHostnameService;
import com.openexchange.custom.parallels.impl.ParallelsOXAuthentication;
import com.openexchange.custom.parallels.impl.ParallelsSpamdService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.osgi.DeferredActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.spamhandler.spamassassin.api.SpamdService;
import com.openexchange.user.UserService;


public class Activator extends DeferredActivator {

    private static transient final Log LOG = LogFactory.getLog(Activator.class);

    // add services which we need in our plugins later
    private static final Class<?>[] NEEDED_SERVICES = { ContextService.class,UserService.class,ConfigurationService.class,HttpService.class};

    private final List<SessionServletRegistration> servletRegistrations = new ArrayList<SessionServletRegistration>(2);

    private ParallelsOXAuthentication authentication ;

    private ServiceRegistration authentication_registration;

    private ParallelsHostnameService hostnameservice ;

    private ServiceRegistration hostname_registration;

    private ServiceRegistration spamdservice_registration;

    public Activator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
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
        try {
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
            if(LOG.isDebugEnabled()){
                LOG.debug("Trying to register POA info servlet");
            }
            servletRegistrations.add(new SessionServletRegistration(context, new com.openexchange.custom.parallels.impl.ParallelsInfoServlet(), getFromConfig("com.openexchange.custom.parallels.sso_info_servlet")));
            servletRegistrations.add(new SessionServletRegistration(context, new com.openexchange.custom.parallels.impl.ParallelsOpenApiServlet(), getFromConfig("com.openexchange.custom.parallels.openapi_servlet")));
            if(LOG.isDebugEnabled()){
                LOG.debug("Successfully registered POA info servlet");
            }


            // register auth plugin
            authentication = new ParallelsOXAuthentication();
            if(LOG.isDebugEnabled()){
                LOG.debug("Trying to register POA authentication plugin");
            }
            authentication_registration = context.registerService(AuthenticationService.class.getName(), authentication, null);
            if(LOG.isDebugEnabled()){
                LOG.debug("Successfully registered POA authentication plugin");
            }

            // regitser hostname service to modify hostnames in directlinks
            hostnameservice = new ParallelsHostnameService();
            if(LOG.isDebugEnabled()){
                LOG.debug("Trying to register POA hostname/directlinks plugin");
            }
            hostname_registration = context.registerService(HostnameService.class.getName(), hostnameservice, null);
            if(LOG.isDebugEnabled()){
                LOG.debug("Successfully registered POA hostname/directlinks plugin");
            }
            spamdservice_registration = context.registerService(SpamdService.class.getName(), ParallelsSpamdService.getInstance(), null);
        } catch (final Throwable t) {
            LOG.error(t.getMessage(), t);
            throw t instanceof Exception ? (Exception) t : new Exception(t);
        }

    }

    @Override
    protected void stopBundle() throws Exception {
        try {

            // stop hostname service
            hostname_registration.unregister();
            hostnameservice = null;

            // stop authentication service
            authentication_registration.unregister();
            authentication = null;

            // stop info/sso servlet
            for(final SessionServletRegistration reg : servletRegistrations) {
                reg.remove();
            }
            servletRegistrations.clear();

            spamdservice_registration.unregister();

            ParallelsServiceRegistry.getServiceRegistry().clearRegistry();
        } catch (final Throwable t) {
            LOG.error(t.getMessage(), t);
            throw t instanceof Exception ? (Exception) t : new Exception(t);
        }
    }

    private String getFromConfig(final String key) throws OXException{
        final ConfigurationService configservice = ParallelsServiceRegistry.getServiceRegistry().getService(ConfigurationService.class,true);
        return configservice.getProperty(key);
    }

}
