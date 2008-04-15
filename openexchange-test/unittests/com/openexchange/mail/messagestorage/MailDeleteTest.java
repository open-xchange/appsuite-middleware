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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;

/**
 * {@link MailDeleteTest}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MailDeleteTest extends AbstractMailTest {

	/**
	 * 
	 */
	public MailDeleteTest() {
		super();
	}

	/**
	 * @param name
	 */
	public MailDeleteTest(final String name) {
		super(name);
	}

	private static final MailField[] FIELDS_ID = { MailField.ID };

	private static final MailField[] FIELDS_MORE = { MailField.ID, MailField.CONTENT_TYPE, MailField.FLAGS,
			MailField.BODY };

	private static final MailField[] FIELDS_EVEN_MORE = { MailField.ID, MailField.CONTENT_TYPE, MailField.FLAGS,
			MailField.FROM, MailField.TO, MailField.DISPOSITION_NOTIFICATION_TO, MailField.COLOR_LABEL,
			MailField.HEADERS, MailField.SUBJECT, MailField.THREAD_LEVEL, MailField.SIZE, MailField.PRIORITY,
			MailField.SENT_DATE, MailField.RECEIVED_DATE, MailField.CC, MailField.BCC, MailField.FOLDER_ID };

	private static final MailField[] FIELDS_FULL = { MailField.FULL };

	public void testMailDelete() {
		try {
			final SessionObject session = SessionObjectWrapper.createSessionObject(getUser(),
					new ContextImpl(getCid()), "mail-test-session");
			session.setPassword(getPassword());
			final MailMessage[] mails = getMessages(getTestMailDir(), -1);

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();
			final long[] uids = mailAccess.getMessageStorage().appendMessages("INBOX", mails);
			long[] trashedIDs = null;
			try {

				final String trashFullname = mailAccess.getFolderStorage().getTrashFolder();
				MailFolder trash = mailAccess.getFolderStorage().getFolder(trashFullname);
				final int prevMessageCount = trash.getMessageCount();

				MailMessage[] trashed = mailAccess.getMessageStorage().getAllMessages(trashFullname, IndexRange.NULL,
						MailListField.RECEIVED_DATE, OrderDirection.DESC, FIELDS_ID);
				final Set<Long> prevIds = new HashSet<Long>(prevMessageCount);
				for (final MailMessage mail : trashed) {
					prevIds.add(Long.valueOf(mail.getMailId()));
				}

				mailAccess.getMessageStorage().deleteMessages("INBOX", uids, false);

				trash = mailAccess.getFolderStorage().getFolder(trashFullname);
				assertTrue("Trash's number of message has not been increased appropriately", prevMessageCount
						+ uids.length == trash.getMessageCount());
				
				trashed = mailAccess.getMessageStorage().getAllMessages(trashFullname, IndexRange.NULL,
						MailListField.RECEIVED_DATE, OrderDirection.DESC, FIELDS_ID);
				assertTrue("Size mismatch: " + trashed.length + " but should be " + trash.getMessageCount(), trashed.length == trash.getMessageCount());
				final Set<Long> ids = new HashSet<Long>(trash.getMessageCount());
				for (final MailMessage mail : trashed) {
					ids.add(Long.valueOf(mail.getMailId()));
				}
				ids.removeAll(prevIds);
				assertTrue("Size mismatch: " + ids.size() + " but should be " + uids.length, ids.size() == uids.length);
				
				trashedIDs = new long[uids.length];
				{
					int k = 0;
					for (final Long id : ids) {
						trashedIDs[k++] = id.longValue();
					}
				}

				trashed = mailAccess.getMessageStorage().getMessages(trashFullname, trashedIDs, FIELDS_EVEN_MORE);
				assertTrue("No matching trashed messages found: "
						+ (null == trashed ? "null" : String.valueOf(trashed.length)) + " IDs: "
						+ Arrays.toString(trashedIDs), trashed != null && trashed.length == uids.length);
				for (int i = 0; i < trashed.length; i++) {
					assertFalse("Missing mail ID", trashed[i].getMailId() == -1);
					assertTrue("Missing content type", trashed[i].containsContentType());
					assertTrue("Missing flags", trashed[i].containsFlags());
					assertTrue("Missing From", trashed[i].containsFrom());
					assertTrue("Missing To", trashed[i].containsTo());
					assertTrue("Missing Disposition-Notification-To", trashed[i].containsDispositionNotification());
					assertTrue("Missing color label", trashed[i].containsColorLabel());
					assertTrue("Missing headers", trashed[i].containsHeaders());
					assertTrue("Missing subject", trashed[i].containsSubject());
					assertTrue("Missing thread level", trashed[i].containsThreadLevel());
					assertTrue("Missing size", trashed[i].containsSize());
					assertTrue("Missing priority", trashed[i].containsPriority());
					assertTrue("Missing sent date", trashed[i].containsSentDate());
					assertTrue("Missing received date", trashed[i].containsReceivedDate());
					assertTrue("Missing Cc", trashed[i].containsCc());
					assertTrue("Missing Bcc", trashed[i].containsBcc());
					assertTrue("Missing folder fullname", trashed[i].containsFolder());
				}

			} finally {

				mailAccess.getMessageStorage().deleteMessages("INBOX", uids, true);

				if (trashedIDs != null) {
					mailAccess.getMessageStorage().deleteMessages(
							mailAccess.getFolderStorage().getTrashFolder(), trashedIDs, true);
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
