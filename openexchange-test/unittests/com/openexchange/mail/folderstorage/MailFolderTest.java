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
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
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

}
