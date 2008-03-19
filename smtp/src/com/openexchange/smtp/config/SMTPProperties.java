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

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ConfigurationServiceHolder;
import com.openexchange.mail.api.AbstractProtocolProperties;
import com.openexchange.mail.config.MailConfigException;

/**
 * {@link SMTPProperties}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class SMTPProperties extends AbstractProtocolProperties {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(SMTPProperties.class);

	private static final SMTPProperties instance = new SMTPProperties();

	/**
	 * Gets the singleton instance of {@link SMTPProperties}
	 * 
	 * @return The singleton instance of {@link SMTPProperties}
	 */
	public static SMTPProperties getInstance() {
		return instance;
	}

	/*
	 * Fields for global properties
	 */
	private String smtpLocalhost;

	private boolean smtpAuth;

	private boolean smtpEnvelopeFrom;

	private String smtpAuthEnc;

	private int smtpTimeout;

	private int smtpConnectionTimeout;

	private ConfigurationServiceHolder configurationServiceHolder;

	/**
	 * Initializes a new {@link SMTPProperties}
	 */
	private SMTPProperties() {
		super();
	}

	/**
	 * Gets the configuration service holder
	 * 
	 * @return The configuration service holder
	 */
	public ConfigurationServiceHolder getConfigurationServiceHolder() {
		return configurationServiceHolder;
	}

	/**
	 * Sets the configuration service holder
	 * 
	 * @param configurationServiceHolder
	 *            The configuration service holder
	 */
	public void setConfigurationServiceHolder(final ConfigurationServiceHolder configurationServiceHolder) {
		this.configurationServiceHolder = configurationServiceHolder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.config.AbstractProtocolProperties#loadProperties0()
	 */
	@Override
	protected void loadProperties0() throws MailConfigException {
		final StringBuilder logBuilder = new StringBuilder(1024);
		logBuilder.append("\nLoading global SMTP properties...\n");

		final ConfigurationService configuration = configurationServiceHolder.getService();
		try {
			{
				final String smtpLocalhostStr = configuration.getProperty("com.openexchange.smtp.smtpLocalhost").trim();
				smtpLocalhost = smtpLocalhostStr == null || smtpLocalhostStr.length() == 0
						|| "null".equalsIgnoreCase(smtpLocalhostStr) ? null : smtpLocalhostStr;
				logBuilder.append("\tSMTP Localhost: ").append(smtpLocalhost).append('\n');
			}

			{
				final String smtpAuthStr = configuration.getProperty("com.openexchange.smtp.smtpAuthentication",
						"false").trim();
				smtpAuth = Boolean.parseBoolean(smtpAuthStr);
				logBuilder.append("\tSMTP Authentication: ").append(smtpAuth).append('\n');
			}

			{
				final String smtpEnvFromStr = configuration.getProperty("com.openexchange.smtp.setSMTPEnvelopeFrom",
						"false").trim();
				smtpEnvelopeFrom = Boolean.parseBoolean(smtpEnvFromStr);
				logBuilder.append("\tSet SMTP ENVELOPE-FROM: ").append(smtpEnvelopeFrom).append('\n');
			}

			{
				final String smtpAuthEncStr = configuration.getProperty("com.openexchange.smtp.smtpAuthEnc", "UTF-8")
						.trim();
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
				final String smtpTimeoutStr = configuration.getProperty("com.openexchange.smtp.smtpTimeout", "5000")
						.trim();
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
				final String smtpConTimeoutStr = configuration.getProperty(
						"com.openexchange.smtp.smtpConnectionTimeout", "10000").trim();
				try {
					smtpConnectionTimeout = Integer.parseInt(smtpConTimeoutStr);
					logBuilder.append("\tSMTP Timeout: ").append(smtpConnectionTimeout).append('\n');
				} catch (final NumberFormatException e) {
					smtpConnectionTimeout = 10000;
					logBuilder.append("\tSMTP Timeout: Invalid value \"").append(smtpConTimeoutStr).append(
							"\". Setting to fallback ").append(smtpConnectionTimeout).append('\n');

				}
			}
		} finally {
			configurationServiceHolder.ungetService(configuration);
		}

		logBuilder.append("Global SMTP properties successfully loaded!");
		if (LOG.isInfoEnabled()) {
			LOG.info(logBuilder.toString());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.config.AbstractProtocolProperties#resetFields()
	 */
	@Override
	protected void resetFields() {
		smtpLocalhost = null;
		smtpAuth = false;
		smtpEnvelopeFrom = false;
		smtpAuthEnc = null;
		smtpTimeout = 0;
		smtpConnectionTimeout = 0;
	}

	/**
	 * Gets the lOG
	 *
	 * @return the lOG
	 */
	static org.apache.commons.logging.Log getLOG() {
		return LOG;
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
	 * Gets the smtpAuth
	 *
	 * @return the smtpAuth
	 */
	boolean isSmtpAuth() {
		return smtpAuth;
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
	 * Gets the smtpAuthEnc
	 *
	 * @return the smtpAuthEnc
	 */
	String getSmtpAuthEnc() {
		return smtpAuthEnc;
	}

	/**
	 * Gets the smtpTimeout
	 *
	 * @return the smtpTimeout
	 */
	int getSmtpTimeout() {
		return smtpTimeout;
	}

	/**
	 * Gets the smtpConnectionTimeout
	 *
	 * @return the smtpConnectionTimeout
	 */
	int getSmtpConnectionTimeout() {
		return smtpConnectionTimeout;
	}

}
