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

package com.openexchange.mail;

import org.json.JSONObject;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.JsonMessageHandler;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.sessiond.impl.SessionObject;

/**
 * {@link MailJSONHandlerTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class MailJSONHandlerTest extends AbstractMailTest {

	/**
	 *
	 */
	public MailJSONHandlerTest() {
		super();
	}

	/**
	 * @param name
	 */
	public MailJSONHandlerTest(final String name) {
		super(name);
	}

	private static final String SRC = "Return-Path: <Colton-reboil@READINGCHILDREN.COM>\n" +
			"Received: from ox.netline-is.de ([unix socket])\n" +
			"	by ox (Cyrus v2.2.3) with LMTP; Wed, 09 Apr 2008 15:25:21 +0200\n" +
			"X-Sieve: CMU Sieve 2.2\n" +
			"Received: by ox.netline-is.de (Postfix, from userid 65534)\n" +
			"	id A2D973DD171; Wed,  9 Apr 2008 15:25:21 +0200 (CEST)\n" +
			"Received: from localhost by ox.netline-is.de\n" +
			"	with SpamAssassin (2.64 2004-01-11);\n" +
			"	Wed, 09 Apr 2008 15:25:21 +0200\n" +
			"From: Ibbotson <Colton-reboil@READINGCHILDREN.COM>\n" +
			"To: \"thorben@open-xchange.org\" <thorben@open-xchange.org>\n" +
			"Subject: *****SPAM***** Give your love life a boost\n" +
			"Date: Wed, 9 Apr 2008 09:28:24 -0430\n" +
			"Message-Id: <3425C917.3%Colton-reboil@READINGCHILDREN.COM>\n" +
			"X-Spam-Flag: YES\n" +
			"X-Spam-Checker-Version: SpamAssassin 2.64 (2004-01-11) on ox.netline-is.de\n" +
			"X-Spam-Level: *****\n" +
			"X-Spam-Status: Yes, hits=5.9 required=5.0 tests=BAYES_90,HTML_40_50,\n" +
			"	HTML_FONTCOLOR_BLUE,HTML_FONT_BIG,HTML_MESSAGE,RCVD_NUMERIC_HELO,\n" +
			"	TO_ADDRESS_EQ_REAL autolearn=no version=2.64\n" +
			"MIME-Version: 1.0\n" +
			"Content-Type: multipart/mixed; boundary=\"----------=_47FCC3C1.00B1B8CA\"\n" +
			"\n" +
			"This is a multi-part message in MIME format.\n" +
			"\n" +
			"------------=_47FCC3C1.00B1B8CA\n" +
			"Content-Type: text/plain\n" +
			"Content-Disposition: inline\n" +
			"Content-Transfer-Encoding: 8bit\n" +
			"\n" +
			"Spam detection software, running on the system \"ox.netline-is.de\", has\n" +
			"identified this incoming email as possible spam.  The original message\n" +
			"has been attached to this so you can view it (if it isn't spam) or block\n" +
			"similar future email.  If you have any questions, see\n" +
			"the administrator of that system for details.\n" +
			"\n" +
			"Content preview:  Achieve guaranteed growth in length and girth with our\n" +
			"  new product http://www.Cafennates.com/ Give your love life a boost\n" +
			"  Achieve guaranteed growth in length and girth with our new product\n" +
			"  URI:http://www.Cafennates.com/ http://www.Cafennates.com/ [...] \n" +
			"\n" +
			"Content analysis details:   (5.9 points, 5.0 required)\n" +
			"\n" +
			" pts rule name              description\n" +
			"---- ---------------------- --------------------------------------------------\n" +
			" 0.6 TO_ADDRESS_EQ_REAL     To: repeats address as real name\n" +
			" 1.5 RCVD_NUMERIC_HELO      Received: contains a numeric HELO\n" +
			" 0.9 HTML_40_50             BODY: Message is 40% to 50% HTML\n" +
			" 2.5 BAYES_90               BODY: Bayesian spam probability is 90 to 99%\n" +
			"                            [score: 0.9087]\n" +
			" 0.1 HTML_FONTCOLOR_BLUE    BODY: HTML font color is blue\n" +
			" 0.1 HTML_MESSAGE           BODY: HTML included in message\n" +
			" 0.3 HTML_FONT_BIG          BODY: HTML has a big font\n" +
			"\n" +
			"The original message was not completely plain text, and may be unsafe to\n" +
			"open with some email clients; in particular, it may contain a virus,\n" +
			"or confirm that your address can receive spam.  If you wish to view\n" +
			"it, it may be safer to save it to a file and open it with an editor.\n" +
			"\n" +
			"\n" +
			"------------=_47FCC3C1.00B1B8CA\n" +
			"Content-Type: message/rfc822; x-spam-type=original\n" +
			"Content-Description: original message before SpamAssassin\n" +
			"Content-Disposition: attachment\n" +
			"Content-Transfer-Encoding: 8bit\n" +
			"\n" +
			"Received: from netline.de (comfire.netline.de [192.168.32.1])\n" +
			"	by ox.netline-is.de (Postfix) with ESMTP id 39A903DD151\n" +
			"	for <thorben@open-xchange.com>; Wed,  9 Apr 2008 15:25:21 +0200 (CEST)\n" +
			"Received: from [10.20.30.11] (helo=www.open-xchange.org ident=mail)\n" +
			"	by netline.de with esmtp (Exim)\n" +
			"	id 1Jja7v-0003JD-00\n" +
			"	for thorben@open-xchange.com; Wed, 09 Apr 2008 15:14:11 +0200\n" +
			"Received: from mail.netline-is.de ([10.20.30.2] helo=netline.de)\n" +
			"	by www.open-xchange.org with esmtp (Exim 3.36 #1 (Debian))\n" +
			"	id 1JjaHv-0005pN-00\n" +
			"	for <thorben@open-xchange.org>; Wed, 09 Apr 2008 15:24:31 +0200\n" +
			"Received: from [190.80.225.212] (helo=132.200.80.190.m.sta.codetel.net.do)\n" +
			"	by netline.de with esmtp (Exim)\n" +
			"	id 1Jja7t-0003J2-00\n" +
			"	for thorben@open-xchange.org; Wed, 09 Apr 2008 15:14:09 +0200\n" +
			"User-Agent: Microsoft-Entourage/12.1.0.080305\n" +
			"Date: Wed, 9 Apr 2008 09:28:24 -0430\n" +
			"Subject: Give your love life a boost\n" +
			"From: Ibbotson <Colton-reboil@READINGCHILDREN.COM>\n" +
			"To: \"thorben@open-xchange.org\" <thorben@open-xchange.org>\n" +
			"Message-ID: <3425C917.3%Colton-reboil@READINGCHILDREN.COM>\n" +
			"Thread-Topic: Give your love life a boost\n" +
			"Thread-Index: AciaJA9f0FFVg8V6TFaa2rJgYzrKpg==\n" +
			"Mime-version: 1.0\n" +
			"Content-type: multipart/alternative;\n" +
			"        boundary=\"B_8623588310_73508\"\n" +
			"X-Scanner: exiscan *1Jja7t-0003J2-00*HjhzP2mBRfM* http://duncanthrax.net/exiscan/\n" +
			"\n" +
			"--B_8623588310_73508\n" +
			"Content-type: text/plain;\n" +
			"        charset=\"US-ASCII\"\n" +
			"Content-transfer-encoding: 7bit\n" +
			"\n" +
			"Achieve guaranteed growth in length and girth with our new product http://www.Cafennates.com/\n" +
			"\n" +
			"\n" +
			"--B_8623588310_73508\n" +
			"Content-type: text/html;\n" +
			"        charset=\"US-ASCII\"\n" +
			"Content-transfer-encoding: quoted-printable\n" +
			"\n" +
			"<HTML>\n" +
			"<HEAD>\n" +
			"<TITLE>Give your love life a boost</TITLE>\n" +
			"</HEAD>\n" +
			"<BODY>\n" +
			"<FONT COLOR=3D\"#000080\"><FONT SIZE=3D\"4\"><FONT FACE=3D\"Calibri, Verdana, =\n" +
			"Helvetica, Arial\"><SPAN STYLE=3D'font-size:11pt'>Achieve guaranteed =\n" +
			"growth in length and girth with our new product <a =\n" +
			"href=3D\"http://www.Cafennates.com/\">http://www.Cafennates.com/</a><BR>\n" +
			"</SPAN></FONT></FONT></FONT>\n" +
			"</BODY>\n" +
			"</HTML>\n" +
			"\n" +
			"\n" +
			"--B_8623588310_73508--\n" +
			"\n" +
			"------------=_47FCC3C1.00B1B8CA--\n" +
			"\n";


	public void testMailGet() {
        try {
            final SessionObject session = getSession();

            final MailMessage mail = MimeMessageConverter.convertMessage(SRC.getBytes(com.openexchange.java.Charsets.US_ASCII));

            final JsonMessageHandler handler = new JsonMessageHandler(
                MailAccount.DEFAULT_ID,
                "INBOX/123",
                DisplayMode.DISPLAY, false,
                session,
                UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), session.getContextId()), false, -1);
            new MailMessageParser().parseMailMessage(mail, handler);
            final JSONObject jo = handler.getJSONObject();

            // System.out.println(jo);

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
