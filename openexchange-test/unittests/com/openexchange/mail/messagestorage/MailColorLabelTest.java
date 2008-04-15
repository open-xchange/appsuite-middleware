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
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;

/**
 * {@link MailColorLabelTest}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MailColorLabelTest extends AbstractMailTest {

	/**
	 * 
	 */
	public MailColorLabelTest() {
		super();
	}

	/**
	 * @param name
	 */
	public MailColorLabelTest(final String name) {
		super(name);
	}

	private static final MailField[] FIELDS_ID_AND_COLORLABEL = { MailField.ID, MailField.COLOR_LABEL };

	public void testMailColorLabel() {
		try {
			final SessionObject session = SessionObjectWrapper.createSessionObject(getUser(),
					new ContextImpl(getCid()), "mail-test-session");
			session.setPassword(getPassword());
			final MailMessage[] mails = getMessages(getTestMailDir(), -1);

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();

			if (!mailAccess.getFolderStorage().getFolder("INBOX").isSupportsUserFlags()) {
				System.out
						.println("MailColorLabelTest.testMailColorLabel() aborted since user flags are not supported");
				return;
			}

			final long[] uids = mailAccess.getMessageStorage().appendMessages("INBOX", mails);
			try {

				mailAccess.getMessageStorage().updateMessageColorLabel("INBOX", uids, 4);
				MailMessage[] fetchedMails = mailAccess.getMessageStorage().getMessages("INBOX", uids,
						FIELDS_ID_AND_COLORLABEL);
				for (int i = 0; i < fetchedMails.length; i++) {
					assertTrue("Missing color label", fetchedMails[i].containsColorLabel());
					assertTrue("Mail's color flag does not carry expected value", fetchedMails[i].getColorLabel() == 4);
				}

				mailAccess.getMessageStorage().updateMessageColorLabel("INBOX", uids, 7);
				fetchedMails = mailAccess.getMessageStorage().getMessages("INBOX", uids, FIELDS_ID_AND_COLORLABEL);
				for (int i = 0; i < fetchedMails.length; i++) {
					assertTrue("Missing color label", fetchedMails[i].containsColorLabel());
					assertTrue("Mail's color flag does not carry expected value", fetchedMails[i].getColorLabel() == 7);
				}

				mailAccess.getMessageStorage().updateMessageColorLabel("INBOX", uids, 0);
				fetchedMails = mailAccess.getMessageStorage().getMessages("INBOX", uids, FIELDS_ID_AND_COLORLABEL);
				for (int i = 0; i < fetchedMails.length; i++) {
					assertTrue("Missing color label", fetchedMails[i].containsColorLabel());
					assertTrue("Mail's color flag does not carry expected value", fetchedMails[i].getColorLabel() == 0);
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
