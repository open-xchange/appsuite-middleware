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

package com.openexchange.spamhandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.openexchange.mail.MailException;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.session.Session;

/**
 * {@link SpamHandlerRegistry}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class SpamHandlerRegistry {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(SpamHandlerRegistry.class);

	/**
	 * Concurrent map for spam handlers
	 */
	private static final Map<String, SpamHandler> spamHandlers = new ConcurrentHashMap<String, SpamHandler>();

	/**
	 * Initializes a new {@link SpamHandlerRegistry}
	 */
	private SpamHandlerRegistry() {
		super();
	}

	/**
	 * Gets the spam handler appropriate for specified session.
	 * 
	 * @param session
	 *            The session
	 * @return The appropriate spam handler
	 * @throws MailException
	 *             If no supporting spam handler can be found
	 */
	public static SpamHandler getSpamHandlerBySession(final Session session) throws MailException {
		return MailProviderRegistry.getMailProviderBySession(session).getSpamHandler();
	}

	/**
	 * Gets the spam handler appropriate for specified registration name.
	 * <p>
	 * If specified registration name is <code>null</code> or equals
	 * {@link SpamHandler#SPAM_HANDLER_FALLBACK},
	 * {@link NoSpamHandler#getInstance()} is returned.
	 * 
	 * @param registrationName
	 *            The spam handler's registration name
	 * @return The appropriate spam handler or <code>null</code>
	 */
	public static SpamHandler getSpamHandler(final String registrationName) {
		if (null == registrationName) {
			if (LOG.isWarnEnabled()) {
				LOG.warn(new StringBuilder(64).append("Given registration name is null. Using fallback spam handler '")
						.append(SpamHandler.SPAM_HANDLER_FALLBACK).append('\'').toString());
			}
			return NoSpamHandler.getInstance();
		} else if (SpamHandler.SPAM_HANDLER_FALLBACK.equals(registrationName)) {
			return NoSpamHandler.getInstance();
		}
		final SpamHandler spamHandler = spamHandlers.get(registrationName);
		if (null == spamHandler) {
			if (LOG.isWarnEnabled()) {
				LOG.warn(new StringBuilder(64).append("No spam handler found for registration name '").append(
						registrationName).append("'. Using fallback '").append(SpamHandler.SPAM_HANDLER_FALLBACK)
						.append('\'').toString());
			}
			return NoSpamHandler.getInstance();
		}
		return spamHandler;
	}

	/**
	 * Registers a spam handler
	 * 
	 * @param registrationName
	 *            The spam handler's registration name
	 * @param spamHandler
	 *            The spam handler to register
	 * @return <code>true</code> if spam handler has been successfully
	 *         registered and no other spam handler uses the same registration
	 *         name; otherwise <code>false</code>
	 */
	public static boolean registerSpamHandler(final String registrationName, final SpamHandler spamHandler) {
		if (null == registrationName || SpamHandler.SPAM_HANDLER_FALLBACK.equals(registrationName)) {
			return false;
		} else if (spamHandlers.containsKey(registrationName)) {
			return false;
		}
		try {
			/*
			 * Add to registry
			 */
			spamHandlers.put(registrationName, spamHandler);
			return true;
		} catch (final RuntimeException t) {
			LOG.error(t.getMessage(), t);
			return false;
		}
	}

	/**
	 * Unregisters all spam handlers
	 */
	public static void unregisterAll() {
		/*
		 * Clear registry
		 */
		spamHandlers.clear();
	}

	/**
	 * Unregisters the spam handler
	 * 
	 * @param spamHandler
	 *            The spam handler to unregister
	 * @return The unregistered spam handler, or <code>null</code>
	 */
	public static SpamHandler unregisterSpamHandler(final SpamHandler spamHandler) {
		/*
		 * Unregister
		 */
		return spamHandlers.remove(spamHandler.getSpamHandlerName());
	}

	/**
	 * Unregisters the spam handler registered by specified name
	 * 
	 * @param registrationName
	 *            The registration name
	 * @return The unregistered instance of {@link SpamHandler}, or
	 *         <code>null</code> if there was no spam handler supporting
	 *         registered by specified name
	 */
	public static SpamHandler unregisterSpamHandlerByName(final String registrationName) {
		/*
		 * Unregister
		 */
		return spamHandlers.remove(registrationName);
	}
}
