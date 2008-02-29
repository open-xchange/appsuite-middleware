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

package com.openexchange.mail.mime.spam;

import static com.openexchange.mail.utils.StorageUtility.prepareMailFolderParam;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.UIDFolder;

import com.openexchange.mail.MailAccess;
import com.openexchange.mail.MailException;

/**
 * {@link DefaultSpamHandler} - Assumes that no wrapping message holds the
 * original spam message.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class DefaultSpamHandler extends SpamHandler {

	/**
	 * Constructor
	 */
	public DefaultSpamHandler() {
		super();
	}

	@Override
	public void handleHam(final Folder spamFolder, final long[] msgUIDs, final boolean move,
			final MailAccess<?, ?, ?> mailConnection, final Store store) throws MessagingException, MailException {
		final boolean closeFolder = !spamFolder.isOpen();
		/*
		 * Copy to confirmed ham
		 */
		try {
			if (closeFolder) {
				spamFolder.open(Folder.READ_ONLY);
			}
			final Message[] uidMsgs = ((UIDFolder) spamFolder).getMessagesByUID(msgUIDs);
			spamFolder.copyMessages(uidMsgs, store.getFolder(prepareMailFolderParam(mailConnection.getFolderStorage()
					.getConfirmedHamFolder())));
			if (move) {
				/*
				 * Copy messages to INBOX
				 */
				spamFolder.copyMessages(uidMsgs, store.getFolder("INBOX"));
				/*
				 * Delete messages
				 */
				for (int i = 0; i < uidMsgs.length; i++) {
					uidMsgs[i].setFlag(Flags.Flag.DELETED, true);
				}
				/*
				 * TODO: Ensure that only affected messages got deleted. Expunge
				 * messages
				 */
				spamFolder.expunge();
			}
		} finally {
			if (closeFolder && spamFolder.isOpen()) {
				spamFolder.close(false);
			}
		}
	}

}
