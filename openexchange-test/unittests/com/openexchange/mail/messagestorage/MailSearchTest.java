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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.mail.MessagingException;
import com.openexchange.exception.OXException;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
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
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.sessiond.impl.SessionObject;

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

	public void testMailSearch() throws OXException, MessagingException, IOException {
			final SessionObject session = getSession();
			final MailMessage[] mails = getMessages(getTestMailDir(), -1);

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();
			final String[] uids = mailAccess.getMessageStorage().appendMessages("INBOX", mails);
			try {

				SearchTerm<?> term = new HeaderTerm(MessageHeaders.HDR_CONTENT_TYPE, "text/plain; charset=us-ascii");
				System.currentTimeMillis();
				MailMessage[] fetchedMails = mailAccess.getMessageStorage().searchMessages("INBOX", IndexRange.NULL,
						null, null, term, FIELDS_ID);
				for (int i = 0; i < fetchedMails.length; i++) {
					assertFalse("Mail ID is -1", fetchedMails[i].getMailId() == null);
					assertTrue("Missing Content-Type", fetchedMails[i].containsContentType());
                    assertTrue("Unexpected Content-Type", fetchedMails[i].getContentType().startsWith("text/plain"));
				}

				term = new FlagTerm(MailMessage.FLAG_SEEN, false);
				System.currentTimeMillis();
				fetchedMails = mailAccess.getMessageStorage().searchMessages("INBOX", IndexRange.NULL, null, null,
						term, FIELDS_MORE);
				for (int i = 0; i < fetchedMails.length; i++) {
					assertFalse("Missing mail ID", fetchedMails[i].getMailId() == null);
					assertTrue("Missing content type", fetchedMails[i].containsContentType());
					assertTrue("Missing flags", fetchedMails[i].containsFlags());
					assertTrue("Message contains flag \\Seen although only unseen messages should have been returned",
							(fetchedMails[i].getFlags() & MailMessage.FLAG_SEEN) == 0);
				}

				/*
				 * All >= 1KB (1024bytes)
				 */
				term = new SizeTerm(ComparisonType.GREATER_THAN, 1023);
				System.currentTimeMillis();
				fetchedMails = mailAccess.getMessageStorage().searchMessages("INBOX", IndexRange.NULL, null, null,
						term, FIELDS_EVEN_MORE);
				for (int i = 0; i < fetchedMails.length; i++) {
					assertFalse("Missing mail ID", fetchedMails[i].getMailId() == null);
					assertTrue("Missing size", fetchedMails[i].containsSize());
					assertTrue("Unexpected size", fetchedMails[i].getSize() > 1023);
				}

				final Map<String, String> map = new HashMap<String, String>(fetchedMails.length);
				for (int i = 0; i < fetchedMails.length && i < 100; i++) {
					final String messageId = fetchedMails[i].getFirstHeader(MessageHeaders.HDR_MESSAGE_ID);
					if (null != messageId && messageId.length() > 0 && !"null".equalsIgnoreCase(messageId)) {
						map.put(fetchedMails[i].getMailId(), messageId);
					}
				}

				final int size = map.size();
				final Iterator<Map.Entry<String, String>> iter = map.entrySet().iterator();
				for (int i = 0; i < size; i++) {
					final Map.Entry<String, String> e = iter.next();
					term = new HeaderTerm(MessageHeaders.HDR_MESSAGE_ID, e.getValue());
					System.currentTimeMillis();
					final MailMessage[] searchedMails = mailAccess.getMessageStorage().searchMessages("INBOX",
							IndexRange.NULL, null, null, term, FIELDS_ID_AND_HEADER);
					assertTrue("Search failed: No result", null != searchedMails);
					assertTrue("Search failed: Non-matching result size", searchedMails.length >= 1);
					boolean found = false;
					for (int j = 0; j < searchedMails.length && !found; j++) {
						final String messageId = searchedMails[j].getFirstHeader(MessageHeaders.HDR_MESSAGE_ID);
						assertTrue("Missing Message-Id", null != messageId);
						assertTrue("Non-matching Message-Id", messageId.equals(e.getValue()));
						found = e.getKey().equals(searchedMails[j].getMailId());
					}
					assertTrue("Non-matching mail ID", found);
				}

			} finally {

				mailAccess.getMessageStorage().deleteMessages("INBOX", uids, true);

				/*
				 * close
				 */
				mailAccess.close(false);
			}
	}

	private static final String RFC822_SRC = "From: \"Di Lella, Leonardo\" <leonardo.dilella@open-xchange.com>\n"
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
			+ "    dfgfdgsssssssssssssssssssssssssssssss&gt; hat am\n"
			+ "    4. April 2008 um 19:17 geschrieben:<br />\n" + "    <br />\n" + "    &gt; Hallo!<br />\n"
			+ "    <br />\n" + "    Hallo,<br />\n" + "    <br />\n" + "    wie lauten die Randbedingungen ? <br />\n"
			+ "    <br />\n" + "    1) Wie lange zahlt uns safgggdfag ?<br />\n"
			+ "    2) PrivasdfASFGDGF einsetzbar sdfSddddddddddd ?<br />\n"
			+ "    3) Was paFSGADFGADFGDAFG SFADGASGRADdigung ?<br />\n" + "    <br />\n" + "    Danke.<br />\n"
			+ "    <br />\n" + "\n" + "    <div>\n" + "      --<br />\n" + "      best regards<br />\n"
			+ "      dfgdfgdfg<br />\n" + "      <br />\n" + "      Open-Xchange GmbH<br />\n" + "      <a\n"
			+ "      href=\"http://www.open-xchange.com/wiki/index.php?title=User:Ledil\"\n"
			+ "       target=\"_blank\">http://www.open-xchange.com/wiki/index.php?title=User:Ledil</a><br />\n"
			+ "      <br />\n" + "      [ngfgdh (irc), kkkoooo (skyphe)]<br />\n"
			+ "      0x15208141 | 2829 F2BE 2242 91F0 24EB C0A7 258E F1A2 1520 8141\n" + "    </div>\n" + "  </body>\n"
			+ "\n" + "</html>\n" + "\n" + "------=_Part_932_16478682.1207643538866--\n" + "\n";

	public void testMailSearchSmallMailbox() throws OXException, MessagingException, IOException {
			final SessionObject session = getSession();

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

				final MailPermission p = MailProviderRegistry.getMailProviderBySession(session, MailAccount.DEFAULT_ID)
						.createNewMailPermission(session, MailAccount.DEFAULT_ID);
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

				final String uid;
				{
					final MailMessage mail = MimeMessageConverter.convertMessage(RFC822_SRC.getBytes(com.openexchange.java.Charsets.US_ASCII));
					assertEquals("Unexpected or missing Message-ID header: ",
							"<32496175.17311207643539009.JavaMail.open-xchange@oxee>", mail.getFirstHeader("Message-ID"));
					uid = mailAccess.getMessageStorage().appendMessages(fullname, new MailMessage[] { mail })[0];
				}

				SearchTerm<?> term = new ToTerm("dream-team@open-xchange.com");
				MailMessage[] result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null,
						null, term, FIELDS_ID);

				boolean found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = null != result[i].getMailId() && result[i].getMailId().equals(uid);
				}
				assertTrue("Message not found by To search term", found);

				term = new FromTerm("\"Di Lella, Leonardo\" <leonardo.dilella@open-xchange.com>");
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = null != result[i].getMailId() && result[i].getMailId().equals(uid);
				}
				assertTrue("Message not found by From search term", found);

				term = new FromTerm("Di Lella");
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = null != result[i].getMailId() && result[i].getMailId().equals(uid);
				}
				assertTrue("Message not found by From search term", found);

				term = new SubjectTerm("Vserver von 1und1");
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = null != result[i].getMailId() && result[i].getMailId().equals(uid);
				}
				assertTrue("Message not found by Subject search term", found);

				term = new SentDateTerm(ComparisonType.GREATER_THAN, DateUtils
						.getDateRFC822("Tue, 8 Apr 2008 10:30:18 +0200 (CEST)"));
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = null != result[i].getMailId() && result[i].getMailId().equals(uid);
				}
				assertTrue("Message not found by Sent Date search term", found);

				term = new HeaderTerm("X-Priority", String.valueOf(3));
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = null != result[i].getMailId() && result[i].getMailId().equals(uid);
				}
				assertTrue("Message not found by Header \"X-Priority\" search term", found);

				term = new HeaderTerm("In-Reply-To", "<47F662C4.5060605@open-xchange.com>");
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				assertTrue("Unexpected result size: " + result.length, result.length == 1);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = null != result[i].getMailId() && result[i].getMailId().equals(uid);
				}
				assertTrue("Message not found by Header \"In-Reply-To\" search term", found);

				term = new BodyTerm("4. April 2008 um 19:17 geschrieben");
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = null != result[i].getMailId() && result[i].getMailId().equals(uid);
				}
				assertTrue("Message not found by body search term", found);

				final long size = mailAccess.getMessageStorage().getMessage(fullname, uid, false).getSize();
				term = new SizeTerm(ComparisonType.GREATER_THAN, (int) (size - 10));
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = null != result[i].getMailId() && result[i].getMailId().equals(uid);
				}
				assertTrue("Message not found by size search term", found);

				term = new SizeTerm(ComparisonType.LESS_THAN, (int) (size + 10));
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = null != result[i].getMailId() && result[i].getMailId().equals(uid);
				}
				assertTrue("Message not found by size search term", found);

				term = new SizeTerm(ComparisonType.EQUALS, (int) (size));
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = null != result[i].getMailId() && result[i].getMailId().equals(uid);
				}
				assertTrue("Message not found by size search term", found);

			} finally {

				if (fullname != null) {
					mailAccess.getFolderStorage().deleteFolder(fullname, true);
				}

				/*
				 * close
				 */
				mailAccess.close(false);
			}
	}

	public void testMailSearchLargeMailbox() throws OXException, MessagingException, IOException {
			final SessionObject session = getSession();

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

				final MailPermission p = MailProviderRegistry.getMailProviderBySession(session, MailAccount.DEFAULT_ID)
						.createNewMailPermission(session, MailAccount.DEFAULT_ID);
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
					final int breakEven = MailProperties.getInstance().getMailFetchLimit();
					final String[] uids = mailAccess.getMessageStorage().appendMessages(fullname, mails);
					int count = mails.length;
					while (count < breakEven) {
						mailAccess.getMessageStorage().copyMessages(fullname, fullname, uids, true);
						count += uids.length;
					}
					/*
					 * One more time...
					 */
					mailAccess.getMessageStorage().appendMessages(fullname, mails);
				}

				final String uid;
				{
					final MailMessage mail = MimeMessageConverter.convertMessage(RFC822_SRC.getBytes(com.openexchange.java.Charsets.US_ASCII));
					uid = mailAccess.getMessageStorage().appendMessages(fullname, new MailMessage[] { mail })[0];
				}

				SearchTerm<?> term = new ToTerm("dream-team@open-xchange.com");
				MailMessage[] result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null,
						null, term, FIELDS_ID);

				boolean found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = null != result[i].getMailId() && result[i].getMailId().equals(uid);
				}
				assertTrue("Message not found by To search term", found);

				term = new FromTerm("\"Di Lella, Leonardo\" <leonardo.dilella@open-xchange.com>");
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = null != result[i].getMailId() && result[i].getMailId().equals(uid);
				}
				assertTrue("Message not found by From search term", found);

				term = new FromTerm("Di Lella");
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = null != result[i].getMailId() && result[i].getMailId().equals(uid);
				}
				assertTrue("Message not found by From search term", found);

				term = new SubjectTerm("Vserver von 1und1");
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = null != result[i].getMailId() && result[i].getMailId().equals(uid);
				}
				assertTrue("Message not found by Subject search term", found);

				term = new SentDateTerm(ComparisonType.GREATER_THAN, DateUtils
						.getDateRFC822("Tue, 8 Apr 2008 10:30:18 +0200 (CEST)"));
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = null != result[i].getMailId() && result[i].getMailId().equals(uid);
				}
				assertTrue("Message not found by Sent Date search term", found);

				term = new HeaderTerm("X-Priority", String.valueOf(3));
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = null != result[i].getMailId() && result[i].getMailId().equals(uid);
				}
				assertTrue("Message not found by Header \"X-Priority\" search term", found);

				term = new HeaderTerm("In-Reply-To", "<47F662C4.5060605@open-xchange.com>");
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				assertTrue("Unexpected result size: " + result.length, result.length == 1);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = null != result[i].getMailId() && result[i].getMailId().equals(uid);
				}
				assertTrue("Message not found by Header \"In-Reply-To\" search term", found);

				term = new BodyTerm("4. April 2008 um 19:17 geschrieben");
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = null != result[i].getMailId() && result[i].getMailId().equals(uid);
				}
				assertTrue("Message not found by body search term", found);

				final long size = mailAccess.getMessageStorage().getMessage(fullname, uid, false).getSize();
				term = new SizeTerm(ComparisonType.GREATER_THAN, (int) (size - 10));
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = null != result[i].getMailId() && result[i].getMailId().equals(uid);
				}
				assertTrue("Message not found by size search term", found);

				term = new SizeTerm(ComparisonType.LESS_THAN, (int) (size + 10));
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = null != result[i].getMailId() && result[i].getMailId().equals(uid);
				}
				assertTrue("Message not found by size search term", found);

				term = new SizeTerm(ComparisonType.EQUALS, (int) (size));
				result = mailAccess.getMessageStorage().searchMessages(fullname, IndexRange.NULL, null, null, term,
						FIELDS_ID);
				found = false;
				for (int i = 0; i < result.length && !found; i++) {
					found = null != result[i].getMailId() && result[i].getMailId().equals(uid);
				}
				assertTrue("Message not found by size search term", found);

			} finally {

				if (fullname != null) {
					mailAccess.getFolderStorage().deleteFolder(fullname, true);
				}

				/*
				 * close
				 */
				mailAccess.close(false);
			}
	}
}
