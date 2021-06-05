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

package com.openexchange.file.storage.json.actions.services;

import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.json.FileStorageAccountWriter;
import com.openexchange.file.storage.json.actions.files.AbstractFileAction;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.tools.session.ServerSession;


/**
 * The common superclass for all AJAXActionServices handling file storage account management. Provides a unified handling
 * for JSONExceptions and stores commonly used services (the registry, a writer and a parser) for subclasses.
 * Subclasses must implement the {@link #doIt(AJAXRequestData, ServerSession)} method.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractFileAction.MODULE, type = RestrictedAction.Type.READ)
public abstract class AbstractFileStorageServiceAction implements AJAXActionService {

    protected FileStorageServiceRegistry registry;
    protected FileStorageAccountWriter writer;

    /**
     * Initializes a new {@link AbstractFileStorageServiceAction}.
     */
    protected AbstractFileStorageServiceAction(final FileStorageServiceRegistry registry) {
        this.registry = registry;
        writer = new FileStorageAccountWriter();
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        try {
            return doIt(requestData, session);
        } catch (JSONException x) {
            throw FileStorageExceptionCodes.JSON_ERROR.create(x,x.toString());
        }
    }

    protected abstract AJAXRequestResult doIt(AJAXRequestData request, ServerSession session) throws JSONException, OXException;

}
