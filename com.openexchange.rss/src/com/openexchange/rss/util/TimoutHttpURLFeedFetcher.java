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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.rss.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FetcherEvent;
import com.sun.syndication.fetcher.FetcherException;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.sun.syndication.fetcher.impl.SyndFeedInfo;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * {@link TimoutHttpURLFeedFetcher} - timeout-capable {@link HttpURLFeedFetcher}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.1
 */
public class TimoutHttpURLFeedFetcher extends HttpURLFeedFetcher {

    /** The timeout value, in milliseconds, to be used when opening a communications link to the resource */
    protected final int connectTimout;

    /** The read timeout to a specified timeout, in milliseconds */
    protected final int readTimout;

    /**
     * Initializes a new {@link TimoutHttpURLFeedFetcher}.
     *
     * @param connectTimout The timeout value, in milliseconds, to be used when opening a communications link to the resource
     * @param readTimout The read timeout to a specified timeout, in milliseconds
     */
    public TimoutHttpURLFeedFetcher(int connectTimout, int readTimout) {
        super();
        this.connectTimout = connectTimout;
        this.readTimout = readTimout;
    }

    /**
     * Initializes a new {@link TimoutHttpURLFeedFetcher}.
     *
     * @param connectTimout The timeout value, in milliseconds, to be used when opening a communications link to the resource
     * @param readTimout The read timeout to a specified timeout, in milliseconds
     * @param feedInfoCache The feed cache
     */
    public TimoutHttpURLFeedFetcher(int connectTimout, int readTimout, FeedFetcherCache feedInfoCache) {
        super(feedInfoCache);
        this.connectTimout = connectTimout;
        this.readTimout = readTimout;
    }

    /**
     * Retrieve a feed over HTTP
     *
     * @param feedUrl A non-null URL of a RSS/Atom feed to retrieve
     * @return A {@link com.sun.syndication.feed.synd.SyndFeed} object
     * @throws IllegalArgumentException if the URL is null;
     * @throws IOException if a TCP error occurs
     * @throws FeedException if the feed is not valid
     * @throws FetcherException if a HTTP error occurred
     */
    @Override
    public SyndFeed retrieveFeed(URL feedUrl) throws IllegalArgumentException, IOException, FeedException, FetcherException {
        if (feedUrl == null) {
            throw new IllegalArgumentException("null is not a valid URL");
        }

        URLConnection connection = feedUrl.openConnection();
        if (!(connection instanceof HttpURLConnection)) {
            throw new IllegalArgumentException(feedUrl.toExternalForm() + " is not a valid HTTP Url");
        }
        HttpURLConnection httpConnection = (HttpURLConnection)connection;
        // httpConnection.setInstanceFollowRedirects(true); // this is true by default, but can be changed on a claswide basis
        if (connectTimout > 0) {
            httpConnection.setConnectTimeout(connectTimout);
        }
        if (readTimout > 0) {
            httpConnection.setReadTimeout(readTimout);
        }

        FeedFetcherCache cache = getFeedInfoCache();
        if (cache == null) {
            fireEvent(FetcherEvent.EVENT_TYPE_FEED_POLLED, connection);
            InputStream inputStream = null;
            setRequestHeaders(connection, null);
            httpConnection.connect();
            try {
                inputStream = httpConnection.getInputStream();
                return getSyndFeedFromStream(inputStream, connection);
            } catch (java.io.IOException e) {
                handleErrorCodes(((HttpURLConnection)connection).getResponseCode());
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                httpConnection.disconnect();
            }
            // we will never actually get to this line
            return null;
        }
        // With cache
        SyndFeedInfo syndFeedInfo = cache.getFeedInfo(feedUrl);
        setRequestHeaders(connection, syndFeedInfo);
        httpConnection.connect();
        try {
            fireEvent(FetcherEvent.EVENT_TYPE_FEED_POLLED, connection);

            if (syndFeedInfo == null) {
                // this is a feed that hasn't been retrieved
                syndFeedInfo = new SyndFeedInfo();
                retrieveAndCacheFeed(feedUrl, syndFeedInfo, httpConnection);
            } else {
                // check the response code
                int responseCode = httpConnection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_NOT_MODIFIED) {
                    // the response code is not 304 NOT MODIFIED
                    // This is either because the feed server
                    // does not support condition gets
                    // or because the feed hasn't changed
                    retrieveAndCacheFeed(feedUrl, syndFeedInfo, httpConnection);
                } else {
                    // the feed does not need retrieving
                    fireEvent(FetcherEvent.EVENT_TYPE_FEED_UNCHANGED, connection);
                }
            }

            return syndFeedInfo.getSyndFeed();
        } finally {
            httpConnection.disconnect();
        }
    }

    private SyndFeed readSyndFeedFromStream(InputStream inputStream, URLConnection connection) throws IOException, IllegalArgumentException, FeedException {
        InputStream is;
        if ("gzip".equalsIgnoreCase(connection.getContentEncoding())) {
            // handle gzip encoded content
            is = new GZIPInputStream(inputStream, 65536);
        } else {
            is = new BufferedInputStream(inputStream, 65536);
        }

        //InputStreamReader reader = new InputStreamReader(is, ResponseHandler.getCharacterEncoding(connection));

        //SyndFeedInput input = new SyndFeedInput();

        XmlReader reader = null;
        if (connection.getHeaderField("Content-Type") != null) {
            reader = new XmlReader(is, connection.getHeaderField("Content-Type"), true);
        } else {
            reader = new XmlReader(is, true);
        }

        SyndFeedInput syndFeedInput = new SyndFeedInput();
        syndFeedInput.setPreserveWireFeed(isPreserveWireFeed());

        return syndFeedInput.build(reader);

    }

    private SyndFeed getSyndFeedFromStream(InputStream inputStream, URLConnection connection) throws IOException, IllegalArgumentException, FeedException {
        SyndFeed feed = readSyndFeedFromStream(inputStream, connection);
        fireEvent(FetcherEvent.EVENT_TYPE_FEED_RETRIEVED, connection, feed);
        return feed;
    }

}
