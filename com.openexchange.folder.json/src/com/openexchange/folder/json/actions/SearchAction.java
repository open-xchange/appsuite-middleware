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

package com.openexchange.folder.json.actions;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.folder.json.Constants;
import com.openexchange.folder.json.FolderField;
import com.openexchange.folder.json.services.ServiceRegistry;
import com.openexchange.folder.json.writer.FolderWriter;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.filestorage.contentType.FileStorageContentType;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SearchAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.5
 */
public class SearchAction extends AbstractFolderAction {

    protected static final String ACTION = "search";

    public static final int[] DEFAULT_COLUMNS = new int[] {
        FolderField.ID.getColumn(), FolderField.CREATED_BY.getColumn(), FolderField.MODIFIED_BY.getColumn(), FolderField.CREATION_DATE.getColumn(),
        FolderField.LAST_MODIFIED.getColumn(), FolderField.CREATED_FROM.getColumn(), FolderField.MODIFIED_FROM.getColumn(), FolderField.FOLDER_NAME.getColumn()
    };

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData request, ServerSession session) throws OXException, JSONException {
        /*
         * Parse parameters
         */
        String treeId = request.getParameter("tree");
        if (null == treeId) {
            /*
             * Fallback to default tree identifier
             */
            treeId = getDefaultTreeIdentifier();
        }
        final String folderId = request.requireParameter("id");
        String module = request.requireParameter("module");
        ContentType contentType = null;
        if ("files".equals(module) || "infostore".equals(module)) {
            contentType = FileStorageContentType.getInstance();
        } else {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(module, "module");
        }
        int[] columns = DEFAULT_COLUMNS;
        String sColums = request.getParameter("columns");
        if (Strings.isNotEmpty(sColums)) {
            columns = parseOptionalIntArrayParameter("columns", request);
        }
        Object data = request.requireData();
        JSONObject body = (JSONObject) data;
        String query = body.getString("query");
        int start = body.optInt("start", 0);
        int size = body.optInt("size", 50);
        long date = body.optLong("date", -1);
        boolean includeSubfolders = body.optBoolean("includeSubfolders", true);
        boolean all = body.optBoolean("all", true);

        FolderServiceDecorator decorator = new FolderServiceDecorator();
        Object connection = session.getParameter(Connection.class.getName());
        if (null != connection) {
            decorator.put(Connection.class.getName(), connection);
        }
        decorator.put("altNames", Boolean.TRUE.toString());
        decorator.setLocale(session.getUser().getLocale());

        FolderService folderService = ServiceRegistry.getInstance().getService(FolderService.class, true);
        FolderResponse<List<UserizedFolder>> response = folderService.searchFolderByName(treeId, folderId, contentType, query, date, includeSubfolders, all, start, size, session, decorator);
        List<UserizedFolder> folders = response.getResponse();
        long lastModified = 0;
        for (int i = folders.size() - 1; i >= 0; i--) {
            Date modified = folders.get(i).getLastModifiedUTC();
            if (modified != null && lastModified < modified.getTime()) {
                lastModified = modified.getTime();
            }
        }
        JSONArray jsonArray = FolderWriter.writeMultiple2Array(request, columns, folders.toArray(new UserizedFolder[folders.size()]), Constants.ADDITIONAL_FOLDER_FIELD_LIST);
        return new AJAXRequestResult(jsonArray, 0 == lastModified ? null : new Date(lastModified)).addWarnings(response.getWarnings());
    }

}
