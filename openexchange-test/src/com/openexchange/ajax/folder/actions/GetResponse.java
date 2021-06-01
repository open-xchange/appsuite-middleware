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

package com.openexchange.ajax.folder.actions;

import java.util.Date;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.FolderFields;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.parser.FolderParser;
import com.openexchange.exception.OXException;
import com.openexchange.folder.json.parser.ParsedFolder;
import com.openexchange.folderstorage.Folder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 * {@link GetResponse}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public final class GetResponse extends AbstractAJAXResponse {

    private FolderObject folder;
    private Folder storageFolder;

    /**
     * Initializes a new {@link GetResponse}
     *
     * @param response The response
     */
    public GetResponse(final Response response) {
        super(response);
    }

    /**
     * @return the folder
     * @throws OXException parsing the folder out of the response fails.
     */
    public FolderObject getFolder() throws OXException {
        if (hasError()) {
            return null;
        }
        if (null == folder) {
            final FolderObject parsed = new FolderObject();
            final JSONObject data = (JSONObject) getData();
            try {
                if (data.has(FolderFields.ID)) {
                    rearrangeId(data);
                }
                if (data.has(FolderFields.FOLDER_ID)) {
                    final String tmp = data.getString(FolderFields.FOLDER_ID);
                    if (tmp.startsWith(FolderObject.SHARED_PREFIX)) {
                        data.put(FolderFields.FOLDER_ID, Integer.toString(FolderObject.SYSTEM_SHARED_FOLDER_ID));
                    }
                }
                if (data.has(FolderFields.LAST_MODIFIED)) {
                    parsed.setLastModified(new Date(data.getLong(FolderFields.LAST_MODIFIED)));
                }
            } catch (JSONException e) {
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create();
            }
            new FolderParser().parse(parsed, data);// .parse(parsed, (JSONObject) getData());
            fillInFullName(data, parsed);
            this.folder = parsed;
        }
        return folder;
    }

    public Folder getStorageFolder() throws OXException {

        if (hasError()) {
            return null;
        }
        if (null == storageFolder) {
            final JSONObject data = (JSONObject) getData();
            try {
                if (data.has(FolderFields.ID)) {
                    rearrangeId(data);
                }
                if (data.has(FolderFields.FOLDER_ID)) {
                    final String tmp = data.getString(FolderFields.FOLDER_ID);
                    if (tmp.startsWith(FolderObject.SHARED_PREFIX)) {
                        data.put(FolderFields.FOLDER_ID, Integer.toString(FolderObject.SYSTEM_SHARED_FOLDER_ID));
                    }
                }
            } catch (JSONException e) {
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create();
            }
            com.openexchange.folder.json.parser.FolderParser parser = new com.openexchange.folder.json.parser.FolderParser(new StaticDiscoveryService());

            ParsedFolder pfolder = parser.parseFolder(data, TimeZone.getDefault());// .parse(parsed, (JSONObject) getData());

            this.storageFolder = pfolder;
        }
        return storageFolder;

    }

    private void fillInFullName(final JSONObject data, final FolderObject parsed) {
        if (data.has("full_name")) {
            parsed.setFullName(data.optString("full_name"));
        }
    }

    private void rearrangeId(final JSONObject data) throws JSONException {
        try {
            Integer.parseInt(data.getString(FolderFields.ID));
        } catch (NumberFormatException x) {
            final String id = data.getString(FolderFields.ID);
            data.remove(FolderFields.ID);
            data.put("full_name", id);
        }
    }
}
