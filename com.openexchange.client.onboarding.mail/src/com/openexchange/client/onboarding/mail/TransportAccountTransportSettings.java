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

package com.openexchange.client.onboarding.mail;

import com.openexchange.config.ConfigurationService;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.TransportAuth;

/**
 * {@link TransportAccountTransportSettings}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class TransportAccountTransportSettings implements TransportSettings {

    private static final int PRIMARY = MailAccount.DEFAULT_ID;

    private final MailAccount mailAccount;
    private final Boolean needAuth;

    /**
     * Initializes a new {@link TransportAccountTransportSettings}.
     */
    public TransportAccountTransportSettings(MailAccount mailAccount, ConfigurationService configService) {
        super();
        this.mailAccount = mailAccount;
        if (mailAccount.getId() == PRIMARY) {
            String smtpAuthStr = configService.getProperty("com.openexchange.smtp.primary.smtpAuthentication");
            if (null != smtpAuthStr) {
                this.needAuth = Boolean.valueOf(smtpAuthStr.trim());
                return;
            }
        }
        String smtpAuthStr = configService.getProperty("com.openexchange.smtp.smtpAuthentication");
        if (null != smtpAuthStr) {
            this.needAuth = Boolean.valueOf(smtpAuthStr.trim());
        } else {
            this.needAuth = null;
        }
    }

    @Override
    public String getLogin() {
        return mailAccount.getLogin();
    }

    @Override
    public String getPassword() {
        // Not available
        return null;
    }

    @Override
    public int getPort() {
        return mailAccount.getTransportPort();
    }

    @Override
    public String getServer() {
        return mailAccount.getTransportServer();
    }

    @Override
    public boolean isSecure() {
        return mailAccount.isTransportSecure();
    }

    @Override
    public boolean needsAuthentication() {
        return needAuth != null ? needAuth.booleanValue() : (TransportAuth.NONE != mailAccount.getTransportAuth());
    }

}
