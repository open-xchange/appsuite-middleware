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

import static com.openexchange.mail.mime.utils.MIMEStorageUtility.toUIDSet;
import static com.openexchange.mail.utils.StorageUtility.prepareMailFolderParam;

import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;

import com.openexchange.imap.IMAPAccess;
import com.openexchange.imap.command.CopyIMAPCommand;
import com.openexchange.imap.command.ExtractSpamMsgIMAPCommand;
import com.openexchange.imap.command.FetchIMAPCommand;
import com.openexchange.imap.command.FlagsIMAPCommand;
import com.openexchange.mail.MailAccess;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.mime.ContainerMessage;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.spam.SpamHandler;
import com.openexchange.tools.Collections.SmartLongArray;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.AppendUID;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;

/**
 * {@link IMAPSpamAssassinSpamHandler}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPSpamAssassinSpamHandler extends SpamHandler {

	private static final String STR_MSEC = "msec";

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPSpamAssassinSpamHandler.class);

	private static final String STR_INBOX = "INBOX";

	private static final String WARN_FLD_ALREADY_CLOSED = "Invoked close() on a closed folder";

	/**
	 * Initializes a new {@link IMAPSpamAssassinSpamHandler}
	 */
	public IMAPSpamAssassinSpamHandler() {
		super();
	}

	@Override
	public void handleHam(final Folder spamFolder, final long[] msgUIDs, final boolean move,
			final MailAccess<?, ?, ?> mailConnection, final Store store) throws MessagingException, MailException {
		_handleHam((IMAPFolder) spamFolder, msgUIDs, move, (IMAPAccess) mailConnection, (IMAPStore) store);
	}

	private void _handleHam(final IMAPFolder spamFolder, final long[] msgUIDs, final boolean move,
			final IMAPAccess imapConnection, final IMAPStore imapStore) throws MessagingException, MailException {
		/*
		 * Mark as ham. In contrast to mark as spam this is a very time sucking
		 * operation. In order to deal with the original messages that are
		 * wrapped inside a SpamAssassin-created message it must be extracted.
		 * Therefore we need to access message's content and cannot deal only
		 * with UIDs
		 */
		final FetchProfile fp = new FetchProfile();
		fp.add(MessageHeaders.HDR_X_SPAM_FLAG);
		fp.add(FetchProfile.Item.CONTENT_INFO);
		final ContainerMessage[] msgs = (ContainerMessage[]) new FetchIMAPCommand(spamFolder, msgUIDs, fp, false, false)
				.doCommand();
		/*
		 * Separate the plain from the nested messages inside spam folder
		 */
		SmartLongArray plainUIDs = new SmartLongArray(msgUIDs.length);
		SmartLongArray extractUIDs = new SmartLongArray(msgUIDs.length);
		for (int i = 0; i < msgs.length; i++) {
			final String[] spamHdr = msgs[i].getHeader(MessageHeaders.HDR_X_SPAM_FLAG);
			final BODYSTRUCTURE bodystructure = msgs[i].getBodystructure();
			if (spamHdr != null && "yes".regionMatches(true, 0, spamHdr[0], 0, 3) && bodystructure.isMulti()
					&& bodystructure.bodies[1].isNested()) {
				extractUIDs.append(msgUIDs[i]);
			} else {
				plainUIDs.append(msgUIDs[i]);
			}
		}
		final String confirmedHamFullname = prepareMailFolderParam(imapConnection.getFolderStorage()
				.getConfirmedHamFolder());
		/*
		 * Copy plain messages to confirmed ham and INBOX
		 */
		long[] plainUIDsArr = plainUIDs.toArray();
		plainUIDs = null;
		new CopyIMAPCommand(spamFolder, plainUIDsArr, confirmedHamFullname, false, true).doCommand();
		if (move) {
			new CopyIMAPCommand(spamFolder, plainUIDsArr, STR_INBOX, false, true).doCommand();
		}
		plainUIDsArr = null;
		/*
		 * Handle spam messages
		 */
		long[] spamArr = extractUIDs.toArray();
		extractUIDs = null;
		IMAPFolder confirmedHamFld = null;
		try {
			confirmedHamFld = (IMAPFolder) imapStore.getFolder(confirmedHamFullname);
			confirmedHamFld.open(Folder.READ_WRITE);
			/*
			 * Get nested spam messages
			 */
			long start = System.currentTimeMillis();
			Message[] nestedMsgs;
			nestedMsgs = new ExtractSpamMsgIMAPCommand(spamFolder, spamArr).doCommand();
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder(100).append("Nested SPAM messages fetched in ").append(
						(System.currentTimeMillis() - start)).append(STR_MSEC).toString());
			}
			spamArr = null;
			/*
			 * ... and append them to confirmed ham folder and - if move enabled -
			 * copy them to INBOX.
			 */
			start = System.currentTimeMillis();
			AppendUID[] appendUIDs = confirmedHamFld.appendUIDMessages(nestedMsgs);
			MailServletInterface.mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder(100).append("Nested SPAM messages appended to ")
						.append(confirmedHamFullname).append(" in ").append((System.currentTimeMillis() - start))
						.append(STR_MSEC).toString());
			}
			nestedMsgs = null;
			if (move) { // Cannot be null
				start = System.currentTimeMillis();
				new CopyIMAPCommand(confirmedHamFld, appendUID2Long(appendUIDs), STR_INBOX, false, true).doCommand();
				if (LOG.isInfoEnabled()) {
					LOG.info(new StringBuilder(100).append("Nested SPAM messages copied to ").append(STR_INBOX).append(
							" in ").append((System.currentTimeMillis() - start)).append(STR_MSEC).toString());
				}
			}
			appendUIDs = null;
			if (move) {
				/*
				 * Expunge messages
				 */
				new FlagsIMAPCommand(spamFolder, msgUIDs, FLAGS_DELETED, true, false).doCommand();
				start = System.currentTimeMillis();
				spamFolder.getProtocol().uidexpunge(toUIDSet(msgUIDs));
				MailServletInterface.mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				if (LOG.isInfoEnabled()) {
					LOG.info(new StringBuilder(100).append("Original spam messages expunged in ").append(
							(System.currentTimeMillis() - start)).append(STR_MSEC).toString());
				}
				/*
				 * Close folder to force JavaMail-internal message cache update
				 */
				spamFolder.close(false);
			}
		} catch (final ProtocolException e1) {
			throw new MessagingException(e1.getMessage(), e1);
		} finally {
			if (confirmedHamFld != null) {
				try {
					confirmedHamFld.close(false);
				} catch (final IllegalStateException e) {
					LOG.warn(WARN_FLD_ALREADY_CLOSED, e);
				}
			}
		}
	}

	private static long[] appendUID2Long(final AppendUID[] appendUIDs) {
		final long[] retval = new long[appendUIDs.length];
		for (int i = 0; i < retval.length; i++) {
			retval[i] = appendUIDs[i].uid;
		}
		return retval;
	}

}
