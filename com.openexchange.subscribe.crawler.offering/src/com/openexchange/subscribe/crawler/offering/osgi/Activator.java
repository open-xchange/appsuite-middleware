package com.openexchange.subscribe.crawler.offering.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.Whiteboard;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;
import com.openexchange.subscribe.crawler.offering.CrawlerOfferingServlet;
import com.openexchange.templating.TemplateService;
import com.openexchange.tools.service.ServletRegistration;

public class Activator extends HousekeepingActivator {

	private static final String ALIAS = "/publications/crawler";

    private Whiteboard whiteboard;

    private ServletRegistration servletRegistration;

    @Override
    public void startBundle() throws Exception {
	    whiteboard = new Whiteboard(context);

	    CrawlerOfferingServlet.setSources(whiteboard.getService(SubscriptionSourceDiscoveryService.class));
        CrawlerOfferingServlet.setTemplateService(whiteboard.getService(TemplateService.class));
        CrawlerOfferingServlet.setConfigService(whiteboard.getService(ConfigurationService.class));
	    servletRegistration = new ServletRegistration(context, new CrawlerOfferingServlet(), ALIAS);
    }

	@Override
    public void stopBundle() throws Exception {
        servletRegistration.remove();
	    whiteboard.close();
	}

    @Override
    protected Class<?>[] getNeededServices() {
        // TODO Auto-generated method stub
        return null;
    }

}
