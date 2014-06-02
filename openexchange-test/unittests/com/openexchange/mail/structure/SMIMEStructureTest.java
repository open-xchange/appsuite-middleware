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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.mail.structure;

import org.json.JSONObject;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.structure.handler.MIMEStructureHandler;

/**
 * {@link SMIMEStructureTest} - Test for output of structured JSON mail object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SMIMEStructureTest extends AbstractMailTest {

    /**
     * Initializes a new {@link SMIMEStructureTest}.
     */
    public SMIMEStructureTest() {
        super();
    }

    /**
     * Initializes a new {@link SMIMEStructureTest}.
     *
     * @param name The test name
     */
    public SMIMEStructureTest(final String name) {
        super(name);
    }

    public void testAnotherMIMEStructure() {
        try {
            getSession();

            final byte[] smime = (
                "From: \"Jane Doe\" <jane@spoof.de>\n" +
                "To: <barfoo@trub.org>\n" +
                "Subject: SMIME\n" +
                "Date: Thu, 30 Jun 2011 11:40:19 +0200\n" +
                "Message-ID: <000233b01cc3709$ba120c00$2e362400$@de>\n" +
                "MIME-Version: 1.0\n" +
                "Content-Language: de\n" +
                "Content-Type: multipart/signed;\n" +
                "    protocol=\"application/x-pkcs7-signature\";\n" +
                "    micalg=MD5;\n" +
                "    boundary=\"----=_NextPart_000_0003_01CC371A.78F2B0B0\"\n" +
                "\n" +
                "This is a multi-part message in MIME format.\n" +
                "\n" +
                "------=_NextPart_000_0003_01CC371A.78F2B0B0\n" +
                "Content-Type: multipart/related;\n" +
                "    boundary=\"----=_NextPart_001_0004_01CC371A.78F2B0B0\"\n" +
                "\n" +
                "\n" +
                "------=_NextPart_001_0004_01CC371A.78F2B0B0\n" +
                "Content-Type: multipart/alternative;\n" +
                "    boundary=\"----=_NextPart_002_0005_01CC371A.78F2D7C0\"\n" +
                "\n" +
                "\n" +
                "------=_NextPart_002_0005_01CC371A.78F2D7C0\n" +
                "Content-Type: text/plain;\n" +
                "    charset=\"UTF-8\"\n" +
                "Content-Transfer-Encoding: quoted-printable\n" +
                "\n" +
                "Hallo Peter Pan,\n" +
                "\n" +
                "=20\n" +
                "\n" +
                "blah blah blah.\n" +
                "\n" +
                "\n" +
                "------=_NextPart_002_0005_01CC371A.78F2D7C0\n" +
                "Content-Type: text/html;\n" +
                "    charset=\"UTF-8\"\n" +
                "Content-Transfer-Encoding: quoted-printable\n" +
                "\n" +
                "<html xmlns:v=3D\"urn:schemas-microsoft-com:vml\" =\n" +
                "xmlns:o=3D\"urn:schemas-microsoft-com:office:office\" =\n" +
                "xmlns:w=3D\"urn:schemas-microsoft-com:office:word\" =\n" +
                "xmlns:m=3D\"http://schemas.microsoft.com/office/2004/12/omml\" =\n" +
                "xmlns=3D\"http://www.w3.org/TR/REC-html40\"><head><meta http-equiv=3DContent-Type content=3D\"text/html; charset=3Dutf-8\">\n" +
                "</head><body lang=3DDE link=3Dblue =\n" +
                "vlink=3Dpurple><div class=3DWordSection1><p class=3DMsoNormal>Hallo =\n" +
                "Dirk,<o:p></o:p></p><p class=3DMsoNormal><o:p>&nbsp;</o:p></p><p =\n" +
                "class=3DMsoNormal>blah blah blah. =\n" +
                "<o:p></o:p></p><p =\n" +
                "class=3DMsoNormal><o:p>&nbsp;</o:p></p><p =\n" +
                "</body></html>\n" +
                "------=_NextPart_002_0005_01CC371A.78F2D7C0--\n" +
                "\n" +
                "------=_NextPart_001_0004_01CC371A.78F2B0B0\n" +
                "Content-Type: image/jpeg;\n" +
                "    name=\"image001.jpg\"\n" +
                "Content-Transfer-Encoding: base64\n" +
                "Content-ID: <image001.jpg@01CC371A.78CBEED0>\n" +
                "\n" +
                "/9j/4AAQSkZJRgABAgAAZABkAAD/7AARRHVja3kAAQAEAAAAVQAA/+4ADkFkb2JlAGTAAAAAAf/b\n" +
                "AIQAAgEBAQEBAgEBAgMCAQIDAwICAgIDAwMDAwMDAwQDBAQEBAMEBAUGBgYFBAcHCAgHBwoKCgoK\n" +
                "BoA0AaANAGgDQBoA0AaANAGgDQBoA0AaANAYLf4B/l+U3+J8vw/tfw+/QAr8C/L8wfP/AE/Z/H7t\n" +
                "AZ6ANAay/Mb4fH7Pj8pfm/j/AFaA+pfAfh8w/L8PmH/z9+gM9AGgDQBoDQl/7Kvyf2Pl+f4D839W\n" +
                "heCN+hA0B//Z\n" +
                "\n" +
                "------=_NextPart_001_0004_01CC371A.78F2B0B0\n" +
                "Content-Type: image/jpeg;\n" +
                "    name=\"image002.jpg\"\n" +
                "Content-Transfer-Encoding: base64\n" +
                "Content-ID: <image002.jpg@01CC371A.78CBEED0>\n" +
                "\n" +
                "/9j/4AAQSkZJRgABAgAAZABkAAD/7AARRHVja3kAAQAEAAAAVQAA/+4ADkFkb2JlAGTAAAAAAf/b\n" +
                "AIQAAgEBAQEBAgEBAgMCAQIDAwICAgIDAwMDAwMDAwQDBAQEBAMEBAUGBgYFBAcHCAgHBwoKCgoK\n" +
                "/i3vJvoLx36yL1o/RGfi5SePGO8HwtQ4rLMnGC2vqvwRWHlVZtobzHiyrjy3yzl2ZTZwY4DuoPaO\n" +
                "5em2wZuLC3G2Babb0Yv+K8p6jy87KzJ3eSEhfLbt0dhpECLxAi30s1KiqIOr60i//9k=\n" +
                "\n" +
                "------=_NextPart_001_0004_01CC371A.78F2B0B0--\n" +
                "\n" +
                "------=_NextPart_000_0003_01CC371A.78F2B0B0\n" +
                "Content-Type: application/x-pkcs7-signature;\n" +
                "    name=\"smime.p7s\"\n" +
                "Content-Transfer-Encoding: base64\n" +
                "Content-Disposition: attachment;\n" +
                "    filename=\"smime.p7s\"\n" +
                "\n" +
                "MIAGCSqGSIb3DQEHAqCAMIACAQExDjAMBggqhkiG9w0CBQUAMIAGCSqGSIb3DQEHAQAAoIISdjCC\n" +
                "A58wggKHoAMCAQICASYwDQYJKoZIhvcNAQEFBQAwcTELMAkGA1UEBhMCREUxHDAaBgNVBAoTE0Rl\n" +
                "+143qlXSfnnnWFAgjCePzaRpl6nzl/WUqBNWldF9POxRqhTnuze1Kt06AyfqxK2Z8fX8FUj8h5Tj\n" +
                "sYOMGi8yNP9Ik5fjuUeLWSoVPnor3rE5YW68xyY79VLOb2pLIqMsF7x0XXkGVn31AAAAAAAA\n" +
                "\n" +
                "------=_NextPart_000_0003_01CC371A.78F2B0B0--\n" +
                "\n").getBytes();

            final MailMessage mail = MimeMessageConverter.convertMessage(smime);

            final MIMEStructureHandler handler = new MIMEStructureHandler(-1L);
            new StructureMailMessageParser().parseMailMessage(mail, handler);

            final JSONObject jsonMailObject = handler.getJSONMailObject();
            assertNotNull("Structured JSON mail object is null.", jsonMailObject);

            // System.out.println(jsonMailObject.toString(2));

            assertTrue("Detected a body object, but shouldn't be there.", !jsonMailObject.hasAndNotNull("body"));

            assertTrue("Missing S/MIME body text.", jsonMailObject.hasAndNotNull("smime_body_text"));
            assertTrue("Missing S/MIME body data.", jsonMailObject.hasAndNotNull("smime_body_data"));
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
