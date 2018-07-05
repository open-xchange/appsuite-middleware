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
import static com.openexchange.chronos.provider.CalendarFolderProperty.DESCRIPTION;
import static com.openexchange.chronos.provider.CalendarFolderProperty.LAST_UPDATE;
import static com.openexchange.chronos.provider.CalendarFolderProperty.SCHEDULE_TRANSP;
import static com.openexchange.chronos.provider.CalendarFolderProperty.USED_FOR_SYNC;
import static com.openexchange.chronos.provider.schedjoules.BasicSchedJoulesCalendarProvider.PROVIDER_ID;
import static com.openexchange.java.Autoboxing.B;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.basic.CalendarSettings;
import com.openexchange.chronos.provider.caching.CachingCalendarUtils;
import com.openexchange.chronos.provider.caching.ExternalCalendarResult;
import com.openexchange.chronos.provider.caching.basic.BasicCachingCalendarAccess;
import com.openexchange.chronos.provider.schedjoules.exception.SchedJoulesProviderExceptionCodes;
import com.openexchange.chronos.provider.schedjoules.osgi.Services;
import com.openexchange.chronos.schedjoules.SchedJoulesService;
import com.openexchange.chronos.schedjoules.api.auxiliary.SchedJoulesCalendar;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 * {@link BasicSchedJoulesCalendarAccess}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class BasicSchedJoulesCalendarAccess extends BasicCachingCalendarAccess {

    /**
     * Default 'X-WR-CALNAME' and 'SUMMARY' contents of an iCal that is not accessible
     */
    private static final String NO_ACCESS = "You have no access to this calendar";

    /**
     * The default calendar name if none supplied by the user
     */
    private static final String DEFAULT_CALENDAR_NAME = "calendar";

    /**
     * Defines the amount of time to wait before attempting another external request upon failure. Defaults in 1 hour.
     */
    private static final int EXTERNAL_REQUEST_TIMEOUT = 3600;

    /**
     * Initialises a new {@link BasicSchedJoulesCalendarAccess}.
     *
     * @param session The {@link Session}
     * @param account The {@link CalendarAccount}
     * @param parameters The optional {@link CalendarParameters}
     * @throws OXException If the context cannot be resolved
     */
    protected BasicSchedJoulesCalendarAccess(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        super(session, account, parameters);
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public CalendarSettings getSettings() {
        JSONObject internalConfig = account.getInternalConfiguration();

        ExtendedProperties extendedProperties = new ExtendedProperties();
        extendedProperties.add(SCHEDULE_TRANSP(TimeTransparency.TRANSPARENT, true));
        extendedProperties.add(DESCRIPTION(internalConfig.optString(SchedJoulesFields.DESCRIPTION, null)));
        if (CachingCalendarUtils.canBeUsedForSync(PROVIDER_ID, session)) {
            extendedProperties.add(USED_FOR_SYNC(B(internalConfig.optBoolean("usedForSync", false)), false));
        } else {
            extendedProperties.add(USED_FOR_SYNC(Boolean.FALSE, true));
        }
        extendedProperties.add(COLOR(internalConfig.optString(SchedJoulesFields.COLOR, null), false));
        extendedProperties.add(LAST_UPDATE(optLastUpdate()));

        CalendarSettings settings = new CalendarSettings();
        settings.setLastModified(account.getLastModified());
        settings.setConfig(account.getUserConfiguration());
        settings.setName(internalConfig.optString(SchedJoulesFields.NAME, DEFAULT_CALENDAR_NAME));
        settings.setExtendedProperties(extendedProperties);
        settings.setError(optAccountError());

        return settings;
    }

    @Override
    protected long getRefreshInterval() throws OXException {
        return account.getUserConfiguration().optLong(SchedJoulesFields.REFRESH_INTERVAL, 0);
    }

    @Override
    public ExternalCalendarResult getAllEvents() throws OXException {
        String itemId = account.getUserConfiguration().optString(SchedJoulesFields.ITEM_ID);
        JSONObject internalConfig = account.getInternalConfiguration();
        try {
            String eTag = internalConfig.optString(SchedJoulesFields.ETAG);
            long lastModified = internalConfig.optLong(SchedJoulesFields.LAST_MODIFIED, -1);
            URL url = getFeedURL(internalConfig);

            SchedJoulesService schedJoulesService = Services.getService(SchedJoulesService.class);
            SchedJoulesCalendar calendar = schedJoulesService.getCalendar(session.getContextId(), url, eTag, lastModified);
            if (eTag.equals(calendar.getETag())) {
                return new ExternalCalendarResult(false, Collections.emptyList());
            }
            if (NO_ACCESS.equals(calendar.getName())) {
                throw SchedJoulesProviderExceptionCodes.NO_ACCESS.create(itemId);
            }

            internalConfig.put(SchedJoulesFields.ETAG, calendar.getETag());
            internalConfig.put(SchedJoulesFields.LAST_MODIFIED, calendar.getLastModified());

            return new ExternalCalendarResult(true, calendar.getEvents());
        } catch (JSONException e) {
            throw SchedJoulesProviderExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (MalformedURLException e) {
            throw SchedJoulesProviderExceptionCodes.INVALID_URL.create(e, itemId);
        }
    }

    @Override
    public long getRetryAfterErrorInterval(OXException e) {
        return TimeUnit.MINUTES.toMinutes(EXTERNAL_REQUEST_TIMEOUT);
    }

    ///////////////////////////////////// HELPERS /////////////////////////////////

    /**
     * Returns the feed URL for the specified folder
     *
     * @param folder The folder metadata
     * @return The feed URL from which to fetch the events
     * @throws MalformedURLException If the URL is invalid
     * @throws JSONException if a JSON error occurs
     * @throws OXException if the feed URL cannot be returned due to either malformed user key
     */
    private URL getFeedURL(JSONObject folder) throws MalformedURLException, JSONException, OXException {
        URL url = new URL(folder.getString(SchedJoulesFields.URL));
        String userKey = getUserKey();
        return new URL(generateURL(url, userKey));
    }

    /**
     * Appends the specified user key to the specified URL
     *
     * @param url The URL
     * @param userKey The user key to append
     * @return The generated URL
     */
    private String generateURL(URL url, String userKey) {
        String urlStr = url.toString();

        StringBuilder sb = new StringBuilder();
        sb.append(urlStr);
        sb.append(urlStr.contains("?") ? "&" : "?");
        sb.append("u=").append(userKey).append("&al=none");

        return sb.toString();
    }

    /**
     * Retrieves the user's key
     *
     * @param internalConfig The internal configuration
     * @return The user key
     * @throws OXException if the userKey is malformed or missing from the configuration
     */
    private String getUserKey() throws OXException {
        String key = account.getInternalConfiguration().optString(SchedJoulesFields.USER_KEY);
        if (Strings.isEmpty(key)) {
            throw SchedJoulesProviderExceptionCodes.MISSING_USER_KEY.create(account.getAccountId(), session.getUserId(), session.getContextId());
        }
        return key;
    }

    @Override
    public List<OXException> getWarnings() {
        // TODO Auto-generated method stub
        return null;
    }
}
