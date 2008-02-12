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

import com.openexchange.mail.MailException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.session.Session;

/**
 * {@link MailTransport} - Provides various operations related to a mail
 * transport.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class MailTransport {

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

	/**
	 * Triggers all implementation-specific startup actions
	 * 
	 * @throws MailException
	 */
	static final void startup() throws MailException {
		internalInstance.startupInternal();
	}

	/**
	 * Triggers all implementation-specific shutdown actions
	 * 
	 * @throws MailException
	 */
	static final void shutdown() throws MailException {
		internalInstance.shutdownInternal();
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
	 * Trigger all necessary startup actions
	 * 
	 * @throws MailException
	 *             If startup actions fail
	 */
	protected abstract void startupInternal() throws MailException;

	/**
	 * Trigger all necessary shutdown actions
	 * 
	 * @throws MailException
	 *             If shutdown actions fail
	 */
	protected abstract void shutdownInternal() throws MailException;
}
