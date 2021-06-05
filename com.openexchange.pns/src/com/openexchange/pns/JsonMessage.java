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

package com.openexchange.pns;

import org.json.JSONValue;

/**
 * {@link JsonMessage} - A message containing a JSON payload.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class JsonMessage implements Message<JSONValue> {

    private final JSONValue jsonValue;

    /**
     * Initializes a new {@link JsonMessage}.
     *
     * @param jsonValue The JSON value
     * @throws IllegalArgumentException If JSON value is <code>null</code>
     */
    public JsonMessage(JSONValue jsonValue) {
        super();
        if (null == jsonValue) {
            throw new IllegalArgumentException("jsonValue must not be null");
        }
        this.jsonValue = jsonValue;
    }

    @Override
    public JSONValue getMessage() {
        return jsonValue;
    }

    @Override
    public String toString() {
        return jsonValue.toString();
    }

}
