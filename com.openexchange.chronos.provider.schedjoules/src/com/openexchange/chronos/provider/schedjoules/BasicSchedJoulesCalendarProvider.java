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

import static com.openexchange.chronos.provider.CalendarFolderProperty.COLOR;
import static com.openexchange.chronos.provider.CalendarFolderProperty.COLOR_LITERAL;
import static com.openexchange.chronos.provider.CalendarFolderProperty.optPropertyValue;
import static com.openexchange.chronos.provider.schedjoules.SchedJoulesFields.ITEM_ID;
import static com.openexchange.chronos.provider.schedjoules.SchedJoulesFields.LOCALE;
import static com.openexchange.chronos.provider.schedjoules.SchedJoulesFields.NAME;
import static com.openexchange.chronos.provider.schedjoules.SchedJoulesFields.REFRESH_INTERVAL;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarCapability;
import com.openexchange.chronos.provider.basic.BasicCalendarAccess;
import com.openexchange.chronos.provider.basic.CalendarSettings;
import com.openexchange.chronos.provider.caching.basic.BasicCachingCalendarProvider;
import com.openexchange.chronos.provider.schedjoules.exception.SchedJoulesProviderExceptionCodes;
import com.openexchange.chronos.provider.schedjoules.osgi.Services;
import com.openexchange.chronos.schedjoules.SchedJoulesService;
import com.openexchange.chronos.schedjoules.exception.SchedJoulesAPIExceptionCodes;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link BasicSchedJoulesCalendarProvider}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class BasicSchedJoulesCalendarProvider extends BasicCachingCalendarProvider {

    public static final String PROVIDER_ID = "schedjoules";
    private static final String DISPLAY_NAME = "SchedJoules";

    /**
     * The minumum value for the refreshInterval in minutes (1 day)
     */
    private static final int MINIMUM_REFRESH_INTERVAL = 1440;

    @Override
    public EnumSet<CalendarCapability> getCapabilities() {
        return CalendarCapability.getCapabilities(BasicSchedJoulesCalendarAccess.class);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayName(Locale locale) {
        return DISPLAY_NAME;
    }

    @Override
    public boolean isAvailable(Session session) {
        SchedJoulesService schedJoulesService = Services.getService(SchedJoulesService.class);
        return null != schedJoulesService && schedJoulesService.isAvailable(session.getContextId());
    }

    @Override
    public BasicCalendarAccess connect(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        return new BasicSchedJoulesCalendarAccess(session, account, parameters);
    }

    @Override
    protected void onAccountCreatedOpt(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // nothing to do
    }

    @Override
    protected void onAccountUpdatedOpt(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // nothing to do
    }

    @Override
    protected void onAccountDeletedOpt(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // nothing to do
    }

    @Override
    protected void onAccountDeletedOpt(Context context, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // nothing to do
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.provider.basic.BasicCalendarProvider#probe(com.openexchange.session.Session, com.openexchange.chronos.provider.basic.CalendarSettings, com.openexchange.chronos.service.CalendarParameters)
     */
    @Override
    public CalendarSettings probe(Session session, CalendarSettings settings, CalendarParameters parameters) throws OXException {
        /*
         * check config & fetch calendar metadata for referenced item
         */
        JSONObject userConfig = settings.getConfig();
        if (null == userConfig) {
            throw SchedJoulesProviderExceptionCodes.MISSING_ITEM_ID_FROM_CONFIG.create(-1, session.getUserId(), session.getContextId());
        }
        String locale = getLocale(session, userConfig);
        int itemId = getItemId(session, userConfig);
        long refreshInterval = getRefreshInterval(session, userConfig);

        JSONObject calendarMetadata = fetchItem(session.getContextId(), itemId, locale);
        String color = optPropertyValue(settings.getExtendedProperties(), COLOR_LITERAL, String.class);
        String name = settings.containsName() && null != settings.getName() ? settings.getName() : calendarMetadata.optString(NAME, "Calendar");
        /*
         * prepare & return proposed settings, taking over client-supplied values if applicable
         */
        CalendarSettings proposedSettings = new CalendarSettings();
        JSONObject proposedConfig = new JSONObject();
        ExtendedProperties proposedExtendedProperties = new ExtendedProperties();
        proposedConfig.putSafe(ITEM_ID, itemId);
        proposedConfig.putSafe(LOCALE, locale);
        proposedConfig.putSafe(REFRESH_INTERVAL, refreshInterval);
        if (null != color) {
            proposedExtendedProperties.add(COLOR(color, false));
        }
        proposedSettings.setConfig(proposedConfig);
        proposedSettings.setExtendedProperties(proposedExtendedProperties);
        proposedSettings.setName(name);
        proposedSettings.setSubscribed(true);
        return proposedSettings;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.provider.caching.basic.BasicCachingCalendarProvider#configureAccountOpt(com.openexchange.session.Session, com.openexchange.chronos.provider.basic.CalendarSettings,
     * com.openexchange.chronos.service.CalendarParameters)
     */
    @Override
    protected JSONObject configureAccountOpt(Session session, CalendarSettings settings, CalendarParameters parameters) throws OXException {
        /*
         * initialize & check user configuration for new account
         */
        JSONObject userConfig = settings.getConfig();

        String locale = getLocale(session, userConfig);
        userConfig.putSafe(SchedJoulesFields.LOCALE, locale);
        userConfig.putSafe(SchedJoulesFields.REFRESH_INTERVAL, getRefreshInterval(session, userConfig));

        JSONObject item = fetchItem(session.getContextId(), getItemId(session, userConfig), locale);
        /*
         * prepare & return internal configuration for new account, taking over client-supplied values if set
         */
        JSONObject internalConfig = new JSONObject();
        Object colorValue = optPropertyValue(settings.getExtendedProperties(), COLOR_LITERAL);
        if (null != colorValue && String.class.isInstance(colorValue)) {
            internalConfig.putSafe(SchedJoulesFields.COLOR, colorValue);
        }

        try {
            if (Strings.isNotEmpty(settings.getName())) {
                internalConfig.putSafe(SchedJoulesFields.NAME, settings.getName());
            } else {
                internalConfig.putSafe(SchedJoulesFields.NAME, item.getString(SchedJoulesFields.NAME));
            }

            internalConfig.putSafe(SchedJoulesFields.URL, item.getString(SchedJoulesFields.URL));
            internalConfig.put(SchedJoulesFields.USER_KEY, generateUserKey(session));

        } catch (JSONException e) {
            throw SchedJoulesProviderExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }

        return internalConfig;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.provider.caching.basic.BasicCachingCalendarProvider#reconfigureAccountOpt(com.openexchange.session.Session, com.openexchange.chronos.provider.CalendarAccount,
     * com.openexchange.chronos.provider.basic.CalendarSettings, com.openexchange.chronos.service.CalendarParameters)
     */
    @Override
    protected JSONObject reconfigureAccountOpt(Session session, CalendarAccount account, CalendarSettings settings, CalendarParameters parameters) throws OXException {
        JSONObject internalConfig = null != account.getInternalConfiguration() ? new JSONObject(account.getInternalConfiguration()) : new JSONObject();
        boolean changed = applyLocaleChange(session, account, settings, internalConfig);
        // Check & apply changes to extended properties
        Object colorValue = optPropertyValue(settings.getExtendedProperties(), COLOR_LITERAL);
        if (null != colorValue && String.class.isInstance(colorValue) && false == colorValue.equals(internalConfig.opt(SchedJoulesFields.COLOR))) {
            internalConfig.putSafe(SchedJoulesFields.COLOR, colorValue);
            changed = true;
        }
        if (Strings.isNotEmpty(settings.getName()) && false == settings.getName().equals(internalConfig.opt(SchedJoulesFields.NAME))) {
            internalConfig.putSafe(SchedJoulesFields.NAME, settings.getName());
            changed = true;
        }
        return changed ? internalConfig : null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.provider.caching.basic.BasicCachingCalendarProvider#triggerCacheInvalidation(com.openexchange.session.Session, org.json.JSONObject, org.json.JSONObject)
     */
    @Override
    public boolean triggerCacheInvalidation(Session session, JSONObject originUserConfiguration, JSONObject newUserConfiguration) throws OXException {
        return false;
    }

    /**
     * Checks if the locale changed and if so applies that change.
     *
     * @param session The groupware {@link Session}
     * @param account The {@link CalendarAccount}
     * @param settings The {@link CalendarSettings}
     * @param internalConfig The internal configuration
     * @return <code>true</code> if the locale was changed and the change was applied successfully, <code>false</code> otherwise
     * @throws OXException if the internal configuration contains a malformed URL, or if any JSON error is occurred
     */
    private boolean applyLocaleChange(Session session, CalendarAccount account, CalendarSettings settings, JSONObject internalConfig) throws OXException {
        JSONObject userConfig = settings.getConfig();
        if (userConfig == null) {
            return false;
        }
        getRefreshInterval(session, userConfig);
        String locale = getLocale(session, userConfig);
        try {
            String url = internalConfig.optString(SchedJoulesFields.URL);
            URL u = new URL(url);
            String path = u.getQuery();
            int startIndex = path.indexOf("l=");
            int endIndex = path.indexOf("&", startIndex);
            String l = path.substring(startIndex + 2, endIndex);
            if (false == l.equals(locale)) {
                JSONObject item = fetchItem(session.getContextId(), getItemId(session, account, userConfig), locale);
                internalConfig.putSafe(SchedJoulesFields.URL, item.getString(SchedJoulesFields.URL));
                internalConfig.putSafe(SchedJoulesFields.NAME, item.getString(SchedJoulesFields.NAME));
                return true;
            }
        } catch (MalformedURLException e) {
            throw SchedJoulesProviderExceptionCodes.INVALID_URL.create(e, account.getAccountId());
        } catch (JSONException e) {
            throw SchedJoulesProviderExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
        return false;
    }

    /**
     * Get the optional locale attribute from the specified user configuration. If not present,
     * the locale is extracted from the specified {@link Session}.
     *
     * @param session The groupware {@link Session}
     * @param userConfig The {@link JSONObject} containing the user configuration
     * @return The value of the {@link SchedJoulesFields#LOCALE} attribute if present, otherwise the locale from the specified {@link Session}
     * @throws OXException if the context of the specified {@link Session} cannot be resolved
     */
    private String getLocale(Session session, JSONObject userConfig) throws OXException {
        return userConfig.optString(SchedJoulesFields.LOCALE, ServerSessionAdapter.valueOf(session).getUser().getLocale().getLanguage());
    }

    /**
     * Retrieves the item identifier form the specified {@link JSONObject}. If not present it tries to retrieve it
     * from the already existing user configuration of the specified {@link CalendarAccount}
     *
     * @param session The groupware {@link Session}
     * @param account The {@link CalendarAccount}
     * @param userConfig The user configuration
     * @return The item identifier
     * @throws OXException if the item identifier is missing from the user configuration
     */
    private int getItemId(Session session, CalendarAccount account, JSONObject userConfig) throws OXException {
        int itemId = userConfig.optInt(SchedJoulesFields.ITEM_ID, 0);
        if (0 == itemId) {
            return getItemId(session, account.getUserConfiguration());
        }
        return itemId;
    }

    /**
     * Retrieves the item identifier form the specified {@link JSONObject}
     *
     * @param session The groupware {@link Session}
     * @param userConfig The user configuration
     * @return the item identifier
     * @throws OXException if the item identifier is missing from the user configuration
     */
    private int getItemId(Session session, JSONObject userConfig) throws OXException {
        int itemId = userConfig.optInt(SchedJoulesFields.ITEM_ID, 0);
        if (0 == itemId) {
            throw SchedJoulesProviderExceptionCodes.MISSING_ITEM_ID_FROM_CONFIG.create(-1, session.getUserId(), session.getContextId());
        }
        return itemId;
    }

    /**
     * Retrieves the refresh interval from the specified user configuration
     *
     * @param session The groupware {@link Session}
     * @param userConfig The user configuration
     * @return The value of the refresh interval
     * @throws OXException if the refresh interval is less than minimum allowed value
     */
    private long getRefreshInterval(Session session, JSONObject userConfig) throws OXException {
        long refreshInterval = userConfig.optLong(SchedJoulesFields.REFRESH_INTERVAL, MINIMUM_REFRESH_INTERVAL);
        if (MINIMUM_REFRESH_INTERVAL > refreshInterval) {
            throw SchedJoulesProviderExceptionCodes.INVALID_REFRESH_MINIMUM_INTERVAL.create(-1, session.getUserId(), session.getContextId());
        }
        return refreshInterval;
    }

    /**
     * Fetches the calendar's metadata with the specified item and the specified locale from the SchedJoules server
     *
     * @param itemId The item identifier
     * @param locale The optional locale
     * @return The calendar's metadata as JSONObject
     * @throws OXException if the calendar is not found, or any other error is occurred
     */
    private JSONObject fetchItem(int contextId, int itemId, String locale) throws OXException {
        try {
            SchedJoulesService schedJoulesService = Services.getService(SchedJoulesService.class);
            JSONValue jsonValue = schedJoulesService.getPage(contextId, itemId, locale, Collections.emptySet()).getData();
            if (!jsonValue.isObject()) {
                throw SchedJoulesProviderExceptionCodes.PAGE_DOES_NOT_DENOTE_TO_JSON.create(itemId);
            }

            JSONObject page = jsonValue.toObject();
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
     * Generates a user key from the user's primary e-mail address
     *
     * @param session The session to retrieve the user information
     * @return The user key
     */
    private String generateUserKey(Session session) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        User user = serverSession.getUser();
        return DigestUtils.sha256Hex(user.getMail());
    }
}
