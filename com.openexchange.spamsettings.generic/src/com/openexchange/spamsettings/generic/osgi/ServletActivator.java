
package com.openexchange.spamsettings.generic.osgi;

import org.apache.commons.logging.Log;
import org.osgi.service.http.HttpService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.osgi.DeferredActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.spamsettings.generic.preferences.SpamSettingsModulePreferences;
import com.openexchange.spamsettings.generic.service.SpamSettingService;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ServletActivator extends DeferredActivator {

    private static transient final Log LOG = com.openexchange.log.Log.loggerFor(ServletActivator.class);

    private SpamSettingsServletRegisterer servletRegisterer;

    public ServletActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HttpService.class, SpamSettingService.class, DispatcherPrefixService.class };
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
        SpamSettingsServletRegisterer.PREFIX.set(getService(DispatcherPrefixService.class));
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
        servletRegisterer.unregisterServlet();
        servletRegisterer = null;
        SpamSettingsModulePreferences.setModule(false);
        SpamSettingsServiceRegistry.getServiceRegistry().clearRegistry();
        SpamSettingsServletRegisterer.PREFIX.set(null);
    }
}
