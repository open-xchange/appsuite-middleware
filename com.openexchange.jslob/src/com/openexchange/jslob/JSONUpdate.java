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

package com.openexchange.jslob;

import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link JSONUpdate} - A JSON update.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JSONUpdate {

    private final List<JSONPathElement> path;

    private final Object value;

    /**
     * Initializes a new {@link JSONUpdate}.
     *
     * @param path The path of the value to update in JSlob
     * @param value The value to set in JSlob
     * @throws OXException If parsing specified path fails
     */
    public JSONUpdate(final String path, final Object value) throws OXException {
        super();
        this.path = JSONPathElement.parsePath(path.startsWith("/") ? path.substring(1) : path);
        this.value = value;
    }

    /**
     * Initializes a new {@link JSONUpdate}.
     *
     * @param path The path of the value to update in JSlob
     * @param value The value to set in JSlob
     */
    public JSONUpdate(final List<JSONPathElement> path, final Object value) {
        super();
        this.path = path;
        this.value = value;
    }

    /**
     * Gets the path.
     *
     * @return The path
     */
    public List<JSONPathElement> getPath() {
        return path;
    }

    /**
     * Gets the value.
     *
     * @return The value
     */
    public Object getValue() {
        return value;
    }

}
