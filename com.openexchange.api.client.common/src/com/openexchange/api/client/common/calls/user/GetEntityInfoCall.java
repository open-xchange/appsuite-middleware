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

package com.openexchange.api.client.common.calls.user;

import java.util.Map;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.annotation.NonNull;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.calls.AbstractGetCall;
import com.openexchange.api.client.common.parser.AbstractHttpResponseParser;
import com.openexchange.api.client.common.parser.CommonApiResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.groupware.EntityInfo.Type;


/**
 * {@link GetEntityInfoCall}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.5
 */
public class GetEntityInfoCall extends AbstractGetCall<EntityInfo> {

    private final String entityIdentifier;
    private final String id;

    /**
     * Initializes a new {@link GetEntityInfoCall}.
     * 
     * @param entityIdentifier The entity ID
     * @param id The user ID
     */
    public GetEntityInfoCall(String entityIdentifier, String id) {
        super();
        this.entityIdentifier = entityIdentifier;
        this.id = id;
    }

    /**
     * Initializes a new {@link GetEntityInfoCall}.
     * 
     * @param entityIdentifier The entity ID
     * @param id The user ID
     */
    public GetEntityInfoCall(String entityIdentifier, int id) {
        this(entityIdentifier, String.valueOf(id));
    }

    @Override
    @NonNull
    public String getModule() {
        return "/user";
    }

    @Override
    public HttpResponseParser<EntityInfo> getParser() {
        String identifier = String.valueOf(id);
        int entity = Integer.parseInt(id); //TODO: leave member as int
        return new AbstractHttpResponseParser<EntityInfo>() {

            @Override
            public EntityInfo parse(CommonApiResponse commonResponse, HttpContext httpContext) throws OXException, JSONException {
                if (commonResponse.isJSONObject()) {
                    JSONObject data = commonResponse.getJSONObject();
                    String displayName = data.optString("display_name");
                    String title = data.optString("title");
                    String firstName = data.optString("first_name");
                    String lastName = data.optString("last_name");
                    String email1 = data.optString("email1");
                    String imageUrl = data.optString("image1_url");
                    Type type = Type.USER;
                    return new EntityInfo(identifier, displayName, title, firstName, lastName, email1, entity, imageUrl, type);
                }
                return null;
            }
            
        };
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        putIfNotEmpty(parameters, "id", id);
    }

    @Override
    protected String getAction() {
        return "get";
    }

    protected String getEntityIdentifier() {
        return entityIdentifier;
    }

}
