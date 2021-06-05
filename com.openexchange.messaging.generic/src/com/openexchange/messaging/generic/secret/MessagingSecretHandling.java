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

package com.openexchange.messaging.generic.secret;

import java.util.Collection;
import java.util.Collections;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccountManager;
import com.openexchange.messaging.MessagingService;
import com.openexchange.secret.recovery.EncryptedItemCleanUpService;
import com.openexchange.secret.recovery.EncryptedItemDetectorService;
import com.openexchange.secret.recovery.SecretMigrator;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link MessagingSecretHandling}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MessagingSecretHandling implements EncryptedItemDetectorService, SecretMigrator, EncryptedItemCleanUpService {

    /**
     * Initializes a new {@link MessagingSecretHandling}.
     */
    public MessagingSecretHandling() {
        super();
    }

    @Override
    public void migrate(final String oldSecret, final String newSecret, final ServerSession session) throws OXException {
        final Collection<MessagingService> messagingServices = getMessagingServices();
        for (final MessagingService messagingService : messagingServices) {
            final MessagingAccountManager accountManager = messagingService.getAccountManager();
            accountManager.migrateToNewSecret(oldSecret, newSecret, session);
        }
    }

    // Override me
    protected Collection<MessagingService> getMessagingServices() {
        return Collections.emptyList();
    }

    @Override
    public boolean hasEncryptedItems(final ServerSession session) throws OXException {
        final Collection<MessagingService> messagingServices = getMessagingServices();
        for (final MessagingService messagingService : messagingServices) {
            if (messagingService.getAccountManager().hasAccount(session)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void cleanUpEncryptedItems(final String secret, final ServerSession session) throws OXException {
        final Collection<MessagingService> messagingServices = getMessagingServices();
        for (final MessagingService messagingService : messagingServices) {
            final MessagingAccountManager accountManager = messagingService.getAccountManager();
            accountManager.cleanUp(secret, session);
        }
    }
    
    @Override
    public void removeUnrecoverableItems(final String secret, final ServerSession session) throws OXException {
        final Collection<MessagingService> messagingServices = getMessagingServices();
        for (final MessagingService messagingService : messagingServices) {
            final MessagingAccountManager accountManager = messagingService.getAccountManager();
            accountManager.removeUnrecoverableItems(secret, session);
        }
    }

}
