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

package com.openexchange.subscribe;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.annotation.NonNull;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.external.account.DefaultExternalAccount;
import com.openexchange.external.account.ExternalAccount;
import com.openexchange.external.account.ExternalAccountModule;
import com.openexchange.external.account.ExternalAccountProvider;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link SubscriptionExternalAccountProvider}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public class SubscriptionExternalAccountProvider implements ExternalAccountProvider {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link SubscriptionExternalAccountProvider}.
     *
     * @param services The service lookup
     */
    public SubscriptionExternalAccountProvider(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public @NonNull ExternalAccountModule getModule() {
        return ExternalAccountModule.CONTACTS;
    }

    @Override
    public List<ExternalAccount> list(int contextId) throws OXException {
        List<Subscription> subscriptionsForContext = getStorage().getSubscriptionsForContext(getContextService().getContext(contextId));
        int size = subscriptionsForContext.size();
        if (size <= 0) {
            return Collections.emptyList();
        }

        List<ExternalAccount> accounts = new ArrayList<>(size);
        for (Subscription subscription : subscriptionsForContext) {
            SubscriptionSource source = subscription.getSource();
            String sourceId = source == null ? (String) subscription.getConfiguration().get("source_id") : source.getId();
            accounts.add(new DefaultExternalAccount(subscription.getId(), contextId, subscription.getUserId(), sourceId, getModule()));
        }
        return accounts;
    }

    @Override
    public List<ExternalAccount> list(int contextId, int userId) throws OXException {
        List<Subscription> subscriptionsOfUser = getStorage().getSubscriptionsOfUser(getContextService().getContext(contextId), userId);
        int size = subscriptionsOfUser.size();
        if (size <= 0) {
            return Collections.emptyList();
        }

        List<ExternalAccount> accounts = new ArrayList<>(size);
        for (Subscription subscription : subscriptionsOfUser) {
            accounts.add(new DefaultExternalAccount(subscription.getId(), contextId, subscription.getUserId(), subscription.getSource().getId(), getModule()));
        }
        return accounts;
    }

    @Override
    public List<ExternalAccount> list(int contextId, int userId, String providerId) throws OXException {
        List<Subscription> subscriptionsOfUser = getStorage().getSubscriptionsOfUser(getContextService().getContext(contextId), userId, providerId);
        int size = subscriptionsOfUser.size();
        if (size <= 0) {
            return Collections.emptyList();
        }

        List<ExternalAccount> accounts = new ArrayList<>(size);
        for (Subscription subscription : subscriptionsOfUser) {
            accounts.add(new DefaultExternalAccount(subscription.getId(), contextId, subscription.getUserId(), subscription.getSource().getId(), getModule()));
        }
        return accounts;
    }

    @Override
    public List<ExternalAccount> list(int contextId, String providerId) throws OXException {
        List<Subscription> subscriptionsForContextAndProvider = getStorage().getSubscriptionsForContextAndProvider(getContextService().getContext(contextId), providerId);
        int size = subscriptionsForContextAndProvider.size();
        if (size <= 0) {
            return Collections.emptyList();
        }

        List<ExternalAccount> accounts = new ArrayList<>(size);
        for (Subscription subscription : subscriptionsForContextAndProvider) {
            accounts.add(new DefaultExternalAccount(subscription.getId(), contextId, subscription.getUserId(), subscription.getSource().getId(), getModule()));
        }
        return accounts;
    }

    @Override
    public boolean delete(int id, int contextId, int userId) throws OXException {
        return getStorage().deleteSubscription(getContextService().getContext(contextId), userId, id);
    }

    @Override
    public boolean delete(int id, int contextId, int userId, Connection connection) throws OXException {
        return getStorage().deleteSubscription(getContextService().getContext(contextId), userId, id, connection);
    }

    ///////////////////////////////////// HELPERS /////////////////////////////////

    /**
     * Returns the {@link AdministrativeSubscriptionStorage}
     *
     * @return the {@link AdministrativeSubscriptionStorage}
     * @throws OXException if the storage is absent
     */
    private AdministrativeSubscriptionStorage getStorage() throws OXException {
        SubscriptionStorage subscriptionStorage = AbstractSubscribeService.STORAGE.get();
        if (subscriptionStorage == null || !(subscriptionStorage instanceof AdministrativeSubscriptionStorage)) {
            throw ServiceExceptionCode.absentService(AdministrativeSubscriptionStorage.class);
        }
        return (AdministrativeSubscriptionStorage) subscriptionStorage;
    }

    /**
     * Returns the {@link ContextService}
     *
     * @return the {@link ContextService}
     * @throws OXException if the service is absent
     */
    private ContextService getContextService() throws OXException {
        return services.getServiceSafe(ContextService.class);
    }

}
