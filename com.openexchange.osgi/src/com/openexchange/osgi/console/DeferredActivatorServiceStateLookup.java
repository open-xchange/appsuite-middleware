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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link DeferredActivatorServiceStateLookup}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DeferredActivatorServiceStateLookup implements ServiceStateLookup {

    private final Map<String, ServiceStateImpl> states;

    /**
     * Initializes a new {@link DeferredActivatorServiceStateLookup}.
     */
    public DeferredActivatorServiceStateLookup() {
        super();
        states = new ConcurrentHashMap<String, ServiceStateImpl>(128, 0.9f, 1);
    }

    /**
     * Applies the given service state information.
     *
     * @param name The bundle's symbolic name
     * @param missing The list of symbolic names of missing services
     * @param present The list of symbolic names of available services
     */
    @Override
    public void setState(final String name, final List<String> missing, final List<String> present) {
        states.put(name, new ServiceStateImpl(name, missing, present));
    }

    @Override
    public ServiceState determineState(final String name) {
        final ServiceStateImpl serviceState = states.get(name);
        if (null == serviceState) {
            return null;
        }
        return serviceState.copy();
    }

    @Override
    public List<String> getNames() {
        return new ArrayList<String>(states.keySet());
    }

}
