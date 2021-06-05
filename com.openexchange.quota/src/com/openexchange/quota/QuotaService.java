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

package com.openexchange.quota;

import java.util.List;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * Open-Xchange consists of a set of modules that serve user requests.
 * Every module that allows users to store data may provide limits for a
 * certain amount of storage and a certain number of items that it will handle
 * for each user. In other words, every module can have user-specific quotas
 * for storage size and items. Those quotas may be set by definition or
 * by configuration and can also be unlimited. The responsibility to
 * enforce quotas lies within the modules themselves, but they can announce
 * their quotas via this service. That enables a client to provide a
 * combined overview over all quotas. Each module that wants to contribute
 * to this service has to implement a {@link QuotaProvider}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
@SingletonService
public interface QuotaService {

    /**
     * Gets all currently known {@link QuotaProvider}s.
     *
     * @return A list of providers. Never <code>null</code> but possibly empty.
     */
    List<QuotaProvider> getAllProviders();

    /**
     * Gets the provider for a specific module, if available.
     *
     * @param moduleID The modules unique identifier.
     * @return The modules provider or <code>null</code>, if unknown.
     */
    QuotaProvider getProvider(String moduleID);

}
