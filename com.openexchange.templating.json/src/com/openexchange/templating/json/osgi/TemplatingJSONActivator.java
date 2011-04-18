package com.openexchange.templating.json.osgi;

import com.openexchange.multiple.MultipleHandler;
import com.openexchange.multiple.MultipleHandlerFactoryService;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.templating.TemplateService;
import com.openexchange.templating.json.TemplateMultipleHandler;
import com.openexchange.templating.json.TemplatingServlet;
import com.openexchange.tools.service.ServletRegistration;
import com.openexchange.tools.service.SessionServletRegistration;

public class TemplatingJSONActivator extends DeferredActivator {

    /**
     * 
     */
    private static final String SERVLET_PATH = "ajax/templating";
    private static final Class<?>[] NEEDED_SERVICES = new Class[]{TemplateService.class};
    private SessionServletRegistration registration;

    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }

    @Override
    protected void handleAvailability(Class<?> clazz) {
    }

    @Override
    protected void handleUnavailability(Class<?> clazz) {
    }

    @Override
    protected void startBundle() throws Exception {
        TemplateMultipleHandler.setServices(this);
        
        context.registerService(MultipleHandlerFactoryService.class.getName(), new MultipleHandlerFactoryService() {

            private final MultipleHandler HANDLER = new TemplateMultipleHandler();
            
            public MultipleHandler createMultipleHandler() {
                return HANDLER;
            }

            public String getSupportedModule() {
                return "templating";
            }
            
        }, null);
        
        registration = new SessionServletRegistration(context, new TemplatingServlet(), SERVLET_PATH);
    }

    @Override
    protected void stopBundle() throws Exception {
        registration.remove();
    }

}
