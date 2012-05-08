
package com.openexchange.spamsettings.generic.osgi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.http.HttpService;
import com.openexchange.exceptions.StringComponent;
import com.openexchange.exceptions.osgi.ComponentRegistration;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.server.osgiservice.ServiceRegistry;
import com.openexchange.spamsettings.generic.preferences.SpamSettingsModulePreferences;
import com.openexchange.spamsettings.generic.service.SpamSettingExceptionFactory;
import com.openexchange.spamsettings.generic.service.SpamSettingService;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ServletActivator extends DeferredActivator {

    private static transient final Log LOG = LogFactory.getLog(ServletActivator.class);

    private static final Class<?>[] NEEDED_SERVICES = { HttpService.class, SpamSettingService.class };

    private ComponentRegistration componentRegistration;

    private SpamSettingsServletRegisterer servletRegisterer;

    public ServletActivator() {
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

        SpamSettingsServiceRegistry.getServiceRegistry().addService(clazz, getService(clazz));
        servletRegisterer.registerServlet();
        SpamSettingsModulePreferences.setModule(true);
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Re-available service: " + clazz.getName());
        }
        servletRegisterer.unregisterServlet();
        SpamSettingsModulePreferences.setModule(false);
        SpamSettingsServiceRegistry.getServiceRegistry().removeService(clazz);

    }

    @Override
    protected void startBundle() throws Exception {
        componentRegistration = new ComponentRegistration(context, new StringComponent("SSG"), "com.openexchange.spamsetting.generic", SpamSettingExceptionFactory.getInstance());

        final ServiceRegistry registry = SpamSettingsServiceRegistry.getServiceRegistry();
        registry.clearRegistry();
        final Class<?>[] classes = getNeededServices();
        for (final Class<?> classe : classes) {
            final Object service = getService(classe);
            if (service != null) {
                registry.addService(classe, service);
            }
        }
        servletRegisterer = new SpamSettingsServletRegisterer();
        servletRegisterer.registerServlet();

    }

    @Override
    protected void stopBundle() throws Exception {
        if(componentRegistration != null) {
            componentRegistration.unregister();
        }


        servletRegisterer.unregisterServlet();
        servletRegisterer = null;
        SpamSettingsModulePreferences.setModule(false);
        SpamSettingsServiceRegistry.getServiceRegistry().clearRegistry();
    }
}
