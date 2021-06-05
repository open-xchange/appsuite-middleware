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

package com.openexchange.ajax.drive.action;

import java.util.Date;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.xing.util.JSONCoercion;

/**
 * {@link GetLinkParser}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class GetLinkParser extends AbstractAJAXParser<GetLinkResponse> {

    /**
     * Initializes a new {@link GetLinkParser}.
     *
     * @param failOnError
     */
    protected GetLinkParser(boolean failOnError) {
        super(failOnError);
    }

    @Override
    protected GetLinkResponse createResponse(Response response) throws JSONException {
        JSONObject json = (JSONObject) response.getData();

        GetLinkResponse retval = new GetLinkResponse(response);

        if (json == null) {
            return retval;
        }

        if (json.hasAndNotNull("url")) {
            retval.setUrl(json.getString("url"));
        }
        if (json.hasAndNotNull("is_new")) {
            retval.setNew(json.getBoolean("is_new"));
        }
        if (json.hasAndNotNull("expiry_date")) {
            retval.setExpiryDate(new Date(json.getLong("expiry_date")));
        }
        if (json.hasAndNotNull("meta")) {
            retval.setMeta((Map<String, Object>) JSONCoercion.coerceToNative(json.getJSONObject("meta")));
        }
        return retval;
    }

}
