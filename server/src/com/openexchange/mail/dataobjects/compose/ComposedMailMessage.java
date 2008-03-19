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

package com.openexchange.mail.dataobjects.compose;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.filler.MIMEMessageFiller;
import com.openexchange.session.Session;

/**
 * {@link ComposedMailMessage} - Subclass of {@link MailPart} designed for
 * composing a mail.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class ComposedMailMessage extends MailMessage {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -6179506566418364076L;

	private MailMessage[] referencedMails;

	private final Session session;

	private final Context ctx;

	private MIMEMessageFiller filler;

	/**
	 * Default constructor
	 */
	protected ComposedMailMessage(final Session session, final Context ctx) {
		super();
		this.session = session;
		this.ctx = ctx;
	}

	/**
	 * Gets the session
	 * 
	 * @return the session
	 */
	public Session getSession() {
		return session;
	}

	/**
	 * Gets the context
	 * 
	 * @return the context
	 */
	public Context getContext() {
		return ctx;
	}

	/**
	 * Sets the mail filler
	 * 
	 * @param filler
	 *            The mail filler
	 */
	public void setFiller(final MIMEMessageFiller filler) {
		this.filler = filler;
	}

	/**
	 * Releases this composed mail's referenced uploaded files
	 */
	public void release() {
		if (null != filler) {
			filler.deleteReferencedUploadFiles();
		}
	}

	@Override
	public int getUnreadMessages() {
		throw new UnsupportedOperationException("ComposedMailMessage.getUnreadMessages() not supported");
	}

	@Override
	public void setUnreadMessages(final int unreadMessages) {
		throw new UnsupportedOperationException("ComposedMailMessage.setUnreadMessages() not supported");
	}

	/**
	 * Checks if this composed mail contains referenced mails
	 * 
	 * @return <code>true</code> if this composed mail contains referenced
	 *         mails; otherwise <code>false</code>
	 */
	public boolean containsReferencedMails() {
		return referencedMails != null && referencedMails.length > 0;
	}

	/**
	 * Gets the number of referenced mails
	 * 
	 * @return The number of referenced mails
	 */
	public int getReferencedMailsSize() {
		return referencedMails == null ? 0 : referencedMails.length;
	}

	/**
	 * Gets the referenced mail of this composed mail
	 * 
	 * @return The referenced mail
	 */
	public MailMessage getReferencedMail() {
		return referencedMails == null ? null : referencedMails[0];
	}

	/**
	 * Gets the referenced mails of this composed mail
	 * 
	 * @return The referenced mails
	 */
	public MailMessage[] getReferencedMails() {
		if (referencedMails == null) {
			return null;
		}
		final MailMessage[] retval = new MailMessage[referencedMails.length];
		System.arraycopy(referencedMails, 0, retval, 0, referencedMails.length);
		return retval;
	}

	/**
	 * Sets the referenced mail of this composed mail
	 * 
	 * @param referencedMail
	 *            The referenced mail
	 */
	public void setReferencedMail(final MailMessage referencedMail) {
		this.referencedMails = new MailMessage[1];
		referencedMails[0] = referencedMail;
	}

	/**
	 * Sets the referenced mails of this composed mail
	 * 
	 * @param referencedMails
	 *            The referenced mails
	 */
	public void setReferencedMails(final MailMessage[] referencedMails) {
		this.referencedMails = new MailMessage[referencedMails.length];
		System.arraycopy(referencedMails, 0, this.referencedMails, 0, referencedMails.length);
	}

	/**
	 * Sets this composed message's body part
	 * 
	 * @param mailPart
	 *            The body part
	 */
	public abstract void setBodyPart(TextBodyMailPart mailPart);

	/**
	 * Removes the enclosed part at the specified position. Shifts any
	 * subsequent parts to the left (subtracts one from their indices). Returns
	 * the part that was removed.
	 * 
	 * @param index
	 *            The index position
	 * @return The removed part
	 */
	public abstract MailPart removeEnclosedPart(int index);

	/**
	 * Adds an instance of {@link MailPart} to enclosed parts
	 * 
	 * @param part
	 *            The instance of {@link MailPart} to add
	 */
	public abstract void addEnclosedPart(MailPart part);

}
