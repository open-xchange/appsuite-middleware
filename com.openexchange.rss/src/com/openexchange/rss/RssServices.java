package com.openexchange.rss;

import java.util.concurrent.atomic.AtomicReference;

import com.openexchange.html.HtmlService;
import com.openexchange.http.client.HTTPClient;
import com.openexchange.server.ServiceLookup;
import com.openexchange.xml.jdom.JDOMParser;

public class RssServices {
	
    public static final AtomicReference<ServiceLookup> LOOKUP = new AtomicReference<ServiceLookup>();
    
    public static HTTPClient getHttpClient() {
    	return LOOKUP.get().getService(HTTPClient.class);
    }
    
    public static HtmlService getHtmlService() {
    	return LOOKUP.get().getService(HtmlService.class);
    }
    
    public static JDOMParser getDOMParser() {
    	return LOOKUP.get().getService(JDOMParser.class);
    }
}
