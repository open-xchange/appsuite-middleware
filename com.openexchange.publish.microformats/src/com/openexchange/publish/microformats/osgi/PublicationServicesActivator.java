
package com.openexchange.publish.microformats.osgi;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.publish.PublicationService;
import com.openexchange.publish.microformats.MicroformatServlet;
import com.openexchange.publish.microformats.OXMFPublicationService;

public class PublicationServicesActivator implements BundleActivator {

    private List<ServiceRegistration> serviceRegistrations = new ArrayList<ServiceRegistration>();
    
    private OXMFPublicationService contactPublisher;

    private List<String> aliases = new LinkedList<String>();
    
    public void start(BundleContext context) throws Exception {
        aliases.clear();
        contactPublisher = new OXMFPublicationService();
        contactPublisher.setFolderType("contacts");
        contactPublisher.setRootURL("/publications/contacts");
        contactPublisher.setTargetDisplayName("OXMF Contacts");
        contactPublisher.setTargetId("com.openexchange.publish.microformats.contacts.online");
        
        MicroformatServlet.registerType("contacts", contactPublisher, "contacts.tmpl");
        aliases.add("/publications/contacts");
        
        serviceRegistrations.add( context.registerService(PublicationService.class.getName(), contactPublisher, null) );
    }

    public void stop(BundleContext context) throws Exception {
        for (ServiceRegistration registration : serviceRegistrations) {
            registration.unregister();
        }
    }

    public List<String> getAliases() {
        return aliases;
    }



    
}
