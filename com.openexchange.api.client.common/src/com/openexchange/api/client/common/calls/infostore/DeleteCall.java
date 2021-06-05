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

import static com.openexchange.java.Autoboxing.B;
import java.util.Arrays;
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
import com.openexchange.api.client.common.parser.IgnonringParser;
import com.openexchange.exception.OXException;

/**
 * {@link DeleteCall}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class DeleteCall extends AbstractPutCall<Void> {

    private final List<InfostoreTuple> filesToDelete;
    private final long timestamp;
    private final Boolean hardDelete;
    private final String pushToken;

    /**
     * Initializes a new {@link DeleteCall}.
     *
     * @param fileToDelete The file to delete
     * @param timestamp The last known timestamp
     */
    public DeleteCall(InfostoreTuple fileToDelete, long timestamp) {
        this(Arrays.asList(fileToDelete), timestamp, null, null);
    }

    /**
     * Initializes a new {@link DeleteCall}.
     *
     * @param filesToDelete A list of files to delete
     * @param timestamp The last known timestamp
     */
    public DeleteCall(List<InfostoreTuple> filesToDelete, long timestamp) {
        this(filesToDelete, timestamp, null, null);
    }

    /**
     * Initializes a new {@link DeleteCall}.
     *
     * @param fileToDelete The file to delete
     * @param timestamp The last known timestamp
     * @param hardDelete True in order to delete the document, false to move it into the trash bin
     * @param pushToken The push token
     */
    public DeleteCall(InfostoreTuple fileToDelete, long timestamp, boolean hardDelete, String pushToken) {
        this(Arrays.asList(fileToDelete), timestamp, B(hardDelete), pushToken);
    }

    /**
     * Initializes a new {@link DeleteCall}.
     *
     * @param filesToDelete A list of files to delete
     * @param timestamp The last known timestamp
     * @param hardDelete True in order to delete the document, false to move it into the trash bin
     */
    public DeleteCall(List<InfostoreTuple> filesToDelete, long timestamp, boolean hardDelete) {
        this(filesToDelete, timestamp, B(hardDelete), null);
    }

    /**
     * Initializes a new {@link DeleteCall}.
     *
     * @param filesToDelete A list of files to delete
     * @param timestamp The last known timestamp
     * @param hardDelete True in order to delete the document, false to move it into the trash bin
     * @param pushToken The push token
     */
    public DeleteCall(List<InfostoreTuple> filesToDelete, long timestamp, Boolean hardDelete, String pushToken) {
        this.filesToDelete = filesToDelete;
        this.timestamp = timestamp;
        this.hardDelete = hardDelete;
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
        JSONArray array = new JSONArray(filesToDelete.size());
        for (InfostoreTuple fileToDelete : filesToDelete) {
            JSONObject json = new JSONObject();
            json.put("id", fileToDelete.getId());
            json.put("folder", fileToDelete.getFolderId());
            array.put(json);
        }
        return ApiClientUtils.createJsonBody(array);
    }

    @Override
    public HttpResponseParser<Void> getParser() {
        return new IgnonringParser();
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put("timestamp", String.valueOf(timestamp));
        putIfPresent(parameters, "hardDelete", String.valueOf(hardDelete));
        putIfNotEmpty(parameters, "pushToken", pushToken);
    }

    @Override
    protected String getAction() {
        return "delete";
    }

}
