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

package com.openexchange.ajax.chronos;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.ajax.chronos.manager.CalendarFolderManager;
import com.openexchange.ajax.chronos.manager.EventManager;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.configuration.asset.AssetManager;
import com.openexchange.exception.OXException;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.CommonResponse;
import com.openexchange.testing.httpclient.models.DeleteEventBody;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.FolderPermission;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;
import com.openexchange.testing.httpclient.models.FoldersVisibilityResponse;
import com.openexchange.testing.httpclient.models.NewFolderBody;
import com.openexchange.testing.httpclient.models.NewFolderBodyFolder;
import com.openexchange.testing.httpclient.models.UpdateEventBody;
import com.openexchange.testing.httpclient.modules.ChronosApi;
import com.openexchange.testing.httpclient.modules.FoldersApi;

/**
 * {@link AbstractChronosTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public class AbstractChronosTest extends AbstractEnhancedApiClientSession {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractChronosTest.class);

    private Set<EventId> eventIds;
    private Set<String> folderToDelete;
    private long lastTimeStamp;

    private static final String CONTACT_FOLDER = "Contacts";
    private static final String CONTACT_MODULE = "contacts";
    private static final String BIRTHDAY_FOLDER = "Birthdays";
    private static final String EVENT_MODULE = "event";
    private static final String CALENDAR_MODULE = "calendar";

    protected UserApi defaultUserApi;
    protected ChronosApi chronosApi;
    protected FoldersApi foldersApi;
    protected String defaultFolderId;

    protected EventManager eventManager;
    protected AssetManager assetManager;
    protected CalendarFolderManager folderManager;

    protected String folderId;

    /**
     * Initializes a new {@link AbstractChronosTest}.
     */
    public AbstractChronosTest() {
        super();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        LOG.info("Setup for test ...");
        ApiClient client = getApiClient();
        rememberClient(client);
        EnhancedApiClient enhancedClient = getEnhancedApiClient();
        rememberClient(enhancedClient);
        defaultUserApi = new UserApi(client, enhancedClient, testUser, false);
        chronosApi = defaultUserApi.getChronosApi();
        foldersApi = defaultUserApi.getFoldersApi();
        defaultFolderId = getDefaultFolder();
        assetManager = new AssetManager();
        eventManager = new EventManager(defaultUserApi, defaultFolderId);
        folderManager = new CalendarFolderManager(defaultUserApi, foldersApi);
        folderId = createAndRememberNewFolder(defaultUserApi, defaultUserApi.getSession(), getDefaultFolder(), defaultUserApi.getCalUser().intValue());
    }

    @Override
    public void tearDown() throws Exception {
        LOG.info("Teardown...");
        Exception exception = null;
        try {
            if (eventIds != null) {
                DeleteEventBody body = new DeleteEventBody();
                body.setEvents(new ArrayList<>(eventIds));
                defaultUserApi.getChronosApi().deleteEvent(defaultUserApi.getSession(), Long.valueOf(Long.MAX_VALUE), body, null, null, Boolean.FALSE, Boolean.FALSE, null, null, null);
            }
            // Clean-up event manager
            eventManager.cleanUp();
            folderManager.cleanUp();

            try {
                if (folderToDelete != null) {
                    defaultUserApi.getFoldersApi().deleteFolders(defaultUserApi.getSession(), new ArrayList<>(folderToDelete), "0", Long.valueOf(System.currentTimeMillis()), "event", Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, null);
                }
            } catch (Exception e) {
                exception = e;
            }
        } finally {
            super.tearDown();
        }

        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Keeps track of the specified {@link EventId}
     *
     * @param eventId The {@link EventId}
     */
    protected void rememberEventId(EventId eventId) {
        if (eventIds == null) {
            eventIds = new HashSet<>();
        }
        eventIds.add(eventId);
    }

    /**
     * Keeps track of the specified folder
     *
     * @param folder The folder
     */
    protected void rememberFolder(String folder) {
        if (folderToDelete == null) {
            folderToDelete = new HashSet<>();
        }
        folderToDelete.add(folder);
    }

    /**
     * Creates a new folder and remembers it.
     *
     * @param api The {@link UserApi}
     * @param session The user's session
     * @param parent The parent folder
     * @param entity The user id
     * @return The result of the operation
     * @throws ApiException if an API error is occurred
     */
    protected String createAndRememberNewFolder(UserApi api, String session, String parent, int entity) throws ApiException {
        FolderPermission perm = new FolderPermission();
        perm.setEntity(I(entity));
        perm.setGroup(Boolean.FALSE);
        perm.setBits(I(403710016));

        List<FolderPermission> permissions = new ArrayList<>();
        permissions.add(perm);
        return createAndRememberNewFolder(api, session, parent, entity, permissions);
    }

    /**
     * Creates a new folder and remembers it.
     *
     * @param api The {@link UserApi}
     * @param session The user's session
     * @param parent The parent folder
     * @param entity The user id
     * @param permissions The permissions to set
     * @return The result of the operation
     * @throws ApiException if an API error is occurred
     */
    protected String createAndRememberNewFolder(UserApi api, String session, String parent, int entity, List<FolderPermission> permissions) throws ApiException {

        NewFolderBodyFolder folderData = new NewFolderBodyFolder();
        folderData.setModule(EVENT_MODULE);
        folderData.setSubscribed(Boolean.TRUE);
        folderData.setTitle("chronos_test_" + new UID().toString());
        folderData.setPermissions(permissions);

        NewFolderBody body = new NewFolderBody();
        body.setFolder(folderData);

        FolderUpdateResponse createFolder = api.getFoldersApi().createFolder(parent, session, body, "0", CALENDAR_MODULE, null, null);
        checkResponse(createFolder.getError(), createFolder.getErrorDesc(), createFolder.getData());

        String result = createFolder.getData();
        rememberFolder(result);

        return result;
    }

    /**
     * Retrieves the default calendar folder of the current user
     *
     * @return The default calendar folder of the current user
     * @throws Exception if the default calendar folder cannot be found
     */
    protected String getDefaultFolder() throws Exception {
        return getDefaultFolder(defaultUserApi.getSession(), defaultUserApi.getFoldersApi());
    }

    /**
     * Retrieves the default calendar folder of the user with the specified session
     *
     * @param session The session of the user
     * @param client The {@link ApiClient}
     * @return The default calendar folder of the user
     * @throws Exception if the default calendar folder cannot be found
     */
    protected String getDefaultFolder(String session, ApiClient client) throws Exception {
        return getDefaultFolder(session, new FoldersApi(client));
    }

    /**
     * @return String The identifier of the birthday calendar folder
     * @throws Exception if the birthday calendar folder cannot be found
     */
    protected String getBirthdayCalendarFolder() throws Exception {
        return getPrivateFolder(defaultUserApi.getFoldersApi(), defaultUserApi.getSession(), EVENT_MODULE, BIRTHDAY_FOLDER);
    }

    /**
     * @param session The session of the user
     * @param client The {@link ApiClient}
     * @return String The identifier of the birthday calendar folder
     * @throws Exception if the birthday calendar folder cannot be found
     */
    protected String getBirthdayCalendarFolder(String session, ApiClient client) throws Exception {
        return getPrivateFolder(new FoldersApi(client), session, EVENT_MODULE, BIRTHDAY_FOLDER);
    }

    /**
     * @return String The identifier of the default contact folder
     * @throws Exception if the default contact folder cannot be found
     */
    protected String getDefaultContactFolder() throws Exception {
        return getPrivateFolder(defaultUserApi.getFoldersApi(), defaultUserApi.getSession(), CONTACT_MODULE, CONTACT_FOLDER);
    }

    /**
     * @param session The session of the user
     * @param client client The {@link ApiClient}
     * @return String The identifier of the default contact folder
     * @throws Exception if the default contact folder cannot be found
     */
    protected String getDefaultContactFolder(String session, ApiClient client) throws Exception {
        return getPrivateFolder(new FoldersApi(client), session, "contacts", "Contacts");
    }

    /**
     * Retrieves the default calendar folder of the user with the specified session
     *
     * @param session The session of the user
     * @param foldersApi The {@link FoldersApi}
     * @return The default calendar folder of the user
     * @throws Exception if the default calendar folder cannot be found
     */
    protected String getDefaultFolder(String session, FoldersApi foldersApi) throws Exception {
        ArrayList<ArrayList<?>> privateList = getPrivateFolderList(foldersApi, session, "event", "1,308", "0");
        if (privateList.size() == 1) {
            return (String) privateList.get(0).get(0);
        }
        for (ArrayList<?> folder : privateList) {
            if (folder.get(1) != null && ((Boolean) folder.get(1)).booleanValue()) {
                return (String) folder.get(0);
            }
        }
        throw new Exception("Unable to find default calendar folder!");
    }

    /**
     * @param session The session of the user
     * @param module The folder module
     * @param folder The name of the folder
     * @return folderId The folderId as string
     * @throws Exception if the folder cannot be found
     */
    private String getPrivateFolder(FoldersApi foldersApi, String session, String module, String folder) throws Exception {
        ArrayList<ArrayList<?>> privateList = getPrivateFolderList(foldersApi, session, module, "1,300,308", "1");
        for (ArrayList<?> folderName : privateList) {
            if (folderName.get(1).equals(folder)) {
                return folderName.get(0).toString();
            }
        }
        throw new Exception("Unable to find default " + module + " folder!");
    }

    /**
     * @param api The {@link FoldersApi} to use
     * @param session The session of the user
     * @param module The folder module
     * @param columns The columns identifier
     * @param tree The folder tree identifier
     * @return List of available folders
     * @throws Exception if the api call fails
     */
    @SuppressWarnings({ "unchecked" })
    protected ArrayList<ArrayList<?>> getPrivateFolderList(FoldersApi foldersApi, String session, String module, String columns, String tree) throws Exception {
        FoldersVisibilityResponse visibleFolders = foldersApi.getVisibleFolders(session, module, columns, tree, null);
        if (visibleFolders.getError() != null) {
            throw new OXException(new Exception(visibleFolders.getErrorDesc()));
        }
        Object privateFolders = visibleFolders.getData().getPrivate();
        return (ArrayList<ArrayList<?>>) privateFolders;
    }

    /**
     * Sets the last timestamp
     *
     * @param timestamp the last timestamp to set
     */
    protected void setLastTimestamp(long timestamp) {
        this.lastTimeStamp = timestamp;
    }

    /**
     * Gets the last timestamp
     *
     * @return the last timestamp
     */
    protected long getLastTimestamp() {
        return lastTimeStamp;
    }

    /**
     * Changes the timezone of the default user to the given value
     *
     * @param tz The new timezone
     * @throws ApiException
     */
    protected void changeTimezone(TimeZone tz) throws ApiException {
        String body = "{timezone: \"" + tz.getID() + "\"}";
        CommonResponse updateJSlob = defaultUserApi.getJslob().updateJSlob(defaultUserApi.getSession(), body, "io.ox/core", null);
        assertNull(updateJSlob.getErrorDesc(), updateJSlob.getError());
    }

    /**
     * Checks if a response doesn't contain any errors
     *
     * @param error The error element of the response
     * @param errorDesc The error description element of the response
     * @param data The data element of the response
     * @return The data
     */
    @Override
    protected <T> T checkResponse(String error, String errorDesc, T data) {
        assertNull(errorDesc, error);
        assertNotNull(data);
        return data;
    }

    /**
     * Generates an {@link UpdateBody}.
     *
     * @param eventData The {@link EventData} to update
     * @return An {@link UpdateBody}.
     */
    protected UpdateEventBody getUpdateBody(EventData eventData) {
    	UpdateEventBody body = new UpdateEventBody();
        body.setEvent(eventData);
        return body;
    }

    protected static List<EventData> getEventsByUid(List<EventData> events, String uid) {
        List<EventData> matchingEvents = new ArrayList<EventData>();
        if (null != events) {
            for (EventData event : events) {
                if (uid.equals(event.getUid())) {
                    matchingEvents.add(event);
                }
            }
        }
        matchingEvents.sort(new Comparator<EventData>() {

            @Override
            public int compare(EventData event1, EventData event2) {
                String recurrenceId1 = event1.getRecurrenceId();
                String recurrenceId2 = event2.getRecurrenceId();
                if (null == recurrenceId1) {
                    return null == recurrenceId2 ? 0 : -1;
                }
                if (null == recurrenceId2) {
                    return 1;
                }
                long dateTime1 = CalendarUtils.decode(recurrenceId1).getTimestamp();
                long dateTime2 = CalendarUtils.decode(recurrenceId2).getTimestamp();
                if(dateTime1 == dateTime2 ) {
                    return 0;
                }
                return dateTime1 < dateTime2 ? -1 : 1;
            }
        });
        return matchingEvents;
    }

    /**
     * Returns the id of the calendar user of the default user api
     *
     * @return The id of the calendar user
     */
    protected int getCalendaruser() {
        return i(defaultUserApi.getCalUser());
    }
}
