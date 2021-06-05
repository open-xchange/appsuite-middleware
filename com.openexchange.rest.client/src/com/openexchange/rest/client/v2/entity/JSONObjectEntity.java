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

package com.openexchange.rest.client.v2.entity;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
import com.openexchange.java.Charsets;
import com.openexchange.rest.client.v2.RESTBodyEntity;

/**
 * {@link JSONObjectEntity}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class JSONObjectEntity implements RESTBodyEntity {

    private final HttpEntity entity;

    /**
     * Initialises a new {@link JSONObjectEntity}.
     */
    public JSONObjectEntity(JSONObject body) {
        super();
        entity = new StringEntity(body.toString(), Charsets.UTF_8);
    }

    @Override
    public HttpEntity getBodyEntity() {
        return entity;
    }
}
