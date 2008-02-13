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

package com.openexchange.smtp;

import com.openexchange.groupware.upload.impl.UploadFile;
import com.openexchange.mail.MailException;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.InfostoreDocumentMailPart;
import com.openexchange.mail.dataobjects.compose.ReferencedMailPart;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.mail.dataobjects.compose.UploadFileMailPart;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.session.Session;
import com.openexchange.smtp.config.GlobalSMTPConfig;
import com.openexchange.smtp.dataobjects.SMTPBodyPart;
import com.openexchange.smtp.dataobjects.SMTPDocumentPart;
import com.openexchange.smtp.dataobjects.SMTPFilePart;
import com.openexchange.smtp.dataobjects.SMTPMailMessage;
import com.openexchange.smtp.dataobjects.SMTPReferencedPart;

/**
 * {@link SMTPProvider}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class SMTPProvider extends TransportProvider {

	/**
	 * Initializes a new {@link SMTPProvider}
	 */
	public SMTPProvider() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.transport.MailTransportProvider#getMailTransportClass()
	 */
	@Override
	public String getMailTransportClass() {
		return SMTPTransport.class.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.transport.MailTransportProvider#getGlobalTransportConfigClass()
	 */
	@Override
	public String getGlobalTransportConfigClass() {
		return GlobalSMTPConfig.class.getName();
	}

	@Override
	public ComposedMailMessage getNewComposedMailMessage() throws MailException {
		return new SMTPMailMessage();
	}

	@Override
	public UploadFileMailPart getNewFilePart(final UploadFile uploadFile) throws MailException {
		return new SMTPFilePart(uploadFile);
	}

	@Override
	public InfostoreDocumentMailPart getNewDocumentPart(final int documentId, final Session session)
			throws MailException {
		return new SMTPDocumentPart(documentId, session);
	}

	@Override
	public ReferencedMailPart getNewReferencedPart(final MailPart referencedPart, final Session session)
			throws MailException {
		return new SMTPReferencedPart(referencedPart, session);
	}

	@Override
	public ReferencedMailPart getNewReferencedPart(final String sequenceId) throws MailException {
		return new SMTPReferencedPart(sequenceId);
	}

	@Override
	public TextBodyMailPart getNewTextBodyPart(final String textBody) throws MailException {
		return new SMTPBodyPart(textBody);
	}

	/**
	 * The SMTP protocol: <code>smtp</code>
	 */
	public static final String PROTOCOL_SMTP = "smtp";

	@Override
	public String getProtocol() {
		return PROTOCOL_SMTP;
	}

	/**
	 * The secure SMTP protocol: <code>smtp</code>
	 */
	public static final String PROTOCOL_SMTP_SECURE = "smtps";

	@Override
	public boolean supportsProtocol(final String protocol) {
		return PROTOCOL_SMTP.equalsIgnoreCase(protocol) || PROTOCOL_SMTP_SECURE.equalsIgnoreCase(protocol);
	}
}
