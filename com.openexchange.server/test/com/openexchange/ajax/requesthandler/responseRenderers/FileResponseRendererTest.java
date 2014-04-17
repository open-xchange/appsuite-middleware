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

import java.awt.image.BufferedImage;
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
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.cache.CachedResource;
import com.openexchange.ajax.requesthandler.cache.ResourceCache;
import com.openexchange.ajax.requesthandler.cache.ResourceCaches;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.SimConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlService;
import com.openexchange.html.SimHtmlService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.images.ImageTransformationService;
import com.openexchange.tools.images.ImageTransformations;
import com.openexchange.tools.images.ScaleType;
import com.openexchange.tools.images.TransformedImage;
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

    public void testContentLengthMailAttachments_Bug26926() {
        try {
            final File file = new File(TEST_DATA_DIR, "OX6-User-Guide-German-v6.22.2.pdf");
            final InputStream is = new FileInputStream(file);
            final byte[] bytes = IOUtils.toByteArray(is);
            final ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(bytes);
            {
                fileHolder.setContentType("application/pdf");
                fileHolder.setDelivery("download");
                fileHolder.setDisposition("attachment");
                fileHolder.setName(file.getName());
            }
            final AJAXRequestData requestData = new AJAXRequestData();
            requestData.setSession(new SimServerSession(1, 1));
            final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
            final SimHttpServletRequest req = new SimHttpServletRequest();
            final SimHttpServletResponse resp = new SimHttpServletResponse();
            {
                final ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
                resp.setOutputStream(servletOutputStream);
            }
            final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();
            fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);
            System.out.println("break");
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testZeroByteTransformation_Bug28429() {
        try {
            final File file = new File(TEST_DATA_DIR, "28429.jpg");
            final InputStream is = new FileInputStream(file);
            final byte[] bytes = IOUtils.toByteArray(is);
            final ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(bytes);
            {
                fileHolder.setContentType("image/jpeg");
                fileHolder.setDelivery("view");
                fileHolder.setDisposition("inline");
                fileHolder.setName(file.getName());
            }
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
            fileResponseRenderer.setScaler(new TestableImageTransformationService(bytes, ImageTransformations.HIGH_EXPENSE));
            fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);
            assertFalse("Got an error status: " + resp.getStatus(), resp.getStatus() >= 400);
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testContentTypeByFileName_Bug31648() {
        try {
            final File file = new File(TEST_DATA_DIR, "VM1161.PNG");
            final InputStream is = new FileInputStream(file);
            final byte[] bytes = IOUtils.toByteArray(is);
            final ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(bytes);
            {
                fileHolder.setContentType("application/binary");
                fileHolder.setDelivery("view");
                fileHolder.setDisposition("inline");
                fileHolder.setName(file.getName());
            }
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
            final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();
            fileResponseRenderer.setScaler(new TestableImageTransformationService(bytes, ImageTransformations.HIGH_EXPENSE));
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
            final File file = new File(TEST_DATA_DIR, "OX6-User-Guide-German-v6.22.2.pdf");
            final InputStream is = new FileInputStream(file);
            final byte[] bytes = IOUtils.toByteArray(is);
            final ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(bytes);
            {
                fileHolder.setContentType("application/pdf");
                fileHolder.setDelivery("view");
                fileHolder.setDisposition("inline");
                fileHolder.setName(file.getName());
            }
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
            fileResponseRenderer.setScaler(new TestableImageTransformationService(bytes, ImageTransformations.HIGH_EXPENSE));
            fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);
            System.out.println("break");
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
        final byte[] bytes = new byte[0];
        final ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(bytes);
        fileHolder.setContentType("application/octet-stream");
        fileHolder.setDelivery("download");
        fileHolder.setDisposition("attachment");
        fileHolder.setName("");

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

    public void testXSSVuln_Bug26244() throws IOException {
        final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();

        final File file = new File(TEST_DATA_DIR, "xss_utf16.html");
        final InputStream is = new FileInputStream(file);
        final byte[] bytes = IOUtils.toByteArray(is);
        final ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(bytes);
        fileHolder.setName(file.getName());

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

        final File file = new File(TEST_DATA_DIR, "29147.svg");
        final InputStream is = new FileInputStream(file);
        final byte[] bytes = IOUtils.toByteArray(is);
        final ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(bytes);
        {
            fileHolder.setContentType("image/svg+xml");
            fileHolder.setDelivery("view");
            fileHolder.setDisposition("inline");
            fileHolder.setName(file.getName());
        }

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

    public void testChunkRead() {
        try {
            final int length = 2048;
            final byte[] bytes = new byte[length];
            for (int i = 0; i < length; i++) {
                bytes[i] = (byte) i;
            }
            bytes[256] = 120;

            final ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(bytes);
            fileHolder.setContentType("application/octet-stream");
            fileHolder.setDelivery("download");
            fileHolder.setDisposition("attachment");
            fileHolder.setName("bin.dat");

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
            final int length = 2048;
            final byte[] bytes = new byte[length];
            for (int i = 0; i < length; i++) {
                bytes[i] = (byte) i;
            }

            final ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(bytes);
            fileHolder.setContentType("application/octet-stream");
            fileHolder.setDelivery("download");
            fileHolder.setDisposition("attachment");
            fileHolder.setName("bin.dat");

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
        final int length = 2048;
        final byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) i;
        }

        final ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(bytes);
        fileHolder.setContentType("image/jpeg");
        fileHolder.setDelivery("view");
        fileHolder.setDisposition("inline");
        fileHolder.setName("someimage.jpg");

        final TestableResourceCache resourceCache = new TestableResourceCache(false);
        ResourceCaches.setResourceCache(resourceCache);
        final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();
        fileResponseRenderer.setScaler(new TestableImageTransformationService(bytes, ImageTransformations.HIGH_EXPENSE));
        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.setSession(new SimServerSession(1, 1));
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
        final int length = 2048;
        final byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) i;
        }

        final ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(bytes);
        fileHolder.setContentType("image/jpeg");
        fileHolder.setDelivery("view");
        fileHolder.setDisposition("inline");
        fileHolder.setName("someimage.jpg");
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
        final int length = 2048;
        final byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) i;
        }

        final ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(bytes);
        fileHolder.setContentType("image/jpeg");
        fileHolder.setDelivery("view");
        fileHolder.setDisposition("inline");
        fileHolder.setName("someimage.jpg");

        final TestableResourceCache resourceCache = new TestableResourceCache(true);
        ResourceCaches.setResourceCache(resourceCache);
        final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();
        fileResponseRenderer.setScaler(new TestableImageTransformationService(bytes, ImageTransformations.LOW_EXPENSE));
        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.setSession(new SimServerSession(1, 1));
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
        final int length = 2048;
        final byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) i;
        }

        final ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(bytes);
        fileHolder.setContentType("image/jpeg");
        fileHolder.setDelivery("view");
        fileHolder.setDisposition("inline");
        fileHolder.setName("someimage.jpg");
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
        public TransformedImage getTransformedImage(String formatName) throws IOException {
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
                public int getHeight() {
                    return 0;
                }

                @Override
                public String getFormatName() {
                    return null;
                }
            };
        }
    }

    private static final class TestableImageTransformationService implements ImageTransformationService {

        private final byte[] imageData;

        private final int expenses;

        public TestableImageTransformationService(byte[] imageData, int expenses) {
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
        public ImageTransformations transfom(byte[] imageData) throws IOException {
            return new TestableImageTransformations(imageData, expenses);
        }

        @Override
        public ImageTransformations transfom(byte[] imageData, Object source) throws IOException {
            return new TestableImageTransformations(imageData, expenses);
        }
    }

}
