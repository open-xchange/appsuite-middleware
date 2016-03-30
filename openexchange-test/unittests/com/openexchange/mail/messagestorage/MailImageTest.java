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

package com.openexchange.mail.messagestorage;

import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.JsonMessageHandler;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.sessiond.impl.SessionObject;

/**
 * {@link MailImageTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class MailImageTest extends AbstractMailTest {

	private final String MSG_WITH_INLINE_IMG = "Return-Path: <thorben@dev-prototyp.open-xchange.com>\n"
			+ "Received: from dev-prototyp.open-xchange.com ([unix socket])\n"
			+ "	 by dev-prototyp (Cyrus v2.2.13-Debian-2.2.13-10) with LMTPA;\n"
			+ "	 Mon, 07 Jan 2008 10:33:31 +0100\n" + "X-Sieve: CMU Sieve 2.2\n"
			+ "Received: from thorben (alcatraz.open-xchange.com [10.20.30.253])\n"
			+ "	by dev-prototyp.open-xchange.com (Postfix) with ESMTP id 259F47A5D0\n"
			+ "	for <thorben@dev-prototyp.open-xchange.com>; Mon,  7 Jan 2008 10:33:31 +0100 (CET)\n"
			+ "Date: Mon, 7 Jan 2008 14:52:42 +0000 (GMT)\n"
			+ "From: \"Betten, Thorben\" <thorben@dev-prototyp.open-xchange.com>\n"
			+ "To: thorben@dev-prototyp.open-xchange.com\n"
			+ "Message-ID: <538163707.01199717562193.JavaMail.thorben@thorben>\n" + "Subject: Mail mit Bild\n"
			+ "MIME-Version: 1.0\n" + "Content-Type: multipart/alternative; \n"
			+ "	boundary=\"----=_Part_0_295964671.1199717561565\"\n" + "X-Priority: 3\n"
			+ "X-Mailer: Open-Xchange Mailer v@replaceVersion@-@replaceBuildnumber@\n" + "\n"
			+ "------=_Part_0_295964671.1199717561565\n" + "MIME-Version: 1.0\n"
			+ "Content-Type: text/plain; charset=UTF-8\n" + "Content-Transfer-Encoding: 7bit\n" + "\n"
			+ "Hier ist das Bild:\n" + "\n" + "\n" + "\n" + "Hardcore!\n" + "\n"
			+ "------=_Part_0_295964671.1199717561565\n" + "Content-Type: multipart/related; \n"
			+ "	boundary=\"----=_Part_1_1273655764.1199717561619\"\n" + "\n"
			+ "------=_Part_1_1273655764.1199717561619\n" + "MIME-Version: 1.0\n"
			+ "Content-Type: text/html; charset=UTF-8\n" + "Content-Transfer-Encoding: 7bit\n" + "\n"
			+ "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n"
			+ "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" + "\n"
			+ "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" + "  <head>\n"
			+ "    <meta content=\"text/html; charset=UTF-8\" http-equiv=\"Content-Type\">\n"
			+ "    <meta name=\"generator\"\n"
			+ "    content=\"HTML Tidy for Java (vers. 26 Sep 2004), see www.w3.org\" />\n" + "\n"
			+ "    <title></title>\n" + "  </head>\n" + "\n" + "  <body>\n" + "    Hier ist das Bild:<br />\n"
			+ "    <br />\n" + "    <img src=\"cid:crim.jpeg@9f05cb4b35bc2d0e\" /><br />\n" + "    <br />\n"
			+ "    Hardcore!<br />\n" + "    <br />\n" + "  </body>\n" + "\n" + "</html>\n" + "\n"
			+ "------=_Part_1_1273655764.1199717561619\n" + "Content-Type: image/jpeg\n"
			+ "Content-Transfer-Encoding: base64\n" + "Content-Disposition: inline; filename=crim.jpeg\n"
			+ "Content-ID: <crim.jpeg@9f05cb4b35bc2d0e>\n" + "\n"
			+ "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\n"
			+ "/wBaKKqfpsUUUVlX/9k=\n" + "------=_Part_1_1273655764.1199717561619--\n" + "\n"
			+ "------=_Part_0_295964671.1199717561565--\n" + "\n";

	/**
	 *
	 */
	public MailImageTest() {
		super();
	}

	/**
	 * @param name
	 */
	public MailImageTest(final String name) {
		super(name);
	}

	public void testRFC2231() {
		try {
			final SessionObject session = getSession();

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();

			final String uid = mailAccess.getMessageStorage()
					.appendMessages(
							"INBOX",
							new MailMessage[] { MimeMessageConverter.convertMessage(MSG_WITH_INLINE_IMG
									.getBytes(com.openexchange.java.Charsets.US_ASCII)) })[0];
			try {
				final MailMessage mail = mailAccess.getMessageStorage().getMessage("INBOX", uid, true);

				final JsonMessageHandler messageHandler = new JsonMessageHandler(MailAccount.DEFAULT_ID, null, mail, DisplayMode.DISPLAY, false,
						session, UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(),
								session.getContextId()), false, -1);
				new MailMessageParser().parseMailMessage(mail, messageHandler);
				final JSONObject jObject = messageHandler.getJSONObject();

				final Set<String> cids = new HashSet<String>();
				if (jObject.has(MailJSONField.ATTACHMENTS.getKey())) {
					final JSONArray jArray = jObject.getJSONArray(MailJSONField.ATTACHMENTS.getKey());
					final int len = jArray.length();
					assertTrue("Missing attachments", len > 0);
					for (int i = 0; i < len; i++) {
						final JSONObject attachObj = jArray.getJSONObject(i);
						if (attachObj.has(MailJSONField.CID.getKey())) {
							cids.add(attachObj.getString(MailJSONField.CID.getKey()));
						}
					}
				} else {
					fail("Missing attachments");
				}

				if (!cids.isEmpty()) {
                    for (final String cid : cids) {
                        final MailPart imgPart = mailAccess.getMessageStorage().getImageAttachment("INBOX", uid, cid);
                        assertFalse("No image part found for Content-Id: " + cid, null == imgPart);
                    }
                }

			} finally {
				mailAccess.getMessageStorage().deleteMessages("INBOX", new String[] { uid }, true);
				/*
				 * close
				 */
				mailAccess.close(false);
			}

		} catch (final Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
