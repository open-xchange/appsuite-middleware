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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.smtp.dataobjects;

import java.io.InputStream;
import java.util.ArrayList;
import javax.activation.DataHandler;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.session.Session;

/**
 * {@link SMTPMailMessage} - Extends the {@link ComposedMailMessage} class to
 * compose SMTP messages.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class SMTPMailMessage extends ComposedMailMessage {

	private static final long serialVersionUID = 9031153888416594901L;

	private TextBodyMailPart mailPart;

	private String uid;

	private final ArrayList<MailPart> enclosedParts;

	/**
	 * Initializes a new {@link SMTPMailMessage}.
	 * <p>
	 * Although content is expected to be HTML, the real message's content type
	 * is defined through {@link #setContentType(String)} or
	 * {@link #setContentType(com.openexchange.mail.mime.ContentType)}. The HTML
	 * is then converted appropriately.
	 *
	 * @param htmlMailBody
	 *            The mail body as HTML content
	 * @param session
	 *            The session providing user data
	 * @param ctx
	 *            The context
	 */
	public SMTPMailMessage(final String htmlMailBody, final Session session, final Context ctx) {
		super(session, ctx);
		mailPart = new SMTPBodyPart(htmlMailBody);
		enclosedParts = new ArrayList<MailPart>();
	}

	/**
	 * Initializes a new {@link SMTPMailMessage}
	 *
	 * @param session
	 *            The session providing needed user data
	 * @param ctx
	 *            The context
	 */
	public SMTPMailMessage(final Session session, final Context ctx) {
		super(session, ctx);
		enclosedParts = new ArrayList<MailPart>();
	}

	@Override
	public Object getContent() throws OXException {
		return mailPart.getContent();
	}

	@Override
	public void setBodyPart(final TextBodyMailPart mailPart) {
		this.mailPart = mailPart;
	}

	@Override
	public TextBodyMailPart getBodyPart() {
	    return mailPart;
	}

	@Override
	public DataHandler getDataHandler() throws OXException {
		return mailPart.getDataHandler();
	}

	@Override
	public int getEnclosedCount() throws OXException {
		return enclosedParts.size();
	}

	@Override
	public MailPart getEnclosedMailPart(final int index) throws OXException {
		return enclosedParts.get(index);
	}

	@Override
	public InputStream getInputStream() throws OXException {
		return mailPart.getInputStream();
	}

	@Override
	public void loadContent() {
		// Nothing to do
	}

	@Override
	public void prepareForCaching() {
		mailPart.prepareForCaching();
	}

	/**
	 * Removes the enclosed part at the specified position. Shifts any
	 * subsequent parts to the left (subtracts one from their indices). Returns
	 * the part that was removed.
	 *
	 * @param index
	 *            The index position
	 * @return The removed part
	 */
	@Override
	public MailPart removeEnclosedPart(final int index) {
		return enclosedParts.remove(index);
	}

	/**
	 * Adds an instance of {@link MailPart} to enclosed parts
	 *
	 * @param part
	 *            The instance of {@link MailPart} to add
	 */
	@Override
	public void addEnclosedPart(final MailPart part) {
		enclosedParts.add(part);
	}

	@Override
	public String getMailId() {
		return uid;
	}

	@Override
	public void setMailId(final String id) {
		uid = id;
	}

}
