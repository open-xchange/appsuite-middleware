/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
                       File.Field.FILENAME.getNumber()},
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
