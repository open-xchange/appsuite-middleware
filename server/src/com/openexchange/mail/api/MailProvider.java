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

package com.openexchange.mail.api;

import com.openexchange.mail.MailException;
import com.openexchange.mail.Protocol;
import com.openexchange.mail.permission.DefaultMailPermission;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.spamhandler.SpamHandler;
import com.openexchange.spamhandler.SpamHandlerRegistry;

/**
 * {@link MailProvider} - The main intention of the provider class is to make
 * the implementing classes available which define the abstract classes of mail
 * API.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class MailProvider {

	/**
	 * The protocol fallback if URL does not contain a protocol
	 * <p>
	 * TODO: Make configurable
	 */
	public static final String PROTOCOL_FALLBACK = "imap";

	private final int hashCode;

	private boolean deprecated;

	/**
	 * Initializes a new {@link MailProvider}
	 */
	protected MailProvider() {
		super();
		hashCode = getProtocol().hashCode();
	}

	@Override
	public final boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (!(obj instanceof MailProvider)) {
			return false;
		}
		final MailProvider other = (MailProvider) obj;
		if (getProtocol() == null) {
			if (other.getProtocol() != null) {
				return false;
			}
		} else if (!getProtocol().equals(other.getProtocol())) {
			return false;
		}
		return true;
	}

	@Override
	public final int hashCode() {
		return hashCode;
	}

	/**
	 * Checks if this provider is deprecated; any cached references should be
	 * discarded
	 * 
	 * @return <code>true</code> if deprecated; otherwise <code>false</code>
	 */
	public boolean isDeprecated() {
		return deprecated;
	}

	/**
	 * Sets the deprecated flag
	 * 
	 * @param deprecated
	 *            <code>true</code> if deprecated; otherwise
	 *            <code>false</code>
	 */
	public void setDeprecated(final boolean deprecated) {
		this.deprecated = deprecated;
	}

	/**
	 * Performs provider's start-up
	 * 
	 * @throws MailException
	 *             If start-up fails
	 */
	public void startUp() throws MailException {
		getProtocolProperties().loadProperties();
		MailAccess.startupImpl(getMailAccessClass());
	}

	/**
	 * Performs provider's shut-down
	 * 
	 * @throws MailException
	 *             if shut-down fails
	 */
	public void shutDown() throws MailException {
		MailAccess.shutdownImpl(getMailAccessClass());
		getProtocolProperties().resetProperties();
	}

	/**
	 * Gets the implementation-specific class of {@link MailPermission}.
	 * <p>
	 * Returns {@link DefaultMailPermission} class if mailing system does not
	 * support permission(s). Overwrite if needed.
	 * 
	 * @return The class of {@link MailPermission} implementation
	 */
	public Class<? extends MailPermission> getMailPermissionClass() {
		return DefaultMailPermission.class;
	}

	/**
	 * Gets the spam handler used by this mail provider.
	 * 
	 * @return The spam handler
	 */
	public final SpamHandler getSpamHandler() {
		return SpamHandlerRegistry.getSpamHandler(getSpamHandlerName());
	}

	/**
	 * Gets this mail provider's protocol
	 * 
	 * @return The protocol
	 */
	public abstract Protocol getProtocol();

	/**
	 * Gets the unique registration name of the spam handler that shall be used
	 * by this mail provider.
	 * <p>
	 * If <code>null</code> is returned, the spam handler associated with
	 * {@link SpamHandler#SPAM_HANDLER_FALLBACK} is used.
	 * 
	 * @return The registration name of the spam handler
	 */
	protected abstract String getSpamHandlerName();

	/**
	 * Checks if this mail provider supports the given protocol (which is either
	 * in secure or non-secure notation).
	 * <p>
	 * This is a convenience method that invokes
	 * {@link Protocol#isSupported(String)}
	 * 
	 * @param protocol
	 *            The protocol
	 * @return <code>true</code> if supported; otherwise <code>false</code>
	 */
	public final boolean supportsProtocol(final String protocol) {
		return getProtocol().isSupported(protocol);
	}

	/**
	 * Gets the class implementing {@link MailAccess}
	 * 
	 * @return The class implementing {@link MailAccess}
	 */
	public abstract Class<? extends MailAccess<?, ?>> getMailAccessClass();

	/**
	 * Gets the protocol properties
	 * 
	 * @return The protocol properties
	 */
	protected abstract AbstractProtocolProperties getProtocolProperties();

}
