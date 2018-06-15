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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.ajax.chronos.manager;

import static org.junit.Assert.assertNotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONObject;
import com.openexchange.ajax.chronos.UserApi;
import com.openexchange.ajax.chronos.factory.CalendarFolderFactory;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.FolderBody;
import com.openexchange.testing.httpclient.models.FolderData;
import com.openexchange.testing.httpclient.models.FolderResponse;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;
import com.openexchange.testing.httpclient.models.NewFolderBody;
import com.openexchange.testing.httpclient.modules.FoldersApi;

/**
 * {@link CalendarFolderManager}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CalendarFolderManager extends AbstractManager {

    public static final String DEFAULT_ACCOUNT_ID = "0";
    public static final String DEFAULT_FOLDER_ID = "1";
    public static final String TREE_ID = "0";
    public static final String MODULE = "calendar";
    public static final String DEFAULT_ACCOUNT_PROVIDER_ID = "chronos";
    public static final String ICAL_ACCOUNT_PROVIDER_ID = "ical";

    public static final String CALENDAR_MODULE = "calendar";
    public static final String EVENT_MODULE = "event";

    private final List<String> folderIds;

    private final FoldersApi foldersApi;
    private final UserApi userApi;

    /**
     * Initialises a new {@link CalendarFolderManager}.
     */
    public CalendarFolderManager(UserApi userApi, FoldersApi foldersApi) {
        super();
        folderIds = new ArrayList<>(4);
        this.userApi = userApi;
        this.foldersApi = foldersApi;
    }

    /**
     * Clean up. Deletes all folders created via this manager.
     */
    public void cleanUp() {
        try {
            foldersApi.deleteFolders(userApi.getSession(), folderIds, TREE_ID, null, CALENDAR_MODULE, true, false, false);
        } catch (ApiException e) {
            System.err.println("Could not clean up the calendar folders for user " + userApi.getCalUser() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     *
     * @param module
     * @param providerId
     * @param title
     * @param config
     * @param extendedProperties
     * @return
     * @throws ApiException
     * @throws ChronosApiException
     */
    public String createFolder(String module, String providerId, String title, JSONObject config, JSONObject extendedProperties) throws ApiException, ChronosApiException {
        return createFolder(module, providerId, title, config, extendedProperties, false);
    }

    /**
     * Creates a folder for the specified module and provider
     *
     * @param module The module
     * @param providerId The provider identifier
     * @param config The configuration
     * @param extendedProperties The extended properties of the folder
     * @return The folder identifier
     * @throws ApiException If an API error is occurred
     * @throws ChronosApiException
     */
    public String createFolder(String module, String providerId, String title, JSONObject config, JSONObject extendedProperties, boolean expectedException) throws ApiException, ChronosApiException {
        NewFolderBody body = CalendarFolderFactory.createFolderBody(module, providerId, title, true, config, extendedProperties);
        FolderUpdateResponse response = foldersApi.createFolder(DEFAULT_FOLDER_ID, userApi.getSession(), body, TREE_ID, module);
        if (expectedException) {
            assertNotNull("An error was expected", response.getError());
            throw new ChronosApiException(response.getCode(), response.getError());
        }
        return handleCreation(response).getData();
    }

    public String createFolder(NewFolderBody body) throws ApiException {
        FolderUpdateResponse response = foldersApi.createFolder(DEFAULT_FOLDER_ID, userApi.getSession(), body, CalendarFolderManager.TREE_ID, CalendarFolderManager.MODULE);
        return handleCreation(response).getData();
    }

    /**
     * Retrieves the folder with the specified identifier
     *
     * @param folderId The folder identifier
     * @return the {@link FolderData}
     * @throws ApiException if an API error is occurred
     * @throws ChronosApiException
     */
    public FolderData getFolder(String folderId) throws ApiException, ChronosApiException {
        return getFolder(folderId, false);
    }

    /**
     * Retrieves the folder with the specified identifier
     *
     * @param folderId The folder identifier
     * @return the {@link FolderData}
     * @throws ApiException if an API error is occurred
     * @throws ChronosApiException
     */
    public FolderData getFolder(String folderId, boolean expectedException) throws ApiException, ChronosApiException {
        FolderResponse response = foldersApi.getFolder(userApi.getSession(), folderId, TREE_ID, CALENDAR_MODULE);
        if (expectedException) {
            assertNotNull("An error was expected", response.getError());
            throw new ChronosApiException(response.getCode(), response.getError());
        }
        return checkResponse(response.getError(), response.getErrorDesc(), response.getCategories(), response).getData();
    }

    /**
     * Updates the folder with the specified identifier
     *
     * @param defaultFolderId The folder identifier
     * @return The {@link FolderUpdateResponse}
     * @throws ApiException if an API error is occurred
     * @throws ChronosApiException
     */
    public FolderUpdateResponse updateFolder(FolderData folderData) throws ApiException, ChronosApiException {
        return updateFolder(folderData, false);
    }

    /**
     * Updates the folder with the specified identifier
     *
     * @param defaultFolderId The folder identifier
     * @return The {@link FolderUpdateResponse}
     * @throws ApiException if an API error is occurred
     * @throws ChronosApiException
     */
    public FolderUpdateResponse updateFolder(FolderData folderData, boolean expectedException) throws ApiException, ChronosApiException {
        FolderBody body = new FolderBody();
        body.setFolder(folderData);

        FolderUpdateResponse response = foldersApi.updateFolder(userApi.getSession(), folderData.getId(), body, Boolean.FALSE, folderData.getLastModifiedUtc(), TREE_ID, CALENDAR_MODULE, Boolean.TRUE);
        if (expectedException) {
            assertNotNull("An error was expected", response.getError());
            throw new ChronosApiException(response.getCode(), response.getError());
        }
        return checkResponse(response.getError(), response.getErrorDesc(), response.getCategories(), response);
    }

    /**
     * Deletes the folder with the specified identifier
     *
     * @param folderId The folder identifier
     * @throws ApiException if an API error is occurred
     */
    public void deleteFolder(String folderId) throws ApiException {
        this.deleteFolders(Collections.singletonList(folderId));
    }

    /**
     * Deletes the folders with the specified identifier
     *
     * @param defaultFolderId The folder identifiers
     * @throws ApiException if an API error is occurred
     */
    public void deleteFolders(List<String> folders) throws ApiException {
        foldersApi.deleteFolders(userApi.getSession(), folders, TREE_ID, null, CALENDAR_MODULE, true, false, false);
        for (String folder : folders) {
            folderIds.remove(folder);
        }
    }

    /**
     * Handles the creation response and remembers the folder id
     *
     * @param response The {@link FolderUpdateResponse}
     * @return The {@link FolderUpdateResponse}
     * @throws ApiException if an API error is occurred
     */
    private FolderUpdateResponse handleCreation(FolderUpdateResponse response) {
        FolderUpdateResponse data = checkResponse(response.getError(), response.getErrorDesc(), response.getCategories(), response);
        folderIds.add(data.getData());
        return data;
    }
}
