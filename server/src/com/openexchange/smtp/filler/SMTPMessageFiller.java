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

package com.openexchange.smtp.filler;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.mail.MailException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.mime.filler.MIMEMessageFiller;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.session.Session;
import com.openexchange.smtp.config.SMTPConfig;
import com.openexchange.smtp.dataobjects.SMTPMailMessage;
import com.sun.mail.smtp.SMTPMessage;

/**
 * {@link SMTPMessageFiller} - Fills an instance of {@link SMTPMessage} with
 * headers/contents given through an instance of {@link SMTPMailMessage}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class SMTPMessageFiller extends MIMEMessageFiller {

	/**
	 * Constructor
	 * 
	 * @param session
	 *            The session
	 * @param ctx
	 *            The context
	 */
	public SMTPMessageFiller(final Session session, final Context ctx) {
		super(session, ctx);
	}

	/**
	 * Constructor
	 * 
	 * @param session
	 *            The session
	 * @param ctx
	 *            The context
	 * @param usm
	 *            The user's mail settings
	 */
	public SMTPMessageFiller(final Session session, final Context ctx, final UserSettingMail usm) {
		super(session, ctx, usm);
	}

	/**
	 * Fills given instance of {@link SMTPMessage}
	 * 
	 * @param mail
	 *            The source mail
	 * @param smtpMessage
	 *            The SMTP message to fill
	 * @throws MessagingException
	 *             If a messaging error occurs
	 * @throws MailException
	 *             If a mail error occurs
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	public void fillMail(final SMTPMailMessage mail, final SMTPMessage smtpMessage) throws MessagingException,
			MailException, IOException {
		fillMail(mail, smtpMessage, null, null);
	}

	/**
	 * Fills given instance of {@link SMTPMessage}
	 * 
	 * @param mail
	 *            The source mail
	 * @param smtpMessage
	 *            The SMTP message to fill
	 * @param type
	 *            The compose type
	 * @throws MessagingException
	 *             If a messaging error occurs
	 * @throws MailException
	 *             If a mail error occurs
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	public void fillMail(final SMTPMailMessage mail, final SMTPMessage smtpMessage, final ComposeType type)
			throws MessagingException, MailException, IOException {
		fillMail(mail, smtpMessage, type, null);
	}

	/**
	 * Fills given instance of {@link SMTPMessage}
	 * 
	 * @param mail
	 *            The source mail
	 * @param smtpMessage
	 *            The SMTP message to fill
	 * @param type
	 *            The compose type
	 * @param originalMail
	 *            The referenced mail (on forward/reply)
	 * @throws MessagingException
	 *             If a messaging error occurs
	 * @throws MailException
	 *             If a mail error occurs
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	public void fillMail(final SMTPMailMessage mail, final SMTPMessage smtpMessage, final ComposeType type,
			final MailMessage originalMail) throws MessagingException, MailException, IOException {
		/*
		 * Check for reply
		 */
		if (mail.getReferencedMail() != null && ComposeType.REPLY.equals(type)) {
			setReplyHeaders(mail.getReferencedMail(), smtpMessage);
		}
		/*
		 * Set headers
		 */
		setMessageHeaders(mail, smtpMessage);
		/*
		 * Set common headers
		 */
		setCommonHeaders(smtpMessage);
		/*
		 * Fill body
		 */
		fillMailBody(mail, smtpMessage, type, originalMail);
	}

	@Override
	public void setCommonHeaders(final MimeMessage mimeMessage) throws MessagingException {
		super.setCommonHeaders(mimeMessage);
		/*
		 * ENVELOPE-FROM
		 */
		if (SMTPConfig.isSmtpEnvelopeFrom()) {
			/*
			 * Set ENVELOPE-FROM in SMTP message to user's primary email address
			 */
			((SMTPMessage) mimeMessage).setEnvelopeFrom(UserStorage.getStorageUser(session.getUserId(), ctx).getMail());
		}
	}

}
