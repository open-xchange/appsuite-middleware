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

package com.openexchange.pop3;

import com.openexchange.exception.OXException;
import com.openexchange.mail.Protocol;
import com.openexchange.mail.api.AbstractProtocolProperties;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.pop3.config.POP3Properties;
import com.openexchange.session.Session;

/**
 * {@link POP3Provider} - The provider for POP3 protocol.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class POP3Provider extends MailProvider {

    /**
     * POP3 protocol.
     */
    public static final Protocol PROTOCOL_POP3 = new Protocol("pop3", "pop3s");

    private static final POP3Provider instance = new POP3Provider();

    /**
     * Gets the singleton instance of POP3 provider.
     *
     * @return The singleton instance of POP3 provider
     */
    public static POP3Provider getInstance() {
        return instance;
    }

    /**
     * Initializes a new {@link POP3Provider}.
     */
    private POP3Provider() {
        super();
    }

    @Override
    public MailAccess<?, ?> createNewMailAccess(final Session session) throws OXException {
        return POP3Access.newInstance(session);
    }

    @Override
    public MailAccess<?, ?> createNewMailAccess(final Session session, final int accountId) throws OXException {
        return POP3Access.newInstance(session, accountId);
    }

    @Override
    public Protocol getProtocol() {
        return PROTOCOL_POP3;
    }

    @Override
    protected AbstractProtocolProperties getProtocolProperties() {
        return POP3Properties.getInstance();
    }

    @Override
    protected String getSpamHandlerName() {
        return POP3Properties.getInstance().getSpamHandlerName();
    }

}
