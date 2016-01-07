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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.file.storage.json;

import java.util.HashSet;
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
     * Initializes a new {@link FileStorageAccountWriter}.
     */
    public FileStorageAccountWriter() {
        super();
    }

    /**
     * Writes given account into its JSON representation.
     *
     * @param account The account
     * @param rootFolder The accounts root folder
     * @return The resulting JSON
     * @throws JSONException If writing JSON fails
     */
    public JSONObject write(FileStorageAccount account, FileStorageFolder rootFolder, Set<String> capabilities) throws JSONException {
        JSONObject accountJSON = new JSONObject(7);
        accountJSON.put(FileStorageAccountConstants.ID, account.getId());
        final FileStorageService fsService = account.getFileStorageService();
        accountJSON.put(FileStorageAccountConstants.QUALIFIED_ID, FileStorageAccounts.getQualifiedID(account));
        accountJSON.put(FileStorageAccountConstants.DISPLAY_NAME, account.getDisplayName());
        accountJSON.put(FileStorageAccountConstants.FILE_STORAGE_SERVICE, fsService.getId());
        accountJSON.put(FileStorageAccountConstants.ROOT_FOLDER, new FolderID(fsService.getId(), account.getId(), rootFolder.getId()).toUniqueID());
        accountJSON.put(FileStorageAccountConstants.IS_DEFAULT_ACCOUNT, FileStorageAccounts.isDefaultAccount(account));

        DynamicFormDescription formDescription = fsService.getFormDescription();
        if (null != formDescription && null != account.getConfiguration()) {
            JSONObject configJSON = FormContentWriter.write(formDescription, account.getConfiguration(), null);
            accountJSON.put(FileStorageAccountConstants.CONFIGURATION, configJSON);
        }

        //add capabilities
        if(capabilities==null)
        {
            capabilities=new HashSet<String>(0);
        }
        accountJSON.put("capabilities", capabilities);
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
