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
 *    trademarks of the OX Software GmbH. group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.file.storage;

import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;

/**
 * {@link FileStorageAccountMetaDataUtil} provides utility methods for handling account meta data
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class FileStorageAccountMetaDataUtil {

    //-----------------------------------------------------------------------
    // "lastError" - Error handling
    public static final String JSON_FIELD_LAST_ERROR = "lastError";
    public static final String JSON_FIELD_EXCEPTION = "exception";
    public static final String JSON_FIELD_TIMESTAMP = "timestamp";

    //-----------------------------------------------------------------------
    // "lastKnownFolders" - The last known root folders for an account
    public static final String JSON_ARRAY_LAST_KNOWN_FOLDERS = "lastKnownFolders";
    public static final String JSON_FIELD_FOLDER_PARENT_ID = "parentId";
    public static final String JSON_FIELD_FOLDER_ID = "folderId";
    public static final String JSON_FIELD_FOLDER_NAME = "folderName";

    /**
     * Gets the JSON meta data as from the given account
     *
     * @param account The account to get the meta data for.
     * @return The JSON meta data for the given account
     */
    public static JSONObject getAccountMetaData(FileStorageAccount account) {
        return account.getMetadata();
    }

    /**
     * Gets the current, latest known, error for the given account.
     *
     * @param account The account to get the latest known error for
     * @return The error as JSON object, or null if no last error is present.
     */
    public static JSONObject getAccountError(FileStorageAccount account) {
        JSONObject metadata = account.getMetadata();
        return metadata.optJSONObject(JSON_FIELD_LAST_ERROR);
    }

    /**
     * Gets the last known folders for the given account as an json array.
     *
     * @param account The account to get the folders for
     * @return the folders as JSONArray, or null if no known folders are present.
     */
    public static JSONArray getLastKnownFolders(FileStorageAccount account) {
        JSONObject metadata = account.getMetadata();
        return metadata.optJSONArray(JSON_ARRAY_LAST_KNOWN_FOLDERS);
    }

    /**
     * Sets the last known folders for the given account
     *
     * @param account The account to set the last known folders for
     * @param lastKnownFolders The folders to set as as list of JSONObjects
     * @return <code>true</code> if the account's metadata has been modified, <code>false</code>, otherwise
     */
    public static boolean setLastKnownFolders(FileStorageAccount account, List<JSONObject> lastKnownFolders) {
        return setLastKnownFolders(account, new JSONArray(lastKnownFolders));
    }

    /**
     * Sets the last known folders for the given account
     *
     * @param account The account to set the last known folders for
     * @param lastKnownFolders The folders to set as JSON array
     * @return <code>true</code> if the account's metadata has been modified, <code>false</code>, otherwise
     */
    public static boolean setLastKnownFolders(FileStorageAccount account, JSONArray lastKnownFolders) {
        JSONObject metadata = account.getMetadata();
        if (false == Objects.equals(lastKnownFolders, metadata.opt(JSON_ARRAY_LAST_KNOWN_FOLDERS))) {
            metadata.putSafe(JSON_ARRAY_LAST_KNOWN_FOLDERS, lastKnownFolders);
            return true;
        }
        return false;
    }

    /**
     * Copies meta-data from one account to another, in memory one.
     *
     * @param from The account to get the meta data from
     * @param to The account to apply the meta data on
     * @throws OXException
     */
    public static void copy(FileStorageAccount from, FileStorageAccount to) throws OXException {
        try {
            JSONObject jsonFrom = from.getMetadata();
            JSONObject jsonTo = to.getMetadata();
            jsonTo.reset();
            for (Entry<String, Object> entry : jsonFrom.entrySet()) {
                jsonTo.put(entry.getKey(), entry.getValue());
            }
        } catch (JSONException e) {
            throw FileStorageExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }
}
