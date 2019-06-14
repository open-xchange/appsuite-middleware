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

package com.openexchange.gmail.send.config;

import com.openexchange.mail.transport.config.MailAccountTransportProperties;
import com.openexchange.mailaccount.MailAccount;

/**
 * {@link MailAccountGmailSendProperties} - Gmail send properties read from mail account with fallback to properties read from properties file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountGmailSendProperties extends MailAccountTransportProperties implements IGmailSendProperties {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailAccountGmailSendProperties.class);

    private static final int PRIMARY = MailAccount.DEFAULT_ID;

    private final int mailAccountId;

    /**
     * Initializes a new {@link MailAccountGmailSendProperties}.
     *
     * @param mailAccount The mail account providing the properties
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws IllegalArgumentException If provided mail account is <code>null</code>
     */
    public MailAccountGmailSendProperties(MailAccount mailAccount, int userId, int contextId) {
        super(mailAccount, userId, contextId);
        this.mailAccountId = mailAccount.getId();
    }

    /**
     * Initializes a new {@link MailAccountGmailSendProperties}.
     *
     * @param accountId The transport account identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public MailAccountGmailSendProperties(int accountId, int userId, int contextId) {
        super(userId, contextId);
        this.mailAccountId = accountId;
    }

    @Override
    public int getConnectionTimeout() {
        String conTimeoutStr = getAccountProperty("com.openexchange.gmail.send.connectionTimeout");
        if (null != conTimeoutStr) {
            try {
                return Integer.parseInt(conTimeoutStr);
            } catch (final NumberFormatException e) {
                LOG.error("Gmail Send Connection Timeout: Invalid value.", e);
                return GmailSendProperties.getInstance().getConnectionTimeout();
            }
        }

        if (mailAccountId == PRIMARY) {
            conTimeoutStr = lookUpProperty("com.openexchange.gmail.send.primary.connectionTimeout");
            if (null != conTimeoutStr) {

                try {
                    return Integer.parseInt(conTimeoutStr);
                } catch (final NumberFormatException e) {
                    LOG.error("Gmail Send Connection Timeout: Invalid value.", e);
                    return GmailSendProperties.getInstance().getConnectionTimeout();
                }
            }
        }

        return lookUpIntProperty("com.openexchange.gmail.send.connectionTimeout", GmailSendProperties.getInstance().getConnectionTimeout());
    }

    @Override
    public int getTimeout() {
        String timeoutStr = properties.get("com.openexchange.gmail.send.timeout");
        if (null != timeoutStr) {
            try {
                return Integer.parseInt(timeoutStr.trim());
            } catch (final NumberFormatException e) {
                LOG.error("Gmail Send Timeout: Invalid value.", e);
                return GmailSendProperties.getInstance().getTimeout();
            }
        }

        if (mailAccountId == PRIMARY) {
            timeoutStr = lookUpProperty("com.openexchange.gmail.send.primary.timeout");
            if (null != timeoutStr) {
                try {
                    return Integer.parseInt(timeoutStr.trim());
                } catch (final NumberFormatException e) {
                    LOG.error("Gmail Send Timeout: Invalid value.", e);
                    return GmailSendProperties.getInstance().getTimeout();
                }
            }
        }

        return lookUpIntProperty("com.openexchange.gmail.send.timeout", GmailSendProperties.getInstance().getTimeout());
    }

    @Override
    public boolean isLogTransport() {
        return lookUpBoolProperty("com.openexchange.gmail.send.logTransport", GmailSendProperties.getInstance().isLogTransport());
    }

}
