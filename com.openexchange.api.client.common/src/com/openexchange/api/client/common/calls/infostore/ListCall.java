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

import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.annotation.NonNull;
import com.openexchange.annotation.Nullable;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.ApiClientUtils;
import com.openexchange.api.client.common.calls.AbstractPutCall;
import com.openexchange.api.client.common.parser.DefaultFileListParser;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;

/**
 * {@link ListCall} - Gets a list of infoitems
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class ListCall extends AbstractPutCall<List<DefaultFile>> {

    private final List<InfostoreTuple> ids;
    private final int[] columns;
    private final Boolean pregeneratePreviews;

    /**
     * Initializes a new {@link ListCall}.
     *
     * @param ids The IDs of the items to list
     * @param columns The columns to return
     */
    public ListCall(List<InfostoreTuple> ids, int[] columns) {
        this(ids, columns, null);
    }

    /**
     * Initializes a new {@link ListCall}.
     *
     * @param ids The IDs of the items to list
     * @param columns The columns to return
     * @param pregeneratePreviews If set to "true" preview generation
     *            is triggered in the background for all files in request result
     */
    public ListCall(List<InfostoreTuple> ids, int[] columns, Boolean pregeneratePreviews) {
        this.ids = ids;
        this.columns = columns;
        this.pregeneratePreviews = pregeneratePreviews;
    }

    @Override
    @NonNull
    public String getModule() {
        return "/infostore";
    }

    @Override
    @Nullable
    public HttpEntity getBody() throws OXException, JSONException {
        JSONArray array = new JSONArray(ids.size());
        for (InfostoreTuple id : ids) {
            JSONObject json = new JSONObject();
            json.put("id", id.getId());
            json.put("folder", id.getFolderId());
            array.put(json);
        }
        return ApiClientUtils.createJsonBody(array);
    }

    @Override
    public HttpResponseParser<List<DefaultFile>> getParser() {
        return new DefaultFileListParser(columns);
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put("columns", ApiClientUtils.toCommaString(columns));
        putIfPresent(parameters, "pregeneratePreviews", pregeneratePreviews);
    }

    @Override
    protected String getAction() {
        return "list";
    }
}
