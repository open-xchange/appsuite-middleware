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

package com.openexchange.mail.mime.converters;

import com.openexchange.exception.OXException;
import com.openexchange.mail.api.IMailProperties;
import com.openexchange.mail.api.MailCapabilities;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.UrlInfo;

/**
 * {@link DummyMailConfig}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class DummyMailConfig extends MailConfig {
    
    private static final DummyMailConfig INSTANCE = new DummyMailConfig();
    
    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static DummyMailConfig getInstance() {
        return INSTANCE;
    }
    
    // -------------------------------------------------------------------------------------------------

    private DummyMailConfig() {
        super();
    }

    @Override
    public MailCapabilities getCapabilities() {
        return MailCapabilities.EMPTY_CAPS;
    }

    @Override
    public int getPort() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getServer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSecure() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPort(int port) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSecure(boolean secure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setServer(String server) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IMailProperties getMailProperties() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMailProperties(IMailProperties mailProperties) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void parseServerURL(UrlInfo urlInfo) throws OXException {
        throw new UnsupportedOperationException();
    }
}
