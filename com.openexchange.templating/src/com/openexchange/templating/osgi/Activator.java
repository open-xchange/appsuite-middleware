
package com.openexchange.templating.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.exceptions.osgi.ComponentRegistration;
import com.openexchange.templating.TemplateErrorMessage;
import com.openexchange.templating.TemplateService;
import com.openexchange.templating.TemplateServiceImpl;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Activator implements BundleActivator {

    private ServiceRegistration templateServiceregistration;
    private ComponentRegistration componentRegistration;

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        templateServiceregistration = context.registerService(TemplateService.class.getName(), new TemplateServiceImpl(), null);
        componentRegistration = new ComponentRegistration(context, "TMPL", "com.openexchange.templating", TemplateErrorMessage.EXCEPTIONS);
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        templateServiceregistration.unregister();
        componentRegistration.unregister();
    }

}
