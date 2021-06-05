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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.annotation.NonNull;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.calls.AbstractGetCall;
import com.openexchange.api.client.common.calls.user.UserInformation.Builder;
import com.openexchange.api.client.common.parser.AbstractHttpResponseParser;
import com.openexchange.api.client.common.parser.CommonApiResponse;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link GetUserCall}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class GetUserCall extends AbstractGetCall<UserInformation> {

    private String id;

    /**
     * Initializes a new {@link GetUserCall}.
     */
    public GetUserCall() {
        this(null);
    }

    /**
     * Initializes a new {@link GetUserCall}.
     * 
     * @param id The ID of the user to get. <code>null</code> for the current session user
     */
    public GetUserCall(String id) {
        super();
        this.id = id;
    }

    @Override
    @NonNull
    public String getModule() {
        return "user";
    }

    @Override
    protected String getAction() {
        return "get";
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        if (Strings.isNotEmpty(id)) {
            parameters.put("id", id);
        }
    }

    @Override
    public HttpResponseParser<UserInformation> getParser() {
        return new AbstractHttpResponseParser<UserInformation>() {

            @Override
            public UserInformation parse(CommonApiResponse commonResponse, HttpContext httpContext) throws OXException, JSONException {
                JSONObject jsonObject = commonResponse.getJSONObject();
                Builder builder = new UserInformation.Builder(); // @formatter:off
                builder.lastModified(jsonObject.optLong("last_modified"))
                    .lastModifiedUtc(jsonObject.optLong("last_modified_utc"))
                    .numberOfImages(jsonObject.optInt("number_of_images"))
                    .folderId(jsonObject.optString("folder_id"))
                    .sortName(jsonObject.optString("sort_name"))
                    .userId(jsonObject.optInt("user_id"))
                    .createdBy(jsonObject.optInt("created_by"))
                    .modifiedBy(jsonObject.optInt("modified_by"))
                    .id(jsonObject.optInt("id"))
                    .email1(jsonObject.optString("email1"))
                    .creationDate(jsonObject.optLong("creation_date"))
                    .locale(jsonObject.optString("locale"))
                    .contactId(jsonObject.optInt("contact_id"))
                    .guestCreatedBy(jsonObject.optInt("guest_created_by"))
                    .timezone(jsonObject.optString("timezone"));
                // @formatter:on

                JSONArray groups = jsonObject.optJSONArray("groups");
                if (null != groups) {
                    for (int i = 0; i < groups.length(); i++) {
                        builder.addGroup(groups.optInt(i));
                    }
                }
                JSONArray aliases = jsonObject.optJSONArray("aliases");
                if (null != aliases) {
                    for (int i = 0; i < aliases.length(); i++) {
                        builder.addAliases(aliases.optString(i));
                    }
                }
                return builder.build();
            }
        };
    }

}
