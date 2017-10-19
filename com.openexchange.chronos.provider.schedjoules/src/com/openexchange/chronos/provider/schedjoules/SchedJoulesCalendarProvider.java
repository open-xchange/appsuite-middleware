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

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.provider.CalendarAccess;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.caching.CachingCalendarAccess;
import com.openexchange.chronos.provider.caching.CachingCalendarProvider;
import com.openexchange.chronos.provider.schedjoules.exception.SchedJoulesProviderExceptionCodes;
import com.openexchange.chronos.schedjoules.api.SchedJoulesAPI;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.exception.OXException;
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
     * @see com.openexchange.chronos.provider.CalendarProvider#configureAccount(com.openexchange.session.Session, org.json.JSONObject, com.openexchange.chronos.service.CalendarParameters)
     */
    @Override
    public JSONObject configureAccount(Session session, JSONObject userConfig, CalendarParameters parameters) throws OXException {
        if (userConfig == null) {
            return new JSONObject();
        }
        JSONObject folders = userConfig.optJSONObject("folders");
        if (folders == null) {
            return new JSONObject();
        }

        try {
            JSONObject internalConfig = new JSONObject();
            JSONObject internalConfigItems = new JSONObject();
            addFolders(folders, internalConfigItems);
            internalConfig.put("folders", internalConfigItems);
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
    public JSONObject reconfigureAccount(Session session, JSONObject internalConfig, JSONObject userConfig, CalendarParameters parameters) throws OXException {
        // User configuration is 'null' or empty or has no 'folders' attribute, thus we have to remove all subscriptions
        if (userConfig == null || userConfig.isEmpty() || !userConfig.hasAndNotNull("folders")) {
            // Remove cache information and folders
            internalConfig = super.reconfigureAccount(session, internalConfig, userConfig, parameters);
            return (internalConfig.remove("folders") == null) ? null : internalConfig;
        }

        JSONObject userConfigFolders = userConfig.optJSONObject("folders");
        JSONObject internalConfigFolders = internalConfig.optJSONObject("folders");
        if (internalConfigFolders == null || internalConfigFolders.isEmpty()) {
            // Add all user configuration folders
            try {
                internalConfigFolders = new JSONObject();
                addFolders(userConfigFolders, internalConfigFolders);
                internalConfig.put("folders", internalConfigFolders);
                return internalConfig;
            } catch (JSONException e) {
                throw SchedJoulesProviderExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
            }
        }

        // Check for differences and merge
        try {
            // Build a set that contains all internal subscribed items 
            Set<String> internalItemIds = new HashSet<>();
            for (String name : internalConfigFolders.keySet()) {
                internalItemIds.add(name);
            }

            boolean added = handleAdditions(internalConfigFolders, userConfigFolders, internalItemIds);
            boolean deleted = handleDeletions(getInternalConfigCaching(internalConfig), internalConfigFolders, internalItemIds);

            return (added || deleted) ? internalConfig : null;
        } catch (JSONException e) {
            throw SchedJoulesProviderExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
        }
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
    private void addFolders(JSONObject folders, JSONObject internalConfigFolders) throws OXException, JSONException {
        for (String name : folders.keySet()) {
            JSONObject folder = folders.getJSONObject(name);
            JSONObject internalItem = prepareFolder(folder);
            internalConfigFolders.put(name, internalItem);
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
        int itemId = folder.getInt("itemId");
        String locale = folder.optString("locale");

        JSONObject page = SchedJoulesAPI.getInstance().pages().getPage(itemId, locale);
        if (!page.hasAndNotNull("url")) {
            throw SchedJoulesProviderExceptionCodes.NO_CALENDAR.create(itemId);
        }

        JSONObject internalItem = new JSONObject();
        internalItem.put("refreshInterval", "PT7D");
        internalItem.put("url", page.getString("url"));
        internalItem.put("itemId", itemId);
        return internalItem;
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
    private boolean handleAdditions(JSONObject internalConfigFolders, JSONObject userConfigFolders, Set<String> internalItemIds) throws JSONException, OXException {
        int origLength = internalConfigFolders.length();
        for (String name : userConfigFolders.keySet()) {
            JSONObject folder = userConfigFolders.getJSONObject(name);
            if (!internalItemIds.contains(name)) {
                internalConfigFolders.put(name, prepareFolder(folder));
            }
            internalItemIds.remove(name);
        }
        return origLength != internalConfigFolders.length();
    }

    /**
     * Handle any potential deletions.
     * 
     * @param internalConfigFolders The internal configuration for 'folderCaching'
     * @param internalConfigFolders The internal configuration for 'folders'
     * @param internalItemIds The items that are to be removed from the internal configuration
     * @return <code>true</code> if the internal configuration was changed, <code>false</code> otherwise
     */
    private boolean handleDeletions(JSONObject internalConfigCaching, JSONObject internalConfigFolders, Set<String> internalItemIds) {
        if (internalItemIds.isEmpty()) {
            return false;
        }

        for (String name : internalItemIds) {
            internalConfigFolders.remove(name);
            internalConfigCaching.remove(name);
        }
        return true;
    }

    /**
     * Returns the {@link CachingCalendarAccess#CACHING} attribute or an empty object
     * 
     * @param internalConfig The internal configuration
     * @return the {@link CachingCalendarAccess#CACHING} attribute or an empty object if no caching elements exist
     *         yet
     */
    private JSONObject getInternalConfigCaching(JSONObject internalConfig) {
        JSONObject internalConfigCaching = internalConfig.optJSONObject(CachingCalendarAccess.CACHING);
        return internalConfigCaching == null ? new JSONObject() : internalConfigCaching;
    }

}
