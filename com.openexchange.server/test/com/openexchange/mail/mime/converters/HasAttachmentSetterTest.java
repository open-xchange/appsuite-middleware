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

package com.openexchange.mail.mime.converters;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.ParameterList;
import javax.mail.internet.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;

/**
 * {@link HasAttachmentSetterTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
@RunWith(PowerMockRunner.class)
public class HasAttachmentSetterTest {

    private static final String ATTACHMENT_WRONG_FOUND = "Attachment found where none should be";
    private static final String TESTFILE_RTF = "testfile; filename=randomfile.rtf";
    private static final String PLAIN_TEXT = "plain/text";
    private static final String ATTACHMENT_NOT_IDENTIFIED = "Attachment was not identified";

    @Mock
    private Part part;
    @Mock
    private MailPart mailpart;
    @Mock
    private BODYSTRUCTURE bodystructure;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testHasAttachment_MultipartDisposition() {
        boolean result = false;
        try {
            Mockito.when(part.getContentType()).thenReturn("inline");
            Mockito.when(part.getContentType()).thenReturn(PLAIN_TEXT);
            Mockito.when(part.getDisposition()).thenReturn("attachment; random=value");
            result = HasAttachmentSetter.hasAttachments(part);
        } catch (MessagingException | OXException e) {
            e.printStackTrace();
        }
        assertTrue(ATTACHMENT_NOT_IDENTIFIED, result);
    }

    @Test
    public void testHasAttachment_MultipartFilename() {
        boolean result = false;
        try {
            Mockito.when(part.getContentType()).thenReturn("inline");
            Mockito.when(part.getContentType()).thenReturn(PLAIN_TEXT);
            Mockito.when(part.getDisposition()).thenReturn(TESTFILE_RTF);
            result = HasAttachmentSetter.hasAttachments(part);
        } catch (MessagingException | OXException e) {
            e.printStackTrace();
        }
        assertTrue(ATTACHMENT_NOT_IDENTIFIED, result);
    }

    @Test
    public void testHasAttachment_MultipartSignature() {
        boolean result = true;
        try {
            Mockito.when(part.getContentType()).thenReturn("inline");
            Mockito.when(part.getContentType()).thenReturn("application/pkcs7-signature");
            result = HasAttachmentSetter.hasAttachments(part);
        } catch (MessagingException | OXException e) {
            e.printStackTrace();
        }
        assertFalse(ATTACHMENT_WRONG_FOUND, result);
    }

    @Test
    public void testHasAttachment_MailpartDisposition() {
        boolean result = false;
        try {
            Mockito.when(mailpart.getEnclosedCount()).thenReturn(1);
            ContentDisposition cd = new ContentDisposition("attachment; random=value");
            Mockito.when(mailpart.getContentDisposition()).thenReturn(cd);
            ContentType ct = new ContentType(PLAIN_TEXT);
            Mockito.when(mailpart.getContentType()).thenReturn(ct);
            Mockito.when(mailpart.getEnclosedMailPart(0)).thenReturn(mailpart);
            result = HasAttachmentSetter.hasAttachments(mailpart, "inline");
        } catch (MessagingException | OXException e) {
            e.printStackTrace();
        }
        assertTrue(ATTACHMENT_NOT_IDENTIFIED, result);
    }

    @Test
    public void testHasAttachment_MailpartFilename() {
        boolean result = false;
        try {
            Mockito.when(mailpart.getEnclosedCount()).thenReturn(1);
            ContentDisposition cd = new ContentDisposition(TESTFILE_RTF);
            Mockito.when(mailpart.getContentDisposition()).thenReturn(cd);
            ContentType ct = new ContentType(PLAIN_TEXT);
            Mockito.when(mailpart.getContentType()).thenReturn(ct);
            Mockito.when(mailpart.getEnclosedMailPart(0)).thenReturn(mailpart);
            result = HasAttachmentSetter.hasAttachments(mailpart, "inline");
        } catch (MessagingException | OXException e) {
            e.printStackTrace();
        }
        assertTrue(ATTACHMENT_NOT_IDENTIFIED, result);
    }

    @Test
    public void testHasAttachment_MailpartSignature() {
        boolean result = true;
        try {
            ContentType ct = new ContentType("Application");
            Mockito.when(mailpart.getContentType()).thenReturn(ct);
            result = HasAttachmentSetter.hasAttachments(mailpart, "Pkcs7-Signature");
        } catch (MessagingException | OXException e) {
            e.printStackTrace();
        }
        assertFalse(ATTACHMENT_WRONG_FOUND, result);
    }

    @Test
    public void testHasAttachment_BodystructureDisposition() {
        boolean result = false;
        setContentTypePlainText(bodystructure);
        bodystructure.disposition = "attachment";
        result = HasAttachmentSetter.hasAttachments(bodystructure);
        assertTrue(ATTACHMENT_NOT_IDENTIFIED, result);
    }

    @Test
    public void testHasAttachment_BodystructureFilename() throws ParseException {
        boolean result = false;
        setContentTypePlainText(bodystructure);
        bodystructure.dParams = new ParameterList(";filename=randomfile.rtf");
        result = HasAttachmentSetter.hasAttachments(bodystructure);
        assertTrue(ATTACHMENT_NOT_IDENTIFIED, result);
    }

    @Test
    public void testHasAttachment_BodystructureSignature() {
        boolean result = true;
        bodystructure.type = "APPLICATION";
        bodystructure.subtype = "Pkcs7-Signature";
        result = HasAttachmentSetter.hasAttachments(bodystructure);
        assertFalse(ATTACHMENT_WRONG_FOUND, result);
    }

    public void testHasAttachment_BodystructureMessageRFC() {
        boolean result = true;
        bodystructure.type = "message";
        bodystructure.subtype = "rfc822";
        result = HasAttachmentSetter.hasAttachments(bodystructure);
        assertTrue(ATTACHMENT_WRONG_FOUND, result);
    }

    private static void setContentTypePlainText(BODYSTRUCTURE bs) {
        bs.type = "plain";
        bs.subtype = "text";
    }

}
