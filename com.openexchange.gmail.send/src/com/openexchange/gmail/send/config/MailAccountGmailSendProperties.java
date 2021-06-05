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
            } catch (NumberFormatException e) {
                LOG.error("Gmail Send Connection Timeout: Invalid value.", e);
                return GmailSendProperties.getInstance().getConnectionTimeout();
            }
        }

        if (mailAccountId == PRIMARY) {
            conTimeoutStr = lookUpProperty("com.openexchange.gmail.send.primary.connectionTimeout");
            if (null != conTimeoutStr) {

                try {
                    return Integer.parseInt(conTimeoutStr);
                } catch (NumberFormatException e) {
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
            } catch (NumberFormatException e) {
                LOG.error("Gmail Send Timeout: Invalid value.", e);
                return GmailSendProperties.getInstance().getTimeout();
            }
        }

        if (mailAccountId == PRIMARY) {
            timeoutStr = lookUpProperty("com.openexchange.gmail.send.primary.timeout");
            if (null != timeoutStr) {
                try {
                    return Integer.parseInt(timeoutStr.trim());
                } catch (NumberFormatException e) {
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
