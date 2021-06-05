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

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.AJAXRequestResult.ResultType;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;

/**
 * {@link BackwardLinkAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class BackwardLinkAction extends AbstractFileAction {

    /**
     * Initializes a new {@link BackwardLinkAction}.
     */
    public BackwardLinkAction() {
        super();
    }

    @Override
    public AJAXRequestResult handle(InfostoreRequest request) throws OXException {
        boolean redirect = request.getBoolParameter("redirect");
        try {
            /*
             * parse parameters & generate backward link
             */
            String folderId = request.getParameter(Param.FOLDER_ID.getName());
            if (Strings.isEmpty(folderId)) {
                throw FileStorageExceptionCodes.MISSING_PARAMETER.create(Param.FOLDER_ID.getName());
            }
            String id = request.getParameter(Param.ID.getName());
            String backwardLink = request.getFileAccess().getBackwardLink(folderId, id, null);
            /*
             * send redirect if requested & possible, otherwise return as api response
             */
            if (redirect) {
                if (null == request.getRequestData()) {
                    throw AjaxExceptionCodes.UNEXPECTED_ERROR.create("Missing request data");
                }
                HttpServletResponse response = request.getRequestData().optHttpServletResponse();
                try {
                    response.sendRedirect(backwardLink);
                    return new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct").setType(ResultType.DIRECT);
                } catch (IOException e) {
                    throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
                }
            }
            return new AJAXRequestResult(new JSONObject().putSafe("link", backwardLink), "json");
        } catch (OXException e) {
            if (redirect) {
                HttpServletResponse response = null != request.getRequestData() ? request.getRequestData().optHttpServletResponse() : null;
                if (null != response) {
                    try {
                        Tools.sendErrorPage(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                        return new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct").setType(ResultType.DIRECT);
                    } catch (IOException i) {
                        throw AjaxExceptionCodes.IO_ERROR.create(i, i.getMessage());
                    }
                }
            }
            throw e;
        }
    }

}
