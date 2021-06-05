/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.chronos.provider.schedjoules;

import static com.openexchange.java.Autoboxing.I;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.caching.ExternalCalendarResult;
import com.openexchange.chronos.provider.caching.basic.BasicCachingCalendarAccess;
import com.openexchange.chronos.provider.extensions.PersonalAlarmAware;
import com.openexchange.chronos.provider.schedjoules.exception.SchedJoulesProviderExceptionCodes;
import com.openexchange.chronos.provider.schedjoules.osgi.Services;
import com.openexchange.chronos.schedjoules.SchedJoulesService;
import com.openexchange.chronos.schedjoules.api.auxiliary.SchedJoulesCalendar;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EventID;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 * {@link BasicSchedJoulesCalendarAccess}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class BasicSchedJoulesCalendarAccess extends BasicCachingCalendarAccess implements PersonalAlarmAware {

    /**
     * Default 'X-WR-CALNAME' and 'SUMMARY' contents of an iCal that is not accessible
     */
    private static final String NO_ACCESS = "You have no access to this calendar";

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
     */
    protected BasicSchedJoulesCalendarAccess(Session session, CalendarAccount account, CalendarParameters parameters) {
        super(session, account, parameters);
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    protected long getRefreshInterval() {
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
            throw SchedJoulesProviderExceptionCodes.MISSING_USER_KEY.create(I(account.getAccountId()), I(session.getUserId()), I(session.getContextId()));
        }
        return key;
    }

    @Override
    public List<OXException> getWarnings() {
        return null;
    }

    @Override
    public CalendarResult updateAlarms(EventID eventID, List<Alarm> alarms, long clientTimestamp) throws OXException {
        return updateAlarmsInternal(eventID, alarms, clientTimestamp, Services.getService(CalendarUtilities.class));
    }

    @Override
    public List<AlarmTrigger> getAlarmTriggers(Set<String> actions) throws OXException {
        return getAlarmTriggersInternal(actions);
    }
}
