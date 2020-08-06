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
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.rss.osgi.Services;
import com.openexchange.rss.util.TimeoutHttpURLFeedFetcher;
import com.openexchange.rss.utils.RssProperties;
import com.openexchange.test.mock.MockUtils;
import com.sun.syndication.feed.synd.SyndFeed;

/**
 * {@link RssActionTestReconfiguredPorts}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Services.class, RssProperties.class, InetAddress.class })
public class RssActionTestReconfiguredPorts {

    private final ConfigurationService configurationService = Mockito.mock(ConfigurationService.class);

    private RssProperties rssProperties;

    List<URL> urls = new ArrayList<>();

    List<OXException> warnings = new ArrayList<>();

    // tests bug 45402: SSRF at RSS feeds
     @Test
     public void testGetAcceptedFeeds_emptyPortListConfigured_allowAllPorts() throws OXException, MalformedURLException, UnknownHostException {
        PowerMockito.mockStatic(Services.class);
        Mockito.when(Services.optService(ConfigurationService.class)).thenReturn(configurationService);
        Mockito.when(Services.getService(ConfigurationService.class)).thenReturn(configurationService);
        PowerMockito.mockStatic(InetAddress.class);
        InetAddress inetAddress = Mockito.mock(InetAddress.class);
        Mockito.when(InetAddress.getByName(ArgumentMatchers.anyString())).thenReturn(inetAddress);

        rssProperties = new RssProperties() {

            @Override
            public boolean isDenied(String uriString) {
                if (uriString.indexOf("localhost") >= 0) {
                    return true;
                }
                if (uriString.indexOf("127.0.0.1") >= 0) {
                    return true;
                }
                if (uriString.indexOf("netdoc://") >= 0) {
                    return true;
                }
                if (uriString.indexOf("file://") >= 0) {
                    return true;
                }
                if (uriString.indexOf("mailto://") >= 0) {
                    return true;
                }
                return false;
            }

            @Override
            public boolean isBlacklisted(String hostName) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isAllowedScheme(String scheme) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isAllowed(int port) {
                // TODO Auto-generated method stub
                return false;
            }
        };
        Mockito.when(Services.optService(RssProperties.class)).thenReturn(rssProperties);
        Mockito.when(Services.getService(RssProperties.class)).thenReturn(rssProperties);

        RssAction newAction = new RssAction();
        MockUtils.injectValueIntoPrivateField(newAction, "fetcher", Mockito.mock(TimeoutHttpURLFeedFetcher.class));

        Mockito.when(configurationService.getProperty("com.openexchange.messaging.rss.feed.whitelist.ports", RssProperties.DEFAULT_PORT_WHITELIST)).thenReturn("");
        Mockito.when(configurationService.getProperty("com.openexchange.messaging.rss.feed.blacklist", RssProperties.DEFAULT_HOST_BLACKLIST)).thenReturn(RssProperties.DEFAULT_HOST_BLACKLIST);
        Mockito.when(configurationService.getProperty(RssProperties.PROP_SCHEMES_WHITELIST, RssProperties.DEFAULT_SCHEMES_WHITELIST)).thenReturn(RssProperties.DEFAULT_SCHEMES_WHITELIST);

        urls.add(new URL("http://tollerLaden.de:80/this/is/not/nice"));
        urls.add(new URL("http://tollerLaden.de:88/this/is/not/nice"));
        urls.add(new URL("http://tollerLaden2.de/this/is/nice/too"));
        urls.add(new URL("http://tollerLaden.de/this/is/nice/too/asFile.xml"));
        urls.add(new URL("https://tollerLaden.de:80/this/is/secured/even/not/nice"));
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

        List<SyndFeed> acceptedFeedsFromUrls = newAction.getAcceptedFeeds(urls, warnings);

        assertEquals(8, acceptedFeedsFromUrls.size());
        assertEquals(8, warnings.size());
    }
}
