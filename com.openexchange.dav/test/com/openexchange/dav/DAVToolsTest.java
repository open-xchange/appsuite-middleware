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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.dav;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import com.google.common.collect.ImmutableMap;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;

/**
 * {@link DAVToolsTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
@RunWith(Parameterized.class)
public class DAVToolsTest {


    @Parameters
    public static List<Object[]> testData() {
        //@formatter:off
        return Arrays.asList(new Object[][] {
                {"/servlet/dav/", "/servlet/dav/", ImmutableMap.<String, String> builder()
                    .put("caldav", "/caldav")
                    .put("/caldav", "/caldav")
                    .put("/caldav/", "/caldav/")
                    .put("/photos/contactXY/image1.jpg", "/photos/contactXY/image1.jpg")
                    .build()
                },
                {"servlet/dav", "servlet/dav/", ImmutableMap.<String, String> builder()
                    .put("caldav", "/caldav")
                    .put("/caldav", "/caldav")
                    .put("/caldav/", "/caldav/")
                    .put("/photos/contactXY/image1.jpg", "/photos/contactXY/image1.jpg")
                    .build()
                },
                {"/servlet/dav/hidden/", "/servlet/dav/", ImmutableMap.<String, String> builder()
                    .put("caldav/", "/hidden/caldav/")
                    .put("/caldav", "/hidden/caldav")
                    .put("/caldav/", "/hidden/caldav/")
                    .put("/caldav/principals/foo/bar", "/hidden/caldav/principals/foo/bar")
                    .put("/caldav/principals/foo/bar/", "/hidden/caldav/principals/foo/bar/")
                    .put("/photos/contactXY/image1.jpg", "/hidden/photos/contactXY/image1.jpg")
                    .build()
                },
                {"/dav/", "/", ImmutableMap.<String, String> builder()
                    .put("caldav", "/dav/caldav")
                    .put("/caldav", "/dav/caldav")
                    .put("/caldav/", "/dav/caldav/")
                    .put("/photos/contactXY/image1.jpg", "/dav/photos/contactXY/image1.jpg")
                    .build()
                },
                {"/dav/", "", ImmutableMap.<String, String> builder()
                    .put("caldav", "/dav/caldav")
                    .put("/caldav", "/dav/caldav")
                    .put("/caldav/", "/dav/caldav/")
                    .put("/photos/contactXY/image1.jpg", "/dav/photos/contactXY/image1.jpg")
                    .build()
                },
        });
        //@formatter:on
    }

    private final String prefixPath;
    private final String proxyprefixPath;
    private final Map<String, String> rawToExpected;

    /**
     * Initializes a new {@link DAVToolsTest}.
     * 
     * @param prefixPath The prefix path
     * @param proxyPrefixPath The proxy prefix path
     * @param rawToExpected The paths to the expected paths after parsing
     */
    public DAVToolsTest(String prefixPath, String proxyPrefixPath, Map<String, String> rawToExpected) {
        super();
        this.prefixPath = prefixPath;
        this.proxyprefixPath = proxyPrefixPath;
        this.rawToExpected = rawToExpected;
    }

    @Mock
    private ConfigViewFactory factory;

    @Mock
    private ConfigView view;

    @Before
    public void setUp() throws Exception {
        /*
         * Initialize mocks
         */
        MockitoAnnotations.initMocks(this);

        // Mock used service classes
        PowerMockito.when(factory.getView()).thenReturn(view);
        PowerMockito.when(view.get(UnitTests.PREFIX_PATH_NAME, String.class)).thenReturn(prefixPath);
        PowerMockito.when(view.get(UnitTests.PROXY_PREFIX_PATH_NAME, String.class)).thenReturn(proxyprefixPath);
    }

    @Test
    public void testCorrectPath() {
        for (Entry<String, String> entry : rawToExpected.entrySet()) {
            String path = DAVTools.getExternalPath(factory, entry.getKey());
            Assert.assertEquals("Not the corect path", entry.getValue(), path);
        }
    }

}
