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

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountMetaDataUtil;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.LoginAwareFileStorageServiceExtension;
import com.openexchange.file.storage.SharingFileStorageService;
import com.openexchange.file.storage.json.FileStorageAccountConstants;
import com.openexchange.file.storage.json.actions.files.AbstractFileAction;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.tools.session.ServerSession;


/**
 * Updates a messaging account. The request must contain a JSON representation of the changes to the messaging account (that is: all fields that are to be changed)
 * and the account id. Returns "1" on success.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractFileAction.MODULE, type = RestrictedAction.Type.WRITE)
public class UpdateAction extends AbstractFileStorageAccountAction {

    public UpdateAction(final FileStorageServiceRegistry registry) {
        super(registry);
    }

    @Override
    protected AJAXRequestResult doIt(final AJAXRequestData request, final ServerSession session) throws JSONException, OXException {

        final JSONObject data = (JSONObject) request.requireData();
        if (!data.has(FileStorageAccountConstants.ID)) {
            throw FileStorageExceptionCodes.MISSING_PARAMETER.create(FileStorageAccountConstants.ID);
        }

        final FileStorageAccount account = parser.parse(data);
        FileStorageService fileStorageService = account.getFileStorageService();
        if(fileStorageService instanceof SharingFileStorageService) {
            //Clear last recent error in order to try the new configuration
            ((SharingFileStorageService)fileStorageService).resetRecentError(account.getId(), session);
        }
        final boolean doConnectionCheck = account.getFileStorageService() instanceof LoginAwareFileStorageServiceExtension && account.getConfiguration() != null;

        //load existing account for resetting if the connection check failed
        FileStorageAccount existingAccount = account.getFileStorageService().getAccountManager().getAccount(account.getId(), session);
        if(existingAccount != null) {
            //Preserve account meta data when updating
            FileStorageAccountMetaDataUtil.copy(existingAccount, account);
        }

        //perform update
        account.getFileStorageService().getAccountManager().updateAccount(account, session);

        if (doConnectionCheck) {
            try {
                //test connection
                ((LoginAwareFileStorageServiceExtension) account.getFileStorageService()).testConnection(account, session);
            } catch (OXException e) {
                //reset
                if(existingAccount != null) {
                    account.getFileStorageService().getAccountManager().updateAccount(existingAccount, session);
                }
                throw e;
            }
        }
        return new AJAXRequestResult(Integer.valueOf(1));
    }

}
