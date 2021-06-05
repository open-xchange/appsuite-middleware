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

package com.openexchange.ajax.chronos;

import static com.openexchange.java.Autoboxing.I;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.java.Strings;

/**
 * {@link AccountConfiguration}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class AccountConfiguration {

    private final Map<Integer, JSONObject> folderConfiguration;

    /**
     * Initialises a new {@link AccountConfiguration}.
     */
    public AccountConfiguration() {
        super();
        folderConfiguration = new HashMap<>();
    }

    public void addFolderConfiguration(int itemId) throws JSONException {
        addFolderConfiguration(itemId, null, -1);
    }

    public void addFolderConfiguration(int itemId, String locale, int refreshInterval) throws JSONException {
        folderConfiguration.put(I(itemId), createFolder(itemId, locale, refreshInterval));
    }

    public void removeFolderConfiguration(int itemId) {
        folderConfiguration.remove(I(itemId));
    }
    
    public void renameFolder(int itemId, String name) throws JSONException {
        JSONObject jsonObject = folderConfiguration.get(I(itemId));
        if (jsonObject == null) {
            return;
        }
        jsonObject.put("name", name);
    }

    public JSONObject getConfiguration() throws JSONException {
        JSONArray folders = new JSONArray();
        for (Integer key : folderConfiguration.keySet()) {
            folders.put(folderConfiguration.get(key));
        }
        JSONObject foldersConfiguration = new JSONObject();
        foldersConfiguration.put("folders", folders);

        JSONObject configuration = new JSONObject();
        configuration.put("configuration", foldersConfiguration);

        return configuration;
    }

    /**
     * Creates a calendar folder configuration object with the specified itemId, optional locale and
     * optional refresh interval.
     * 
     * @param itemId The item identifier
     * @param locale The optional locale
     * @param refreshInterval The optional refresh interval
     * @return The folder configuration
     * @throws JSONException if a JSON error is occurred
     */
    private JSONObject createFolder(int itemId, String locale, int refreshInterval) throws JSONException {
        JSONObject folder = new JSONObject();
        folder.put("itemId", itemId);
        if (refreshInterval > 0) {
            folder.put("refreshInterval", refreshInterval);
        }
        if (Strings.isNotEmpty(locale)) {
            folder.put("locale", locale);
        }
        return folder;
    }
}
