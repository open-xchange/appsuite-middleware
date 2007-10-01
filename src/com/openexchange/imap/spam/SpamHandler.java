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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.mail.Flags;
import javax.mail.MessagingException;

import com.openexchange.imap.IMAPConnection;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.command.CopyIMAPCommand;
import com.openexchange.imap.command.FlagsIMAPCommand;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.mail.MailException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link SpamHandler}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class SpamHandler {

	private static final Lock LOCK = new ReentrantLock();

	private static final AtomicBoolean initialized = new AtomicBoolean();

	private static SpamHandler instance;

	protected static final Flags FLAGS_DELETED = new Flags(Flags.Flag.DELETED);

	public static final SpamHandler getInstance() throws MailException {
		if (!initialized.get()) {
			LOCK.lock();
			try {
				if (null == instance) {
					final String clazz = IMAPConfig.getSpamHandlerClass();
					if (clazz == null || clazz.length() == 0) {
						instance = new DefaultSpamHandler();
					} else {
						instance = Class.forName(clazz).asSubclass(SpamHandler.class).newInstance();
						initialized.set(true);
					}
				}
			} catch (final InstantiationException e) {
				throw new MailException(MailException.Code.SPAM_HANDLER_INIT_FAILED, e, e.getLocalizedMessage());
			} catch (final IllegalAccessException e) {
				throw new MailException(MailException.Code.SPAM_HANDLER_INIT_FAILED, e, e.getLocalizedMessage());
			} catch (final ClassNotFoundException e) {
				throw new MailException(MailException.Code.SPAM_HANDLER_INIT_FAILED, e, e.getLocalizedMessage());
			} finally {
				LOCK.unlock();
			}
		}
		return instance;
	}

	/**
	 * Constructor
	 */
	protected SpamHandler() {
		super();
	}

	/**
	 * Handles messages that are located in spam folder but should be treated as
	 * ham messages.
	 * <p>
	 * Dependent on the used spam system, the spam messages cannot be
	 * copied/moved as they are, but need to be parsed in the way the spam
	 * system wraps spam messages.
	 * 
	 * @param spamFolder
	 *            The spam folder
	 * @param msgUIDs
	 *            The message UIDs
	 * @param move
	 *            Whether to move or to copy the messages
	 * @param imapConnection
	 *            The IMAP connection providing access to storages
	 * @param imapStore
	 *            The imap store
	 * @throws MessagingException
	 * @throws MailException
	 */
	public abstract void handleHam(IMAPFolder spamFolder, long[] msgUIDs, boolean move, IMAPConnection imapConnection,
			IMAPStore imapStore) throws MessagingException, MailException;

	/**
	 * Handles messages that should be treated as spam messages
	 * 
	 * @param imapFolder
	 *            The IMAP folder
	 * @param msgUIDs
	 *            The message UIDs
	 * @param move
	 *            Whether to move or to copy the messages
	 * @param imapConnection
	 *            The IMAP connection providing access to storages
	 * @throws MessagingException
	 * @throws MailException
	 */
	public void handleSpam(final IMAPFolder imapFolder, final long[] msgUIDs, final boolean move,
			final IMAPConnection imapConnection) throws MessagingException, MailException {
		/*
		 * Copy to confirmed spam
		 */
		new CopyIMAPCommand(imapFolder, msgUIDs, prepareMailFolderParam(imapConnection.getFolderStorage()
				.getConfirmedSpamFolder()), false, true).doCommand();
		if (move) {
			/*
			 * Copy messages to spam folder
			 */
			new CopyIMAPCommand(imapFolder, msgUIDs, prepareMailFolderParam(imapConnection.getFolderStorage()
					.getSpamFolder()), false, true).doCommand();
			/*
			 * Delete messages
			 */
			new FlagsIMAPCommand(imapFolder, msgUIDs, FLAGS_DELETED, true, false).doCommand();
			/*
			 * Expunge messages immediately
			 */
			try {
				imapFolder.getProtocol().uidexpunge(toUIDSet(msgUIDs));
				/*
				 * Force folder cache update through a close
				 */
				imapFolder.close(false);
			} catch (final ProtocolException e) {
				throw new IMAPException(IMAPException.Code.MOVE_PARTIALLY_COMPLETED, e, Arrays.toString(msgUIDs),
						imapFolder.getFullName(), e.getMessage());
			}
		}
	}
}
