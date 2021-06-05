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

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.datatypes.genericonf.json.FormContentParser;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.generic.DefaultFileStorageAccount;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;

/**
 * {@link FileStorageAccountParser} - Parses the JSON representation of a messaging account according to its messaging services dynamic
 * form.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FileStorageAccountParser {

    private final FileStorageServiceRegistry registry;

    /**
     * Initializes a new {@link FileStorageAccountParser}.
     *
     * @param serviceRegistry The service registry
     */
    public FileStorageAccountParser(final FileStorageServiceRegistry serviceRegistry) {
        super();
        registry = serviceRegistry;
    }

    /**
     * Parses specified account's JSON representation to a {@code FileStorageAccount}.
     *
     * @param accountJSON
     * @return
     * @throws OXException
     * @throws JSONException
     */
    public FileStorageAccount parse(final JSONObject accountJSON) throws OXException, JSONException {
        final DefaultFileStorageAccount account = new DefaultFileStorageAccount();

        account.setId(accountJSON.optString(FileStorageAccountConstants.ID));
        if (accountJSON.has(FileStorageAccountConstants.DISPLAY_NAME)) {
            account.setDisplayName(accountJSON.optString(FileStorageAccountConstants.DISPLAY_NAME));
        }
        final FileStorageService fsService =
            registry.getFileStorageService(accountJSON.getString(FileStorageAccountConstants.FILE_STORAGE_SERVICE));
        account.setFileStorageService(fsService);
        if (accountJSON.has(FileStorageAccountConstants.CONFIGURATION)) {
            account.setConfiguration(FormContentParser.parse(
                accountJSON.getJSONObject(FileStorageAccountConstants.CONFIGURATION),
                fsService.getFormDescription()));
        }

        return account;
    }

}
