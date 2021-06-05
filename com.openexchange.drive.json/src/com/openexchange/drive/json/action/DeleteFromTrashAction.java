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

package com.openexchange.drive.json.action;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.drive.json.internal.DefaultDriveSession;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 *
 * {@link DeleteFromTrashAction}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class DeleteFromTrashAction extends AbstractDriveWriteAction {

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

        List<String> files = new ArrayList<>();
        List<String> folders = new ArrayList<>();
        try {
            if (body.has("files")) {
                JSONArray filesArray = body.getJSONArray("files");
                for (Object o : filesArray) {
                    if (!(o instanceof String)) {
                        throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
                    }
                    files.add((String) o);
                }
            }

            if (body.has("directories")) {
                JSONArray foldersArray = body.getJSONArray("directories");
                for (Object o : foldersArray) {
                    if (!(o instanceof String)) {
                        throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
                    }
                    folders.add((String) o);
                }
            }
        } catch (JSONException e) {
            throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
        }

        getDriveService().getUtility().removeFromTrash(session, files, folders);

        // Load trash contents again
        JSONObject result = getDriveService().getUtility().getTrashContent(session);
        return new AJAXRequestResult(result == null ? JSONObject.EMPTY_OBJECT : result, "json");
    }

}
