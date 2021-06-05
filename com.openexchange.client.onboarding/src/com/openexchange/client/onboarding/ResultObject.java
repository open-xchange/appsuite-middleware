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

package com.openexchange.client.onboarding;

import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link ResultObject} - The result object for an {@link Result on-boarding result}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public interface ResultObject {

    /**
     * Gets the object
     *
     * @return The object
     */
    public Object getObject();

    /**
     * Gets the format; if <code>null</code> <code>"json"</code> is assumed
     *
     * @return The format or <code>null</code>
     */
    public String getFormat();

    /**
     * Indicates if the the ResulObject has warnings
     * 
     * @return true if the ResultObject has warnings, false otherwise
     */
    public boolean hasWarnings();

    /**
     * Retrieves the warnings of this ResultObject
     * 
     * @return The warnings or null if {@link #hasWarnings} returns false
     */
    public List<OXException> getWarnings();

    /**
     * Add a warning to this ResultObject
     * 
     * @param warning The warning
     */
    public void addWarning(OXException warning);

}
