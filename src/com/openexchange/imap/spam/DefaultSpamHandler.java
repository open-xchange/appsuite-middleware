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

package com.openexchange.imap.spam;

import static com.openexchange.imap.utils.IMAPStorageUtility.toUIDSet;
import static com.openexchange.mail.utils.StorageUtility.prepareMailFolderParam;

import java.util.Arrays;

import javax.mail.MessagingException;

import com.openexchange.imap.IMAPConnection;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.command.CopyIMAPCommand;
import com.openexchange.imap.command.FlagsIMAPCommand;
import com.openexchange.mail.MailException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link DefaultSpamHandler}
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
	public void handleHam(final IMAPFolder spamFolder, final long[] msgUIDs, final boolean move,
			final IMAPConnection imapConnection, final IMAPStore imapStore) throws MessagingException, MailException {
		/*
		 * Copy to confirmed ham
		 */
		new CopyIMAPCommand(spamFolder, msgUIDs, prepareMailFolderParam(imapConnection.getFolderStorage()
				.getConfirmedHamFolder()), false, true).doCommand();
		if (move) {
			/*
			 * Copy messages to INBOX
			 */
			new CopyIMAPCommand(spamFolder, msgUIDs, "INBOX", false, true).doCommand();
			/*
			 * Delete messages
			 */
			new FlagsIMAPCommand(spamFolder, msgUIDs, FLAGS_DELETED, true, false).doCommand();
			/*
			 * Expunge messages immediately
			 */
			try {
				spamFolder.getProtocol().uidexpunge(toUIDSet(msgUIDs));
				/*
				 * Force folder cache update through a close
				 */
				spamFolder.close(false);
			} catch (final ProtocolException e) {
				throw new IMAPException(IMAPException.Code.MOVE_PARTIALLY_COMPLETED, e, Arrays.toString(msgUIDs),
						spamFolder.getFullName(), e.getMessage());
			}
		}
	}

}
