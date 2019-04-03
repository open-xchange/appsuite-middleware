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

package com.openexchange.chronos.provider.caching.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.sql.Connection;
import java.sql.Date;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.DefaultCalendarAccount;
import com.openexchange.chronos.provider.caching.basic.exception.BasicCachingCalendarExceptionCodes;
import com.openexchange.chronos.provider.caching.impl.TestCachingCalendarAccessImpl;
import com.openexchange.chronos.provider.caching.internal.CachingCalendarAccessConstants;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.service.CalendarEventNotificationService;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link BasicCachingCalendarAccessTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ServerSessionAdapter.class, Services.class })
public class BasicCachingCalendarAccessTest {

    protected TestCachingCalendarAccessImpl cachingCalendarAccess;

    @Mock
    protected Session session;

    @Mock
    private ServerSession serverSession;

    protected CalendarAccount account;

    @Mock
    protected CalendarParameters parameters;

    @Mock
    private CalendarStorageFactory calendarStorageFactory;

    @Mock
    private CalendarEventNotificationService calendarEventNotificationService;

    @Mock
    private DatabaseService databaseService;

    @Mock
    private Connection connection;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(ServerSessionAdapter.class);
        PowerMockito.when(ServerSessionAdapter.valueOf((com.openexchange.session.Session) ArgumentMatchers.any())).thenReturn(serverSession);

        PowerMockito.mockStatic(Services.class);
        PowerMockito.when(Services.getService(CalendarStorageFactory.class)).thenReturn(calendarStorageFactory);
        PowerMockito.when(Services.getService(CalendarEventNotificationService.class)).thenReturn(calendarEventNotificationService);
        PowerMockito.when(Services.getService(DatabaseService.class)).thenReturn(databaseService);
        PowerMockito.when(databaseService.getWritable((Context) ArgumentMatchers.any())).thenReturn(connection);

