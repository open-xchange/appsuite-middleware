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

package com.openexchange.jslob.config;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSONPathElement;
import com.openexchange.jslob.JSlobEntry;

/**
 * {@link JSlobEntryWrapper} - a wrapper for a JSlob entry also providing the parsed path.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
final class JSlobEntryWrapper {

    /** The JSlob entry */
    private final JSlobEntry jSlobEntry;

    /** The parsed path */
    private final List<JSONPathElement> parsedPath;

    /**
     * Initializes a new {@link JSlobEntryWrapper}.
     *
     * @param jSlobEntry The entry to wrap
     * @throws OXException If path cannot be parsed
     */
    public JSlobEntryWrapper(JSlobEntry jSlobEntry) throws OXException {
        super();
        this.jSlobEntry = jSlobEntry;
        this.parsedPath = JSONPathElement.parsePath(jSlobEntry.getPath());
    }

    /**
     * Gets the parsed path
     *
     * @return The parsed path
     */
    public List<JSONPathElement> getParsedPath() {
        return parsedPath;
    }

    /**
     * Gets the wrapped JSlob entry.
     *
     * @return The wrapped entry
     */
    public JSlobEntry getJSlobEntry() {
        return jSlobEntry;
    }

}
