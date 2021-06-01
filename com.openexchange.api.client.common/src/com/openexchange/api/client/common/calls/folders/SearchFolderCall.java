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

package com.openexchange.api.client.common.calls.folders;

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
import com.openexchange.exception.OXException;


/**
 * {@link SearchFolderCall}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.5
 */
public class SearchFolderCall extends AbstractPutCall<List<RemoteFolder>> {

    public static final int[] DEFAULT_COLUMNS = new int[] {
        RemoteFolderField.ID.getColumn(), RemoteFolderField.CREATED_BY.getColumn(), RemoteFolderField.MODIFIED_BY.getColumn(), RemoteFolderField.CREATION_DATE.getColumn(),
        RemoteFolderField.LAST_MODIFIED.getColumn(), RemoteFolderField.CREATED_FROM.getColumn(), RemoteFolderField.MODIFIED_FROM.getColumn(), RemoteFolderField.TITLE.getColumn()
    };

    private final String tree;
    private final String id;
    private final int[] columns; 
    private final String module;
    private final String query;
    private final long date;
    private final boolean includeSubfolders;
    private final boolean all;
    private final int start;
    private final int size;

    public SearchFolderCall(String tree, String id, int[] columns, String module, String query, long date, boolean includeSubfolders, boolean all, int start, int size) {
        super();
        this.tree = tree;
        this.id = id;
        this.columns = columns;
        this.module = module;
        this.query = query;
        this.date = date;
        this.includeSubfolders = includeSubfolders;
        this.all = all;
        this.start = start;
        this.size = size;
    }

    @Override
    @NonNull
    public String getModule() {
        return "/folders";
    }

    @Override
    public HttpResponseParser<List<RemoteFolder>> getParser() {
        return new RemoteFolderParser(columns);
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put("tree", tree);
        parameters.put("id", id);
        parameters.put("columns", toCommaSeparatedString(columns));
        parameters.put("module", module);
    }

    @Override
    protected String getAction() {
        return "search";
    }

    @Override
    @Nullable
    public HttpEntity getBody() throws OXException, JSONException {
        JSONObject body = new JSONObject(6);
        body.put("query", query);
        body.put("start", start);
        body.put("size", size);
        body.put("includeSubfolders", includeSubfolders);
        body.put("all", all);
        body.put("date", date);
        return ApiClientUtils.createJsonBody(body);
    }

    private String toCommaSeparatedString(int[] columns) {
        if (null == columns || 0 == columns.length) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int id : columns) {
            sb.append(id).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

}
