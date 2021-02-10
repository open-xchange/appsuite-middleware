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
