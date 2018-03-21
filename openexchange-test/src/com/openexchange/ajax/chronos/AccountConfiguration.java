/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2017-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.ajax.chronos;

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
        folderConfiguration.put(itemId, createFolder(itemId, locale, refreshInterval));
    }

    public void removeFolderConfiguration(int itemId) {
        folderConfiguration.remove(itemId);
    }
    
    public void renameFolder(int itemId, String name) throws JSONException {
        JSONObject jsonObject = folderConfiguration.get(itemId);
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
     * Creates a calendar folder configuration with the specified item id
     * 
     * @param itemId The item id
     * @return The folder configuration
     * @throws JSONException if a JSON error is occurred
     */
    private JSONObject createFolder(int itemId) throws JSONException {
        return createFolder(itemId, null, -1);
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
        if (!Strings.isEmpty(locale)) {
            folder.put("locale", locale);
        }
        return folder;
    }
}
