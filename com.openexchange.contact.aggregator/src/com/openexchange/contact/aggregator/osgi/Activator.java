package com.openexchange.contact.aggregator.osgi;

import com.openexchange.api2.ContactSQLInterface;
import com.openexchange.config.ConfigurationService;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.server.osgiservice.HousekeepingActivator;
import com.openexchange.subscribe.SubscribeService;

public class Activator extends HousekeepingActivator {

    private static final Class[] NEEDED = {FolderService.class, ConfigurationService.class};
    
    @Override
    protected Class<?>[] getNeededServices() {        
        return NEEDED;
    }

    @Override
    protected void startBundle() throws Exception {
        FolderService folders = getService(FolderService.class);
        ConfigurationService config = getService(ConfigurationService.class);
        
        MailFolderDiscoverer mailFolderDiscoverer = new MailFolderDiscoverer(folders);
        
        ContactAggregator aggregator = new ContactAggregator();
        aggregator.add(new ContactFolderContactSourceFactory());
        aggregator.add(new EMailFolderContactSource(mailFolderDiscoverer, config.getIntProperty("com.openexchange.contact.aggregator.mailLimit", 3000)));
        
        AggregatingSubscribeService subscribeService = new AggregatingSubscribeService(aggregator);
        
        registerService(SubscribeService.class, subscribeService);
        
        
    }

	

}
