/*
 *
 *    OPEN-XCHANGE legal informationn
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
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
