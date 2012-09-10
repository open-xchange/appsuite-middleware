package com.openexchange.rss;

import java.util.concurrent.atomic.AtomicReference;

import com.openexchange.html.HtmlService;
import com.openexchange.server.ServiceLookup;

public class RssServices {
	
    public static final AtomicReference<ServiceLookup> LOOKUP = new AtomicReference<ServiceLookup>();
    
    public static HtmlService getHtmlService() {
    	return LOOKUP.get().getService(HtmlService.class);
    }
}
