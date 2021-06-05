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

package com.openexchange.sessionstorage;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.Service;

/**
 * {@link SessionStorageParameterNamesProvider} - Provides a listing of names for parameters that
 * are supposed to be stored along-side with the session representation held in session storage.
 * <p>
 * {@link org.osgi.framework.BundleContext#registerService(Class, Object, java.util.Dictionary) (OSGi-wise) Registered}
 * implementations of this interface are tracked and contribute to the set of parameter names that
 * are supposed to be taken over from a session to its session storage representation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
@Service
public interface SessionStorageParameterNamesProvider {

    /**
     * Gets the names for such parameters that are supposed to stored in sessions held in session storage for specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The parameter names or <code>null</code> (in case no such parameters shall be stored)
     * @throws OXException If parameter names cannot be returned
     */
    List<String> getParameterNames(int userId, int contextId) throws OXException;

}
