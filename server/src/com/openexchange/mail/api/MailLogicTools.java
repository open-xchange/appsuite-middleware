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

package com.openexchange.mail.api;

import com.openexchange.mail.MailException;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.processing.MimeForward;
import com.openexchange.mail.mime.processing.MimeReply;
import com.openexchange.session.Session;

/**
 * {@link MailLogicTools} - Provides convenience methods to reply/forward a mail
 * message.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class MailLogicTools {

	/**
	 * The session providing user data
	 */
	protected final Session session;

	/**
	 * Initializes a new {@link MailLogicTools}
	 * 
	 * @param session
	 *            The session providing user data
	 */
	public MailLogicTools(final Session session) {
		super();
		this.session = session;
	}

	/**
	 * Creates a reply message for the message specified by
	 * <code>originalMail</code>.
	 * <p>
	 * If mailing system deals with common RFC822 messages, this convenience
	 * method only delegates its request to
	 * {@link MimeReply#getReplyMail(MailMessage, boolean, Session)} and can be
	 * left unchanged. Otherwise an message data specific implementation is
	 * needed.
	 * 
	 * @param originalMail
	 *            The original mail
	 * @param replyAll
	 *            <code>true</code> to reply to all recipients; otherwise
	 *            <code>false</code>
	 * @return An instance of {@link MailMessage} representing the reply message
	 * @throws MailException
	 *             If reply message cannot be generated
	 */
	public MailMessage getReplyMessage(final MailMessage originalMail, final boolean replyAll) throws MailException {
		final MailMessage replyMail = MimeReply.getReplyMail(originalMail, replyAll, session);
		replyMail.setMsgref(MailPath.getMailPath(originalMail.getFolder(), originalMail.getMailId()));
		return replyMail;
	}

	/**
	 * Creates a forward message for the messages specified by
	 * <code>originalMails</code>. If multiple messages are specified then
	 * these messages are forwarded as <b>attachment</b> since no inline
	 * forward is possible.
	 * <p>
	 * If mailing system deals with common RFC822 messages, this convenience
	 * method only delegates its request to
	 * {@link MimeForward#getFowardMail(MailMessage[], Session)} and can be left
	 * unchanged. Otherwise an message data specific implementation is needed.
	 * 
	 * @param originalMails
	 *            The original mails
	 * @return An instance of {@link MailMessage} representing the forward
	 *         message
	 * @throws MailException
	 *             If forward message cannot be generated
	 */
	public MailMessage getFowardMessage(final MailMessage[] originalMails) throws MailException {
		final MailMessage forwardMail = MimeForward.getFowardMail(originalMails, session);
		{
			final StringBuilder sb = new StringBuilder(originalMails.length * 16);
			sb.append(MailPath.getMailPath(originalMails[0].getFolder(), originalMails[0].getMailId()));
			for (int i = 1; i < originalMails.length; i++) {
				sb.append(',').append(MailPath.getMailPath(originalMails[i].getFolder(), originalMails[i].getMailId()));
			}
			forwardMail.setMsgref(sb.toString());
		}
		return forwardMail;
	}

}
