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

package com.openexchange.smtp.config;

import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.mail.config.GlobalTransportConfig;
import com.openexchange.mail.config.MailConfig;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.config.TransportConfig;
import com.openexchange.session.Session;

/**
 * {@link SMTPConfig}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class SMTPConfig extends TransportConfig {

	/*
	 * User-specific fields
	 */
	private String smtpServer;

	private int smtpPort;

	/**
	 * Constructor
	 */
	private SMTPConfig() {
		super();
	}

	/**
	 * Gets the user-specific IMAP configuration
	 * 
	 * @param session
	 *            The session providing needed user data
	 * @return The user-specific IMAP configuration
	 * @throws MailConfigException
	 *             If user-specific IMAP configuration cannot be determined
	 */
	public static SMTPConfig getSmtpConfig(final Session session) throws MailConfigException {
		final SMTPConfig smtpConf = new SMTPConfig();
		/*
		 * Fetch user object and create its IMAP properties
		 */
		final User user = UserStorage.getStorageUser(session.getUserId(), session.getContext());
		fillLoginAndPassword(smtpConf, session, user);
		if (LoginType.GLOBAL.equals(getLoginType())) {
			String smtpServer = MailConfig.getTransportServer();
			if (smtpServer == null) {
				throw new MailConfigException(new StringBuilder(128).append("Property \"").append("transportServer")
						.append("\" not set in mail properties").toString());
			}
			int smtpPort = 25;
			{
				final int pos = smtpServer.indexOf(':');
				if (pos > -1) {
					smtpPort = Integer.parseInt(smtpServer.substring(pos + 1));
					smtpServer = smtpServer.substring(0, pos);
				}
			}
			final String masterPw = MailConfig.getMasterPassword();
			if (masterPw == null) {
				throw new MailConfigException(new StringBuilder().append("Property \"").append("masterPassword")
						.append("\" not set in mail properties").toString());
			}
			smtpConf.smtpServer = smtpServer;
			smtpConf.smtpPort = smtpPort;
		} else if (LoginType.USER.equals(getLoginType())) {
			String smtpServer = user.getSmtpServer();
			int smtpPort = 25;
			{
				final int pos = smtpServer.indexOf(':');
				if (pos > -1) {
					smtpPort = Integer.parseInt(smtpServer.substring(pos + 1));
					smtpServer = smtpServer.substring(0, pos);
				}
			}
			smtpConf.smtpServer = smtpServer;
			smtpConf.smtpPort = smtpPort;
		} else if (LoginType.ANONYMOUS.equals(getLoginType())) {
			String smtpServer = user.getSmtpServer();
			int smtpPort = 25;
			{
				final int pos = smtpServer.indexOf(':');
				if (pos > -1) {
					smtpPort = Integer.parseInt(smtpServer.substring(pos + 1));
					smtpServer = smtpServer.substring(0, pos);
				}
			}
			smtpConf.smtpServer = smtpServer;
			smtpConf.smtpPort = smtpPort;
		}
		return smtpConf;
	}

	/**
	 * Gets the smtpAuth
	 * 
	 * @return the smtpAuth
	 */
	public static boolean isSmtpAuth() {
		return ((GlobalSMTPConfig) GlobalTransportConfig.getTransportInstance()).isSmtpAuth();
	}

	/**
	 * Gets the smtpAuthEnc
	 * 
	 * @return the smtpAuthEnc
	 */
	public static String getSmtpAuthEnc() {
		return ((GlobalSMTPConfig) GlobalTransportConfig.getTransportInstance()).getSmtpAuthEnc();
	}

	/**
	 * Gets the smtpEnvelopeFrom
	 * 
	 * @return the smtpEnvelopeFrom
	 */
	public static boolean isSmtpEnvelopeFrom() {
		return ((GlobalSMTPConfig) GlobalTransportConfig.getTransportInstance()).isSmtpEnvelopeFrom();
	}

	/**
	 * Gets the smtpLocalhost
	 * 
	 * @return the smtpLocalhost
	 */
	public static String getSmtpLocalhost() {
		return ((GlobalSMTPConfig) GlobalTransportConfig.getTransportInstance()).getSmtpLocalhost();
	}

	/**
	 * Gets the smtpsEnabled
	 * 
	 * @return the smtpsEnabled
	 */
	public static boolean isSmtpsEnabled() {
		return ((GlobalSMTPConfig) GlobalTransportConfig.getTransportInstance()).isSmtpsEnabled();
	}

	/**
	 * Gets the smtpsPort
	 * 
	 * @return the smtpsPort
	 */
	public static int getSmtpsPort() {
		return ((GlobalSMTPConfig) GlobalTransportConfig.getTransportInstance()).getSmtpsPort();
	}

	/**
	 * Gets the smtpConnectionTimeout
	 * 
	 * @return the smtpConnectionTimeout
	 */
	public static int getSmtpConnectionTimeout() {
		return ((GlobalSMTPConfig) GlobalTransportConfig.getTransportInstance()).getSmtpConnectionTimeout();
	}

	/**
	 * Gets the smtpTimeout
	 * 
	 * @return the smtpTimeout
	 */
	public static int getSmtpTimeout() {
		return ((GlobalSMTPConfig) GlobalTransportConfig.getTransportInstance()).getSmtpTimeout();
	}

	/**
	 * Gets the smtpPort
	 * 
	 * @return the smtpPort
	 */
	@Override
	public int getPort() {
		return smtpPort;
	}

	/**
	 * Gets the smtpServer
	 * 
	 * @return the smtpServer
	 */
	@Override
	public String getServer() {
		return smtpServer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.config.MailConfig#getCapabilities()
	 */
	@Override
	public int getCapabilities() {
		return 0;
	}
}
