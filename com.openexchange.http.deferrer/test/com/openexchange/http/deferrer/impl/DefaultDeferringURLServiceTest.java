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

package com.openexchange.http.deferrer.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import junit.framework.TestCase;
import com.openexchange.dispatcher.DispatcherPrefixService;


/**
 * {@link DefaultDeferringURLServiceTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DefaultDeferringURLServiceTest extends TestCase {

    TestDeferringURLService service = new TestDeferringURLService();

    @Override
    protected void setUp() throws Exception {
        DefaultDeferringURLService.PREFIX.set(new DispatcherPrefixService() {
            
            @Override
            public String getPrefix() {
                return "/ajax/";
            }
        });
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        DefaultDeferringURLService.PREFIX.set(null);
    }

    public void testBasic() throws UnsupportedEncodingException {
        final String url = "http://mydomain.de/ajax/someModule";
        final String encodedUrl = URLEncoder.encode(url, "UTF-8");
        assertTransformed(url, "https://www.open-xchange.com/ajax/defer?redirect="+encodedUrl);
    }

    public void testEncoding() throws UnsupportedEncodingException {
        final String url = "http://mydomain.de/ajax/someModule?arg1=value1&arg2=value2";
        final String encodedUrl = URLEncoder.encode(url, "UTF-8");
        assertTransformed(url, "https://www.open-xchange.com/ajax/defer?redirect="+encodedUrl);

    }

    public void testNoDeferrer() {
        service.setUrl(null);
        final String url = "http://mydomain.de/ajax/someModule";
        assertTransformed(url, url);
    }

    public void testNull() {
        assertTrue(null == service.getDeferredURL(null));
    }

    public void assertTransformed(final String url, final String expectedTransformation) {
        final String deferredURL = service.getDeferredURL(url);
        assertNotNull("URL was null: ", deferredURL);
        assertEquals(expectedTransformation, deferredURL);
    }

    private static class TestDeferringURLService extends DefaultDeferringURLService {

        private String url = "https://www.open-xchange.com";

        public void setUrl(final String url) {
            this.url = url;
        }

        @Override
        public String getDeferrerURL() {
            return url;
        }

    }

}
