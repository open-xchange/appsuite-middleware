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

package com.openexchange.unifiedinbox.config;

import com.openexchange.mail.api.IMailProperties;
import com.openexchange.mail.api.MailCapabilities;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.UrlInfo;

/**
 * {@link UnifiedInboxConfig}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedInboxConfig extends MailConfig {

    private IMailProperties mailProperties;

    /**
     * Default constructor
     */
    public UnifiedInboxConfig() {
        super();
    }

    @Override
    public MailCapabilities getCapabilities() {
        return MailCapabilities.EMPTY_CAPS;
    }

    @Override
    public int getPort() {
        return -1;
    }

    @Override
    public void setPort(final int pop3Port) {
        // Nothing to set
    }

    @Override
    public String getServer() {
        return "localhost";
    }

    @Override
    public void setServer(final String pop3Server) {
        // Nothing to set
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public void setSecure(final boolean secure) {
        // Nothing to set
    }

    @Override
    protected void parseServerURL(final UrlInfo urlInfo) {
        // Nothing to parse
        login = "dummy";
        password = "secret";
    }

    @Override
    public IMailProperties getMailProperties() {
        return mailProperties;
    }

    @Override
    public void setMailProperties(final IMailProperties mailProperties) {
        this.mailProperties = mailProperties;
    }

}
