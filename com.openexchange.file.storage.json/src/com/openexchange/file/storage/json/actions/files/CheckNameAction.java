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

import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.composition.FilenameValidationUtils;
import com.openexchange.tools.servlet.AjaxExceptionCodes;


/**
 * {@link CheckNameAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class CheckNameAction extends AbstractFileAction {

    /**
     * Initializes a new {@link CheckNameAction}.
     */
    public CheckNameAction() {
        super();
    }

    @Override
    protected AJAXRequestResult handle(InfostoreRequest request) throws OXException {
        String name = request.getParameter("name");
        if (null == name) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("name");
        }
        FilenameValidationUtils.checkCharacters(name);
        FilenameValidationUtils.checkName(name);
        return new AJAXRequestResult(new JSONObject());
    }

}
