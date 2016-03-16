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
import org.json.JSONObject;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.structure.handler.MIMEStructureHandler;

/**
 * {@link Bug27640_StructureTest} - Test for output of structured JSON mail object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Bug27640_StructureTest extends AbstractMailTest {

    /**
     * Initializes a new {@link Bug27640_StructureTest}.
     */
    public Bug27640_StructureTest() {
        super();
    }

    /**
     * Initializes a new {@link Bug27640_StructureTest}.
     *
     * @param name The test name
     */
    public Bug27640_StructureTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testMIMEStructure() {
        try {
            getSession();
            final InputStream is = new ByteArrayInputStream(("date: Thu, 18 Jul 2013 17:56:26 +0200\n" +
                "from: =?UTF-8?Q?=22de_la_Fert=C3=A9=2C_Maurice=22?= <maurice.delaferte@open-xchange.com>\n" +
                "To: \n" +
                "    =?UTF-8?Q?=22de_la_Fert=C3=A9=2C_Maurice=22?= <maurice.delaferte@open-xchange.com>\n" +
                "subject: =?UTF-8?Q?Ufersicherungen_an_Seeschifffahr?= =?UTF-8?Q?ts-_und_Binnenschifffahrtsstrassen?=\n" +
                "MIME-Version: 1.0\n" +
                "Content-Type: multipart/alternative; \n" +
                "    boundary=\"----=_Part_3625_315087694.1374162989634\"\n" +
                "Message-ID: <123456>\n" +
                "\n" +
                "------=_Part_3625_315087694.1374162989634\n" +
                "Content-Type: text/plain; charset=utf-8\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "\n" +
                "test2\n" +
                "\n" +
                "\n" +
                "------=_Part_3625_315087694.1374162989634\n" +
                "Content-Type: text/html; charset=utf-8\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "\n" +
                "<html xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:w=\"urn:schemas-microsoft-com:office:word\" xmlns:m=\"http://schemas.microsoft.com/office/2004/12/omml\" xmlns=\"http://www.w3.org/TR/REC-html40\"><head><meta http-equiv=Content-Type content=\"text/html; charset=utf-8\"><meta name=Generator content=\"Microsoft Word 14 (filtered medium)\"><style><!--\n" +
                "/* Font Definitions */\n" +
                "@font-face\n" +
                "    {font-family:Calibri;\n" +
                "    panose-1:2 15 5 2 2 2 4 3 2 4;}\n" +
                "/* Style Definitions */\n" +
                "p.MsoNormal, li.MsoNormal, div.MsoNormal\n" +
                "    {margin:0cm;\n" +
                "    margin-bottom:.0001pt;\n" +
                "    font-size:11.0pt;\n" +
                "    font-family:\"Calibri\",\"sans-serif\";\n" +
                "    mso-fareast-language:EN-US;}\n" +
                "a:link, span.MsoHyperlink\n" +
                "    {mso-style-priority:99;\n" +
                "    color:blue;\n" +
                "    text-decoration:underline;}\n" +
                "a:visited, span.MsoHyperlinkFollowed\n" +
                "    {mso-style-priority:99;\n" +
                "    color:purple;\n" +
                "    text-decoration:underline;}\n" +
                "span.E-MailFormatvorlage17\n" +
                "    {mso-style-type:personal-compose;\n" +
                "    font-family:\"Calibri\",\"sans-serif\";\n" +
                "    color:windowtext;}\n" +
                ".MsoChpDefault\n" +
                "    {mso-style-type:export-only;\n" +
                "    font-family:\"Calibri\",\"sans-serif\";\n" +
                "    mso-fareast-language:EN-US;}\n" +
                "@page WordSection1\n" +
                "    {size:612.0pt 792.0pt;\n" +
                "    margin:70.85pt 70.85pt 2.0cm 70.85pt;}\n" +
                "div.WordSection1\n" +
                "    {page:WordSection1;}\n" +
                "--></style><!--[if gte mso 9]><xml>\n" +
                "<o:shapedefaults v:ext=\"edit\" spidmax=\"1026\" />\n" +
                "</xml><![endif]--><!--[if gte mso 9]><xml>\n" +
                "<o:shapelayout v:ext=\"edit\">\n" +
                "<o:idmap v:ext=\"edit\" data=\"1\" />\n" +
                "</o:shapelayout></xml><![endif]--></head><body lang=DE link=blue vlink=purple><div class=WordSection1><p class=MsoNormal>test2<o:p></o:p></p></div></body></html>\n" +
                "------=_Part_3625_315087694.1374162989634--\n" +
                "").getBytes());
            MimeMessage mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession(), is);
            final MailMessage mail = MimeMessageConverter.convertMessage(mimeMessage);

            final MIMEStructureHandler handler = new MIMEStructureHandler(-1L);
            new StructureMailMessageParser().setParseTNEFParts(true).parseMailMessage(mail, handler);

            final JSONObject jsonMailObject = handler.getJSONMailObject();

            System.out.println(jsonMailObject.toString(2));

            final String jsonSubject = jsonMailObject.getJSONObject("headers").getString("subject");
            final String expectedSubject = "Ufersicherungen an Seeschifffahrts- und Binnenschifffahrtsstrassen";
            assertEquals("Unexpected subject in JSON MIME structure.", expectedSubject, jsonSubject);

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
