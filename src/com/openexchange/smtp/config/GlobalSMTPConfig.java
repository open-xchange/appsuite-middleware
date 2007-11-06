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

import java.nio.charset.Charset;
import java.util.Properties;

import com.openexchange.configuration.SystemConfig;
import com.openexchange.mail.config.GlobalTransportConfig;
import com.openexchange.mail.config.MailConfigException;

/**
 * {@link GlobalSMTPConfig}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class GlobalSMTPConfig extends GlobalTransportConfig {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(GlobalSMTPConfig.class);

	/*
	 * Fields for global properties
	 */
	private String smtpLocalhost;

	private boolean smtpsEnabled;

	private int smtpsPort;

	private boolean smtpAuth;

	private boolean smtpEnvelopeFrom;

	private String smtpAuthEnc;

	private int smtpTimeout;

	private int smtpConnectionTimeout;

	/**
	 * No instantiation
	 */
	public GlobalSMTPConfig() {
		super();
	}

	/*
	 * (Kein Javadoc)
	 * 
	 * @see com.openexchange.mail.config.GlobalTransportConfig#loadGlobalTransportSubConfig()
	 */
	@Override
	protected void loadGlobalTransportSubConfig() throws MailConfigException {
		loadGlobalSMTPConfig();
	}

	private void loadGlobalSMTPConfig() throws MailConfigException {
		final Properties smtpProperties;
		{
			String smtpPropFile = SystemConfig.getProperty("SMTPProperties");
			if (smtpPropFile == null || (smtpPropFile = smtpPropFile.trim()).length() == 0) {
				throw new MailConfigException(new StringBuilder(50).append("Property \"").append("SMTPProperties")
						.append("\" not defined in system.properties").toString());
			}
			smtpProperties = readPropertiesFromFile(smtpPropFile);
		}
		final StringBuilder logBuilder = new StringBuilder(1024);
		logBuilder.append("\nLoading global SMTP properties...\n");

		{
			final String smtpLocalhostStr = smtpProperties.getProperty("smtpLocalhost").trim();
			smtpLocalhost = smtpLocalhostStr == null || smtpLocalhostStr.length() == 0
					|| "null".equalsIgnoreCase(smtpLocalhostStr) ? null : smtpLocalhostStr;
			logBuilder.append("\tSMTP Localhost: ").append(smtpLocalhost).append('\n');
		}

		{
			final String smtpsEnStr = smtpProperties.getProperty("smtps", "false").trim();
			smtpsEnabled = Boolean.parseBoolean(smtpsEnStr);
			logBuilder.append("\tSMTP/S enabled: ").append(smtpsEnabled).append('\n');
		}

		{
			final String smtpsPortStr = smtpProperties.getProperty("smtpsPort", "465").trim();
			try {
				smtpsPort = Integer.parseInt(smtpsPortStr);
				logBuilder.append("\tSMTP/S port: ").append(smtpsPort).append('\n');
			} catch (final NumberFormatException e) {
				smtpsPort = 465;
				logBuilder.append("\tSMTP/S port: Invalid value \"").append(smtpsPortStr).append(
						"\". Setting to fallback ").append(smtpsPort).append('\n');
			}
		}

		{
			final String smtpAuthStr = smtpProperties.getProperty("smtpAuthentication", "false").trim();
			smtpAuth = Boolean.parseBoolean(smtpAuthStr);
			logBuilder.append("\tSMTP Authentication: ").append(smtpAuth).append('\n');
		}

		{
			final String smtpEnvFromStr = smtpProperties.getProperty("setSMTPEnvelopeFrom", "false").trim();
			smtpEnvelopeFrom = Boolean.parseBoolean(smtpEnvFromStr);
			logBuilder.append("\tSet SMTP ENVELOPE-FROM: ").append(smtpEnvelopeFrom).append('\n');
		}

		{
			final String smtpAuthEncStr = smtpProperties.getProperty("smtpAuthEnc", "UTF-8").trim();
			if (Charset.isSupported(smtpAuthEncStr)) {
				smtpAuthEnc = smtpAuthEncStr;
				logBuilder.append("\tSMTP Auth Encoding: ").append(smtpAuthEnc).append('\n');
			} else {
				smtpAuthEnc = "UTF-8";
				logBuilder.append("\tSMTP Auth Encoding: Unsupported charset \"").append(smtpAuthEncStr).append(
						"\". Setting to fallback ").append(smtpEnvelopeFrom).append('\n');
			}
		}

		{
			final String smtpTimeoutStr = smtpProperties.getProperty("smtpTimeout", "5000").trim();
			try {
				smtpTimeout = Integer.parseInt(smtpTimeoutStr);
				logBuilder.append("\tSMTP Timeout: ").append(smtpTimeout).append('\n');
			} catch (final NumberFormatException e) {
				smtpTimeout = 5000;
				logBuilder.append("\tSMTP Timeout: Invalid value \"").append(smtpTimeoutStr).append(
						"\". Setting to fallback ").append(smtpTimeout).append('\n');

			}
		}

		{
			final String smtpConTimeoutStr = smtpProperties.getProperty("smtpConnectionTimeout", "10000").trim();
			try {
				smtpConnectionTimeout = Integer.parseInt(smtpConTimeoutStr);
				logBuilder.append("\tSMTP Timeout: ").append(smtpConnectionTimeout).append('\n');
			} catch (final NumberFormatException e) {
				smtpConnectionTimeout = 10000;
				logBuilder.append("\tSMTP Timeout: Invalid value \"").append(smtpConTimeoutStr).append(
						"\". Setting to fallback ").append(smtpConnectionTimeout).append('\n');

			}
		}

		logBuilder.append("Global SMTP properties successfully loaded!");
		if (LOG.isInfoEnabled()) {
			LOG.info(logBuilder.toString());
		}
	}

	/**
	 * Gets the smtpAuth
	 * 
	 * @return the smtpAuth
	 */
	boolean isSmtpAuth() {
		return smtpAuth;
	}

	/**
	 * Gets the smtpAuthEnc
	 * 
	 * @return the smtpAuthEnc
	 */
	String getSmtpAuthEnc() {
		return smtpAuthEnc;
	}

	/**
	 * Gets the smtpConnectionTimeout
	 * 
	 * @return the smtpConnectionTimeout
	 */
	int getSmtpConnectionTimeout() {
		return smtpConnectionTimeout;
	}

	/**
	 * Gets the smtpEnvelopeFrom
	 * 
	 * @return the smtpEnvelopeFrom
	 */
	boolean isSmtpEnvelopeFrom() {
		return smtpEnvelopeFrom;
	}

	/**
	 * Gets the smtpLocalhost
	 * 
	 * @return the smtpLocalhost
	 */
	String getSmtpLocalhost() {
		return smtpLocalhost;
	}

	/**
	 * Gets the smtpsEnabled
	 * 
	 * @return the smtpsEnabled
	 */
	boolean isSmtpsEnabled() {
		return smtpsEnabled;
	}

	/**
	 * Gets the smtpsPort
	 * 
	 * @return the smtpsPort
	 */
	int getSmtpsPort() {
		return smtpsPort;
	}

	/**
	 * Gets the smtpTimeout
	 * 
	 * @return the smtpTimeout
	 */
	int getSmtpTimeout() {
		return smtpTimeout;
	}

}
