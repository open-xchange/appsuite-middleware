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

package com.openexchange.api.client.common.calls.infostore;

import java.util.Map;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.annotation.NonNull;
import com.openexchange.api.client.ApiClientExceptions;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.calls.AbstractGetCall;
import com.openexchange.api.client.common.calls.infostore.mapping.DefaultFileMapper;
import com.openexchange.api.client.common.parser.AbstractHttpResponseParser;
import com.openexchange.api.client.common.parser.CommonApiResponse;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.groupware.tools.mappings.json.JsonMapping;
import com.openexchange.java.Strings;

/**
 * {@link GetCall}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class GetCall extends AbstractGetCall<DefaultFile> {

    private final String folder;
    private final String id;
    private final String version;

    /**
     * Initializes a new {@link GetCall}.
     *
     * @param folder The ID of the folder who contains the info item
     * @param id The ID of the requested info item
     */
    public GetCall(String folder, String id) {
        this(folder, id, null);
    }

    /**
     * Initializes a new {@link GetCall}.
     *
     * @param folder The ID of the folder who contains the info item
     * @param id The ID of the requested info item
     * @param version The version to get, or null to get the current version
     */
    public GetCall(String folder, String id, String version) {
        this.folder = folder;
        this.id = id;
        this.version = version;
    }

    @Override
    @NonNull
    public String getModule() {
        return "/infostore";
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put("id", id);
        parameters.put("folder", folder);
        putIfPresent(parameters, "version", version);
    }

    @Override
    protected String getAction() {
        return "get";
    }

    @Override
    public HttpResponseParser<DefaultFile> getParser() {
        return new AbstractHttpResponseParser<DefaultFile>() {

            private void setMedia(JSONObject json, DefaultFileMapper mapper, DefaultFile file) throws JSONException, OXException {
                if (json.hasAndNotNull("media") && !mapper.getMappings().isEmpty()) {
                    JSONObject jsonMedia = json.getJSONObject("media");
                    for (Field mediaField : File.Field.MEDIA_FIELDS) {
                        JsonMapping<? extends Object, DefaultFile> jsonMapping = mapper.getMappings().get(mediaField);
                        if (jsonMapping != null && Strings.isNotEmpty(jsonMapping.getAjaxName()) && jsonMedia.has(jsonMapping.getAjaxName())) {
                            jsonMapping.deserialize(jsonMedia, file);
                        }
                    }
                }
            }

            @Override
            public DefaultFile parse(CommonApiResponse commonResponse, HttpContext httpContext) throws OXException, JSONException {

                if (commonResponse.isJSONObject()) {
                    JSONObject jsonObject = commonResponse.getJSONObject();
                    DefaultFileMapper mapper = new DefaultFileMapper();
                    DefaultFile file = mapper.deserialize(jsonObject, mapper.getMappedFields());
                    //"media" is actually not a field but a nested object
                    setMedia(jsonObject, mapper, file);
                    return file;
                }
                throw ApiClientExceptions.JSON_ERROR.create("Not an JSON object");
            }
        };
    }
}
