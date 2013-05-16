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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.ajax.requesthandler.responseRenderers;

import junit.framework.TestCase;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.html.HtmlService;
import com.openexchange.html.SimHtmlService;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link FileResponseRendererTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FileResponseRendererTest extends TestCase {

    /**
     * Initializes a new {@link FileResponseRendererTest}.
     */
    public FileResponseRendererTest() {
        super();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ServerServiceRegistry.getInstance().addService(HtmlService.class, new SimHtmlService());
    }

    @Override
    protected void tearDown() throws Exception {
        ServerServiceRegistry.getInstance().removeService(HtmlService.class);
        super.tearDown();
    }

    public void testProperContentLength() {
        try {
            final String html = "foo\n" + "<object/data=\"data:text/html;base64,PHNjcmlwdD5hbGVydCgiWFNTIFNjaHdhY2hzdGVsbGUiKTwvc2NyaXB0Pg==\"></object>\n" + "bar";
            final byte[] bytes = html.getBytes("ISO-8859-1");
            final int length = bytes.length;

            final ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(bytes);
            fileHolder.setContentType("text/html; charset=ISO-8859-1");
            fileHolder.setDelivery("view");
            fileHolder.setDisposition("inline");
            fileHolder.setName("document.html");

            final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();

            final AJAXRequestData requestData = new AJAXRequestData();
            final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
            final SimHttpServletRequest req = new SimHttpServletRequest();
            final SimHttpServletResponse resp = new SimHttpServletResponse();
            final ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
            resp.setOutputStream(servletOutputStream);
            fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);

            final int contentLength = resp.getContentLength();
            assertTrue("Unexpected Content-Length: " + contentLength, contentLength > 0);
            assertTrue("Unexpected Content-Length: " + contentLength + ", but should be less than " + length, length > contentLength);
            final int size = servletOutputStream.size();
            assertEquals("Unexpected Content-Length.", size, contentLength);
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
