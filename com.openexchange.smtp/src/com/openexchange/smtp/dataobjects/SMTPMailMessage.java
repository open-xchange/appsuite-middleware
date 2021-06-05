/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.smtp.dataobjects;

import java.io.InputStream;
import java.util.ArrayList;
import javax.activation.DataHandler;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
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
