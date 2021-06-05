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

package com.openexchange.mail.mime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import org.junit.Test;
import com.openexchange.mail.MailcapInitialization;


/**
 * {@link MimeSmilFixerTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.0
 */
public class MimeSmilFixerTest {
    /**
     * Initializes a new {@link MimeSmilFixerTest}.
     */
    public MimeSmilFixerTest() {
        super();
    }

         @Test
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
