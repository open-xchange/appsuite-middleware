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

package com.openexchange.oauth;

import java.util.Collection;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link OAuthAPIRegistry} - A registry for known OAuth APIs.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
@SingletonService
public interface OAuthAPIRegistry {

    /**
     * Registers specified OAuth API.
     *
     * @param serviceId The service id
     * @param DefaultAPI the OAuth API
     * @return <code>true</code> if specified OAuth API has been successfully registered; otherwise <code>false</code> if there is already such an OAuth API
     */
    boolean registerAPI(String serviceId, API defaultAPI);

    /**
     * Resolves the specified service identifier to a known OAuth API
     *
     * @param serviceId The service identifier to resolve
     * @return The resolved OAuth API or <code>null</code> if no such API is known
     */
    API resolveFromServiceId(String serviceId);

    /**
     * Gets all currently registered OAuth APIs.
     *
     * @return All OAuth APIs
     */
    Collection<API> getAllAPIs();

}
