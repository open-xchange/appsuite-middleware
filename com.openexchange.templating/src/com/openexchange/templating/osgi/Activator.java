
package com.openexchange.templating.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exceptions.osgi.ComponentRegistration;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.server.osgiservice.ServiceDependentRegistration;
import com.openexchange.server.osgiservice.Whiteboard;
import com.openexchange.templating.TemplateErrorMessage;
import com.openexchange.templating.TemplateService;
import com.openexchange.templating.TemplateServiceImpl;
import com.openexchange.templating.impl.OXIntegration;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Activator implements BundleActivator {

    private ComponentRegistration componentRegistration;
    private Whiteboard whiteboard;
    private ServiceDependentRegistration<TemplateServiceImpl> serviceRegistration;

    
    public void start(BundleContext context) throws Exception {
        whiteboard = new Whiteboard(context);
        
        
        this.serviceRegistration = new ServiceDependentRegistration<TemplateServiceImpl>(context, TemplateService.class.getName(), null, whiteboard) {
            private ConfigurationService config;

            @Override
            public TemplateServiceImpl configure(TemplateServiceImpl service) {
                config = get(ConfigurationService.class);
                OXIntegration integration = new OXIntegration(get(InfostoreFacade.class));
                TemplateServiceImpl templates = new TemplateServiceImpl(config);
                templates.setOXFolderHelper(integration);
                templates.setInfostoreHelper(integration);
                return templates;
            }
            
            @Override
            public boolean validateServices() {
                boolean hasProperty = config.getProperty(TemplateServiceImpl.PATH_PROPERTY) != null;
                if(!hasProperty) {
                    LOG.warn(TemplateServiceImpl.PATH_PROPERTY+" is not set. Templating will remain inactive.");
                }
                return hasProperty;
            }
        };
        
        serviceRegistration.start();
        componentRegistration = new ComponentRegistration(context, "TMPL", "com.openexchange.templating", TemplateErrorMessage.EXCEPTIONS);
    }

    public void stop(BundleContext context) throws Exception {
        componentRegistration.unregister();
        serviceRegistration.close();
        whiteboard.close();
    }

}
