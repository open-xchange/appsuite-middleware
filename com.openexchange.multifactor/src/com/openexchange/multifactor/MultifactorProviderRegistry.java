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

package com.openexchange.multifactor;

import java.util.Collection;
import java.util.Optional;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link MultifactorProviderRegistry} - a registry for {@link MultifactorProvider} instances.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public interface MultifactorProviderRegistry {

    /**
     * Registers a {@link MultifactorProvider} provider
     *
     * @param provider The provider to register
     */
    void registerProvider(MultifactorProvider provider);

    /**
     * Removes a {@link MultifactorProvider} provider from the registry
     *
     * @param provider The provider to remove
     */
    void unRegisterProvider(MultifactorProvider provider);

    /**
     * Returns, a read-only, collection of all {@link MultifactorProvider}
     *
     * @return A read-only collection of all registered {@link MultifactorProvider}
     */
    Collection<MultifactorProvider> getProviders();

    /**
     * Returns an {@link Optional} describing the {@link MultifactorProvider} with the given name,
     * or an empty {@link Optional} if no such {@link MultifactorProvider} was found.
     *
     * @param name The name of the provider to get
     * @return An {@link Optional} describing the {@link MultifactorProvider} with the given name,
     *         or an empty {@link Optional} if no such {@link MultifactorProvider} was found.
     */
    Optional<MultifactorProvider> getProvider(String name);

    /**
     * Returns all available {@link MultifactorProvider} instances for the given session.
     * <br>
     * (see {@link MultifactorProvider#isEnabled(Session)}
     *
     * @param multifactorRequest The request to get all registered {@link MultifactorProvider} instances for
     * @return A collection of {@link MultifactorProvider} instances which are available for the given session
     * @throws OXException
     */
    Collection<MultifactorProvider> getProviders(MultifactorRequest multifactorRequest) throws OXException;

    /**
     * Returns all available {@link MultifactorProvider} instances for the given session filtered by the names
     * <br>
     * (see {@link MultifactorProvider#isEnabled(MultifactorRequest)}
     *
     * @param multifactorRequest The request to get all registered {@link MultifactorProvider} instances for
     * @param nameFilters The names of providers to return or null to return all providers
     * @return A collection of {@link MultifactorProvider} instances, which are available for the given session filtered by the given names, if provided.
     * @throws OXException
     */
    Collection<MultifactorProvider> getProviders(MultifactorRequest multifactorRequest, String[] nameFilters) throws OXException;
}