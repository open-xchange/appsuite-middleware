package com.openexchange.recaptcha.osgi;

import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exceptions.osgi.ComponentRegistration;
import com.openexchange.recaptcha.ReCaptchaService;
import com.openexchange.recaptcha.ReCaptchaServlet;
import com.openexchange.recaptcha.impl.ReCaptchaServiceImpl;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.server.osgiservice.ServiceRegistry;

public class Activator extends DeferredActivator {
    
    private static final Log LOG = LogFactory.getLog(Activator.class);
    
    private static final String ALIAS = "/ajax/recaptcha";
    
    private ReCaptchaServlet servlet;

    private ServiceRegistration serviceRegistration;
    private ComponentRegistration componentRegistration;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, HttpService.class };
    }

    @Override
    protected void handleAvailability(Class<?> clazz) {
        registerServlet();
    }

    @Override
    protected void handleUnavailability(Class<?> clazz) {
        unregisterServlet();
    }

    @Override
    protected void startBundle() throws Exception {
        ServiceRegistry registry = ReCaptchaServiceRegistry.getInstance();
        registry.clearRegistry();
        Class<?>[] classes = getNeededServices();
        for (int i = 0; i < classes.length; i++) {
            Object service = getService(classes[i]);
            if (service != null) {
                registry.addService(classes[i], service);
            }
        }
        
        ConfigurationService config = registry.getService(ConfigurationService.class);
        Properties props = config.getFile("recaptcha.properties");
        Properties options = config.getFile("recaptcha_options.properties");
        ReCaptchaServiceImpl reCaptchaService = new ReCaptchaServiceImpl(props, options);
        serviceRegistration = context.registerService(ReCaptchaService.class.getName(), reCaptchaService, null);
        registry.addService(ReCaptchaService.class, reCaptchaService);
        
        registerServlet();
    }

    @Override
    protected void stopBundle() throws Exception {
        if(componentRegistration != null) {
            componentRegistration.unregister();
            componentRegistration = null;
        }
        
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            serviceRegistration = null;
        }
        
        unregisterServlet();
        ReCaptchaServiceRegistry.getInstance().clearRegistry();
    }
    
    private void registerServlet() {
        ServiceRegistry registry = ReCaptchaServiceRegistry.getInstance();
        HttpService httpService = registry.getService(HttpService.class);
        if(servlet == null) {
            try {
                httpService.registerServlet(ALIAS, servlet = new ReCaptchaServlet(), null, null);
                LOG.info("reCAPTCHA Servlet registered.");
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }
    
    private void unregisterServlet() {
        HttpService httpService = getService(HttpService.class);
        if(httpService != null && servlet != null) {
            httpService.unregister(ALIAS);
            servlet = null;
            LOG.info("reCAPTCHA Servlet unregistered.");
        }
    }

}
