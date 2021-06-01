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

package com.openexchange.imap;

import java.io.IOException;
import java.util.Map;
import javax.mail.internet.idn.IDNA;
import com.openexchange.exception.OXException;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.ping.IMAPCapabilityAndGreetingCache;
import com.openexchange.imap.util.HostAndPort;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mailaccount.MailAccount;

/**
 * {@link IMAPServerInfo} - IMAP server information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class IMAPServerInfo {

    /**
     * Gets the IMAP server information for given configuration
     *
     * @param imapConfig The configuration
     * @param accountId The account identifier
     * @return The IMAP server information
     * @throws OXException If IMAP server information cannot be returned
     */
    public static IMAPServerInfo instanceFor(IMAPConfig imapConfig, int accountId) throws OXException {
        try {
            boolean isPrimary = imapConfig.getAccountId() == MailAccount.DEFAULT_ID;
            String greeting = IMAPCapabilityAndGreetingCache.getGreeting(new HostAndPort(IDNA.toASCII(imapConfig.getServer()), imapConfig.getPort()), imapConfig.isSecure(), imapConfig.getIMAPProperties(), isPrimary);
            Map<String, String> capabilities = imapConfig.asMap();
            return new IMAPServerInfo(greeting, capabilities, accountId);
        } catch (IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------

    private final String greeting;
    private final Map<String, String> capabilities;
    private final int accountId;

    /**
     * Initializes a new {@link IMAPServerInfo}.
     */
    private IMAPServerInfo(String greeting, Map<String, String> capabilities, int accountId) {
        super();
        this.greeting = greeting;
        this.capabilities = capabilities;
        this.accountId = accountId;
    }

    /**
     * Gets the account identifier
     *
     * @return The account identifier
     */
    public int getAccountId() {
        return accountId;
    }

    /**
     * Gets the greeting
     *
     * @return The greeting
     */
    public String getGreeting() {
        return greeting;
    }

    /**
     * Gets the capabilities
     *
     * @return The capabilities
     */
    public Map<String, String> getCapabilities() {
        return capabilities;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        builder.append("accountId=").append(accountId);
        if (greeting != null) {
            builder.append(", ").append("greeting=\"").append(greeting).append('\"');
        }
        if (capabilities != null) {
            builder.append(", ").append("capabilities=").append(capabilities.values());
        }
        builder.append(']');
        return builder.toString();
    }

}
