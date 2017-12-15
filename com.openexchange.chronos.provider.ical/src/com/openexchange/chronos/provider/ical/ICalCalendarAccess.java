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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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
import static com.openexchange.chronos.provider.CalendarFolderProperty.COLOR_LITERAL;
import static com.openexchange.chronos.provider.CalendarFolderProperty.DESCRIPTION;
import static com.openexchange.chronos.provider.CalendarFolderProperty.SCHEDULE_TRANSP;
import static com.openexchange.chronos.provider.CalendarFolderProperty.USED_FOR_SYNC;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.dmfs.rfc5545.Duration;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarCapability;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.CalendarPermission;
import com.openexchange.chronos.provider.DefaultCalendarFolder;
import com.openexchange.chronos.provider.DefaultCalendarPermission;
import com.openexchange.chronos.provider.SingleFolderCalendarAccessUtils;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.provider.caching.ExternalCalendarResult;
import com.openexchange.chronos.provider.caching.SingleFolderCachingCalendarAccess;
import com.openexchange.chronos.provider.ical.conn.ICalFeedClient;
import com.openexchange.chronos.provider.ical.osgi.Services;
import com.openexchange.chronos.provider.ical.properties.ICalCalendarProviderProperties;
import com.openexchange.chronos.provider.ical.result.GetResponse;
import com.openexchange.chronos.provider.ical.result.GetResponseState;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.conversion.ConversionService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 *
 * {@link ICalCalendarAccess}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class ICalCalendarAccess extends SingleFolderCachingCalendarAccess {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ICalCalendarAccess.class);

    private final ICalFeedClient feedClient;
    private final ICalCalendarFeedConfig iCalFeedConfig;

    /**
     * Initializes a new {@link ICalCalendarAccess}.
     *
     * @param feedClient The underlying feed reader
     * @param session The calendar session
     * @param account The calendar account
     * @param parameters The calendar parameters
     */
    public ICalCalendarAccess(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        super(session, account, parameters, prepareFolder(account));
        JSONObject userConfiguration = new JSONObject(account.getUserConfiguration());
        this.iCalFeedConfig = new ICalCalendarFeedConfig.DecryptedBuilder(session, userConfiguration, getICalConfiguration()).build();
        this.feedClient = new ICalFeedClient(session, iCalFeedConfig);
    }

    @Override
    public void close() {}

    @Override
    public long getRefreshInterval() {
        JSONObject iCalConfiguration = getICalConfiguration();

        if (iCalConfiguration != null && !iCalConfiguration.isEmpty()) {
            Number calendarProviderInterval = (Number) iCalConfiguration.opt(ICalCalendarConstants.REFRESH_INTERVAL);
            if (calendarProviderInterval != null && calendarProviderInterval.longValue() > 0) {
                return calendarProviderInterval.longValue();
            }
        }
        return Services.getService(LeanConfigurationService.class).getLongProperty(getSession().getUserId(), getSession().getContextId(), ICalCalendarProviderProperties.refreshInterval);
    }

    protected JSONObject getICalConfiguration() {
        JSONObject internalConfig = this.getAccount().getInternalConfiguration();
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
    public String updateFolder(CalendarFolder folder, long clientTimestamp) throws OXException {
        ExtendedProperties originalProperties = this.folder.getExtendedProperties();
        ExtendedProperties updatedProperties = SingleFolderCalendarAccessUtils.merge(originalProperties, folder.getExtendedProperties());

        JSONObject internalConfig = getAccount().getInternalConfiguration();
        if (!originalProperties.equals(updatedProperties)) {
            try {
                internalConfig.put("extendedProperties", SingleFolderCalendarAccessUtils.writeExtendedProperties(Services.getService(ConversionService.class), updatedProperties));
            } catch (JSONException e) {
                throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }

        JSONObject iCalConfigurationFromJSON = getICalConfigurationFromJSON(internalConfig);
        iCalConfigurationFromJSON.putSafe(ICalCalendarConstants.NAME, folder.getName());

        AdministrativeCalendarAccountService service = Services.getService(AdministrativeCalendarAccountService.class);
        service.updateAccount(getSession().getContextId(), getAccount().getUserId(), getAccount().getAccountId(), iCalConfigurationFromJSON, null, clientTimestamp);

        return folder.getId();
    }

    private void updateICalConfiguration(GetResponse importResult) {
        JSONObject iCalConfig = getICalConfiguration();
        setLastUpdate(iCalConfig);
        setRefreshInterval(importResult, iCalConfig);
        setETag(importResult, iCalConfig);
    }

    private void setLastUpdate(JSONObject iCalConfig) {
        iCalConfig.putSafe(ICalCalendarConstants.LAST_UPDATE, System.currentTimeMillis());
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

    @Override
    public long getRetryAfterErrorInterval() {
        return TimeUnit.MINUTES.toMinutes(60);
    }

    @Override
    public void handleExceptions(OXException e) {
        //nothing to handle
    }

    private static DefaultCalendarFolder prepareFolder(CalendarAccount account) throws OXException {
        ExtendedProperties extendedProperties = SingleFolderCalendarAccessUtils.parseExtendedProperties(Services.getService(ConversionService.class), account.getInternalConfiguration().optJSONObject("extendedProperties"));
        if (null == extendedProperties) {
            extendedProperties = new ExtendedProperties();
        }

        JSONObject iCalConfigurationFromJSON = getICalConfigurationFromJSON(account.getInternalConfiguration());

        DefaultCalendarFolder folder = new DefaultCalendarFolder(FOLDER_ID, iCalConfigurationFromJSON.optString(ICalCalendarConstants.NAME, account.getUserConfiguration().optString(ICalCalendarConstants.URI)));
        folder.setSupportedCapabilites(CalendarCapability.getCapabilities(ICalCalendarAccess.class));
        folder.setLastModified(account.getLastModified());
        /*
         * always apply or overwrite protected defaults
         */
        extendedProperties.replace(SCHEDULE_TRANSP(TimeTransparency.TRANSPARENT, false));
        extendedProperties.replace(USED_FOR_SYNC(Boolean.FALSE, false));
        extendedProperties.replace(DESCRIPTION(iCalConfigurationFromJSON.optString(ICalCalendarConstants.DESCRIPTION, null), false));
        /*
         * insert further defaults if missing
         */
        if (!extendedProperties.contains(COLOR_LITERAL)) {
            extendedProperties.add(COLOR(null, false));
        }
        folder.setExtendedProperties(extendedProperties);
        /*
         *
         */
        CalendarPermission permission = new DefaultCalendarPermission(account.getUserId(), CalendarPermission.READ_FOLDER, CalendarPermission.READ_ALL_OBJECTS, CalendarPermission.WRITE_ALL_OBJECTS, CalendarPermission.NO_PERMISSIONS, false, false, CalendarPermission.NO_PERMISSIONS);
        folder.setPermissions(Collections.singletonList(permission));

        return folder;
    }
}
