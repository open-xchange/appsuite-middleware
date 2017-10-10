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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpStatus;
import org.dmfs.rfc5545.Duration;
import org.json.JSONObject;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.caching.ExternalCalendarResult;
import com.openexchange.chronos.provider.caching.SingleFolderCachingCalendarAccess;
import com.openexchange.chronos.provider.ical.exception.ICalProviderExceptionCodes;
import com.openexchange.chronos.provider.ical.internal.ICalCalendarProviderProperties;
import com.openexchange.chronos.provider.ical.internal.Services;
import com.openexchange.chronos.provider.ical.result.GetResult;
import com.openexchange.chronos.provider.ical.result.HeadResult;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.config.lean.LeanConfigurationService;
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

    private final ICalFeedReader reader;
    private final ICalFeedConfig iCalFeedConfig;

    /**
     * Initializes a new {@link ICalCalendarAccess}.
     *
     * @param reader The underlying feed reader
     * @param session The calendar session
     * @param account The calendar account
     * @param parameters The calendar parameters
     */
    public ICalCalendarAccess(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        super(session, account, parameters);
        this.iCalFeedConfig = new ICalFeedConfig.Builder(account, getFolderConfiguration()).build();
        this.reader = new ICalFeedReader(session,account, iCalFeedConfig);
    }

    @Override
    public void close() {
        // * FIXME - return connection to pool
    }

    @Override
    public long getRefreshInterval() {
        long refreshInterval = getFolderConfiguration().optLong(REFRESH_INTERVAL, 0);
        if (refreshInterval > 0) {
            return refreshInterval;
        }
        return Services.getService(LeanConfigurationService.class).getLongProperty(getSession().getUserId(), getSession().getContextId(), ICalCalendarProviderProperties.refreshInterval);
    }

    @Override
    public ExternalCalendarResult getEvents() throws OXException {
        String uri = this.iCalFeedConfig.getFeedUrl();
        verifyURI(uri);

        HeadResult headResult = reader.head(uri);
        String etag = iCalFeedConfig.getEtag();
        if (headResult.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            throw CalendarExceptionCodes.AUTH_FAILED.create(uri);
        }
        if (headResult.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            throw ICalProviderExceptionCodes.NO_FEED.create(uri);
        }
        if (headResult.getStatusCode() == HttpStatus.SC_NOT_MODIFIED || // response says not modified
            ((etag != null) && (headResult.getETag().equals(etag)))) { // same etag
            return new ExternalCalendarResult();
        }
        return getAndHandleFeed(uri);
    }

    private void verifyURI(String uri) throws OXException {
        if (Strings.isEmpty(uri)) {
            throw ICalProviderExceptionCodes.MISSING_FEED_URI.create(getAccount().getAccountId(), getSession().getContextId());
        }
        try {
            boolean denied = ICalCalendarProviderProperties.isDenied(new URI(uri));
            if (denied) {
                throw ICalProviderExceptionCodes.FEED_URI_NOT_ALLOWED.create(uri);
            }
        } catch (URISyntaxException e) {
            throw ICalProviderExceptionCodes.BAD_FEED_URI.create(uri, e);
        }
    }

    private ExternalCalendarResult getAndHandleFeed(String uri) throws OXException {
        ExternalCalendarResult externalCalendarResult = new ExternalCalendarResult();
        GetResult importResult = reader.get(uri);
        if (importResult == null || importResult.getCalendar() == null) {
            LOG.debug("Unable to retrieve data from feed URI {}.", uri);
            throw ICalProviderExceptionCodes.NO_FEED.create(uri);
        }

        updateFolderConfiguration(importResult);
        externalCalendarResult.addEvents(importResult.getCalendar().getEvents());

        return externalCalendarResult;
    }

    private void updateFolderConfiguration(GetResult importResult) {
        JSONObject folderConfig = getFolderConfiguration();
        setRefreshInterval(importResult, folderConfig);
        setETag(importResult, folderConfig);
    }

    private void setETag(GetResult importResult, JSONObject folderConfig) {
        String etag = importResult.getETag();
        if (Strings.isNotEmpty(etag)) {
            folderConfig.putSafe(ICalFeedConfig.ETAG, etag);
        } else { // maybe deleted from ics in the meantime
            folderConfig.remove(ICalFeedConfig.ETAG);
        }
    }

    private void setRefreshInterval(GetResult importResult, JSONObject folderConfig) {

        long persistedInterval = folderConfig.optLong(REFRESH_INTERVAL, 0);
        String refreshInterval = importResult.getRefreshInterval();
        if (Strings.isNotEmpty(refreshInterval)) {
            Duration duration = org.dmfs.rfc5545.Duration.parse(refreshInterval);
            long refreshIntervalFromFeed = TimeUnit.MILLISECONDS.toMinutes(duration.toMillis());
            if (0 == persistedInterval || persistedInterval != refreshIntervalFromFeed) {
                folderConfig.putSafe(REFRESH_INTERVAL, refreshIntervalFromFeed);
            }
        } else if (persistedInterval != 0) { // maybe deleted from ics in the meantime
            folderConfig.remove(REFRESH_INTERVAL);
        }
    }

    @Override
    public long getExternalRequestTimeout() {
        return TimeUnit.MINUTES.toMinutes(60);
    }

    @Override
    public void handleExceptions(String calendarFolderId, OXException e) {

    }

    @Override
    public String updateFolder(String folderId, CalendarFolder folder, long clientTimestamp) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }
}
