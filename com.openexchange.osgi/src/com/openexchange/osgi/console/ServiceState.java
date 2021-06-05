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
 * A {@link ServiceState} object details which service constraints have been satisfied and which are not satisfied.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ServiceState {

    /**
     * Retrieves a list of service class names that have not been satisfied.
     *
     * @return A list of service class names that have not been satisfied.
     */
    public List<String> getMissingServices();

    /**
     * Retrieves a list of service class names that have been satisfied.
     *
     * @return A list of service class names that have been satisfied.
     */
    public List<String> getPresentServices();

    /**
     * Retrieves the name of the bundle this state describes.
     *
     * @return The name of the bundle this state describes.
     */
    public String getName();
}
