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

package com.openexchange.database.internal.reloadable;

import java.security.KeyStore;
import com.openexchange.database.internal.Configuration;
import com.openexchange.database.internal.ConfigurationListener;
import com.openexchange.exception.OXException;

/**
 * {@link ConnectionReloader}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public interface ConnectionReloader {

    /**
     * Loads or reloads the {@link KeyStore}
     * 
     * @param configuration The {@link Configuration} containing the file path to the key store
     *            and the password for the key store
     * @return <code>true</code> if {@link KeyStore} was updated
     *         <code>false</code> otherwise, also e.g. if <code>useSSL</code> is disabled
     * @throws OXException In case {@link KeyStore} can't be loaded
     * 
     */
    boolean loadKeyStores(Configuration configuration) throws OXException;

    /**
     * Set a new {@link ConfigurationListener} to notify on changes
     * 
     * @param listener The listener
     * @return <code>true</code> if this set did not already contain the specified element
     */
    boolean setConfigurationListener(ConfigurationListener listener);

    /**
     * Remove a listener
     * 
     * @param poolId The ID of the pool the listener belongs to
     * @return <code>true</code> if the listener was removed
     *         <code>false</code> otherwise
     */
    boolean removeConfigurationListener(int poolId);
}
