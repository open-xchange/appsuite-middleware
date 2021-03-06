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

package com.openexchange.gmail.send.dataobjects;

import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ReferencedMailPart;
import com.openexchange.session.Session;

/**
 * {@link GmailSendReferencedPart}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class GmailSendReferencedPart extends ReferencedMailPart {

    private static final long serialVersionUID = -324097128586148044L;

    /**
	 * Initializes a new {@link GmailSendReferencedPart}
	 *
	 * @param referencedPart
	 *            The referenced {@link MailPart part}
	 * @param session
	 *            The {@link Session session} providing needed user data
	 * @throws OXException
	 *             If instantiation fails
	 */
	public GmailSendReferencedPart(final MailPart referencedPart, final Session session) throws OXException {
		super(referencedPart, session);
	}

	/**
	 * Initializes a new {@link GmailSendReferencedPart}
	 *
	 * @param referencedMail
	 *            The referenced {@link MailMessage mail}
	 * @param session
	 *            The {@link Session session} providing needed user data
	 * @throws OXException
	 *             If instantiation fails
	 */
	public GmailSendReferencedPart(final MailMessage referencedMail, final Session session) throws OXException {
		super(referencedMail, session);
	}

}
