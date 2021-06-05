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

package com.openexchange.mailaccount;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.session.Session;

/**
 * {@link CredentialsProviderRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class CredentialsProviderRegistry {

    private static final CredentialsProviderRegistry INSTANCE = new CredentialsProviderRegistry();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static CredentialsProviderRegistry getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------

    private final AtomicReference<ServiceListing<CredentialsProviderService>> providers;

    /**
     * Initializes a new {@link CredentialsProviderRegistry}.
     */
    private CredentialsProviderRegistry() {
        super();
        providers = new AtomicReference<ServiceListing<CredentialsProviderService>>(null);
    }

    /**
     * Determines the appropriate credentials provider for specified arguments.
     *
     * @param forMailAccess <code>true</code> if credentials are supposed to be determined for mail access; otherwise <code>false</code> for transport
     * @param accountId The account identifier
     * @param session The session
     * @return The appropriate credentials provider or <code>null</code>
     * @throws OXException If appropriate credentials provider cannot be returned due to an error
     */
    public CredentialsProviderService optCredentialsProviderFor(boolean forMailAccess, int accountId, Session session) throws OXException {
        ServiceListing<CredentialsProviderService> serviceListing = providers.get();
        if (null == serviceListing) {
            return null;
        }

        for (CredentialsProviderService credentialsProvider : serviceListing) {
            if (credentialsProvider.isApplicableFor(forMailAccess, accountId, session)) {
                return credentialsProvider;
            }
        }
        return null;
    }

    /**
     * Applies given service listing.
     *
     * @param listing The service listing
     */
    public void applyListing(ServiceListing<CredentialsProviderService> listing) {
        providers.set(listing);
    }

}
