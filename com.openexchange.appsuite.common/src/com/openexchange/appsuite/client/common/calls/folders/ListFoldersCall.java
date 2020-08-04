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

package com.openexchange.appsuite.client.common.calls.folders;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.annotation.NonNull;
import com.openexchange.appsuite.client.AppsuiteClientExceptions;
import com.openexchange.appsuite.client.HttpResponseParser;
import com.openexchange.appsuite.client.common.AppsuiteClientUtils;
import com.openexchange.appsuite.client.common.calls.AbstractGetAppsuiteCall;
import com.openexchange.exception.OXException;

/**
 * {@link ListFoldersCall} - The "list" action for the folders module
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class ListFoldersCall extends AbstractGetAppsuiteCall<List<RemoteFolder>> {

    private final String parent;
    private final RemoteFolderField[] columns;

    /**
     * Initializes a new {@link ListFoldersCall}.
     *
     * @param parent
     */
    public ListFoldersCall(String parent) {
        //@formatter:off
        this(parent, new RemoteFolderField[] {
            RemoteFolderField.ID,
            RemoteFolderField.TITLE,
            RemoteFolderField.CREATED_BY,
            RemoteFolderField.CREATION_DATE,
            RemoteFolderField.LAST_MODIFIED});
        //@formatter:on
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
    public String getPath() {
        return "/folders";
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        final int[] columnIds = Arrays.stream(columns).mapToInt(f -> f.getColumn()).toArray();
        parameters.put("parent", parent);
        parameters.put("columns", AppsuiteClientUtils.toCommaString(columnIds));
    }

    @Override
    protected String getAction() {
        return "list";
    }

    @Override
    public HttpResponseParser<List<RemoteFolder>> getParser() throws OXException {
        return new HttpResponseParser<List<RemoteFolder>>() {

            @Override
            public List<RemoteFolder> parse(HttpResponse response, HttpContext httpContext) throws OXException {
                try {
                    JSONArray data = AppsuiteClientUtils.parseDataArray(response);
                    RemoteFolderMapper mapper = new RemoteFolderMapper();
                    return mapper.deserialize(data, columns);
                } catch (JSONException e) {
                    throw AppsuiteClientExceptions.JSON_ERROR.create(e, e.getMessage());
                }
            }
        };
    }
}
