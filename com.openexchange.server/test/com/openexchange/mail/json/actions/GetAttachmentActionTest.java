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
import org.mockito.ArgumentMatchers;
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
@PrepareForTest({ GetAttachmentAction.class, AbstractMailAction.class, MailRequest.class, MailProperties.class, MessageUtility.class })
public class GetAttachmentActionTest {

    public GetAttachmentActionTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(MailProperties.class);
        PowerMockito.mockStatic(MessageUtility.class);

        PowerMockito.when(MessageUtility.readMailPart(ArgumentMatchers.any(MailPart.class), ArgumentMatchers.anyString())).thenReturn("MailPartContent_but_I_am_not_in_the_mood_to_get_a_correct_content_example");

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
        assertEquals("Wrong caching value", Boolean.FALSE, ajaxRequestData.getParameter("cache", boolean.class));
        assertTrue("Wrong class", IFileHolder.class.isInstance(object));
        assertEquals("Wrong content type", "image/jpeg", (IFileHolder.class.cast(object)).getContentType());
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
        assertEquals("Wrong caching value", Boolean.FALSE, ajaxRequestData.getParameter("cache", boolean.class));
        assertTrue("Wrong class", IFileHolder.class.isInstance(object));
        assertEquals("Wrong content type", "text/html; charset=ISO-8859-1", (IFileHolder.class.cast(object)).getContentType());
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
        assertEquals("Wrong caching value", Boolean.FALSE, ajaxRequestData.getParameter("cache", boolean.class));
        assertTrue("Wrong class", IFileHolder.class.isInstance(object));
        assertEquals("Wrong content type", "application/pdf", (IFileHolder.class.cast(object)).getContentType());
    }

}
