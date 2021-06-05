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

package com.openexchange.ajax.simple;

import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.tools.JSONCoercion;

/**
 * {@link SimpleResponse}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SimpleResponse {

    private Object data;
    private String error;

    public SimpleResponse(JSONObject response) throws JSONException {
        if (response.has("error")) {
            this.error = response.toString();
        } else if (response.has("data")) {
            this.data = JSONCoercion.coerceToNative(response.get("data"));
        }
    }

    public boolean hasError() {
        return error != null;
    }

    public String getError() {
        return error;
    }

    public Object getData() {
        return data;
    }

    public Map<String, Object> getObjectData() {
        return (Map<String, Object>) data;
    }

    public List<List<Object>> getListData() {
        return (List<List<Object>>) data;
    }
}
