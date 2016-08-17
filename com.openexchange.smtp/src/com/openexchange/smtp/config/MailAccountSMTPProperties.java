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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import com.openexchange.mail.transport.config.MailAccountTransportProperties;
import com.openexchange.mailaccount.MailAccount;

/**
 * {@link MailAccountSMTPProperties} - SMTP properties read from mail account with fallback to properties read from properties file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountSMTPProperties extends MailAccountTransportProperties implements ISMTPProperties {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailAccountSMTPProperties.class);

    private static final int PRIMARY = MailAccount.DEFAULT_ID;

    private final int mailAccountId;

    /**
     * Initializes a new {@link MailAccountSMTPProperties}.
     *
     * @param mailAccount The mail account providing the properties
     * @throws IllegalArgumentException If provided mail account is <code>null</code>
     */
    public MailAccountSMTPProperties(final MailAccount mailAccount) {
        super(mailAccount);
        this.mailAccountId = mailAccount.getId();
    }

    /**
     * Initializes a new {@link MailAccountSMTPProperties}.
     *
     * @param accountId The transport account identifier
     */
    public MailAccountSMTPProperties(int accountId) {
        super();
        this.mailAccountId = accountId;
    }

    @Override
    public String getSmtpAuthEnc() {
        String smtpAuthEncStr = properties.get("com.openexchange.smtp.smtpAuthEnc");
        if (null != smtpAuthEncStr) {

            if (Charset.isSupported(smtpAuthEncStr)) {
                return smtpAuthEncStr;
            }
            final String fallback = SMTPProperties.getInstance().getSmtpAuthEnc();
            LOG.error("SMTP Auth Encoding: Unsupported charset \"{}\". Setting to fallback {}", smtpAuthEncStr, fallback);
            return fallback;
        }

        if (mailAccountId == PRIMARY) {
            smtpAuthEncStr = lookUpProperty("com.openexchange.smtp.primary.smtpAuthEnc");
            if (null != smtpAuthEncStr) {

                if (Charset.isSupported(smtpAuthEncStr)) {
                    return smtpAuthEncStr;
                }
                final String fallback = SMTPProperties.getInstance().getSmtpAuthEnc();
                LOG.error("SMTP Auth Encoding: Unsupported charset \"{}\". Setting to fallback {}", smtpAuthEncStr, fallback);
                return fallback;
            }
        }

        return SMTPProperties.getInstance().getSmtpAuthEnc();
    }

    @Override
    public int getSmtpConnectionTimeout() {
        String smtpConTimeoutStr = properties.get("com.openexchange.smtp.smtpConnectionTimeout");
        if (null != smtpConTimeoutStr) {

            try {
                return Integer.parseInt(smtpConTimeoutStr);
            } catch (final NumberFormatException e) {
                LOG.error("SMTP Connection Timeout: Invalid value.", e);
                return SMTPProperties.getInstance().getSmtpConnectionTimeout();
            }
        }

        if (mailAccountId == PRIMARY) {
            smtpConTimeoutStr = lookUpProperty("com.openexchange.smtp.primary.smtpConnectionTimeout");
            if (null != smtpConTimeoutStr) {

                try {
                    return Integer.parseInt(smtpConTimeoutStr);
                } catch (final NumberFormatException e) {
                    LOG.error("SMTP Connection Timeout: Invalid value.", e);
                    return SMTPProperties.getInstance().getSmtpConnectionTimeout();
                }
            }
        }

        return SMTPProperties.getInstance().getSmtpConnectionTimeout();
    }

    @Override
    public String getSmtpLocalhost() {
        String smtpLocalhostStr = properties.get("com.openexchange.smtp.smtpLocalhost");
        if (null != smtpLocalhostStr) {
            return (smtpLocalhostStr.length() == 0) || "null".equalsIgnoreCase(smtpLocalhostStr) ? null : smtpLocalhostStr;
        }

        if (mailAccountId == PRIMARY) {
            smtpLocalhostStr = lookUpProperty("com.openexchange.smtp.primary.smtpLocalhost");
            if (null != smtpLocalhostStr) {
                return (smtpLocalhostStr.length() == 0) || "null".equalsIgnoreCase(smtpLocalhostStr) ? null : smtpLocalhostStr;
            }
        }

        return SMTPProperties.getInstance().getSmtpLocalhost();
    }

    @Override
    public int getSmtpTimeout() {
        String smtpTimeoutStr = properties.get("com.openexchange.smtp.smtpTimeout");
        if (null != smtpTimeoutStr) {
            try {
                return Integer.parseInt(smtpTimeoutStr.trim());
            } catch (final NumberFormatException e) {
                LOG.error("SMTP Timeout: Invalid value.", e);
                return SMTPProperties.getInstance().getSmtpTimeout();
            }
        }

        if (mailAccountId == PRIMARY) {
            smtpTimeoutStr = lookUpProperty("com.openexchange.smtp.primary.smtpTimeout");
            if (null != smtpTimeoutStr) {
                try {
                    return Integer.parseInt(smtpTimeoutStr.trim());
                } catch (final NumberFormatException e) {
                    LOG.error("SMTP Timeout: Invalid value.", e);
                    return SMTPProperties.getInstance().getSmtpTimeout();
                }
            }
        }

        return SMTPProperties.getInstance().getSmtpTimeout();
    }

    @Override
    public boolean isSmtpAuth() {
        String smtpAuthStr = properties.get("com.openexchange.smtp.smtpAuthentication");
        if (null != smtpAuthStr) {
            return Boolean.parseBoolean(smtpAuthStr.trim());
        }

        if (mailAccountId == PRIMARY) {
            smtpAuthStr = lookUpProperty("com.openexchange.smtp.primary.smtpAuthentication");
            if (null != smtpAuthStr) {
                return Boolean.parseBoolean(smtpAuthStr.trim());
            }
        }

        return SMTPProperties.getInstance().isSmtpAuth();
    }

    @Override
    public boolean isSendPartial() {
        String smtpPartialStr = properties.get("com.openexchange.smtp.sendPartial");
        if (null != smtpPartialStr) {
            return Boolean.parseBoolean(smtpPartialStr.trim());
        }

        if (mailAccountId == PRIMARY) {
            smtpPartialStr = lookUpProperty("com.openexchange.smtp.primary.sendPartial");
            if (null != smtpPartialStr) {
                return Boolean.parseBoolean(smtpPartialStr.trim());
            }
        }

        return SMTPProperties.getInstance().isSendPartial();
    }

    @Override
    public boolean isSmtpEnvelopeFrom() {
        if (mailAccountId != PRIMARY) {
            return false;
        }

        String smtpEnvFromStr = properties.get("com.openexchange.smtp.setSMTPEnvelopeFrom");
        if (null == smtpEnvFromStr) {
            return SMTPProperties.getInstance().isSmtpEnvelopeFrom();
        }
        return Boolean.parseBoolean(smtpEnvFromStr);
    }

    @Override
    public boolean isLogTransport() {
        if (mailAccountId != PRIMARY) {
            return false;
        }

        String tmp = properties.get("com.openexchange.smtp.logTransport");
        if (null == tmp) {
            return SMTPProperties.getInstance().isLogTransport();
        }
        return Boolean.parseBoolean(tmp);
    }

    @Override
    public String getSSLProtocols() {
        String tmp = properties.get("com.openexchange.smtp.ssl.protocols");
        if (null != tmp) {
            return tmp.trim();
        }

        if (mailAccountId == PRIMARY) {
            tmp = lookUpProperty("com.openexchange.smtp.primary.ssl.protocols");
            if (null != tmp) {
                return tmp.trim();
            }
        }

        return SMTPProperties.getInstance().getSSLProtocols();
    }

    @Override
    public String getSSLCipherSuites() {
        String tmp = properties.get("com.openexchange.smtp.ssl.ciphersuites");
        if (null != tmp) {
            return tmp.trim();
        }

        if (mailAccountId == PRIMARY) {
            tmp = lookUpProperty("com.openexchange.smtp.primary.ssl.ciphersuites");
            if (null != tmp) {
                return tmp.trim();
            }
        }

        return SMTPProperties.getInstance().getSSLCipherSuites();
    }

}
