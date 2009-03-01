
package com.openexchange.tagging.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.tagging.TaggingService;
import com.openexchange.tagging.TaggingServiceImpl;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Activator implements BundleActivator {

    private ServiceRegistration registryTagging;

    public void start(BundleContext context) throws Exception {
        registryTagging = context.registerService(TaggingService.class.getName(), new TaggingServiceImpl(), null);
    }

    public void stop(BundleContext context) throws Exception {
        registryTagging.unregister();
    }

}
