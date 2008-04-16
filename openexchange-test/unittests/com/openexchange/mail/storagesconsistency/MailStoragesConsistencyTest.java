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

package com.openexchange.mail.storagesconsistency;

import java.util.HashSet;
import java.util.Set;

import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;

/**
 * {@link MailStoragesConsistencyTest}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MailStoragesConsistencyTest extends AbstractMailTest {

	private static final String TEMPORARY_FOLDER = "TemporaryFolder";

	private static final String INBOX = "INBOX";

	private static final MailField[] FIELDS_ID = { MailField.ID };

	/**
	 * 
	 */
	public MailStoragesConsistencyTest() {
		super();
	}

	/**
	 * @param name
	 */
	public MailStoragesConsistencyTest(final String name) {
		super(name);
	}

	public void testMailStoragesConsistency1() {
		try {
			final SessionObject session = SessionObjectWrapper.createSessionObject(getUser(),
					new ContextImpl(getCid()), "mail-test-session");
			session.setPassword(getPassword());

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();

			String fullname = null;
			try {
				String parentFullname = null;
				{
					final MailFolder inbox = mailAccess.getFolderStorage().getFolder(INBOX);
					if (inbox.isHoldsFolders()) {
						fullname = new StringBuilder(inbox.getFullname()).append(inbox.getSeparator()).append(
								TEMPORARY_FOLDER).toString();
						parentFullname = INBOX;
					} else {
						fullname = TEMPORARY_FOLDER;
						parentFullname = MailFolder.DEFAULT_FOLDER_ID;
					}

					final MailFolderDescription mfd = new MailFolderDescription();
					mfd.setExists(false);
					mfd.setParentFullname(parentFullname);
					mfd.setSeparator(inbox.getSeparator());
					mfd.setSubscribed(false);
					mfd.setName(TEMPORARY_FOLDER);

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

				final long[] uids = mailAccess.getMessageStorage().appendMessages(fullname,
						getMessages(getTestMailDir(), -1));
				mailAccess.getMessageStorage().copyMessages(fullname, fullname, uids, true);

				mailAccess.getFolderStorage().deleteFolder(fullname, true);

				try {
					mailAccess.getMessageStorage().getAllMessages(fullname, IndexRange.NULL, null, null, FIELDS_ID);
				} catch (final MailException e) {
					if (e.getCause() != null) {
						e.printStackTrace();
						fail("Folder/message storage inconsistency detected: " + e.getCause().getMessage());
					}
				} catch (final Exception e) {
					e.printStackTrace();
					fail("Folder/message storage inconsistency detected: " + e.getMessage());
				} finally {
					fullname = null;
				}

			} finally {
				if (fullname != null && mailAccess.getFolderStorage().exists(fullname)) {
					mailAccess.getFolderStorage().deleteFolder(fullname, true);
				}

				mailAccess.close(false);
			}
		} catch (final Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	public void testMailStoragesConsistency2() {
		try {
			final SessionObject session = SessionObjectWrapper.createSessionObject(getUser(),
					new ContextImpl(getCid()), "mail-test-session");
			session.setPassword(getPassword());

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();

			String fullname = null;
			long[] trashedIds = null;
			try {
				String parentFullname = null;
				{
					final MailFolder inbox = mailAccess.getFolderStorage().getFolder(INBOX);
					if (inbox.isHoldsFolders()) {
						fullname = new StringBuilder(inbox.getFullname()).append(inbox.getSeparator()).append(
								TEMPORARY_FOLDER).toString();
						parentFullname = INBOX;
					} else {
						fullname = TEMPORARY_FOLDER;
						parentFullname = MailFolder.DEFAULT_FOLDER_ID;
					}

					final MailFolderDescription mfd = new MailFolderDescription();
					mfd.setExists(false);
					mfd.setParentFullname(parentFullname);
					mfd.setSeparator(inbox.getSeparator());
					mfd.setSubscribed(false);
					mfd.setName(TEMPORARY_FOLDER);

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
				final long[] uids = mailAccess.getMessageStorage().appendMessages(fullname,
						getMessages(getTestMailDir(), -1));

				/*
				 * Touch trash folder by message storage
				 */
				final String trashFullname = mailAccess.getFolderStorage().getTrashFolder();

				final int numTrashedMails = mailAccess.getFolderStorage().getFolder(trashFullname).getMessageCount();
				MailMessage[] trashed = mailAccess.getMessageStorage().getAllMessages(trashFullname, IndexRange.NULL,
						MailListField.RECEIVED_DATE, OrderDirection.ASC, FIELDS_ID);
				assertTrue("Size mismatch: " + trashed.length + " but should be " + numTrashedMails,
						trashed.length == numTrashedMails);
				final Set<Long> oldIds = new HashSet<Long>(numTrashedMails);
				for (int i = 0; i < trashed.length; i++) {
					oldIds.add(Long.valueOf(trashed[i].getMailId()));
				}

				/*
				 * Alter trash's content through clearing temporary folder by
				 * folder storage
				 */
				mailAccess.getFolderStorage().clearFolder(fullname);

				/*
				 * Check if folder storage's modification has been reported to
				 * message storage
				 */
				final int expectedMsgCount = numTrashedMails + uids.length;
				assertTrue("Mails not completely moved to trash", mailAccess.getFolderStorage()
						.getFolder(trashFullname).getMessageCount() == expectedMsgCount);
				trashed = mailAccess.getMessageStorage().getAllMessages(trashFullname, IndexRange.NULL,
						MailListField.RECEIVED_DATE, OrderDirection.ASC, FIELDS_ID);
				assertTrue("Size mismatch: " + trashed.length + " but should be " + expectedMsgCount,
						trashed.length == expectedMsgCount);
				final Set<Long> newIds = new HashSet<Long>(numTrashedMails);
				for (int i = 0; i < trashed.length; i++) {
					newIds.add(Long.valueOf(trashed[i].getMailId()));
				}
				newIds.removeAll(oldIds);

				trashedIds = new long[newIds.size()];
				assertTrue("Number of new trash mails does not match trashed mails", trashedIds.length == uids.length);
				int i = 0;
				for (final Long id : newIds) {
					trashedIds[i++] = id.longValue();
				}

			} finally {
				if (fullname != null && mailAccess.getFolderStorage().exists(fullname)) {
					mailAccess.getFolderStorage().deleteFolder(fullname, true);
				}

				if (trashedIds != null) {
					mailAccess.getMessageStorage().deleteMessages(mailAccess.getFolderStorage().getTrashFolder(),
							trashedIds, true);
				}

				mailAccess.close(false);
			}
		} catch (final Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
}
