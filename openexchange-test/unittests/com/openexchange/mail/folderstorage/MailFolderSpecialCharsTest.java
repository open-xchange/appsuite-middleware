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

import com.openexchange.exception.OXException;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.sessiond.impl.SessionObject;

/**
 * {@link MailFolderSpecialCharsTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class MailFolderSpecialCharsTest extends AbstractMailTest {

	private static final String INBOX = "INBOX";

	/**
	 *
	 */
	public MailFolderSpecialCharsTest() {
		super();
	}

	/**
	 * @param name
	 */
	public MailFolderSpecialCharsTest(final String name) {
		super(name);
	}

	public void testFolderCreateAndSubfolders() throws OXException {
			final SessionObject session = getSession();

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();

			String fullname = null;
			try {

				String parentFullname = null;
				final MailFolder inbox = mailAccess.getFolderStorage().getFolder(INBOX);
				final String invalidName = "Foo" + inbox.getSeparator() + "Bar";
				if (inbox.isHoldsFolders()) {
					fullname = new StringBuilder(inbox.getFullname()).append(inbox.getSeparator()).append(invalidName)
							.toString();
					parentFullname = INBOX;
				} else {
					fullname = invalidName;
					parentFullname = MailFolder.DEFAULT_FOLDER_ID;
				}

				final MailFolderDescription mfd = new MailFolderDescription();
				mfd.setExists(false);
				mfd.setParentFullname(parentFullname);
				mfd.setSeparator(inbox.getSeparator());
				mfd.setSubscribed(false);
				mfd.setName(invalidName);

				final MailPermission p = MailProviderRegistry.getMailProviderBySession(session, MailAccount.DEFAULT_ID)
						.createNewMailPermission(session, MailAccount.DEFAULT_ID);
				p.setEntity(getUser());
				p.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
						OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
				p.setFolderAdmin(true);
				p.setGroupPermission(false);
				mfd.addPermission(p);
				OXException me = null;
				try {
					mailAccess.getFolderStorage().createFolder(mfd);
				} catch (final OXException e) {
					me = e;
				}
				assertTrue("Folder created although an invalid name was specified", me != null);

				String validName = "Foo&Bar";
				if (inbox.isHoldsFolders()) {
					fullname = new StringBuilder(inbox.getFullname()).append(inbox.getSeparator()).append(validName)
							.toString();
					parentFullname = INBOX;
				} else {
					fullname = validName;
					parentFullname = MailFolder.DEFAULT_FOLDER_ID;
				}
				mfd.setExists(false);
				mfd.setParentFullname(parentFullname);
				mfd.setSeparator(inbox.getSeparator());
				mfd.setSubscribed(false);
				mfd.setName(validName);
				mailAccess.getFolderStorage().createFolder(mfd);
				assertTrue(mailAccess.getFolderStorage().getFolder(fullname).getName().equals(validName));
				if (fullname != null) {
					mailAccess.getFolderStorage().deleteFolder(fullname, true);
				}

				validName = "Foo 1 Bar";
				if (inbox.isHoldsFolders()) {
					fullname = new StringBuilder(inbox.getFullname()).append(inbox.getSeparator()).append(validName)
							.toString();
					parentFullname = INBOX;
				} else {
					fullname = validName;
					parentFullname = MailFolder.DEFAULT_FOLDER_ID;
				}
				mfd.setExists(false);
				mfd.setParentFullname(parentFullname);
				mfd.setSeparator(inbox.getSeparator());
				mfd.setSubscribed(false);
				mfd.setName(validName);
				mailAccess.getFolderStorage().createFolder(mfd);
				assertTrue(mailAccess.getFolderStorage().getFolder(fullname).getName().equals(validName));
				if (fullname != null) {
					mailAccess.getFolderStorage().deleteFolder(fullname, true);
				}

				validName = "1 und 2";
				if (inbox.isHoldsFolders()) {
					fullname = new StringBuilder(inbox.getFullname()).append(inbox.getSeparator()).append(validName)
							.toString();
					parentFullname = INBOX;
				} else {
					fullname = validName;
					parentFullname = MailFolder.DEFAULT_FOLDER_ID;
				}
				mfd.setExists(false);
				mfd.setParentFullname(parentFullname);
				mfd.setSeparator(inbox.getSeparator());
				mfd.setSubscribed(false);
				mfd.setName(validName);
				mailAccess.getFolderStorage().createFolder(mfd);
				assertTrue(mailAccess.getFolderStorage().getFolder(fullname).getName().equals(validName));

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

	public void testFailIfSeparatorContained() throws OXException {
			final SessionObject session = getSession();

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();

			String fullname = null;
			try {

				String parentFullname = null;
				final MailFolder inbox = mailAccess.getFolderStorage().getFolder(INBOX);
				String invalidName = "Foo" + inbox.getSeparator() + "Bar";
				if (inbox.isHoldsFolders()) {
					fullname = new StringBuilder(inbox.getFullname()).append(inbox.getSeparator()).append(invalidName)
							.toString();
					parentFullname = INBOX;
				} else {
					fullname = invalidName;
					parentFullname = MailFolder.DEFAULT_FOLDER_ID;
				}

				final MailFolderDescription mfd = new MailFolderDescription();
				mfd.setExists(false);
				mfd.setParentFullname(parentFullname);
				mfd.setSeparator(inbox.getSeparator());
				mfd.setSubscribed(false);
				mfd.setName(invalidName);

				final MailPermission p = MailProviderRegistry.getMailProviderBySession(session, MailAccount.DEFAULT_ID)
						.createNewMailPermission(session, MailAccount.DEFAULT_ID);
				p.setEntity(getUser());
				p.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
						OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
				p.setFolderAdmin(true);
				p.setGroupPermission(false);
				mfd.addPermission(p);
				OXException me = null;
				try {
					mailAccess.getFolderStorage().createFolder(mfd);
				} catch (final OXException e) {
					me = e;
					fullname = null;
				}
				assertTrue("Folder created although an invalid name was specified", me != null);

				invalidName = inbox.getSeparator() + "Foobar";
				if (inbox.isHoldsFolders()) {
					fullname = new StringBuilder(inbox.getFullname()).append(inbox.getSeparator()).append(invalidName)
							.toString();
					parentFullname = INBOX;
				} else {
					fullname = invalidName;
					parentFullname = MailFolder.DEFAULT_FOLDER_ID;
				}
				mfd.setExists(false);
				mfd.setParentFullname(parentFullname);
				mfd.setSeparator(inbox.getSeparator());
				mfd.setSubscribed(false);
				mfd.setName(invalidName);
				me = null;
				try {
					mailAccess.getFolderStorage().createFolder(mfd);
				} catch (final OXException e) {
					me = e;
					fullname = null;
				}
				assertTrue("Folder created although an invalid name was specified", me != null);

				invalidName = "Foobar" + inbox.getSeparator();
				if (inbox.isHoldsFolders()) {
					fullname = new StringBuilder(inbox.getFullname()).append(inbox.getSeparator()).append(invalidName)
							.toString();
					parentFullname = INBOX;
				} else {
					fullname = invalidName;
					parentFullname = MailFolder.DEFAULT_FOLDER_ID;
				}
				mfd.setExists(false);
				mfd.setParentFullname(parentFullname);
				mfd.setSeparator(inbox.getSeparator());
				mfd.setSubscribed(false);
				mfd.setName(invalidName);
				me = null;
				try {
					mailAccess.getFolderStorage().createFolder(mfd);
				} catch (final OXException e) {
					me = e;
					fullname = null;
				}
				assertTrue("Folder created although an invalid name was specified", me != null);

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
