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

import com.openexchange.exception.OXException;
import com.openexchange.mail.api.MailCapabilities;
import com.openexchange.mail.api.UrlInfo;
import com.openexchange.mail.transport.config.ITransportProperties;
import com.openexchange.mail.transport.config.TransportConfig;

/**
 * {@link GmailSendConfig} - The Gmail send configuration.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GmailSendConfig extends TransportConfig {

    private IGmailSendProperties transportProperties;

    /**
     * Default constructor
     */
    public GmailSendConfig() {
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
        return "www.googleapis.com";
    }

    @Override
    public void setServer(final String pop3Server) {
        // Nothing to set
    }

    @Override
    public void setSecure(final boolean secure) {
        // Nothing to set
    }

    @Override
    public boolean isSecure() {
        return true;
    }

    @Override
    protected void parseServerURL(final UrlInfo urlInfo) throws OXException {
        // Nothing to parse
    }

    @Override
    public ITransportProperties getTransportProperties() {
        return transportProperties;
    }

    public IGmailSendProperties getGmailSendProperties() {
        return transportProperties;
    }

    @Override
    public void setTransportProperties(final ITransportProperties transportProperties) {
        this.transportProperties = (IGmailSendProperties) transportProperties;
    }

}
