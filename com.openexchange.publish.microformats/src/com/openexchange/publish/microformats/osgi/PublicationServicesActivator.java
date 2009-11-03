
package com.openexchange.publish.microformats.osgi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.publish.PublicationService;
import com.openexchange.publish.microformats.ContactPictureServlet;
import com.openexchange.publish.microformats.InfostoreFileServlet;
import com.openexchange.publish.microformats.MicroformatServlet;
import com.openexchange.publish.microformats.OXMFPublicationService;
import com.openexchange.publish.microformats.tools.ContactTemplateUtils;
import com.openexchange.publish.microformats.tools.InfostoreTemplateUtils;
import com.openexchange.templating.TemplateService;

public class PublicationServicesActivator implements BundleActivator {

    private List<ServiceRegistration> serviceRegistrations = new ArrayList<ServiceRegistration>();

    private OXMFPublicationService contactPublisher;

    private OXMFPublicationService infostorePublisher;

    public void start(BundleContext context) throws Exception {
        contactPublisher = new OXMFPublicationService();
        contactPublisher.setFolderType("contacts");
        contactPublisher.setRootURL("/publications/contacts");
        contactPublisher.setTargetDisplayName("OXMF Contacts");
        contactPublisher.setTargetId("com.openexchange.publish.microformats.contacts.online");
        contactPublisher.setDefaultTemplateName("contacts.tmpl");
        
        Map<String, Object> additionalVars = new HashMap<String, Object>();
        additionalVars.put("utils", new ContactTemplateUtils());
        
        MicroformatServlet.registerType("contacts", contactPublisher,additionalVars);
        ContactPictureServlet.setContactPublisher(contactPublisher);

        serviceRegistrations.add(context.registerService(PublicationService.class.getName(), contactPublisher, null));

        infostorePublisher = new OXMFPublicationService();
        infostorePublisher.setFolderType("infostore");
        infostorePublisher.setRootURL("/publications/infostore");
        infostorePublisher.setTargetDisplayName("OXMF Infostore");
        infostorePublisher.setTargetId("com.openexchange.publish.microformats.infostore.online");
        infostorePublisher.setDefaultTemplateName("infostore.tmpl");
        InfostoreFileServlet.setInfostorePublisher(infostorePublisher);

        HashMap<String, Object> infoAdditionalVars = new HashMap<String, Object>();
        infoAdditionalVars.put("utils", new InfostoreTemplateUtils());

        MicroformatServlet.registerType("infostore", infostorePublisher, infoAdditionalVars);

        serviceRegistrations.add(context.registerService(PublicationService.class.getName(), infostorePublisher, null));

    }

    public void stop(BundleContext context) throws Exception {
        for (ServiceRegistration registration : serviceRegistrations) {
            registration.unregister();
        }
    }
    
    public void setTemplateService(TemplateService templateService) {
        infostorePublisher.setTemplateService(templateService);
        contactPublisher.setTemplateService(templateService);
    }

}
