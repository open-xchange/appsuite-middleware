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

package com.openexchange.drive.json.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.drive.RestoreContent;
import com.openexchange.drive.json.internal.DefaultDriveSession;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 *
 * {@link RestoreFromTrashAction}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class RestoreFromTrashAction extends AbstractDriveAction {

    @Override
    public AJAXRequestResult doPerform(AJAXRequestData requestData, DefaultDriveSession session) throws OXException {

        Object data = requestData.getData();
        if (data == null) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }
        if (!(data instanceof JSONObject)) {
            throw AjaxExceptionCodes.ILLEGAL_REQUEST_BODY.create();
        }

        JSONObject body = (JSONObject) data;

        List<String> files = Collections.emptyList();
        List<String> folders = Collections.emptyList();
        try {
            if (body.has("files")) {
                JSONArray filesArray = body.getJSONArray("files");
                files = new ArrayList<>(filesArray.length());
                for (Object o : filesArray) {
                    if (!(o instanceof String)) {
                        throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
                    }
                    files.add((String) o);
                }
            }

            if (body.has("folders")) {
                JSONArray foldersArray = body.getJSONArray("folders");
                folders = new ArrayList<>(foldersArray.length());
                for (Object o : foldersArray) {
                    if (!(o instanceof String)) {
                        throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
                    }
                    folders.add((String) o);
                }
            }


            RestoreContent content = getDriveService().getUtility().restoreFromTrash(session, files, folders);
            return new AJAXRequestResult(toJSON(content, session.getServerSession().getUser().getLocale()), "json");
        } catch (JSONException e) {
            throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
        }
    }

    private JSONObject toJSON(RestoreContent content, Locale locale) throws JSONException {
        if (content == null) {
            return null;
        }

        Map<String, FileStorageFolder[]> restoredFolders = content.getRestoredFolders();
        Map<String, FileStorageFolder[]> restoredFiles = content.getRestoredFiles();
        if (restoredFiles == null && restoredFolders == null) {
            return null;
        }

        JSONObject result = new JSONObject(4);
        if (restoredFiles != null) {
            JSONArray restoredFilesJSON = new JSONArray(restoredFiles.size());
            for (Map.Entry<String, FileStorageFolder[]> restoredFilesEntry : restoredFiles.entrySet()) {
                JSONObject restoredFile = new JSONObject(2);
                restoredFile.put("name", restoredFilesEntry.getKey());

                FileStorageFolder[] path = restoredFilesEntry.getValue();
                JSONArray jPath = new JSONArray(path.length);
                for (FileStorageFolder folder : path) {
                    JSONObject jPathEntry = new JSONObject(2);
                    jPathEntry.put("id", folder.getId());
                    jPathEntry.put("title", folder.getLocalizedName(locale));
                    jPath.put(jPathEntry);
                }
                restoredFile.put("path", jPath);
                restoredFilesJSON.put(restoredFile);
            }
            result.put("files", restoredFilesJSON);
        }

        if (restoredFolders != null) {
            JSONArray restoredFoldersJSON = new JSONArray(restoredFolders.size());
            for (Map.Entry<String, FileStorageFolder[]> restoredFoldersEntry : restoredFolders.entrySet()) {
                JSONObject restoredFolder = new JSONObject(2);
                restoredFolder.put("name", restoredFoldersEntry.getKey());

                FileStorageFolder[] path = restoredFoldersEntry.getValue();
                JSONArray jPath = new JSONArray(path.length);
                for (FileStorageFolder folder : path) {
                    JSONObject jPathEntry = new JSONObject(2);
                    jPathEntry.put("id", folder.getId());
                    jPathEntry.put("title", folder.getLocalizedName(locale));
                    jPath.put(jPathEntry);
                }
                restoredFolder.put("path", jPath);
                restoredFoldersJSON.put(restoredFolder);
            }
            result.put("folders", restoredFoldersJSON);
        }

        return result;
    }

}
