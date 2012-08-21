package com.openexchange.recaptcha.osgi;

import java.util.Properties;
import org.apache.commons.logging.Log;
import org.osgi.service.http.HttpService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.log.LogFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.recaptcha.ReCaptchaService;
import com.openexchange.recaptcha.ReCaptchaServlet;
import com.openexchange.recaptcha.impl.ReCaptchaServiceImpl;

public class Activator extends HousekeepingActivator {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(Activator.class);

    private static final String ALIAS_APPENDIX = "recaptcha";

    private ReCaptchaServlet servlet;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, HttpService.class, DispatcherPrefixService.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        registerServlet();
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        unregisterServlet();
    }

    @Override
    protected void startBundle() throws Exception {
        final ServiceRegistry registry = ReCaptchaServiceRegistry.getInstance();
        registry.clearRegistry();
        final Class<?>[] classes = getNeededServices();
        for (int i = 0; i < classes.length; i++) {
            final Object service = getService(classes[i]);
            if (service != null) {
                registry.addService(classes[i], service);
            }
        }

        final ConfigurationService config = registry.getService(ConfigurationService.class);
        final Properties props = config.getFile("recaptcha.properties");
        final Properties options = config.getFile("recaptcha_options.properties");
        final ReCaptchaServiceImpl reCaptchaService = new ReCaptchaServiceImpl(props, options);
        registerService(ReCaptchaService.class, reCaptchaService, null);
        registry.addService(ReCaptchaService.class, reCaptchaService);

        registerServlet();
    }

    @Override
    protected void stopBundle() throws Exception {

        cleanUp();

        unregisterServlet();
        ReCaptchaServiceRegistry.getInstance().clearRegistry();
    }

    private void registerServlet() {
        final ServiceRegistry registry = ReCaptchaServiceRegistry.getInstance();
        final HttpService httpService = registry.getService(HttpService.class);
        if(servlet == null) {
            try {
                httpService.registerServlet(getService(DispatcherPrefixService.class).getPrefix() + ALIAS_APPENDIX, servlet = new ReCaptchaServlet(), null, null);
                LOG.info("reCAPTCHA Servlet registered.");
            } catch (final Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private void unregisterServlet() {
        final HttpService httpService = getService(HttpService.class);
        if(httpService != null && servlet != null) {
            httpService.unregister(getService(DispatcherPrefixService.class).getPrefix() + ALIAS_APPENDIX);
            servlet = null;
            LOG.info("reCAPTCHA Servlet unregistered.");
        }
    }

}
