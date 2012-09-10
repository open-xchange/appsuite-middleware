package com.openexchange.rss.actions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.List;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.rss.RssResult;
import com.openexchange.rss.preprocessors.ImagePreprocessor;
import com.openexchange.rss.preprocessors.RssPreprocessor;
import com.openexchange.rss.preprocessors.WhitelistPreprocessor;
import com.openexchange.tools.session.ServerSession;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
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
		fetcher = new HttpURLFeedFetcher(); //feedCache); TODO use cache when done testing
	}

	@Override
	public AJAXRequestResult perform(AJAXRequestData request, ServerSession session) throws OXException {
		String uriString = request.checkParameter("feedUrl");
		URL uri = null;
		SyndFeed feed = null;
		try {
			uriString = URLDecoder.decode(uriString, "UTF-8");
			uri = new URL(uriString);
			feed = fetcher.retrieveFeed(uri);
		} catch (UnsupportedEncodingException e) { 
			/* yeah, right... not happening for UTF-8 */ 
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		List<RssResult> results = new LinkedList<RssResult>();
		
		for(Object obj : feed.getEntries()) {
			SyndEntry entry = (SyndEntry) obj;
			RssResult result = new RssResult()
				.setAuthor(entry.getAuthor())
				.setSubject(entry.getTitle())
				.setSource(entry.getUri());

			results.add(result);
			
			List<SyndContent> contents = entry.getContents();
			boolean foundHtml = false;
			for(SyndContent content: contents) {
				if ("html".equals(content.getType())) {
					foundHtml = true;
					String htmlContent = preprocessor.process(content.getValue());
					result
						.setBody(htmlContent)
						.setFormat("text/html");
					System.out.println(htmlContent);
					break;
				}
				if(!foundHtml) {
					result
						.setBody(content.getValue())
						.setFormat(content.getType());
				}
			}
		}
		return new AJAXRequestResult(results, "rss");
	}
}
