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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.chronos.provider.ical;

import static com.openexchange.chronos.provider.CalendarFolderProperty.COLOR;
import static com.openexchange.chronos.provider.CalendarFolderProperty.DESCRIPTION;
import static com.openexchange.chronos.provider.CalendarFolderProperty.LAST_UPDATE;
import static com.openexchange.chronos.provider.CalendarFolderProperty.SCHEDULE_TRANSP;
import static com.openexchange.chronos.provider.CalendarFolderProperty.USED_FOR_SYNC;
import static com.openexchange.java.Autoboxing.B;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.dmfs.rfc5545.Duration;
import org.json.JSONObject;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.basic.CalendarSettings;
import com.openexchange.chronos.provider.caching.ExternalCalendarResult;
import com.openexchange.chronos.provider.caching.basic.BasicCachingCalendarAccess;
import com.openexchange.chronos.provider.caching.basic.BasicCachingCalendarConstants;
import com.openexchange.chronos.provider.ical.conn.ICalFeedClient;
import com.openexchange.chronos.provider.ical.osgi.Services;
import com.openexchange.chronos.provider.ical.properties.ICalCalendarProviderProperties;
import com.openexchange.chronos.provider.ical.result.GetResponse;
import com.openexchange.chronos.provider.ical.result.GetResponseState;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 *
 * {@link BasicICalCalendarAccess}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class BasicICalCalendarAccess extends BasicCachingCalendarAccess {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BasicICalCalendarAccess.class);

    private final ICalFeedClient feedClient;
    private final ICalCalendarFeedConfig iCalFeedConfig;

    /**
     * Initializes a new {@link BasicICalCalendarAccess}.
     *
     * @param services The {@link ServiceLookup} instance
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
    public CalendarSettings getSettings() {
        JSONObject internalConfig = account.getInternalConfiguration();

        ExtendedProperties extendedProperties = new ExtendedProperties();
        extendedProperties.add(SCHEDULE_TRANSP(TimeTransparency.TRANSPARENT, true));
        extendedProperties.add(DESCRIPTION(internalConfig.optString("description", null)));
        extendedProperties.add(USED_FOR_SYNC(B(internalConfig.optBoolean("usedForSync", false)), false));
        extendedProperties.add(COLOR(internalConfig.optString("color", null), false));
        extendedProperties.add(LAST_UPDATE(optLastUpdate()));

        CalendarSettings settings = new CalendarSettings();
        settings.setLastModified(account.getLastModified());
        settings.setConfig(account.getUserConfiguration());
        settings.setName(internalConfig.optString("name", "Calendar"));
        settings.setExtendedProperties(extendedProperties);
        settings.setSubscribed(internalConfig.optBoolean("subscribed", true));
        settings.setError(optAccountError());
        return settings;
    }

    @Override
    protected long getRefreshInterval() throws OXException {
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
                    iCalConfig.putSafe(ICalCalendarConstants.REFRESH_INTERVAL, refreshIntervalFromFeed);
                }
            } catch (IllegalArgumentException e) {
                LOG.error("Unable to parse and persist calendars refresh interval {}.", refreshInterval, e);
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
}
