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

package com.openexchange.mail.folderstorage;

import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;

/**
 * {@link MailFolderTest}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MailFolderTest extends AbstractMailTest {

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
			final SessionObject session = SessionObjectWrapper.createSessionObject(getUser(),
					new ContextImpl(getCid()), "mail-test-session");
			session.setPassword(getPassword());

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
			fail(e.getLocalizedMessage());
		}
	}

	public void testExistStandardFolders() {
		try {
			final SessionObject session = SessionObjectWrapper.createSessionObject(getUser(),
					new ContextImpl(getCid()), "mail-test-session");
			session.setPassword(getPassword());

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
			fail(e.getLocalizedMessage());
		}
	}

	public void testFolderGet() {
		try {
			final SessionObject session = SessionObjectWrapper.createSessionObject(getUser(),
					new ContextImpl(getCid()), "mail-test-session");
			session.setPassword(getPassword());

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();
			try {
				final MailFolder f = mailAccess.getFolderStorage().getFolder("INBOX");
				assertTrue("Missing default folder flag", f.containsDefaulFolder());
				assertTrue("Missing deleted count", f.containsDeletedMessageCount());
				assertTrue("Missing exists flag", f.containsExists());
				assertTrue("Missing fullname", f.containsFullname());
				assertTrue("Missing holds folders flag", f.containsHoldsFolders());
				assertTrue("Missing holds messages flag", f.containsHoldsMessages());
				assertTrue("Missing message count", f.containsMessageCount());
				assertTrue("Missing name", f.containsName());
				assertTrue("Missing new message count", f.containsNewMessageCount());
				assertTrue("Missing non-existent flag", f.containsNonExistent());
				assertTrue("Missing own permission", f.containsOwnPermission());
				assertTrue("Missing parent fullname", f.containsParentFullname());
				assertTrue("Missing permissions", f.containsPermissions());
				assertTrue("Missing root folder flag", f.containsRootFolder());
				assertTrue("Missing separator flag", f.containsSeparator());
				assertTrue("Missing subfolder flag", f.containsSubfolders());
				assertTrue("Missing subscribed flag", f.containsSubscribed());
				assertTrue("Missing subscribed subfolders flag", f.containsSubscribedSubfolders());
				assertTrue("Missing summary", f.containsSummary());
				assertTrue("Missing supports user flags flag", f.containsSupportsUserFlags());
				assertTrue("Missing unread message count", f.containsUnreadMessageCount());

			} finally {
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

	public void testFolderSubfolders() {
		try {
			final SessionObject session = SessionObjectWrapper.createSessionObject(getUser(),
					new ContextImpl(getCid()), "mail-test-session");
			session.setPassword(getPassword());

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();

			String fullname = null;
			try {
				final MailFolder f = mailAccess.getFolderStorage().getFolder("INBOX");
				assertTrue("Missing default folder flag", f.containsDefaulFolder());
				assertTrue("Missing deleted count", f.containsDeletedMessageCount());
				assertTrue("Missing exists flag", f.containsExists());
				assertTrue("Missing fullname", f.containsFullname());
				assertTrue("Missing holds folders flag", f.containsHoldsFolders());
				assertTrue("Missing holds messages flag", f.containsHoldsMessages());
				assertTrue("Missing message count", f.containsMessageCount());
				assertTrue("Missing name", f.containsName());
				assertTrue("Missing new message count", f.containsNewMessageCount());
				assertTrue("Missing non-existent flag", f.containsNonExistent());
				assertTrue("Missing own permission", f.containsOwnPermission());
				assertTrue("Missing parent fullname", f.containsParentFullname());
				assertTrue("Missing permissions", f.containsPermissions());
				assertTrue("Missing root folder flag", f.containsRootFolder());
				assertTrue("Missing separator flag", f.containsSeparator());
				assertTrue("Missing subfolder flag", f.containsSubfolders());
				assertTrue("Missing subscribed flag", f.containsSubscribed());
				assertTrue("Missing subscribed subfolders flag", f.containsSubscribedSubfolders());
				assertTrue("Missing summary", f.containsSummary());
				assertTrue("Missing supports user flags flag", f.containsSupportsUserFlags());
				assertTrue("Missing unread message count", f.containsUnreadMessageCount());

				String parentFullname = null;
				{
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
					mfd.setSubscribed(false);
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

				boolean found = false;
				MailFolder[] folders = mailAccess.getFolderStorage().getSubfolders(parentFullname, true);
				for (int i = 0; i < folders.length; i++) {
					final MailFolder mf = folders[i];
					assertTrue("Missing default folder flag", mf.containsDefaulFolder());
					assertTrue("Missing deleted count", mf.containsDeletedMessageCount());
					assertTrue("Missing exists flag", mf.containsExists());
					assertTrue("Missing fullname", mf.containsFullname());
					assertTrue("Missing holds folders flag", mf.containsHoldsFolders());
					assertTrue("Missing holds messages flag", mf.containsHoldsMessages());
					assertTrue("Missing message count", mf.containsMessageCount());
					assertTrue("Missing name", mf.containsName());
					assertTrue("Missing new message count", mf.containsNewMessageCount());
					assertTrue("Missing non-existent flag", mf.containsNonExistent());
					assertTrue("Missing own permission", mf.containsOwnPermission());
					assertTrue("Missing parent fullname", mf.containsParentFullname());
					assertTrue("Missing permissions", mf.containsPermissions());
					assertTrue("Missing root folder flag", mf.containsRootFolder());
					assertTrue("Missing separator flag", mf.containsSeparator());
					assertTrue("Missing subfolder flag", mf.containsSubfolders());
					assertTrue("Missing subscribed flag", mf.containsSubscribed());
					assertTrue("Missing subscribed subfolders flag", mf.containsSubscribedSubfolders());
					assertTrue("Missing summary", mf.containsSummary());
					assertTrue("Missing supports user flags flag", mf.containsSupportsUserFlags());
					assertTrue("Missing unread message count", mf.containsUnreadMessageCount());
					if (fullname.equals(mf.getFullname())) {
						found = true;
						assertFalse("Subscribed, but shouldn't be", MailConfig.isSupportSubscription() ? mf
								.isSubscribed() : false);
					}
				}
				assertTrue("Newly created subfolder not found!", found);

				if (MailConfig.isSupportSubscription()) {
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
					System.out.println("Temporary folder deleted");
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

	public void testFolderRoot() {
		try {
			final SessionObject session = SessionObjectWrapper.createSessionObject(getUser(),
					new ContextImpl(getCid()), "mail-test-session");
			session.setPassword(getPassword());

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();
			try {
				final MailFolder f = mailAccess.getFolderStorage().getRootFolder();
				assertTrue("Missing default folder flag", f.containsDefaulFolder());
				assertTrue("Missing deleted count", f.containsDeletedMessageCount());
				assertTrue("Missing exists flag", f.containsExists());
				assertTrue("Missing fullname", f.containsFullname());
				assertTrue("Missing holds folders flag", f.containsHoldsFolders());
				assertTrue("Missing holds messages flag", f.containsHoldsMessages());
				assertTrue("Missing message count", f.containsMessageCount());
				assertTrue("Missing name", f.containsName());
				assertTrue("Missing new message count", f.containsNewMessageCount());
				assertTrue("Missing non-existent flag", f.containsNonExistent());
				assertTrue("Missing own permission", f.containsOwnPermission());
				assertTrue("Missing parent fullname", f.containsParentFullname());
				assertTrue("Missing permissions", f.containsPermissions());
				assertTrue("Missing root folder flag", f.containsRootFolder());
				assertTrue("Missing separator flag", f.containsSeparator());
				assertTrue("Missing subfolder flag", f.containsSubfolders());
				assertTrue("Missing subscribed flag", f.containsSubscribed());
				assertTrue("Missing subscribed subfolders flag", f.containsSubscribedSubfolders());
				assertTrue("Missing summary", f.containsSummary());
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
			fail(e.getLocalizedMessage());
		}
	}
}
