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
import java.util.Collections;
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
 * {@link RestoreFromTrashAction}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class RestoreFromTrashAction extends AbstractDriveWriteAction {

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

            if (body.has("directories")) {
                folders = parseArrayToList(body, "directories");
            }
            getDriveService().getUtility().restoreFromTrash(session, files, folders);

            // Load trash contents again
            JSONObject result = getDriveService().getUtility().getTrashContent(session);
            return new AJAXRequestResult(result == null ? JSONObject.EMPTY_OBJECT : result, "json");
        } catch (JSONException e) {
            throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
        }
    }

    /**
     * Parses an the json array with the name arrayName to a {@link List} of {@link String}
     *
     * @param body The {@link JSONObject} which contains the {@link JSONArray} field
     * @param arrayName The name of the {@link JSONArray}
     * @return A {@link List} of {@link String}
     * @throws JSONException
     * @throws OXException
     */
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

}
