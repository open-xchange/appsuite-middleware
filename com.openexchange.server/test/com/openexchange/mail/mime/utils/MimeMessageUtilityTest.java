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

package com.openexchange.mail.mime.utils;

import java.io.IOException;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.ParameterList;
import javax.mail.internet.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;
import junit.framework.TestCase;

/**
 * {@link MimeMessageUtilityTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since 7.6.1
 */
@RunWith(PowerMockRunner.class)
public class MimeMessageUtilityTest extends TestCase {
    
    private static final String ATTACHMENT_WRONG_FOUND = "Attachment found where none should be";
    private static final String TESTFILE_RTF = "testfile; filename=randomfile.rtf";
    private static final String PLAIN_TEXT = "plain/text";
    private static final String NOT_PROPERLY_UNFOLDED_DECODED = "Not properly unfolded/decoded.";
    private static final String ATTACHMENT_NOT_IDENTIFIED = "Attachment was not identified";
    @Mock
    private Multipart multipart;
    @Mock
    private MailPart mailpart;
    @Mock
    private BODYSTRUCTURE bodystructure;
    
    private static final String MIXED_SUBTYPE = "mixed";
    
    @Before
    public void setUp() {
        bodystructure.subtype = MIXED_SUBTYPE;
    }

    public MimeMessageUtilityTest() {
        super();
    }

    public void testForBug33044_SubjectUnfolding() {
        String s = "=?UTF-8?B?UG90d2llcmR6ZW5pZSB6YW3Ds3dp?=\r\n =?UTF-8?B?ZW5pYQ==?=";
        s = MimeMessageUtility.decodeEnvelopeSubject(s);

        assertEquals("Subject nor properly unfolded/decoded.", "Potwierdzenie zam\u00f3wienia", s);
    }

    public void testForBug36072_AddressUnfolding() {
        String s = "=?UTF-8?Q?Wielkoszcz=C4=99ko=C5=9Bciskowiczkiewi?= =?UTF-8?Q?cz=C3=B3wnaOm=C3=B3jbo=C5=BCejestemno=C5=BCemwie?= " + "=?UTF-8?Q?leznacz=C4=85cychznak=C3=B3wsi=C4=99znaczyb?= =?UTF-8?Q?oprzecie=C5=BCniemo=C5=BCeby=C4=87zbyt=C5=82atwo!?= <foo@bar.tld>";
        s = MimeMessageUtility.decodeMultiEncodedHeader(s);
        assertEquals("Address nor properly unfolded/decoded.", "Wielkoszcz\u0119ko\u015bciskowiczkiewicz\u00f3wnaOm\u00f3jbo\u017cejestemno\u017cemwieleznacz\u0105cychznak\u00f3wsi\u0119znaczyboprzecie\u017cniemo\u017ceby\u0107zbyt\u0142atwo! " + "<foo@bar.tld>", s);

        s = "=?ISO-8859-1?Q?a?= b";
        s = MimeMessageUtility.decodeMultiEncodedHeader(s);
        assertEquals(NOT_PROPERLY_UNFOLDED_DECODED, "a b", s);

        s = "=?ISO-8859-1?Q?a?= =?ISO-8859-1?Q?b?=";
        s = MimeMessageUtility.decodeMultiEncodedHeader(s);
        assertEquals(NOT_PROPERLY_UNFOLDED_DECODED, "ab", s);

        s = "=?ISO-8859-1?Q?a?=  =?ISO-8859-1?Q?b?=";
        s = MimeMessageUtility.decodeMultiEncodedHeader(s);
        assertEquals(NOT_PROPERLY_UNFOLDED_DECODED, "ab", s);

        s = "=?ISO-8859-1?Q?a?=\r\n" + "\t=?ISO-8859-1?Q?b?=";
        s = MimeMessageUtility.decodeMultiEncodedHeader(s);
        assertEquals(NOT_PROPERLY_UNFOLDED_DECODED, "ab", s);

        s = "=?ISO-8859-1?Q?a_b?=";
        s = MimeMessageUtility.decodeMultiEncodedHeader(s);
        assertEquals(NOT_PROPERLY_UNFOLDED_DECODED, "a b", s);

        s = "=?ISO-8859-1?Q?a?= =?ISO-8859-2?Q?_b?=";
        s = MimeMessageUtility.decodeMultiEncodedHeader(s);
        assertEquals(NOT_PROPERLY_UNFOLDED_DECODED, "a b", s);

        s = "a b";
        s = MimeMessageUtility.decodeMultiEncodedHeader(s);
        assertEquals(NOT_PROPERLY_UNFOLDED_DECODED, "a b", s);
    }

    @Test
    public void testHasAttachment_MultipartDisposition() {
        boolean result = false;
        BodyPart bodyPart = Mockito.mock(BodyPart.class);
        try {
            Mockito.when(multipart.getBodyPart(org.mockito.Matchers.anyInt())).thenReturn(bodyPart);
            Mockito.when(multipart.getCount()).thenReturn(1);
            Mockito.when(bodyPart.getContentType()).thenReturn(PLAIN_TEXT);
            Mockito.when(bodyPart.getDisposition()).thenReturn("attachment; random=value");
            result = MimeMessageUtility.hasAttachments(multipart, MIXED_SUBTYPE);
        } catch (MessagingException | OXException | IOException e) {
            e.printStackTrace();
        }
        assertTrue(ATTACHMENT_NOT_IDENTIFIED, result);
    }

