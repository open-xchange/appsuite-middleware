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

package com.openexchange.ajax;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import junit.framework.TestCase;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.mail.attachment.AttachmentToken;
import com.openexchange.mail.attachment.AttachmentTokenRegistry;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;


/**
 * {@link MailAttachmentTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( value = { AttachmentTokenRegistry.class, AttachmentToken.class })
public class MailAttachmentTest extends TestCase {

    /**
     * Initializes a new {@link MailAttachmentTest}.
     */
    public MailAttachmentTest() {
        super();
    }

    /**
     * Initializes a new {@link MailAttachmentTest}.
     */
    public MailAttachmentTest(String name) {
        super(name);
    }

    public void testDoGet() {
        try {
            PowerMockito.mockStatic(AttachmentTokenRegistry.class);

            final HttpServletRequest mockRequest = PowerMockito.mock(HttpServletRequest.class);
            final HttpServletResponse mockResponse = PowerMockito.mock(HttpServletResponse.class);

            final ByteArrayOutputStream bout = new ByteArrayOutputStream(256);
            final ServletOutputStream out = new ServletOutputStream() {

                @Override
                public void write(int b) throws IOException {
                    bout.write(b);
                }
            };
            PowerMockito.when(mockResponse.getOutputStream()).thenReturn(out);

            final AtomicReference<String> cts = new AtomicReference<String>();
            PowerMockito.doAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    if ("content-type".equalsIgnoreCase((String) invocation.getArguments()[0])) {
                        cts.set((String) invocation.getArguments()[1]);
                    }
                    return null;
                }
            }).when(mockResponse).setHeader(Mockito.anyString(), Mockito.anyString());
            PowerMockito.doAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    cts.set((String) invocation.getArguments()[0]);
                    return null;
                }
            }).when(mockResponse).setContentType(Mockito.anyString());

            final AtomicReference<String> cds = new AtomicReference<String>();
            PowerMockito.doAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    if ("Content-Disposition".equalsIgnoreCase((String) invocation.getArguments()[0])) {
                        cds.set((String) invocation.getArguments()[1]);
                    }
                    return null;
                }
            }).when(mockResponse).setHeader(Mockito.anyString(), Mockito.anyString());

            PowerMockito.when(mockRequest.getParameter("id")).thenReturn("123");
            PowerMockito.when(mockRequest.getParameter("save")).thenReturn("1");
            PowerMockito.when(mockRequest.getParameter("filter")).thenReturn(null);

            final AttachmentTokenRegistry mockAttachmentTokenRegistry = PowerMockito.mock(AttachmentTokenRegistry.class);
            PowerMockito.when(AttachmentTokenRegistry.getInstance()).thenReturn(mockAttachmentTokenRegistry);

            final AttachmentToken mockAttachmentToken = PowerMockito.mock(AttachmentToken.class);

            PowerMockito.when(mockAttachmentTokenRegistry.getToken("123")).thenReturn(mockAttachmentToken);
            PowerMockito.when(Boolean.valueOf(mockAttachmentToken.isCheckIp())).thenReturn(Boolean.FALSE);
            PowerMockito.when(Boolean.valueOf(mockAttachmentToken.isOneTime())).thenReturn(Boolean.FALSE);


            final MailPart mockMailPart = PowerMockito.mock(MailPart.class);

            PowerMockito.when(mockAttachmentToken.getAttachment()).thenReturn(mockMailPart);
            PowerMockito.when(mockMailPart.getContentType()).thenReturn(new ContentType("application/zip; name=archive.zip"));
            PowerMockito.when(mockMailPart.getContentDisposition()).thenReturn(new ContentDisposition("attachment; filename=archive.zip"));
            PowerMockito.when(mockMailPart.getFileName()).thenReturn("archive.zip");
            PowerMockito.when(mockMailPart.containsContentType()).thenReturn(true);

            final byte[] bytes = new byte[127];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) (i);
            }

            PowerMockito.when(mockMailPart.getInputStream()).thenReturn(new ByteArrayInputStream(bytes));

            new MailAttachment().doGet(mockRequest, mockResponse);

            byte[] byteArray = bout.toByteArray();

            int length = byteArray.length;
            assertEquals("Unexpected number of bytes written to ServletOutputStream.", 127, length);

            for (int i = 0; i < length; i++) {
                assertEquals("Unexpected byte written to ServletOutputStream.", (byte) i, byteArray[i]);
            }

            assertEquals("Unexpected Content-Type.", "application/zip", cts.get());
            assertTrue("Unexpected Content-Disposition.", null != cds.get() && cds.get().startsWith("attachment"));

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
