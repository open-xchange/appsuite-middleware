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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlService;
import com.openexchange.java.Strings;
import com.openexchange.rss.RssExceptionCodes;
import com.openexchange.rss.RssResult;
import com.openexchange.rss.osgi.Services;
import com.openexchange.rss.preprocessors.RssPreprocessor;
import com.openexchange.rss.preprocessors.SanitizingPreprocessor;
import com.openexchange.rss.util.RssProperties;
import com.openexchange.rss.util.TimoutHttpURLFeedFetcher;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndImage;
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
    private static final int FORBIDDEN = 403;

    private static final Comparator<RssResult> ASC = new Comparator<RssResult>() {

        @Override
        public int compare(RssResult r1, RssResult r2) {
            return r1.getDate().compareTo(r2.getDate());
        }
    };

    private static final Comparator<RssResult> DESC = new Comparator<RssResult>() {

        @Override
        public int compare(RssResult r1, RssResult r2) {
            return r2.getDate().compareTo(r1.getDate());
        }
    };

    // ------------------------------------------------------------------------------------------------------------------------------

    private final TimoutHttpURLFeedFetcher fetcher;
    private final HashMapFeedInfoCache feedCache;

    /**
     * Initializes a new {@link RssAction}.
     */
    public RssAction() {
        feedCache = new HashMapFeedInfoCache();
        fetcher = new TimoutHttpURLFeedFetcher(10000, 30000, feedCache);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData request, ServerSession session) throws OXException {
        List<OXException> warnings = new LinkedList<OXException>();
        List<SyndFeed> feeds = null;

        try {
            List<URL> urls = getUrls(request);
            feeds = getAcceptedFeeds(urls, warnings);
        } catch (IllegalArgumentException | MalformedURLException e) {
            throw AjaxExceptionCodes.IMVALID_PARAMETER.create(e, e.getMessage());
        } catch (IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }

        if (feeds == null) {
            return new AJAXRequestResult(new ArrayList<RssResult>(), "rss").addWarnings(warnings);
        }

        List<RssResult> results = new ArrayList<RssResult>(feeds.size());
        boolean dropExternalImages = AJAXRequestDataTools.parseBoolParameter("drop_images", request, true);
        RssPreprocessor preprocessor = new SanitizingPreprocessor(dropExternalImages);

        // Iterate feeds
        for (SyndFeed feed : feeds) {
            if (feed == null) {
                continue;
            }

            // Iterate feed's entries
            for (Object obj : feed.getEntries()) {
                SyndEntry entry = (SyndEntry) obj;

                // Create appropriate RssResult instance
                RssResult result;
                try {
                    result = new RssResult().setAuthor(entry.getAuthor()).setSubject(sanitiseString(entry.getTitle())).setUrl(checkUrl(entry.getLink()));
                    result.setFeedUrl(checkUrl(feed.getLink())).setFeedTitle(sanitiseString(feed.getTitle())).setDate(entry.getUpdatedDate(), entry.getPublishedDate(), new Date());
                    // Check possible image
                    SyndImage image = feed.getImage();
                    if (image != null) {
                        result.setImageUrl(checkUrl(image.getUrl()));
                    }
                } catch (MalformedURLException e) {
                    throw RssExceptionCodes.INVALID_RSS.create(e, entry.getLink());
                }

                // Add to results list
                results.add(result);

                @SuppressWarnings("unchecked") List<SyndContent> contents = entry.getContents();
                boolean foundHtml = false;
                for (SyndContent content : contents) {
                    if ("html".equals(content.getType())) {
                        foundHtml = true;
                        String htmlContent = preprocessor.process(content.getValue(), result);
                        result.setBody(htmlContent).setFormat("text/html");
                        break;
                    }
                    if (!foundHtml) {
                        result.setBody(content.getValue()).setFormat(content.getType());
                    }
                }
            }
        }

        String sort = request.getParameter("sort"); // DATE or SOURCE
        if (sort == null) {
            sort = "DATE";
        }
        String order = request.getParameter("order"); // ASC or DESC
        if (order == null) {
            order = "DESC";
        }
        if (sort.equalsIgnoreCase("DATE")) {
            Collections.sort(results, "DESC".equalsIgnoreCase(order) ? DESC : ASC);
        }

        return new AJAXRequestResult(results, "rss").addWarnings(warnings);
    }

    /**
     * Retrieves all RSS URLs wrapped in the given request.
     * 
     * @param request - the {@link AJAXRequestData} containing all feed URLs
     * @return {@link List} with desired feed URLs for further processing
     * @throws OXException
     * @throws MalformedURLException
     * @throws JSONException
     */
    protected List<URL> getUrls(AJAXRequestData request) throws OXException, MalformedURLException, JSONException {
        List<URL> urls = new ArrayList<>();

        String urlString = "";
        JSONObject data = (JSONObject) request.requireData();
        JSONArray test = data.optJSONArray("feedUrl");
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
        return urls;
    }

    /**
     * Checks given URLs for validity (esp. host name blacklisting and port whitelisting) and adds not accepted feeds to the provided warnings list. Returns a list of accepted feeds for further processing
     * 
     * @param urls - List of {@link URL}s to check
     * @param warnings - List of {@link OXException} that might be enhanced by possible errors
     * @return {@link List} of {@link SyndFeed}s that have been accepted for further processing
     * @throws OXException
     */
    protected List<SyndFeed> getAcceptedFeeds(List<URL> urls, List<OXException> warnings) throws OXException {
        List<SyndFeed> feeds = new LinkedList<SyndFeed>();

        for (URL url : urls) {
            if (RssProperties.isDenied(url.getHost(), url.getPort())) {
                final OXException oxe = RssExceptionCodes.RSS_CONNECTION_ERROR.create(url.toString());
                warnings.add(oxe);
                continue;
            }
            try {
                feeds.add(fetcher.retrieveFeed(url));
            } catch (java.net.SocketTimeoutException e) {
                OXException oxe = RssExceptionCodes.TIMEOUT_ERROR.create(e, url.toString());
                if (1 == urls.size()) {
                    throw oxe;
                }
                warnings.add(oxe);
            } catch (UnsupportedEncodingException e) {
                /* yeah, right... not happening for UTF-8 */
            } catch (IOException e) {
                OXException oxe = RssExceptionCodes.IO_ERROR.create(e, e.getMessage(), url.toString());
                if (1 == urls.size()) {
                    throw oxe;
                }
                warnings.add(oxe);
            } catch (ParsingFeedException parsingException) {
                Throwable t = parsingException.getCause();
                if (t != null && t instanceof IOException) {
                    String exceptionMessage = t.getMessage();
                    if (!Strings.isEmpty(exceptionMessage) && exceptionMessage.contains("exceeded")) {
                        ConfigurationService configService = Services.getService(ConfigurationService.class);
                        int maximumAllowedSize = configService.getIntProperty("com.openexchange.messaging.rss.feed.size", 4194304);
                        OXException oxe = RssExceptionCodes.RSS_SIZE_EXCEEDED.create(FileUtils.byteCountToDisplaySize(maximumAllowedSize), maximumAllowedSize);
                        if (1 == urls.size()) {
                            throw oxe;
                        }
                        warnings.add(oxe);
                    }
                }
                final OXException oxe = RssExceptionCodes.INVALID_RSS.create(parsingException, url.toString());
                if (1 == urls.size()) {
                    throw oxe;
                }
                oxe.setCategory(Category.CATEGORY_WARNING);
                warnings.add(oxe);
            } catch (FeedException e) {
                LOG.warn("Could not load RSS feed from: {}", url, e);
            } catch (FetcherException e) {
                int responseCode = e.getResponseCode();
                if (responseCode <= 0) {
                    // No response code available
                    LOG.warn("Could not load RSS feed from: {}", url, e);
                }
                if (NOT_FOUND == responseCode) {
                    LOG.debug("Resource could not be found: {}", url, e);
                } else if (FORBIDDEN == responseCode) {
                    LOG.debug("Authentication required for resource: {}", url, e);
                } else if (responseCode >= 500 && responseCode < 600) {
                    OXException oxe = RssExceptionCodes.RSS_HTTP_ERROR.create(e, Integer.valueOf(responseCode), url);
                    if (1 == urls.size()) {
                        throw oxe;
                    }
                    oxe.setCategory(Category.CATEGORY_WARNING);
                    warnings.add(oxe);
                } else {
                    LOG.warn("Could not load RSS feed from: {}", url, e);
                }
            } catch (IllegalArgumentException e) {
                String exceptionMessage = e.getMessage();
                if (!"Invalid document".equals(exceptionMessage)) {
                    throw AjaxExceptionCodes.IMVALID_PARAMETER.create(e, exceptionMessage);
                }
                // There is no parser for current document
                LOG.warn("Could not load RSS feed from: {}", url);
            }
        }
        return feeds;
    }

    /**
     * Sanitises the specified string via the {@link HtmlService}
     * 
     * @param string The string to sanitise
     * @return The sanitised string if the {@link HtmlService} is available
     */
    private static String sanitiseString(String string) {
        final HtmlService htmlService = Services.getService(HtmlService.class);
        if (htmlService == null) {
            LOG.warn("The HTMLService is unavailable at the moment, thus the RSS string '{}' might not be sanitised", string);
            return string;
        }
        return htmlService.sanitize(string, null, true, null, null);
    }

    private static String urlDecodeSafe(final String urlString) throws MalformedURLException {
        if (com.openexchange.java.Strings.isEmpty(urlString)) {
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
        if (com.openexchange.java.Strings.isEmpty(sUrl)) {
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
        if (com.openexchange.java.Strings.isEmpty(urlString)) {
            return urlString;
        }
        final String tmp = urlString.toLowerCase(Locale.US);
        int pos = tmp.indexOf("http://");
        if (pos < 0) {
            pos = tmp.indexOf("https://");
        }
        return pos > 0 ? urlString.substring(pos) : urlString;
    }
}
