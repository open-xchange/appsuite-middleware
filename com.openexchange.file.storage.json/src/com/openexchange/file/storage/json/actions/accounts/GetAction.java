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

package com.openexchange.file.storage.json.actions.accounts;

import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.json.FileStorageAccountConstants;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * Loads a file storage account. Parameters are:
 * <dl>
 * <dt>filestorageService</dt> <dd>The ID of the messaging service. </dd>
 * <dt>id</dt><dd>The id of the messaging service that is to be loaded</dd>
 * </dl>
 * Throws an exception upon an error or returns the loaded FileStorageAccount JSON representation.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class GetAction extends AbstractFileStorageAccountAction {

    public GetAction(final FileStorageServiceRegistry registry) {
        super(registry);
    }

    @Override
    protected AJAXRequestResult doIt(final AJAXRequestData request, final ServerSession session) throws JSONException, OXException {
        List<String> missingParameters = request.getMissingParameters(FileStorageAccountConstants.FILE_STORAGE_SERVICE, FileStorageAccountConstants.ID);
        if (!missingParameters.isEmpty()) {
            throw FileStorageExceptionCodes.MISSING_PARAMETER.create(missingParameters.toString());
        }

        String fsServiceId = request.getParameter(FileStorageAccountConstants.FILE_STORAGE_SERVICE);
        FileStorageService fsService = registry.getFileStorageService(fsServiceId);

        String id = request.getParameter(FileStorageAccountConstants.ID);
        FileStorageAccount account = fsService.getAccountManager().getAccount(id, session);

        FileStorageAccountAccess access = fsService.getAccountAccess(account.getId(), session);
        FileStorageFolder rootFolder = optRootFolder(access);

        try {
            return new AJAXRequestResult(writer.write(account, rootFolder, determineCapabilities(access), optMetadata(session, account)));
        } catch (OXException e) {
            //Add account with error
            boolean includeStackTraceOnError = AJAXRequestDataTools.parseBoolParameter(AJAXServlet.PARAMETER_INCLUDE_STACK_TRACE_ON_ERROR, request);
            JSONObject accountJSON = writer.write(account, null, determineCapabilities(access), null);
            accountJSON.put("hasError", true);
            ResponseWriter.addException(accountJSON, e, localeFrom(session), includeStackTraceOnError);
            return new AJAXRequestResult(accountJSON);
        }
    }
}
