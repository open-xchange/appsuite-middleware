package com.openexchange.contact.aggregator.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.subscribe.SubscribeService;

public class Activator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {FolderService.class, ConfigurationService.class};
    }

    @Override
    protected void startBundle() throws Exception {
        final FolderService folders = getService(FolderService.class);
        final ConfigurationService config = getService(ConfigurationService.class);

        final MailFolderDiscoverer mailFolderDiscoverer = new MailFolderDiscoverer(folders);

        final ContactAggregator aggregator = new ContactAggregator();
        aggregator.add(new ContactFolderContactSourceFactory());
        aggregator.add(new EMailFolderContactSource(mailFolderDiscoverer, config.getIntProperty("com.openexchange.contact.aggregator.mailLimit", 3000)));

        final AggregatingSubscribeService subscribeService = new AggregatingSubscribeService(aggregator);

        registerService(SubscribeService.class, subscribeService);


    }



}
