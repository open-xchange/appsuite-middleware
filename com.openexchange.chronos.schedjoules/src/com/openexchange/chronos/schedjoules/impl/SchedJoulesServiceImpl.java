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

package com.openexchange.chronos.schedjoules.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.chronos.provider.CalendarProviderRegistry;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.schedjoules.SchedJoulesResult;
import com.openexchange.chronos.schedjoules.SchedJoulesService;
import com.openexchange.chronos.schedjoules.api.SchedJoulesAPI;
import com.openexchange.chronos.schedjoules.exception.SchedJoulesExceptionCodes;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.id.IDMangler;

/**
 * {@link SchedJoulesServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SchedJoulesServiceImpl implements SchedJoulesService, CalendarParameters {

    private final SchedJoulesAPI api;

    private final ServiceLookup services;

    private final Map<String, Object> parameters;

    /**
     * Initialises a new {@link SchedJoulesServiceImpl}.
     * 
     * @services The {@link ServiceLookup} instance
     * @throws OXException if the {@link SchedJoulesAPI} cannot be initialised
     */
    public SchedJoulesServiceImpl(ServiceLookup services) throws OXException {
        super();
        this.services = services;
        api = new SchedJoulesAPI();
        this.parameters = new HashMap<String, Object>(); // FIXME: Should be per user
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.service.SchedJoulesService#getRoot()
     */
    @Override
    public SchedJoulesResult getRoot() throws OXException {
        return new SchedJoulesResult(api.pages().getRootPage());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.service.SchedJoulesService#getRoot(java.lang.String, java.lang.String)
     */
    @Override
    public SchedJoulesResult getRoot(String locale, String location) throws OXException {
        return new SchedJoulesResult(api.pages().getRootPage(locale, location));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.service.SchedJoulesService#getPage(int)
     */
    @Override
    public SchedJoulesResult getPage(int pageId) throws OXException {
        return new SchedJoulesResult(api.pages().getPage(pageId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.schedjoules.SchedJoulesService#getPage(int, java.lang.String)
     */
    @Override
    public SchedJoulesResult getPage(int pageId, String locale) throws OXException {
        return new SchedJoulesResult(api.pages().getPage(pageId, locale));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.schedjoules.SchedJoulesService#listCountries()
     */
    @Override
    public SchedJoulesResult listCountries() throws OXException {
        return new SchedJoulesResult(api.countries().listCountries());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.schedjoules.SchedJoulesService#listCountries(java.lang.String)
     */
    @Override
    public SchedJoulesResult listCountries(String locale) throws OXException {
        return new SchedJoulesResult(api.countries().listCountries(locale));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.schedjoules.SchedJoulesService#listLanguages()
     */
    @Override
    public SchedJoulesResult listLanguages() throws OXException {
        return new SchedJoulesResult(api.languages().listLanguages());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.schedjoules.SchedJoulesService#subscribeCalendar(com.openexchange.session.Session, int, int)
     */
    @Override
    public String subscribeCalendar(Session session, int id, int accountId) throws OXException {
        return subscribeCalendar(session, id, accountId, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.schedjoules.SchedJoulesService#search(java.lang.String, java.lang.String, int, int, int)
     */
    @Override
    public SchedJoulesResult search(String query, String locale, int countryId, int categoryId, int maxRows) throws OXException {
        return new SchedJoulesResult(api.pages().search(query, locale, countryId, categoryId, maxRows));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.schedjoules.SchedJoulesService#subscribeCalendar(com.openexchange.session.Session, int, int, java.lang.String)
     */
    @Override
    public String subscribeCalendar(Session session, int id, int accountId, String locale) throws OXException {
        // Resolve the user's SchedJoules calendar account
        CalendarAccountService calendarAccountService = services.getService(CalendarAccountService.class);
        CalendarAccount calendarAccount = calendarAccountService.getAccount(session, accountId);
        if (calendarAccount == null) {
            throw CalendarExceptionCodes.ACCOUNT_NOT_FOUND.create(accountId);
        }

        // Get the SchedJoules calendar provider
        CalendarProviderRegistry registry = services.getService(CalendarProviderRegistry.class);
        CalendarProvider calendarProvider = registry.getCalendarProvider(calendarAccount.getProviderId());
        if (calendarProvider == null) {
            throw CalendarExceptionCodes.PROVIDER_NOT_AVAILABLE.create(calendarAccount.getProviderId());
        }

        JSONObject page = api.pages().getPage(id, locale);
        if (!page.hasAndNotNull("url")) {
            throw SchedJoulesExceptionCodes.NO_CALENDAR.create(id);
        }

        try {
            // Re-configure
            JSONObject userConfiguration = calendarAccount.getUserConfiguration();
            if (userConfiguration == null) {
                userConfiguration = new JSONObject();
            }

            JSONArray folders = userConfiguration.optJSONArray("folders");
            if (folders == null) {
                folders = new JSONArray();
                userConfiguration.put("folders", folders);
            }
            // Prepare the folder configuration
            JSONObject singleCalendarConfiguration = new JSONObject();
            singleCalendarConfiguration.put("url", page.getString("url"));
            singleCalendarConfiguration.put("name", page.getString("name"));
            singleCalendarConfiguration.put("refreshInterval", "PT7D"); //TODO: either default or user defined

            folders.put(singleCalendarConfiguration);

            calendarAccountService.updateAccount(session, accountId, userConfiguration, System.currentTimeMillis() + 100, null);

            return IDMangler.mangle("cal", String.valueOf(accountId), page.getString("name"));
        } catch (JSONException e) {
            throw SchedJoulesExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.schedjoules.SchedJoulesService#unsubscribeCalendar(com.openexchange.session.Session, java.lang.String, int)
     */
    @Override
    public void unsubscribeCalendar(Session session, String folderId, int accountId) throws OXException {
        // Resolve the user's SchedJoules calendar account
        CalendarAccountService calendarAccountService = services.getService(CalendarAccountService.class);
        CalendarAccount calendarAccount = calendarAccountService.getAccount(session, accountId);
        if (calendarAccount == null) {
            throw CalendarExceptionCodes.ACCOUNT_NOT_FOUND.create(accountId);
        }

        // Get the SchedJoules calendar provider
        CalendarProviderRegistry registry = services.getService(CalendarProviderRegistry.class);
        CalendarProvider calendarProvider = registry.getCalendarProvider(calendarAccount.getProviderId());
        if (calendarProvider == null) {
            throw CalendarExceptionCodes.PROVIDER_NOT_AVAILABLE.create(calendarAccount.getProviderId());
        }

        JSONObject userConfiguration = calendarAccount.getUserConfiguration();
        if (userConfiguration == null) {
            // Already unsubscribed
            return;
        }

        JSONArray folders = userConfiguration.optJSONArray("folders");
        if (folders == null || folders.isEmpty()) {
            // Already unsubscribed
            return;
        }

        List<String> unmangled = IDMangler.unmangle(folderId);
        String name = unmangled.get(2);
        try {
            Iterator<Object> iterator = folders.iterator();
            boolean removed = false;
            while(iterator.hasNext()) {
                JSONObject folder = (JSONObject) iterator.next();
                if (name.equals(folder.getString("name"))) {
                    iterator.remove();
                    removed = true;
                    break;
                }
            }
            if (removed) {
                calendarAccountService.updateAccount(session, accountId, userConfiguration, System.currentTimeMillis() + 100, null);
            }
        } catch (JSONException e) {
            throw SchedJoulesExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.service.CalendarParameters#set(java.lang.String, java.lang.Object)
     */
    @Override
    public <T> CalendarParameters set(String parameter, T value) {
        parameters.put(parameter, value);
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.service.CalendarParameters#get(java.lang.String, java.lang.Class)
     */
    @Override
    public <T> T get(String parameter, Class<T> clazz) {
        return get(parameter, clazz, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.service.CalendarParameters#get(java.lang.String, java.lang.Class, java.lang.Object)
     */
    @Override
    public <T> T get(String parameter, Class<T> clazz, T defaultValue) {
        Object value = parameters.get(parameter);
        return null == value ? defaultValue : clazz.cast(value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.service.CalendarParameters#contains(java.lang.String)
     */
    @Override
    public boolean contains(String parameter) {
        return parameters.containsKey(parameter);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.service.CalendarParameters#entrySet()
     */
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return Collections.unmodifiableSet(parameters.entrySet());
    }
}
