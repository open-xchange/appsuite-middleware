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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.openexchange.annotation.NonNull;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.ApiClientUtils;
import com.openexchange.api.client.common.calls.AbstractGetCall;
import com.openexchange.api.client.common.parser.JsonArrayParser;

/**
 * {@link ListFoldersCall} - The "list" action for the folders module
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class ListFoldersCall extends AbstractGetCall<List<RemoteFolder>> {

    private final String parent;
    protected final RemoteFolderField[] columns;

    /**
     * Initializes a new {@link ListFoldersCall}.
     *
     * @param parent The ID of the parent folder to list
     */
    public ListFoldersCall(String parent) {
        this(parent, RemoteFolderField.values());
    }

    /**
     * Initializes a new {@link ListFoldersCall}.
     *
     * @param parent The ID of the parent folder to list
     * @param columns The columns to
     */
    public ListFoldersCall(String parent, RemoteFolderField[] columns) {
        this.parent = parent;
        this.columns = columns;
    }

    @Override
    @NonNull
    public String getModule() {
        return "/folders";
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        final int[] columnIds = Arrays.stream(columns).mapToInt(f -> f.getColumn()).toArray();
        parameters.put("parent", parent);
        parameters.put("columns", ApiClientUtils.toCommaString(columnIds));
    }

    @Override
    protected String getAction() {
        return "list";
    }

    @Override
    public HttpResponseParser<List<RemoteFolder>> getParser() {
        return new JsonArrayParser<>(new RemoteFolderMapper(), columns);
    }
}
