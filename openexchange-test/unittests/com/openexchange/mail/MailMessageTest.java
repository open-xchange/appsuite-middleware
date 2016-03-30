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

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.sessiond.impl.SessionObject;

/**
 * MailMessageTest
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class MailMessageTest extends AbstractMailTest {

	/**
	 *
	 */
	public MailMessageTest() {
		super();
	}

	/**
	 * @param name
	 */
	public MailMessageTest(final String name) {
		super(name);
	}

	private static final MailField[] COMMON_LIST_FIELDS = { MailField.ID, MailField.FOLDER_ID, MailField.FROM,
			MailField.TO, MailField.RECEIVED_DATE, MailField.SENT_DATE, MailField.SUBJECT, MailField.CONTENT_TYPE,
			MailField.FLAGS, MailField.PRIORITY, MailField.COLOR_LABEL };

	public void testGetMessages() {
		try {
			final SessionObject session = getSession();
			final MailAccess<?, ?> mailConnection = MailAccess.getInstance(session);
			mailConnection.connect(/* mailConfig */);
			try {
				final MailFolder inboxFolder = mailConnection.getFolderStorage().getFolder("INBOX");

				final MailMessage[] msgs = mailConnection.getMessageStorage().searchMessages(inboxFolder.getFullname(),
						null, MailSortField.RECEIVED_DATE, OrderDirection.DESC, null, COMMON_LIST_FIELDS);
				assertTrue("No messages returned!", msgs != null && msgs.length > 0);

				if (msgs == null) {
					/*
					 * Make the warining disappear...
					 */
					return;
				}

				assertTrue("No not all messages returned!", msgs.length == inboxFolder.getMessageCount());
				final boolean cached = (msgs.length < MailProperties.getInstance().getMailFetchLimit());

				String[] uids = null;
				{
					final List<String> sla = new ArrayList<String>(msgs.length);
					for (int i = 0; i < msgs.length; i++) {
						assertTrue("Missing UID", msgs[i].getMailId() != null);
						sla.add(msgs[i].getMailId());
						assertTrue("Missing Subject", msgs[i].containsSubject());
						assertTrue("Missing Priority", msgs[i].containsPriority());
						assertTrue("Missing To", msgs[i].containsTo());
						assertTrue("Missing Flags", msgs[i].containsFlags());
						assertTrue("Missing User Flags", msgs[i].containsUserFlags());
						if (!cached) {
							assertFalse("Non-Requested field size is present", msgs[i].containsSize());
						}
					}
					uids = sla.toArray(new String[sla.size()]);
				}

				if (cached) {
					/*
					 * Test cache functionality
					 */
					final MailMessage[] mails = mailConnection.getMessageStorage().getMessages(
							inboxFolder.getFullname(), uids, new MailField[] { MailField.SIZE });
					for (int i = 0; i < mails.length; i++) {
						assertTrue("Cached message does not contain size!", mails[i].containsSize());
					}
					/*
					 * Test cache update functionality
					 */
					final String[] newUids = new String[50];
					System.arraycopy(uids, 0, newUids, 0, newUids.length);
					mailConnection.getMessageStorage()
							.updateMessageFlags(inboxFolder.getFullname(), newUids, 32, false);
				}
			} finally {
				mailConnection.close(true);
			}
		} catch (final Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void notestGetAllMessages() {
		try {
			final SessionObject session = getSession();
			final MailAccess<?, ?> mailConnection = MailAccess.getInstance(session);
			mailConnection.connect(/* mailConfig */);
			try {
				final MailFolder inboxFolder = mailConnection.getFolderStorage().getFolder("INBOX");
				checkSubfolders(inboxFolder, mailConnection);

			} finally {
				mailConnection.close(true);
			}
		} catch (final Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private void checkSubfolders(final MailFolder parent, final MailAccess<?, ?> mailConnection) throws OXException {
		checkMessages(parent, mailConnection);
		final MailFolder[] subfolders = mailConnection.getFolderStorage().getSubfolders(parent.getFullname(), true);
		assertTrue("Has Subfolders is wrong!", parent.hasSubfolders() ? subfolders != null && subfolders.length > 0
				: subfolders == null || subfolders.length <= 0);
		for (int i = 0; i < subfolders.length; i++) {
			checkSubfolders(subfolders[i], mailConnection);
		}
	}

	private static final String TEMPL = "Missing field %s in message %d in folder %s";

	private void checkMessages(final MailFolder folder, final MailAccess<?, ?> mailConnection) {
		MailMessage[] msgs = null;
		try {
			msgs = mailConnection.getMessageStorage().searchMessages(folder.getFullname(), null,
					MailSortField.RECEIVED_DATE, OrderDirection.DESC, null, COMMON_LIST_FIELDS);
		} catch (final OXException e) {
			assertTrue("Error during fetching messages: " + e.getMessage(), e.getCategory().equals(
					Category.CATEGORY_PERMISSION_DENIED));
			return;
		}
		if (msgs == null) {
			fail("Not all messages returned! Got none, expected: " + folder.getMessageCount());
			return;
		}

		assertTrue("No not all messages returned!", msgs.length == folder.getMessageCount());

		for (int i = 0; i < msgs.length; i++) {
			assertTrue(String.format(TEMPL, "UID", Long.valueOf(-1), folder.getFullname()), msgs[i].getMailId() != null);
			assertTrue(String.format(TEMPL, "Subject", Long.valueOf(msgs[i].getMailId()), folder.getFullname()),
					msgs[i].containsSubject());
			assertTrue(String.format(TEMPL, "Priority", Long.valueOf(msgs[i].getMailId()), folder.getFullname()),
					msgs[i].containsPriority());
			assertTrue(String.format(TEMPL, "To", Long.valueOf(msgs[i].getMailId()), folder.getFullname()), msgs[i]
					.containsTo());
			assertTrue(String.format(TEMPL, "Flags", Long.valueOf(msgs[i].getMailId()), folder.getFullname()), msgs[i]
					.containsFlags());
			assertTrue(String.format(TEMPL, "User Flags", Long.valueOf(msgs[i].getMailId()), folder.getFullname()),
					msgs[i].containsUserFlags());
		}
	}
}
