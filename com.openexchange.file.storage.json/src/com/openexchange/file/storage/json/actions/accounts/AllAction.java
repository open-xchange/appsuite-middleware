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

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AccountAware;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.LoginAwareFileStorageServiceExtension;
import com.openexchange.file.storage.json.FileStorageAccountConstants;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * A class implementing the "all" action for listing file storage accounts. Optionally only accounts of a certain service
 * are returned. Parameters are:
 * <dl>
 * <dt>filestorageService</dt><dd>(optional) The ID of the file storage service. If present lists only accounts of this service.</dd>
 * </dl>
 * Returns a JSONArray of JSONObjects representing the file storage accounts.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AllAction extends AbstractFileStorageAccountAction {

    static final Logger LOG = LoggerFactory.getLogger(AllAction.class.getName());

    public AllAction(final FileStorageServiceRegistry registry) {
        super(registry);
    }

    @Override
    protected AJAXRequestResult doIt(AJAXRequestData request, ServerSession session) throws JSONException, OXException {
        String fsServiceId = request.getParameter(FileStorageAccountConstants.FILE_STORAGE_SERVICE);
        Boolean connectionCheck = request.getParameter(FileStorageAccountConstants.CONNECTION_CHECK, Boolean.class, true);

        List<FileStorageService> services = new ArrayList<FileStorageService>();
        if (fsServiceId != null) {
            services.add(registry.getFileStorageService(fsServiceId));
        } else {
            services.addAll(registry.getAllServices());
        }

        JSONArray result = new JSONArray(services.size() << 1);
        AJAXRequestResult requestResult = new AJAXRequestResult(result);

        for (FileStorageService fsService : services) {
            // Get the accounts associated with current file storage service
            List<FileStorageAccount> userAccounts = null;
            if (fsService instanceof AccountAware) {
                userAccounts = AccountAware.class.cast(fsService).getAccounts(session);
            }
            if (null == userAccounts) {
                userAccounts = fsService.getAccountManager().getAccounts(session);
            }

            // Iterate accounts and append its JSON representation
            for (FileStorageAccount account : userAccounts) {
                FileStorageAccountAccess access = null;
                try {
                    access = fsService.getAccountAccess(account.getId(), session);
                    FileStorageFolder rootFolder = optRootFolder(access);

                    //Extended connection check if requested by the client and supported by the FileStorage
                    if (Boolean.TRUE.equals(connectionCheck) && account.getFileStorageService() instanceof LoginAwareFileStorageServiceExtension) {
                        LoginAwareFileStorageServiceExtension.class.cast(account.getFileStorageService()).testConnection(account, session);
                    }

                    result.put(writer.write(account, rootFolder, determineCapabilities(access), optMetadata(session, account)));

                } catch (OXException e) {
                    LOG.debug(e.getMessage(), e);
                    if (e.equalsCode(6, "OAUTH")) {
                        // "OAUTH-0006" --> OAuth account not found
                        try {
                            fsService.getAccountManager().deleteAccount(account, session);
                        } catch (Exception x) {
                            LOG.debug("Failed to delete the file storage account '{}' for user '{}' in context '{}'", account.getId(), I(session.getUserId()), I(session.getContextId()), x);
                        }
                    } else {
                        // Add account with error
                        boolean includeStackTraceOnError = AJAXRequestDataTools.parseBoolParameter(AJAXServlet.PARAMETER_INCLUDE_STACK_TRACE_ON_ERROR, request);
                        JSONObject accountJSON = writer.write(account, null, determineCapabilities(access), null);
                        accountJSON.put("hasError", true);
                        ResponseWriter.addException(accountJSON, e, localeFrom(session), includeStackTraceOnError);
                        result.put(accountJSON);
                    }
                }
            }
        }

        return requestResult;
    }
}
