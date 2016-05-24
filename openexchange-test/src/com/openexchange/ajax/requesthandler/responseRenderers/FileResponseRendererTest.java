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

package com.openexchange.ajax.requesthandler.responseRenderers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.sim.SimHttpServletRequest;
import javax.servlet.http.sim.SimHttpServletResponse;
import javax.servlet.sim.ByteArrayServletOutputStream;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.cache.CachedResource;
import com.openexchange.ajax.requesthandler.cache.ResourceCache;
import com.openexchange.ajax.requesthandler.cache.ResourceCaches;
import com.openexchange.ajax.requesthandler.responseRenderers.FileResponseRendererTools.Delivery;
import com.openexchange.ajax.requesthandler.responseRenderers.FileResponseRendererTools.Disposition;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.SimConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlService;
import com.openexchange.html.SimHtmlService;
import com.openexchange.imagetransformation.BasicTransformedImage;
import com.openexchange.imagetransformation.ImageTransformationService;
import com.openexchange.imagetransformation.ImageTransformations;
import com.openexchange.imagetransformation.ScaleType;
import com.openexchange.imagetransformation.TransformedImage;
import com.openexchange.imagetransformation.java.impl.JavaImageTransformationProvider;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.image.WrappingImageTransformationService;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.SimServerSession;
import com.openexchange.tools.strings.BasicTypesStringParser;
import com.openexchange.tools.strings.StringParser;

