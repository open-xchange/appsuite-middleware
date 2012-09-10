package com.openexchange.rss.actions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPGetRequestBuilder;
import com.openexchange.http.client.builder.HTTPRequestBuilder;
import com.openexchange.http.client.builder.HTTPResponse;
import com.openexchange.rss.RssServices;
import com.openexchange.rss.preprocessors.ImagePreprocessor;
import com.openexchange.rss.preprocessors.RssPreprocessor;
import com.openexchange.rss.preprocessors.WhitelistPreprocessor;
import com.openexchange.tools.session.ServerSession;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FetcherException;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.sun.syndication.io.FeedException;

public class RssAction implements AJAXActionService {
	private HttpURLFeedFetcher fetcher;
	private HashMapFeedInfoCache feedCache;
	
	public RssAction () {
		feedCache = new HashMapFeedInfoCache();
		fetcher = new HttpURLFeedFetcher(feedCache);
	}

	@Override
	public AJAXRequestResult perform(AJAXRequestData request, ServerSession session) throws OXException {
		String uriString = request.checkParameter("feedUrl");
		URL uri = null;
		try {
			uriString = URLDecoder.decode(uriString, "UTF-8");
			uri = new URL(uriString);
			
		} catch (UnsupportedEncodingException e) { 
			/* yeah, right... not happening for UTF-8 */ 
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			    
	    try {
			SyndFeed feed = fetcher.retrieveFeed(uri);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FeedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FetcherException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		RssPreprocessor preprocessor = new ImagePreprocessor().chain(new WhitelistPreprocessor());
		return new AJAXRequestResult("HELLO WORLD", "string");
	}
}
