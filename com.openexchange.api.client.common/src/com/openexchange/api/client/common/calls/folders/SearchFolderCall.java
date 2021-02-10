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
