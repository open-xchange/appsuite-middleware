/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
