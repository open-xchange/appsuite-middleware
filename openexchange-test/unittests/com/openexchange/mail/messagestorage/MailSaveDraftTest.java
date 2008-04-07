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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import javax.mail.internet.InternetAddress;

import org.json.JSONArray;
import org.json.JSONObject;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.JSONMessageHandler;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.smtp.dataobjects.SMTPMailMessage;

/**
 * {@link MailSaveDraftTest}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MailSaveDraftTest extends AbstractMailTest {

	private final String MSG_DRAFT = "Return-Path: <thorben@dev-prototyp.open-xchange.com>\n"
			+ "Received: from dev-prototyp.open-xchange.com ([unix socket])\n"
			+ "	 by dev-prototyp (Cyrus v2.2.13-Debian-2.2.13-10) with LMTPA;\n"
			+ "	 Wed, 19 Mar 2008 09:52:23 +0100\n" + "X-Sieve: CMU Sieve 2.2\n"
			+ "Received: from thorben (unknown [192.168.32.167])\n"
			+ "	by dev-prototyp.open-xchange.com (Postfix) with ESMTP id 313D77AF18\n"
			+ "	for <thorben@dev-prototyp.open-xchange.com>; Wed, 19 Mar 2008 09:52:23 +0100 (CET)\n"
			+ "Date: Wed, 19 Mar 2008 12:41:10 +0000 (GMT)\n"
			+ "From: \"Betten, Thorben\" <thorben@dev-prototyp.open-xchange.com>\n" + "Sender: thorben@1337\n"
			+ "To: thorben@dev-prototyp.open-xchange.com\n"
			+ "Message-ID: <974704333.01205930471012.JavaMail.thorben@thorben>\n" + "Subject: sdfsdf\n"
			+ "MIME-Version: 1.0\n" + "Content-Type: multipart/alternative; \n"
			+ "	boundary=\"----=_Part_0_112430522.1205930470650\"\n" + "X-Priority: 3\n"
			+ "X-Mailer: Open-Xchange Mailer v6.5.0-0\n" + "\n" + "------=_Part_0_112430522.1205930470650\n"
			+ "MIME-Version: 1.0\n" + "Content-Type: text/plain; charset=UTF-8\n" + "Content-Transfer-Encoding: 7bit\n"
			+ "\n" + "dsfsdfsdfsdf\n" + "------=_Part_0_112430522.1205930470650\n" + "MIME-Version: 1.0\n"
			+ "Content-Type: text/html; charset=UTF-8\n" + "Content-Transfer-Encoding: 7bit\n" + "\n"
			+ "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n"
			+ "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" + "\n"
			+ "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" + "  <head>\n"
			+ "    <meta content=\"text/html; charset=UTF-8\" http-equiv=\"Content-Type\">\n"
			+ "    <meta name=\"generator\"\n"
			+ "    content=\"HTML Tidy for Java (vers. 26 Sep 2004), see www.w3.org\" />\n" + "\n"
			+ "    <title></title>\n" + "  </head>\n" + "\n" + "  <body>\n" + "    dsfsdfsdfsdf\n" + "  </body>\n"
			+ "</html>\n" + "\n" + "------=_Part_0_112430522.1205930470650--\n" + "\n";

	/**
	 * 
	 */
	public MailSaveDraftTest() {
		super();
	}

	/**
	 * @param name
	 */
	public MailSaveDraftTest(final String name) {
		super(name);
	}

	public void testMailDraft() {
		try {
			final SessionObject session = SessionObjectWrapper.createSessionObject(getUser(),
					new ContextImpl(getCid()), "mail-test-session");
			session.setPassword(getPassword());
			final Context dummyContext = new ContextImpl(session.getContextId());

			ComposedMailMessage draftMail = new SMTPMailMessage("The first line", session, dummyContext);
			draftMail.addFrom(new InternetAddress("someone@somewhere.com", true));
			draftMail.addTo(new InternetAddress("someone.else@another.com", true));

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();
			long prevUid = -1;
			long uid = -1;
			try {
				MailMessage mail = mailAccess.getMessageStorage().saveDraft(
						mailAccess.getFolderStorage().getDraftsFolder(), draftMail);
				uid = mail.getMailId();
				prevUid = uid;
				System.out.println("First draft's UID: " + uid);
				/*
				 * Check content
				 */
				String content = mail.getContent().toString();
				assertTrue("Content mismatch", "The first line".equals(content));
				/*
				 * Edit draft
				 */
				draftMail = new SMTPMailMessage("The first line<br>And the second line", session, dummyContext);
				draftMail.addFrom(new InternetAddress("someone@somewhere.com", true));
				draftMail.addTo(new InternetAddress("someone.else@another.com", true));
				draftMail.addTo(new InternetAddress("Jane Doe <another.one@anywhere.org>", true));
				draftMail.setReferencedMail(mail);
				draftMail.setMsgref(new MailPath(mail.getFolder(), mail.getMailId()).toString());

				mail = mailAccess.getMessageStorage().saveDraft(mailAccess.getFolderStorage().getDraftsFolder(),
						draftMail);
				uid = mail.getMailId();
				System.out.println("Edit-draft's UID: " + uid);
				/*
				 * Check existence of former draft version
				 */
				Exception exc = null;
				try {
					mailAccess.getMessageStorage().getMessage(mailAccess.getFolderStorage().getDraftsFolder(), prevUid,
							false);
				} catch (final MailException e) {
					prevUid = -1;
					exc = e;
				}
				assertTrue("Former draft version still available", exc != null);
				/*
				 * Check content again
				 */
				final String expected = "The first line\nAnd the second line";
				content = mail.getContent().toString();
				if (!expected.equals(content.replaceAll("\r\n", "\n"))) {
					final StringBuilder sb = new StringBuilder(1024);
					sb.append("Expected value:\n");
					char[] chars = expected.toCharArray();
					sb.append((int) chars[0]);
					for (int i = 1; i < chars.length; i++) {
						sb.append(' ').append((int) chars[i]);
					}
					sb.append("\nReturned value:\n");
					chars = content.toCharArray();
					sb.append((int) chars[0]);
					for (int i = 1; i < chars.length; i++) {
						sb.append(' ').append((int) chars[i]);
					}
					sb.append("\nIn words:");
					sb.append('"').append(expected).append('"').append('\n');
					sb.append('"').append(content).append('"');
					assertTrue(sb.toString(), false);
				}
			} finally {
				if (prevUid != -1) {
					final boolean success = mailAccess.getMessageStorage().deleteMessages("INBOX",
							new long[] { prevUid }, true);
					if (success) {
						System.out.println("Successfully deleted");
					} else {
						System.out.println("Delete failed");
					}
				}
				if (uid != -1) {
					final boolean success = mailAccess.getMessageStorage().deleteMessages("INBOX", new long[] { uid },
							true);
					if (success) {
						System.out.println("Successfully deleted");
					} else {
						System.out.println("Delete failed");
					}
				}
				/*
				 * close
				 */
				mailAccess.close(false);
			}

		} catch (final Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

}
