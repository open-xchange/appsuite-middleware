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

package com.openexchange.mail.mime.processing;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.internet.MimeMessage;
import junit.framework.TestCase;
import com.openexchange.html.HtmlService;
import com.openexchange.html.SimHtmlService;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Streams;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.sessiond.impl.SessionObject;


/**
 * {@link MimeReplyTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MimeReplyTest extends TestCase {

    /**
     * Initializes a new {@link MimeReplyTest}.
     */
    public MimeReplyTest() {
        super();
    }

    public void testForBug33061() {
        final byte[] bytes = ("Date: Tue, 10 Jun 2014 15:54:55 +0200 (CEST)\n" +
            "From: aaa@open-xchange.com\n" +
            "To: bbb@open-xchange.com\n" +
            "Message-ID: <12345678>\n" +
            "Subject: Blah\n" +
            "MIME-Version: 1.0\n" +
            "Content-Type: multipart/alternative; boundary=\"----=_Part_163_136634806.1402408495930\"\n" +
            "X-Priority: 3\n" +
            "Importance: Medium\n" +
            "X-Mailer: Open-Xchange Mailer v7.6.0-Rev4\n" +
            "X-Originating-Client: open-xchange-appsuite\n" +
            "\n" +
            "------=_Part_163_136634806.1402408495930\n" +
            "MIME-Version: 1.0\n" +
            "Content-Type: text/plain; charset=UTF-8\n" +
            "Content-Transfer-Encoding: quoted-printable\n" +
            "\n" +
            "blah blah blah blah\n" +
            "\n" +
            "\n" +
            "------=_Part_163_136634806.1402408495930\n" +
            "Content-Type: multipart/related; \n" +
            "    boundary=\"----=_Part_164_761165799.1402408495931\"\n" +
            "\n" +
            "------=_Part_164_761165799.1402408495931\n" +
            "MIME-Version: 1.0\n" +
            "Content-Type: text/html; charset=UTF-8\n" +
            "Content-Transfer-Encoding: 7bit\n" +
            "\n" +
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\"><head>\n" +
            "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
            " \n" +
            " </head><body>\n" +
            " \n" +
            "  <p>blah blah blah blah</p>\n" +
            " \n" +
            "</body></html>\n" +
            "------=_Part_164_761165799.1402408495931\n" +
            "Content-Type: image/png; name=\"Screen Shot 2014-06-10 at 15.54.06.png\"\n" +
            "Content-Transfer-Encoding: base64\n" +
            "Content-ID: <e654a06fd06b4a6ea2788e79dd908b19@Open-Xchange>\n" +
            "Content-Disposition: inline; filename=\"Screen Shot 2014-06-10 at\n" +
            " 15.54.06.png\"\n" +
            "\n" +
            "iVBORw0KGgoAAAANSUhEUgAABboAAAPgCAYAAAD0pMq/AAAYI2lDQ1BJQ0MgUHJvZmlsZQAAWIWV\n" +
            "eQdUFE2zds/OBliWJeeck+QMknPOGYEl55xRiSJBRRBQBFRQQVDBQBIxIUgQEVTAgEgwkFRQQBGQ\n" +
            "OwR9v/+9/z333D5nZp6trqp5uqu6Z2oHADZmUnh4MIoagJDQ6EhrA21uRydnbtwYwAIUYALkgIzk\n" +
            "FRWuZWlpCv7HtjIMoK3rc/EtX/+z3v+30Xj7RHkBAFki2NM7yisEwQ0AoFm9wiOjAcAMIHK+uOjw\n" +
            "LbyEYPpIhCAAWLIt7LeD2bew5w6W2taxtdZBsC4AZAQSKdIPAOKWf+5YLz/EDzEc6aMN9Q4IRVQz\n" +
            "EKzu5U/yBoC1A9HZExIStoUXECzs+R9+/P4fn55/fZJIfn/xzli2G5luQFR4MCnh/zgd/3sLCY75\n" +
            "cw9e5CD4Rxpab40ZmbdLQWEmW5iA4LZQT3MLBNMiuDvAe1t/C7/2jzG029Wf94rSQeYMMAIk2N4k\n" +
            "XRMEI3OJYowJstPaxTKkyG1bRB9lHhBtZLuLPSPDrHf9o2J9ovRs/mB/HyPTXZ9ZocHmf/AZ3wB9\n" +
            "AhCAAAQgAAEIQAACEIAABCAAAQhAAAIQgAAEIAABCEAAAhCAAAQgAAEIQAACEIAABCAAAQhAAAIQ\n" +
            "gAAEIAABCEAAAhCAAAQgAAEIQAACEIAABCAAAQhAAAIQgAAEIAABCEAAAhCAAAQgAAEIQAACEIDA\n" +
            "QCPw/wNsHbPpztp8EAAAAABJRU5ErkJggg==\n" +
            "------=_Part_164_761165799.1402408495931--\n" +
            "\n" +
            "------=_Part_163_136634806.1402408495930--\n").getBytes();

        try {
            final javax.mail.Session mailSession = MimeDefaultSession.getDefaultSession();
            final MimeMessage originalMessage = new MimeMessage(mailSession, Streams.newByteArrayInputStream(bytes));
            // Convert
            final MailMessage mailMessage = MimeMessageConverter.convertMessage(originalMessage);
            // More parameters
            final List<String> list = new LinkedList<String>();
            final ContentType retvalContentType = new ContentType();
            final Locale locale = Locale.US;
            final LocaleAndTimeZone ltz = new LocaleAndTimeZone(locale, "Europe/Berlin");

            final UserSettingMail usm = new UserSettingMail(17, 1337);
            usm.parseBits(381191);
            usm.setDisplayHtmlInlineContent(false);

            final SessionObject session = new SessionObject("Bug31644");
            session.setContextId(1337);
            session.setUsername("17");

            ServerServiceRegistry.getInstance().addService(HtmlService.class, new SimHtmlService());

            MimeReply.generateReplyText(mailMessage, retvalContentType, StringHelper.valueOf(locale), ltz, usm, mailSession, session, 0, list);

            assertEquals("Unexpected number of reply texts", 1, list.size());

            final StringBuilder replyTextBuilder = new StringBuilder(8192 << 1);
            for (int i = list.size() - 1; i >= 0; i--) {
                replyTextBuilder.append(list.get(i));
            }

            // System.out.println(replyTextBuilder.toString());

            Pattern p = Pattern.compile(Pattern.quote("blah"));
            Matcher m = p.matcher(replyTextBuilder.toString());
            int cnt = 0;
            while (m.find()) {
                cnt++;
            }

            assertEquals("Unexpected reply text", 4, cnt);
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testForBug31644() {
        final byte[] bytes = ("From: asd@asd.de\n" +
            "To: dfg@dfg.com\n" +
            "Subject: Subject\n" +
            "Date: Tue, 18 Mar 2014 09:51:20 +0100\n" +
            "Message-ID: <07432353E3AC4F7C9924A69D2F04A4C4@de696630.foogroup.com>\n" +
            "MIME-Version: 1.0\n" +
            "Content-Type: multipart/mixed;\n" +
            "    boundary=\"----=_NextPart_000_0005_01CF428F.A3C3FA00\"\n" +
            "X-Mailer: Microsoft Office Outlook 11\n" +
            "Thread-Index: Ac9ChzuoyUyiFt75RLKVtS9ZgkIl0A==\n" +
            "\n" +
            "This is a multi-part message in MIME format.\n" +
            "\n" +
            "------=_NextPart_000_0005_01CF428F.A3C3FA00\n" +
            "Content-Type: text/plain;\n" +
            "    charset=\"utf-8\"\n" +
            "Content-Transfer-Encoding: quoted-printable\n" +
            "\n" +
            "             =20\n" +
            "Hallo Frau Barfoo,\n" +
            "\n" +
            "removed for privacy\n" +
            "\n" +
            "Mit freundlichen Gr=C3=BC=C3=9Fen\n" +
            "=20\n" +
            "Jan Doe\n" +
            "Verkauf Gro=C3=9Fkunden\n" +
            "\n" +
            "Tel. 1111/123123-15\n" +
            "Fax 0265756/345543543-11\n" +
            "=20\n" +
            "=20\n" +
            "\n" +
            "=20\n" +
            "AUTO ZENTRUM KAWASAKALALA\n" +
            "Norbert Schlegelfegelhebel GmbH & Co KG\n" +
            "\n" +
            "=20\n" +
            "=20\n" +
            "\n" +
            "\n" +
            "------=_NextPart_000_0005_01CF428F.A3C3FA00\n" +
            "Content-Type: image/jpeg;\n" +
            "    name=\"P1010752.jpg\"\n" +
            "Content-Transfer-Encoding: base64\n" +
            "Content-Disposition: attachment;\n" +
            "    filename=\"P1010752.jpg\"\n" +
            "\n" +
            "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0a\n" +
            "KfU9aTjtRnj1oC9wzg5pDxQaaSSeuKYDjTT1xmjNGaNhMBkmpmbEeB6VEgywp8xwAKZlJ3aR/9k=\n" +
            "\n" +
            "------=_NextPart_000_0005_01CF428F.A3C3FA00\n" +
            "Content-Type: image/jpeg;\n" +
            "    name=\"P1010753.jpg\"\n" +
            "Content-Transfer-Encoding: base64\n" +
            "Content-Disposition: attachment;\n" +
            "    filename=\"P1010753.jpg\"\n" +
            "\n" +
            "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0a\n" +
            "4pFCnk9uKjl+Zs03fwBznpRYCUuCQo47UyUlBwc5poVh8xJ60x2J4OPxoGNDknrQxJOByaYcqM5H\n" +
            "4U+JgTknmgD/2Q==\n" +
            "\n" +
            "------=_NextPart_000_0005_01CF428F.A3C3FA00\n" +
            "Content-Type: image/jpeg;\n" +
            "    name=\"P1010754.jpg\"\n" +
            "Content-Transfer-Encoding: base64\n" +
            "Content-Disposition: attachment;\n" +
            "    filename=\"P1010754.jpg\"\n" +
            "\n" +
            "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0a\n" +
            "kzSnA69KaMZwKBD1IxTSBkHtQSB0oBBGM0AMY/NSqSckkYoIGc5oCkjAB5oQm7AT2HNOWB3AODip\n" +
            "reDOSTgj9KmknESADk00rGbk3ohI41gTLEAgVBNdk/KvA9qgllaRiSaiPAyTQ3oEYdWf/9k=\n" +
            "\n" +
            "------=_NextPart_000_0005_01CF428F.A3C3FA00\n" +
            "Content-Type: image/jpeg;\n" +
            "    name=\"P1010755.jpg\"\n" +
            "Content-Transfer-Encoding: base64\n" +
            "Content-Disposition: attachment;\n" +
            "    filename=\"P1010755.jpg\"\n" +
            "\n" +
            "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0a\n" +
            "EwJWIIweRioGyCQD+NSZHQd/Wo5BkAgYHpQtwEB96aT60A4HFJkA9etNhcUnAx1zSEjHJz+NBYEZ\n" +
            "pppMY4kDrSZB6k5zSE5xyPSkPBouFxSe/vzRjIPOQKbnJNNJJOc+9C0Ef//Z\n" +
            "\n" +
            "------=_NextPart_000_0005_01CF428F.A3C3FA00\n" +
            "Content-Type: image/jpeg;\n" +
            "    name=\"P1010756.jpg\"\n" +
            "Content-Transfer-Encoding: base64\n" +
            "Content-Disposition: attachment;\n" +
            "    filename=\"P1010756.jpg\"\n" +
            "\n" +
            "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0a\n" +
            "QxCNxz6GkOD607aTyDQFyPWk00IYMjkU8gEYzSAHOMd6CRTVxiEjHFIRxwPqaDnI/KlJz1JoAYQB\n" +
            "69aFwTjNBPXHIoGOCCcUlcD/2Q==\n" +
            "\n" +
            "------=_NextPart_000_0005_01CF428F.A3C3FA00\n" +
            "Content-Type: image/jpeg;\n" +
            "    name=\"P1010757.jpg\"\n" +
            "Content-Transfer-Encoding: base64\n" +
            "Content-Disposition: attachment;\n" +
            "    filename=\"P1010757.jpg\"\n" +
            "\n" +
            "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0a\n" +
            "KACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAoooo\n" +
            "AKKKKACiiigD/9k=\n" +
            "\n" +
            "------=_NextPart_000_0005_01CF428F.A3C3FA00\n" +
            "Content-Type: image/jpeg;\n" +
            "    name=\"P1010758.jpg\"\n" +
            "Content-Transfer-Encoding: base64\n" +
            "Content-Disposition: attachment;\n" +
            "    filename=\"P1010758.jpg\"\n" +
            "\n" +
            "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0a\n" +
            "FMAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKK\n" +
            "ACiiigAooooAKKKKACiiigAooooAKKKKACiiigAopCwAqJpT0FAH/9k=\n" +
            "\n" +
            "------=_NextPart_000_0005_01CF428F.A3C3FA00\n" +
            "Content-Type: multipart/alternative;\n" +
            "    boundary=\"----=_NextPart_Avast_Info_Boundary\"\n" +
            "\n" +
            "------=_NextPart_Avast_Info_Boundary\n" +
            "Content-Type: text/plain\n" +
            "Content-Transfer-Encoding: quoted-printable\n" +
            "Content-Description: avast info\n" +
            "\n" +
            "\n" +
            "\n" +
            "---\n" +
            "avast! Antivirus: Ausgehende Nachricht sauber.\n" +
            "Virus-Datenbank (VPS): 140204-0, 04.02.2014\n" +
            "Getestet am: 18.03.2014 09:51:33\n" +
            "avast! - copyright (c) 1988-2014 AVAST Software.\n" +
            "http://www.avast.com\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "------=_NextPart_Avast_Info_Boundary--\n" +
            "\n" +
            "------=_NextPart_000_0005_01CF428F.A3C3FA00--\n" +
            "\n").getBytes();

        try {
            final javax.mail.Session mailSession = MimeDefaultSession.getDefaultSession();
            final MimeMessage originalMessage = new MimeMessage(mailSession, Streams.newByteArrayInputStream(bytes));
            // Convert
            final MailMessage mailMessage = MimeMessageConverter.convertMessage(originalMessage);
            // More parameters
            final List<String> list = new LinkedList<String>();
            final ContentType retvalContentType = new ContentType();
            final Locale locale = Locale.US;
            final LocaleAndTimeZone ltz = new LocaleAndTimeZone(locale, "Europe/Berlin");

            final UserSettingMail usm = new UserSettingMail(17, 1337);
            usm.parseBits(364807);

            final SessionObject session = new SessionObject("Bug31644");
            session.setContextId(1337);
            session.setUsername("17");

            MimeReply.generateReplyText(mailMessage, retvalContentType, StringHelper.valueOf(locale), ltz, usm, mailSession, session, 0, list);

            assertEquals("Unexpected number of reply texts", 1, list.size());

            final StringBuilder replyTextBuilder = new StringBuilder(8192 << 1);
            for (int i = list.size() - 1; i >= 0; i--) {
                replyTextBuilder.append(list.get(i));
            }

            // System.out.println(replyTextBuilder.toString());

            assertTrue("Missing sub-text in composed reply text", replyTextBuilder.indexOf("AUTO ZENTRUM") > 0);
            assertTrue("Missing sub-text in composed reply text", replyTextBuilder.indexOf("AVAST Software") > 0);
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
