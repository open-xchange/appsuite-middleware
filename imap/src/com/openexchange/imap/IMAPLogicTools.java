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

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.openexchange.mail.MailException;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.processing.MimeForward;
import com.openexchange.mail.mime.processing.MimeReply;
import com.openexchange.session.Session;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link IMAPLogicTools}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPLogicTools {

	private static final long serialVersionUID = 5007382448690742714L;

	private final IMAPStore imapStore;

	private final IMAPAccess imapAccess;

	private final Session session;

	/**
	 * Initializes a new {@link IMAPLogicTools}
	 * 
	 * @param imapStore
	 *            The IMAP store
	 * @param imapAccess
	 *            The IMAP access
	 * @param session
	 *            The session providing needed user data
	 * @throws IMAPException
	 *             If context loading fails
	 */
	public IMAPLogicTools(final IMAPStore imapStore, final IMAPAccess imapAccess, final Session session)
			throws IMAPException {
		super();
		this.imapStore = imapStore;
		this.imapAccess = imapAccess;
		this.session = session;
	}

	public MailMessage getFowardMessage(final long[] originalUIDs, final String fullname) throws MailException {
		try {
			final IMAPFolder imapFolder = (IMAPFolder) imapStore.getFolder(fullname);
			imapFolder.open(Folder.READ_ONLY);
			try {
				/*
				 * Fetch original messages
				 */
				final MimeMessage[] msgs = new MimeMessage[originalUIDs.length];
				for (int i = 0; i < originalUIDs.length; i++) {
					msgs[i] = (MimeMessage) imapFolder.getMessageByUID(originalUIDs[i]);
					if (msgs[i] == null) {
						throw new MailException(MailException.Code.MAIL_NOT_FOUND, Long.valueOf(originalUIDs[i]),
								imapFolder.getFullName());
					}
				}
				final MailMessage forwardMail = MimeForward.getFowardMail(msgs, session);
				{
					final StringBuilder sb = new StringBuilder(msgs.length * 16);
					sb.append(MailPath.getMailPath(fullname, originalUIDs[0]));
					for (int i = 1; i < originalUIDs.length; i++) {
						sb.append(',').append(MailPath.getMailPath(fullname, originalUIDs[i]));
					}
					forwardMail.setMsgref(sb.toString());
				}
				return forwardMail;
			} finally {
				imapFolder.close(false);
			}
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapAccess.getIMAPConfig());
		} catch (final IMAPException e) {
			throw e;
		} catch (final MailException e) {
			throw new IMAPException(e);
		}
	}

	public MailMessage getReplyMessage(final long originalUID, final String fullname, final boolean replyAll)
			throws MailException {
		try {
			final IMAPFolder imapFolder = (IMAPFolder) imapStore.getFolder(fullname);
			imapFolder.open(Folder.READ_ONLY);
			try {
				/*
				 * Fetch original message
				 */
				final MailMessage replyMail = MimeReply.getReplyMail((MimeMessage) imapFolder
						.getMessageByUID(originalUID), replyAll, session, imapAccess.getSession());
				replyMail.setMsgref(MailPath.getMailPath(fullname, originalUID));
				return replyMail;
			} finally {
				imapFolder.close(false);
			}
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapAccess.getIMAPConfig());
		} catch (final IMAPException e) {
			throw e;
		} catch (final MailException e) {
			throw new IMAPException(e);
		}
	}

}
