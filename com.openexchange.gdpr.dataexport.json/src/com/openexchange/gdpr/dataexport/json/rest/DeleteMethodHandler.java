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

package com.openexchange.gdpr.dataexport.json.rest;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link DeleteMethodHandler} - Serves the REST-like <code>DELETE</code> request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DeleteMethodHandler extends AbstractDataExportMethodHandler {

    /**
     * Initializes a new {@link DeleteMethodHandler}.
     */
    public DeleteMethodHandler() {
        super();
    }

    // DELETE /gdpr/dataexport

    @Override
    protected void modifyByPathInfo(AJAXRequestData requestData, String[] restPathElements, HttpServletRequest req) throws IOException, OXException {
        String action = getAction(restPathElements, req);
        if (action == null) {
            throw AjaxExceptionCodes.BAD_REQUEST.create();
        }
        requestData.setAction(action);

        if ("cancel".equals(action)) {
            return;
        }
        if ("delete".equals(action)) {
            return;
        }
        throw AjaxExceptionCodes.UNKNOWN_ACTION.create(req.getPathInfo());
    }

    @Override
    protected String doGetAction(String[] restPathElements, HttpServletRequest restRequest) {
        if (hasNoPathInfo(restPathElements)) {
            // Cancel user-associated task.
            return "cancel";
        }

        int length = restPathElements.length;
        if (0 == length) {
            // Cancel user-associated task.
            return "cancel";
        }

        if (1 == length && "delete".equals(restPathElements[0])) {
            // DELETE /gdpr/dataexport/delete
            return "delete";
        }

        return null;
    }

}
