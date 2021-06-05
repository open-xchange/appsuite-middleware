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

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * {@link SimpleOXModule}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SimpleOXModule {

    private final SimpleOXClient client;
    private final String moduleName;

    public SimpleOXModule(SimpleOXClient client, String moduleName) {
        super();
        this.client = client;
        this.moduleName = moduleName;
    }

    public SimpleResponse call(String action, Object... parameters) throws JSONException, IOException {
        return client.call(moduleName, action, parameters);
    }

    public JSONObject raw(String action, Object... parameters) throws JSONException, IOException {
        return client.raw(moduleName, action, parameters);
    }

}
