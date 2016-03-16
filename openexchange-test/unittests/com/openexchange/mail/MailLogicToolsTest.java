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

import com.openexchange.exception.OXException;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.DumperMessageHandler;
import com.openexchange.sessiond.impl.SessionObject;

/**
 * {@link MailLogicToolsTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class MailLogicToolsTest extends AbstractMailTest {

	/**
	 * Default
	 */
	public MailLogicToolsTest() {
		super();
	}

	private static final String TEST_MAIL = ""
			+ "Return-Path: <tobias.prinz@open-xchange.com>\r\n"
			+ "Received: from ox.netline-is.de ([unix socket]) by ox (Cyrus v2.2.3) with LMTP; Wed, 15 Aug 2007 14:03:12 +0200\r\n"
			+ "X-Sieve: CMU Sieve 2.2\r\n"
			+ "Received: by ox.netline-is.de (Postfix, from userid 65534) id 5073F318173; Wed, 15 Aug 2007 14:03:12 +0200 (CEST)\r\n"
			+ "Received: from netline.de (comfire.netline.de [192.168.32.1]) by ox.netline-is.de (Postfix) with ESMTP id 494D531816D for <thorben@open-xchange.com>; Wed, 15 Aug 2007 14:03:11 +0200 (CEST)\r\n"
			+ "Received: from [10.20.30.11] (helo=www.open-xchange.org ident=mail) by netline.de with esmtp (Exim) id 1ILHV0-00063Y-00 for thorben@open-xchange.com; Wed, 15 Aug 2007 13:57:18 +0200\r\n"
			+ "Received: from mail.open-xchange.com ([10.20.30.22] helo=ox.open-xchange.com) by www.open-xchange.org with esmtp (Exim 3.36 #1 (Debian)) id 1ILHZi-0004x6-00 for <thorben@open-xchange.org>; Wed, 15 Aug 2007 14:02:10 +0200\r\n"
			+ "Received: by ox.open-xchange.com (Postfix, from userid 76) id 469A532CDE6; Wed, 15 Aug 2007 14:02:05 +0200 (CEST)\r\n"
			+ "Received: from ox.open-xchange.com ([unix socket]) by ox.open-xchange.com (Cyrus v2.2.12-Invoca-RPM-2.2.12-8.1.RHEL4) with LMTPA; Wed, 15 Aug 2007 14:02:05 +0200\r\n"
			+ "X-Sieve: CMU Sieve 2.2\r\n"
			+ "Received: by ox.open-xchange.com (Postfix, from userid 99) id F0DBC32CDE4; Wed, 15 Aug 2007 14:02:04 +0200 (CEST)\r\n"
			+ "Received: from oxee (unknown [192.168.32.9]) by ox.open-xchange.com (Postfix) with ESMTP id E5C4332C84D; Wed, 15 Aug 2007 14:02:03 +0200 (CEST)\r\n"
			+ "Date: Wed, 15 Aug 2007 14:01:20 +0200 (CEST)r\n"
			+ "From: \"Prinz, Tobias\" <tobias.prinz@open-xchange.com>\r\n"
			+ "Reply-To: \"Prinz, Tobias\" <tobias.prinz@open-xchange.com>\r\n"
			+ "To: Thorben <thorben.betten@open-xchange.com>, Betten@ox.open-xchange.com\r\n"
			+ "Message-ID: <9692463.1901187179280437.JavaMail.open-xchange@oxee>\r\n"
			+ "Subject: Bug 8844: Kannst Du den Patch mal probieren?\r\n"
			+ "MIME-Version: 1.0\r\n"
			+ "Content-Type: multipart/mixed; boundary=\"----=_Part_73_20070782.1187179280395\"\r\n"
			+ "X-Priority: 3\r\n"
			+ "X-Mailer: Open-Xchange Mailer v6.3.0-6270\r\n"
			+ "X-Spam-Checker-Version: SpamAssassin 2.64 (2004-01-11) on ox.netline-is.de\r\n"
			+ "X-Spam-Level: \r\n"
			+ "X-Spam-Status: No, hits=-4.8 required=5.0 tests=AWL,BAYES_00 autolearn=ham version=2.64\r\n"
			+ "\r\n"
			+ "------=_Part_73_20070782.1187179280395\r\n"
			+ "MIME-Version: 1.0\r\n"
			+ "Content-Type: text/plain; charset=UTF-8\r\n"
			+ "Content-Transfer-Encoding: 7bit\r\n"
			+ "\r\n"
			+ "Bitte vorher CVS auschecken oder wenigstens die com.openexchange.tools.versit.old.OldNPropertyDefinition aktualisieren.\r\n"
			+ "\r\n"
			+ "-- \r\n"
			+ "Tobias Prinz, Developer\r\n"
			+ "Phone +49 2761 - 83 85 21\r\n"
			+ "Fax +49 2761 - 83 85 30\r\n"
			+ "Mobil + 49 160 - 91 40 80 95\r\n"
			+ "Open-Xchange GmbH, Martinstr. 41, D-57462 Olpe\r\n"
			+ "AG N&uuml;rnberg, HRB 22121\r\n"
			+ "Gesch&auml;ftsf&uuml;hrer: Frank Hoberg, Martin Kauss\r\n"
			+ "------=_Part_73_20070782.1187179280395\r\n"
			+ "Content-Type: text/plain\r\n"
			+ "Content-Transfer-Encoding: 7bit\r\n"
			+ "Content-Disposition: attachment; filename=patch-8844.txt\r\n"
			+ "\r\n"
			+ "### Eclipse Workspace Patch 1.0\r\n"
			+ "#P osr-openexchange-HEAD\r\n"
			+ "Index: src/com/openexchange/groupware/container/mail/filler/MessageFiller.java\r\n"
			+ "===================================================================\r\n"
			+ "RCS file: /var/lib/cvs/open-xchange/src/com/openexchange/groupware/container/mail/filler/MessageFiller.java,v\r\n"
			+ "retrieving revision 1.31\r\n"
			+ "diff -u -r1.31 MessageFiller.java\r\n"
			+ "--- src/com/openexchange/groupware/container/mail/filler/MessageFiller.java	14 Aug 2007 14:56:14 -0000	1.31\r\n"
			+ "+++ src/com/openexchange/groupware/container/mail/filler/MessageFiller.java	15 Aug 2007 11:59:54 -0000\r\n"
			+ "@@ -140,7 +140,7 @@\r\n" + " \r\n"
			+ "private static final String MIME_MESSAGE_RFC822 = \"message/rfc822\";\r\n" + " \r\n"
			+ "-	private static final String MIME_TEXT_VCARD = \"text/vcard\";\r\n"
			+ "+	private static final String MIME_TEXT_VCARD = \"text/x-vcard\";\r\n" + " \r\n"
			+ "private static final String MIME_TEXT_PLAIN = \"text/plain\";\r\n" + " \r\n" + "@@ -898,7 +898,7 @@\r\n"
			+ "} catch (final Exception e) {\r\n"
			+ "throw new OXMailException(MailCode.INTERNAL_ERROR, e, e.getMessage());\r\n" + "}\r\n"
			+ "-			final VersitObject versitObj = converter.convertContact(contactObj, \"3.0\");\r\n"
			+ "+			final VersitObject versitObj = converter.convertContact(contactObj, \"2.1\");\r\n"
			+ "final ByteArrayOutputStream os = new ByteArrayOutputStream();\r\n"
			+ "final VersitDefinition def = Versit.getDefinition(MIME_TEXT_VCARD);\r\n"
			+ "final VersitDefinition.Writer w = def.getWriter(os, IMAPProperties.getDefaultMimeCharset());\r\n"
			+ "\r\n" + "------=_Part_73_20070782.1187179280395\r\n" + "MIME-Version: 1.0\r\n"
			+ "Content-Type: text/vcard; charset=us-ascii; name=\"Prinz,Tobias.vcf\"\r\n"
			+ "Content-Transfer-Encoding: 7bit\r\n"
			+ "Content-Disposition: attachment; filename=\"Prinz,Tobias.vcf\"\r\n" + "\r\n" + "BEGIN:VCARD\r\n"
			+ "VERSION:3.0\r\n" + "PRODID:OPEN-XCHANGE\r\n" + "FN:Prinz\\, Tobias\r\n" + "N:Prinz;Tobias;;;\r\n"
			+ "NICKNAME:Tierlieb\r\n" + "BDAY:19810501\r\n" + "ADR;TYPE=work:;;;Meinerzhagen;NRW;58540;DE\r\n"
			+ "TEL;TYPE=home,voice:+49 2358 7192\r\n" + "EMAIL:tobias.prinz@open-xchange.com\r\n"
			+ "ORG:- deactivated -\r\n" + "REV:20061204T160750.018Z\r\n" + "URL:www.tobias-prinz.de\r\n"
			+ "UID:80@oxee.netline.de\r\n" + "END:VCARD\r\n" + "\r\n" + "------=_Part_73_20070782.1187179280395--";

	/**
	 * @param name
	 *            The test case name
	 */
	public MailLogicToolsTest(final String name) {
		super(name);
	}

	private static final MailField[] COMMON_LIST_FIELDS = { MailField.ID, MailField.FOLDER_ID, MailField.FROM,
			MailField.TO, MailField.RECEIVED_DATE, MailField.SENT_DATE, MailField.SUBJECT, MailField.CONTENT_TYPE,
			MailField.FLAGS, MailField.PRIORITY, MailField.COLOR_LABEL };

	public void testForward() {
		try {
			final SessionObject session = getSession();
			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect(/* mailConfig */);
			try {
				// ByteArrayInputStream in = new
				// ByteArrayInputStream(TEST_MAIL.getBytes(com.openexchange.java.Charsets.US_ASCII));

				final MailMessage[] mails = mailAccess.getMessageStorage().searchMessages("INBOX", null,
						MailSortField.RECEIVED_DATE, OrderDirection.DESC, null, COMMON_LIST_FIELDS);
				int count = 0;
				for (int i = 0; i < mails.length; i++) {
					if (mails[i].getContentType().isMimeType("multipart/mixed")) {
						final DumperMessageHandler msgHandler1 = new DumperMessageHandler(false);
						new MailMessageParser().parseMailMessage(mailAccess.getMessageStorage().getMessage("INBOX",
								mails[i].getMailId(), true), msgHandler1);

						final MailMessage[] ms = new MailMessage[] { mailAccess.getMessageStorage().getMessage("INBOX",
								mails[i].getMailId(), false) };
						final MailMessage forwardMail = mailAccess.getLogicTools().getFowardMessage(ms, false);
						final DumperMessageHandler msgHandler = new DumperMessageHandler(false);
						new MailMessageParser().parseMailMessage(forwardMail, msgHandler);
						if (++count == 50) {
							break;
						}
					}
				}

			} finally {
				mailAccess.close(true);
			}

		} catch (final OXException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testReply() {
		try {
			final SessionObject session = getSession();
			final MailAccess<?, ?> mailConnection = MailAccess.getInstance(session);
			mailConnection.connect(/* mailConfig */);
			try {
				// ByteArrayInputStream in = new
				// ByteArrayInputStream(TEST_MAIL.getBytes(com.openexchange.java.Charsets.US_ASCII));

				final MailMessage[] mails = mailConnection.getMessageStorage().searchMessages("INBOX", null,
						MailSortField.RECEIVED_DATE, OrderDirection.DESC, null, COMMON_LIST_FIELDS);
				int count = 0;
				for (int i = 0; i < mails.length; i++) {
					if (!"11611".equals(mails[i].getMailId())) {
						continue;
					}
					final DumperMessageHandler msgHandler1 = new DumperMessageHandler(true);
					new MailMessageParser().parseMailMessage(mailConnection.getMessageStorage().getMessage(
							"default/INBOX", mails[i].getMailId(), true), msgHandler1);

					final MailMessage originalMail = mailConnection.getMessageStorage().getMessage("INBOX",
							mails[i].getMailId(), false);
					final MailMessage replyMail = mailConnection.getLogicTools().getReplyMessage(originalMail, true);
					final DumperMessageHandler msgHandler = new DumperMessageHandler(true);
					new MailMessageParser().parseMailMessage(replyMail, msgHandler);
					if (++count == 50) {
						break;
					}
				}

			} finally {
				mailConnection.close(true);
			}

		} catch (final OXException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
