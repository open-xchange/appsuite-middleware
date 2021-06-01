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

package com.openexchange.serverconfig;

import java.util.List;

/**
 * {@link ServerConfigServicesLookup} - Contains services that can be used when building the server config.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface ServerConfigServicesLookup {

    /**
     * Get services that contribute computed values to the server config
     * 
     * @return A {@link List} of services.
     */
    public List<ComputedServerConfigValueService> getComputed();
    
    /**
     * Get services that filter the generated {@link ServerConfig}, removing fields that aren't intended for clients.
     * @return
     */
    public List<ClientServerConfigFilter> getClientFilters();

}
