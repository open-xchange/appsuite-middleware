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

/**
 * {@link ServiceStateImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ServiceStateImpl implements ServiceState {

    private final List<String> missing;

    private final List<String> present;

    private final String name;

    public ServiceStateImpl(final String name, final List<String> missing, final List<String> present) {
        super();
        this.missing = missing;
        this.present = present;
        this.name = name;
    }

    @Override
    public List<String> getMissingServices() {
        return missing;
    }

    @Override
    public List<String> getPresentServices() {
        return present;
    }

    /**
     * Creates a copy of this {@link ServiceStateImpl}.
     *
     * @return A copy of this {@link ServiceStateImpl}.
     */
    public ServiceStateImpl copy() {
        return new ServiceStateImpl(name, new ArrayList<String>(missing), new ArrayList<String>(present));
    }

    @Override
    public String getName() {
        return name;
    }

}
