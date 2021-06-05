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

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.annotation.NonNull;
import com.openexchange.annotation.Nullable;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.ApiClientUtils;
import com.openexchange.api.client.common.calls.AbstractPutCall;
import com.openexchange.api.client.common.parser.AbstractHttpResponseParser;
import com.openexchange.api.client.common.parser.CommonApiResponse;
import com.openexchange.exception.OXException;

/**
 * {@link DetachCall} - Deletes version of an infoitem
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class DetachCall extends AbstractPutCall<List<Integer>> {

    private final String id;
    private final String folder;
    private final long timestamp;
    private final String pushToken;

    private final int[] versionsToDelete;

    /**
     * Initializes a new {@link DetachCall}.
     *
     * @param id The ID of the file to delete the versions for
     * @param folder The folder ID of the file
     * @param timestamp The timestamp / sequencenumber
     * @param versionsToDelete A list of versions to delete
     */
    public DetachCall(String id, String folder, long timestamp, int[] versionsToDelete) {
        this(id, folder, timestamp, versionsToDelete, null);
    }

    /**
     * Initializes a new {@link DetachCall}.
     *
     * @param id The ID of the file to delte the versions for
     * @param folder The folder ID of the file
     * @param timestamp The timestamp / sequencenumber
     * @param versionsToDelete A list of versions to delete
     * @param pushToken The optional drive push-token
     */
    public DetachCall(String id, String folder, long timestamp, int[] versionsToDelete, @Nullable String pushToken) {
        this.id = id;
        this.folder = folder;
        this.timestamp = timestamp;
        this.versionsToDelete = versionsToDelete;
        this.pushToken = pushToken;
    }

    @Override
    @NonNull
    public String getModule() {
        return "/infostore";
    }

    @Override
    @Nullable
    public HttpEntity getBody() throws OXException, JSONException {
        JSONArray jsonArray = new JSONArray(versionsToDelete.length);
        for (int i = 0; i < versionsToDelete.length; i++) {
            jsonArray.put(versionsToDelete[i]);
        }
        return ApiClientUtils.createJsonBody(jsonArray);
    }

    @Override
    public HttpResponseParser<List<Integer>> getParser() {
        return new AbstractHttpResponseParser<List<Integer>>() {

            @Override
            public List<Integer> parse(CommonApiResponse commonResponse, HttpContext httpContext) throws OXException, JSONException {
                JSONArray jsonArray = commonResponse.getJSONArray();
                List<Integer> ret = new ArrayList<>(jsonArray.length());
                for (int i = 0; i < jsonArray.length(); i++) {
                    if (!jsonArray.isNull(i)) {
                        ret.add(I(jsonArray.getInt(i)));
                    }
                }
                return ret;
            }
        };
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put("id", id);
        parameters.put("folder", folder);
        parameters.put("timestamp", String.valueOf(timestamp));
        putIfNotEmpty(parameters, "pushToken", pushToken);
    }

    @Override
    protected String getAction() {
        return "detach";
    }
}
