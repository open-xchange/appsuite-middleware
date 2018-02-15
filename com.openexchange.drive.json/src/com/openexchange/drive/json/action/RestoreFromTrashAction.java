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
                files = parseArrayToList(body, "files");
            }

            if (body.has("folders")) {
                folders = parseArrayToList(body, "folders");
            }
            RestoreContent content = getDriveService().getUtility().restoreFromTrash(session, files, folders);
         // TODO QS-KR: Is it okay to return a null object and define the format as "json"? wouldnt it be better 
            // to return an empty JSONObject instead or with empty lists for files and folders? RestoreContent can be null, which may be not so clear at this point.
            return new AJAXRequestResult(toJSON(content, session.getServerSession().getUser().getLocale()), "json");
        } catch (JSONException e) {
            throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
        }
    }
    
    // TODO QS-KR: Maybe document the private methods also
    
    private List<String> parseArrayToList(JSONObject body, String arrayName) throws JSONException, OXException {
        JSONArray array = body.getJSONArray(arrayName);
        List<String> result = new ArrayList<>(array.length());
        for (Object o : array) {
            if (!(o instanceof String)) {
                throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
            }
            result.add((String) o);
        }
        return result;
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
            JSONArray restoredJSON = parseMapToArray(locale, restoredFiles);
            result.put("files", restoredJSON);
        }

        if (restoredFolders != null) {
            JSONArray restoredFoldersJSON = parseMapToArray(locale, restoredFolders);
            result.put("folders", restoredFoldersJSON);
        }

        return result;
    }
    
    private JSONArray parseMapToArray(Locale locale, Map<String, FileStorageFolder[]> restoredContent) throws JSONException {
        JSONArray restoredJSON = new JSONArray(restoredContent.size());
        for (Map.Entry<String, FileStorageFolder[]> restoredObject : restoredContent.entrySet()) {
            JSONObject restored = new JSONObject(2);
            restored.put("name", restoredObject.getKey());

            FileStorageFolder[] path = restoredObject.getValue();
            JSONArray jPath = new JSONArray(path.length);
            for (FileStorageFolder folder : path) {
                JSONObject jPathEntry = new JSONObject(2);
                jPathEntry.put("id", folder.getId());
                jPathEntry.put("title", folder.getLocalizedName(locale));
                jPath.put(jPathEntry);
            }
            restored.put("path", jPath);
            restoredJSON.put(restored);
        }
        return restoredJSON;
    }

}
