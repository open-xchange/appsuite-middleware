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

package com.openexchange.mail.mime;

import java.io.ByteArrayInputStream;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import com.openexchange.mail.MailcapInitialization;
import junit.framework.TestCase;


/**
 * {@link MimeSmilFixerTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.0
 */
public class MimeSmilFixerTest extends TestCase {

    /**
     * Initializes a new {@link MimeSmilFixerTest}.
     */
    public MimeSmilFixerTest() {
        super();
    }

    public void testFixSmil() {
        try {
            String src = "From: \"Mobile Inbound Agent\" incomingmessage@service-provider.com\n" +
                "To: someone@example.com\n" +
                "Subject: This is a multimedia message (Open the message to view its content)\n" +
                "Date: Mon, 7 Nov 2005 17:52:00 +0800\n" +
                "Content-class: MS-OMS-MMS\n" +
                "X-MS-Reply-to-mobile: +8613601391354\n" +
                "MIME-Version: 1.0\n" +
                "Content-Type: multipart/related; type=\"application/smil\"; boundary=\"------------Boundary=_thisisboundary\"\n" +
                "\n" +
                "--------------Boundary=_thisisboundary \n" +
                "Content-Type: application/smil; name=\"mmspresent.smil\"\n" +
                "Content-Location: \"mmspresent.smil\"\n" +
                "Content-Transfer-Encoding: Base64\n" +
                "\n" +
                "PHNtaWw+... 1pbD4=\n" +
                "--------------Boundary=_thisisboundary \n" +
                "Content-Type: text/plain; name=\"textpart.txt\"\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "Content-Location: textpart.txt\n" +
                "\n" +
                "Hello World!\n" +
                "--------------Boundary=_thisisboundary \n" +
                "Content-Type: image/gif; name=\"imagepart.gif\"\n" +
                "Content-Transfer-Encoding: Base64\n" +
                "Content-Location:imagepart.gif\n" +
                "\n" +
                "R0lGODlheABaAPf/...BDQi6j4uQAxwcixRzZErI5ROjfvSHJcmRMGBAAOw==\n" +
                "--------------Boundary=_thisisboundary \n" +
                "Content-Type: audio/mid; name=\"audiopart.mid\"\n" +
                "Content-Transfer-Encoding: Base64\n" +
                "Content-Location: audiopart.mid\n" +
                "\n" +
                "TVRoZAAAAAY...XBDfwA/fwA6f4dAOgAAPwAAQwAA/y8A\n" +
                "--------------Boundary=_thisisboundary--";

            MailcapInitialization.getInstance().init();
            final MimeMessage appleMimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession(), new ByteArrayInputStream(src.getBytes()));


            MimeMessage processed = MimeSmilFixer.getInstance().process(appleMimeMessage);

            assertTrue("No multipart content", processed.getContent() instanceof Multipart);
            Multipart multipart = (Multipart) processed.getContent();
            assertTrue("Unexpected Content-Type header.", multipart.getContentType().startsWith("multipart/mixed"));
            int count = multipart.getCount();
            assertEquals("Unexpected number of body parts.", 3, count);

            BodyPart bodyPart = multipart.getBodyPart(0);
            assertTrue("Unexpected Content-Type header.", bodyPart.getContentType().startsWith("text/plain"));

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
