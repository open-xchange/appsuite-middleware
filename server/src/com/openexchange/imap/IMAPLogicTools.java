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

package com.openexchange.imap;

import static com.openexchange.mail.utils.StorageUtility.prepareMailFolderParam;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.openexchange.mail.MailException;
import com.openexchange.mail.MailLogicTools;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.processing.MimeForward;
import com.openexchange.mail.mime.processing.MimeReply;
import com.openexchange.session.Session;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link IMAPLogicTools}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPLogicTools extends IMAPFolderWorker implements MailLogicTools {

	/*
	 * Static constants
	 */
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPLogicTools.class);

	/**
	 * Initializes a new {@link IMAPLogicTools}
	 * 
	 * @param imapStore
	 *            The IMAP store
	 * @param imapConnection
	 *            The IMAP connection
	 * @param session
	 *            The session providing needed user data
	 * @throws IMAPException
	 *             If context loading fails
	 */
	public IMAPLogicTools(final IMAPStore imapStore, final IMAPConnection imapConnection, final Session session)
			throws IMAPException {
		super(imapStore, imapConnection, session);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.MailTools#getFowardMessage(long,
	 *      java.lang.String)
	 */
	public MailMessage getFowardMessage(final long originalUID, final String folder) throws MailException {
		try {
			final String fullname = prepareMailFolderParam(folder);
			imapFolder = setAndOpenFolder(imapFolder, fullname, Folder.READ_ONLY);
			/*
			 * Fetch original message
			 */
			final MailMessage forwardMail = MimeForward.getFowardMail((MimeMessage) imapFolder
					.getMessageByUID(originalUID), session);
			forwardMail.setMsgref(MailPath.getMailPath(fullname, originalUID));
			return forwardMail;
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapConnection);
		} catch (final IMAPException e) {
			throw e;
		} catch (final MailException e) {
			throw new IMAPException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.MailTools#getReplyMessage(long,
	 *      java.lang.String, boolean)
	 */
	public MailMessage getReplyMessage(final long originalUID, final String folder, final boolean replyAll)
			throws MailException {
		try {
			final String fullname = prepareMailFolderParam(folder);
			imapFolder = setAndOpenFolder(imapFolder, fullname, Folder.READ_ONLY);
			/*
			 * Fetch original message
			 */
			final MailMessage replyMail = MimeReply.getReplyMail((MimeMessage) imapFolder.getMessageByUID(originalUID),
					replyAll, session, imapConnection.getSession());
			replyMail.setMsgref(MailPath.getMailPath(fullname, originalUID));
			return replyMail;
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapConnection);
		} catch (final IMAPException e) {
			throw e;
		} catch (final MailException e) {
			throw new IMAPException(e);
		}
	}

	public void releaseResources() throws IMAPException {
		if (null != imapFolder) {
			try {
				imapFolder.close(false);
				resetIMAPFolder();
			} catch (final IllegalStateException e) {
				LOG.warn(WARN_FLD_ALREADY_CLOSED, e);
			} catch (final MessagingException e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
		}
	}

}
