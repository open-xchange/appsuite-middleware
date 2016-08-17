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

package com.openexchange.ajax;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import junit.framework.TestCase;
import org.json.JSONObject;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.groupware.upload.impl.UploadEvent;


/**
 * {@link ProcessUploadStaticTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RunWith(PowerMockRunner.class)
public class ProcessUploadStaticTest extends TestCase {

    /**
     * Initializes a new {@link ProcessUploadStaticTest}.
     */
    public ProcessUploadStaticTest() {
        super();
    }

    public void testProcessUploadStatic() {
        try {
            final CountDownLatch startUpLatch = new CountDownLatch(1);
            final int threadCount = 100;
            final CountDownLatch finishedLatch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                final int num = i + 1;
                Runnable r = new Runnable() {

                    @Override
                    public void run() {
                        try {
                            // Mocking
                            final HttpServletRequest mockRequest = PowerMockito.mock(HttpServletRequest.class);

                            final ByteArrayInputStream bin = new ByteArrayInputStream((
                                "-----------------------------1902770288168124293960248547\r\nContent-Disposition: form-data; name=\"json_0\"\r\n\r\n{\"from\":\"T" +
                                "horben Betten <thorben@devel-mail.netline.de>\",\"to\":\"Thorben Betten <thorben@devel-mail.netline.de>\",\"cc\":\"\",\"bcc\":\"\",\"subje" +
                                "ct\":\"Subject\",\"priority\":\"3\",\"vcard\":1,\"attachments\":[{\"content_type\":\"ALTERNATIVE\",\"content\":\"<html><body" +
                                ">&gt;Content"+num+"&lt;</body></html>\"}],\"datasources\":[]}\r\n-----------------------------1902770288168124293960248547--").getBytes());
                            final ServletInputStream in = new ServletInputStream() {

                                @Override
                                public int read() throws IOException {
                                    return bin.read();
                                }
                            };
                            PowerMockito.when(mockRequest.getInputStream()).thenReturn(in);
                            PowerMockito.when(mockRequest.getContentType()).thenReturn("multipart/form-data; boundary=---------------------------1902770288168124293960248547");
                            PowerMockito.when(mockRequest.getContentLength()).thenReturn(-1);
                            PowerMockito.when(mockRequest.getCharacterEncoding()).thenReturn("UTF-8");
                            PowerMockito.when(mockRequest.getParameter("action")).thenReturn("new");

                            // Parse multipart
                            startUpLatch.await();
                            UploadEvent uploadEvent = AJAXServlet.processUploadStatic(mockRequest);
                            assertEquals("Unexpected number of form fields", 1, uploadEvent.getNumberOfFormFields());
                            final JSONObject jo = new JSONObject(uploadEvent.getFormField("json_0"));
                            assertEquals(
                                "Unexpected form field content",
                                "<html><body>&gt;Content"+num+"&lt;</body></html>",
                                jo.getJSONArray("attachments").getJSONObject(0).getString("content"));
                        } catch (Exception e) {
                            e.printStackTrace();
                            fail(e.getMessage());
                        } finally {
                            finishedLatch.countDown();
                        }
                    }
                };
                new Thread(r).start();
            }

            // Start threads
            startUpLatch.countDown();

            // Await completion
            finishedLatch.await();
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testProcessUploadStatic2() {
        try {
            // Mocking
            final HttpServletRequest mockRequest = PowerMockito.mock(HttpServletRequest.class);

            final ByteArrayInputStream bin = new ByteArrayInputStream((
                "-----------------------------1902770288168124293960248547\r\nContent-Disposition: form-data; name=\"json_0\"\r\n\r\n{\"from\":\"T" +
                "horben Betten <thorben@devel-mail.netline.de>\",\"to\":\"Thorben Betten <thorben@devel-mail.netline.de>\",\"cc\":\"\",\"bcc\":\"\",\"subje" +
                "ct\":\"Subject\",\"priority\":\"3\",\"vcard\":1,\"attachments\":[{\"content_type\":\"ALTERNATIVE\",\"content\":\"<html><body" +
                ">&gt;Content1&lt;</body></html>\"}],\"datasources\":[]}\r\n"
                 +
                 "-----------------------------1902770288168124293960248547\r\nContent-Disposition: form-data; name=\"json_0\"\r\n\r\n{\"from\":\"T" +
                 "horben Betten <thorben@devel-mail.netline.de>\",\"to\":\"Thorben Betten <thorben@devel-mail.netline.de>\",\"cc\":\"\",\"bcc\":\"\",\"subje" +
                 "ct\":\"Subject\",\"priority\":\"3\",\"vcard\":1,\"attachments\":[{\"content_type\":\"ALTERNATIVE\",\"content\":\"<html><body" +
                 ">&gt;Content2&lt;</body></html>\"}],\"datasources\":[]}\r\n-----------------------------1902770288168124293960248547--"
                ).getBytes());
            final ServletInputStream in = new ServletInputStream() {

                @Override
                public int read() throws IOException {
                    return bin.read();
                }
            };
            PowerMockito.when(mockRequest.getInputStream()).thenReturn(in);
            PowerMockito.when(mockRequest.getContentType()).thenReturn("multipart/form-data; boundary=---------------------------1902770288168124293960248547");
            PowerMockito.when(mockRequest.getContentLength()).thenReturn(-1);
            PowerMockito.when(mockRequest.getCharacterEncoding()).thenReturn("UTF-8");
            PowerMockito.when(mockRequest.getParameter("action")).thenReturn("new");

            // Parse multipart
            UploadEvent uploadEvent = AJAXServlet.processUploadStatic(mockRequest);
            assertEquals("Unexpected number of form fields", 1, uploadEvent.getNumberOfFormFields());
            final JSONObject jo = new JSONObject(uploadEvent.getFormField("json_0"));
            assertEquals(
                "Unexpected form field content",
                "<html><body>&gt;Content2&lt;</body></html>",
                jo.getJSONArray("attachments").getJSONObject(0).getString("content"));

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
