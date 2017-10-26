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

package com.openexchange.chronos.provider.schedjoules;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Calendar;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.CalendarFolderProperty;
import com.openexchange.chronos.provider.DefaultCalendarFolder;
import com.openexchange.chronos.provider.DefaultCalendarPermission;
import com.openexchange.chronos.provider.caching.CachingCalendarAccess;
import com.openexchange.chronos.provider.caching.ExternalCalendarResult;
import com.openexchange.chronos.provider.schedjoules.exception.SchedJoulesProviderExceptionCodes;
import com.openexchange.chronos.schedjoules.api.SchedJoulesAPI;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.exception.OXException;
import com.openexchange.java.Enums;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 * {@link SchedJoulesCalendarAccess}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SchedJoulesCalendarAccess extends CachingCalendarAccess {

    private static final Logger LOG = LoggerFactory.getLogger(SchedJoulesCalendarAccess.class);

    /**
     * Default 'X-WR-CALNAME' and 'SUMMARY' contents of an iCal that is not accessible
     */
    private static final String NO_ACCESS = "You have no access to this calendar";

    /**
     * Defines the amount of time to wait before attempting another external request upon failure. Defaults in 60 minutes.
     */
    private static final int EXTERNAL_REQUEST_TIMEOUT = 60;

    /**
     * Initialises a new {@link SchedJoulesCalendarAccess}.
     *
     * @param session The groupware {@link Session}
     * @param account The {@link CalendarAccount}
     * @param parameters The optional {@link CalendarParameters}
     * @throws OXException If the context cannot be resolved
     */
    protected SchedJoulesCalendarAccess(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        super(session, account, parameters);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.provider.CalendarAccess#close()
     */
    @Override
    public void close() {
        // no-op
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.provider.CalendarAccess#getFolder(java.lang.String)
     */
    @Override
    public CalendarFolder getFolder(String folderId) throws OXException {
        return prepareFolder(folderId);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.provider.CalendarAccess#getVisibleFolders()
     */
    @Override
    public List<CalendarFolder> getVisibleFolders() throws OXException {
        JSONArray foldersArray = getAccount().getUserConfiguration().optJSONArray(SchedJoulesFields.FOLDERS);
        if (foldersArray == null || foldersArray.isEmpty()) {
            return Collections.emptyList();
        }
        List<CalendarFolder> folders = new ArrayList<>(foldersArray.length());
        for (int index = 0; index < foldersArray.length(); index++) {
            JSONObject folder = foldersArray.optJSONObject(index);
            if (!hasMetadata(folder)) {
                continue;
            }
            String name = getFolderName(folder);
            if (Strings.isEmpty(name)) {
                continue;
            }
            folders.add(prepareFolder(name));
        }
        return folders;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.provider.CalendarAccess#updateFolder(java.lang.String, com.openexchange.chronos.provider.CalendarFolder, long)
     */
    @Override
    public String updateFolder(String folderId, CalendarFolder folder, long clientTimestamp) throws OXException {
        try {
            ExtendedProperties extendedProperties = folder.getExtendedProperties();
            if (extendedProperties == null) {
                extendedProperties = new ExtendedProperties();
            }

            JSONObject folderJson = findFolderInInternalConfiguration(folderId);

            folderJson.put(SchedJoulesFields.COLOR, extendedProperties.get(CalendarFolderProperty.COLOR_LITERAL));
            folderJson.put(SchedJoulesFields.USED_FOR_SYNC, extendedProperties.get(CalendarFolderProperty.USED_FOR_SYNC_LITERAL));
            folderJson.put(SchedJoulesFields.SCHEDULE_TRANSP, extendedProperties.get(CalendarFolderProperty.SCHEDULE_TRANSP_LITERAL));

            updateInternalConfigurationData(getAccount().getInternalConfiguration());

            return folderId;
        } catch (JSONException e) {
            throw SchedJoulesProviderExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.provider.CalendarAccess#getChangeExceptions(java.lang.String, java.lang.String)
     */
    @Override
    public List<Event> getChangeExceptions(String folderId, String seriesId) throws OXException {
        return Collections.emptyList();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.provider.caching.CachingCalendarAccess#getRefreshInterval()
     */
    @Override
    protected long getRefreshInterval(String folderId) {
        JSONObject internalConfig = getAccount().getInternalConfiguration();
        if (internalConfig == null || internalConfig.isEmpty()) {
            return 0;
        }
        JSONArray foldersArray = internalConfig.optJSONArray(SchedJoulesFields.FOLDERS);
        try {
            JSONObject folder = findFolder(folderId, foldersArray);
            return folder.optInt(SchedJoulesFields.REFRESH_INTERVAL, 0);
        } catch (OXException e) {
            LOG.debug("{}", e.getMessage(), e);
            return 0;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.provider.caching.CachingCalendarAccess#getExternalRequestTimeout()
     */
    @Override
    public long getExternalRequestTimeout() {
        return TimeUnit.MINUTES.toMinutes(EXTERNAL_REQUEST_TIMEOUT);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.provider.caching.CachingCalendarAccess#getEvents(java.lang.String)
     */
    @Override
    public ExternalCalendarResult getAllEvents(String folderId) throws OXException {
        try {
            URL url = getFeedURL(folderId);
            SchedJoulesAPI api = SchedJoulesAPI.getInstance();
            Calendar calendar = api.calendar().getCalendar(url);
            if (NO_ACCESS.equals(calendar.getName())) {
                throw SchedJoulesProviderExceptionCodes.NO_ACCESS.create(folderId);
            }

            return new ExternalCalendarResult(calendar.getEvents());
        } catch (JSONException e) {
            throw SchedJoulesProviderExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
        } catch (MalformedURLException e) {
            throw SchedJoulesProviderExceptionCodes.INVALID_URL.create(folderId, e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.provider.caching.CachingCalendarAccess#handleExceptions(java.lang.String, com.openexchange.exception.OXException)
     */
    @Override
    public void handleExceptions(String calendarFolderId, OXException e) {
        // no-op
    }

    ///////////////////////////////////// HELPERS /////////////////////////////////

    /**
     * Returns the feed URL for the specified folder
     *
     * @param folderId The folder identifier
     * @return The feed URL from which to fetch the events
     * @throws MalformedURLException If the URL is invalid
     * @throws JSONException if a JSON error occurs
     * @throws OXException if the feed URL cannot be returned due to either malformed user key,
     *             or non-existing configuration or folder
     */
    private URL getFeedURL(String folderId) throws MalformedURLException, JSONException, OXException {
        JSONObject folder = findFolderInInternalConfiguration(folderId);
        URL url = new URL(folder.getString(SchedJoulesFields.URL));
        UUID userKey = getUserKey();
        return new URL(generateURL(url, userKey));
    }

    /**
     * Appends the specified user key to the specified URL
     * 
     * @param url The URL
     * @param userKey The user key to append
     * @return The generated URL
     */
    private String generateURL(URL url, UUID userKey) {
        StringBuilder sb = new StringBuilder();
        sb.append(url.toString()).append("&u").append(userKey.toString());
        return sb.toString();
    }

    /**
     * Retrieves the user's key
     *
     * @param internalConfig The internal configuration
     * @return The user key
     * @throws OXException if the userKey is malformed or missing from the configuration
     */
    private UUID getUserKey() throws OXException {
        String key = getAccount().getInternalConfiguration().optString(SchedJoulesFields.USER_KEY);
        if (Strings.isEmpty(key)) {
            throw SchedJoulesProviderExceptionCodes.MISSING_USER_KEY.create(getAccount().getAccountId(), getSession().getUserId(), getSession().getContextId());
        }
        try {
            return UUID.fromString(key);
        } catch (IllegalArgumentException e) {
            throw SchedJoulesProviderExceptionCodes.MALFORMED_USER_KEY.create(e, getAccount().getAccountId(), getSession().getUserId(), getSession().getContextId());
        }
    }

    /**
     * Finds the folder with the specified identifier in the internal configuration
     * 
     * @param folderId The folder identifier
     * @return The found folder as a {@link JSONObject}
     * @throws OXException if no internal configuration exists or no folder metadata is found for the specified folder
     */
    private JSONObject findFolderInInternalConfiguration(String folderId) throws OXException {
        JSONObject internalUserConfig = getAccount().getInternalConfiguration();
        if (internalUserConfig == null || internalUserConfig.isEmpty()) {
            throw SchedJoulesProviderExceptionCodes.NO_INTERNAL_CONFIGURATION.create(getAccount().getAccountId(), getSession().getUserId(), getSession().getContextId());
        }
        return findFolder(folderId, internalUserConfig.optJSONArray(SchedJoulesFields.FOLDERS));
    }

    /**
     * Find the folder in the specified folders array
     *
     * @param folderId The folder identifier
     * @param foldersArray The folders array
     * @return The found folder as a {@link JSONObject}
     * @throws OXException if no folder metadata is found for the specified folder
     */
    private JSONObject findFolder(String folderId, JSONArray foldersArray) throws OXException {
        if (foldersArray == null || foldersArray.isEmpty()) {
            throw SchedJoulesProviderExceptionCodes.NO_FOLDERS_METADATA.create(getAccount().getAccountId(), getSession().getUserId(), getSession().getContextId());
        }
        for (int index = 0; index < foldersArray.length(); index++) {
            JSONObject folder = foldersArray.optJSONObject(index);
            if (!hasMetadata(folder)) {
                continue;
            }
            if (folderId.equals(getFolderName(folder))) {
                return folder;
            }
        }
        throw SchedJoulesProviderExceptionCodes.NO_FOLDER_METADATA.create(folderId, getAccount().getAccountId(), getSession().getUserId(), getSession().getContextId());
    }

    /**
     * Prepares the specified folder
     *
     * @param folderName The folder name
     * @return the prepared {@link CalendarFolder}
     * @throws OXException
     * @throws JSONException
     */
    private CalendarFolder prepareFolder(String folderName) throws OXException {
        DefaultCalendarFolder folder = new DefaultCalendarFolder(folderName, folderName);

        folder.setPermissions(Collections.singletonList(DefaultCalendarPermission.readOnlyPermissionsFor(getAccount().getUserId())));
        folder.setLastModified(getAccount().getLastModified());

        JSONObject internalConfig = getAccount().getInternalConfiguration();
        if (internalConfig == null) {
            return folder;
        }

        // Apply extended properties
        JSONArray folders = internalConfig.optJSONArray(SchedJoulesFields.FOLDERS);
        JSONObject folderJson = findFolder(folderName, folders);

        ExtendedProperties extendedProperties = new ExtendedProperties();
        extendedProperties.replace(CalendarFolderProperty.COLOR(folderJson.optString(SchedJoulesFields.COLOR, null), false));
        extendedProperties.replace(CalendarFolderProperty.DESCRIPTION(folderJson.optString(SchedJoulesFields.DESCRIPTION, null), true));
        extendedProperties.replace(CalendarFolderProperty.USED_FOR_SYNC(folderJson.optString(SchedJoulesFields.DESCRIPTION, null), true));
        extendedProperties.replace(CalendarFolderProperty.SCHEDULE_TRANSP(Enums.parse(TimeTransparency.class, folderJson.optString(SchedJoulesFields.SCHEDULE_TRANSP, null), TimeTransparency.OPAQUE), true));

        folder.setExtendedProperties(extendedProperties);

        return folder;
    }

    /**
     * Gets the folder's name from the specified {@link JSONObject} folder metadata
     *
     * @param folder the metadata {@link JSONObject}
     * @return The folder's name or <code>null</code> if the metdata have no 'name' information
     */
    private String getFolderName(JSONObject folder) {
        String name = folder.optString(SchedJoulesFields.NAME);
        if (Strings.isEmpty(name)) {
            LOG.warn("Missing the 'name' attribute from folder metadata for account '{}' of user '{}' in context '{}'", getAccount().getAccountId(), getSession().getUserId(), getSession().getContextId());
        }
        return name;
    }

    /**
     * Checks if the specified {@link JSONObject} has metadata stored
     *
     * @param folder the metadata
     * @return <code>true</code> if the metadata are present, <code>false</code> otherwise
     */
    private boolean hasMetadata(JSONObject folder) {
        if (folder == null || folder.isEmpty()) {
            LOG.warn("Encountered an empty folder metadata entry for account '{}' of user '{}' in context '{}'", getAccount().getAccountId(), getSession().getUserId(), getSession().getContextId());
            return false;
        }
        return true;
    }
}
