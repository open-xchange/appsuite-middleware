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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.UIDFolder;

import com.openexchange.mail.MailConnection;
import com.openexchange.mail.MailException;
import com.openexchange.mail.config.MailConfig;

/**
 * {@link SpamHandler} - Handles spam/ham messages with <a
 * href="java.sun.com/products/javamail/">JavaMail API</a>
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class SpamHandler {

	private static final Lock LOCK = new ReentrantLock();

	private static final AtomicBoolean initialized = new AtomicBoolean();

	private static SpamHandler instance;

	protected static final Flags FLAGS_DELETED = new Flags(Flags.Flag.DELETED);

	/**
	 * Gets the singleton instance of {@link SpamHandler}
	 * 
	 * @return The singleton instance of {@link SpamHandler}
	 * @throws MailException
	 */
	public static final SpamHandler getInstance() throws MailException {
		if (!initialized.get()) {
			LOCK.lock();
			try {
				if (null == instance) {
					final String clazz = MailConfig.getSpamHandlerClass();
					if (clazz == null || clazz.length() == 0) {
						instance = new DefaultSpamHandler();
					} else {
						instance = Class.forName(clazz).asSubclass(SpamHandler.class).newInstance();
					}
					initialized.set(true);
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
	 * Releases the singleton instance of {@link SpamHandler}
	 */
	public static final void releaseInstance() {
		if (initialized.get()) {
			LOCK.lock();
			try {
				if (!initialized.get()) {
					return;
				}
				instance = null;
				initialized.set(false);
			} finally {
				LOCK.unlock();
			}
		}
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
	 * @param mailConnection
	 *            The mail connection providing access to storages
	 * @param store
	 *            The store
	 * @throws MessagingException
	 * @throws MailException
	 */
	public abstract void handleHam(Folder spamFolder, long[] msgUIDs, boolean move,
			MailConnection<?, ?, ?> mailConnection, Store store) throws MessagingException, MailException;

	/**
	 * Handles messages that should be treated as spam messages
	 * 
	 * @param folder
	 *            The folder
	 * @param msgUIDs
	 *            The message UIDs
	 * @param move
	 *            Whether to move or to copy the messages
	 * @param mailConnection
	 *            The mail connection providing access to storages
	 * @param store
	 *            The store
	 * @throws MessagingException
	 * @throws MailException
	 */
	public void handleSpam(final Folder folder, final long[] msgUIDs, final boolean move,
			final MailConnection<?, ?, ?> mailConnection, final Store store) throws MessagingException, MailException {
		if (!(folder instanceof UIDFolder)) {
			throw new IllegalArgumentException("Folder argument must implement " + UIDFolder.class.getCanonicalName());
		}
		/*
		 * Copy to confirmed spam
		 */
		final boolean closeFolder = !folder.isOpen();
		try {
			if (closeFolder) {
				folder.open(Folder.READ_ONLY);
			}
			final Message[] uidMsgs = ((UIDFolder) folder).getMessagesByUID(msgUIDs);
			folder.copyMessages(uidMsgs, store.getFolder(prepareMailFolderParam(mailConnection.getFolderStorage()
					.getConfirmedSpamFolder())));
			if (move) {
				/*
				 * Copy messages to spam folder
				 */
				folder.copyMessages(uidMsgs, store.getFolder(prepareMailFolderParam(mailConnection.getFolderStorage()
						.getSpamFolder())));
				/*
				 * Delete messages
				 */
				for (int i = 0; i < uidMsgs.length; i++) {
					uidMsgs[i].setFlag(Flags.Flag.DELETED, true);
				}
				/*
				 * TODO: Ensure that only affected messages got expunged.
				 * Expunge messages immediately
				 */
				folder.expunge();
				/*
				 * Force folder cache update through a close
				 */
				folder.close(false);
			}
		} finally {
			if (closeFolder && folder.isOpen()) {
				folder.close(false);
			}
		}
	}
}