/**
 * {@link FileResponseRendererTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FileResponseRendererTest extends TestCase {

    private final String TEST_DATA_DIR = "testconf/";

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
        final SimConfigurationService simConfigurationService = new SimConfigurationService();
        simConfigurationService.stringProperties.put("UPLOAD_DIRECTORY", "/tmp/");
        ServerServiceRegistry.getInstance().addService(ConfigurationService.class, simConfigurationService);
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
            ByteArrayFileHolder fileHolder = FileResponseRendererTools.getFileHolder(bytes, "text/html; charset=ISO-8859-1", Delivery.view, Disposition.inline, "document.html");

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

    public void testApplicationHtml_Bug35512() {
        try {
            String html = "foo\n" + "<object/data=\"data:text/html;base64,PHNjcmlwdD5hbGVydCgiWFNTIFNjaHdhY2hzdGVsbGUiKTwvc2NyaXB0Pg==\"></object>\n" + "bar";
            ByteArrayFileHolder fileHolder = FileResponseRendererTools.getFileHolder(html.getBytes(), "application/xhtml+xml", Delivery.view, Disposition.inline, "evil.html");

            AJAXRequestData requestData = new AJAXRequestData();
            {
                requestData.setSession(new SimServerSession(1, 1));
                requestData.putParameter("width", "10");
                requestData.putParameter("height", "10");
            }
            AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
            result.setExpires(Tools.getDefaultImageExpiry());
            SimHttpServletRequest req = new SimHttpServletRequest();
            SimHttpServletResponse resp = new SimHttpServletResponse();
            ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
            resp.setOutputStream(servletOutputStream);
            FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();
            fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);

            String s = new String(servletOutputStream.toByteArray());
            assertTrue("HTML content not sanitized: " + s, s.indexOf("<object/data") < 0);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testMaxAgeHeader_Bug33441() {
        try {
            ByteArrayFileHolder fileHolder = FileResponseRendererTools.getFileHolder("28082.jpg", "image/jpeg", Delivery.view, Disposition.inline, "28082.jpg");
            final AJAXRequestData requestData = new AJAXRequestData();
            {
                requestData.setSession(new SimServerSession(1, 1));
                requestData.putParameter("width", "10");
                requestData.putParameter("height", "10");
            }
            final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
            result.setExpires(Tools.getDefaultImageExpiry());
            final SimHttpServletRequest req = new SimHttpServletRequest();
            final SimHttpServletResponse resp = new SimHttpServletResponse();
            final ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
            resp.setOutputStream(servletOutputStream);
            final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();
            fileResponseRenderer.setScaler(new TestableImageTransformationService(IOUtils.toByteArray(fileHolder.getStream()), ImageTransformations.HIGH_EXPENSE));
            fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);

            assertTrue("HTTP header \"cache-control\" is missing", resp.containsHeader("cache-control"));

            String sCacheControl = resp.getHeaders().get("cache-control");
            assertTrue("HTTP header \"cache-control\" is missing", !Strings.isEmpty(sCacheControl));

            assertTrue("Invalid HTTP header \"cache-control\"", sCacheControl.indexOf("max-age=3600") > 0);
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testZeroByteTransformation_Bug28429() {
        try {
            ByteArrayFileHolder fileHolder = FileResponseRendererTools.getFileHolder("28429.jpg", "image/jpeg", Delivery.view, Disposition.inline, "28429.jpg");
            final AJAXRequestData requestData = new AJAXRequestData();
            {
                requestData.setSession(new SimServerSession(1, 1));
                requestData.putParameter("width", "10");
                requestData.putParameter("height", "10");
            }
            final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
            final SimHttpServletRequest req = new SimHttpServletRequest();
            final SimHttpServletResponse resp = new SimHttpServletResponse();
            {
                final ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
                resp.setOutputStream(servletOutputStream);
            }
            final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();
            fileResponseRenderer.setScaler(new TestableImageTransformationService(IOUtils.toByteArray(fileHolder.getStream()), ImageTransformations.HIGH_EXPENSE));
            fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);
            assertFalse("Got an error status: " + resp.getStatus(), resp.getStatus() >= 400);
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testContentTypeByFileName_Bug31648() {
        try {
            ByteArrayFileHolder fileHolder = FileResponseRendererTools.getFileHolder("31648.png", "application/binary", Delivery.view, Disposition.inline, "31648.png");
            final AJAXRequestData requestData = new AJAXRequestData();
            {
                requestData.setSession(new SimServerSession(1, 1));
                requestData.putParameter("width", "10");
                requestData.putParameter("height", "10");
            }
            final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
            final SimHttpServletRequest req = new SimHttpServletRequest();
            final SimHttpServletResponse resp = new SimHttpServletResponse();
            final ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
            resp.setOutputStream(servletOutputStream);

            MimeType2ExtMap.addMimeType("image/png", "png");

            final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();
            fileResponseRenderer.setScaler(new TestableImageTransformationService(IOUtils.toByteArray(fileHolder.getStream()), ImageTransformations.HIGH_EXPENSE));
            fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);
            final String expectedContentType = "image/png";
            final String currentContentType = resp.getContentType();
            assertEquals("Unexpected content-type.", expectedContentType, currentContentType);
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testRangeHeader_Bug27394() {
        try {
            ByteArrayFileHolder fileHolder = FileResponseRendererTools.getFileHolder("26926_27394.pdf", "application/pdf", Delivery.view, Disposition.inline, "26926_27394.pdf");
            final AJAXRequestData requestData = new AJAXRequestData();
            {
                requestData.setSession(new SimServerSession(1, 1));
                requestData.putParameter("width", "10");
                requestData.putParameter("height", "10");
            }
            final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
            final SimHttpServletRequest req = new SimHttpServletRequest();
            {
                req.setHeader("Range", "bytes=0-50");
            }
            final SimHttpServletResponse resp = new SimHttpServletResponse();
            final ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
            resp.setOutputStream(servletOutputStream);
            final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();
            fileResponseRenderer.setScaler(new TestableImageTransformationService(IOUtils.toByteArray(fileHolder.getStream()), ImageTransformations.HIGH_EXPENSE));
            fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);
            assertTrue("HTTP header \"accept-ranges\" is missing", resp.containsHeader("accept-ranges"));
            assertTrue("HTTP header \"content-range\" is missing", resp.containsHeader("content-range"));
            assertEquals("bytes", resp.getHeaders().get("accept-ranges"));
            assertEquals("bytes 0-50/3852226", resp.getHeaders().get("content-range"));
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void test404_Bug26848() {
        final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();

        // test with null file
        AJAXRequestData requestData = new AJAXRequestData();
        AJAXRequestResult result = new AJAXRequestResult();
        SimHttpServletRequest req = new SimHttpServletRequest();
        SimHttpServletResponse resp = new SimHttpServletResponse();
        ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
        resp.setOutputStream(servletOutputStream);
        fileResponseRenderer.write(requestData, result, req, resp);
        assertEquals("Wrong status code", 404, resp.getStatus());

        // test with empty filename
        ByteArrayFileHolder fileHolder = FileResponseRendererTools.getFileHolder(new byte[0], "application/octet-stream", Delivery.download, Disposition.attachment, "");

        resp = new SimHttpServletResponse();
        servletOutputStream = new ByteArrayServletOutputStream();
        resp.setOutputStream(servletOutputStream);

        result = new AJAXRequestResult(fileHolder, "file");
        fileResponseRenderer.write(requestData, result, req, resp);
        assertEquals("Wrong status code", 404, resp.getStatus());

        // test with empty content type and empty filename
        fileHolder.setContentType("");

        resp = new SimHttpServletResponse();
        servletOutputStream = new ByteArrayServletOutputStream();
        resp.setOutputStream(servletOutputStream);

        result = new AJAXRequestResult(fileHolder, "file");
        fileResponseRenderer.write(requestData, result, req, resp);
        assertEquals("Wrong status code", 404, resp.getStatus());
    }

    public void testShouldDetectVulnerableHtmlTags_Bug28637() {
        try {
            final ByteArrayFileHolder fileHolder = FileResponseRendererTools.getFileHolder(
                "28637.htm",
                "text/html",
                Delivery.view,
                Disposition.inline,
                "28637.htm");
            AJAXRequestData requestData = new AJAXRequestData();
            AJAXRequestResult result = new AJAXRequestResult();
            SimHttpServletRequest req = new SimHttpServletRequest();
            SimHttpServletResponse resp = new SimHttpServletResponse();
            ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
            resp.setOutputStream(servletOutputStream);
            final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();
            fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);
            assertNotNull("No output stream found" + servletOutputStream.toString());
            final String outputString = servletOutputStream.toString();
            assertFalse("Output stream contains vulnerable '<script>' tag", outputString.contains("<script>"));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testXSSVuln_Bug26244() throws IOException {
        final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();

        ByteArrayFileHolder fileHolder = FileResponseRendererTools.getFileHolder("xss_utf16.html");

        final AJAXRequestData requestData = new AJAXRequestData();
        final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
        final SimHttpServletRequest req = new SimHttpServletRequest();
        req.setParameter("content_type", "text/html; charset=UTF-16");
        req.setParameter("content_disposition", "inline");
        final SimHttpServletResponse resp = new SimHttpServletResponse();
        final ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
        resp.setOutputStream(servletOutputStream);
        fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);
        final String expectedCT = "text/html";
        assertEquals("Wrong Content-Type", expectedCT, resp.getContentType());
    }

    public void testXSSVuln_Bug29147() throws IOException {
        final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();
        ByteArrayFileHolder fileHolder = FileResponseRendererTools.getFileHolder("29147.svg", "image/svg+xml", Delivery.view, Disposition.inline, "29147.svg");
        final AJAXRequestData requestData = new AJAXRequestData();
        final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
        final SimHttpServletRequest req = new SimHttpServletRequest();
        final SimHttpServletResponse resp = new SimHttpServletResponse();
        final ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
        resp.setOutputStream(servletOutputStream);
        fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);
        final String expectedCT = "application/octet-stream"; // force download
        assertEquals("Wrong Content-Type", expectedCT, resp.getContentType());
    }

    public void testXSSVuln_Bug26373_view() throws IOException {
        final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();

        ByteArrayFileHolder fileHolder = FileResponseRendererTools.getFileHolder("26237.html");

        final AJAXRequestData requestData = new AJAXRequestData();
        final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
        final SimHttpServletRequest req = new SimHttpServletRequest();
        req.setParameter("content_disposition", "view");
        final SimHttpServletResponse resp = new SimHttpServletResponse();
        final ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
        resp.setOutputStream(servletOutputStream);
        fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);

        assertEquals("Wrong Content-Type", "application/octet-stream", resp.getContentType());
    }

    public void testXSSVuln_Bug26237_inline() throws IOException {
        final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();

        ByteArrayFileHolder fileHolder = FileResponseRendererTools.getFileHolder("26237.html");

        final AJAXRequestData requestData = new AJAXRequestData();
        final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
        final SimHttpServletRequest req = new SimHttpServletRequest();
        req.setParameter("content_disposition", "inline");
        final SimHttpServletResponse resp = new SimHttpServletResponse();
        final ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
        resp.setOutputStream(servletOutputStream);
        fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);

        assertEquals("Wrong Content-Type", "application/octet-stream", resp.getContentType());
    }

    public void testXSSVuln_Bug26243() throws IOException {
        final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();

        ByteArrayFileHolder fileHolder = FileResponseRendererTools.getFileHolder("31714.jpg");

        final AJAXRequestData requestData = new AJAXRequestData();
        final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
        final SimHttpServletRequest req = new SimHttpServletRequest();
        req.setParameter("content_disposition", "inline");
        req.setParameter("content_type", "text/html%0d%0a%0d%0a<script>alert(\"XSS\")</script>");
        final SimHttpServletResponse resp = new SimHttpServletResponse();
        final ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
        resp.setOutputStream(servletOutputStream);
        fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);

        assertEquals("Wrong Content-Type", "image/jpeg", resp.getContentType());
    }

    public void testBug31714() throws IOException {
        ByteArrayFileHolder fileHolder = FileResponseRendererTools.getFileHolder("31714.jpg", "image/jpeg", Delivery.view, Disposition.inline, "31714.jpg");

        final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();
        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.putParameter("width", "1000000");
        requestData.putParameter("height", "1000000");
        requestData.setSession(new SimServerSession(1, 1));
        final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");

        final SimHttpServletRequest req = new SimHttpServletRequest();
        final SimHttpServletResponse resp = new SimHttpServletResponse();
        fileResponseRenderer.setScaler(new TestableImageTransformationService(IOUtils.toByteArray(fileHolder.getStream()), ImageTransformations.HIGH_EXPENSE));
        fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);
        assertEquals("Unexpected status code.", 500, resp.getStatus());
    }

    public void testBug26995() throws IOException {
        ByteArrayFileHolder fileHolder = FileResponseRendererTools.getFileHolder("26995", "application/octet-stream", Delivery.view, Disposition.attachment, "26995");

        final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();
        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.setSession(new SimServerSession(1, 1));
        final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");

        final SimHttpServletRequest req = new SimHttpServletRequest();
        final SimHttpServletResponse resp = new SimHttpServletResponse();
        final ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
        resp.setOutputStream(servletOutputStream);
        fileResponseRenderer.setScaler(new TestableImageTransformationService(IOUtils.toByteArray(fileHolder.getStream()), ImageTransformations.HIGH_EXPENSE));
        fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);

        assertEquals("Wrong Content-Type", "image/jpeg", resp.getContentType());
    }

    public void testBug25133() throws IOException {
        ByteArrayFileHolder fileHolder = FileResponseRendererTools.getFileHolder(
            "25133.eml",
            null,
            Delivery.download,
            Disposition.attachment,
            "25133.eml");

        final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();
        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.putParameter("transformationNeeded", "true");
        requestData.setSession(new SimServerSession(1, 1));
        final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");

        final SimHttpServletRequest req = new SimHttpServletRequest();
        final SimHttpServletResponse resp = new SimHttpServletResponse();
        final ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
        resp.setOutputStream(servletOutputStream);
        fileResponseRenderer.setScaler(new WrappingImageTransformationService(new JavaImageTransformationProvider()));
        fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);
        assertNotNull("Header content-type not found", resp.getContentType());
        assertEquals("Wrong Content-Type", "application/octet-stream", resp.getContentType());
    }

    public void testTikaShouldDetectCorrectContenType_Bug26153() throws IOException, OXException {
        ByteArrayFileHolder fileHolder = FileResponseRendererTools.getFileHolder(
            "Rotate_90CW.jpg",
            "application/octet-stream",
            Delivery.view,
            Disposition.inline,
            "Rotate");

        final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();
        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.setSession(new SimServerSession(1, 1));
        final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");

        final SimHttpServletRequest req = new SimHttpServletRequest();
        final SimHttpServletResponse resp = new SimHttpServletResponse();
        final ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
        resp.setOutputStream(servletOutputStream);
        fileResponseRenderer.setScaler(new WrappingImageTransformationService(new JavaImageTransformationProvider()));
        fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);
        requestData.setSession(new SimServerSession(1, 1));
        assertEquals("Wrong Content-Type", "image/jpeg", resp.getContentType());
    }

    public void testUnquoteContentTypeAndDisposition_Bug26153() throws IOException, OXException {
        ByteArrayFileHolder fileHolder = FileResponseRendererTools.getFileHolder(
            "Rotate_90CW.jpg",
            "\"image/jpeg\"",
            Delivery.view,
            Disposition.inline,
            "Rotate_90CW.jpg");
        fileHolder.setDisposition("\"inline\"");
        fileHolder.setDelivery("\"view\"");
        final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();
        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.setSession(new SimServerSession(1, 1));
        final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");

        final SimHttpServletRequest req = new SimHttpServletRequest();
        final SimHttpServletResponse resp = new SimHttpServletResponse();
        final ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
        resp.setOutputStream(servletOutputStream);
        fileResponseRenderer.setScaler(new WrappingImageTransformationService(new JavaImageTransformationProvider()));
        fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);
        requestData.setSession(new SimServerSession(1, 1));
        assertEquals("Wrong Content-Type", "image/jpeg", resp.getContentType());
        assertNotNull("Header content-disposition not present", resp.getHeaders().get("content-disposition"));
        String contentDisposition = resp.getHeaders().get("content-disposition");
        contentDisposition = contentDisposition.substring(0, contentDisposition.indexOf(';'));
        assertEquals("Wrong Content-Disposition", "inline", contentDisposition);
    }

    public void testSanitizingUrlParameter() throws IOException, OXException {
        ByteArrayFileHolder fileHolder = FileResponseRendererTools.getFileHolder(
            "XSSFile.html",
            null,
            Delivery.view,
            Disposition.inline,
            "XSSFile.html");
        final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();
        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.setSession(new SimServerSession(1, 1));
        final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
        final SimHttpServletRequest req = new SimHttpServletRequest();
        req.setParameter("content_type", "text/html&amp;lt;script&amp;gt;x=/xss/;alert&amp;#40;x.source&amp;#41;&amp;lt;/script&amp;gt;");
        final SimHttpServletResponse resp = new SimHttpServletResponse();
        final ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
        resp.setOutputStream(servletOutputStream);
        fileResponseRenderer.setScaler(new WrappingImageTransformationService(new JavaImageTransformationProvider()));
        fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);
        requestData.setSession(new SimServerSession(1, 1));
        assertEquals("Wrong Content-Type", "text/html", resp.getContentType());
    }

    public void testSanitizingFileArguments() throws Exception {
        ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(new byte[] {1,1,1,1});
        fileHolder.setContentType("<pwn/ny");
        fileHolder.setDelivery(Delivery.view.name());
        fileHolder.setDisposition(Disposition.inline.name());
        fileHolder.setName("<svg onload=alert(1)>");
        final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();
        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.setSession(new SimServerSession(1, 1));
        final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
        final SimHttpServletRequest req = new SimHttpServletRequest();
        final SimHttpServletResponse resp = new SimHttpServletResponse();
        resp.setCharacterEncoding("UTF-8");
        final ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
        resp.setOutputStream(servletOutputStream);
        fileResponseRenderer.setScaler(new WrappingImageTransformationService(new JavaImageTransformationProvider()));
        fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);

        String response = new String(servletOutputStream.toByteArray(), "UTF-8");
        assertFalse("Response contains malicious content", response.indexOf("<svg onload=alert") >= 0);
    }

    public void testContentLengthMailAttachments_Bug26926() {
        try {
            InputStream is = null;
            is = new FileInputStream(new File(TEST_DATA_DIR + "26926_27394.pdf"));
            FileHolder fileHolder = new FileHolder(is, -1, "image/jpeg", "28082.jpg");
            fileHolder.setDelivery("download");
            final AJAXRequestData requestData = new AJAXRequestData();
            requestData.putParameter("delivery", "download");
            requestData.setAction("attachment");
            requestData.setSession(new SimServerSession(1, 1));
            final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
            final SimHttpServletRequest req = new SimHttpServletRequest();
            final SimHttpServletResponse resp = new SimHttpServletResponse();
            {
                final ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
                resp.setOutputStream(servletOutputStream);
            }
            requestData.setHttpServletResponse(resp);
            requestData.setHttpServletRequest(req);
            final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();
            fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);
            final int contentLength = resp.getContentLength();
            assertTrue("Content-Length should be -1", contentLength == -1);
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testChunkRead() {
        try {
            byte[] bytes = FileResponseRendererTools.newByteArray(2048);
            bytes[256] = 120;

            ByteArrayFileHolder fileHolder = FileResponseRendererTools.getFileHolder(
                bytes,
                "application/octet-stream",
                Delivery.download,
                Disposition.attachment,
                "bin.data");

            final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();

            {
                final AJAXRequestData requestData = new AJAXRequestData();
                final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
                final SimHttpServletRequest req = new SimHttpServletRequest();
                req.setParameter("off", "0");
                req.setParameter("len", "256");
                final SimHttpServletResponse resp = new SimHttpServletResponse();
                final ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
                resp.setOutputStream(servletOutputStream);
                fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);

                final byte[] writtenBytes = servletOutputStream.toByteArray();
                assertEquals("Unexpected number of written bytes.", 256, writtenBytes.length);
            }

            {
                final AJAXRequestData requestData = new AJAXRequestData();
                final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
                final SimHttpServletRequest req = new SimHttpServletRequest();
                req.setParameter("off", "256");
                req.setParameter("len", "256");
                final SimHttpServletResponse resp = new SimHttpServletResponse();
                final ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
                resp.setOutputStream(servletOutputStream);
                fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);

                final byte[] writtenBytes = servletOutputStream.toByteArray();
                assertEquals("Unexpected number of written bytes.", 256, writtenBytes.length);
                assertEquals("Unexpected starting byte.", 120, writtenBytes[0]);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testChunkReadOutOfRange() {
        try {
            byte[] bytes = FileResponseRendererTools.newByteArray(2048);

            ByteArrayFileHolder fileHolder = FileResponseRendererTools.getFileHolder(
                bytes,
                "application/octet-stream",
                Delivery.download,
                Disposition.attachment,
                "bin.data");

            final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();

            final AJAXRequestData requestData = new AJAXRequestData();
            final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
            final SimHttpServletRequest req = new SimHttpServletRequest();
            req.setParameter("off", "2049");
            req.setParameter("len", "256");
            final SimHttpServletResponse resp = new SimHttpServletResponse();
            final ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
            resp.setOutputStream(servletOutputStream);
            fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);

            assertEquals("Unexpected status code.", 416, resp.getStatus());
            assertEquals("Unexpected 'Content-Range' header.", "bytes */2048", resp.getHeaders().get("content-range"));
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testResourceCacheIsDisabled() throws Exception {
        byte[] bytes = FileResponseRendererTools.newByteArray(2048);

        ByteArrayFileHolder fileHolder = FileResponseRendererTools.getFileHolder(
            bytes,
            "image/jpeg",
            Delivery.view,
            Disposition.inline,
            "someimage.jpg");

        final TestableResourceCache resourceCache = new TestableResourceCache(false);
        ResourceCaches.setResourceCache(resourceCache);
        final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();
        fileResponseRenderer.setScaler(new TestableImageTransformationService(bytes, ImageTransformations.HIGH_EXPENSE));
        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.setSession(new SimServerSession(1, 1));
        requestData.putParameter("width", "80");
        requestData.putParameter("height", "80");
        final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
        result.setHeader("ETag", "1323jjlksldfsdkfms");
        final SimHttpServletRequest req = new SimHttpServletRequest();
        final SimHttpServletResponse resp = new SimHttpServletResponse();
        final ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
        resp.setOutputStream(servletOutputStream);
        fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);
        assertEquals("isEnabled() not called", 1, resourceCache.callsToIsEnabledFor);
        assertEquals("get() called", 0, resourceCache.callsToGet);
        assertEquals("save() called", 0, resourceCache.callsToSave);
    }

    public void testResourceCacheIsDisabled2() throws Exception {
        byte[] bytes = FileResponseRendererTools.newByteArray(2048);

        ByteArrayFileHolder fileHolder = FileResponseRendererTools.getFileHolder(
            bytes,
            "image/jpeg",
            Delivery.view,
            Disposition.inline,
            "someimage.jpg");

        ServerServiceRegistry.getInstance().addService(StringParser.class, new BasicTypesStringParser());
        final TestableResourceCache resourceCache = new TestableResourceCache(true);
        ResourceCaches.setResourceCache(resourceCache);
        final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();
        fileResponseRenderer.setScaler(new TestableImageTransformationService(bytes, ImageTransformations.HIGH_EXPENSE));
        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.setSession(new SimServerSession(1, 1));
        requestData.putParameter("width", "10");
        requestData.putParameter("height", "10");
        requestData.putParameter("cache", "false");
        final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
        final SimHttpServletRequest req = new SimHttpServletRequest();
        final SimHttpServletResponse resp = new SimHttpServletResponse();
        final ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
        resp.setOutputStream(servletOutputStream);
        fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);
        assertEquals("get() not called", 0, resourceCache.callsToGet);
        assertEquals("save() called", 0, resourceCache.callsToSave);
    }

    public void testNoCachingOnCheapTransformations() throws Exception {
        byte[] bytes = FileResponseRendererTools.newByteArray(2048);

        ByteArrayFileHolder fileHolder = FileResponseRendererTools.getFileHolder(
            bytes,
            "image/jpeg",
            Delivery.view,
            Disposition.inline,
            "someimage.jpg");

        final TestableResourceCache resourceCache = new TestableResourceCache(true);
        ResourceCaches.setResourceCache(resourceCache);
        final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();
        fileResponseRenderer.setScaler(new TestableImageTransformationService(bytes, ImageTransformations.LOW_EXPENSE));
        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.setSession(new SimServerSession(1, 1));
        requestData.putParameter("width", "80");
        requestData.putParameter("height", "80");
        final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
        result.setHeader("ETag", "1323jjlksldfsdkfms");
        final SimHttpServletRequest req = new SimHttpServletRequest();
        final SimHttpServletResponse resp = new SimHttpServletResponse();
        final ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
        resp.setOutputStream(servletOutputStream);
        fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);
        assertEquals("get() not called", 1, resourceCache.callsToGet);
        assertEquals("save() called", 0, resourceCache.callsToSave);
    }

    public void testCachingOnExpensiveTransformations() throws Exception {
        byte[] bytes = FileResponseRendererTools.newByteArray(2048);

        ByteArrayFileHolder fileHolder = FileResponseRendererTools.getFileHolder(
            bytes,
            "image/jpeg",
            Delivery.view,
            Disposition.inline,
            "someimage.jpg");

        ServerServiceRegistry.getInstance().addService(StringParser.class, new BasicTypesStringParser());
        final TestableResourceCache resourceCache = new TestableResourceCache(true);
        ResourceCaches.setResourceCache(resourceCache);
        final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();
        fileResponseRenderer.setScaler(new TestableImageTransformationService(bytes, ImageTransformations.HIGH_EXPENSE));
        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.putParameter("width", "10");
        requestData.putParameter("height", "10");
        requestData.setSession(new SimServerSession(1, 1));
        final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
        result.setHeader("ETag", "1323jjlksldfsdkfms");
        final SimHttpServletRequest req = new SimHttpServletRequest();
        final SimHttpServletResponse resp = new SimHttpServletResponse();
        final ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
        resp.setOutputStream(servletOutputStream);
        fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);
        assertEquals("get() not called", 1, resourceCache.callsToGet);
        assertEquals("save() called", 1, resourceCache.callsToSave);
    }

    private static final class TestableResourceCache implements ResourceCache {

        private final boolean isEnabled;

        int callsToIsEnabledFor = 0;

        int callsToGet = 0;

        int callsToSave = 0;

        public TestableResourceCache(final boolean isEnabled) {
            super();
            this.isEnabled = isEnabled;
        }

        @Override
        public boolean isEnabledFor(int contextId, int userId) throws OXException {
            callsToIsEnabledFor++;
            return isEnabled;
        }

        @Override
        public boolean save(String id, CachedResource resource, int userId, int contextId) throws OXException {
            callsToSave++;
            return false;
        }

        @Override
        public CachedResource get(String id, int userId, int contextId) throws OXException {
            callsToGet++;
            return null;
        }

        @Override
        public void remove(int userId, int contextId) throws OXException {

        }

        @Override
        public void removeAlikes(String id, int userId, int contextId) throws OXException {

        }

        @Override
        public void clearFor(int contextId) throws OXException {

        }

        @Override
        public boolean exists(String id, int userId, int contextId) throws OXException {
            return false;
        }
    }

    private static final class TestableImageTransformations implements ImageTransformations {

        private final byte[] imageData;

        private final int expenses;

        public TestableImageTransformations(byte[] imageData, int expenses) {
            super();
            this.imageData = imageData;
            this.expenses = expenses;
        }

        @Override
        public ImageTransformations rotate() {
            return this;
        }

        @Override
        public ImageTransformations scale(int maxWidth, int maxHeight, ScaleType scaleType) {
            return this;
        }

        @Override
        public ImageTransformations scale(int maxWidth, int maxHeight, ScaleType scaleType, boolean shrinkOnly) {
            return this;
        }

        @Override
        public ImageTransformations crop(int x, int y, int width, int height) {
            return this;
        }

        @Override
        public ImageTransformations compress() {
            return this;
        }

        @Override
        public BufferedImage getImage() throws IOException {
            return null;
        }

        @Override
        public byte[] getBytes(String formatName) throws IOException {
            return null;
        }

        @Override
        public InputStream getInputStream(String formatName) throws IOException {
            return null;
        }

        @Override
        public BasicTransformedImage getTransformedImage(String formatName) throws IOException {
            return new BasicTransformedImage() {

                @Override
                public int getTransformationExpenses() {
                    return expenses;
                }

                @Override
                public long getSize() {
                    return 0;
                }

                @Override
                public byte[] getImageData() {
                    return imageData;
                }

                @Override
                public InputStream getImageStream() throws OXException {
                    return new ByteArrayInputStream(imageData);
                }

                @Override
                public IFileHolder getImageFile() {
                    return null;
                }

                @Override
                public String getFormatName() {
                    return null;
                }

                @Override
                public void close() {
                    // Nothing
                }
            };
        }

        @Override
        public TransformedImage getFullTransformedImage(String formatName) throws IOException {
            return new TransformedImage() {

                @Override
                public int getWidth() {
                    return 0;
                }

                @Override
                public int getTransformationExpenses() {
                    return expenses;
                }

                @Override
                public long getSize() {
                    return 0;
                }

                @Override
                public byte[] getMD5() {
                    return null;
                }

                @Override
                public byte[] getImageData() {
                    return imageData;
                }

                @Override
                public InputStream getImageStream() throws OXException {
                    return new ByteArrayInputStream(imageData);
                }

                @Override
                public IFileHolder getImageFile() {
                    return null;
                }

                @Override
                public int getHeight() {
                    return 0;
                }

                @Override
                public String getFormatName() {
                    return null;
                }

                @Override
                public void close() {
                    // Nothing
                }
            };
        }
    }

    private static final class TestableImageTransformationService implements ImageTransformationService {

        private final byte[] imageData;
        private final int expenses;

        TestableImageTransformationService(byte[] imageData, int expenses) {
            super();
            this.imageData = imageData;
            this.expenses = expenses;
        }

        @Override
        public ImageTransformations transfom(BufferedImage sourceImage) {
            return new TestableImageTransformations(imageData, expenses);
        }

        @Override
        public ImageTransformations transfom(BufferedImage sourceImage, Object source) {
            return new TestableImageTransformations(imageData, expenses);
        }

        @Override
        public ImageTransformations transfom(InputStream imageStream) throws IOException {
            return new TestableImageTransformations(imageData, expenses);
        }

        @Override
        public ImageTransformations transfom(InputStream imageStream, Object source) throws IOException {
            return new TestableImageTransformations(imageData, expenses);
        }

        @Override
        public ImageTransformations transfom(IFileHolder imageFile, Object source) throws IOException {
            return new TestableImageTransformations(imageData, expenses);
        }

        @Override
        public ImageTransformations transfom(byte[] imageData) throws IOException {
            return new TestableImageTransformations(imageData, expenses);
        }

        @Override
        public ImageTransformations transfom(byte[] imageData, Object source) throws IOException {
            return new TestableImageTransformations(imageData, expenses);
        }
    }

}
