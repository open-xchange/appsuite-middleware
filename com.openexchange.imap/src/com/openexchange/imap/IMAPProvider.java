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

import com.openexchange.imap.config.IMAPProperties;
import com.openexchange.mail.Protocol;
import com.openexchange.mail.api.AbstractProtocolProperties;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.session.Session;

/**
 * {@link IMAPProvider} - The provider for IMAP protocol.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPProvider extends MailProvider {

    /**
     * IMAP protocol.
     */
    public static final Protocol PROTOCOL_IMAP = IMAPProtocol.getInstance();

    private static final IMAPProvider instance = new IMAPProvider();

    /**
     * Gets the singleton instance of IMAP provider.
     *
     * @return The singleton instance of IMAP provider
     */
    public static IMAPProvider getInstance() {
        return instance;
    }

    /**
     * Initializes a new {@link IMAPProvider}.
     */
    private IMAPProvider() {
        super();
    }

    @Override
    public MailAccess<?, ?> createNewMailAccess(Session session) {
        return new IMAPAccess(session);
    }

    @Override
    public MailAccess<?, ?> createNewMailAccess(Session session, int accountId) {
        return new IMAPAccess(session, accountId);
    }

    @Override
    public MailPermission createNewMailPermission(Session session, int accountId) {
        return new ACLPermission();
    }

    @Override
    public Protocol getProtocol() {
        return PROTOCOL_IMAP;
    }

    @Override
    protected AbstractProtocolProperties getProtocolProperties() {
        return IMAPProperties.getInstance();
    }

    @Override
    protected String getSpamHandlerName() {
        return IMAPProperties.getInstance().getSpamHandlerName();
    }

}
