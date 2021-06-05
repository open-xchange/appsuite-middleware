package com.openexchange.multifactor;

import java.util.Collection;

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

import com.openexchange.exception.OXException;

/**
 * {@link MultifactorProviderStrategy} - A strategy to define the way a client can authenticate against a set of {@link MultifactorProvider}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public interface MultifactorProviderStrategy {

    /**
     * Performs the authentication against a set of {@link MultifactorProvider}.
     * <br>
     * It is up to the implementation which providers are used
     *
     * @param providers A set of providers
     * @param multifactorRequest The {@link MultifactorRequest}
     * @return true if multifactor exists and is authenticated. False if no multifactor registered for the user
     * @throws OXException
     */
    public boolean requireAuthentication(Collection<MultifactorProvider> providers, MultifactorRequest multifactorRequest) throws OXException;
}
