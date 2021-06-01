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

package com.openexchange.pop3.config;

import com.openexchange.mail.config.MailAccountProperties;
import com.openexchange.mailaccount.MailAccount;

/**
 * {@link MailAccountPOP3Properties} - POP3 properties read from mail account with fallback to properties read from properties file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountPOP3Properties extends MailAccountProperties implements IPOP3Properties {

    /**
     * Initializes a new {@link MailAccountPOP3Properties}.
     *
     * @param mailAccount The mail account
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws IllegalArgumentException If provided mail account is <code>null</code>
     */
    public MailAccountPOP3Properties(MailAccount mailAccount, int userId, int contextId) {
        super(mailAccount, userId, contextId);
    }

    @Override
    public String getPOP3AuthEnc() {
        return lookUpProperty("com.openexchange.pop3.pop3AuthEnc", POP3Properties.getInstance().getPOP3AuthEnc());
    }

    @Override
    public int getPOP3ConnectionIdleTime() {
        return lookUpIntProperty("com.openexchange.pop3.pop3ConnectionIdleTime", POP3Properties.getInstance().getPOP3ConnectionIdleTime());
    }

    @Override
    public int getPOP3BlockSize() {
        return lookUpIntProperty("com.openexchange.pop3.pop3BlockSize", POP3Properties.getInstance().getPOP3BlockSize());
    }

    @Override
    public int getPOP3ConnectionTimeout() {
        return lookUpIntProperty("com.openexchange.pop3.pop3ConnectionTimeout", POP3Properties.getInstance().getPOP3ConnectionTimeout());
    }

    @Override
    public int getPOP3TemporaryDown() {
        return lookUpIntProperty("com.openexchange.pop3.pop3TemporaryDown", POP3Properties.getInstance().getPOP3TemporaryDown());
    }

    @Override
    public int getPOP3Timeout() {
        return lookUpIntProperty("com.openexchange.pop3.pop3Timeout", POP3Properties.getInstance().getPOP3Timeout());
    }

    @Override
    public String getSSLProtocols() {
        return lookUpProperty("com.openexchange.pop3.ssl.protocols", POP3Properties.getInstance().getSSLProtocols());
    }

    @Override
    public String getSSLCipherSuites() {
        return lookUpProperty("com.openexchange.pop3.ssl.ciphersuites", POP3Properties.getInstance().getSSLCipherSuites());
    }

}
