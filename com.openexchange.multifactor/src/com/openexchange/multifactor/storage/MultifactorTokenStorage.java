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

package com.openexchange.multifactor.storage;

import java.util.Optional;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.MultifactorRequest;
import com.openexchange.multifactor.MultifactorToken;

/**
 * {@link MultifactorTokenStorage} - A two layered storage which stores {@link MultifactorToken}s for a session associated with a given key.
 * <br>
 * A token is associated with exactly one key.
 * <br>
 * A token is associated with exactly one session.
 * <br>
 * A session can be associated with more than one token.
 *
 * @param <T> The type of the token to store.
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public interface MultifactorTokenStorage<T extends MultifactorToken<?>> {

    /**
     * Gets the token for the given key and session
     *
     * @param multifactorRequest The {@link MultifactorRequest} owning the token
     * @param key The key of the token to get
     * @return An {@link Optional} containing the Token associated with the specified key for the given session or an empty {@link Optional}
     * @throws OXException
     */
    Optional<T> getAndRemove(MultifactorRequest multifactorRequest, String key) throws OXException;

    /**
     * Adds a new token for the given key and session
     *
     * @param multifactorRequest The {@link MultifactorRequest} to add the token for
     * @param key the key to add the token for
     * @param token The token to add
     * @throws OXException
     */
    void add(MultifactorRequest multifactorRequest, String key, T token) throws OXException;

    /**
     * Gets the number of active tokens for the given session
     *
     * @param multifactorRequest The {@link MultifactorRequest} to get the active tokens for
     * @return The amount of the active tokens for the given session
     * @throws OXException
     */
    int getTokenCount(MultifactorRequest multifactorRequest) throws OXException;
}
