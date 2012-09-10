package com.openexchange.rss.osgi;

import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.html.HtmlService;
import com.openexchange.rss.RssJsonConverter;
import com.openexchange.rss.RssServices;
import com.openexchange.rss.actions.RssActionFactory;

public class RssActivator extends AJAXModuleActivator {

    private static final Class<?>[] NEEDED_SERVICES = {HtmlService.class};

    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }

    @Override
    protected void startBundle() throws Exception {
    	RssServices.LOOKUP.set(this);
    	registerModule(new RssActionFactory(), "rss");
    	registerService(ResultConverter.class, new RssJsonConverter());
    }

    @Override
    protected void stopBundle() throws Exception {
    	RssServices.LOOKUP.set(null);
    }

}
