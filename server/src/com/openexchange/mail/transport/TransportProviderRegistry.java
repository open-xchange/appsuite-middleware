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

import static com.openexchange.mail.utils.ProviderUtility.extractProtocol;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.openexchange.mail.MailException;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.Protocol;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.transport.config.TransportConfig;
import com.openexchange.session.Session;

/**
 * {@link TransportProviderRegistry}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class TransportProviderRegistry {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(TransportProviderRegistry.class);

	/**
	 * Concurrent map used as set for transport providers
	 */
	private static final Map<Protocol, TransportProvider> providers = new ConcurrentHashMap<Protocol, TransportProvider>();

	/**
	 * Initializes a new {@link TransportProviderRegistry}
	 */
	private TransportProviderRegistry() {
		super();
	}

	/**
	 * Gets the transport provider appropriate for specified session
	 * 
	 * @param session
	 *            The session
	 * @return The appropriate transport provider
	 * @throws MailException
	 *             If no supporting transport provider can be found
	 */
	public static TransportProvider getTransportProviderBySession(final Session session) throws MailException {
		TransportProvider provider;
		try {
			provider = (TransportProvider) session.getParameter(MailSessionParameterNames.PARAM_TRANSPORT_PROVIDER);
		} catch (final ClassCastException e) {
			/*
			 * Probably caused by bundle update(s)
			 */
			provider = null;
		}
		final String protocol = extractProtocol(TransportConfig.getTransportServerURL(session),
				TransportProvider.PROTOCOL_FALLBACK);
		if (null != provider && !provider.isDeprecated() && provider.supportsProtocol(protocol)) {
			return provider;
		}
		provider = getTransportProvider(protocol);
		if (null == provider) {
			throw new MailException(MailException.Code.UNKNOWN_PROTOCOL, TransportConfig.getTransportServerURL(session));
		}
		session.setParameter(MailSessionParameterNames.PARAM_TRANSPORT_PROVIDER, provider);
		return provider;
	}

	/**
	 * Gets the transport provider appropriate for specified mail server URL.
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
	 * @return The appropriate transport provider
	 */
	public static TransportProvider getTransportProviderByURL(final String serverUrl) {
		/*
		 * Get appropriate provider
		 */
		return getTransportProvider(extractProtocol(serverUrl, TransportProvider.PROTOCOL_FALLBACK));
	}

	/**
	 * Gets the transport provider appropriate for specified protocol.
	 * 
	 * @param protocol
	 *            The mail protocol
	 * @return The appropriate transport provider
	 */
	public static TransportProvider getTransportProvider(final String protocol) {
		if (null == protocol) {
			return null;
		}
		for (final Iterator<Map.Entry<Protocol, TransportProvider>> iter = providers.entrySet().iterator(); iter
				.hasNext();) {
			final Map.Entry<Protocol, TransportProvider> entry = iter.next();
			if (entry.getKey().isSupported(protocol)) {
				return entry.getValue();
			}
		}
		return null;
	}

	/**
	 * Registers a transport provider and performs its start-up actions
	 * 
	 * @param protocol
	 *            The transport protocol's string representation; e.g.
	 *            <code>"smtp_smtps"</code>
	 * @param provider
	 *            The transport provider to register
	 * @return <code>true</code> if transport provider has been successfully
	 *         registered and no other transport provider supports the same
	 *         protocol; otherwise <code>false</code>
	 * @throws MailException
	 *             If provider's start-up fails
	 */
	public static boolean registerTransportProvider(final String protocol, final TransportProvider provider)
			throws MailException {
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
	 * Unregisters all transport providers
	 */
	public static void unregisterAll() {
		for (final Iterator<TransportProvider> iter = providers.values().iterator(); iter.hasNext();) {
			final TransportProvider provider = iter.next();
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
	 * Unregisters the transport provider
	 * 
	 * @param provider
	 *            The transport provider to unregister
	 * @return The unregistered transport provider, or <code>null</code>
	 * @throws MailException
	 *             If provider's shut-down fails
	 */
	public static TransportProvider unregisterTransportProvider(final TransportProvider provider) throws MailException {
		if (!providers.containsKey(provider.getProtocol())) {
			return null;
		}
		/*
		 * Unregister
		 */
		final TransportProvider removed = providers.remove(provider);
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
	 * Unregisters the transport provider supporting specified protocol
	 * 
	 * @param protocol
	 *            The protocol
	 * @return The unregistered instance of {@link TransportProvider}, or
	 *         <code>null</code> if there was no provider supporting specified
	 *         protocol
	 * @throws MailException
	 *             If provider's shut-down fails
	 */
	public static TransportProvider unregisterTransportProviderByProtocol(final String protocol) throws MailException {
		for (final Iterator<Map.Entry<Protocol, TransportProvider>> iter = providers.entrySet().iterator(); iter
				.hasNext();) {
			final Map.Entry<Protocol, TransportProvider> entry = iter.next();
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
