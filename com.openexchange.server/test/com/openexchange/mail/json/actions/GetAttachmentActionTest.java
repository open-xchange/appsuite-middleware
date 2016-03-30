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

package com.openexchange.mail.json.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.strings.BasicTypesStringParser;
import com.openexchange.tools.strings.StringParser;


/**
 * {@link GetAttachmentActionTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({GetAttachmentAction.class, AbstractMailAction.class, MailRequest.class, MailProperties.class, MessageUtility.class})
public class GetAttachmentActionTest {

    public GetAttachmentActionTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(MailProperties.class);
        PowerMockito.mockStatic(MessageUtility.class);

        PowerMockito.when(MessageUtility.readMailPart((MailPart)Matchers.any(), Matchers.anyString())).thenReturn("MailPartContent_but_I_am_not_in_the_mood_to_get_a_correct_content_example");

        MailProperties mailProperties = PowerMockito.mock(MailProperties.class);
        PowerMockito.when(mailProperties.getDefaultMimeCharset()).thenReturn("UTF-8");
        PowerMockito.when(MailProperties.getInstance()).thenReturn(mailProperties);
    }

    /**
     * GetAttachmentAction.perform(MailRequest) used to set an improper content type for the returned {@link IFileHolder}.
     * Formerly the content type of the attachments mail part was set. As content types for mime parts may contain optional
     * parameters, something like 'image/jpeg; name=I_am_a_filename_for_an_image.jpg' was returned. That's wrong, we expect
     * only 'image/jpeg' here.
     */
    @Test
    public void testReturnedContentTypeAndModifiedParameters() throws Exception {
        String folder = "default0/INBOX";
        String uid = "1";
        String attachmentId = "2";

        ServiceLookup serviceLookup = mock(ServiceLookup.class);
        ServerServiceRegistry.getInstance().addService(StringParser.class, new BasicTypesStringParser());
        AJAXRequestData ajaxRequestData = new AJAXRequestData();
        MailRequest mailRequest = mock(MailRequest.class);
        doReturn(folder).when(mailRequest).checkParameter(AJAXServlet.PARAMETER_FOLDERID);
        doReturn(uid).when(mailRequest).checkParameter(AJAXServlet.PARAMETER_ID);
        doReturn(attachmentId).when(mailRequest).getParameter(Mail.PARAMETER_MAILATTCHMENT);
        doReturn(null).when(mailRequest).getParameter(Mail.PARAMETER_MAILCID);
        doReturn("0").when(mailRequest).getParameter(Mail.PARAMETER_SAVE);
        doReturn("1").when(mailRequest).getParameter(Mail.PARAMETER_FILTER);
        doReturn(ajaxRequestData).when(mailRequest).getRequest();

        // Jenkins might not load mime.types
        MimeType2ExtMap.addMimeType("image/jpg", "jpg");

        String filename = "I_am_a_filename_for_an_image.jpg";
        MailPart mailPart = mock(MailPart.class);
        when(mailPart.getFileName()).thenReturn(filename);
        ContentType ct = new ContentType("image/jpeg");
        ct.addParameter("name", filename);
        when(mailPart.getContentType()).thenReturn(ct);
        MailServletInterface mailServletInterface = mock(MailServletInterface.class);
        doReturn(mailPart).when(mailServletInterface).getMessageAttachment(folder, uid, attachmentId, true);
        GetAttachmentAction action = spy(new GetAttachmentAction(serviceLookup));
        doReturn(mailServletInterface).when(action).getMailInterface(mailRequest);

        AJAXRequestResult result = action.perform(mailRequest);
        Object object = result.getResultObject();
        assertEquals("Wrong format", "file", ajaxRequestData.getFormat());
        assertEquals("Wrong caching value", false, ajaxRequestData.getParameter("cache", boolean.class));
        assertTrue("Wrong class", IFileHolder.class.isInstance(object));
        assertEquals("Wrong content type", "image/jpeg", ((IFileHolder) object).getContentType());
    }

    @Test
    public void testProperlyDetectedInvalidHtmlContent() throws Exception {
        String folder = "default0/INBOX";
        String uid = "1";
        String attachmentId = "2";

        ServiceLookup serviceLookup = mock(ServiceLookup.class);
        ServerServiceRegistry.getInstance().addService(StringParser.class, new BasicTypesStringParser());
        AJAXRequestData ajaxRequestData = new AJAXRequestData();
        MailRequest mailRequest = mock(MailRequest.class);
        doReturn(folder).when(mailRequest).checkParameter(AJAXServlet.PARAMETER_FOLDERID);
        doReturn(uid).when(mailRequest).checkParameter(AJAXServlet.PARAMETER_ID);
        doReturn(attachmentId).when(mailRequest).getParameter(Mail.PARAMETER_MAILATTCHMENT);
        doReturn(null).when(mailRequest).getParameter(Mail.PARAMETER_MAILCID);
        doReturn("0").when(mailRequest).getParameter(Mail.PARAMETER_SAVE);
        doReturn("1").when(mailRequest).getParameter(Mail.PARAMETER_FILTER);
        doReturn(ajaxRequestData).when(mailRequest).getRequest();

        // Jenkins might not load mime.types
        MimeType2ExtMap.addMimeType("application/pdf", "pdf");

        String filename = "I_am_a_filename_for_a_pdf.pdf";
        MailPart mailPart = mock(MailPart.class);
        when(mailPart.getFileName()).thenReturn(filename);
        ContentType ct = new ContentType("text/html; charset=ISO-8859-1; name=" + filename);
        when(mailPart.getContentType()).thenReturn(ct);
        MailServletInterface mailServletInterface = mock(MailServletInterface.class);
        doReturn(mailPart).when(mailServletInterface).getMessageAttachment(folder, uid, attachmentId, true);
        GetAttachmentAction action = spy(new GetAttachmentAction(serviceLookup));
        doReturn(mailServletInterface).when(action).getMailInterface(mailRequest);

        AJAXRequestResult result = action.perform(mailRequest);
        Object object = result.getResultObject();
        assertEquals("Wrong format", "file", ajaxRequestData.getFormat());
        assertEquals("Wrong caching value", false, ajaxRequestData.getParameter("cache", boolean.class));
        assertTrue("Wrong class", IFileHolder.class.isInstance(object));
        assertEquals("Wrong content type", "text/html", ((IFileHolder) object).getContentType());
    }

    @Test
    public void testProperlyDetectedNonHtmlBinary() throws Exception {
        String folder = "default0/INBOX";
        String uid = "1";
        String attachmentId = "2";

        ServiceLookup serviceLookup = mock(ServiceLookup.class);
        ServerServiceRegistry.getInstance().addService(StringParser.class, new BasicTypesStringParser());
        AJAXRequestData ajaxRequestData = new AJAXRequestData();
        MailRequest mailRequest = mock(MailRequest.class);
        doReturn(folder).when(mailRequest).checkParameter(AJAXServlet.PARAMETER_FOLDERID);
        doReturn(uid).when(mailRequest).checkParameter(AJAXServlet.PARAMETER_ID);
        doReturn(attachmentId).when(mailRequest).getParameter(Mail.PARAMETER_MAILATTCHMENT);
        doReturn(null).when(mailRequest).getParameter(Mail.PARAMETER_MAILCID);
        doReturn("0").when(mailRequest).getParameter(Mail.PARAMETER_SAVE);
        doReturn("1").when(mailRequest).getParameter(Mail.PARAMETER_FILTER);
        doReturn(ajaxRequestData).when(mailRequest).getRequest();

        // Jenkins might not load mime.types
        MimeType2ExtMap.addMimeType("application/pdf", "pdf");

        String filename = null;
        MailPart mailPart = mock(MailPart.class);
        when(mailPart.getFileName()).thenReturn(filename);

        ContentType ct = new ContentType("application/pdf");
        when(mailPart.getContentType()).thenReturn(ct);
        MailServletInterface mailServletInterface = mock(MailServletInterface.class);
        doReturn(mailPart).when(mailServletInterface).getMessageAttachment(folder, uid, attachmentId, true);
        GetAttachmentAction action = spy(new GetAttachmentAction(serviceLookup));
        doReturn(mailServletInterface).when(action).getMailInterface(mailRequest);

        AJAXRequestResult result = action.perform(mailRequest);
        Object object = result.getResultObject();
        assertEquals("Wrong format", "file", ajaxRequestData.getFormat());
        assertEquals("Wrong caching value", false, ajaxRequestData.getParameter("cache", boolean.class));
        assertTrue("Wrong class", IFileHolder.class.isInstance(object));
        assertEquals("Wrong content type", "application/pdf", ((IFileHolder) object).getContentType());
    }

}
