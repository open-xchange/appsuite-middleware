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

package com.openexchange.file.storage.json;

import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.json.FormContentWriter;
import com.openexchange.datatypes.genericonf.json.FormDescriptionWriter;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccounts;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.composition.FolderID;

/**
 * Renders a FileStorageAccount in its JSON representation also using the dynamic form description of the parent file storage service.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FileStorageAccountWriter {

    /**
     * Name of the JSON attribute containing the error message.
     */
    public static final String ERROR = "error";

    /**
     * Name of the JSON attribute containing the error categories.
     */
    public static final String ERROR_CATEGORIES = "categories";

    /**
     * <b>Deprecated</b>: Name of the JSON attribute containing the error category.
     */
    public static final String ERROR_CATEGORY = "category";

    /**
     * Name of the JSON attribute containing the error code.
     */
    public static final String ERROR_CODE = "code";

    /**
     * Name of the JSON attribute containing the unique error identifier.
     */
    public static final String ERROR_ID = "error_id";

    /**
     * Name of the JSON attribute containing the array of the error message attributes.
     */
    public static final String ERROR_PARAMS = "error_params";

    /**
     * Name of the JSON attribute containing the stacks of the error.
     */
    public static final String ERROR_STACK = "error_stack";

    /**
     * Name of the JSON attribute containing the rather technical error description.
     */
    public static final String ERROR_DESC = "error_desc";

    /**
     * Initializes a new {@link FileStorageAccountWriter}.
     */
    public FileStorageAccountWriter() {
        super();
    }

    /**
     * Writes given account into its JSON representation.
     *
     * @param account The account
     * @param rootFolder The accounts root folder, or <code>null</code> if there is none
     * @param capabilities The capabilities to include, or <code>null</code> if not applicable
     * @param metadata A json object providing additional arbitrary metadata of the account for clients, or <code>null</code> if not applicable
     * @return The resulting JSON
     * @throws JSONException If writing JSON fails
     */
    public JSONObject write(FileStorageAccount account, FileStorageFolder rootFolder, Set<String> capabilities, JSONObject metadata) throws JSONException {
        JSONObject accountJSON = new JSONObject();
        accountJSON.put(FileStorageAccountConstants.ID, account.getId());
        FileStorageService fsService = account.getFileStorageService();
        accountJSON.put(FileStorageAccountConstants.QUALIFIED_ID, FileStorageAccounts.getQualifiedID(account));
        accountJSON.put(FileStorageAccountConstants.DISPLAY_NAME, account.getDisplayName());
        accountJSON.put(FileStorageAccountConstants.FILE_STORAGE_SERVICE, fsService.getId());
        if (null != rootFolder) {
            accountJSON.put(FileStorageAccountConstants.ROOT_FOLDER, new FolderID(fsService.getId(), account.getId(), rootFolder.getId()).toUniqueID());
        }
        accountJSON.put(FileStorageAccountConstants.IS_DEFAULT_ACCOUNT, FileStorageAccounts.isDefaultAccount(account));

        DynamicFormDescription formDescription = fsService.getFormDescription();
        if (null != formDescription && null != account.getConfiguration()) {
            JSONObject configJSON = FormContentWriter.write(formDescription, account.getConfiguration(), null);
            accountJSON.put(FileStorageAccountConstants.CONFIGURATION, configJSON);
        }

        accountJSON.putOpt("capabilities", null != capabilities ? new JSONArray(capabilities) : null);
        accountJSON.putOpt("metadata", metadata);

        return accountJSON;
    }

    /**
     * Writes the given file storage service into its JSON representation.
     *
     * @param service The file storage service
     * @return The resulting JSON
     * @throws JSONException If writing JSON fails
     */
    public JSONObject write(FileStorageService service) throws JSONException {
        JSONObject serviceJSON = new JSONObject(6);
        serviceJSON.put(FileStorageAccountConstants.ID, service.getId());
        serviceJSON.put(FileStorageAccountConstants.DISPLAY_NAME, service.getDisplayName());

        DynamicFormDescription formDescription = service.getFormDescription();
        if (null != formDescription) {
            JSONArray jFormDescription = new FormDescriptionWriter().write(formDescription);
            if (jFormDescription.length() > 0) {
                serviceJSON.put(FileStorageAccountConstants.CONFIGURATION, jFormDescription);
            }
        }
        return serviceJSON;
    }

}