        cachingCalendarAccess = new TestCachingCalendarAccessImpl(session, account, parameters);
    }

    @Test
    public void testGetLatestUpdateStates_configNullAndProviderIntervalBiggerThanOneDay_useFromProvider() throws OXException {
        final long refreshInterval = 600000;
        cachingCalendarAccess = new TestCachingCalendarAccessImpl(session, account, parameters) {

            @Override
            public long getRefreshInterval() {
                return refreshInterval;
            }
        };
        long cascadedRefreshInterval = cachingCalendarAccess.getCascadedRefreshInterval();

        assertEquals(refreshInterval, cascadedRefreshInterval);
    }

    @Test
    public void testGetLatestUpdateStates_configEmptyAndProviderIntervalBiggerThanOneDay_useFromProvider() throws OXException {
        final long refreshInterval = 600000;
        cachingCalendarAccess = new TestCachingCalendarAccessImpl(session, account, parameters) {

            @Override
            public long getRefreshInterval() {
                return refreshInterval;
            }
        };
        long cascadedRefreshInterval = cachingCalendarAccess.getCascadedRefreshInterval();

        assertEquals(refreshInterval, cascadedRefreshInterval);
    }

    @Test
    public void testUpdateCacheIfNeeded_internalConfigNull_update() throws OXException {
        account = new DefaultCalendarAccount("providerId", 1, 1, null, new JSONObject(), new Date(System.currentTimeMillis()));
        cachingCalendarAccess = new TestCachingCalendarAccessImpl(session, account, parameters);

        cachingCalendarAccess.updateCacheIfNeeded();

        assertTrue(cachingCalendarAccess.getCacheUpdated());
    }

    @Test
    public void testUpdateCacheIfNeeded_internalConfigEmpty_update() throws OXException {
        account = new DefaultCalendarAccount("providerId", 1, 1, new JSONObject(), new JSONObject(), new Date(System.currentTimeMillis()));
        cachingCalendarAccess = new TestCachingCalendarAccessImpl(session, account, parameters);

        cachingCalendarAccess.updateCacheIfNeeded();

        assertTrue(cachingCalendarAccess.getCacheUpdated());
    }

    @Test // first caching
    public void testUpdateCacheIfNeeded_lastUpdateNull_update() throws OXException {
        JSONObject internalConfig = new JSONObject();
        internalConfig.putSafe(CachingCalendarAccessConstants.CACHING, new JSONObject());
        account = new DefaultCalendarAccount("providerId", 1, 1, internalConfig, internalConfig, new Date(System.currentTimeMillis()));
        cachingCalendarAccess = new TestCachingCalendarAccessImpl(session, account, parameters);

        cachingCalendarAccess.updateCacheIfNeeded();

        assertTrue(cachingCalendarAccess.getCacheUpdated());
    }

    @Test // cache invalidated
    public void testUpdateCacheIfNeeded_lastUpdateNegative_update() throws OXException {
        JSONObject lastUpdate = new JSONObject();
        lastUpdate.putSafe(CachingCalendarAccessConstants.LAST_UPDATE, -1);
        JSONObject internalConfig = new JSONObject();
        internalConfig.putSafe(CachingCalendarAccessConstants.CACHING, lastUpdate);
        account = new DefaultCalendarAccount("providerId", 1, 1, internalConfig, internalConfig, new Date(System.currentTimeMillis()));
        cachingCalendarAccess = new TestCachingCalendarAccessImpl(session, account, parameters);

        cachingCalendarAccess.updateCacheIfNeeded();

        assertTrue(cachingCalendarAccess.getCacheUpdated());
    }

    @Test
    public void testUpdateCacheIfNeeded_lastUpdateZero_update() throws OXException {
        JSONObject lastUpdate = new JSONObject();
        lastUpdate.putSafe(CachingCalendarAccessConstants.LAST_UPDATE, 0);
        JSONObject internalConfig = new JSONObject();
        internalConfig.putSafe(CachingCalendarAccessConstants.CACHING, lastUpdate);
        account = new DefaultCalendarAccount("providerId", 1, 1, internalConfig, internalConfig, new Date(System.currentTimeMillis()));
        cachingCalendarAccess = new TestCachingCalendarAccessImpl(session, account, parameters);

        cachingCalendarAccess.updateCacheIfNeeded();

        assertTrue(cachingCalendarAccess.getCacheUpdated());
    }

    @Test
    public void testUpdateCacheIfNeeded_refreshIntervalExceeded_update() throws OXException {
        JSONObject lastUpdate = new JSONObject();
        lastUpdate.putSafe(CachingCalendarAccessConstants.LAST_UPDATE, System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(61L));
        JSONObject internalConfig = new JSONObject();
        internalConfig.putSafe(CachingCalendarAccessConstants.CACHING, lastUpdate);
        account = new DefaultCalendarAccount("providerId", 1, 1, internalConfig, internalConfig, new Date(System.currentTimeMillis()));
        cachingCalendarAccess = new TestCachingCalendarAccessImpl(session, account, parameters);

        cachingCalendarAccess.updateCacheIfNeeded();

        assertTrue(cachingCalendarAccess.getCacheUpdated());
    }

    @Test
    public void testUpdateCacheIfNeeded_cacheUpdateRequestedButInBlockingTime_throwException() throws OXException {
        JSONObject lastUpdate = new JSONObject();
        lastUpdate.putSafe(CachingCalendarAccessConstants.LAST_UPDATE, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(60L));
        JSONObject internalConfig = new JSONObject();
        internalConfig.putSafe(CachingCalendarAccessConstants.CACHING, lastUpdate);
        account = new DefaultCalendarAccount("providerId", 1, 1, internalConfig, internalConfig, new Date(System.currentTimeMillis()));
        Mockito.when(parameters.contains(CalendarParameters.PARAMETER_UPDATE_CACHE)).thenReturn(Boolean.TRUE);
        Mockito.when(parameters.get(CalendarParameters.PARAMETER_UPDATE_CACHE, Boolean.class, Boolean.FALSE)).thenReturn(Boolean.TRUE);

        cachingCalendarAccess = new TestCachingCalendarAccessImpl(session, account, parameters);

        try {
            cachingCalendarAccess.updateCacheIfNeeded();
            fail();
        } catch (OXException e) {
            assertTrue(BasicCachingCalendarExceptionCodes.ALREADY_UP_TO_DATE.equals(e));
        }

        assertFalse(cachingCalendarAccess.getCacheUpdated());
    }

    @Test // see com.openexchange.chronos.provider.caching.basic.BasicCachingCalendarConstants.MINIMUM_DEFAULT_RETRY_AFTER_ERROR_INTERVAL
    public void testUpdateCacheIfNeeded_updateRejectedDueTo2SecondsBlockingToPreventAbuse_noUpdate() throws OXException {
        JSONObject lastUpdate = new JSONObject();
        lastUpdate.putSafe(CachingCalendarAccessConstants.LAST_UPDATE, System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(1L));
        JSONObject internalConfig = new JSONObject();
        internalConfig.putSafe(CachingCalendarAccessConstants.CACHING, lastUpdate);
        account = new DefaultCalendarAccount("providerId", 1, 1, internalConfig, internalConfig, new Date(System.currentTimeMillis()));
        cachingCalendarAccess = new TestCachingCalendarAccessImpl(session, account, parameters);

        cachingCalendarAccess.updateCacheIfNeeded();

        assertFalse(cachingCalendarAccess.getCacheUpdated());
    }

    @Test
    public void testUpdateCacheIfNeeded_refreshIntervalNotExceededButCacheRefreshForced_update() throws OXException {
        JSONObject lastUpdate = new JSONObject();
        lastUpdate.putSafe(CachingCalendarAccessConstants.LAST_UPDATE, System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(59L));
        JSONObject internalConfig = new JSONObject();
        internalConfig.putSafe(CachingCalendarAccessConstants.CACHING, lastUpdate);
        account = new DefaultCalendarAccount("providerId", 1, 1, internalConfig, internalConfig, new Date(System.currentTimeMillis()));
        Mockito.when(parameters.contains(CalendarParameters.PARAMETER_UPDATE_CACHE)).thenReturn(Boolean.TRUE);
        Mockito.when(parameters.get(CalendarParameters.PARAMETER_UPDATE_CACHE, Boolean.class, Boolean.FALSE)).thenReturn(Boolean.TRUE);
        cachingCalendarAccess = new TestCachingCalendarAccessImpl(session, account, parameters);

        cachingCalendarAccess.updateCacheIfNeeded();

        assertTrue(cachingCalendarAccess.getCacheUpdated());
    }
}
