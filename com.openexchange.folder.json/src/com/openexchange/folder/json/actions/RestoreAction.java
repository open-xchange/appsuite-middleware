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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.folder.json.Constants;
import com.openexchange.folder.json.FolderField;
import com.openexchange.folder.json.services.ServiceRegistry;
import com.openexchange.folder.json.writer.FolderWriter;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.RestoringFolderService;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.java.Strings;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link RestoreAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.0
 */
public class RestoreAction extends AbstractFolderAction {

    /** The action identifier for the <code>"restore"</code> action */
    public final static String ACTION = "restore";

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData request, ServerSession session) throws OXException, JSONException {
        FolderService folderService = ServiceRegistry.getInstance().getService(FolderService.class, true);
        if (false == (folderService instanceof RestoringFolderService)) {
            throw FileStorageExceptionCodes.NO_RESTORE_SUPPORT.create();
        }

        String treeId = request.getParameter("tree");
        if (Strings.isEmpty(treeId)) {
            treeId = getDefaultTreeIdentifier();
        }

        List<String> folderIds;
        {
            JSONArray jsonArray = (JSONArray) request.requireData();
            folderIds = new ArrayList<>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                folderIds.add(jsonArray.getString(i));
            }
        }

        FolderServiceDecorator decorator = getDecorator(request);
        UserizedFolder defaultFolder = folderService.getDefaultFolder(session.getUser(), treeId, InfostoreContentType.getInstance(), session, decorator);
        FolderResponse<Map<String, List<UserizedFolder>>> result = ((RestoringFolderService) folderService).restoreFolderFromTrash(treeId, folderIds, defaultFolder, session, decorator);
        Map<String, List<UserizedFolder>> restoreResult = result.getResponse();

        JSONArray jResults = new JSONArray(restoreResult.size());
        String keyFolderId = FolderField.ID.getName();
        int[] columns = new int[] { FolderField.ID.getColumn(), FolderField.FOLDER_NAME.getColumn() };
        for (Map.Entry<String, List<UserizedFolder>> resultEntry : restoreResult.entrySet()) {
            String folder = resultEntry.getKey();
            List<UserizedFolder> path = resultEntry.getValue();

            JSONObject json = new JSONObject(2);
            json.put(keyFolderId, folder);
            JSONArray array = new JSONArray(path.size());
            for (UserizedFolder userizedFolder : path) {
                array.put(FolderWriter.writeSingle2Object(request, columns, userizedFolder, Constants.ADDITIONAL_FOLDER_FIELD_LIST));
            }
            json.put("path", array);
            jResults.put(json);
        }
        return new AJAXRequestResult(jResults, "json");
    }

}
