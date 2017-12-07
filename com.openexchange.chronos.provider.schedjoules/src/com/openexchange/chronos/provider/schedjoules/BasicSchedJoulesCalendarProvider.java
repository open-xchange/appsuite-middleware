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

import static com.openexchange.chronos.provider.CalendarFolderProperty.COLOR_LITERAL;
import static com.openexchange.chronos.provider.CalendarFolderProperty.optPropertyValue;
import java.util.EnumSet;
import java.util.Locale;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarCapability;
import com.openexchange.chronos.provider.basic.BasicCalendarAccess;
import com.openexchange.chronos.provider.basic.BasicCalendarProvider;
import com.openexchange.chronos.provider.basic.CalendarSettings;
import com.openexchange.chronos.provider.schedjoules.exception.SchedJoulesProviderExceptionCodes;
import com.openexchange.chronos.schedjoules.SchedJoulesService;
import com.openexchange.chronos.schedjoules.exception.SchedJoulesAPIExceptionCodes;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link BasicSchedJoulesCalendarProvider}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class BasicSchedJoulesCalendarProvider implements BasicCalendarProvider {

    public static final String PROVIDER_ID = "schedjoules";
    private static final String DISPLAY_NAME = "SchedJoules";

    /**
     * The minumum value for the refreshInterval in minutes (1 day)
     */
    private static final int MINIMUM_REFRESH_INTERVAL = 1440;
    private final ServiceLookup services;

    /**
     * Initialises a new {@link BasicSchedJoulesCalendarProvider}.
     */
    public BasicSchedJoulesCalendarProvider(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public EnumSet<CalendarCapability> getCapabilities() {
        return CalendarCapability.getCapabilities(SchedJoulesCalendarAccess.class);
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
    public BasicCalendarAccess connect(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        return new BasicSchedJoulesCalendarAccess(services, session, account, parameters);
    }

    @Override
    public void onAccountCreated(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void onAccountUpdated(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void onAccountDeleted(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public JSONObject configureAccount(Session session, CalendarSettings settings, CalendarParameters parameters) throws OXException {

        /*
         * initialize & check user configuration for new account
         */
        JSONObject userConfig = settings.getConfig();
        String locale = userConfig.optString(SchedJoulesFields.LOCALE, ServerSessionAdapter.valueOf(session).getUser().getLocale().getLanguage());
        int itemId = userConfig.optInt(SchedJoulesFields.ITEM_ID, 0);
        long refreshInterval = userConfig.optLong(SchedJoulesFields.REFRESH_INTERVAL, MINIMUM_REFRESH_INTERVAL);
        if (MINIMUM_REFRESH_INTERVAL > refreshInterval) {
            throw OXException.general("");
        }
        if (0 == itemId) {
            throw OXException.general("missing item id in config");
        }
        userConfig.putSafe(SchedJoulesFields.LOCALE, locale);
        userConfig.putSafe(SchedJoulesFields.REFRESH_INTERVAL, refreshInterval);

        JSONObject item = fetchItem(session.getContextId(), itemId, locale);
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
            throw OXException.general("", e);
        }

        return internalConfig;
    }

    @Override
    public JSONObject reconfigureAccount(Session session, CalendarAccount account, CalendarSettings settings, CalendarParameters parameters) throws OXException {

        //TODO take over further changes like locale or refresh interval
        //TODO prevent unallowed changes (like itemid, or too low refresh interval)

        /*
         * check & apply changes to extended properties
         */
        boolean changed = false;
        JSONObject internalConfig = null != account.getInternalConfiguration() ? new JSONObject(account.getInternalConfiguration()) : new JSONObject();
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

    /**
     * Fetches the calendar's metdata with the specified item and the specified locale from the SchedJoules server
     *
     * @param itemId The item identifier
     * @param locale The optional locale
     * @return The calendar's metadata as JSONObject
     * @throws OXException if the calendar is not found, or any other error is occurred
     */
    private JSONObject fetchItem(int contextId, int itemId, String locale) throws OXException {
        try {
            SchedJoulesService schedJoulesService = services.getService(SchedJoulesService.class);
            // FIXME: type check
            JSONObject page = (JSONObject) schedJoulesService.getPage(contextId, itemId, locale).getData();
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
