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
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.provider.CalendarAccess;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.chronos.provider.schedjoules.exception.SchedJoulesProviderExceptionCodes;
import com.openexchange.chronos.schedjoules.api.SchedJoulesAPI;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link SchedJoulesCalendarProvider}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SchedJoulesCalendarProvider implements CalendarProvider {

    private static final String PROVIDER_ID = "schedjoules";
    private static final String DISPLAY_NAME = "SchedJoules";

    private final ServiceLookup services;

    /**
     * Initialises a new {@link SchedJoulesCalendarProvider}.
     *
     * @param services The {@link ServiceLookup} reference
     */
    public SchedJoulesCalendarProvider(ServiceLookup services) {
        super();
        this.services = services;
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
        // User configuration is 'null' or empty, thus we have to remove all subscriptions
        if (userConfig == null || userConfig.isEmpty()) {
            return (internalConfig.remove("folders") == null) ? null : internalConfig;
        }

        // User configuration has no 'folders' attribute, thus we have to remove all subscriptions
        JSONObject userConfigFolders = userConfig.optJSONObject("folders");
        if (userConfigFolders == null || userConfigFolders.isEmpty()) {
            return (internalConfig.remove("folders") == null) ? null : internalConfig;
        }

        JSONObject internalConfigFolders = internalConfig.optJSONObject("folders");
        // Add all user configuration folders
        if (internalConfigFolders == null) {
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
            // Build a set that contains all internal subscribed items and their position in the array 
            Map<Integer, String> internalItemIds = new HashMap<>();
            for (String name : internalConfigFolders.keySet()) {
                JSONObject folder = internalConfigFolders.getJSONObject(name);
                internalItemIds.put(folder.getInt("itemId"), name);
            }

            // Build a set that contains all user configured subscribed items
            // and add any new items
            Set<Integer> userConfigItemIds = new HashSet<>();
            boolean changed = false;
            JSONObject additions = new JSONObject();
            for (String name : userConfigFolders.keySet()) {
                JSONObject folder = userConfigFolders.getJSONObject(name);
                int itemId = folder.getInt("itemId");
                userConfigItemIds.add(itemId);
                if (!internalItemIds.containsKey(itemId)) {
                    changed = true;
                    additions.put(name, prepareFolder(folder));
                }
                internalItemIds.remove(itemId);
            }

            // Handle deletions
            if (!internalItemIds.isEmpty()) {
                for (String name : internalItemIds.values()) {
                    internalConfigFolders.remove(name);
                }
                changed = true;
            }

            // Add the new items
            if (!additions.isEmpty()) {
                for (String name : additions.keySet()) {
                    internalConfigFolders.put(name, additions.getJSONObject(name));
                }
                changed = true;
            }

            //TODO: Update references in 'folderCaching' object if renames occurred

            return changed ? internalConfig : null;
        } catch (JSONException e) {
            throw SchedJoulesProviderExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.provider.CalendarProvider#onAccountCreated(com.openexchange.session.Session, com.openexchange.chronos.provider.CalendarAccount, com.openexchange.chronos.service.CalendarParameters)
     */
    @Override
    public void onAccountCreated(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.provider.CalendarProvider#onAccountUpdated(com.openexchange.session.Session, com.openexchange.chronos.provider.CalendarAccount, com.openexchange.chronos.service.CalendarParameters)
     */
    @Override
    public void onAccountUpdated(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.provider.CalendarProvider#onAccountDeleted(com.openexchange.session.Session, com.openexchange.chronos.provider.CalendarAccount, com.openexchange.chronos.service.CalendarParameters)
     */
    @Override
    public void onAccountDeleted(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // TODO Auto-generated method stub

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

        //String calendarName = page.getString("name");
        String url = page.getString("url");

        //folder.put("name", calendarName);

        JSONObject internalItem = new JSONObject();
        internalItem.put("refreshInterval", "PT7D");
        internalItem.put("url", url);
        internalItem.put("itemId", itemId);
        //internalItem.put("name", calendarName);
        return internalItem;
    }
}
