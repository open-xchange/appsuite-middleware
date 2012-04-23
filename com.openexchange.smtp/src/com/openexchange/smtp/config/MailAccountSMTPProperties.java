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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.mail.transport.config.MailAccountTransportProperties;
import com.openexchange.mailaccount.MailAccount;

/**
 * {@link MailAccountSMTPProperties} - SMTP properties read from mail account with fallback to properties read from properties file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountSMTPProperties extends MailAccountTransportProperties implements ISMTPProperties {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(MailAccountSMTPProperties.class));

    private final MailAccount mailAccount;

    /**
     * Initializes a new {@link MailAccountSMTPProperties}.
     *
     * @param mailAccount The mail account providing the properties
     * @throws IllegalArgumentException If provided mail account is <code>null</code>
     */
    public MailAccountSMTPProperties(final MailAccount mailAccount) {
        super(mailAccount);
        this.mailAccount = mailAccount;
    }

    @Override
    public String getSmtpAuthEnc() {
        final String smtpAuthEncStr = properties.get("com.openexchange.smtp.smtpAuthEnc");
        if (null == smtpAuthEncStr) {
            return SMTPProperties.getInstance().getSmtpAuthEnc();
        }

        if (Charset.isSupported(smtpAuthEncStr)) {
            return smtpAuthEncStr;
        }
        final String fallback = SMTPProperties.getInstance().getSmtpAuthEnc();
        LOG.error(new StringBuilder(64).append("SMTP Auth Encoding: Unsupported charset \"").append(smtpAuthEncStr).append(
            "\". Setting to fallback ").append(fallback));
        return fallback;
    }

    @Override
    public int getSmtpConnectionTimeout() {
        final String smtpConTimeoutStr = properties.get("com.openexchange.smtp.smtpConnectionTimeout");
        if (null == smtpConTimeoutStr) {
            return SMTPProperties.getInstance().getSmtpConnectionTimeout();
        }

        try {
            return Integer.parseInt(smtpConTimeoutStr);
        } catch (final NumberFormatException e) {
            LOG.error("SMTP Connection Timeout: Invalid value.", e);
            return SMTPProperties.getInstance().getSmtpConnectionTimeout();
        }
    }

    @Override
    public String getSmtpLocalhost() {
        final String smtpLocalhostStr = properties.get("com.openexchange.smtp.smtpLocalhost");
        if (null == smtpLocalhostStr) {
            return SMTPProperties.getInstance().getSmtpLocalhost();
        }

        return (smtpLocalhostStr.length() == 0) || "null".equalsIgnoreCase(smtpLocalhostStr) ? null : smtpLocalhostStr;
    }

    @Override
    public int getSmtpTimeout() {
        final String smtpTimeoutStr = properties.get("com.openexchange.smtp.smtpTimeout");
        if (null == smtpTimeoutStr) {
            return SMTPProperties.getInstance().getSmtpTimeout();
        }

        try {
            return Integer.parseInt(smtpTimeoutStr);
        } catch (final NumberFormatException e) {
            LOG.error("SMTP Timeout: Invalid value.", e);
            return SMTPProperties.getInstance().getSmtpTimeout();
        }
    }

    @Override
    public boolean isSmtpAuth() {
        final String smtpAuthStr = properties.get("com.openexchange.smtp.smtpAuthentication");
        if (null == smtpAuthStr) {
            return SMTPProperties.getInstance().isSmtpAuth();
        }

        return Boolean.parseBoolean(smtpAuthStr);
    }

    @Override
    public boolean isSmtpEnvelopeFrom() {
        final boolean retval;
        if (mailAccount.getId() == 0) {
            final String smtpEnvFromStr = properties.get("com.openexchange.smtp.setSMTPEnvelopeFrom");
            if (null == smtpEnvFromStr) {
                return SMTPProperties.getInstance().isSmtpEnvelopeFrom();
            }
            retval = Boolean.parseBoolean(smtpEnvFromStr);
        } else {
            retval = false;
        }
        return retval;
    }
}
