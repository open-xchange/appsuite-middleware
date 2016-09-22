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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.rss.RssExceptionCodes;
import com.openexchange.rss.osgi.Services;
import com.openexchange.rss.util.RssProperties;
import com.openexchange.rss.util.TimoutHttpURLFeedFetcher;
import com.openexchange.test.mock.MockUtils;
import com.openexchange.tools.session.ServerSession;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.fetcher.FetcherException;
import com.sun.syndication.io.FeedException;

/**
 * {@link RssActionTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Services.class })
public class RssActionTest {

    private RssAction action;

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private TimoutHttpURLFeedFetcher fetcher;

    @Mock
    private ConfigViewFactory configViewFactory;

    @Mock
    private ConfigView configView;

    @Mock
    private ServerSession session;

    List<URL> urls = new ArrayList<>();

    List<OXException> warnings = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(Services.class);
        PowerMockito.mockStatic(ConfigViewFactory.class);
        Mockito.when(Services.optService(ConfigurationService.class)).thenReturn(configurationService);
        Mockito.when(Services.getService(ConfigurationService.class)).thenReturn(configurationService);
        Mockito.when(Services.getService(ConfigViewFactory.class)).thenReturn(configViewFactory);
        Mockito.when(Services.optService(ConfigViewFactory.class)).thenReturn(configViewFactory);
        Mockito.when(configurationService.getProperty("com.openexchange.messaging.rss.feed.blacklist", RssProperties.HOST_BLACKLIST_DEFAULT)).thenReturn(RssProperties.HOST_BLACKLIST_DEFAULT);
        Mockito.when(configurationService.getProperty("com.openexchange.messaging.rss.feed.whitelist.ports", RssProperties.PORT_WHITELIST_DEFAULT)).thenReturn(RssProperties.PORT_WHITELIST_DEFAULT);
        Mockito.when(configurationService.getProperty(RssProperties.SCHEMES_KEY, RssProperties.SCHEMES_DEFAULT)).thenReturn(RssProperties.SCHEMES_DEFAULT);
        Mockito.when(configViewFactory.getView()).thenReturn(configView);
        Mockito.when(configViewFactory.getView(Mockito.anyInt(), Mockito.anyInt())).thenReturn(configView);
        Mockito.when(configView.get("com.openexchange.net.ssl.user.configuration.enabled", Boolean.class)).thenReturn(Boolean.TRUE);
        Mockito.when(session.getUserId()).thenReturn(0);
        Mockito.when(session.getContextId()).thenReturn(0);

        action = new RssAction();
        MockitoAnnotations.initMocks(this);

        MockUtils.injectValueIntoPrivateField(action, "fetcher", fetcher);
    }

    // tests bug 45402: SSRF at RSS feeds
    @Test
    public void testGetAcceptedFeeds_emptyURLs_returnNoWarningAndNoFeed() throws OXException {
        List<SyndFeed> acceptedFeedsFromUrls = action.getAcceptedFeeds(urls, warnings, session);

        assertEquals(0, acceptedFeedsFromUrls.size());
        assertEquals(0, warnings.size());
    }

    // tests bug 45402: SSRF at RSS feeds
    @Test
    public void testGetAcceptedFeeds_localhostURL_returnWarning() throws OXException, MalformedURLException {
        urls.add(new URL("http://localhost:80"));
        List<SyndFeed> acceptedFeedsFromUrls = action.getAcceptedFeeds(urls, warnings, session);

        assertEquals(0, acceptedFeedsFromUrls.size());
        assertEquals(1, warnings.size());
    }

    // tests bug 45402: SSRF at RSS feeds
    @Test
    public void testGetAcceptedFeeds_localhostOnly_returnWarning() throws OXException, MalformedURLException {
        urls.add(new URL("http://localhost"));
        List<SyndFeed> acceptedFeedsFromUrls = action.getAcceptedFeeds(urls, warnings, session);

        assertEquals(0, acceptedFeedsFromUrls.size());
        assertEquals(1, warnings.size());
    }

    // tests bug 45402: SSRF at RSS feeds
    @Test
    public void testGetAcceptedFeeds_LocalhostNotAllowedAndAllowedUrl_returnWarningAndFeed() throws OXException, MalformedURLException {
        urls.add(new URL("http://localhost:80"));
        urls.add(new URL("http://guteStube.com"));

        List<SyndFeed> acceptedFeedsFromUrls = action.getAcceptedFeeds(urls, warnings, session);

        assertEquals(1, acceptedFeedsFromUrls.size());
        assertEquals(1, warnings.size());
    }

    // tests bug 45402: SSRF at RSS feeds
    @Test
    public void testGetAcceptedFeeds_LocalhostNotAllowedAndAllowedUrl_checkCorrectAdded() throws OXException, IllegalArgumentException, IOException, FeedException, FetcherException {
        urls.add(new URL("http://localhost:80"));
        URL guteStube = new URL("http://guteStube.com");
        urls.add(guteStube);
        SyndFeed syndFeedImpl = new SyndFeedImpl();
        syndFeedImpl.setUri(guteStube.toString());
        Mockito.when(fetcher.retrieveFeed(guteStube)).thenReturn(syndFeedImpl);

        List<SyndFeed> acceptedFeedsFromUrls = action.getAcceptedFeeds(urls, warnings, session);

        assertEquals(guteStube.toString(), acceptedFeedsFromUrls.get(0).getUri());
    }

    // tests bug 45402: SSRF at RSS feeds
    @Test
    public void testGetAcceptedFeeds_LocalhostNotAllowedAndAllowedUrl_checkCorrectWarning() throws OXException, MalformedURLException {
        URL localhost = new URL("http://localhost:80");
        urls.add(localhost);
        urls.add(new URL("http://guteStube.com"));

        action.getAcceptedFeeds(urls, warnings, session);

        assertTrue(RssExceptionCodes.RSS_CONNECTION_ERROR.create(localhost.toString()).similarTo(warnings.get(0)));
    }

    // tests bug 45402: SSRF at RSS feeds
    @Test
    public void testGetAcceptedFeeds_TwoNotAllowed_returnWarningAndFeed() throws OXException, MalformedURLException {
        urls.add(new URL("http://localhost:80"));
        urls.add(new URL("http://guteStube.com:77"));

        List<SyndFeed> acceptedFeedsFromUrls = action.getAcceptedFeeds(urls, warnings, session);

        assertEquals(0, acceptedFeedsFromUrls.size());
        assertEquals(2, warnings.size());
    }

    // tests bug 45402: SSRF at RSS feeds
    @Test
    public void testGetAcceptedFeeds_TwoNotAllowedWithPathAndFile_returnWarningAndFeed() throws OXException, MalformedURLException {
        urls.add(new URL("http://localhost:80/this/is/nice"));
        urls.add(new URL("http://guteStube.com:77/tritt/mich.rss"));

        List<SyndFeed> acceptedFeedsFromUrls = action.getAcceptedFeeds(urls, warnings, session);

        assertEquals(0, acceptedFeedsFromUrls.size());
        assertEquals(2, warnings.size());
    }

    // tests bug 45402: SSRF at RSS feeds
    @Test
    public void testGetAcceptedFeeds_multipleCorrectAndInvalid_returnCorrectData() throws OXException, MalformedURLException {
        urls.add(new URL("http://tollerLaden.de:80/this/is/nice"));
        urls.add(new URL("http://tollerLaden.de:88/this/is/not/nice"));
        urls.add(new URL("http://tollerLaden2.de/this/is/nice/too"));
        urls.add(new URL("http://tollerLaden.de/this/is/nice/too/asFile.xml"));
        urls.add(new URL("https://tollerLaden.de:80/this/is/secured/nice"));
        urls.add(new URL("https://tollerLaden.de:88/this/is/secured/not/nice"));
        urls.add(new URL("https://tollerLaden.de/this/is/secured/nice/too"));
        urls.add(new URL("https://tollerLaden2.de/this/is/secured/nice/too/asFile.xml"));
        urls.add(new URL("http://127.0.0.1:80/this/is/never/nice"));
        urls.add(new URL("http://127.0.0.1:88/this/is/never/not/nice"));
        urls.add(new URL("http://127.0.0.1/this/is/never/nice/too"));
        urls.add(new URL("http://127.0.0.1/this/is/never/nice/too/asFile.xml"));
        urls.add(new URL("https://127.0.0.1/this/is/secured/never/nice"));
        urls.add(new URL("https://127.0.0.1:88/this/is/secured/never/d/nice"));
        urls.add(new URL("https://127.0.0.1/this/is/secured/never/nice/too"));
        urls.add(new URL("https://127.0.0.1/this/is/secured/never/nice/too/asFile.xml"));

        List<SyndFeed> acceptedFeedsFromUrls = action.getAcceptedFeeds(urls, warnings, session);

        assertEquals(6, acceptedFeedsFromUrls.size());
        assertEquals(10, warnings.size());
    }

    // tests bug 47891: RSS reader allows to detect local files
    @Test
    public void testGetAcceptedFeeds_onlyInvalidSchemes_returnNoCorrectData() throws OXException, MalformedURLException {
        urls.add(new URL("netdoc://tollerLaden.de/this/is/secured/never/nice/too/asFile.xml"));
        urls.add(new URL("file://tollerLaden.de/this/is/secured/never/nice/too/asFile.xml"));
        urls.add(new URL("mailto://tollerLaden.de/this/is/secured/never/nice/too/asFile.xml"));
        urls.add(new URL("netdoc://tollerLaden.de/this/is/secured/never/nice"));
        urls.add(new URL("file://tollerLaden.de/"));
        urls.add(new URL("mailto://tollerLaden.de/this/is/"));

        List<SyndFeed> acceptedFeedsFromUrls = action.getAcceptedFeeds(urls, warnings, session);

        assertEquals(0, acceptedFeedsFromUrls.size());
        assertEquals(6, warnings.size());
    }

    // tests bug 47891: RSS reader allows to detect local files
    @Test
    public void testGetAcceptedFeeds_mixedValidAndInvalidSchemes_returnData() throws OXException, MalformedURLException {
        urls.add(new URL("netdoc://tollerLaden.de:80/is/secured/never/nice/too/asFile.xml"));
        urls.add(new URL("file://tollerLaden.de/is/secured/never/nice/too/asFile.xml"));
        urls.add(new URL("mailto://tollerLaden.de/this/is/secured/never/nice/too/asFile.xml"));
        urls.add(new URL("netdoc://tollerLaden.de/this/is/secured/never/nice"));
        urls.add(new URL("file://tollerLaden.de/"));
        urls.add(new URL("mailto://tollerLaden.de/this/is/"));
        urls.add(new URL("https://tollerLaden.de:80/this/is/secured/not/nice"));
        urls.add(new URL("https://tollerLaden2.de/this/is/secured/nice/too/asFile.xml"));
        urls.add(new URL("http://tollerLaden.de/this/is/never/nice"));
        urls.add(new URL("ftp://tollerLaden.de/this/is/never/nice/too/asFile.xml"));
        // should be used later when URI is supported
        //        urls.add(new URL("news://tollerLaden.de/this/is/never/nice/too/asFile.xml"));
        //        urls.add(new URL("feed://tollerLaden.de/this/is/never/nice/too/asFile.xml"));

        List<SyndFeed> acceptedFeedsFromUrls = action.getAcceptedFeeds(urls, warnings, session);

        assertEquals(4, acceptedFeedsFromUrls.size());
        assertEquals(6, warnings.size());
    }
}
