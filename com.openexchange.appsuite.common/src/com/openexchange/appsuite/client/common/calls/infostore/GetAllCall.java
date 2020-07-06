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

package com.openexchange.appsuite.client.common.calls.infostore;

import java.util.List;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.annotation.NonNull;
import com.openexchange.appsuite.client.AppsuiteClientExceptions;
import com.openexchange.appsuite.client.common.AppsuiteClientUtils;
import com.openexchange.appsuite.client.common.calls.AbstractGetAppsuiteCall;
import com.openexchange.appsuite.client.common.calls.infostore.mapping.DefaultFileMapper;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.groupware.container.DataObject;

/**
 * {@link GetAllCall}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class GetAllCall extends AbstractGetAppsuiteCall<List<DefaultFile>> {

    private final String folderId;
    private final int[] columns;
    private final Integer sortColumn;
    private final SortDirection sortDirection;

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
        this.folderId = folderId;
        this.columns = columns;
        this.sortColumn = sortColumn;
        this.sortDirection = sortDirection;
    }

    @Override
    @NonNull
    public String getPath() {
        return "/infostore";
    }

    @Override
    public List<DefaultFile> parse(HttpResponse response, HttpContext httpContext) throws OXException {
        JSONArray data = AppsuiteClientUtils.parseDataArray(response);
        DefaultFileMapper mapper = new DefaultFileMapper();
        try {
            List<DefaultFile> files = mapper.deserialize(data, mapper.getMappedFields(columns));
            return files;
        } catch (JSONException e) {
            throw AppsuiteClientExceptions.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put("folder", folderId);
        parameters.put("columns", AppsuiteClientUtils.toCommaString(columns));
        if (this.sortColumn != null) {
            parameters.put("sort", sortColumn.toString());
            parameters.put("order", sortDirection.toString());
        }
    }

    @Override
    protected String getAction() {
        return "all";
    }
}
