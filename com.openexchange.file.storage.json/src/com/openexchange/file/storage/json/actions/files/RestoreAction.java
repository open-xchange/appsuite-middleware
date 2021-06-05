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

package com.openexchange.file.storage.json.actions.files;

import java.util.Locale;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.json.services.Services;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link RestoreAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class RestoreAction extends AbstractWriteAction {

    /**
     * Initializes a new {@link RestoreAction}.
     */
    public RestoreAction() {
        super();
    }

    @Override
    protected AJAXRequestResult handle(InfostoreRequest request) throws OXException {
        // Determine user's default Drive folder
        FolderService folderService = Services.getFolderService();
        if (null == folderService) {
            throw ServiceExceptionCode.absentService(FolderService.class);
        }
        ServerSession session = request.getSession();
        String folderId = folderService.getDefaultFolder(session.getUser(), "1", InfostoreContentType.getInstance(), null, session, new FolderServiceDecorator()).getID();
        folderId = new FolderID(folderId).getFolderId();

        // Execute the restore
        IDBasedFileAccess fileAccess = request.getFileAccess();
        Map<FileID, FileStorageFolder[]> result = fileAccess.restore(request.getIds(), folderId);

        // Build JSON response
        Locale locale = request.getSession().getUser().getLocale();
        try {
            JSONArray jsonResult = new JSONArray(result.size());
            for (Map.Entry<FileID, FileStorageFolder[]> entry : result.entrySet()) {
                FileID id = entry.getKey();
                FileStorageFolder[] path = entry.getValue();

                JSONObject jRestored = new JSONObject(2).put("id", id.toString());
                JSONArray jPath = new JSONArray(path.length);
                for (FileStorageFolder p : path) {
                    JSONObject jPathEntry = new JSONObject(2);
                    jPathEntry.put("id", p.getId());
                    jPathEntry.put("title", p.getLocalizedName(locale));
                    jPath.put(jPathEntry);
                }
                jRestored.put("path", jPath);
                jsonResult.put(jRestored);
            }
            return new AJAXRequestResult(jsonResult, "json");
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
