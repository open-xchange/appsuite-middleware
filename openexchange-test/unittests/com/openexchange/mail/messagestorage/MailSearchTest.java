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
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.search.ComparisonType;
import com.openexchange.mail.search.FlagTerm;
import com.openexchange.mail.search.HeaderTerm;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.search.SizeTerm;
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
}
