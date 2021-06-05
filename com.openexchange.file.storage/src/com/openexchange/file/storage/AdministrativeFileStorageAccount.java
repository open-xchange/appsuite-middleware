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

package com.openexchange.file.storage;

/**
 * {@link AdministrativeFileStorageAccount}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public interface AdministrativeFileStorageAccount {

    /**
     * Returns the context identifier
     *
     * @return The context identifier
     */
    int getContextId();

    /**
     * Returns the user identifier
     *
     * @return the user identifier
     */
    int getUserId();

    /**
     * Returns the account identifier
     *
     * @return the account identifier
     */
    int getId();

    /**
     * Returns the service identifier
     *
     * @return the service identifier
     */
    String getServiceId();
}
