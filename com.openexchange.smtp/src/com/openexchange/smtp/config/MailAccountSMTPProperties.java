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
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws IllegalArgumentException If provided mail account is <code>null</code>
     */
    public MailAccountSMTPProperties(MailAccount mailAccount, int userId, int contextId) {
        super(mailAccount, userId, contextId);
        this.mailAccountId = mailAccount.getId();
    }

    /**
     * Initializes a new {@link MailAccountSMTPProperties}.
     *
     * @param accountId The transport account identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public MailAccountSMTPProperties(int accountId, int userId, int contextId) {
        super(userId, contextId);
        this.mailAccountId = accountId;
    }

    @Override
    public String getSmtpAuthEnc() {
        String smtpAuthEncStr = getAccountProperty("com.openexchange.smtp.smtpAuthEnc");
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

        return lookUpProperty("com.openexchange.smtp.smtpAuthEnc", SMTPProperties.getInstance().getSmtpAuthEnc());
    }

    @Override
    public int getSmtpConnectionTimeout() {
        String smtpConTimeoutStr = getAccountProperty("com.openexchange.smtp.smtpConnectionTimeout");
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

        return lookUpIntProperty("com.openexchange.smtp.smtpConnectionTimeout", SMTPProperties.getInstance().getSmtpConnectionTimeout());
    }

    @Override
    public String getSmtpLocalhost() {
        String smtpLocalhostStr = getAccountProperty("com.openexchange.smtp.smtpLocalhost");
        if (null != smtpLocalhostStr) {
            return (smtpLocalhostStr.length() == 0) || "null".equalsIgnoreCase(smtpLocalhostStr) ? null : smtpLocalhostStr;
        }

        if (mailAccountId == PRIMARY) {
            smtpLocalhostStr = lookUpProperty("com.openexchange.smtp.primary.smtpLocalhost");
            if (null != smtpLocalhostStr) {
                return (smtpLocalhostStr.length() == 0) || "null".equalsIgnoreCase(smtpLocalhostStr) ? null : smtpLocalhostStr;
            }
        }

        return lookUpProperty("com.openexchange.smtp.smtpLocalhost", SMTPProperties.getInstance().getSmtpLocalhost());
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

        return lookUpIntProperty("com.openexchange.smtp.smtpTimeout", SMTPProperties.getInstance().getSmtpTimeout());
    }

    @Override
    public boolean isSmtpAuth() {
        String smtpAuthStr = getAccountProperty("com.openexchange.smtp.smtpAuthentication");
        if (null != smtpAuthStr) {
            return Boolean.parseBoolean(smtpAuthStr.trim());
        }

        if (mailAccountId == PRIMARY) {
            smtpAuthStr = lookUpProperty("com.openexchange.smtp.primary.smtpAuthentication");
            if (null != smtpAuthStr) {
                return Boolean.parseBoolean(smtpAuthStr.trim());
            }
        }

        return lookUpBoolProperty("com.openexchange.smtp.smtpAuthentication", SMTPProperties.getInstance().isSmtpAuth());
    }

    @Override
    public boolean isSendPartial() {
        String smtpPartialStr = getAccountProperty("com.openexchange.smtp.sendPartial");
        if (null != smtpPartialStr) {
            return Boolean.parseBoolean(smtpPartialStr.trim());
        }

        if (mailAccountId == PRIMARY) {
            smtpPartialStr = lookUpProperty("com.openexchange.smtp.primary.sendPartial");
            if (null != smtpPartialStr) {
                return Boolean.parseBoolean(smtpPartialStr.trim());
            }
        }

        return lookUpBoolProperty("com.openexchange.smtp.sendPartial", SMTPProperties.getInstance().isSendPartial());
    }

    @Override
    public boolean isSmtpEnvelopeFrom() {
        if (mailAccountId != PRIMARY) {
            return false;
        }

        return lookUpBoolProperty("com.openexchange.smtp.setSMTPEnvelopeFrom", SMTPProperties.getInstance().isSmtpEnvelopeFrom());
    }

    @Override
    public boolean isLogTransport() {
        if (mailAccountId != PRIMARY) {
            return false;
        }

        return lookUpBoolProperty("com.openexchange.smtp.logTransport", SMTPProperties.getInstance().isLogTransport());
    }

    @Override
    public String getSSLProtocols() {
        String tmp = getAccountProperty("com.openexchange.smtp.ssl.protocols");
        if (null != tmp) {
            return tmp.trim();
        }

        if (mailAccountId == PRIMARY) {
            tmp = lookUpProperty("com.openexchange.smtp.primary.ssl.protocols");
            if (null != tmp) {
                return tmp.trim();
            }
        }

        return lookUpProperty("com.openexchange.smtp.ssl.protocols", SMTPProperties.getInstance().getSSLProtocols());
    }

    @Override
    public String getSSLCipherSuites() {
        String tmp = getAccountProperty("com.openexchange.smtp.ssl.ciphersuites");
        if (null != tmp) {
            return tmp.trim();
        }

        if (mailAccountId == PRIMARY) {
            tmp = lookUpProperty("com.openexchange.smtp.primary.ssl.ciphersuites");
            if (null != tmp) {
                return tmp.trim();
            }
        }

        return lookUpProperty("com.openexchange.smtp.ssl.ciphersuites", SMTPProperties.getInstance().getSSLCipherSuites());
    }

}
