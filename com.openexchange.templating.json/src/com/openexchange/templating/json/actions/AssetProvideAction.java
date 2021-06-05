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

package com.openexchange.templating.json.actions;

import java.io.File;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.templating.TemplateErrorMessage;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link AssetProvideAction}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
@DispatcherNotes(allowPublicSession = true)
public class AssetProvideAction implements AJAXActionService {

    private final ServiceLookup services;

    public AssetProvideAction(ServiceLookup services) {
        this.services = services;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData request, ServerSession session) throws OXException {
        String templateDirectory = services.getService(ConfigurationService.class).getProperty("com.openexchange.templating.assets.path");

        String requestedAsset = request.getParameter("name");

        File asset = new File(new File(templateDirectory), requestedAsset);

        // Check for directory traversal
        if (!asset.getParent().equals(templateDirectory)){
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("name", requestedAsset);
        }

        if (!asset.exists()) {
            throw TemplateErrorMessage.TemplateNotFound.create();
        }
        return new AJAXRequestResult(new FileHolder(asset), "file");
    }

}
