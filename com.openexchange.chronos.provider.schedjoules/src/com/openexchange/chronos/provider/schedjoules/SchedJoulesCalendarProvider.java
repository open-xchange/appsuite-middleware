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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.provider.CalendarAccess;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.caching.CachingCalendarProvider;
import com.openexchange.chronos.provider.schedjoules.exception.SchedJoulesProviderExceptionCodes;
import com.openexchange.chronos.schedjoules.api.SchedJoulesAPI;
import com.openexchange.chronos.schedjoules.exception.SchedJoulesAPIExceptionCodes;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 * {@link SchedJoulesCalendarProvider}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SchedJoulesCalendarProvider extends CachingCalendarProvider {

    private static final String PROVIDER_ID = "schedjoules";
    private static final String DISPLAY_NAME = "SchedJoules";

    /**
     * Default value for refreshInterval in minutes (1 week)
     */
    private static final int DEFAULT_REFRESH_INTERVAL = 10080;
    /**
     * The minumum value for the refreshInterval in minutes (1 day)
     */
    private static final int MINIMUM_REFRESH_INTERVAL = 1440;

    /**
     * Initialises a new {@link SchedJoulesCalendarProvider}.
     */
    public SchedJoulesCalendarProvider() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.provider.CalendarProvider#getId()
     */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.provider.CalendarProvider#getDisplayName(java.util.Locale)
     */
    @Override
    public String getDisplayName(Locale locale) {
        return DISPLAY_NAME;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.provider.CalendarProvider#connect(com.openexchange.chronos.provider.Session, com.openexchange.chronos.provider.CalendarAccount, com.openexchange.chronos.provider.CalendarParameters)
     */
    @Override
    public CalendarAccess connect(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        return new SchedJoulesCalendarAccess(session, account, parameters);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.provider.caching.CachingCalendarProvider#configureAccountOpt(com.openexchange.session.Session, org.json.JSONObject, com.openexchange.chronos.service.CalendarParameters)
     */
    @Override
    protected JSONObject configureAccountOpt(Session session, JSONObject userConfig, CalendarParameters parameters) throws OXException {
        if (userConfig == null) {
            return new JSONObject();
        }
        JSONArray folders = userConfig.optJSONArray(SchedJoulesFields.FOLDERS);
        if (folders == null) {
            return new JSONObject();
        }

        try {
            JSONObject internalConfig = new JSONObject();
            JSONArray internalConfigItems = new JSONArray();
            addFolders(folders, internalConfigItems);
            internalConfig.put(SchedJoulesFields.FOLDERS, internalConfigItems);
            internalConfig.put(SchedJoulesFields.USER_KEY, UUID.randomUUID().toString());
            return internalConfig;
        } catch (JSONException e) {
            throw SchedJoulesProviderExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.provider.CalendarProvider#reconfigureAccount(com.openexchange.session.Session, org.json.JSONObject, org.json.JSONObject, com.openexchange.chronos.service.CalendarParameters)
     */
    @Override
    protected JSONObject reconfigureAccountOpt(Session session, CalendarAccount calendarAccount, JSONObject userConfig, CalendarParameters parameters) throws OXException {
        JSONObject internalConfig = calendarAccount.getInternalConfiguration();
        // User configuration is 'null' or empty or has no 'folders' attribute, thus we have to remove all subscriptions
        if (userConfig == null || userConfig.isEmpty() || !userConfig.hasAndNotNull(SchedJoulesFields.FOLDERS)) {
            // Remove cache information and folders
            return (internalConfig.remove(SchedJoulesFields.FOLDERS) == null) ? null : internalConfig;
        }

        JSONArray userConfigFolders = userConfig.optJSONArray(SchedJoulesFields.FOLDERS);
        JSONArray internalConfigFolders = internalConfig.optJSONArray(SchedJoulesFields.FOLDERS);
        if (internalConfigFolders == null || internalConfigFolders.isEmpty()) {
            // Add all user configuration folders
            try {
                internalConfigFolders = new JSONArray();
                addFolders(userConfigFolders, internalConfigFolders);
                internalConfig.put(SchedJoulesFields.FOLDERS, internalConfigFolders);
                return internalConfig;
            } catch (JSONException e) {
                throw SchedJoulesProviderExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
            }
        }

        // Check for differences and merge
        try {
            // Build a map that contains all internal subscribed items and their position in the array  
            Map<String, Integer> internalItemIds = new HashMap<>();
            for (int index = 0; index < internalConfigFolders.length(); index++) {
                JSONObject folder = internalConfigFolders.getJSONObject(index);
                internalItemIds.put(folder.getString(SchedJoulesFields.NAME), index);
            }

            JSONArray additions = new JSONArray();
            boolean added = handleAdditions(userConfigFolders, internalItemIds, additions);
            boolean deleted = handleDeletions(internalConfigFolders, internalItemIds);
            addToInternalConfiguration(internalConfigFolders, additions);

            return (added || deleted) ? internalConfig : null;
        } catch (JSONException e) {
            throw SchedJoulesProviderExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.provider.caching.CachingCalendarProvider#onAccountCreatedOpt(com.openexchange.session.Session, com.openexchange.chronos.provider.CalendarAccount, com.openexchange.chronos.service.CalendarParameters)
     */
    @Override
    protected void onAccountCreatedOpt(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // nothing to do
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.provider.caching.CachingCalendarProvider#onAccountUpdatedOpt(com.openexchange.session.Session, com.openexchange.chronos.provider.CalendarAccount, com.openexchange.chronos.service.CalendarParameters)
     */
    @Override
    protected void onAccountUpdatedOpt(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // nothing to do
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.provider.caching.CachingCalendarProvider#onAccountDeletedOpt(com.openexchange.session.Session, com.openexchange.chronos.provider.CalendarAccount, com.openexchange.chronos.service.CalendarParameters)
     */
    @Override
    protected void onAccountDeletedOpt(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // nothing to do
    }

    @Override
    public boolean recreateData(Session session, JSONObject originUserConfiguration, JSONObject newUserConfiguration) throws OXException {
        // TODO Auto-generated method stub
        return true;
    }

    ///////////////////////////////////////////// HELPERS ///////////////////////////////////////////

    /**
     * Converts and adds the user configuration folders to internal configuration folders
     * 
     * @param folders The array of the user configuration folders
     * @param internalConfigFolders The internal configuration folders
     * @throws OXException If an error is occurred
     * @throws JSONException if a JSON error is occurred
     */
    private void addFolders(JSONArray folders, JSONArray internalConfigFolders) throws OXException, JSONException {
        for (int index = 0; index < folders.length(); index++) {
            JSONObject folder = folders.getJSONObject(index);
            JSONObject internalItem = prepareFolder(folder);
            internalConfigFolders.put(internalItem);
        }
    }

    /**
     * Prepares a folder for internal configuration. Fetches the item via the itemId and
     * stores the URL
     * 
     * @param folder The JSONObject that denotes a subscription candidate
     * @return The internal item
     * @throws JSONException if a JSON error is occurred
     * @throws OXException if an error is occurred
     */
    private JSONObject prepareFolder(JSONObject folder) throws JSONException, OXException {
        int itemId = folder.getInt(SchedJoulesFields.ITEM_ID);
        String locale = folder.optString(SchedJoulesFields.LOCALE);
        int refreshInterval = folder.optInt(SchedJoulesFields.REFRESH_INTERVAL, DEFAULT_REFRESH_INTERVAL);
        if (refreshInterval < MINIMUM_REFRESH_INTERVAL) {
            refreshInterval = MINIMUM_REFRESH_INTERVAL;
        }

        JSONObject page = fetchItem(itemId, locale);

        String name = folder.optString(SchedJoulesFields.NAME);
        if (Strings.isEmpty(name)) {
            name = page.getString(SchedJoulesFields.NAME);
            folder.put(SchedJoulesFields.NAME, name);
        }

        JSONObject internalItem = new JSONObject();
        internalItem.put(SchedJoulesFields.REFRESH_INTERVAL, refreshInterval);
        internalItem.put(SchedJoulesFields.URL, page.getString(SchedJoulesFields.URL));
        internalItem.put(SchedJoulesFields.ITEM_ID, itemId);
        internalItem.put(SchedJoulesFields.NAME, name);
        return internalItem;
    }

    /**
     * Fetches the calendar's metdata with the specified item and the specified locale from the SchedJoules server
     * 
     * @param itemId The item identifier
     * @param locale The optional locale
     * @return The calendar's metadata as JSONObject
     * @throws OXException if the calendar is not found, or any other error is occurred
     */
    private JSONObject fetchItem(int itemId, String locale) throws OXException {
        try {
            JSONObject page = SchedJoulesAPI.getInstance().pages().getPage(itemId, locale);
            if (!page.hasAndNotNull(SchedJoulesFields.URL)) {
                throw SchedJoulesProviderExceptionCodes.NO_CALENDAR.create(itemId);
            }
            return page;
        } catch (OXException e) {
            if (SchedJoulesAPIExceptionCodes.PAGE_NOT_FOUND.equals(e)) {
                throw SchedJoulesProviderExceptionCodes.CALENDAR_DOES_NOT_EXIST.create(e, itemId);
            }
            throw e;
        }
    }

    /**
     * Handles the additions.
     * 
     * @param userConfigFolders The user configuration for 'folders'
     * @param internalItemIds The internal items
     * @param additions The target 'additions' object
     * @return <code>true</code> if there were new additions, <code>false</code> otherwise
     * @throws JSONException if a JSON error occurs
     * @throws OXException if any other error occurs
     */
    private boolean handleAdditions(JSONArray userConfigFolders, Map<String, Integer> internalItemIds, JSONArray additions) throws JSONException, OXException {
        for (int index = 0; index < userConfigFolders.length(); index++) {
            JSONObject folder = userConfigFolders.getJSONObject(index);
            String name = folder.optString(SchedJoulesFields.NAME);
            if (!internalItemIds.containsKey(name)) {
                additions.put(prepareFolder(folder));
            }
            internalItemIds.remove(name);
        }
        return !additions.isEmpty();
    }

    /**
     * Handle any potential deletions.
     * 
     * @param internalConfigFolders The internal configuration for 'folders'
     * @param internalItemIds The items that are to be removed from the internal configuration
     * @return <code>true</code> if the internal configuration was changed, <code>false</code> otherwise
     * @throws JSONException if a JSON error occurs
     */
    private boolean handleDeletions(JSONArray internalConfigFolders, Map<String, Integer> internalItemIds) throws JSONException {
        if (internalItemIds.isEmpty()) {
            return false;
        }

        for (String name : internalItemIds.keySet()) {
            internalConfigFolders.remove(internalItemIds.get(name));
        }
        return true;
    }

    /**
     * Adds the new items to the internal configuration
     * 
     * @param internalConfigFolders The internal configuration for folders
     * @param additions The array holding the new items
     * @throws JSONException if a JSON error is occurred
     */
    private void addToInternalConfiguration(JSONArray internalConfigFolders, JSONArray additions) throws JSONException {
        for (int index = 0; index < additions.length(); index++) {
            internalConfigFolders.put(additions.getJSONObject(index));
        }
    }
}
