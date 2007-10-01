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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openexchange.configuration.SystemConfig;
import com.openexchange.mail.MailConnection;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.config.MailConfig;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.sessiond.SessionObject;

/**
 * {@link MailTransport}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class MailTransport<T extends MailMessage> {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MailTransport.class);

	private static final Lock LOCK_INIT = new ReentrantLock();

	private static Class<? extends MailTransport> clazz;

	private static final AtomicBoolean initialized = new AtomicBoolean();

	/**
	 * Initializes the mail transport
	 * 
	 * @throws MailException
	 *             If implementing class cannot be found
	 */
	public static final void init() throws MailException {
		if (!initialized.get()) {
			LOCK_INIT.lock();
			try {
				if (clazz == null) {
					final String className = SystemConfig.getProperty(SystemConfig.Property.MailTransportProtocol);
					try {
						if (className == null) {
							/*
							 * Fallback
							 */
							if (LOG.isWarnEnabled()) {
								LOG.warn("Using fallback \"com.openexchange.mail.transport.smtp.SMTPTransport\"");
							}
							clazz = Class.forName("com.openexchange.mail.transport.smtp.SMTPTransport").asSubclass(
									MailTransport.class);
							initialized.set(true);
							return;
						}
						clazz = Class.forName(className).asSubclass(MailTransport.class);
					} catch (final ClassNotFoundException e) {
						throw new MailException(MailException.Code.INITIALIZATION_PROBLEM, e, new Object[0]);
					}
					initialized.set(true);
				}
			} finally {
				LOCK_INIT.unlock();
			}
		}
	}

	private static final Class[] CONSTRUCTOR_ARGS = new Class[] { SessionObject.class, MailConnection.class };

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
	public static final MailTransport getInstance(final SessionObject session, final MailConnection mailConnection)
			throws MailException {
		if (!initialized.get()) {
			init();
		}
		/*
		 * Create a new mail transport
		 */
		try {
			return clazz.getConstructor(CONSTRUCTOR_ARGS).newInstance(new Object[] { session, mailConnection });
		} catch (SecurityException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (NoSuchMethodException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (IllegalArgumentException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (InstantiationException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (IllegalAccessException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (InvocationTargetException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
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
	public abstract MailConfig getTransportConfig(SessionObject session) throws MailException;

	/**
	 * Sends a mail message
	 * 
	 * @param mail
	 *            The mail message to send (containing necessary header data and
	 *            body)
	 * @param sendType
	 *            The send type
	 * @return The mail's unique path in mailbox if stored; meaning either a
	 *         draft message has been "sent" or a message copy has been appended
	 *         to <i>Sent</i> folder. If no local copy is available,
	 *         {@link MailPath#NULL} is returned
	 * @throws MailException
	 *             If transport fails
	 */
	public abstract MailPath sendMailMessage(T mail, SendType sendType) throws MailException;

	/**
	 * Sends specified message's raw ascii bytes. The given bytes are
	 * interpreted dependent on implementation, but in most cases it's treated
	 * as an rfc822 MIME message.
	 * 
	 * @param asciiBytes
	 *            The raw ascii bytes
	 * @return The mail's unique path in mailbox if stored; meaning either a
	 *         draft message has been "sent" or a message copy has been appended
	 *         to <i>Sent</i> folder. If no local copy is available,
	 *         {@link MailPath#NULL} is returned
	 * @throws MailException
	 */
	public abstract MailPath sendRawMessage(byte[] asciiBytes) throws MailException;

	/**
	 * Sends a receipt acknowledgement for the message located in given folder
	 * with given UID.
	 * 
	 * @param fullname
	 *            The folder fullname
	 * @param msgUID
	 *            The message UID
	 * @param fromAddr
	 *            The from address (as unicode string). If set to
	 *            <code>null</code>, user's default email address is used as
	 *            value for header <code>From</code>
	 * @throws MailException
	 *             If transport fails
	 */
	public abstract void sendReceiptAck(String fullname, long msgUID, String fromAddr) throws MailException;
}
