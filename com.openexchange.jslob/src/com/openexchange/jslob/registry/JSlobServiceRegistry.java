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

package com.openexchange.jslob.registry;

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSlobService;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link JSlobServiceRegistry} - A registry for JSlob services.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface JSlobServiceRegistry {

    /**
     * Gets the JSlob service associated with given service identifier.
     *
     * @param serviceId The service identifier or an alias
     * @return The JSlob service associated with given service identifier
     * @throws OXException If returning the service fails or not found
     */
    JSlobService getJSlobService(String serviceId) throws OXException;

    /**
     * Gets the JSlob service associated with given service identifier.
     *
     * @param serviceId The service identifier or an alias
     * @return The JSlob service associated with given service identifier or <code>null</code>
     * @throws OXException If returning the service fails
     */
    JSlobService optJSlobService(String serviceId) throws OXException;

    /**
     * Gets a collection containing all registered JSlob services
     *
     * @return A collection containing all registered JSlob services
     * @throws OXException If returning the collection fails
     */
    Collection<JSlobService> getJSlobServices() throws OXException;

    /**
     * Puts given JSlob service into this registry.
     *
     * @param jslobService The JSlob service to put
     * @return <code>true</code> on success; otherwise <code>false</code> if another service is already bound to the same identifier
     */
    boolean putJSlobService(JSlobService jslobService);

    /**
     * Removes the JSlob service associated with given service identifier.
     *
     * @param jslobService The JSlob service to remove
     * @throws OXException If removing the service fails
     */
    void removeJSlobService(JSlobService jslobService) throws OXException;

}
