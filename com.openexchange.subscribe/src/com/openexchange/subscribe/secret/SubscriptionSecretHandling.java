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

package com.openexchange.subscribe.secret;

import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.secret.recovery.EncryptedItemCleanUpService;
import com.openexchange.secret.recovery.EncryptedItemDetectorService;
import com.openexchange.secret.recovery.SecretMigrator;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link SubscriptionSecretHandling}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SubscriptionSecretHandling implements EncryptedItemDetectorService, SecretMigrator, EncryptedItemCleanUpService {

    private SubscriptionSourceDiscoveryService discovery = null;

    public SubscriptionSecretHandling(final SubscriptionSourceDiscoveryService discovery) {
        super();
        this.discovery = discovery;
    }

    @Override
    public boolean hasEncryptedItems(final ServerSession session) throws OXException {
        final List<SubscriptionSource> sources = discovery.getSources();
        for (final SubscriptionSource subscriptionSource : sources) {
            final Set<String> passwordFields = subscriptionSource.getPasswordFields();
            if (passwordFields.isEmpty()) {
                continue;
            }

            final SubscribeService subscribeService = subscriptionSource.getSubscribeService();
            if (subscribeService.hasAccounts(session.getContext(), session.getUser())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void migrate(final String oldSecret, final String newSecret, final ServerSession session) throws OXException {
        final List<SubscriptionSource> sources = discovery.getSources();
        for (final SubscriptionSource subscriptionSource : sources) {
            final Set<String> passwordFields = subscriptionSource.getPasswordFields();
            if (passwordFields.isEmpty()) {
                continue;
            }

            final SubscribeService subscribeService = subscriptionSource.getSubscribeService();
            if (null != subscribeService) {
                subscribeService.migrateSecret(session, oldSecret, newSecret);
            }
        }
    }

    @Override
    public void cleanUpEncryptedItems(String secret, ServerSession session) throws OXException {
        final List<SubscriptionSource> sources = discovery.getSources();
        for (final SubscriptionSource subscriptionSource : sources) {
            final Set<String> passwordFields = subscriptionSource.getPasswordFields();
            if (passwordFields.isEmpty()) {
                continue;
            }

            final SubscribeService subscribeService = subscriptionSource.getSubscribeService();
            if (null != subscribeService) {
                subscribeService.cleanUp(secret, session);
            }
        }
    }
    
    @Override
    public void removeUnrecoverableItems(String secret, ServerSession session) throws OXException {
        final List<SubscriptionSource> sources = discovery.getSources();
        for (final SubscriptionSource subscriptionSource : sources) {
            final Set<String> passwordFields = subscriptionSource.getPasswordFields();
            if (passwordFields.isEmpty()) {
                continue;
            }

            final SubscribeService subscribeService = subscriptionSource.getSubscribeService();
            if (null != subscribeService) {
                subscribeService.removeUnrecoverableItems(secret, session);
            }
        }
    }

}
