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

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.json.FormContentWriter;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageService;

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
     * @return The resulting JSON
     * @throws JSONException If writing JSON fails
     */
    public JSONObject write(final FileStorageAccount account) throws JSONException {
        final JSONObject accountJSON = new JSONObject();
        accountJSON.put(FileStorageAccountConstants.ID, account.getId());
        accountJSON.put(FileStorageAccountConstants.DISPLAY_NAME, account.getDisplayName());
        final FileStorageService fsService = account.getFileStorageService();
        accountJSON.put(FileStorageAccountConstants.FILE_STORAGE_SERVICE, fsService.getId());
        final DynamicFormDescription formDescription = fsService.getFormDescription();
        if (null != formDescription && null != account.getConfiguration()) {
            final JSONObject configJSON = new FormContentWriter().write(formDescription, account.getConfiguration(), null);
            accountJSON.put(FileStorageAccountConstants.CONFIGURATION, configJSON);
        }
        return accountJSON;
    }

}
