package com.openexchange.subscribe.crawler.offering.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import com.openexchange.config.ConfigurationService;
import com.openexchange.server.osgiservice.Whiteboard;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;
import com.openexchange.subscribe.crawler.offering.CrawlerOfferingServlet;
import com.openexchange.templating.TemplateService;
import com.openexchange.tools.service.ServletRegistration;

public class Activator implements BundleActivator {

	private static final String ALIAS = "/publications/crawler";

    private Whiteboard whiteboard;

    private ServletRegistration servletRegistration;

    public void start(BundleContext context) throws Exception {
	    whiteboard = new Whiteboard(context);

	    CrawlerOfferingServlet.setSources(whiteboard.getService(SubscriptionSourceDiscoveryService.class));
        CrawlerOfferingServlet.setTemplateService(whiteboard.getService(TemplateService.class));
        CrawlerOfferingServlet.setConfigService(whiteboard.getService(ConfigurationService.class));
	    servletRegistration = new ServletRegistration(context, new CrawlerOfferingServlet(), ALIAS);
    }

	public void stop(BundleContext context) throws Exception {
        servletRegistration.remove();
	    whiteboard.close();
	}

}
