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

import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.drive.json.internal.DefaultDriveSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.Strings;

/**
 * {@link SubfoldersAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SubfoldersAction extends AbstractDriveAction {

    private static String ROOT_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID);

    @Override
    public AJAXRequestResult doPerform(AJAXRequestData requestData, DefaultDriveSession session) throws OXException {
        /*
         * get parent folder identifier & prepare custom drive session containing an appropriate root folder for the subfolders request
         */
        String parentFolderID = requestData.getParameter("parent");
        String rootFolderID = Strings.isEmpty(parentFolderID) ? ROOT_FOLDER_ID : parentFolderID;
        DefaultDriveSession driveSession = new DefaultDriveSession(
            session.getServerSession(), rootFolderID, session.getHostData(), session.getApiVersion(), session.getClientVersion(), session.getLocale());
        /*
         * get & return metadata for subfolders as JSON
         */
        List<JSONObject> metadata = getDriveService().getUtility().getSubfolderMetadata(driveSession);
        return new AJAXRequestResult(new JSONArray(metadata), "json");
    }

    @Override
    protected boolean requiresRootFolderID() {
        return false;
    }

}
