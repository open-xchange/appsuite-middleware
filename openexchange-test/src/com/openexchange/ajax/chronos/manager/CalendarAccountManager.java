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

package com.openexchange.ajax.chronos.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONObject;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openexchange.ajax.chronos.UserApi;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.java.Strings;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.CalendarAccountData;
import com.openexchange.testing.httpclient.models.CalendarAccountResponse;
import com.openexchange.testing.httpclient.models.FolderBody;
import com.openexchange.testing.httpclient.models.FolderData;
import com.openexchange.testing.httpclient.models.FolderDataComOpenexchangeCalendarConfig;
import com.openexchange.testing.httpclient.models.FolderPermission;
import com.openexchange.testing.httpclient.models.FolderResponse;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;
import com.openexchange.testing.httpclient.models.FoldersResponse;
import com.openexchange.testing.httpclient.models.NewFolderBody;
import com.openexchange.testing.httpclient.models.NewFolderBodyFolder;
import com.openexchange.testing.httpclient.modules.FoldersApi;

/**
 * {@link CalendarAccountManager}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public class CalendarAccountManager extends AbstractManager {

    private static final String COLUMNS = Strings.concat(",", Arrays.stream(Metadata.columns(Metadata.HTTPAPI_VALUES_ARRAY)).mapToObj(String::valueOf).toArray(String[]::new));

    public final String DEFAULT_ACCOUNT_ID = "0";

    public final String DEFAULT_FOLDER_ID = "1";

    public final String TREE_ID = "0";

    public final String DEFAULT_ACCOUNT_PROVIDER_ID = "chronos";

    private final String MODULE = "calendar";

    private final UserApi userApi;

    private List<String> calAccIds;

    private FoldersApi foldersApi;

    /**
     * Initializes a new {@link CalendarAccountManager}.
     */
    public CalendarAccountManager(UserApi userApi, FoldersApi foldersApi) {
        super();
        this.userApi = userApi;
        this.foldersApi = foldersApi;
        this.calAccIds = new ArrayList<>();
    }

    public void cleanUp() {
        try {
            foldersApi.deleteFolders(userApi.getSession(), calAccIds, TREE_ID, System.currentTimeMillis(), MODULE, true, false, false);
        } catch (ApiException e) {
            System.err.println("Could not clean up the calendar accounts for user " + userApi.getCalUser() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public FolderUpdateResponse createCalendarAccount(String providerId, String folderName, FolderDataComOpenexchangeCalendarConfig config) throws ApiException {
        NewFolderBody body = createBody(providerId, folderName, config);
        FolderUpdateResponse response = foldersApi.createFolder(DEFAULT_FOLDER_ID, userApi.getSession(), body, TREE_ID, MODULE);

        assertNull("Calendar account could not be created due an error.", response.getError());
        assertNotNull(response.getData());
        rememberCalendarAccountId(response.getData());
        return response;
    }

    private NewFolderBody createBody(String providerId, String folderId, FolderDataComOpenexchangeCalendarConfig config) {
        NewFolderBody body = new NewFolderBody();
        NewFolderBodyFolder folder = new NewFolderBodyFolder();
        folder.setComOpenexchangeCalendarProvider(providerId);
        folder.setComOpenexchangeCalendarConfig(config);
        folder.setTitle(folderId);
        folder.setFolderId(folderId);
        folder.setId(folderId);
        folder.setModule(MODULE);

        FolderPermission perm = new FolderPermission();
        perm.setEntity(userApi.getCalUser());
        perm.setGroup(Boolean.FALSE);
        perm.setBits(403710016);

        List<FolderPermission> permissions = new ArrayList<>();
        permissions.add(perm);

        folder.setPermissions(permissions);
        body.setFolder(folder);
        return body;
    }

    /**
     * Creates a calendar account with the specified configuration for the specified provider
     *
     * @param providerId The calendar provider identifier
     * @param configuration The configuration
     * @param expectedException flag to indicate that an exception is expected
     * @return The created {@link CalendarAccountData}
     * @throws ApiException if an API error is occurred
     * @throws ChronosApiException if a Chronos API error is occurred
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    public FolderUpdateResponse createCalendarAccount(String providerId, String folderName, JSONObject config, boolean expectedException) throws ApiException, ChronosApiException, JsonParseException, JsonMappingException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        FolderDataComOpenexchangeCalendarConfig calendarConfig = objectMapper.readValue(config.toString(), FolderDataComOpenexchangeCalendarConfig.class);

        NewFolderBody body = createBody(providerId, folderName, calendarConfig);
        FolderUpdateResponse response = foldersApi.createFolder(DEFAULT_FOLDER_ID, userApi.getSession(), body, TREE_ID, MODULE);

        if (expectedException) {
            assertNotNull("An error was expected", response.getError());
            throw new ChronosApiException(response.getCode(), response.getError());
        }
        return handleCreation(response);
    }

    public void deleteCalendarAccount(List<String> idsToDelete) throws ApiException {
        foldersApi.deleteFolders(userApi.getSession(), idsToDelete, TREE_ID, System.currentTimeMillis(), MODULE, true, false, false);
        for (String calendarAccountId : idsToDelete) {
            forgetCalendarAccountId(calendarAccountId);
        }
    }

    public FolderResponse loadCalendarAccount(String calAccId) throws ApiException {
        FolderResponse response = foldersApi.getFolder(userApi.getSession(), calAccId, TREE_ID, MODULE);
        assertNull(response.getError(), response.getError());
        assertNotNull(response.getData());
        assertEquals("The id of the calendar account is invalid!", calAccId, response.getData().getId());
        return response;
    }

    public FoldersResponse loadAllCalendarAccounts() throws ApiException {
        FoldersResponse response = foldersApi.getSubFolders(userApi.getSession(), DEFAULT_FOLDER_ID, COLUMNS, 1, TREE_ID, MODULE, false);
        assertNull(response.getError(), response.getError());
        assertNotNull(response.getData());
        return response;
    }

    /**
     * Helper method for {@link #updateCalendarAccount(CalendarAccountId, String)}
     *
     * @param accountId The account identifier
     * @param timestamp The latest known timestamp
     * @param configuration The optional configuration
     * @return The updated {@link CalendarAccountData}
     * @throws ApiException if an API error is occurred
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    public FolderUpdateResponse updateCalendarAccount(String providerId, String accountId, long timestamp, JSONObject configuration) throws ApiException, JsonParseException, JsonMappingException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        FolderDataComOpenexchangeCalendarConfig calendarConfig = objectMapper.readValue(configuration.toString(), FolderDataComOpenexchangeCalendarConfig.class);

        FolderBody body = new FolderBody();
        FolderData folderData = new FolderData();
        folderData.setAccountId(accountId);
        folderData.setComOpenexchangeCalendarConfig(calendarConfig);
        folderData.setComOpenexchangeCalendarProvider(providerId);
        body.setFolder(folderData);
        FolderUpdateResponse response = foldersApi.updateFolder(userApi.getSession(), accountId, timestamp, body, Boolean.FALSE, TREE_ID, MODULE, true);

        return checkResponse(response.getError(), response.getErrorDesc(), response);
    }

    public FolderUpdateResponse updateCalendarAccount(String providerId, String accountId, String configuration) throws ApiException, JsonParseException, JsonMappingException, IOException {
        forgetCalendarAccountId(accountId);

        ObjectMapper objectMapper = new ObjectMapper();
        FolderDataComOpenexchangeCalendarConfig calendarConfig = objectMapper.readValue(configuration.toString(), FolderDataComOpenexchangeCalendarConfig.class);
        FolderBody body = new FolderBody();
        FolderData folderData = new FolderData();
        folderData.setAccountId(accountId);
        folderData.setComOpenexchangeCalendarConfig(calendarConfig);
        folderData.setComOpenexchangeCalendarProvider(providerId);
        body.setFolder(folderData);

        FolderUpdateResponse response = foldersApi.updateFolder(userApi.getSession(), accountId, System.currentTimeMillis(), body, Boolean.FALSE, TREE_ID, MODULE, true);

        if (null != response.getData()) {
            rememberCalendarAccountId(response.getData());
        }
        return response;
    }

    public FolderUpdateResponse updateCalendarAccount(String providerId, String accountId, String folderId, FolderDataComOpenexchangeCalendarConfig configuration) throws ApiException {
        forgetCalendarAccountId(accountId);

        createBody(providerId, folderId, configuration);

        FolderBody body = new FolderBody();
        FolderData folderData = new FolderData();
        folderData.setAccountId(accountId);
        folderData.setComOpenexchangeCalendarConfig(configuration);
        folderData.setComOpenexchangeCalendarProvider(providerId);
        folderData.setTitle(folderId);
        folderData.setFolderId(DEFAULT_FOLDER_ID);
        folderData.setId(folderId);
        FolderPermission perm = new FolderPermission();
        perm.setEntity(userApi.getCalUser());
        perm.setGroup(Boolean.FALSE);
        perm.setBits(403710016);

        List<FolderPermission> permissions = new ArrayList<>();
        permissions.add(perm);

        folderData.setPermissions(permissions);
        body.setFolder(folderData);

        FolderUpdateResponse response = foldersApi.updateFolder(userApi.getSession(), DEFAULT_FOLDER_ID, System.currentTimeMillis(), body, Boolean.FALSE, TREE_ID, MODULE, true);

        if (null != response.getData()) {
            rememberCalendarAccountId(response.getData());
        }
        return response;
    }

    private void rememberCalendarAccountId(String calAccId) {
        if (calAccIds == null) {
            calAccIds = new ArrayList<>();
        }
        calAccIds.add(calAccId);
    }

    private void forgetCalendarAccountId(String calAccId) {
        if (calAccIds == null) {
            calAccIds = new ArrayList<>();
        }
        if (Strings.isNotEmpty(calAccId)) {
            calAccIds.remove(calAccId);
        }
    }

    public FolderDataComOpenexchangeCalendarConfig createCalendarAccountTestConfiguration(boolean updateConfig) {
        FolderDataComOpenexchangeCalendarConfig config = new FolderDataComOpenexchangeCalendarConfig();
        config.setEnabled(Boolean.TRUE);
        config.setColor(updateConfig ? "blue" : "red");
        return config;
    }

    /**
     * Handles the creation response and remembers the account id
     *
     * @param response The {@link CalendarAccountResponse}
     * @return The {@link CalendarAccountData}
     * @throws ApiException if an API error is occurred
     */
    private FolderUpdateResponse handleCreation(FolderUpdateResponse response) throws ApiException {
        FolderUpdateResponse data = checkResponse(response.getError(), response.getErrorDesc(), response);
        rememberCalendarAccountId(data.getData());
        return data;
    }
}
