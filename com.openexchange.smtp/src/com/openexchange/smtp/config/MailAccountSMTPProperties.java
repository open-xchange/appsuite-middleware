/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.smtp.config;

import com.openexchange.java.CharsetDetector;
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
            if (CharsetDetector.isValid(smtpAuthEncStr)) {
                return smtpAuthEncStr;
            }
            final String fallback = SMTPProperties.getInstance().getSmtpAuthEnc();
            LOG.error("SMTP Auth Encoding: Unsupported charset \"{}\". Setting to fallback {}", smtpAuthEncStr, fallback);
            return fallback;
        }

        if (mailAccountId == PRIMARY) {
            smtpAuthEncStr = lookUpProperty("com.openexchange.smtp.primary.smtpAuthEnc");
            if (null != smtpAuthEncStr) {

                if (CharsetDetector.isValid(smtpAuthEncStr)) {
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
            } catch (NumberFormatException e) {
                LOG.error("SMTP Connection Timeout: Invalid value.", e);
                return SMTPProperties.getInstance().getSmtpConnectionTimeout();
            }
        }

        if (mailAccountId == PRIMARY) {
            smtpConTimeoutStr = lookUpProperty("com.openexchange.smtp.primary.smtpConnectionTimeout");
            if (null != smtpConTimeoutStr) {

                try {
                    return Integer.parseInt(smtpConTimeoutStr);
                } catch (NumberFormatException e) {
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

        smtpLocalhostStr = lookUpProperty("com.openexchange.smtp.smtpLocalhost", SMTPProperties.getInstance().getSmtpLocalhost());
        return (smtpLocalhostStr == null) || (smtpLocalhostStr.length() == 0) || "null".equalsIgnoreCase(smtpLocalhostStr) ? null : smtpLocalhostStr;
    }

    @Override
    public int getSmtpTimeout() {
        String smtpTimeoutStr = properties.get("com.openexchange.smtp.smtpTimeout");
        if (null != smtpTimeoutStr) {
            try {
                return Integer.parseInt(smtpTimeoutStr.trim());
            } catch (NumberFormatException e) {
                LOG.error("SMTP Timeout: Invalid value.", e);
                return SMTPProperties.getInstance().getSmtpTimeout();
            }
        }

        if (mailAccountId == PRIMARY) {
            smtpTimeoutStr = lookUpProperty("com.openexchange.smtp.primary.smtpTimeout");
            if (null != smtpTimeoutStr) {
                try {
                    return Integer.parseInt(smtpTimeoutStr.trim());
                } catch (NumberFormatException e) {
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

    @Override
    public String getPrimaryAddressHeader() {
        // Only applies for the primary mail account
        if (mailAccountId != PRIMARY) {
            return null;
        }

        String tmp = getAccountProperty("com.openexchange.smtp.setPrimaryAddressHeader");
        if (null != tmp) {
            return tmp.trim();
        }

        return lookUpProperty("com.openexchange.smtp.setPrimaryAddressHeader", SMTPProperties.getInstance().getPrimaryAddressHeader());
    }

}
