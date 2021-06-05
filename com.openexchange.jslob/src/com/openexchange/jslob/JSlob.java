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

import java.io.Serializable;
import org.json.JSONObject;

/**
 * {@link JSlob} - A JSlob holding a JSON object.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface JSlob extends Serializable {

    /**
     * Creates and returns a copy of this object.
     * 
     * @return A clone of this instance.
     * @see java.lang.Cloneable
     */
    public JSlob clone();

    /**
     * Gets the identifier
     * 
     * @return The identifier
     */
    public JSlobId getId();

    /**
     * Sets the identifier
     * 
     * @param id The identifier to set
     * @return This JSlob with new identifier applied
     */
    public JSlob setId(JSlobId id);

    /**
     * Gets the JSON object stored in this JSlob.
     * 
     * @return The JSON object
     */
    public JSONObject getJsonObject();

    /**
     * Gets the json object with unmodifiable metadata describing the regular payload data
     * 
     * @return The metadata object
     */
    public JSONObject getMetaObject();

}
