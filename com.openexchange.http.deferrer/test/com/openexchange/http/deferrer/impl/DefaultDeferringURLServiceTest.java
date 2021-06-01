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

package com.openexchange.http.deferrer.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.sim.SimHttpServletRequest;
import javax.servlet.http.sim.SimHttpServletResponse;
import javax.servlet.sim.ByteArrayServletOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.http.deferrer.servlet.DeferrerServlet;

/**
 * {@link DefaultDeferringURLServiceTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DefaultDeferringURLServiceTest {
    TestDeferringURLService service = new TestDeferringURLService();

    @Before
    public void setUp() {
        DefaultDeferringURLService.PREFIX.set(new DispatcherPrefixService() {

            @Override
            public String getPrefix() {
                return "/ajax/";
            }
        });
    }

    @After
    public void tearDown() {
        DefaultDeferringURLService.PREFIX.set(null);
    }

    @Test
    public void testBasic() throws UnsupportedEncodingException {
        final String url = "http://mydomain.de/ajax/someModule";
        final String encodedUrl = URLEncoder.encode(url, "UTF-8");
        assertTransformed(url, "https://www.open-xchange.com/ajax/defer?redirect=" + encodedUrl);
    }

    @Test
    public void testEncoding() throws UnsupportedEncodingException {
        final String url = "http://mydomain.de/ajax/someModule?arg1=value1&arg2=value2";
        final String encodedUrl = URLEncoder.encode(url, "UTF-8");
        assertTransformed(url, "https://www.open-xchange.com/ajax/defer?redirect=" + encodedUrl);

    }

    @Test
    public void testNoDeferrer() {
        service.setUrl(null);
        final String url = "http://mydomain.de/ajax/someModule";
        assertTransformed(url, url);
    }

    @Test
    public void testNull() {
        assertTrue(null == service.getDeferredURL(null, 0, 0));
    }

    @Test
    public void testForBug44583() throws Exception {
        final SimHttpServletRequest req = new SimHttpServletRequest();
        req.setParameter("redirect", "http://evil.hacker.com/ajax/someModule");

        final SimHttpServletResponse resp = new SimHttpServletResponse();
        final ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
        resp.setOutputStream(servletOutputStream);
        resp.setCharacterEncoding("UTF-8");

        DeferrerServlet deferrerServlet = new DeferrerServlet(null);
        deferrerServlet.service(req, resp);

        int status = resp.getStatus();
        assertTrue("Unexpected response code: " + status, HttpServletResponse.SC_BAD_REQUEST == status);

        String s = new String(servletOutputStream.toByteArray(), "UTF-8");
        assertTrue("Unexpected HTML response: " + s, s.indexOf("400 Bad Request") > 0);
    }

    public void assertTransformed(final String url, final String expectedTransformation) {
        final String deferredURL = service.getDeferredURL(url, 0, 0);
        assertNotNull("URL was null: ", deferredURL);
        assertEquals(expectedTransformation, deferredURL);
    }

    // ----------------------------------------------------------------------------------------------------------

    private static class TestDeferringURLService extends DefaultDeferringURLService {

        private String url = "https://www.open-xchange.com";

        /**
         * Initializes a new {@link DefaultDeferringURLServiceTest.TestDeferringURLService}.
         */
        TestDeferringURLService() {
            super();
        }

        public void setUrl(final String url) {
            this.url = url;
        }

        @Override
        public String getDeferrerURL(int userId, int contextId) {
            return url;
        }

    }

}
