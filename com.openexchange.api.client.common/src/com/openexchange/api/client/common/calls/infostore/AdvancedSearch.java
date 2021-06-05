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
 * {@link AdvancedSearch} - performs an advanced infostore search
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class AdvancedSearch extends AbstractPutCall<List<DefaultFile>> {

    private final JSONObject searchBody;
    private final String folderId;
    private final int[] columns;

    private String sortBy;
    private SortOrder order;
    private Integer start;
    private Integer end;
    private Boolean includeSubFolders;
    private Boolean pregenerateaPreviews;

    /**
     * Initializes a new {@link AdvancedSearch}.
     *
     * @param folderId The ID of the folder to search within
     * @param columns The columns to query
     * @param The search query
     */
    public AdvancedSearch(String folderId, int[] columns, JSONObject query) {
        this.folderId = folderId;
        this.columns = columns;
        this.searchBody = query;
    }

    /**
     * Sets the "sortby"
     *
     * @param sortBy the sort by field ID
     * @return this
     */
    public AdvancedSearch setSortBy(String sortBy) {
        this.sortBy = sortBy;
        return this;
    }

    /**
     * Sets the sort oder
     *
     * @param order The order
     * @return this
     */
    public AdvancedSearch setOrder(SortOrder order) {
        this.order = order;
        return this;
    }

    /**
     * Sets the start of the pagination
     *
     * @param start The start index (inclusive)
     * @return this
     */
    public AdvancedSearch setStart(Integer start) {
        this.start = start;
        return this;
    }

    /**
     * Sets the end of the pagination
     *
     * @param start The end index (excluding)
     * @return this
     */
    public AdvancedSearch setEnd(Integer end) {
        this.end = end;
        return this;
    }

    /**
     * Sets whether or not the search should include sub folders
     *
     * @param includeSubfolders Whether or not to include sub folders
     * @return this
     */
    public AdvancedSearch setIncludeSubfolders(Boolean includeSubfolders) {
        this.includeSubFolders = includeSubfolders;
        return this;
    }

    /**
     * Sets whether or not to pregenerate previews
     *
     * @param pregeneratePreviews Whether or not to pregenerate previews
     * @return this
     */
    public AdvancedSearch setPregeneratePreviews(Boolean pregeneratePreviews) {
        this.pregenerateaPreviews = pregeneratePreviews;
        return this;
    }

    @Override
    @NonNull
    public String getModule() {
        return "/infostore";
    }

    @Override
    @Nullable
    public HttpEntity getBody() throws OXException, JSONException {
        return ApiClientUtils.createJsonBody(this.searchBody);
    }

    @Override
    public HttpResponseParser<List<DefaultFile>> getParser() {
        return new DefaultFileListParser(columns);
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put("folder", folderId);
        parameters.put("columns", ApiClientUtils.toCommaString(columns));

        putIfPresent(parameters, "sort", sortBy);

        if (order != null) {
            parameters.put("order", order.toString().toLowerCase());
        }

        if (start != null) {
            parameters.put("start", String.valueOf(start));
        }
        if (end != null) {
            parameters.put("end", String.valueOf(end));
        }

        putIfPresent(parameters, "includeSubfolders", includeSubFolders);
        putIfPresent(parameters, "pregenerate_previews", pregenerateaPreviews);
    }

    @Override
    protected String getAction() {
        return "advancedSearch";
    }
}
