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

package com.openexchange.password.mechanism;

import java.util.List;

/**
 * Registry for available {@link PasswordMech} implementations that can be retrieved via {@link PasswordMechRegistry#get(String)} by giving the crypt mechanism identifier.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public interface PasswordMechRegistry {

    /**
     * Returns the password mechanism related to given identifier or <code>null</code> if no password mechanism is registered for the given identifier.
     *
     * @param identifier The identifier for the password mechanism
     * @return {@link PasswordMech} associated to given identifier or <code>null</code> if no password mechanism is registered for the given identifier
     */
    public PasswordMech get(String identifier);

    /**
     * Returns the main identifiers of the registered {@link PasswordMech}s which might be used.
     *
     * @return {@link List} containing the main identifiers of the registered {@link PasswordMech}.
     */
    public List<String> getIdentifiers();

    /**
     * Returns the password mechanism that is configured to be the standard mechanism.
     *
     * @return {@link PasswordMech} associated and never <code>null</code>.
     */
    public PasswordMech getDefault();

}
