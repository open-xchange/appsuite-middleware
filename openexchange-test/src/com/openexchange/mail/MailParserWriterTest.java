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
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.writer.FolderWriter;
import com.openexchange.mail.json.writer.MessageWriter;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessionObjectWrapper;

/**
 * {@link MailParserWriterTest}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MailParserWriterTest extends AbstractMailTest {

	/**
	 * 
	 */
	public MailParserWriterTest() {
		super();
	}

	/**
	 * @param name
	 */
	public MailParserWriterTest(final String name) {
		super(name);
	}

	private static final MailListField[] COMMON_LIST_FIELDS = { MailListField.ID, MailListField.FOLDER_ID,
			MailListField.SIZE, MailListField.FROM, MailListField.TO, MailListField.RECEIVED_DATE,
			MailListField.SENT_DATE, MailListField.SUBJECT, MailListField.ATTACHMENT, MailListField.FLAGS,
			MailListField.PRIORITY, MailListField.COLOR_LABEL };

	public void testMessageWriter() {
		try {
			final SessionObject session = SessionObjectWrapper.createSessionObject(getUser(), new ContextImpl(getCid()),
					"mail-test-session");
			final MailConnection mailConnection = MailConnection.getInstance(session);
			mailConnection.setLogin(getLogin());
			mailConnection.setMailServer(getServer());
			mailConnection.setPassword(getPassword());
			mailConnection.setMailServerPort(getPort());
			mailConnection.connect();
			try {
				final MailMessage[] mails = mailConnection.getMessageStorage().getAllMessages("default.INBOX", null, null,
						null, new MailListField[] { MailListField.ID, MailListField.ATTACHMENT });

				for (final MailMessage mail : mails) {
					if (mail.getContentType().isMimeType("multipart/mixed")) {
						System.out.println(MessageWriter.writeMailMessage(mailConnection.getMessageStorage().getMessage(
								"default.INBOX", mail.getUid()), true, session));
						break;
					}
				}
				
				for (final MailMessage mail : mails) {
					if (mail.getContentType().isMimeType("multipart/alternative")) {
						System.out.println(MessageWriter.writeMailMessage(mailConnection.getMessageStorage().getMessage(
								"default.INBOX", mail.getUid()), true, session));
						break;
					}
				}
			} finally {
				mailConnection.close(true);
			}
		} catch (final MailException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (final LdapException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testFolderWriter() {
		try {
			final SessionObject session = SessionObjectWrapper.createSessionObject(getUser(), new ContextImpl(getCid()),
					"mail-test-session");
			final MailConnection mailConnection = MailConnection.getInstance(session);
			mailConnection.setLogin(getLogin());
			mailConnection.setMailServer(getServer());
			mailConnection.setPassword(getPassword());
			mailConnection.setMailServerPort(getPort());
			mailConnection.connect();
			try {
				final MailFolder root = mailConnection.getFolderStorage().getRootFolder();
				writeFolder(root, mailConnection);
				
				
				
			} finally {
				mailConnection.close(true);
			}
		} catch (final MailException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (final LdapException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	private static void writeFolder(final MailFolder f, final MailConnection mailConnection) throws MailException {
		System.out.println(FolderWriter.writeMailFolder(f, mailConnection.getMailConfig()));
		MailFolder[] flds = mailConnection.getFolderStorage().getSubfolders(f.getFullname(), true);
		for (MailFolder folder : flds) {
			writeFolder(folder, mailConnection);
		}
	}
}
