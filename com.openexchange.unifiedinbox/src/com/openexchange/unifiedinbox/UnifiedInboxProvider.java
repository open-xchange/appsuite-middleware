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

package com.openexchange.unifiedinbox;

import com.openexchange.mail.Protocol;
import com.openexchange.mail.api.AbstractProtocolProperties;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.SpamHandler;
import com.openexchange.unifiedinbox.config.UnifiedInboxProperties;

/**
 * {@link UnifiedInboxProvider} - The provider for Unified Mail protocol.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedInboxProvider extends MailProvider {

    /**
     * Unified Mail protocol.
     */
    public static final Protocol PROTOCOL_UNIFIED_INBOX = new Protocol(UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX);

    private static final UnifiedInboxProvider instance = new UnifiedInboxProvider();

    /**
     * Gets the singleton instance of Unified Mail provider.
     *
     * @return The singleton instance of Unified Mail provider
     */
    public static UnifiedInboxProvider getInstance() {
        return instance;
    }

    /**
     * Initializes a new {@link UnifiedInboxProvider}.
     */
    private UnifiedInboxProvider() {
        super();
    }

    @Override
    public MailAccess<?, ?> createNewMailAccess(final Session session) {
        return new UnifiedInboxAccess(session);
    }

    @Override
    public MailAccess<?, ?> createNewMailAccess(final Session session, final int accountId) {
        return new UnifiedInboxAccess(session, accountId);
    }

    @Override
    public Protocol getProtocol() {
        return PROTOCOL_UNIFIED_INBOX;
    }

    @Override
    protected AbstractProtocolProperties getProtocolProperties() {
        return UnifiedInboxProperties.getInstance();
    }

    @Override
    protected String getSpamHandlerName() {
        return SpamHandler.SPAM_HANDLER_FALLBACK;
    }

}
