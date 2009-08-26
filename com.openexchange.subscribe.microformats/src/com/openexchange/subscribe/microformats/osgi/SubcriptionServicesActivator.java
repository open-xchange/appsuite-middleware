
package com.openexchange.subscribe.microformats.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.exceptions.osgi.ComponentRegistration;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.microformats.MicroformatSubscribeService;
import com.openexchange.subscribe.microformats.OXMFParserFactoryService;
import com.openexchange.subscribe.microformats.OXMFSubscriptionErrorMessage;
import com.openexchange.subscribe.microformats.datasources.HTTPOXMFDataSource;
import com.openexchange.subscribe.microformats.parser.CybernekoOXMFFormParser;
import com.openexchange.subscribe.microformats.parser.HTMLMicroformatParserFactory;
import com.openexchange.subscribe.microformats.parser.OXMFFormParser;
import com.openexchange.subscribe.microformats.transformers.MapToContactObjectTransformer;
import com.openexchange.subscribe.microformats.transformers.MapToDocumentMetadataHolderTransformer;

public class SubcriptionServicesActivator implements BundleActivator {

    private ComponentRegistration componentRegistration;


    public void start(BundleContext context) throws Exception {
        componentRegistration = new ComponentRegistration(
            context,
            "MFS",
            "com.openexchange.subscribe.microformats",
            OXMFSubscriptionErrorMessage.EXCEPTIONS);

        HTTPOXMFDataSource dataSource = new HTTPOXMFDataSource();
        HTMLMicroformatParserFactory parserFactory = new HTMLMicroformatParserFactory();
        MapToContactObjectTransformer mapToContactObject = new MapToContactObjectTransformer();

        SubscriptionSource contactSubscriptionSource = new SubscriptionSource();
        contactSubscriptionSource.setDisplayName("OXMF Contacts");
        contactSubscriptionSource.setId("com.openexchange.subscribe.microformats.contacts.http");
        contactSubscriptionSource.setFolderModule(FolderObject.CONTACT);

        DynamicFormDescription form = new DynamicFormDescription();
        form.add(FormElement.input("url", "URL", true, null));
        contactSubscriptionSource.setFormDescription(form);

        MicroformatSubscribeService subscribeService = new MicroformatSubscribeService();
        subscribeService.setOXMFParserFactory(parserFactory);
        subscribeService.setOXMFSource(dataSource);
        subscribeService.setTransformer(mapToContactObject);
        subscribeService.setSource(contactSubscriptionSource);
        subscribeService.addContainerElement("ox_contact");
        subscribeService.addPrefix("ox_");

        contactSubscriptionSource.setSubscribeService(subscribeService);


        MapToDocumentMetadataHolderTransformer mapToDocumentMetadataHolder = new MapToDocumentMetadataHolderTransformer();

        SubscriptionSource infostoreSubscriptionSource = new SubscriptionSource();
        infostoreSubscriptionSource.setDisplayName("OXMF Infostore");
        infostoreSubscriptionSource.setId("com.openexchange.subscribe.microformats.infostore.http");
        infostoreSubscriptionSource.setFolderModule(FolderObject.INFOSTORE);

        infostoreSubscriptionSource.setFormDescription(form);

        MicroformatSubscribeService infostoreService = new MicroformatSubscribeService();
        infostoreService.setOXMFParserFactory(parserFactory);
        infostoreService.setOXMFSource(dataSource);
        infostoreService.setTransformer(mapToDocumentMetadataHolder);
        infostoreService.setSource(infostoreSubscriptionSource);
        infostoreService.addContainerElement("ox_infoitem");
        infostoreService.addPrefix("ox_");

        infostoreSubscriptionSource.setSubscribeService(infostoreService);
        
        context.registerService(OXMFParserFactoryService.class.getName(), parserFactory, null);
        context.registerService(OXMFFormParser.class.getName(), new CybernekoOXMFFormParser(), null);
    }

    public void stop(BundleContext context) throws Exception {
        componentRegistration.unregister();
    }

}
