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
import com.openexchange.file.storage.LoginAwareFileStorageServiceExtension;
import com.openexchange.file.storage.json.actions.files.AbstractFileAction;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.tools.session.ServerSession;


/**
 * Creates a new MessagingAccount. The body of the request must contain the JSON representation of the given account.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractFileAction.MODULE, type = RestrictedAction.Type.WRITE)
public class NewAction extends AbstractFileStorageAccountAction {

    public NewAction(final FileStorageServiceRegistry registry) {
        super(registry);
    }

    @Override
    protected AJAXRequestResult doIt(final AJAXRequestData request, final ServerSession session) throws JSONException, OXException {
        FileStorageAccount account = parser.parse((JSONObject) request.requireData());
        final String id = account.getFileStorageService().getAccountManager().addAccount(account, session);
        if (account.getFileStorageService() instanceof LoginAwareFileStorageServiceExtension) {
            try {
                //load account and test connection
                account = account.getFileStorageService().getAccountManager().getAccount(id, session);
                ((LoginAwareFileStorageServiceExtension) account.getFileStorageService()).testConnection(account, session);
            }
            catch(OXException e) {
               account.getFileStorageService().getAccountManager().deleteAccount(account, session);
               throw e;
            }
        }
        return new AJAXRequestResult(id);
    }

}
