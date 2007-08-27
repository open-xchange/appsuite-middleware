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

package com.openexchange.mail;

import com.openexchange.groupware.contexts.ContextImpl;
import com.openexchange.imap.IMAPProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessionObjectWrapper;

/**
 * {@link MailFolderTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class MailFolderTest extends AbstractMailTest {

	/**
	 * Default constructor
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

	public void testGetINBOXFolder() {
		try {
			SessionObject session = SessionObjectWrapper.createSessionObject(17, new ContextImpl(1337), "mail-test-session");
			MailConnection mailConnection = MailConnection.getInstance(session);
			mailConnection.setLogin(getLogin());
			mailConnection.setMailServer(getServer());
			mailConnection.setPassword(getPassword());
			mailConnection.setMailServerPort(getPort());
			mailConnection.setMailProperties(IMAPProperties.getDefaultJavaMailProperties());
			mailConnection.connect();
			try {
				final MailFolder inboxFolder = mailConnection.getFolderStorage().getFolder("INBOX");
				
				assertTrue("No INBOX folder returned!", inboxFolder != null && inboxFolder.getName().equals("INBOX"));
			} finally {
				mailConnection.close();
			}
		} catch (final Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	
	public void testGetSubfolders() {
		try {
			SessionObject session = SessionObjectWrapper.createSessionObject(17, new ContextImpl(1337), "mail-test-session");
			MailConnection mailConnection = MailConnection.getInstance(session);
			mailConnection.setLogin(getLogin());
			mailConnection.setMailServer(getServer());
			mailConnection.setPassword(getPassword());
			mailConnection.setMailServerPort(getPort());
			mailConnection.setMailProperties(IMAPProperties.getDefaultJavaMailProperties());
			mailConnection.connect();
			try {
				final MailFolder[] flds = mailConnection.getFolderStorage().getSubfolders("default/INBOX", true);
				
				assertTrue("No subfolders returned!", flds != null && flds.length > 0);
				
				for (int i = 0; i < flds.length; i++) {
					System.out.println(flds[i].getFullname() + " Subscribed=" + flds[i].isSubscribed() + " Summary=" + flds[i].getSummary());
				}
				
			} finally {
				mailConnection.close();
			}
		} catch (final Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	
}
