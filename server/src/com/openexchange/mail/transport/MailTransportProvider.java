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

package com.openexchange.mail.transport;

import java.util.concurrent.atomic.AtomicBoolean;

import com.openexchange.configuration.SystemConfig;
import com.openexchange.groupware.upload.impl.UploadFile;
import com.openexchange.mail.MailException;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.InfostoreDocumentMailPart;
import com.openexchange.mail.dataobjects.compose.ReferencedMailPart;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.mail.dataobjects.compose.UploadFileMailPart;
import com.openexchange.mail.transport.config.GlobalTransportConfig;
import com.openexchange.session.Session;

/**
 * {@link MailTransportProvider} - Provider for mail transport
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class MailTransportProvider {

	private static final AtomicBoolean initialized = new AtomicBoolean();

	private static MailTransportProvider instance;

	/**
	 * Initializes the transport provider
	 * 
	 * @throws MailException
	 *             If initialization of transport provider fails
	 */
	static void initTransportProvider() throws MailException {
		if (!initialized.get()) {
			synchronized (initialized) {
				if (!initialized.get()) {
					final String className = SystemConfig.getProperty(SystemConfig.Property.MailTransportProvider);
					try {
						if (className == null) {
							throw new MailConfigException("Missing mail transport provider");
						}
						instance = Class.forName(className).asSubclass(MailTransportProvider.class).newInstance();
						initialized.set(true);
					} catch (final ClassNotFoundException e) {
						throw new MailException(MailException.Code.INITIALIZATION_PROBLEM, e, new Object[0]);
					} catch (final InstantiationException e) {
						throw new MailException(MailException.Code.INITIALIZATION_PROBLEM, e, new Object[0]);
					} catch (final IllegalAccessException e) {
						throw new MailException(MailException.Code.INITIALIZATION_PROBLEM, e, new Object[0]);
					}
				}
			}
		}
	}

	/**
	 * Returns the singleton instance of mail provider
	 * 
	 * @return The singleton instance of mail provider
	 */
	public static final MailTransportProvider getInstance() {
		return instance;
	}

	/**
	 * Resets the transport provider
	 */
	static void resetTransportProvider() {
		initialized.set(false);
	}

	/**
	 * Initializes a new {@link MailTransportProvider}
	 */
	protected MailTransportProvider() {
		super();
	}

	/**
	 * Gets the name of the class implementing {@link MailTransport}
	 * 
	 * @return The name of the class implementing {@link MailTransport}
	 */
	public abstract String getMailTransportClass();

	/**
	 * Gets the name of {@link GlobalTransportConfig} implementation
	 * 
	 * @return The name of {@link GlobalTransportConfig} implementation
	 */
	public abstract String getGlobalTransportConfigClass();

	/**
	 * Gets a new instance of {@link ComposedMailMessage}
	 * 
	 * @return A new instance of {@link ComposedMailMessage}
	 * @throws MailException
	 *             If a new instance of {@link ComposedMailMessage} cannot be
	 *             created
	 */
	public abstract ComposedMailMessage getNewComposedMailMessage() throws MailException;

	/**
	 * Gets a new instance of {@link UploadFileMailPart}
	 * 
	 * @param uploadFile
	 *            The upload file
	 * @return A new instance of {@link UploadFileMailPart}
	 * @throws MailException
	 *             If a new instance of {@link UploadFileMailPart} cannot be
	 *             created
	 */
	public abstract UploadFileMailPart getNewFilePart(UploadFile uploadFile) throws MailException;

	/**
	 * Gets a new instance of {@link InfostoreDocumentMailPart}
	 * 
	 * @param documentId
	 *            The infostore document's unique ID
	 * @param session
	 *            The session providing needed user data
	 * @return A new instance of {@link InfostoreDocumentMailPart}
	 * @throws MailException
	 *             If a new instance of {@link InfostoreDocumentMailPart} cannot
	 *             be created
	 */
	public abstract InfostoreDocumentMailPart getNewDocumentPart(int documentId, Session session) throws MailException;

	/**
	 * Gets a new instance of {@link TextBodyMailPart}
	 * 
	 * @param textBody
	 *            The text body
	 * @return A new instance of {@link TextBodyMailPart}
	 * @throws MailException
	 *             If a new instance of {@link TextBodyMailPart} cannot be
	 *             created
	 */
	public abstract TextBodyMailPart getNewTextBodyPart(String textBody) throws MailException;

	/**
	 * Gets a new instance of {@link ReferencedMailPart}
	 * 
	 * @param referencedPart
	 *            The referenced part
	 * @param session
	 *            The session providing user data
	 * @return A new instance of {@link ReferencedMailPart}
	 * @throws MailException
	 *             If a new instance of {@link ReferencedMailPart} cannot be
	 *             created
	 */
	public abstract ReferencedMailPart getNewReferencedPart(MailPart referencedPart, Session session)
			throws MailException;

	/**
	 * Gets a new instance of {@link ReferencedMailPart}
	 * 
	 * @param sequenceId
	 *            The sequence ID in referenced mail
	 * @return A new instance of {@link ReferencedMailPart}
	 * @throws MailException
	 *             If a new instance of {@link ReferencedMailPart} cannot be
	 *             created
	 */
	public abstract ReferencedMailPart getNewReferencedPart(String sequenceId) throws MailException;
}
