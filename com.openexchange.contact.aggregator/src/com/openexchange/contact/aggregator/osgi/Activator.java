package com.openexchange.contact.aggregator.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.aggregator.AggregatingSubscribeService;
import com.openexchange.contact.aggregator.ContactAggregator;
import com.openexchange.contact.aggregator.ContactFolderContactSourceFactory;
import com.openexchange.contact.aggregator.EMailFolderContactSource;
import com.openexchange.contact.aggregator.MailFolderDiscoverer;
import com.openexchange.contact.aggregator.loginHandlers.AggregatedContactFolderLoginHandler;
import com.openexchange.database.DatabaseService;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.subscribe.SubscriptionExecutionService;

public class Activator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {        
        return new Class[] {FolderService.class, ConfigurationService.class, ConfigViewFactory.class, DatabaseService.class, SubscriptionExecutionService.class};
    }

    @Override
    protected void startBundle() throws Exception {
        final FolderService folders = getService(FolderService.class);
        final ConfigurationService config = getService(ConfigurationService.class);
        final ConfigViewFactory configs = getService(ConfigViewFactory.class);
        final DatabaseService databaseService = getService(DatabaseService.class);
        final SubscriptionExecutionService executor = getService(SubscriptionExecutionService.class);
        
        final MailFolderDiscoverer mailFolderDiscoverer = new MailFolderDiscoverer(folders);
        
        final ContactAggregator aggregator = new ContactAggregator();
        aggregator.setConfigViews(configs);
        aggregator.add(new ContactFolderContactSourceFactory(configs));
        aggregator.add(new EMailFolderContactSource(mailFolderDiscoverer, config.getIntProperty("com.openexchange.contact.aggregator.mailLimit", 3000)));
        
        final AggregatingSubscribeService subscribeService = new AggregatingSubscribeService(aggregator);
        
        //registerService(SubscribeService.class, subscribeService);
        registerService(LoginHandlerService.class, new AggregatedContactFolderLoginHandler(configs, databaseService, subscribeService, executor));
        
    }

	

}
