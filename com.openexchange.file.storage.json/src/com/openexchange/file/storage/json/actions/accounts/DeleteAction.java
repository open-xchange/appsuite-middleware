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

import static com.openexchange.file.storage.json.FileStorageAccountConstants.FILE_STORAGE_SERVICE;
import static com.openexchange.file.storage.json.FileStorageAccountConstants.ID;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.json.actions.files.AbstractFileAction;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * This action deletes a file storage account. Parameters are:
 * <dl>
 *  <dt>filestorageService</dt> <dd>The ID of the messaging service. </dd>
 *  <dt>id</dt><dd>The id of the file storage service that is to be deleted</dd>
 * </dl>
 * Throws an exception upon an error or returns "1" on success.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractFileAction.MODULE, type = RestrictedAction.Type.WRITE)
public class DeleteAction extends AbstractFileStorageAccountAction {

    public DeleteAction(final FileStorageServiceRegistry registry) {
        super(registry);
    }

    @Override
    protected AJAXRequestResult doIt(AJAXRequestData request, ServerSession session) throws JSONException, OXException {
        List<String> missingParameters = request.getMissingParameters(FILE_STORAGE_SERVICE, ID);
        if (false == missingParameters.isEmpty()) {
            throw FileStorageExceptionCodes.MISSING_PARAMETER.create(missingParameters.toString());
        }
        /*
         * get targeted account
         */
        FileStorageService fileStorageService = registry.getFileStorageService(request.getParameter(FILE_STORAGE_SERVICE));
        FileStorageAccountManager accountManager = fileStorageService.getAccountManager();
        FileStorageAccount account = accountManager.getAccount(request.getParameter(ID), session);
        /*
         * delete the account & return appropriate result on success
         */
        accountManager.deleteAccount(account, session);
        return new AJAXRequestResult(Integer.valueOf(1));
    }

}
