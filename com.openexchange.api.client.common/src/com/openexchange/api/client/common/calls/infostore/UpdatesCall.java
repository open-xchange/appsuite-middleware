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

import static com.openexchange.java.Autoboxing.l;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.annotation.NonNull;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.ApiClientUtils;
import com.openexchange.api.client.common.calls.AbstractGetCall;
import com.openexchange.api.client.common.calls.infostore.mapping.DefaultFileMapper;
import com.openexchange.api.client.common.parser.AbstractHttpResponseParser;
import com.openexchange.api.client.common.parser.CommonApiResponse;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;

/**
 * {@link UpdatesCall} - Gets the new, modified and deleted infoitems
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class UpdatesCall extends AbstractGetCall<UpdatesResponse> {

    /**
     * An update type which can be used to specify which types should be ignored
     */
    public enum UpdateType {
        /**
         * Deleted files
         */
        DELETED;
    }

    protected final String folderId;
    protected final int[] columns;

    private final Long timestamp;
    private final UpdateType[] ignored;
    private final String sortedBy;
    private final SortOrder order;
    private final Boolean pregenerated_previews;

    /**
     * Initializes a new {@link UpdatesCall}.
     *
     * @param folderId The ID of the folder to check for updates
     * @param columns The columns to fetch
     * @param timestamp The timestamp of the last update of the requested items
     * @param sortedBy The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.
     * @param order The sort order. If this parameter is specified, then the parameter sort must be also specified.
     */
    public UpdatesCall(String folderId, int[] columns, Long timestamp, String sortedBy, SortOrder order) {
        this(folderId, columns, timestamp, null, sortedBy, order, null);
    }

    /**
     * Initializes a new {@link UpdatesCall}.
     *
     * @param folderId The ID of the folder to check for updates
     * @param columns The columns to fetch
     * @param timestamp The timestamp of the last update of the requested items
     * @param ignored A list of types that should be ignored
     * @param sortedBy The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.
     * @param order The sort order. If this parameter is specified, then the parameter sort must be also specified.
     * @param pregenerated_previews If set to "true" preview generation is triggered in the background for all files in request result
     */
    public UpdatesCall(String folderId, int[] columns, Long timestamp, UpdateType[] ignored, String sortedBy, SortOrder order, Boolean pregenerated_previews) {
        this.folderId = Objects.requireNonNull(folderId, "folderId must not be null");
        this.columns = Objects.requireNonNull(columns, "columns must not be null");
        this.timestamp = timestamp;
        this.ignored = ignored;
        this.sortedBy = sortedBy;
        this.order = order;
        this.pregenerated_previews = pregenerated_previews;
    }

    @Override
    @NonNull
    public String getModule() {
        return "/infostore";
    }

    @Override
    public HttpResponseParser<UpdatesResponse> getParser() {
        return new AbstractHttpResponseParser<UpdatesResponse>() {

            @Override
            public UpdatesResponse parse(CommonApiResponse commonResponse, HttpContext httpContext) throws OXException, JSONException {
                JSONArray jsonArray = commonResponse.getJSONArray();
                JSONArray jsonNewItems = null;
                JSONArray jsonModifiedItems = null;
                List<String> jsonDeletedFileIDs = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    Object object = jsonArray.get(i);
                    if (object instanceof JSONArray) {
                        if (jsonNewItems == null) {
                            jsonNewItems = (JSONArray) object;
                        } else {
                            jsonModifiedItems = (JSONArray) object;
                        }
                    } else if (object instanceof String) {
                        jsonDeletedFileIDs.add((String) object);
                    }
                }

                DefaultFileMapper mapper = new DefaultFileMapper();
                List<DefaultFile> newFiles = Collections.emptyList();
                List<DefaultFile> modifiedFiles = Collections.emptyList();
                List<DefaultFile> deletedFiles = Collections.emptyList();

                if (jsonNewItems != null) {
                    newFiles = mapper.deserialize(jsonNewItems, mapper.getMappedFields(columns));
                }

                if (jsonModifiedItems != null) {
                    modifiedFiles = mapper.deserialize(jsonModifiedItems, mapper.getMappedFields(columns));
                }

                if (jsonDeletedFileIDs.size() > 0) {
                    deletedFiles = new ArrayList<>(jsonDeletedFileIDs.size());
                    for (String deletedFileId : jsonDeletedFileIDs) {
                        DefaultFile deletedFile = new DefaultFile();
                        deletedFile.setId(deletedFileId);
                        deletedFile.setFolderId(folderId);
                        deletedFiles.add(deletedFile);
                    }
                }

                return new UpdatesResponse(newFiles, modifiedFiles, deletedFiles, l(commonResponse.getTimestamp()));
            }
        };
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put("folder", folderId);
        parameters.put("columns", ApiClientUtils.toCommaString(columns));
        putIfPresent(parameters, "timestamp", timestamp.toString());
        putIfPresent(parameters, "ignored", ApiClientUtils.toCommaString((Object[]) ignored));
        putIfPresent(parameters, "sortedBy", sortedBy);
        putIfPresent(parameters, "order", order.toString().toLowerCase());
        putIfPresent(parameters, "pregenerated_previews", pregenerated_previews.toString());
    }

    @Override
    protected String getAction() {
        return "updates";
    }
}
