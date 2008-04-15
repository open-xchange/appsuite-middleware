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

import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;

/**
 * {@link MailCopyTest}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MailCopyTest extends AbstractMailTest {

	/**
	 * 
	 */
	public MailCopyTest() {
		super();
	}

	/**
	 * @param name
	 */
	public MailCopyTest(final String name) {
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

	public void testMailCopy() {
		try {
			final SessionObject session = SessionObjectWrapper.createSessionObject(getUser(),
					new ContextImpl(getCid()), "mail-test-session");
			session.setPassword(getPassword());
			final MailMessage[] mails = getMessages(getTestMailDir(), -1);

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();
			final long[] uids = mailAccess.getMessageStorage().appendMessages("INBOX", mails);
			try {

				final String fullname;
				{
					final String parentFullname;
					final MailFolder inbox = mailAccess.getFolderStorage().getFolder("INBOX");
					if (inbox.isHoldsFolders()) {
						fullname = new StringBuilder(inbox.getFullname()).append(inbox.getSeparator()).append(
								"TemporaryFolder").toString();
						parentFullname = "INBOX";
					} else {
						fullname = "TemporaryFolder";
						parentFullname = MailFolder.DEFAULT_FOLDER_ID;
					}

					final MailFolderDescription mfd = new MailFolderDescription();
					mfd.setExists(false);
					mfd.setParentFullname(parentFullname);
					mfd.setSeparator(inbox.getSeparator());
					mfd.setName("TemporaryFolder");

					final Class<? extends MailPermission> clazz = MailProviderRegistry
							.getMailProviderBySession(session).getMailPermissionClass();
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

					final long[] copied = mailAccess.getMessageStorage().copyMessages("INBOX", fullname, uids, false);
					assertTrue("Missing copied mail IDs", copied != null);
					assertTrue("Number of copied messages does not match", copied.length == uids.length);
					for (int i = 0; i < copied.length; i++) {
						assertTrue("Invalid mail ID", copied[i] != -1);
					}

					MailMessage[] fetchedMails = mailAccess.getMessageStorage()
							.getMessages(fullname, copied, FIELDS_ID);
					for (int i = 0; i < fetchedMails.length; i++) {
						assertFalse("Mail ID is -1", fetchedMails[i].getMailId() == -1);
					}

					fetchedMails = mailAccess.getMessageStorage().getMessages(fullname, copied, FIELDS_MORE);
					for (int i = 0; i < fetchedMails.length; i++) {
						assertFalse("Missing mail ID", fetchedMails[i].getMailId() == -1);
						assertTrue("Missing content type", fetchedMails[i].containsContentType());
						assertTrue("Missing flags", fetchedMails[i].containsFlags());
						if (fetchedMails[i].getContentType().isMimeType("multipart/*")) {
							assertFalse("Enclosed count returned -1", fetchedMails[i].getEnclosedCount() == -1);
						} else {
							assertFalse("Content is null", fetchedMails[i].getContent() == null);
						}
					}

					fetchedMails = mailAccess.getMessageStorage().getMessages(fullname, copied, FIELDS_EVEN_MORE);
					for (int i = 0; i < fetchedMails.length; i++) {
						assertFalse("Missing mail ID", fetchedMails[i].getMailId() == -1);
						assertTrue("Missing content type", fetchedMails[i].containsContentType());
						assertTrue("Missing flags", fetchedMails[i].containsFlags());
						assertTrue("Missing From", fetchedMails[i].containsFrom());
						assertTrue("Missing To", fetchedMails[i].containsTo());
						assertTrue("Missing Disposition-Notification-To", fetchedMails[i]
								.containsDispositionNotification());
						assertTrue("Missing color label", fetchedMails[i].containsColorLabel());
						assertTrue("Missing headers", fetchedMails[i].containsHeaders());
						assertTrue("Missing subject", fetchedMails[i].containsSubject());
						assertTrue("Missing thread level", fetchedMails[i].containsThreadLevel());
						assertTrue("Missing size", fetchedMails[i].containsSize());
						assertTrue("Missing priority", fetchedMails[i].containsPriority());
						assertTrue("Missing sent date", fetchedMails[i].containsSentDate());
						assertTrue("Missing received date", fetchedMails[i].containsReceivedDate());
						assertTrue("Missing Cc", fetchedMails[i].containsCc());
						assertTrue("Missing Bcc", fetchedMails[i].containsBcc());
						assertTrue("Missing folder fullname", fetchedMails[i].containsFolder());
					}

					fetchedMails = mailAccess.getMessageStorage().getMessages(fullname, copied, FIELDS_FULL);
					for (int i = 0; i < fetchedMails.length; i++) {
						assertFalse("Missing mail ID", fetchedMails[i].getMailId() == -1);
						assertTrue("Missing content type", fetchedMails[i].containsContentType());
						assertTrue("Missing flags", fetchedMails[i].containsFlags());
						assertTrue("Missing From", fetchedMails[i].containsFrom());
						assertTrue("Missing To", fetchedMails[i].containsTo());
						assertTrue("Missing Disposition-Notification-To", fetchedMails[i]
								.containsDispositionNotification());
						assertTrue("Missing color label", fetchedMails[i].containsColorLabel());
						assertTrue("Missing headers", fetchedMails[i].containsHeaders());
						assertTrue("Missing subject", fetchedMails[i].containsSubject());
						assertTrue("Missing thread level", fetchedMails[i].containsThreadLevel());
						assertTrue("Missing size", fetchedMails[i].containsSize());
						assertTrue("Missing priority", fetchedMails[i].containsPriority());
						assertTrue("Missing sent date", fetchedMails[i].containsSentDate());
						assertTrue("Missing received date", fetchedMails[i].containsReceivedDate());
						assertTrue("Missing Cc", fetchedMails[i].containsCc());
						assertTrue("Missing Bcc", fetchedMails[i].containsBcc());
						assertTrue("Missing folder fullname", fetchedMails[i].containsFolder());
						if (fetchedMails[i].getContentType().isMimeType("multipart/*")) {
							assertFalse("Enclosed count returned -1", fetchedMails[i].getEnclosedCount() == -1);
						} else {
							assertFalse("Content is null", fetchedMails[i].getContent() == null);
						}
					}

				} finally {
					mailAccess.getFolderStorage().deleteFolder(fullname, true);
					System.out.println("Temporary folder successfully deleted");
				}

			} finally {

				mailAccess.getMessageStorage().deleteMessages("INBOX", uids, true);

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
