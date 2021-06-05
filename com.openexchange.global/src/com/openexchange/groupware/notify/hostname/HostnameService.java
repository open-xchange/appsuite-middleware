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

package com.openexchange.groupware.notify.hostname;

import com.openexchange.osgi.annotation.OptionalService;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * An optional service providing the host name part in generated links to internal objects, e.g. for notifications:
 *
 * <pre>
 * http://[hostname]/[uiwebpath]#m=[module]&i=[object]&f=[folder]
 * </pre>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
@OptionalService
public interface HostnameService {

    /**
     * The key under which {@link HostData} instances are stored within generic parameter maps.
     *
     * @type <code>com.openexchange.groupware.notify.hostname.HostData</code>
     */
    public static final String PARAM_HOST_DATA = "com.openexchange.groupware.hostdata";

    /**
     * Returns the host name part used in generated links to internal objects; meaning the replacement for &quot;[hostname]&quot; in URL
     * template defined by property &quot;object_link&quot; in properties file &quot;notification.properties&quot;. Additionally this
     * service may be used for the host name when generating direct links into the UI.
     *
     * @param userId The user ID or a value less than/equal to zero if not available
     * @param contextId The context ID or a value less than/equal to zero if not available
     * @return The host name part used in generated links to internal objects or <code>null</code> (if user ID and/or context ID could not
     *         be resolved or any error occurred).
     */
    String getHostname(int userId, int contextId);

    /**
     * Returns the host name part used in generated links to internal objects for guest user accounts; meaning the replacement for
     * &quot;[hostname]&quot; in URL template defined by property &quot;object_link&quot; in properties file
     * &quot;notification.properties&quot;. Additionally this service may be used for the host name when generating direct links into
     * the UI.
     *
     * @param userId The user ID or a value less than/equal to zero if not available
     * @param contextId The context ID or a value less than/equal to zero if not available
     * @return The host name part used in generated links to internal objects or <code>null</code> (if user ID and/or context ID could not
     *         be resolved or any error occurred).
     */
    String getGuestHostname(int userId, int contextId);

}
