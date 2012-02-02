package com.openexchange.mobile.configuration.generator.osgi;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.mobile.configuration.generator.MobileConfigServlet;
import com.openexchange.mobile.configuration.generator.configuration.ConfigurationException;
import com.openexchange.mobile.configuration.generator.configuration.MobileConfigProperties;
import com.openexchange.mobile.configuration.generator.configuration.Property;
import com.openexchange.mobile.configuration.generator.services.MobileConfigServiceRegistry;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.templating.TemplateService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.service.ServletRegistration;

public class Activator extends HousekeepingActivator {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(Activator.class));

    private static final Class<?>[] NEEDED_SERVICES = { ConfigurationService.class, TemplateService.class, ThreadPoolService.class };
    public static final String ALIAS = "/servlet/mobileconfig";

    private ServletRegistration registration;

    public Activator() {
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Re-available service: " + clazz.getName());
        }
        MobileConfigServiceRegistry.getServiceRegistry().addService(clazz, getService(clazz));

        register();
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        if (LOG.isWarnEnabled()) {
            LOG.warn("Absent service: " + clazz.getName());
        }
        MobileConfigServiceRegistry.getServiceRegistry().removeService(clazz);

        unregister();
    }

    @Override
    protected void startBundle() throws Exception {
        {
            final ServiceRegistry registry = MobileConfigServiceRegistry.getServiceRegistry();
            registry.clearRegistry();
            final Class<?>[] classes = getNeededServices();
            for (int i = 0; i < classes.length; i++) {
                final Object service = getService(classes[i]);
                if (null != service) {
                    registry.addService(classes[i], service);
                }
            }
        }

        checkConfiguration();

        register();

        // Test encoding:
        try {
            URLEncoder.encode("test", "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            LOG.error("Stopping mobileconfig bundle because UTF-8 charset encoding is not available: ", e);
            throw e;
        }
    }

    private void checkConfiguration() throws ConfigurationException {
        MobileConfigProperties.check(MobileConfigServiceRegistry.getServiceRegistry(), Property.values(), "Mobileconfig");
    }

    @Override
    protected void stopBundle() throws Exception {
        unregister();

        MobileConfigServiceRegistry.getServiceRegistry().clearRegistry();
    }

    private void register() {
        if(registration != null) {
            return;
        }

        registration = new ServletRegistration(context, new MobileConfigServlet(), ALIAS);
        LOG.info("MobileConfig servlet registered");
    }

    public void unregister() {
        if (registration == null) {
            return;
        }
        registration.remove();
        registration = null;
    }


}
