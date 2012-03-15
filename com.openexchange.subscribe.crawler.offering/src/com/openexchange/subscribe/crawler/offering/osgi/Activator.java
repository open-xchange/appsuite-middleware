
package com.openexchange.subscribe.crawler.offering.osgi;

import org.osgi.service.http.HttpService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;
import com.openexchange.subscribe.crawler.offering.CrawlerOfferingServlet;
import com.openexchange.templating.TemplateService;

public class Activator extends HousekeepingActivator {

    private static final String ALIAS = "/publications/crawler";

    @Override
    public void startBundle() throws Exception {
        CrawlerOfferingServlet.setSources(getService(SubscriptionSourceDiscoveryService.class));
        CrawlerOfferingServlet.setTemplateService(getService(TemplateService.class));
        CrawlerOfferingServlet.setConfigService(getService(ConfigurationService.class));
        getService(HttpService.class).registerServlet(ALIAS, new CrawlerOfferingServlet(), null, null);
    }

    @Override
    public void stopBundle() throws Exception {
        getService(HttpService.class).unregister(ALIAS);
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { SubscriptionSourceDiscoveryService.class, TemplateService.class, ConfigurationService.class, HttpService.class };
    }

}
