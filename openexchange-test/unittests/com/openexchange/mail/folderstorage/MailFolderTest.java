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

package com.openexchange.mail.folderstorage;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.mail.MessagingException;
import com.openexchange.exception.OXException;
import com.openexchange.imap.dataobjects.IMAPMailFolder;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.Quota;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailCapabilities;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.permission.DefaultMailPermission;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.sessiond.impl.SessionObject;

/**
 * {@link MailFolderTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class MailFolderTest extends AbstractMailTest {

	private static final String INBOX = "INBOX";

	/**
	 *
	 */
	public MailFolderTest() {
		super();
	}

	/**
	 * @param name
	 */
	public MailFolderTest(final String name) {
		super(name);
	}

	public void testStandardFolders() {
		try {
			final SessionObject session = getSession();

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();
			try {
				String fullname = mailAccess.getFolderStorage().getDraftsFolder();
				assertTrue("Missing drafts folder", fullname != null && fullname.length() > 0);

				fullname = mailAccess.getFolderStorage().getSentFolder();
				assertTrue("Missing sent folder", fullname != null && fullname.length() > 0);

				fullname = mailAccess.getFolderStorage().getSpamFolder();
				assertTrue("Missing spam folder", fullname != null && fullname.length() > 0);

				fullname = mailAccess.getFolderStorage().getTrashFolder();
				assertTrue("Missing trash folder", fullname != null && fullname.length() > 0);

				if (UserSettingMailStorage.getInstance().getUserSettingMail(getUser(), getCid()).isSpamEnabled()) {
					fullname = mailAccess.getFolderStorage().getConfirmedHamFolder();
					assertTrue("Missing confirmed ham folder: " + fullname, fullname != null && fullname.length() > 0);

					fullname = mailAccess.getFolderStorage().getConfirmedSpamFolder();
					assertTrue("Missing confirmed spam folder: " + fullname, fullname != null && fullname.length() > 0);
				}

			} finally {
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

	public void testExistStandardFolders() {
		try {
			final SessionObject session = getSession();

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();
			try {
				String fullname = mailAccess.getFolderStorage().getDraftsFolder();
				assertTrue("Non-existing drafts folder", mailAccess.getFolderStorage().exists(fullname));

				fullname = mailAccess.getFolderStorage().getSentFolder();
				assertTrue("Non-existing sent folder", mailAccess.getFolderStorage().exists(fullname));

				fullname = mailAccess.getFolderStorage().getSpamFolder();
				assertTrue("Non-existing spam folder", mailAccess.getFolderStorage().exists(fullname));

				fullname = mailAccess.getFolderStorage().getTrashFolder();
				assertTrue("Non-existing trash folder", mailAccess.getFolderStorage().exists(fullname));

				if (UserSettingMailStorage.getInstance().getUserSettingMail(getUser(), getCid()).isSpamEnabled()) {
					fullname = mailAccess.getFolderStorage().getConfirmedHamFolder();
					assertTrue("Non-existing confirmed ham folder", mailAccess.getFolderStorage().exists(fullname));

					fullname = mailAccess.getFolderStorage().getConfirmedSpamFolder();
					assertTrue("Non-existing confirmed spam folder", mailAccess.getFolderStorage().exists(fullname));
				}

			} finally {
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

	public void testFolderGet() throws OXException {
			final SessionObject session = getSession();

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();
			try {
				final MailFolder f = mailAccess.getFolderStorage().getFolder(INBOX);
				assertTrue("Missing default folder flag", f.containsDefaultFolder());
				assertTrue("Missing deleted count", f.containsDeletedMessageCount());
				assertTrue("Missing exists flag", f.containsExists());
				assertTrue("Missing fullname", f.containsFullname());
				assertTrue("Missing holds folders flag", f.containsHoldsFolders());
				assertTrue("Missing holds messages flag", f.containsHoldsMessages());
				assertTrue("Missing message count", f.containsMessageCount());
				assertTrue("Missing name", f.containsName());
				assertTrue("Missing new message count", f.containsNewMessageCount());
				if (f instanceof IMAPMailFolder) {
					assertTrue("Missing non-existent flag", ((IMAPMailFolder) f).containsNonExistent());
				}
				assertTrue("Missing own permission", f.containsOwnPermission());
				assertTrue("Missing parent fullname", f.containsParentFullname());
				assertTrue("Missing permissions", f.containsPermissions());
				assertTrue("Missing root folder flag", f.containsRootFolder());
				assertTrue("Missing separator flag", f.containsSeparator());
				assertTrue("Missing subfolder flag", f.containsSubfolders());
				assertTrue("Missing subscribed flag", f.containsSubscribed());
				assertTrue("Missing subscribed subfolders flag", f.containsSubscribedSubfolders());
				assertTrue("Missing supports user flags flag", f.containsSupportsUserFlags());
				assertTrue("Missing unread message count", f.containsUnreadMessageCount());

			} finally {
				/*
				 * close
				 */
				mailAccess.close(false);
			}
	}

	public void testFolderCreateAndSubfolders() throws OXException {
			final SessionObject session = getSession();

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();

			String fullname = null;
			try {
				final MailFolder f = mailAccess.getFolderStorage().getFolder(INBOX);
				assertTrue("Missing default folder flag", f.containsDefaultFolder());
				assertTrue("Missing deleted count", f.containsDeletedMessageCount());
				assertTrue("Missing exists flag", f.containsExists());
				assertTrue("Missing fullname", f.containsFullname());
				assertTrue("Missing holds folders flag", f.containsHoldsFolders());
				assertTrue("Missing holds messages flag", f.containsHoldsMessages());
				assertTrue("Missing message count", f.containsMessageCount());
				assertTrue("Missing name", f.containsName());
				assertTrue("Missing new message count", f.containsNewMessageCount());
				if (f instanceof IMAPMailFolder) {
					assertTrue("Missing non-existent flag", ((IMAPMailFolder) f).containsNonExistent());
				}
				assertTrue("Missing own permission", f.containsOwnPermission());
				assertTrue("Missing parent fullname", f.containsParentFullname());
				assertTrue("Missing permissions", f.containsPermissions());
				assertTrue("Missing root folder flag", f.containsRootFolder());
				assertTrue("Missing separator flag", f.containsSeparator());
				assertTrue("Missing subfolder flag", f.containsSubfolders());
				assertTrue("Missing subscribed flag", f.containsSubscribed());
				assertTrue("Missing subscribed subfolders flag", f.containsSubscribedSubfolders());
				assertTrue("Missing supports user flags flag", f.containsSupportsUserFlags());
				assertTrue("Missing unread message count", f.containsUnreadMessageCount());

				String parentFullname = null;
				{
					final MailFolder inbox = mailAccess.getFolderStorage().getFolder(INBOX);
					String name = "TemporaryFolder" + UUID.randomUUID().toString().substring(0, 8);
                    if (inbox.isHoldsFolders()) {
						fullname = new StringBuilder(inbox.getFullname()).append(inbox.getSeparator()).append(name).toString();
						parentFullname = INBOX;
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

				boolean found = false;
				MailFolder[] folders = mailAccess.getFolderStorage().getSubfolders(parentFullname, true);
				for (int i = 0; i < folders.length; i++) {
					final MailFolder mf = folders[i];
					assertTrue("Missing default folder flag", mf.containsDefaultFolder());
					assertTrue("Missing deleted count", mf.containsDeletedMessageCount());
					assertTrue("Missing exists flag", mf.containsExists());
					assertTrue("Missing fullname", mf.containsFullname());
					assertTrue("Missing holds folders flag", mf.containsHoldsFolders());
					assertTrue("Missing holds messages flag", mf.containsHoldsMessages());
					assertTrue("Missing message count", mf.containsMessageCount());
					assertTrue("Missing name", mf.containsName());
					assertTrue("Missing new message count", mf.containsNewMessageCount());
					if (mf instanceof IMAPMailFolder) {
						assertTrue("Missing non-existent flag", ((IMAPMailFolder) mf).containsNonExistent());
					}
					assertTrue("Missing own permission", mf.containsOwnPermission());
					assertTrue("Missing parent fullname", mf.containsParentFullname());
					assertTrue("Missing permissions", mf.containsPermissions());
					assertTrue("Missing root folder flag", mf.containsRootFolder());
					assertTrue("Missing separator flag", mf.containsSeparator());
					assertTrue("Missing subfolder flag", mf.containsSubfolders());
					assertTrue("Missing subscribed flag", mf.containsSubscribed());
					assertTrue("Missing subscribed subfolders flag", mf.containsSubscribedSubfolders());
					assertTrue("Missing supports user flags flag", mf.containsSupportsUserFlags());
					assertTrue("Missing unread message count", mf.containsUnreadMessageCount());
					if (fullname.equals(mf.getFullname())) {
						found = true;
						assertFalse("Subscribed, but shouldn't be", MailProperties.getInstance().isSupportSubscription() ? mf
								.isSubscribed() : false);
					}
				}
				assertTrue("Newly created subfolder not found!", found);

				if (MailProperties.getInstance().isSupportSubscription()) {
					found = false;
					folders = mailAccess.getFolderStorage().getSubfolders(parentFullname, false);
					for (final MailFolder mailFolder : folders) {
						found |= (fullname.equals(mailFolder.getFullname()));
					}
					assertFalse("Unsubscribed subfolder listed as subscribed!", found);
				}

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

	public void testFolderRoot() {
		try {
			final SessionObject session = getSession();

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();
			try {
				final MailFolder f = mailAccess.getFolderStorage().getRootFolder();
				assertTrue("Missing default folder flag", f.containsDefaultFolder());
				assertTrue("Missing deleted count", f.containsDeletedMessageCount());
				assertTrue("Missing exists flag", f.containsExists());
				assertTrue("Missing fullname", f.containsFullname());
				assertTrue("Missing holds folders flag", f.containsHoldsFolders());
				assertTrue("Missing holds messages flag", f.containsHoldsMessages());
				assertTrue("Missing message count", f.containsMessageCount());
				assertTrue("Missing name", f.containsName());
				assertTrue("Missing new message count", f.containsNewMessageCount());
				if (f instanceof IMAPMailFolder) {
					assertTrue("Missing non-existent flag", ((IMAPMailFolder) f).containsNonExistent());
				}
				assertTrue("Missing own permission", f.containsOwnPermission());
				assertTrue("Missing parent fullname", f.containsParentFullname());
				assertTrue("Missing permissions", f.containsPermissions());
				assertTrue("Missing root folder flag", f.containsRootFolder());
				assertTrue("Missing separator flag", f.containsSeparator());
				assertTrue("Missing subfolder flag", f.containsSubfolders());
				assertTrue("Missing subscribed flag", f.containsSubscribed());
				assertTrue("Missing subscribed subfolders flag", f.containsSubscribedSubfolders());
				assertTrue("Missing supports user flags flag", f.containsSupportsUserFlags());
				assertTrue("Missing unread message count", f.containsUnreadMessageCount());

				assertTrue("Unexpected root fullname", MailFolder.DEFAULT_FOLDER_ID.equals(f.getFullname()));
				assertTrue("Unexpected root's parent fullname", f.getParentFullname() == null);

			} finally {
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

	public void testFolderUpdate() throws OXException {
			final SessionObject session = getSession();

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();

			String fullname = null;
			try {

				String parentFullname = null;
				{
					final MailFolder inbox = mailAccess.getFolderStorage().getFolder(INBOX);
					String name = "TemporaryFolder" + UUID.randomUUID().toString().substring(0, 8);
                    if (inbox.isHoldsFolders()) {
						fullname = new StringBuilder(inbox.getFullname()).append(inbox.getSeparator()).append(name).toString();
						parentFullname = INBOX;
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

				if (MailProperties.getInstance().isSupportSubscription() && !MailProperties.getInstance().isIgnoreSubscription()) {
					final MailFolderDescription mfd = new MailFolderDescription();
					mfd.setSubscribed(true);
					mailAccess.getFolderStorage().updateFolder(fullname, mfd);

					assertTrue("Could not be subscribed", mailAccess.getFolderStorage().getFolder(fullname)
							.isSubscribed());
				}

				final MailFolderDescription mfd = new MailFolderDescription();
				final MailPermission p1 = MailProviderRegistry.getMailProviderBySession(session, MailAccount.DEFAULT_ID)
						.createNewMailPermission(session, MailAccount.DEFAULT_ID);
				p1.setEntity(getUser());
				p1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
						OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
				p1.setFolderAdmin(true);
				p1.setGroupPermission(false);
				mfd.addPermission(p1);
				final MailPermission p2 = MailProviderRegistry.getMailProviderBySession(session, MailAccount.DEFAULT_ID)
						.createNewMailPermission(session, MailAccount.DEFAULT_ID);
				p2.setEntity(getSecondUser());
				p2.setAllPermission(OCLPermission.READ_FOLDER, OCLPermission.ADMIN_PERMISSION,
						OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
				p2.setFolderAdmin(false);
				p2.setGroupPermission(false);
				mfd.addPermission(p2);
				assertTrue("Hugh... No permissions?!", mfd.containsPermissions());
				mailAccess.getFolderStorage().updateFolder(fullname, mfd);

				final MailFolder updatedFolder = mailAccess.getFolderStorage().getFolder(fullname);
				final OCLPermission[] perms = updatedFolder.getPermissions();
				if (mailAccess.getMailConfig().getCapabilities().hasPermissions()) {

					assertTrue("Unexpected number of permissions: " + perms.length, perms.length == 2);

					for (final OCLPermission permission : perms) {
						if (permission.getEntity() == getUser()) {
							assertTrue(permission.getFolderPermission() >= OCLPermission.CREATE_SUB_FOLDERS);
							assertTrue(permission.getReadPermission() >= OCLPermission.READ_ALL_OBJECTS);
							assertTrue(permission.getWritePermission() >= OCLPermission.WRITE_ALL_OBJECTS);
							assertTrue(permission.getDeletePermission() >= OCLPermission.DELETE_ALL_OBJECTS);
							assertTrue(permission.isFolderAdmin());
						} else if (permission.getEntity() == getSecondUser()) {
							assertTrue(permission.getFolderPermission() == OCLPermission.READ_FOLDER);
							assertTrue(permission.getReadPermission() >= OCLPermission.READ_ALL_OBJECTS);
							assertTrue(permission.getWritePermission() == OCLPermission.NO_PERMISSIONS);
							assertTrue(permission.getDeletePermission() == OCLPermission.NO_PERMISSIONS);
							assertFalse(permission.isFolderAdmin());
						}
					}
				} else {
					assertTrue("No default permission set!" + perms.length, perms.length == 1
							&& DefaultMailPermission.class.isInstance(perms[0]));
				}
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

	public void testFolderMove() throws OXException {
			final SessionObject session = getSession();

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();

			String fullname = null;
			try {

				String parentFullname = null;
				char separator = '\0';
				final boolean parentIsDefault;
				{
					final MailFolder inbox = mailAccess.getFolderStorage().getFolder(INBOX);
					String name = "TemporaryFolder" + UUID.randomUUID().toString().substring(0, 8);
                    if (inbox.isHoldsFolders()) {
						fullname = new StringBuilder(inbox.getFullname()).append(inbox.getSeparator()).append(name).toString();
						parentFullname = INBOX;
						parentIsDefault = false;
					} else {
						fullname = name;
						parentFullname = MailFolder.DEFAULT_FOLDER_ID;
						parentIsDefault = true;
					}

					final MailFolderDescription mfd = new MailFolderDescription();
					mfd.setExists(false);
					mfd.setParentFullname(parentFullname);
					mfd.setSeparator((separator = inbox.getSeparator()));
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

				String stamp = Long.toString(System.currentTimeMillis());
                String newFullname = parentIsDefault ? ("TemporaryFolderMoved" + stamp) : new StringBuilder(parentFullname)
						.append(separator).append("TemporaryFolderMoved").append(stamp).toString();
				// MOVE FOLDER
				mailAccess.getFolderStorage().moveFolder(fullname, newFullname);
				// ENSURE OLD ONE DOES NO MORE EXIST
				Exception exc = null;
				try {
					mailAccess.getFolderStorage().getFolder(fullname);
				} catch (final OXException e) {
					exc = e;
				}
				assertTrue("Moved folder still exists", exc != null);

				fullname = newFullname;
				MailFolder mf = mailAccess.getFolderStorage().getFolder(fullname);

				assertEquals("Unexpected name: " + mf.getName(), ("TemporaryFolderMoved" + stamp), mf.getName());
				assertEquals("Unexpected parent: " + mf.getParentFullname(), parentFullname, mf.getParentFullname());

				/*
				 * Move below root folder
				 */
				final MailFolder root = mailAccess.getFolderStorage().getRootFolder();
				final OCLPermission[] rootPerms = root.getPermissions();

				if (canCreateSubfolders(rootPerms)) {
					newFullname = "TemporaryFolderMovedAgain" + stamp;
					exc = null;
					try {
						mailAccess.getFolderStorage().moveFolder(fullname, newFullname);
					} catch (final OXException e) {
						exc = e;
					}
					assertTrue("Move below root folder failed", exc == null);
					exc = null;
					try {
						mailAccess.getFolderStorage().getFolder(fullname);
					} catch (final OXException e) {
						exc = e;
					}
					assertTrue("Moved folder still exists", exc != null);
					fullname = newFullname;
					mf = mailAccess.getFolderStorage().getFolder(fullname);

					assertTrue("Unexpected name: " + mf.getName(), ("TemporaryFolderMovedAgain" + stamp).equals(mf.getName()));
					assertTrue("Unexpected parent: " + mf.getParentFullname(), MailFolder.DEFAULT_FOLDER_ID.equals(mf.getParentFullname()));
				} else {
				    fullname = null;
				}

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

	private boolean canCreateSubfolders(final OCLPermission[] perms) {
		if (null == perms) {
			return false;
		}
		for (final OCLPermission permission : perms) {
			if (permission.getEntity() == getUser()) {
				return permission.getFolderPermission() >= OCLPermission.CREATE_SUB_FOLDERS;
			}
		}
		return false;
	}

	public void testFolderRename() throws OXException {
			final SessionObject session = getSession();

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();

			String fullname = null;
			try {

				String parentFullname = null;
				char separator = '\0';
				final boolean isParentDefault;
				{
					final MailFolder inbox = mailAccess.getFolderStorage().getFolder(INBOX);
					String name = "TemporaryFolder" + UUID.randomUUID().toString().substring(0, 8);
                    if (inbox.isHoldsFolders()) {
						fullname = new StringBuilder(inbox.getFullname()).append(inbox.getSeparator()).append(name).toString();
						parentFullname = INBOX;
						isParentDefault = false;
					} else {
						fullname = name;
						parentFullname = MailFolder.DEFAULT_FOLDER_ID;
						isParentDefault = true;
					}

					final MailFolderDescription mfd = new MailFolderDescription();
					mfd.setExists(false);
					mfd.setParentFullname(parentFullname);
					mfd.setSeparator((separator = inbox.getSeparator()));
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

				mailAccess.getFolderStorage().renameFolder(fullname, "TemporaryFolderRenamed");

				Exception exc = null;
				try {
					mailAccess.getFolderStorage().getFolder(fullname);
				} catch (final OXException e) {
					exc = e;
				}
				assertTrue("Renamed folder still exists", exc != null);

				fullname = isParentDefault ? "TemporaryFolderRenamed" : new StringBuilder(parentFullname).append(
						separator).append("TemporaryFolderRenamed").toString();
				final MailFolder mf = mailAccess.getFolderStorage().getFolder(fullname);

				assertEquals("Unexpected name: " + mf.getName(), "TemporaryFolderRenamed", mf.getName());
				assertEquals("Unexpected parent: " + mf.getParentFullname(), parentFullname, mf.getParentFullname());

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

	public void testFolderDelete() throws OXException {
			final SessionObject session = getSession();

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();

			String fullname = null;
			try {
				final String name = "TemporaryFolder" + UUID.randomUUID().toString().substring(0, 8);

				String parentFullname = null;
				{
					final MailFolder inbox = mailAccess.getFolderStorage().getFolder(INBOX);
					if (inbox.isHoldsFolders()) {
						fullname = new StringBuilder(inbox.getFullname()).append(inbox.getSeparator()).append(name)
								.toString();
						parentFullname = INBOX;
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

				int numAppendix = mailAccess.getFolderStorage().getFolder(
						mailAccess.getFolderStorage().getTrashFolder()).isHoldsFolders() ? 0 : -1;
				if (numAppendix == 0) {
					final MailFolder[] trashedFolders = mailAccess.getFolderStorage().getSubfolders(
							mailAccess.getFolderStorage().getTrashFolder(), true);
					for (final MailFolder mailFolder : trashedFolders) {
						if (mailFolder.getName().startsWith(name)) {
							final String substr = mailFolder.getName().substring(name.length());
							if (substr.length() > 0) {
								final int tmp = numAppendix;
								try {
									numAppendix = Math.max(numAppendix, Integer.parseInt(substr));
								} catch (final NumberFormatException e) {
									// ignore
									numAppendix = tmp;
								}
							}
						}
					}
					if (numAppendix > 0) {
						numAppendix++;
					}
				}

				mailAccess.getFolderStorage().deleteFolder(fullname);

				Exception exc = null;
				try {
					mailAccess.getFolderStorage().getFolder(fullname);
				} catch (final OXException e) {
					exc = e;
				}
				assertTrue("Deleted folder still exists", exc != null);

				if (numAppendix >= 0) {
					boolean found = false;
					/*
					 * Find backup folder below trash folder
					 */
					final MailFolder[] trashedFolders = mailAccess.getFolderStorage().getSubfolders(
							mailAccess.getFolderStorage().getTrashFolder(), true);
					for (int i = 0; i < trashedFolders.length && !found; i++) {
						if (trashedFolders[i].getName().startsWith(name)) {
							if (numAppendix == 0) {
								found = trashedFolders[i].getName().equals(name);
							} else {
								final String substr = trashedFolders[i].getName().substring(name.length());
								try {
									found = (numAppendix == Integer.parseInt(substr));
								} catch (final NumberFormatException e) {
									// ignore
								}
							}
							if (found) {
								fullname = trashedFolders[i].getFullname();
							}
						}
					}
					if (!found) {
						fullname = null;
					}
					assertTrue("No backup folder found below trash folder", found);
				}

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

	private static final MailField[] FIELDS_ID = { MailField.ID };

	public void testFolderClear() throws OXException, MessagingException, IOException {
			final SessionObject session = getSession();

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();

			String fullname = null;
			String[] trashedIDs = null;
			String trashFullname = null;
			try {
				final String name = "TemporaryFolder" + UUID.randomUUID().toString().substring(0, 8);

				{
					final MailFolder inbox = mailAccess.getFolderStorage().getFolder(INBOX);
					final String parentFullname;
					if (inbox.isHoldsFolders()) {
						fullname = new StringBuilder(inbox.getFullname()).append(inbox.getSeparator()).append(name)
								.toString();
						parentFullname = INBOX;
					} else {
						fullname = name;
						parentFullname = MailFolder.DEFAULT_FOLDER_ID;
					}

					if (mailAccess.getFolderStorage().exists(fullname)) {
					    mailAccess.getFolderStorage().clearFolder(fullname, true);
					} else {
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
				}

				final MailMessage[] mails = getMessages(getTestMailDir(), -1);
				String[] uids = mailAccess.getMessageStorage().appendMessages(fullname, mails);

				final MailFolder f = mailAccess.getFolderStorage().getFolder(fullname);
				final int messageCount = f.getMessageCount();
				if (messageCount > 0) {
                    assertTrue("Messages not completely appended to mail folder " + fullname + ": " + messageCount + " < " + uids.length, messageCount >= uids.length);
                }

				trashFullname = mailAccess.getFolderStorage().getTrashFolder();
				int numTrashedMails = getMessageCount(mailAccess, trashFullname);
				final Set<String> ids = new HashSet<String>(numTrashedMails);
				MailMessage[] trashed = mailAccess.getMessageStorage().getAllMessages(trashFullname, IndexRange.NULL,
						MailSortField.RECEIVED_DATE, OrderDirection.ASC, FIELDS_ID);
				for (int i = 0; i < trashed.length; i++) {
					ids.add(trashed[i].getMailId());
				}

				mailAccess.getFolderStorage().clearFolder(fullname);

				assertTrue("Folder should be empty", getMessageCount(mailAccess, fullname) == 0);
				final int expectedMsgCount = numTrashedMails + uids.length;
				assertTrue("Mails not completely backuped", getMessageCount(mailAccess, trashFullname) == expectedMsgCount);

				final Set<String> newIds = new HashSet<String>(expectedMsgCount);
				trashed = mailAccess.getMessageStorage().getAllMessages(trashFullname, IndexRange.NULL,
						MailSortField.RECEIVED_DATE, OrderDirection.ASC, FIELDS_ID);
				assertTrue("Size mismatch: " + trashed.length + " but should be " + expectedMsgCount,
						trashed.length == expectedMsgCount);
				for (int i = 0; i < trashed.length; i++) {
					newIds.add(trashed[i].getMailId());
				}
				newIds.removeAll(ids);
				trashedIDs = new String[newIds.size()];
				int i = 0;
				for (final String id : newIds) {
					trashedIDs[i++] = id;
				}
				mailAccess.getMessageStorage().deleteMessages(trashFullname, trashedIDs, true);
				trashedIDs = null;

				uids = mailAccess.getMessageStorage().appendMessages(fullname, mails);
				//f = mailAccess.getFolderStorage().getFolder(fullname);
				assertTrue("Messages not completely appended to mail folder " + fullname,
						getMessageCount(mailAccess, fullname) == uids.length);

				numTrashedMails = getMessageCount(mailAccess, trashFullname);

				mailAccess.getFolderStorage().clearFolder(fullname, true);

				assertTrue("Folder should be empty", getMessageCount(mailAccess, fullname) == 0);
				assertTrue("Mails not deleted permanently although hardDelete flag set to true", getMessageCount(mailAccess, trashFullname) == numTrashedMails);

			} finally {
				if (fullname != null) {
					mailAccess.getFolderStorage().deleteFolder(fullname, true);
				}

				if (trashedIDs != null) {
					mailAccess.getMessageStorage().deleteMessages(trashFullname, trashedIDs, true);
				}

				/*
				 * close
				 */
				mailAccess.close(false);
			}
	}

	public void testFolderQuota() throws OXException, MessagingException, IOException {
			final SessionObject session = getSession();

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();

			final MailCapabilities caps = mailAccess.getMailConfig().getCapabilities();
			if (!caps.hasQuota()) {
				return;
			}

			String fullname = null;
			try {
				final String name = "TemporaryFolder" + UUID.randomUUID().toString().substring(0, 8);

				{
					final MailFolder inbox = mailAccess.getFolderStorage().getFolder(INBOX);
					final String parentFullname;
					if (inbox.isHoldsFolders()) {
						fullname = new StringBuilder(inbox.getFullname()).append(inbox.getSeparator()).append(name)
								.toString();
						parentFullname = INBOX;
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
				final Quota.Type[] types = new Quota.Type[] { Quota.Type.STORAGE };
				final long prevUsage = mailAccess.getFolderStorage().getQuotas(fullname, types)[0].getUsage();
				if (prevUsage == Quota.UNLIMITED) {
					return;
				}

				mailAccess.getMessageStorage().appendMessages(fullname, getMessages(getTestMailDir(), -1));
				assertTrue("QUOTA not increased although mails were appended", mailAccess.getFolderStorage().getQuotas(
						fullname, types)[0].getUsage() > prevUsage);

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

	public void testPath2DefaultFolder() throws OXException {
			final SessionObject session = getSession();

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();
			String fullname = null;
			String anotherFullname = null;
			try {
				final String name = "TemporaryFolder" + UUID.randomUUID().toString().substring(0, 8);

				final boolean isParentDefault;
				{
					final MailFolder inbox = mailAccess.getFolderStorage().getFolder(INBOX);
					final String parentFullname;
					if (inbox.isHoldsFolders()) {
						fullname = new StringBuilder(inbox.getFullname()).append(inbox.getSeparator()).append(name)
								.toString();
						parentFullname = INBOX;
						isParentDefault = false;
					} else {
						fullname = name;
						parentFullname = MailFolder.DEFAULT_FOLDER_ID;
						isParentDefault = true;
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

				MailFolder[] path = mailAccess.getFolderStorage().getPath2DefaultFolder(fullname);
				assertEquals("Unexpected path length: " + path.length, (isParentDefault ? 1 : 2), path.length);

				for (int i = 0; i < path.length; i++) {
					if (i == 0) {
						assertTrue("Unexpected mail folder in first path position", path[i].getFullname().equals(
								fullname));
					} else {
						assertTrue("Unexpected mail folder in second path position", path[i].getFullname()
								.equals(INBOX));
					}
				}
				path = null;

				{
					final MailFolder temp = mailAccess.getFolderStorage().getFolder(fullname);
					final String parentFullname;
					if (temp.isHoldsFolders()) {
						anotherFullname = new StringBuilder(temp.getFullname()).append(temp.getSeparator())
								.append(name).toString();
						parentFullname = temp.getFullname();

						final MailFolderDescription mfd = new MailFolderDescription();
						mfd.setExists(false);
						mfd.setParentFullname(parentFullname);
						mfd.setSeparator(temp.getSeparator());
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

						final MailFolder[] apath = mailAccess.getFolderStorage().getPath2DefaultFolder(anotherFullname);
						assertTrue("Unexpected path length: " + apath.length, apath.length == (isParentDefault ? 2 : 3));

						for (int i = 0; i < apath.length; i++) {
							if (i == 0) {
								assertTrue("Unexpected mail folder in first path position", apath[i].getFullname()
										.equals(anotherFullname));
							} else if (i == 1) {
								assertTrue("Unexpected mail folder in first path position", apath[i].getFullname()
										.equals(fullname));
							} else {
								assertTrue("Unexpected mail folder in second path position", apath[i].getFullname()
										.equals(INBOX));
							}
						}
					}

				}

			} finally {
				if (anotherFullname != null) {
					mailAccess.getFolderStorage().deleteFolder(anotherFullname, true);
				}

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
