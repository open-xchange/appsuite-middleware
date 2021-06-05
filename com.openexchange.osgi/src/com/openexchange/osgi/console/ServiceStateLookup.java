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

package com.openexchange.osgi.console;

import java.util.List;

/**
 * A {@link ServiceStateLookup} provides access to {@link ServiceState} Objects.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ServiceStateLookup {

    /**
     * Retrieves a snapshot of the current service state.
     *
     * @param name The name of the bundle to query the service state for.
     * @return The current service state, or <code>null</code> if the bundle is not known.
     */
    public ServiceState determineState(String name);

    /**
     * Retrieves the names this lookup knows about
     *
     * @return
     */
    public List<String> getNames();

    void setState(String name, List<String> missing, List<String> present);
}
