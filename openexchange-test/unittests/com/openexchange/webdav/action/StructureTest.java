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

package com.openexchange.webdav.action;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

public abstract class StructureTest extends ActionTestCase {
    // noroot ?

    protected WebdavPath INDEX_HTML_URL = null;
    protected WebdavPath COPIED_INDEX_HTML_URL = null;
    private WebdavPath SITEMAP_HTML_URL = null;
    private WebdavPath DEVELOPMENT_URL = null;
    private WebdavPath PM_URL = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        INDEX_HTML_URL = testCollection.dup().append("index.html");
        COPIED_INDEX_HTML_URL = testCollection.dup().append("copied_index.html");
        SITEMAP_HTML_URL = testCollection.dup().append("sitemap.html");
        DEVELOPMENT_URL = testCollection.dup().append("development");
        PM_URL = testCollection.dup().append("pm");
    }

    public void testResource() throws Exception {

        final String content = getContent(INDEX_HTML_URL);

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);
        req.setHeader("Destination", COPIED_INDEX_HTML_URL.toString());

        final WebdavAction action = getAction(factory);
        action.perform(req, res);

        assertEquals(HttpServletResponse.SC_CREATED, res.getStatus());

        WebdavResource resource = factory.resolveResource(INDEX_HTML_URL);
        assertTrue(resource.exists());
        assertEquals(content, getContent(INDEX_HTML_URL));

        resource = factory.resolveResource(COPIED_INDEX_HTML_URL);
        assertTrue(resource.exists());
        assertEquals(content, getContent(COPIED_INDEX_HTML_URL));
    }

    public void testOverwrite() throws Exception {

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);
        req.setHeader("Destination", SITEMAP_HTML_URL.toString());
        req.setHeader("Overwrite", "F");

        final WebdavAction action = getAction(factory);
        try {
            action.perform(req, res);
            fail("Expected 412 Precondition Failed");
        } catch (final WebdavProtocolException x) {
            assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, x.getStatus());
        }
    }

    public void testSuccessfulOverwrite() throws Exception {

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);
        req.setHeader("Destination", SITEMAP_HTML_URL.toString());
        req.setHeader("Overwrite", "T");

        final WebdavAction action = getAction(factory);
        action.perform(req, res);

        assertEquals(HttpServletResponse.SC_NO_CONTENT, res.getStatus());

    }

    public void testOverwriteCollection() throws Exception {

        factory.resolveCollection(DEVELOPMENT_URL).resolveResource(new WebdavPath("test.html")).create();
        factory.resolveCollection(PM_URL).resolveResource(new WebdavPath("test.html")).create();

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(DEVELOPMENT_URL);
        req.setHeader("Destination", PM_URL.toString());
        req.setHeader("Overwrite", "F");

        final WebdavAction action = getAction(factory);
        try {
            action.perform(req, res);
            fail("Expected 412 Precondition Failed");
        } catch (final WebdavProtocolException x) {
            assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, x.getStatus());
        }

    }

    public void testMergeCollection() throws Exception {
        WebdavResource r = factory.resolveCollection(DEVELOPMENT_URL).resolveResource(new WebdavPath("test.html"));
        r.putBodyAndGuessLength(new ByteArrayInputStream(new byte[2]));
        r.create(); // FIXME

        r = factory.resolveCollection(PM_URL).resolveResource(new WebdavPath("test2.html"));
        r.putBodyAndGuessLength(new ByteArrayInputStream(new byte[2]));
        r.create(); // FIXME

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(DEVELOPMENT_URL);
        req.setHeader("Destination", PM_URL.toString());

        final WebdavAction action = getAction(factory);

        action.perform(req, res);

        //assertEquals(HttpServletResponse.SC_CREATED, res.getStatus());
        assertTrue(factory.resolveResource(PM_URL+"/test.html").exists());
    }

    public void testSame() throws Exception {

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(DEVELOPMENT_URL);
        req.setHeader("Destination", DEVELOPMENT_URL.toString());

        final WebdavAction action = getAction(factory);
        try {
            action.perform(req, res);
            fail("Expected 403 FORBIDDEN");
        } catch (final WebdavProtocolException x) {
            assertEquals(HttpServletResponse.SC_FORBIDDEN, x.getStatus());
        }
    }

    public void testConflict() throws Exception {

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(DEVELOPMENT_URL);
        req.setHeader("Destination", "/doesntExist/nonono");

        final WebdavAction action = getAction(factory);
        try {
            action.perform(req, res);
            fail("Expected 409 CONFLICT, 412 PRECONDITION FAILED or 207 MULTISTATUS");
        } catch (final WebdavProtocolException x) {
            StringWriter sw = new StringWriter();
            x.printStackTrace(new PrintWriter(sw));
            assertTrue(x.getStatus() + " - " + x.getMessage() + "\n" + sw.toString(), HttpServletResponse.SC_CONFLICT == x.getStatus()
                    || Protocol.SC_MULTISTATUS == x.getStatus()
                    || HttpServletResponse.SC_PRECONDITION_FAILED == x.getStatus()
            );
        }
    }



    public abstract WebdavAction getAction(WebdavFactory factory);
}
