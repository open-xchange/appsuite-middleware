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

package com.openexchange.mail.structure;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.mail.internet.MimeMessage;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.structure.handler.MIMEStructureHandler;

/**
 * {@link Bug29227_StructureTest} - Test for output of structured JSON mail object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Bug29227_StructureTest extends AbstractMailTest {

    /**
     * Initializes a new {@link Bug29227_StructureTest}.
     */
    public Bug29227_StructureTest() {
        super();
    }

    /**
     * Initializes a new {@link Bug29227_StructureTest}.
     *
     * @param name The test name
     */
    public Bug29227_StructureTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testMIMEStructure() {
        try {
            getSession();
            final InputStream is = new ByteArrayInputStream(("From: Thomas Siedentopf <thomas.siedentopf@open-xchange.com>\n" +
                "Message-Id: <78C422B9-380A-4A65-BF83-EFE1D82BE266@open-xchange.com>\n" +
                "Date: Thu, 10 Oct 2013 16:54:40 +0200\n" +
                "To: \"usm-internal@open-xchange.com\" <usm-internal@open-xchange.com>\n" +
                "Mime-Version: 1.0 (Mac OS X Mail 6.6 \\(1510\\))\n" +
                "X-Mailer: Apple Mail (2.1510)\n" +
                "X-purgate-ID: 151428::1381416880-0000781E-F10036FE/0-0/0-0\n" +
                "X-purgate-type: clean\n" +
                "X-purgate-size: 880\n" +
                "X-purgate-Ad: Categorized by eleven eXpurgate (R) http://www.eleven.de\n" +
                "X-Notice: Whitelisted\n" +
                "X-purgate: This mail is considered clean (visit http://www.eleven.de for\n" +
                "    further information)\n" +
                "X-purgate: clean\n" +
                "Subject: [Usm-internal] Displayname in EAS\n" +
                "X-BeenThere: usm-internal@open-xchange.com\n" +
                "X-Mailman-Version: 2.1.11\n" +
                "Precedence: list\n" +
                "Reply-To: usm-internal@open-xchange.com\n" +
                "List-Id: \"Mailinglist: USM related coordination\"\n" +
                "    <usm-internal.open-xchange.com>\n" +
                "List-Unsubscribe: <https://lists-int.open-xchange.com/mailman/options/usm-internal>,\n" +
                "    <mailto:usm-internal-request@open-xchange.com?subject=unsubscribe>\n" +
                "List-Archive: <http://lists-int.open-xchange.com/pipermail/usm-internal>\n" +
                "List-Post: <mailto:usm-internal@open-xchange.com>\n" +
                "List-Help: <mailto:usm-internal-request@open-xchange.com?subject=help>\n" +
                "List-Subscribe: <https://lists-int.open-xchange.com/mailman/listinfo/usm-internal>,\n" +
                "    <mailto:usm-internal-request@open-xchange.com?subject=subscribe>\n" +
                "Content-Type: multipart/mixed; boundary=\"===============1069459393==\"\n" +
                "Sender: usm-internal-bounces@open-xchange.com\n" +
                "Errors-To: usm-internal-bounces@open-xchange.com\n" +
                "\n" +
                "\n" +
                "--===============1069459393==\n" +
                "Content-Type: multipart/signed; boundary=\"Apple-Mail=_A3CDE761-6E8C-4EF6-813D-04E4A348F283\"; protocol=\"application/pgp-signature\"; micalg=pgp-sha1\n" +
                "\n" +
                "\n" +
                "--Apple-Mail=_A3CDE761-6E8C-4EF6-813D-04E4A348F283\n" +
                "Content-Transfer-Encoding: quoted-printable\n" +
                "Content-Type: text/plain;\n" +
                "    charset=windows-1252\n" +
                "\n" +
                "Hi,\n" +
                "\n" +
                "simple question - which name should be displayed in a From: Field of a =\n" +
                "mail that was send over by an EAS client?\n" +
                "\n" +
                "E-Mail Address?\n" +
                "Displayname?\n" +
                "Name Surname?\n" +
                "=85?\n" +
                "\n" +
                "Thomas\n" +
                "\n" +
                "--Apple-Mail=_A3CDE761-6E8C-4EF6-813D-04E4A348F283\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "Content-Disposition: attachment;\n" +
                "    filename=signature.asc\n" +
                "Content-Type: application/pgp-signature;\n" +
                "    name=signature.asc\n" +
                "Content-Description: Message signed with OpenPGP using GPGMail\n" +
                "\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iEYEARECAAYFAlJWv7AACgkQdERBopHyS4M0tQCgngPOOZwVZBSazGKlZhrb4Xer\n" +
                "RXAAnjefwajmiOSQHyVLXNxSbD/0ZfbH\n" +
                "=Zvzy\n" +
                "-----END PGP SIGNATURE-----\n" +
                "\n" +
                "--Apple-Mail=_A3CDE761-6E8C-4EF6-813D-04E4A348F283--\n" +
                "\n" +
                "--===============1069459393==\n" +
                "Content-Type: text/plain; charset=\"us-ascii\"\n" +
                "MIME-Version: 1.0\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "Content-Disposition: inline\n" +
                "\n" +
                "_______________________________________________\n" +
                "usm-internal mailing list\n" +
                "usm-internal@open-xchange.com\n" +
                "https://lists-int.open-xchange.com/mailman/listinfo/usm-internal\n" +
                "\n" +
                "--===============1069459393==--").getBytes());
            MimeMessage mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession(), is);
            final MailMessage mail = MimeMessageConverter.convertMessage(mimeMessage);

            final MIMEStructureHandler handler = new MIMEStructureHandler(-1L);
            new StructureMailMessageParser().setParseTNEFParts(true).parseMailMessage(mail, handler);

            final JSONObject jsonMailObject = handler.getJSONMailObject();
            assertNotNull("Structured JSON mail object is null.", jsonMailObject);

            final JSONArray bodies = jsonMailObject.optJSONArray("body");
            assertNotNull("Structured JSON mail object has no body party.", bodies);

            assertEquals("Unexpected number of body parts", 2, bodies.length());

            final JSONObject signed = bodies.optJSONObject(0);
            assertNotNull("Missing multipart/signed part.", signed);

            final JSONArray signedBodies = signed.optJSONArray("body");
            assertNotNull("SignedJSON mail object has no body party.", signedBodies);

            assertEquals("Unexpected number of signed body parts", 2, signedBodies.length());

            // System.out.println(jsonMailObject.toString(2));

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
