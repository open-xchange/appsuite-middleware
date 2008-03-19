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

package com.openexchange.mail;

import static com.openexchange.mail.utils.ProviderUtility.extractProtocol;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.session.Session;

/**
 * {@link MailProviderRegistry}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MailProviderRegistry {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MailProviderRegistry.class);

	/**
	 * Concurrent map used as set for mail providers
	 */
	private static final Map<Protocol, MailProvider> providers = new ConcurrentHashMap<Protocol, MailProvider>();

	/**
	 * Initializes a new {@link MailProviderRegistry}
	 */
	private MailProviderRegistry() {
		super();
	}

	/**
	 * Gets the mail provider appropriate for specified session.
	 * 
	 * @param session
	 *            The session
	 * @return The appropriate mail provider
	 * @throws MailException
	 *             If no supporting mail provider can be found
	 */
	public static MailProvider getMailProviderBySession(final Session session) throws MailException {
		MailProvider provider;
		try {
			provider = (MailProvider) session.getParameter(MailSessionParameterNames.PARAM_MAIL_PROVIDER);
		} catch (final ClassCastException e) {
			/*
			 * Probably caused by bundle update(s)
			 */
			provider = null;
		}
		final String protocol = extractProtocol(MailConfig.getMailServerURL(session), MailProvider.PROTOCOL_FALLBACK);
		if (null != provider && !provider.isDeprecated() && provider.supportsProtocol(protocol)) {
			return provider;
		}
		provider = getMailProvider(protocol);
		if (null == provider) {
			throw new MailException(MailException.Code.UNKNOWN_PROTOCOL, MailConfig.getMailServerURL(session));
		}
		session.setParameter(MailSessionParameterNames.PARAM_MAIL_PROVIDER, provider);
		return provider;
	}

	/**
	 * Gets the mail provider appropriate for specified mail server URL.
	 * <p>
	 * The given URL should match pattern
	 * 
	 * <pre>
	 * &lt;protocol&gt;://&lt;host&gt;(:&lt;port&gt;)?
	 * </pre>
	 * 
	 * The protocol should be present. Otherwise the configured fallback is used
	 * as protocol.
	 * 
	 * @param serverUrl
	 *            The mail server URL
	 * @return The appropriate mail provider
	 */
	public static MailProvider getMailProviderByURL(final String serverUrl) {
		/*
		 * Get appropriate provider
		 */
		return getMailProvider(extractProtocol(serverUrl, MailProvider.PROTOCOL_FALLBACK));
	}

	/**
	 * Gets the mail provider appropriate for specified protocol.
	 * 
	 * @param protocolName
	 *            The mail protocol; e.g. <code>"imap"</code>
	 * @return The appropriate mail provider
	 */
	public static MailProvider getMailProvider(final String protocolName) {
		if (null == protocolName) {
			return null;
		}
		for (final Iterator<Map.Entry<Protocol, MailProvider>> iter = providers.entrySet().iterator(); iter.hasNext();) {
			final Map.Entry<Protocol, MailProvider> entry = iter.next();
			if (entry.getKey().isSupported(protocolName)) {
				return entry.getValue();
			}
		}
		return null;
	}

	/**
	 * Registers a mail provider and performs its start-up actions
	 * 
	 * @param protocol
	 *            The mail protocol's string representation; e.g.
	 *            <code>"imap_imaps"</code>
	 * @param provider
	 *            The mail provider to register
	 * @return <code>true</code> if mail provider has been successfully
	 *         registered and no other mail provider supports the same protocol;
	 *         otherwise <code>false</code>
	 * @throws MailException
	 *             If provider's start-up fails
	 */
	public static boolean registerMailProvider(final String protocol, final MailProvider provider) throws MailException {
		final Protocol p = Protocol.parseProtocol(protocol);
		if (providers.containsKey(p)) {
			return false;
		}
		try {
			/*
			 * Startup
			 */
			provider.startUp();
			provider.setDeprecated(false);
			/*
			 * Add to registry
			 */
			providers.put(p, provider);
			return true;
		} catch (final MailException e) {
			throw e;
		} catch (final RuntimeException t) {
			LOG.error(t.getMessage(), t);
			return false;
		}
	}

	/**
	 * Unregisters all mail providers
	 */
	public static void unregisterAll() {
		for (final Iterator<MailProvider> iter = providers.values().iterator(); iter.hasNext();) {
			final MailProvider provider = iter.next();
			/*
			 * Perform shutdown
			 */
			try {
				provider.setDeprecated(true);
				provider.shutDown();
			} catch (final MailException e) {
				LOG.error("Mail connection implementation could not be shut down", e);
			} catch (final RuntimeException t) {
				LOG.error("Mail connection implementation could not be shut down", t);
			}
		}
		/*
		 * Clear registry
		 */
		providers.clear();
	}

	/**
	 * Unregisters the mail provider
	 * 
	 * @param provider
	 *            The mail provider to unregister
	 * @return The unregistered mail provider, or <code>null</code>
	 * @throws MailException
	 *             If provider's shut-down fails
	 */
	public static MailProvider unregisterMailProvider(final MailProvider provider) throws MailException {
		if (!providers.containsKey(provider.getProtocol())) {
			return null;
		}
		/*
		 * Unregister
		 */
		final MailProvider removed = providers.remove(provider);
		if (null == removed) {
			return null;
		}
		/*
		 * Perform shutdown
		 */
		try {
			removed.setDeprecated(true);
			removed.shutDown();
			return removed;
		} catch (final MailException e) {
			throw e;
		} catch (final RuntimeException t) {
			LOG.error(t.getMessage(), t);
			return removed;
		}
	}

	/**
	 * Unregisters the mail provider supporting specified protocol
	 * 
	 * @param protocol
	 *            The protocol
	 * @return The unregistered instance of {@link MailProvider}, or
	 *         <code>null</code> if there was no provider supporting specified
	 *         protocol
	 * @throws MailException
	 *             If provider's shut-down fails
	 */
	public static MailProvider unregisterMailProviderByProtocol(final String protocol) throws MailException {
		for (final Iterator<Map.Entry<Protocol, MailProvider>> iter = providers.entrySet().iterator(); iter.hasNext();) {
			final Map.Entry<Protocol, MailProvider> entry = iter.next();
			if (entry.getKey().isSupported(protocol)) {
				iter.remove();
				entry.getValue().setDeprecated(true);
				entry.getValue().shutDown();
				return entry.getValue();
			}
		}
		return null;
	}
}
