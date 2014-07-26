/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */
package com.openexchange.rss.actions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlService;
import com.openexchange.rss.FeedByDateSorter;
import com.openexchange.rss.RssExceptionCodes;
import com.openexchange.rss.RssResult;
import com.openexchange.rss.RssServices;
import com.openexchange.rss.preprocessors.RssPreprocessor;
import com.openexchange.rss.preprocessors.SanitizingPreprocessor;
import com.openexchange.rss.util.TimoutHttpURLFeedFetcher;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FetcherException;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.ParsingFeedException;

/**
 * {@link RssAction} - The RSS action.
 */
public class RssAction implements AJAXActionService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RssAction.class);

    private static final int NOT_FOUND = 404;

	private final TimoutHttpURLFeedFetcher fetcher;
	private final HashMapFeedInfoCache feedCache;

	/**
	 * Initializes a new {@link RssAction}.
	 */
	public RssAction () {
		feedCache = new HashMapFeedInfoCache();
		fetcher = new TimoutHttpURLFeedFetcher(10000, 30000, feedCache);
	}

	@Override
    public AJAXRequestResult perform(AJAXRequestData request, ServerSession session) throws OXException {
        String sort = request.getParameter("sort"); // DATE or SOURCE
        if (sort == null) {
            sort = "DATE";
        }
        String order = request.getParameter("order"); // ASC or DESC
        if (order == null) {
            order = "DESC";
        }

        List<OXException> warnings = new LinkedList<OXException>();

        List<SyndFeed> feeds = new LinkedList<SyndFeed>();
        {
            String urlString = "";
            try {
                final JSONObject data = (JSONObject) request.requireData();
                final JSONArray test = data.optJSONArray("feedUrl");

                final List<URL> urls;
                if (test == null) {
                    urlString = request.checkParameter("feedUrl");
                    urlString = urlDecodeSafe(urlString);
                    urls = Collections.singletonList(new URL(prepareUrlString(urlString)));
                } else {
                    final int length = test.length();
                    urls = new ArrayList<URL>(length);
                    for (int i = 0; i < length; i++) {
                        urlString = test.getString(i);
                        urls.add(new URL(prepareUrlString(urlString)));
                    }
                }
                for (final URL url : urls) {
                    try {
                        feeds.add(fetcher.retrieveFeed(url));
                    } catch (final ParsingFeedException parsingException) {
                        final OXException oxe = RssExceptionCodes.INVALID_RSS.create(parsingException, url.toString());
                        if (1 == urls.size()) {
                            throw oxe;
                        }
                        oxe.setCategory(Category.CATEGORY_WARNING);
                        warnings.add(oxe);
                    } catch (final FeedException e) {
                        LOG.warn("Could not load RSS feed from: {}", url, e);
                    } catch (final FetcherException e) {
                        final int responseCode = e.getResponseCode();
                        if (responseCode <= 0) {
                            // No response code available
                            LOG.warn("Could not load RSS feed from: {}", url, e);
                        }
                        if (NOT_FOUND == responseCode) {
                            LOG.debug("Resource could not be found: {}", url, e);
                        } else {
                            LOG.warn("Could not load RSS feed from: {}", url, e);
                        }
                    }
                }

            } catch (final UnsupportedEncodingException e) {
                /* yeah, right... not happening for UTF-8 */
            } catch (final MalformedURLException e) {
                throw AjaxExceptionCodes.IMVALID_PARAMETER.create(e, urlString);
            } catch (final IllegalArgumentException e) {
                throw AjaxExceptionCodes.IMVALID_PARAMETER.create(e, e.getMessage());
            } catch (final IOException e) {
                throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
            } catch (final JSONException e) {
                throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
            }
        }

        List<RssResult> results = new ArrayList<RssResult>(feeds.size());
        for (SyndFeed feed : feeds) {
            for (Object obj : feed.getEntries()) {
                SyndEntry entry = (SyndEntry) obj;
                RssResult result = new RssResult()
                	.setAuthor(entry.getAuthor())
                	.setSubject(getTitle(entry))
                	.setUrl(entry.getLink())
                	.setFeedUrl(feed.getLink())
                	.setFeedTitle(feed.getTitle())
                	.setDate(entry.getUpdatedDate(), entry.getPublishedDate(), new Date());

                if (feed.getImage() != null) {
                    result.setImageUrl(feed.getImage().getLink());
                }

                results.add(result);

                RssPreprocessor preprocessor = new SanitizingPreprocessor();
                @SuppressWarnings("unchecked") List<SyndContent> contents = entry.getContents();
                boolean foundHtml = false;
                for (SyndContent content : contents) {
                    if ("html".equals(content.getType())) {
                        foundHtml = true;
                        String htmlContent = preprocessor.process(content.getValue());
                        result.setBody(htmlContent).setFormat("text/html");
                        break;
                    }
                    if (!foundHtml) {
                        result.setBody(content.getValue()).setFormat(content.getType());
                    }
                }
            }
        }

        if (sort.equalsIgnoreCase("DATE")) {
            Collections.sort(results, new FeedByDateSorter(order));
        }
        return new AJAXRequestResult(results, "rss").addWarnings(warnings);
    }

    private String getTitle(SyndEntry entry) {
        final HtmlService htmlService = RssServices.getHtmlService();
        return null == htmlService ? entry.getTitle() : htmlService.sanitize(entry.getTitle(), null, true, null, null);
    }

	private static String urlDecodeSafe(final String urlString) throws MalformedURLException {
	    if (isEmpty(urlString)) {
            return urlString;
        }
	    try {
            final String ret = URLDecoder.decode(urlString, "ISO-8859-1");
            if (isAscii(ret)) {
                return checkUrl(ret);
            }
            return checkUrl(URLDecoder.decode(urlString, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return urlString;
        }
	}

	/**
     * Checks given URL string for syntactical correctness.
     *
     * @param sUrl The URL string
	 * @throws MalformedURLException If URL string is invalid
     */
    private static String checkUrl(final String sUrl) throws MalformedURLException {
        if (isEmpty(sUrl)) {
            // Nothing to check
            return sUrl;
        }
        final java.net.URL url = new java.net.URL(sUrl);
        final String protocol = url.getProtocol();
        if (!"http".equals(protocol) && !"https".equals(protocol)) {
            throw new MalformedURLException("Only http & https protocols supported.");
        }
        return sUrl;
    }

	/**
     * Checks whether the specified string's characters are ASCII 7 bit
     *
     * @param s The string to check
     * @return <code>true</code> if string's characters are ASCII 7 bit; otherwise <code>false</code>
     */
    private static boolean isAscii(final String s) {
        final int length = s.length();
        boolean isAscci = true;
        for (int i = 0; (i < length) && isAscci; i++) {
            isAscci = (s.charAt(i) < 128);
        }
        return isAscci;
    }

	private static String prepareUrlString(final String urlString) {
	    if (isEmpty(urlString)) {
            return urlString;
        }
	    final String tmp = urlString.toLowerCase(Locale.US);
        int pos = tmp.indexOf("http://");
        if (pos < 0) {
            pos = tmp.indexOf("https://");
        }
        return pos > 0 ? urlString.substring(pos) : urlString;
	}

	/** Checks for an empty string */
    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }
}
