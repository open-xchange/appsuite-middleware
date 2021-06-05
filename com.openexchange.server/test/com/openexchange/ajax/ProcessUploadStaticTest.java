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

package com.openexchange.ajax;

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.groupware.upload.impl.UploadEvent;


/**
 * {@link ProcessUploadStaticTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SuppressWarnings("deprecation")
@RunWith(PowerMockRunner.class)
public class ProcessUploadStaticTest {
    /**
     * Initializes a new {@link ProcessUploadStaticTest}.
     */
    public ProcessUploadStaticTest() {
        super();
    }

    @Test
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

                                @Override
                                public boolean isFinished() {
                                    return bin.available() <= 0;
                                }

                                @Override
                                public boolean isReady() {
                                    return true;
                                }

                                @Override
                                public void setReadListener(ReadListener readListener) {
                                    // Ignore
                                }
                            };
                            PowerMockito.when(mockRequest.getInputStream()).thenReturn(in);
                            PowerMockito.when(mockRequest.getContentType()).thenReturn("multipart/form-data; boundary=---------------------------1902770288168124293960248547");
                            PowerMockito.when(I(mockRequest.getContentLength())).thenReturn(I(-1));
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
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
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

                @Override
                public boolean isFinished() {
                    return bin.available() <= 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    // Ignore
                }
            };
            PowerMockito.when(mockRequest.getInputStream()).thenReturn(in);
            PowerMockito.when(mockRequest.getContentType()).thenReturn("multipart/form-data; boundary=---------------------------1902770288168124293960248547");
            PowerMockito.when(I(mockRequest.getContentLength())).thenReturn(I(-1));
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

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
