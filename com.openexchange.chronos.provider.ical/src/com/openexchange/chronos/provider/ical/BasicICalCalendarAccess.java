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

package com.openexchange.chronos.provider.ical;

import static com.openexchange.java.Autoboxing.L;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.dmfs.rfc5545.Duration;
import org.json.JSONObject;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.caching.ExternalCalendarResult;
import com.openexchange.chronos.provider.caching.basic.BasicCachingCalendarAccess;
import com.openexchange.chronos.provider.caching.basic.BasicCachingCalendarConstants;
import com.openexchange.chronos.provider.extensions.PersonalAlarmAware;
import com.openexchange.chronos.provider.ical.conn.ICalFeedClient;
import com.openexchange.chronos.provider.ical.osgi.Services;
import com.openexchange.chronos.provider.ical.properties.ICalCalendarProviderProperties;
import com.openexchange.chronos.provider.ical.result.GetResponse;
import com.openexchange.chronos.provider.ical.result.GetResponseState;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EventID;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 *
 * {@link BasicICalCalendarAccess}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class BasicICalCalendarAccess extends BasicCachingCalendarAccess implements PersonalAlarmAware {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BasicICalCalendarAccess.class);

    private final ICalFeedClient feedClient;
    private final ICalCalendarFeedConfig iCalFeedConfig;

    /**
     * Initializes a new {@link BasicICalCalendarAccess}.
     *
     * @param session The calendar session
     * @param account The calendar account
     * @param parameters The calendar parameters
     */
    public BasicICalCalendarAccess(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        super(session, account, parameters);
        JSONObject userConfiguration = new JSONObject(account.getUserConfiguration());
        this.iCalFeedConfig = new ICalCalendarFeedConfig.DecryptedBuilder(session, userConfiguration, getICalConfiguration()).build();
        this.feedClient = new ICalFeedClient(session, iCalFeedConfig);
    }

    @Override
    protected long getRefreshInterval() {
        JSONObject userConfig = account.getUserConfiguration();
        if (userConfig != null && userConfig.hasAndNotNull(ICalCalendarConstants.REFRESH_INTERVAL)) {
            try {
                Number userInterval = userConfig.optNumber(ICalCalendarConstants.REFRESH_INTERVAL);
                if (userInterval != null && userInterval.longValue() > 0) {
                    return userInterval.longValue();
                }
            } catch (ClassCastException e) {
                LOG.warn("Unable to parse refresh interval '{}' taken from user config.", userConfig.opt(ICalCalendarConstants.REFRESH_INTERVAL), e);
            }
        }

        JSONObject iCalConfiguration = getICalConfiguration();

        if (iCalConfiguration != null && !iCalConfiguration.isEmpty()) {
            Number calendarProviderInterval = iCalConfiguration.optNumber(ICalCalendarConstants.REFRESH_INTERVAL);
            if (calendarProviderInterval != null && calendarProviderInterval.longValue() > 0) {
                return calendarProviderInterval.longValue();
            }
        }
        return Services.getService(LeanConfigurationService.class).getLongProperty(session.getUserId(), session.getContextId(), ICalCalendarProviderProperties.refreshInterval);
    }

    @Override
    public ExternalCalendarResult getAllEvents() throws OXException {
        GetResponse getResult = feedClient.executeRequest();
        String etag = iCalFeedConfig.getEtag();

        if (getResult.getState() == GetResponseState.NOT_MODIFIED || ((etag != null) && (etag.equals(getResult.getETag())))) {
            return new ExternalCalendarResult(false, Collections.emptyList());
        } else if (getResult.getState() == GetResponseState.REMOVED) {
            return new ExternalCalendarResult(true, Collections.emptyList());
        }
        ExternalCalendarResult externalCalendarResult = new ExternalCalendarResult(true, getResult.getCalendar().getEvents());
        updateICalConfiguration(getResult);
        externalCalendarResult.addWarnings(getResult.getWarnings());

        return externalCalendarResult;
    }

    @Override
    public long getRetryAfterErrorInterval(OXException e) {
        if (e == null || e.getExceptionCode() == null || CalendarExceptionCodes.AUTH_FAILED.equals(e)) {
            return BasicCachingCalendarConstants.MINIMUM_DEFAULT_RETRY_AFTER_ERROR_INTERVAL;
        }
        return Services.getService(LeanConfigurationService.class).getLongProperty(session.getUserId(), session.getContextId(), ICalCalendarProviderProperties.retryAfterErrorInterval);
    }

    private void updateICalConfiguration(GetResponse importResult) {
        JSONObject iCalConfig = getICalConfiguration();
        setLastUpdate(importResult, iCalConfig);
        setRefreshInterval(importResult, iCalConfig);
        setETag(importResult, iCalConfig);
    }

    private void setLastUpdate(GetResponse importResult, JSONObject iCalConfig) {
        if (Strings.isNotEmpty(importResult.getLastModified())) {
            iCalConfig.putSafe(ICalCalendarConstants.LAST_LAST_MODIFIED, importResult.getLastModified());
        }
    }

    private void setETag(GetResponse importResult, JSONObject iCalConfig) {
        String etag = importResult.getETag();
        if (Strings.isNotEmpty(etag)) {
            iCalConfig.putSafe(ICalCalendarConstants.ETAG, etag);
        } else { // maybe deleted from ics in the meantime
            iCalConfig.remove(ICalCalendarConstants.ETAG);
        }
    }

    private void setRefreshInterval(GetResponse importResult, JSONObject iCalConfig) {
        long persistedInterval = iCalConfig.optLong(ICalCalendarConstants.REFRESH_INTERVAL, 0);
        String refreshInterval = importResult.getRefreshInterval();

        if (Strings.isNotEmpty(refreshInterval)) {
            try {
                Duration duration = org.dmfs.rfc5545.Duration.parse(refreshInterval);
                long refreshIntervalFromFeed = TimeUnit.MILLISECONDS.toMinutes(duration.toMillis());
                if (0 == persistedInterval || persistedInterval != refreshIntervalFromFeed) {
                    iCalConfig.putSafe(ICalCalendarConstants.REFRESH_INTERVAL, L(refreshIntervalFromFeed));
                }
            } catch (IllegalArgumentException e) {
                LOG.debug("Unable to parse and persist calendars refresh interval {}.", refreshInterval, e);
            }
        } else if (persistedInterval != 0) { // maybe deleted from ics in the meantime
            iCalConfig.remove(ICalCalendarConstants.REFRESH_INTERVAL);
        }
    }

    protected JSONObject getICalConfiguration() {
        JSONObject internalConfig = account.getInternalConfiguration();
        return getICalConfigurationFromJSON(internalConfig);
    }

    private static JSONObject getICalConfigurationFromJSON(JSONObject internalConfig) {
        JSONObject icalConfig = internalConfig.optJSONObject(ICalCalendarConstants.PROVIDER_ID);
        if (icalConfig == null) {
            icalConfig = new JSONObject();
            internalConfig.putSafe(ICalCalendarConstants.PROVIDER_ID, icalConfig);
        }
        return icalConfig;
    }

    @Override
    public void close() {
        // nothing to do
    }

    @Override
    public List<OXException> getWarnings() {
        // TODO implement get warning
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
