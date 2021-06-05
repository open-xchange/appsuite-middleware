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

package com.openexchange.ajax.request;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Folder;
import com.openexchange.exception.OXException;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link FolderRequest} - Folder request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FolderRequest {

    private final ServerSession session;

    private final JSONWriter pw;

    private static final Folder FOLDER_SERVLET = new Folder();

    public FolderRequest(final ServerSession session, final JSONWriter pw) {
        super();
        this.session = session;
        this.pw = pw;
    }

    public void action(final String action, final JSONObject jsonObject) throws JSONException, OXException {
        if (action.equalsIgnoreCase(AJAXServlet.ACTION_ROOT)) {
            FOLDER_SERVLET.actionGetRoot(session, pw, jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_LIST)) {
            FOLDER_SERVLET.actionGetSubfolders(session, pw, jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_PATH)) {
            FOLDER_SERVLET.actionGetPath(session, pw, jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATES)) {
            FOLDER_SERVLET.actionGetUpdatedFolders(session, pw, jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_GET)) {
            FOLDER_SERVLET.actionGetFolder(session, pw, jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATE)) {
            FOLDER_SERVLET.actionPutUpdateFolder(session, pw, jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_NEW)) {
            FOLDER_SERVLET.actionPutInsertFolder(session, pw, jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_DELETE)) {
            FOLDER_SERVLET.actionPutDeleteFolder(session, pw, jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_CLEAR)) {
            FOLDER_SERVLET.actionPutClearFolder(session, pw, jsonObject);
        } else {
            throw OXFolderExceptionCode.UNKNOWN_ACTION.create(action);
        }
    }

}
