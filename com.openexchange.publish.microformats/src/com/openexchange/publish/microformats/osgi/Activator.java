
package com.openexchange.publish.microformats.osgi;

import java.util.ArrayList;
import java.util.List;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.publish.PublicationService;
import com.openexchange.publish.microformats.OXMFPublicationService;

public class Activator implements BundleActivator {

    private List<ServiceRegistration> serviceRegistrations = new ArrayList<ServiceRegistration>();
    
    private OXMFPublicationService contactPublisher;

    public void start(BundleContext context) throws Exception {
        contactPublisher = new OXMFPublicationService();
        contactPublisher.setFolderType("contacts");
        contactPublisher.setRootURL("/publications/contacts");
        contactPublisher.setTargetDisplayName("OXMF Contacts");
        contactPublisher.setTargetId("com.openexchange.publish.microformats.contacts.online");
        
        serviceRegistrations.add( context.registerService(PublicationService.class.getName(), contactPublisher, null) );
    }

    public void stop(BundleContext context) throws Exception {
        for (ServiceRegistration registration : serviceRegistrations) {
            registration.unregister();
        }
    }



    
}
