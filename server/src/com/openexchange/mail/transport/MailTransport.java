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

import java.lang.reflect.InvocationTargetException;

import com.openexchange.groupware.upload.impl.UploadFile;
import com.openexchange.mail.MailException;
import com.openexchange.mail.config.GlobalTransportConfig;
import com.openexchange.mail.config.MailConfig;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.InfostoreDocumentMailPart;
import com.openexchange.mail.dataobjects.compose.ReferencedMailPart;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.mail.dataobjects.compose.UploadFileMailPart;
import com.openexchange.session.Session;

/**
 * {@link MailTransport} - Provides various operations related to a mail
 * transport.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class MailTransport {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MailTransport.class);

	private static Class<? extends MailTransport> clazz;

	private static MailTransport internalInstance;

	static void setImplementingClass(final Class<? extends MailTransport> clazz) throws MailException {
		MailTransport.clazz = clazz;
		/*
		 * Create internal instance
		 */
		try {
			internalInstance = clazz.getConstructor(new Class[0]).newInstance(new Object[0]);
		} catch (final SecurityException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final NoSuchMethodException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final IllegalArgumentException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final InstantiationException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final IllegalAccessException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final InvocationTargetException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		}
	}

	private static final Class<?>[] CONSTRUCTOR_ARGS = new Class[] { Session.class };

	/**
	 * Gets the proper instance of {@link MailTransport} parameterized with
	 * given session
	 * 
	 * @param session
	 *            The session
	 * @return A proper instance of {@link MailTransport}
	 * @throws MailException
	 *             If instantiation fails
	 */
	public static final MailTransport getInstance(final Session session) throws MailException {
		/*
		 * Create a new mail transport
		 */
		try {
			return clazz.getConstructor(CONSTRUCTOR_ARGS).newInstance(new Object[] { session });
		} catch (final SecurityException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final NoSuchMethodException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final IllegalArgumentException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final InstantiationException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final IllegalAccessException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final InvocationTargetException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		}
	}

	/**
	 * Gets the class name of {@link GlobalTransportConfig} implementation
	 * 
	 * @return The class name of {@link GlobalTransportConfig} implementation
	 */
	public static final String getGlobalTransportConfigClass() {
		return internalInstance.getGlobalTransportConfigClassInternal();
	}

	/**
	 * Gets a new instance of {@link ComposedMailMessage}
	 * 
	 * @return A new instance of {@link ComposedMailMessage}
	 */
	public static final ComposedMailMessage getNewTransportMailMessage() {
		try {
			return internalInstance.getNewTransportMailMessageInternal();
		} catch (final MailException e) {
			LOG.error(e.getLocalizedMessage(), e);
			return null;
		}
	}

	/**
	 * Gets a new instance of {@link UploadFileMailPart}
	 * 
	 * @param uploadFile
	 *            The upload file
	 * @return A new instance of {@link UploadFileMailPart}
	 */
	public static final UploadFileMailPart getNewFilePart(final UploadFile uploadFile) {
		try {
			return internalInstance.getNewFilePartInternal(uploadFile);
		} catch (final MailException e) {
			LOG.error(e.getLocalizedMessage(), e);
			return null;
		}
	}

	/**
	 * Gets a new instance of {@link InfostoreDocumentMailPart}
	 * 
	 * @param documentId
	 *            The infostore document's unique ID
	 * @param session
	 *            The session providing needed user data
	 * @return A new instance of {@link InfostoreDocumentMailPart}
	 */
	public static final InfostoreDocumentMailPart getNewDocumentPart(final int documentId, final Session session) {
		try {
			return internalInstance.getNewDocumentPartInternal(documentId, session);
		} catch (final MailException e) {
			LOG.error(e.getLocalizedMessage(), e);
			return null;
		}
	}

	/**
	 * Gets a new instance of {@link TextBodyMailPart}
	 * 
	 * @param textBody
	 *            The text body
	 * @return A new instance of {@link TextBodyMailPart}
	 */
	public static final TextBodyMailPart getNewTextBodyPart(final String textBody) {
		try {
			return internalInstance.getNewTextBodyPartInternal(textBody);
		} catch (final MailException e) {
			LOG.error(e.getLocalizedMessage(), e);
			return null;
		}
	}

	/**
	 * Gets a new instance of {@link ReferencedMailPart}
	 * 
	 * @param referencedPart
	 *            The referenced part
	 * @param session
	 *            The session providing user data
	 * @return A new instance of {@link ReferencedMailPart}
	 */
	public static final ReferencedMailPart getNewReferencedPart(final MailPart referencedPart, final Session session) {
		try {
			return internalInstance.getNewReferencedPartInternal(referencedPart, session);
		} catch (final MailException e) {
			LOG.error(e.getLocalizedMessage(), e);
			return null;
		}
	}

	/**
	 * Gets a new instance of {@link ReferencedMailPart}
	 * 
	 * @param sequenceId
	 *            The sequence ID in referenced mail
	 * @return A new instance of {@link ReferencedMailPart}
	 */
	public static final ReferencedMailPart getNewReferencedPart(final String sequenceId) {
		try {
			return internalInstance.getNewReferencedPartInternal(sequenceId);
		} catch (final MailException e) {
			LOG.error(e.getLocalizedMessage(), e);
			return null;
		}
	}

	/**
	 * Gets user-specific transport configuration with properly set login and
	 * password
	 * 
	 * @param session
	 *            The session providing needed user data
	 * @return User-specific transport configuration
	 * @throws MailException
	 *             If transport configuration cannot be determined
	 */
	public abstract MailConfig getTransportConfig(Session session) throws MailException;

	/**
	 * Sends a mail message
	 * 
	 * @param transportMail
	 *            The mail message to send (containing necessary header data and
	 *            body)
	 * @param sendType
	 *            The send type
	 * @return The sent mail message
	 * @throws MailException
	 *             If transport fails
	 */
	public abstract MailMessage sendMailMessage(ComposedMailMessage transportMail, ComposeType sendType)
			throws MailException;

	/**
	 * Sends specified message's raw ascii bytes. The given bytes are
	 * interpreted dependent on implementation, but in most cases it's treated
	 * as an rfc822 MIME message.
	 * 
	 * @param asciiBytes
	 *            The raw ascii bytes
	 * @return The sent mail message
	 * @throws MailException
	 *             If sending fails
	 */
	public abstract MailMessage sendRawMessage(byte[] asciiBytes) throws MailException;

	/**
	 * Sends a receipt acknowledgment for the specified message.
	 * 
	 * @param srcMail
	 *            The source mail
	 * @param fromAddr
	 *            The from address (as unicode string). If set to
	 *            <code>null</code>, user's default email address is used as
	 *            value for header <code>From</code>
	 * @throws MailException
	 *             If transport fails
	 */
	public abstract void sendReceiptAck(MailMessage srcMail, String fromAddr) throws MailException;

	/**
	 * Closes this mail transport
	 * 
	 * @throws MailException
	 *             If closing fails
	 */
	public abstract void close() throws MailException;

	/**
	 * Gets the name of {@link GlobalTransportConfig} implementation
	 * 
	 * @return The name of {@link GlobalTransportConfig} implementation
	 */
	protected abstract String getGlobalTransportConfigClassInternal();

	/**
	 * Gets a new instance of {@link ComposedMailMessage}
	 * 
	 * @return A new instance of {@link ComposedMailMessage}
	 * @throws MailException
	 */
	protected abstract ComposedMailMessage getNewTransportMailMessageInternal() throws MailException;

	/**
	 * Gets a new instance of {@link UploadFileMailPart}
	 * 
	 * @param uploadFile
	 *            The upload file
	 * @return A new instance of {@link UploadFileMailPart}
	 * @throws MailException
	 */
	protected abstract UploadFileMailPart getNewFilePartInternal(UploadFile uploadFile) throws MailException;

	/**
	 * Gets a new instance of {@link InfostoreDocumentMailPart}
	 * 
	 * @param documentId
	 *            The infostore document's unique ID
	 * @param session
	 *            The session providing needed user data
	 * @return A new instance of {@link InfostoreDocumentMailPart}
	 * @throws MailException
	 */
	protected abstract InfostoreDocumentMailPart getNewDocumentPartInternal(int documentId, Session session)
			throws MailException;

	/**
	 * Gets a new instance of {@link TextBodyMailPart}
	 * 
	 * @param textBody
	 *            The text body
	 * @return A new instance of {@link TextBodyMailPart}
	 * @throws MailException
	 */
	protected abstract TextBodyMailPart getNewTextBodyPartInternal(String textBody) throws MailException;

	/**
	 * Gets a new instance of {@link ReferencedMailPart}
	 * 
	 * @param referencedPart
	 *            The referenced part
	 * @param session
	 *            The session providing user data
	 * @return A new instance of {@link ReferencedMailPart}
	 * @throws MailException
	 */
	protected abstract ReferencedMailPart getNewReferencedPartInternal(MailPart referencedPart, Session session)
			throws MailException;

	/**
	 * Gets a new instance of {@link ReferencedMailPart}
	 * 
	 * @param sequenceId
	 *            The sequence ID in referenced mail
	 * @return A new instance of {@link ReferencedMailPart}
	 * @throws MailException
	 */
	protected abstract ReferencedMailPart getNewReferencedPartInternal(String sequenceId) throws MailException;

}
