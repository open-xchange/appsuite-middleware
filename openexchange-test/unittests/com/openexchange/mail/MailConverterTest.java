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

import static com.openexchange.java.Charsets.US_ASCII;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.MailPartHandler;

/**
 * MailConverterTest
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class MailConverterTest extends AbstractMailTest {

	private static final String SRC = "Return-Path: <tanyascott.com@quecall.com>\n" +
			"Received: from ox.netline-is.de ([unix socket])\n" +
			"	by ox (Cyrus v2.2.3) with LMTP; Thu, 28 Jun 2007 23:21:40 +0200\n" +
			"X-Sieve: CMU Sieve 2.2\n" +
			"Received: by ox.netline-is.de (Postfix, from userid 65534)\n" +
			"	id 19EFF2FB5F1; Thu, 28 Jun 2007 23:21:39 +0200 (CEST)\n" +
			"Received: from localhost by ox.netline-is.de\n" +
			"	with SpamAssassin (2.64 2004-01-11);\n" +
			"	Thu, 28 Jun 2007 23:21:39 +0200\n" +
			"From: \"Trenton Gonzalez\" <tanyascott.com@quecall.com>\n" +
			"To: <thorben@open-xchange.org>\n" +
			"Subject: *****SPAM***** Why be an average guy any longer\n" +
			"Date: Thu, 28 Jun 2007 23:19:52 +0200\n" +
			"Message-Id: <000a01c7b9c9$5a6ce700$0100007f@uwisy>\n" +
			"X-Spam-Flag: YES\n" +
			"X-Spam-Checker-Version: SpamAssassin 2.64 (2004-01-11) on ox.netline-is.de\n" +
			"X-Spam-Level: ********\n" +
			"X-Spam-Status: Yes, hits=8.9 required=5.0 tests=BAYES_99,HTML_30_40,\n" +
			"	HTML_IMAGE_ONLY_10,HTML_MESSAGE,MIME_QP_DEFICIENT autolearn=no \n" +
			"	version=2.64\n" +
			"MIME-Version: 1.0\n" +
			"Content-Type: multipart/mixed; boundary=\"----------=_46842663.60434D7C\"\n" +
			"\n" +
			"This is a multi-part message in MIME format.\n" +
			"\n" +
			"------------=_46842663.60434D7C\n" +
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
			"Content preview:  ------=_NextPart_001_0013_01C7B9C9.5A6CE700\n" +
			"  Content-Type: text/plain; charset=\"us-ascii\" Content-Transfer-Encoding:\n" +
			"  quoted-printable See attach http://www.deilapo.com/ [...] \n" +
			"\n" +
			"Content analysis details:   (8.9 points, 5.0 required)\n" +
			"\n" +
			" pts rule name              description\n" +
			"---- ---------------------- --------------------------------------------------\n" +
			" 0.9 HTML_30_40             BODY: Message is 30% to 40% HTML\n" +
			" 0.1 HTML_MESSAGE           BODY: HTML included in message\n" +
			" 0.4 HTML_IMAGE_ONLY_10     BODY: HTML: images with 800-1000 bytes of words\n" +
			" 5.4 BAYES_99               BODY: Bayesian spam probability is 99 to 100%\n" +
			"                            [score: 1.0000]\n" +
			" 2.1 MIME_QP_DEFICIENT      RAW: Deficient quoted-printable encoding in body\n" +
			"\n" +
			"The original message was not completely plain text, and may be unsafe to\n" +
			"open with some email clients; in particular, it may contain a virus,\n" +
			"or confirm that your address can receive spam.  If you wish to view\n" +
			"it, it may be safer to save it to a file and open it with an editor.\n" +
			"\n" +
			"\n" +
			"------------=_46842663.60434D7C\n" +
			"Content-Type: message/rfc822; x-spam-type=original\n" +
			"Content-Description: original message before SpamAssassin\n" +
			"Content-Disposition: attachment\n" +
			"Content-Transfer-Encoding: 8bit\n" +
			"\n" +
			"Received: from netline.de (comfire.netline.de [192.168.32.1])\n" +
			"	by ox.netline-is.de (Postfix) with ESMTP id A3A242FB5D4\n" +
			"	for <thorben@open-xchange.com>; Thu, 28 Jun 2007 23:21:39 +0200 (CEST)\n" +
			"Received: from [10.20.30.11] (helo=www.open-xchange.org ident=mail)\n" +
			"	by netline.de with esmtp (Exim)\n" +
			"	id 1I412Y-0008VX-00\n" +
			"	for thorben@open-xchange.com; Thu, 28 Jun 2007 22:56:34 +0200\n" +
			"Received: from mail.netline-is.de ([10.20.30.2] helo=netline.de)\n" +
			"	by www.open-xchange.org with esmtp (Exim 3.36 #1 (Debian))\n" +
			"	id 1I41Po-0005FL-00\n" +
			"	for <thorben@open-xchange.org>; Thu, 28 Jun 2007 23:20:36 +0200\n" +
			"Received: from [83.5.5.177] (helo=jvjrwdn)\n" +
			"	by netline.de with smtp (Exim)\n" +
			"	id 1I412W-0008VM-00\n" +
			"	for thorben@open-xchange.org; Thu, 28 Jun 2007 22:56:32 +0200\n" +
			"Message-ID: <000a01c7b9c9$5a6ce700$0100007f@uwisy>\n" +
			"Date: Thu, 28 Jun 2007 23:19:52 +0200\n" +
			"From: \"Trenton Gonzalez\" <tanyascott.com@quecall.com>\n" +
			"To: <thorben@open-xchange.org>\n" +
			"Subject: Why be an average guy any longer\n" +
			"MIME-Version: 1.0\n" +
			"Content-Type: multipart/related;\n" +
			"	boundary=\"----=_NextPart_000_0039_01C7B9C9.5A6CE700\"\n" +
			"X-Priority: 3\n" +
			"X-MSMail-Priority: Normal\n" +
			"X-Mailer: Microsoft Outlook Express 6.00.2600.0000\n" +
			"X-MimeOLE: Produced By Microsoft MimeOLE V6.00.2600.0000\n" +
			"\n" +
			"This is a multi-part message in MIME format.\n" +
			"\n" +
			"------=_NextPart_000_0039_01C7B9C9.5A6CE700\n" +
			"Content-Type: multipart/alternative;\n" +
			"	boundary=\"----=_NextPart_001_0013_01C7B9C9.5A6CE700\"\n" +
			"\n" +
			"\n" +
			"------=_NextPart_001_0013_01C7B9C9.5A6CE700\n" +
			"Content-Type: text/plain;\n" +
			"	charset=\"us-ascii\"\n" +
			"Content-Transfer-Encoding: quoted-printable\n" +
			"\n" +
			"See attach\n" +
			"http://www.deilapo.com/\n" +
			"\n" +
			"-----\n" +
			"The soldiers reflected the val\n" +
			"Madelyne had little knowledge \n" +
			"She turned her attention to th\n" +
			"The road to the drawbridge cur\n" +
			" \n" +
			"  \n" +
			"\n" +
			"------=_NextPart_001_0013_01C7B9C9.5A6CE700\n" +
			"Content-Type: text/html;\n" +
			"    charset=\"us-ascii\"\n" +
			"Content-Transfer-Encoding: quoted-printable\n" +
			"\n" +
			"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n" +
			"<HTML><HEAD><TITLE>Hi</TITLE>\n" +
			"<META http-equiv=3D\"Content-Type\" content=3D\"text/html; charset=3Dus-ascii\">\n" +
			"<STYLE> textarea { display:none; } </STYLE></HEAD>\n" +
			"\n" +
			"<BODY>\n" +
			"<DIV><A href=3D\"http://www.deilapo.com/\">\n" +
			"<IMG src=3D\"cid:img098.jpg@28274575.90935083\" border=3D0></A></DIV><br><br>\n" +
			"<textarea>I must go and have a few words\n" +
			"</textarea><textarea>I will give him my honesty, Ma\n" +
			"</textarea><textarea>Louddon looked slightly appeas\n" +
			"</textarea><textarea>The bitch needs reminding of h\n" +
			"</textarea><textarea>There is always the chance tha\n" +
			"\n" +
			"</textarea><textarea>Madelyne pretended humility. S\n" +
			"</textarea><textarea>My request? Louddon laughed, a\n" +
			"</textarea><textarea>Louddon turned and walked away\n" +
			"</textarea><textarea>We both know Duncan isnt my hu\n" +
			"</textarea><textarea>Madelyne, I know you think to \n" +
			"</textarea><textarea>Nay, Anthony, Madelyne interru\n" +
			"</textarea><textarea>You must protect yourself, Ant\n" +
			"</textarea><textarea>She patted Anthonys hand and t\n" +
			"</textarea><textarea>What decision? Anthony asked. \n" +
			"\n" +
			"</textarea><textarea>You actually doubt\u2026 Madelyne l\n" +
			"</textarea><textarea>Anthony couldnt fault Madelyne\n" +
			"</textarea> \n" +
			"</BODY></HTML>\n" +
			"\n" +
			"------=_NextPart_001_0013_01C7B9C9.5A6CE700--\n" +
			"\n" +
			"------=_NextPart_000_0039_01C7B9C9.5A6CE700\n" +
			"Content-Type: image/jpeg;\n" +
			"	name=\"img66.jpg\"\n" +
			"Content-Transfer-Encoding: base64\n" +
			"Content-ID: <img098.jpg@28274575.90935083>\n" +
			"\n" +
			"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\n" +
			"\n" +
			"------=_NextPart_000_0039_01C7B9C9.5A6CE700--\n" +
			"\n" +
			"\n" +
			"------------=_46842663.60434D7C--\n" +
			"\n";

	/**
	 *
	 */
	public MailConverterTest() {
		super();
	}

	/**
	 * @param name
	 */
	public MailConverterTest(final String name) {
		super(name);
	}

	public void testMIMEConverter() {
		try {
			getSession();

			final MailMessage mail = MimeMessageConverter.convertMessage(SRC.getBytes(com.openexchange.java.Charsets.US_ASCII));

			final MailPartHandler handler = new MailPartHandler("2");
			new MailMessageParser().parseMailMessage(mail, handler);
			final MailPart part = handler.getMailPart();

			assertTrue("Nested message not recognized", part.getContent() instanceof MailMessage);
		} catch (final Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private static final String SRC2 = "From lothar.freihoff@arcor.de  Thu Oct  2 12:49:22 2008\n" +
	"Return-Path: <lothar.freihoff@arcor.de>\n" +
	"X-Original-To: l.f@mailgh.com\n" +
	"Delivered-To: l.f@mailgh.com\n" +
	"Received: from mail-in-14.arcor-online.net (mail-in-14.arcor-online.net [151.189.21.54])\n" +
	" (using TLSv1 with cipher ADH-AES256-SHA (256/256 bits))\n" +
	" (No client certificate requested)\n" +
	" by hermes.mailgh.com (Postfix) with ESMTPS id 314A3124C0D\n" +
	" for <l.f@mailgh.com>; Thu,  2 Oct 2008 12:49:21 +0200 (CEST)\n" +
	"Received: from mail-in-01-z2.arcor-online.net (mail-in-01-z2.arcor-online.net [151.189.8.13])\n" +
	" by mail-in-14.arcor-online.net (Postfix) with ESMTP id 278C3187A8D\n" +
	" for <l.f@mailgh.com>; Thu,  2 Oct 2008 12:49:12 +0200 (CEST)\n" +
	"Received: from mail-in-04.arcor-online.net (mail-in-04.arcor-online.net [151.189.21.44])\n" +
	" by mail-in-01-z2.arcor-online.net (Postfix) with ESMTP id AD4D82C0309\n" +
	" for <l.f@mailgh.com>; Thu,  2 Oct 2008 12:49:11 +0200 (CEST)\n" +
	"Received: from webmail10.arcor-online.net (webmail10.arcor-online.net [151.189.8.93])\n" +
	" by mail-in-04.arcor-online.net (Postfix) with ESMTP id 6B4561F7042\n" +
	" for <l.f@mailgh.com>; Thu,  2 Oct 2008 12:49:11 +0200 (CEST)\n" +
	"Received: from [84.62.189.67] by webmail10.arcor-online.net (151.189.8.93) with HTTP (Arcor Webmail); Thu, 2 Oct 2008 12:49:09 +0200 (CEST)\n" +
	"Message-ID: <27546971.1222944551364.JavaMail.ngmail@webmail10.arcor-online.net>\n" +
	"Date: Thu, 2 Oct 2008 12:49:11 +0200 (CEST)\n" +
	"From: lothar.freihoff@arcor.de\n" +
	"To: l.f@mailgh.com\n" +
	"Subject: Aw: test\n" +
	"In-Reply-To: <1584118582.1.1222942365178.JavaMail.open-xchange@hermes>\n" +
	"MIME-Version: 1.0\n" +
	"Content-Type: multipart/mixed; \n" +
	" boundary=\"----=_Part_45472_32597969.1222944551363\"\n" +
	"References: <1584118582.1.1222942365178.JavaMail.open-xchange@hermes>\n" +
	"X-ngMessageSubType: MessageSubType_MAIL\n" +
	"X-WebmailclientIP: 84.62.189.67\n" +
	"\n" +
	"------=_Part_45472_32597969.1222944551363\n" +
	"Content-Type: multipart/alternative; \n" +
	" boundary=\"----=_Part_45471_6887715.1222944551363\"\n" +
	"\n" +
	"------=_Part_45471_6887715.1222944551363\n" +
	"Content-Type: text/plain; charset=ISO-8859-1\n" +
	"Content-Transfer-Encoding: 7bit\n" +
	"\n" +
	" \n" +
	"\n" +
	"\n" +
	"----- Original Nachricht ----\n" +
	"Von:     l f <l.f@mailgh.com>\n" +
	"An:      lothar.freihoff@arcor.de\n" +
	"Datum:   02.10.2008 12:12\n" +
	"Betreff: test\n" +
	"\n" +
	"> test\n" +
	"------=_Part_45471_6887715.1222944551363--\n" +
	"\n" +
	"------=_Part_45472_32597969.1222944551363\n" +
	"Content-Type: text/plain; charset=ISO-8859-1\n" +
	"Content-Transfer-Encoding: 7bit\n" +
	"Content-Disposition: attachment; filename=test.txt\n" +
	"\n" +
	"Foo bar foo bar foo bar\n" +
	"------=_Part_45472_32597969.1222944551363--\n" +
	"\n";

	public void testMIMEConverter2() {
		try {
			final MailMessage mail = MimeMessageConverter.convertMessage(SRC2.getBytes(US_ASCII));

			final int expectedCount = 2;
			assertEquals("Unexpected number of enclosed parts", expectedCount, mail.getEnclosedCount());

			for (int i = 0; i < expectedCount; i++) {
				final MailPart enclosedMailPart = mail.getEnclosedMailPart(i);

				if (i == 0) {
					assertTrue("Unexpected content-type: " + enclosedMailPart.getContentType().toString(),
							enclosedMailPart.getContentType().isMimeType("multipart/alternative"));
					final int expectedNestedCount = 1;
					assertEquals("Unexpected number of nested enclosed parts", expectedNestedCount, enclosedMailPart
							.getEnclosedCount());

					final MailPart mp = enclosedMailPart.getEnclosedMailPart(0);
					assertTrue("Unexpected content-type: " + mp.getContentType().toString(), mp.getContentType()
							.isMimeType("text/plain"));
				} else {
					assertTrue("Unexpected content-type: " + enclosedMailPart.getContentType().toString(),
							enclosedMailPart.getContentType().isMimeType("text/plain"));
					assertEquals("Non-multipart part contains nested parts", MailPart.NO_ENCLOSED_PARTS,
							enclosedMailPart.getEnclosedCount());
				}
			}

		} catch (final Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testBug37537() throws Exception {
        String uid = "123";
        String fullName = "INBOX/testBug37537";
        MailMessage mail = MimeMessageConverter.convertMessage(SRC.getBytes(US_ASCII), uid, fullName, '/', MailField.FIELDS_WO_BODY);
        assertNotNull(mail);
        assertEquals(uid, mail.getMailId());
        assertEquals(fullName, mail.getFolder());
    }
}