    public void testHasAttachment_MultipartFilename() {
        boolean result = false;
        BodyPart bodyPart = Mockito.mock(BodyPart.class);
        try {
            Mockito.when(multipart.getBodyPart(org.mockito.Matchers.anyInt())).thenReturn(bodyPart);
            Mockito.when(multipart.getCount()).thenReturn(1);
            Mockito.when(bodyPart.getContentType()).thenReturn(PLAIN_TEXT);
            Mockito.when(bodyPart.getDisposition()).thenReturn(TESTFILE_RTF);
            result = MimeMessageUtility.hasAttachments(multipart, MIXED_SUBTYPE);
        } catch (MessagingException | OXException | IOException e) {
            e.printStackTrace();
        }
        assertTrue(ATTACHMENT_NOT_IDENTIFIED, result);
    }

    public void testHasAttachment_MultipartSignature() {
        boolean result = true;
        BodyPart bodyPart = Mockito.mock(BodyPart.class);
        try {
            Mockito.when(multipart.getBodyPart(org.mockito.Matchers.anyInt())).thenReturn(bodyPart);
            Mockito.when(multipart.getCount()).thenReturn(1);
            Mockito.when(bodyPart.getContentType()).thenReturn("application/pkcs7-signature");
            result = MimeMessageUtility.hasAttachments(multipart, MIXED_SUBTYPE);
        } catch (MessagingException | OXException | IOException e) {
            e.printStackTrace();
        }
        assertFalse(ATTACHMENT_WRONG_FOUND, result);
    }
    
    public void testHasAttachment_MailpartDisposition() {
        boolean result = false;
        try {
            Mockito.when(mailpart.getEnclosedCount()).thenReturn(1);
            ContentDisposition cd = new ContentDisposition("attachment; random=value");
            Mockito.when(mailpart.getContentDisposition()).thenReturn(cd);
            ContentType ct = new ContentType(PLAIN_TEXT);
            Mockito.when(mailpart.getContentType()).thenReturn(ct);
            Mockito.when(mailpart.getEnclosedMailPart(0)).thenReturn(mailpart);
            result = MimeMessageUtility.hasAttachments(mailpart, "inline");
        } catch (MessagingException | OXException | IOException e) {
            e.printStackTrace();
        }
        assertTrue(ATTACHMENT_NOT_IDENTIFIED, result);
    }

    public void testHasAttachment_MailpartFilename() {
        boolean result = false;
        try {
            Mockito.when(mailpart.getEnclosedCount()).thenReturn(1);
            ContentDisposition cd = new ContentDisposition(TESTFILE_RTF);
            Mockito.when(mailpart.getContentDisposition()).thenReturn(cd);
            ContentType ct = new ContentType(PLAIN_TEXT);
            Mockito.when(mailpart.getContentType()).thenReturn(ct);
            Mockito.when(mailpart.getEnclosedMailPart(0)).thenReturn(mailpart);
            result = MimeMessageUtility.hasAttachments(mailpart, "inline");
        } catch (MessagingException | OXException | IOException e) {
            e.printStackTrace();
        }
        assertTrue(ATTACHMENT_NOT_IDENTIFIED, result);
    }

    public void testHasAttachment_MailpartSignature() {
        boolean result = true;
        try {
            ContentType ct = new ContentType("Application");
            Mockito.when(mailpart.getContentType()).thenReturn(ct);
            result = MimeMessageUtility.hasAttachments(mailpart, "Pkcs7-Signature");
        } catch (MessagingException | OXException | IOException e) {
            e.printStackTrace();
        }
        assertFalse(ATTACHMENT_WRONG_FOUND, result);
    }

    public void testHasAttachment_BodystructureDisposition() {
        boolean result = false;
        setContentTypePlainText(bodystructure);
        bodystructure.disposition = "attachment";
        result = MimeMessageUtility.hasAttachments(bodystructure);
        assertTrue(ATTACHMENT_NOT_IDENTIFIED, result);
    }

    public void testHasAttachment_BodystructureFilename() throws ParseException {
        boolean result = false;
        setContentTypePlainText(bodystructure);
        bodystructure.dParams = new ParameterList(";filename=randomfile.rtf");
        result = MimeMessageUtility.hasAttachments(bodystructure);
        assertTrue(ATTACHMENT_NOT_IDENTIFIED, result);
    }

    public void testHasAttachment_BodystructureSignature() {
        boolean result = true;
        bodystructure.type = "APPLICATION";
        bodystructure.subtype = "Pkcs7-Signature";
        result = MimeMessageUtility.hasAttachments(bodystructure);
        assertFalse(ATTACHMENT_WRONG_FOUND, result);
    }
    
    private static void setContentTypePlainText(BODYSTRUCTURE bs) {
        bs.type = "plain";
        bs.subtype = "text";
    }
}
