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
import com.openexchange.annotation.NonNull;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.ApiClientUtils;
import com.openexchange.api.client.common.calls.AbstractGetCall;
import com.openexchange.api.client.common.parser.DefaultFileListParser;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;

/**
 * {@link VersionsCall}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class VersionsCall extends AbstractGetCall<List<DefaultFile>> {

    private final String id;
    private final int[] columns;
    private final Integer sortColumn;
    private final SortDirection sortDirection;

    /**
     * Initializes a new {@link VersionsCall}.
     *
     * @param id The ID of the item to get the version from
     * @param columns The columns to fetch
     * @param sortColumn The sorting column
     * @param sortDirection The sort direction
     */
    public VersionsCall(String id, int[] columns, Integer sortColumn, SortDirection sortDirection) {
        super();
        this.id = id;
        this.columns = columns;
        this.sortColumn = sortColumn;
        this.sortDirection = sortDirection;
    }

    @Override
    @NonNull
    public String getModule() {
        return "/infostore";
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put("id", id);
        parameters.put("columns", ApiClientUtils.toCommaString(columns));
        if (this.sortColumn != null) {
            parameters.put("sort", sortColumn.toString());
            parameters.put("order", sortDirection.toString());
        }
    }

    @Override
    protected String getAction() {
        return "versions";
    }

    @Override
    public HttpResponseParser<List<DefaultFile>> getParser() {
        return new DefaultFileListParser(columns);
    }
}
