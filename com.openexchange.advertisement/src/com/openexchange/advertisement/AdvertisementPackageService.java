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

package com.openexchange.advertisement;

import com.openexchange.config.Reloadable;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link AdvertisementPackageService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
@SingletonService
public interface AdvertisementPackageService extends Reloadable {

    public static final String DEFAULT_SCHEME_ID = "Global";

    public static final String DEFAULT_RESELLER = "default";

    /**
     * Gets the suitable advertisement configuration manager for specified context.
     *
     * @param contextId The context identifier
     * @return The suitable advertisement configuration manager
     */
    AdvertisementConfigService getScheme(int contextId);

    /**
     * Gets the default advertisement configuration manager
     *
     * @return The default advertisement configuration manager
     */
    AdvertisementConfigService getDefaultScheme();

}
