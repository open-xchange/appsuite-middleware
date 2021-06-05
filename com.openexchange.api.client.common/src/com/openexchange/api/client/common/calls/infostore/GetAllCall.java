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
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.groupware.container.DataObject;

/**
 * {@link GetAllCall} - Gets all infoitems in a specified folder
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class GetAllCall extends AbstractGetCall<List<DefaultFile>> {

    private final String folderId;
    private final int[] columns;
    private final Integer sortColumn;
    private final SortDirection sortDirection;
    private final Integer leftHandLimit;
    private final Integer rightHandLimit;

    /**
     * Initializes a new {@link GetAllCall}.
     *
     * @param folderId The ID of the folder to get the items for
     */
    public GetAllCall(String folderId) {
        //@formatter:off
        this(folderId,
            new int[] { DataObject.OBJECT_ID,
                        File.Field.TITLE.getNumber(),
                        File.Field.FILENAME.getNumber() },
            null,
            SortDirection.DEFAULT);
        //@formatter:on
    }

    /**
     * Initializes a new {@link GetAllCall}.
     *
     * @param folderId The ID of the folder to get the items for
     * @param columns The columns of the items to fetch
     * @param sortColumn The column to sort, or null to not apply any sorting
     * @param sortDirection The sort direction, or null if not applying any sorting
     */
    public GetAllCall(String folderId, int[] columns, Integer sortColumn, SortDirection sortDirection) {
        this(folderId, columns, sortColumn, sortDirection, null, null);
    }

    /**
     * Initializes a new {@link GetAllCall}.
     *
     * @param folderId The ID of the folder to get the items for
     * @param columns The columns of the items to fetch
     * @param sortColumn The column to sort, or null to not apply any sorting
     * @param sortDirection The sort direction, or null if not applying any sorting
     * @param leftHandLimit A positive integer number to specify the "right-hand" limit of the range to return
     * @param rightHandLimit A positive integer number to specify the "left-hand" limit of the range to return
     */
    public GetAllCall(String folderId, int[] columns, Integer sortColumn, SortDirection sortDirection, Integer leftHandLimit, Integer rightHandLimit) {
        this.folderId = folderId;
        this.columns = columns;
        this.sortColumn = sortColumn;
        this.sortDirection = sortDirection;
        this.leftHandLimit = leftHandLimit;
        this.rightHandLimit = rightHandLimit;
    }

    @Override
    @NonNull
    public String getModule() {
        return "/infostore";
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put("folder", folderId);
        parameters.put("columns", ApiClientUtils.toCommaString(columns));
        if (this.sortColumn != null) {
            parameters.put("sort", sortColumn.toString());
            parameters.put("order", sortDirection.toString());
        }
        putIfPresent(parameters, "left_hand_limit", leftHandLimit);
        putIfPresent(parameters, "right_hand_limit", rightHandLimit);
    }

    @Override
    protected String getAction() {
        return "all";
    }

    @Override
    public HttpResponseParser<List<DefaultFile>> getParser() {
        return new DefaultFileListParser(columns);
    }
}
