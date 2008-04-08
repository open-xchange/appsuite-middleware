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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.mail.search.BodyTerm;
import com.openexchange.mail.search.ComparisonType;
import com.openexchange.mail.search.FlagTerm;
import com.openexchange.mail.search.FromTerm;
import com.openexchange.mail.search.HeaderTerm;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.search.SentDateTerm;
import com.openexchange.mail.search.SizeTerm;
import com.openexchange.mail.search.SubjectTerm;
import com.openexchange.mail.search.ToTerm;
import com.openexchange.mail.utils.DateUtils;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;

/**
 * {@link MailSearchTest}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MailSearchTest extends AbstractMailTest {

	/**
	 * 
	 */
	public MailSearchTest() {
		super();
	}

	/**
	 * @param name
	 */
	public MailSearchTest(final String name) {
		super(name);
	}

	private static final MailField[] FIELDS_ID = { MailField.ID };

	private static final MailField[] FIELDS_ID_AND_HEADER = { MailField.ID, MailField.HEADERS };

	private static final MailField[] FIELDS_MORE = { MailField.ID, MailField.CONTENT_TYPE, MailField.FLAGS };

	private static final MailField[] FIELDS_EVEN_MORE = { MailField.ID, MailField.CONTENT_TYPE, MailField.FLAGS,
			MailField.FROM, MailField.TO, MailField.DISPOSITION_NOTIFICATION_TO, MailField.COLOR_LABEL,
			MailField.HEADERS, MailField.SUBJECT, MailField.THREAD_LEVEL, MailField.SIZE, MailField.PRIORITY };

	private static final MailField[] FIELDS_FULL = { MailField.FULL };

	public void testMailSearch() {
		try {
			final SessionObject session = SessionObjectWrapper.createSessionObject(getUser(),
					new ContextImpl(getCid()), "mail-test-session");
			session.setPassword(getPassword());
			final MailMessage[] mails = getMessages(getTestMailDir(), -1);

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();
			final long[] uids = mailAccess.getMessageStorage().appendMessages("INBOX", mails);
			try {

				SearchTerm<?> term = new HeaderTerm(MessageHeaders.HDR_CONTENT_TYPE, "text/plain; charset=us-ascii");
				long start = System.currentTimeMillis();
				MailMessage[] fetchedMails = mailAccess.getMessageStorage().searchMessages("INBOX", IndexRange.NULL,
						null, null, term, FIELDS_ID);
				System.out.println("Header search took: " + (System.currentTimeMillis() - start) + "msec");
				for (int i = 0; i < fetchedMails.length; i++) {
					assertFalse("Mail ID is -1", fetchedMails[i].getMailId() == -1);
				}

				term = new FlagTerm(MailMessage.FLAG_SEEN, false);
				start = System.currentTimeMillis();
				fetchedMails = mailAccess.getMessageStorage().searchMessages("INBOX", IndexRange.NULL, null, null,
						term, FIELDS_MORE);
				System.out.println("Unseen search took: " + (System.currentTimeMillis() - start) + "msec");
				for (int i = 0; i < fetchedMails.length; i++) {
					assertFalse("Missing mail ID", fetchedMails[i].getMailId() == -1);
					assertTrue("Missing content type", fetchedMails[i].containsContentType());
					assertTrue("Missing flags", fetchedMails[i].containsFlags());
				}

				/*
				 * All >= 1KB (1024bytes)
				 */
				term = new SizeTerm(ComparisonType.GREATER_THAN, 1023);
				start = System.currentTimeMillis();
				fetchedMails = mailAccess.getMessageStorage().searchMessages("INBOX", IndexRange.NULL, null, null,
						term, FIELDS_EVEN_MORE);
				System.out.println("Size search took: " + (System.currentTimeMillis() - start) + "msec");
				for (int i = 0; i < fetchedMails.length; i++) {
					assertFalse("Missing mail ID", fetchedMails[i].getMailId() == -1);
					assertTrue("Missing content type", fetchedMails[i].containsContentType());
					assertTrue("Missing flags", fetchedMails[i].containsFlags());
					assertTrue("Missing From", fetchedMails[i].containsFrom());
					assertTrue("Missing To", fetchedMails[i].containsTo());
					assertTrue("Missing Disposition-Notification-To", fetchedMails[i].containsDispositionNotification());
					assertTrue("Missing color label", fetchedMails[i].containsColorLabel());
					assertTrue("Missing headers", fetchedMails[i].containsHeaders());
					assertTrue("Missing subject", fetchedMails[i].containsSubject());
					assertTrue("Missing thread level", fetchedMails[i].containsThreadLevel());
					assertTrue("Missing size", fetchedMails[i].containsSize());
					assertTrue("Missing priority", fetchedMails[i].containsPriority());
				}

				final Map<Long, String> map = new HashMap<Long, String>(fetchedMails.length);
				for (int i = 0; i < fetchedMails.length && i < 100; i++) {
					final String messageId = fetchedMails[i].getHeader(MessageHeaders.HDR_MESSAGE_ID);
					if (null != messageId && messageId.length() > 0 && !"null".equalsIgnoreCase(messageId)) {
						map.put(Long.valueOf(fetchedMails[i].getMailId()), messageId);
					}
				}

				final int size = map.size();
				final Iterator<Map.Entry<Long, String>> iter = map.entrySet().iterator();
				for (int i = 0; i < size; i++) {
					final Map.Entry<Long, String> e = iter.next();
					term = new HeaderTerm(MessageHeaders.HDR_MESSAGE_ID, e.getValue());
					start = System.currentTimeMillis();
					final MailMessage[] searchedMails = mailAccess.getMessageStorage().searchMessages("INBOX",
							IndexRange.NULL, null, null, term, FIELDS_ID_AND_HEADER);
					assertTrue("Search failed: No result", null != searchedMails);
					assertTrue("Search failed: Non-matching result size", searchedMails.length >= 1);
					boolean found = false;
					for (int j = 0; j < searchedMails.length && !found; j++) {
						final String messageId = searchedMails[j].getHeader(MessageHeaders.HDR_MESSAGE_ID);
						assertTrue("Missing Message-Id", null != messageId);
						assertTrue("Non-matching Message-Id", messageId.equals(e.getValue()));
						found = e.getKey().longValue() == searchedMails[j].getMailId();
					}
					assertTrue("Non-matching mail ID", found);
				}

			} finally {

				final boolean success = mailAccess.getMessageStorage().deleteMessages("INBOX", uids, true);
				if (success) {
					System.out.println("Successfully deleted");
				} else {
					System.out.println("Delete failed");
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

	private static final String RFC822_SRC = "Return-Path: <dream-team-bounces@open-xchange.com>\n"
			+ "Received: from ox.netline-is.de ([unix socket])\n"
			+ "	by ox (Cyrus v2.2.3) with LMTP; Tue, 08 Apr 2008 10:33:24 +0200\n" + "X-Sieve: CMU Sieve 2.2\n"
			+ "Received: by ox.netline-is.de (Postfix, from userid 65534)\n"
			+ "	id B46993DCB9E; Tue,  8 Apr 2008 10:33:23 +0200 (CEST)\n"
			+ "Received: from netline.de (comfire.netline.de [192.168.32.1])\n"
			+ "	by ox.netline-is.de (Postfix) with ESMTP id 70E1A3DCB9A\n"
			+ "	for <thorben@netline-is.de>; Tue,  8 Apr 2008 10:33:22 +0200 (CEST)\n"
			+ "Received: from [10.20.30.11] (helo=www.open-xchange.org ident=mail)\n"
			+ "	by netline.de with esmtp (Exim)\n" + "	id 1Jj95z-0003bM-00\n"
			+ "	for thorben@netline-is.de; Tue, 08 Apr 2008 10:22:23 +0200\n"
			+ "Received: from mail.open-xchange.com ([10.20.30.22] helo=ox.open-xchange.com)\n"
			+ "	by www.open-xchange.org with esmtp (Exim 3.36 #1 (Debian))\n" + "	id 1Jj9Fo-000709-00\n"
			+ "	for <thorben@open-xchange.org>; Tue, 08 Apr 2008 10:32:32 +0200\n"
			+ "Received: by ox.open-xchange.com (Postfix, from userid 76)\n"
			+ "	id D038132C89D; Tue,  8 Apr 2008 10:32:31 +0200 (CEST)\n"
			+ "Received: from ox.open-xchange.com ([unix socket])\n"
			+ "	 by ox.open-xchange.com (Cyrus v2.2.12-Invoca-RPM-2.2.12-8.1.RHEL4) with LMTPA;\n"
			+ "	 Tue, 08 Apr 2008 10:32:29 +0200\n" + "X-Sieve: CMU Sieve 2.2\n"
			+ "Received: by ox.open-xchange.com (Postfix, from userid 99)\n"
			+ "	id B862832C87B; Tue,  8 Apr 2008 10:32:25 +0200 (CEST)\n"
			+ "Received: from ox.open-xchange.com (localhost.localdomain [127.0.0.1])\n"
			+ "	by ox.open-xchange.com (Postfix) with ESMTP id C7C6632C60D;\n"
			+ "	Tue,  8 Apr 2008 10:32:24 +0200 (CEST)\n" + "X-Original-To: dream-team@ox.open-xchange.com\n"
			+ "Delivered-To: dream-team@ox.open-xchange.com\n"
			+ "Received: by ox.open-xchange.com (Postfix, from userid 99)\n"
			+ "	id C263B32C8A3; Tue,  8 Apr 2008 10:32:22 +0200 (CEST)\n"
			+ "Received: from netline.de (mail.netline-is.de [10.20.30.2])\n"
			+ "	by ox.open-xchange.com (Postfix) with ESMTP id 5E96D32C87C;\n"
			+ "	Tue,  8 Apr 2008 10:32:20 +0200 (CEST)\n" + "Received: from [192.168.32.7] (helo=ox.netline-is.de)\n"
			+ "	by netline.de with esmtp (Exim)\n" + "	id 1Jj95n-0003af-00; Tue, 08 Apr 2008 10:22:11 +0200\n"
			+ "Received: by ox.netline-is.de (Postfix, from userid 65534)\n"
			+ "	id 8A38F3DCB91; Tue,  8 Apr 2008 10:33:08 +0200 (CEST)\n"
			+ "Received: from oxee (unknown [192.168.32.9])\n"
			+ "	by ox.netline-is.de (Postfix) with ESMTP id 0657B3DCB89;\n"
			+ "	Tue,  8 Apr 2008 10:33:08 +0200 (CEST)\n" + "Date: Tue, 8 Apr 2008 10:32:18 +0200 (CEST)\n"
			+ "From: \"Di Lella, Leonardo\" <leonardo.dilella@open-xchange.com>\n"
			+ "To: dream-team@open-xchange.com,\n" + "	Holger Achtziger <Holger.Achtziger@open-xchange.com>\n"
			+ "Message-ID: <32496175.17311207643539009.JavaMail.open-xchange@oxee>\n"
			+ "In-Reply-To: <47F662C4.5060605@open-xchange.com>\n"
			+ "References: <47F662C4.5060605@open-xchange.com>\n"
			+ "Subject: =?UTF-8?Q?Re:_[dream-team]_Vserver_von_1und1_f=C3=BCr_alle_OX_Mitarbeiter?=\n"
			+ "MIME-Version: 1.0\n" + "Content-Type: multipart/alternative; \n"
			+ "	boundary=\"----=_Part_932_16478682.1207643538866\"\n" + "X-Priority: 3\n"
			+ "X-Mailer: Open-Xchange Mailer v6.5.0-6342\n" + "X-Scanner: exiscan *1Jj95n-0003af-00*Nlh5NV02rM6*\n"
			+ "	http://duncanthrax.net/exiscan/\n" + "Cc: \n" + "X-BeenThere: dream-team@open-xchange.com\n"
			+ "X-Mailman-Version: 2.1.5\n" + "Precedence: list\n"
			+ "List-Id: Mailinglist for whole the dream-team of Open-Xchange - all members of\n"
			+ "	Open-Change <dream-team.open-xchange.com>\n"
			+ "List-Unsubscribe: <https://ox.open-xchange.com/mailman/listinfo/dream-team>,\n"
			+ "	<mailto:dream-team-request@open-xchange.com?subject=unsubscribe>\n"
			+ "List-Archive: <https://ox.open-xchange.com/pipermail/dream-team>\n"
			+ "List-Post: <mailto:dream-team@open-xchange.com>\n"
			+ "List-Help: <mailto:dream-team-request@open-xchange.com?subject=help>\n"
			+ "List-Subscribe: <https://ox.open-xchange.com/mailman/listinfo/dream-team>,\n"
			+ "	<mailto:dream-team-request@open-xchange.com?subject=subscribe>\n"
			+ "Sender: dream-team-bounces@open-xchange.com\n" + "Errors-To: dream-team-bounces@open-xchange.com\n"
			+ "X-Scanner: exiscan *1Jj95z-0003bM-00*HSfWvPhKQvA* http://duncanthrax.net/exiscan/\n"
			+ "X-Spam-Checker-Version: SpamAssassin 2.64 (2004-01-11) on ox.netline-is.de\n" + "X-Spam-Level: \n"
			+ "X-Spam-Status: No, hits=-4.2 required=5.0 tests=AWL,BAYES_00,HTML_30_40,\n"
			+ "	HTML_MESSAGE,HTML_TITLE_EMPTY autolearn=no version=2.64\n" + "\n"
			+ "------=_Part_932_16478682.1207643538866\n" + "MIME-Version: 1.0\n"
			+ "Content-Type: text/plain; charset=UTF-8\n" + "Content-Transfer-Encoding: 7bit\n" + "\n"
			+ "Holger Achtziger <Holger.Achtziger@open-xchange.com> hat am 4. April 2008 um 19:17 geschrieben:\n"
			+ "\n" + "> Hallo!\n" + "\n" + "Hallo,\n" + "\n" + "wie lauten die Randbedingungen ? \n" + "\n"
			+ "1) Wie lange zahlt uns 1&1 den Server (nur einen Jahr?) ?\n"
			+ "2) Privat oder nur geschaeftlich einsetzbar ?\n" + "3) Was passiert bei einer OX-Kuendigung ?\n" + "\n"
			+ "Danke.\n" + "\n" + "--\n" + "best regards\n" + "Leonardo Di Lella\n" + "\n" + "Open-Xchange GmbH\n"
			+ "http://www.open-xchange.com/wiki/index.php?title=User:Ledil\n" + "\n"
			+ "[ledil (irc), leonardo_dilella (skype)]\n"
			+ "0x15208141 | 2829 F2BE 2242 91F0 24EB C0A7 258E F1A2 1520 8141\n"
			+ "------=_Part_932_16478682.1207643538866\n" + "MIME-Version: 1.0\n"
			+ "Content-Type: text/html; charset=UTF-8\n" + "Content-Transfer-Encoding: 7bit\n" + "\n" + "\n"
			+ "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n"
			+ "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" + "\n"
			+ "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" + "  <head>\n"
			+ "    <meta content=\"text/html; charset=UTF-8\" http-equiv=\"Content-Type\">\n"
			+ "    <meta name=\"generator\"\n"
			+ "    content=\"HTML Tidy for Java (vers. 26 Sep 2004), see www.w3.org\" />\n" + "\n"
			+ "    <title></title>\n" + "  </head>\n" + "\n" + "  <body>\n"
			+ "    Holger Achtziger &lt;Holger.Achtziger@open-xchange.com&gt; hat am\n"
			+ "    4. April 2008 um 19:17 geschrieben:<br />\n" + "    <br />\n" + "    &gt; Hallo!<br />\n"
			+ "    <br />\n" + "    Hallo,<br />\n" + "    <br />\n" + "    wie lauten die Randbedingungen ? <br />\n"
			+ "    <br />\n" + "    1) Wie lange zahlt uns 1&amp;1 den Server (nur einen Jahr?) ?<br />\n"
			+ "    2) Privat oder nur geschaeftlich einsetzbar ?<br />\n"
			+ "    3) Was passiert bei einer OX-Kuendigung ?<br />\n" + "    <br />\n" + "    Danke.<br />\n"
			+ "    <br />\n" + "\n" + "    <div>\n" + "      --<br />\n" + "      best regards<br />\n"
			+ "      Leonardo Di Lella<br />\n" + "      <br />\n" + "      Open-Xchange GmbH<br />\n" + "      <a\n"
			+ "      href=\"http://www.open-xchange.com/wiki/index.php?title=User:Ledil\"\n"
			+ "       target=\"_blank\">http://www.open-xchange.com/wiki/index.php?title=User:Ledil</a><br />\n"
			+ "      <br />\n" + "      [ledil (irc), leonardo_dilella (skype)]<br />\n"
			+ "      0x15208141 | 2829 F2BE 2242 91F0 24EB C0A7 258E F1A2 1520 8141\n" + "    </div>\n" + "  </body>\n"
			+ "\n" + "</html>\n" + "\n" + "------=_Part_932_16478682.1207643538866--\n" + "\n";

	public void testMailSearchSmallMailbox() {
		try {
			final SessionObject session = SessionObjectWrapper.createSessionObject(getUser(),
					new ContextImpl(getCid()), "mail-test-session");
			session.setPassword(getPassword());

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();

			final String name = "TemporaryFolder";
			String fullname = null;
			{
				final MailFolder inbox = mailAccess.getFolderStorage().getFolder("INBOX");
				final String parentFullname;
				if (inbox.isHoldsFolders()) {
					fullname = new StringBuilder(inbox.getFullname()).append(inbox.getSeparator()).append(name)
							.toString();
					parentFullname = "INBOX";
				} else {
					fullname = name;
					parentFullname = MailFolder.DEFAULT_FOLDER_ID;
				}

				final MailFolderDescription mfd = new MailFolderDescription();
				mfd.setExists(false);
				mfd.setParentFullname(parentFullname);
				mfd.setSeparator(inbox.getSeparator());
				mfd.setSubscribed(false);
				mfd.setName(name);

				final Class<? extends MailPermission> clazz = MailProviderRegistry.getMailProviderBySession(session)
						.getMailPermissionClass();
				final MailPermission p = MailPermission.newInstance(clazz);
				p.setEntity(getUser());
				p.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
						OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
				p.setFolderAdmin(true);
				p.setGroupPermission(false);
				mfd.addPermission(p);
				mailAccess.getFolderStorage().createFolder(mfd);
			}
			try {
				/*
				 * Fill
				 */
				mailAccess.getMessageStorage().appendMessages(fullname, getMessages(getTestMailDir(), -1));

				final long uid;
				{
					final MailMessage mail = MIMEMessageConverter.convertMessage(RFC822_SRC.getBytes("US-ASCII"));
					assertTrue("Unexpected or missing Message-ID header",
							"<32496175.17311207643539009.JavaMail.open-xchange@oxee>".equals(mail
									.getHeader("Message-ID")));
					uid = mailAccess.getMessageStorage().appendMessages(fullname, new MailMessage[] { mail })[0];
				}

				SearchTerm<?> term = new ToTerm("dream-team@open-xchange.com");
				MailMessage[] result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null,
						null, term, FIELDS_ID);

				boolean found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = result[i].getMailId() == uid;
				}
				assertTrue("Message not found by To search term", found);

				term = new FromTerm("\"Di Lella, Leonardo\" <leonardo.dilella@open-xchange.com>");
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = result[i].getMailId() == uid;
				}
				assertTrue("Message not found by From search term", found);

				term = new FromTerm("Di Lella");
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = result[i].getMailId() == uid;
				}
				assertTrue("Message not found by From search term", found);

				term = new SubjectTerm("Vserver von 1und1");
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = result[i].getMailId() == uid;
				}
				assertTrue("Message not found by Subject search term", found);

				term = new SentDateTerm(ComparisonType.GREATER_THAN, DateUtils
						.getDateRFC822("Tue, 8 Apr 2008 10:30:18 +0200 (CEST)"));
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = result[i].getMailId() == uid;
				}
				assertTrue("Message not found by Sent Date search term", found);

				term = new HeaderTerm("X-Priority", String.valueOf(3));
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = result[i].getMailId() == uid;
				}
				assertTrue("Message not found by Header \"X-Priority\" search term", found);

				term = new HeaderTerm("In-Reply-To", "<47F662C4.5060605@open-xchange.com>");
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				assertTrue("Unexpected result size: " + result.length, result.length == 1);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = result[i].getMailId() == uid;
				}
				assertTrue("Message not found by Header \"In-Reply-To\" search term", found);
				
				
				term = new BodyTerm("4. April 2008 um 19:17 geschrieben");
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = result[i].getMailId() == uid;
				}
				assertTrue("Message not found by body search term", found);
				
				
				final long size = mailAccess.getMessageStorage().getMessage(fullname, uid, false).getSize();
				term = new SizeTerm(ComparisonType.GREATER_THAN, (int) (size - 10));
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = result[i].getMailId() == uid;
				}
				assertTrue("Message not found by size search term", found);
				
				term = new SizeTerm(ComparisonType.LESS_THAN, (int) (size + 10));
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = result[i].getMailId() == uid;
				}
				assertTrue("Message not found by size search term", found);
				
				term = new SizeTerm(ComparisonType.EQUALS, (int) (size));
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = result[i].getMailId() == uid;
				}
				assertTrue("Message not found by size search term", found);

			} finally {

				if (fullname != null) {
					mailAccess.getFolderStorage().deleteFolder(fullname, true);
					System.out.println("Temporary folder deleted: " + fullname);
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

	public void testMailSearchLargeMailbox() {
		try {
			final SessionObject session = SessionObjectWrapper.createSessionObject(getUser(),
					new ContextImpl(getCid()), "mail-test-session");
			session.setPassword(getPassword());

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();

			final String name = "TemporaryFolder";
			String fullname = null;
			{
				final MailFolder inbox = mailAccess.getFolderStorage().getFolder("INBOX");
				final String parentFullname;
				if (inbox.isHoldsFolders()) {
					fullname = new StringBuilder(inbox.getFullname()).append(inbox.getSeparator()).append(name)
							.toString();
					parentFullname = "INBOX";
				} else {
					fullname = name;
					parentFullname = MailFolder.DEFAULT_FOLDER_ID;
				}

				final MailFolderDescription mfd = new MailFolderDescription();
				mfd.setExists(false);
				mfd.setParentFullname(parentFullname);
				mfd.setSeparator(inbox.getSeparator());
				mfd.setSubscribed(false);
				mfd.setName(name);

				final Class<? extends MailPermission> clazz = MailProviderRegistry.getMailProviderBySession(session)
						.getMailPermissionClass();
				final MailPermission p = MailPermission.newInstance(clazz);
				p.setEntity(getUser());
				p.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
						OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
				p.setFolderAdmin(true);
				p.setGroupPermission(false);
				mfd.addPermission(p);
				mailAccess.getFolderStorage().createFolder(mfd);
			}
			try {
				{
					/*
					 * Fill until fetch limit exceeded
					 */
					final MailMessage[] mails = getMessages(getTestMailDir(), -1);
					final int breakEven = MailConfig.getMailFetchLimit();
					int count = 0;
					while (count < breakEven) {
						mailAccess.getMessageStorage().appendMessages(fullname, mails);
						count += mails.length;
					}
					/*
					 * One more time...
					 */
					mailAccess.getMessageStorage().appendMessages(fullname, mails);
				}

				final long uid;
				{
					final MailMessage mail = MIMEMessageConverter.convertMessage(RFC822_SRC.getBytes("US-ASCII"));
					uid = mailAccess.getMessageStorage().appendMessages(fullname, new MailMessage[] { mail })[0];
				}

				SearchTerm<?> term = new ToTerm("dream-team@open-xchange.com");
				MailMessage[] result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null,
						null, term, FIELDS_ID);

				boolean found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = result[i].getMailId() == uid;
				}
				assertTrue("Message not found by To search term", found);

				term = new FromTerm("\"Di Lella, Leonardo\" <leonardo.dilella@open-xchange.com>");
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = result[i].getMailId() == uid;
				}
				assertTrue("Message not found by From search term", found);

				term = new FromTerm("Di Lella");
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = result[i].getMailId() == uid;
				}
				assertTrue("Message not found by From search term", found);

				term = new SubjectTerm("Vserver von 1und1");
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = result[i].getMailId() == uid;
				}
				assertTrue("Message not found by Subject search term", found);

				term = new SentDateTerm(ComparisonType.GREATER_THAN, DateUtils
						.getDateRFC822("Tue, 8 Apr 2008 10:30:18 +0200 (CEST)"));
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = result[i].getMailId() == uid;
				}
				assertTrue("Message not found by Sent Date search term", found);

				term = new HeaderTerm("X-Priority", String.valueOf(3));
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = result[i].getMailId() == uid;
				}
				assertTrue("Message not found by Header \"X-Priority\" search term", found);

				term = new HeaderTerm("In-Reply-To", "<47F662C4.5060605@open-xchange.com>");
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				assertTrue("Unexpected result size: " + result.length, result.length == 1);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = result[i].getMailId() == uid;
				}
				assertTrue("Message not found by Header \"In-Reply-To\" search term", found);
				
				
				term = new BodyTerm("4. April 2008 um 19:17 geschrieben");
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = result[i].getMailId() == uid;
				}
				assertTrue("Message not found by body search term", found);
				
				
				final long size = mailAccess.getMessageStorage().getMessage(fullname, uid, false).getSize();
				term = new SizeTerm(ComparisonType.GREATER_THAN, (int) (size - 10));
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = result[i].getMailId() == uid;
				}
				assertTrue("Message not found by size search term", found);
				
				term = new SizeTerm(ComparisonType.LESS_THAN, (int) (size + 10));
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = result[i].getMailId() == uid;
				}
				assertTrue("Message not found by size search term", found);
				
				term = new SizeTerm(ComparisonType.EQUALS, (int) (size));
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = result[i].getMailId() == uid;
				}
				assertTrue("Message not found by size search term", found);

			} finally {

				if (fullname != null) {
					mailAccess.getFolderStorage().deleteFolder(fullname, true);
					System.out.println("Temporary folder deleted: " + fullname);
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
