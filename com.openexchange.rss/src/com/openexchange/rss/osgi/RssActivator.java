package com.openexchange.rss.osgi;

import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.html.HtmlService;
import com.openexchange.http.client.HTTPClient;
import com.openexchange.rss.RssServices;
import com.openexchange.rss.actions.RssActionFactory;
import com.openexchange.xml.jdom.JDOMParser;

public class RssActivator extends AJAXModuleActivator {

    private static final Class<?>[] NEEDED_SERVICES = {HTTPClient.class, HtmlService.class, JDOMParser.class};

    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }

    @Override
    protected void startBundle() throws Exception {
    	RssServices.LOOKUP.set(this);
    	registerModule(new RssActionFactory(), "rss");
    }

    @Override
    protected void stopBundle() throws Exception {
    	RssServices.LOOKUP.set(null);
    }

}
