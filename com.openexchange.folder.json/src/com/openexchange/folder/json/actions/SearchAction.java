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
