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

package com.openexchange.secret.recovery.mail.osgi;

import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.secret.recovery.EncryptedItemCleanUpService;
import com.openexchange.secret.recovery.EncryptedItemDetectorService;
import com.openexchange.secret.recovery.SecretMigrator;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MailSecretRecoveryActivator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MailSecretRecoveryActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { MailAccountStorageService.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        // Ignore
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        // Ignore
    }

    @Override
    protected void startBundle() throws Exception {
        final MailAccountStorageService mailAccountStorage = getService(MailAccountStorageService.class);
        registerService(EncryptedItemDetectorService.class, new EncryptedItemDetectorService() {

            @Override
            public boolean hasEncryptedItems(final ServerSession session) throws OXException {
                return mailAccountStorage.hasAccounts(session);
            }

        });
        registerService(SecretMigrator.class, new SecretMigrator() {

            @Override
            public void migrate(final String oldSecret, final String newSecret, final ServerSession session) throws OXException {
                mailAccountStorage.migratePasswords(oldSecret, newSecret, session);
            }

        });
        registerService(EncryptedItemCleanUpService.class, new EncryptedItemCleanUpService() {

            @Override
            public void cleanUpEncryptedItems(final String secret, final ServerSession session) throws OXException {
                mailAccountStorage.cleanUp(secret, session);
            }

            @Override
            public void removeUnrecoverableItems(String secret, ServerSession session) throws OXException {
                mailAccountStorage.removeUnrecoverableItems(secret, session);

            }

        });
    }
}
