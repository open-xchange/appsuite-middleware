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

import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.drive.json.internal.DefaultDriveSession;
import com.openexchange.drive.json.json.JsonFileVersion;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link MoveFileAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.3
 */
public class MoveFileAction extends AbstractDriveWriteAction {

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, DefaultDriveSession session) throws OXException {
        /*
         * parse parameters
         */
        String path = requestData.getParameter("path");
        if (Strings.isEmpty(path)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("path");
        }
        String name = requestData.getParameter("name");
        if (Strings.isEmpty(name)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("name");
        }
        String checksum = requestData.getParameter("checksum");
        if (Strings.isEmpty(checksum)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("checksum");
        }
        String newName = requestData.getParameter("newName");
        String newPath = requestData.getParameter("newPath");
        if (Strings.isEmpty(newName) && Strings.isEmpty(newPath)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("newName,newPath");
        }
        /*
         * move the file, return empty result in case of success
         */
        getDriveService().getUtility().moveFile(session, path, new JsonFileVersion(checksum, name), newPath, newName);
        return new AJAXRequestResult(new JSONObject(), "json");
    }

}
