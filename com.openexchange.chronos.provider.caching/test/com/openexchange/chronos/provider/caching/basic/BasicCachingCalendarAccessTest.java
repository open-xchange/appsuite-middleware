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

package com.openexchange.chronos.provider.caching.basic;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
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
    public void testGetLatestUpdateStates_configNullAndProviderIntervalBiggerThanOneDay_useFromProvider() {
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
    public void testGetLatestUpdateStates_configEmptyAndProviderIntervalBiggerThanOneDay_useFromProvider() {
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
        lastUpdate.putSafe(CachingCalendarAccessConstants.LAST_UPDATE, I(-1));
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
        lastUpdate.putSafe(CachingCalendarAccessConstants.LAST_UPDATE, I(0));
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
        lastUpdate.putSafe(CachingCalendarAccessConstants.LAST_UPDATE, L(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(61L)));
        JSONObject internalConfig = new JSONObject();
        internalConfig.putSafe(CachingCalendarAccessConstants.CACHING, lastUpdate);
        account = new DefaultCalendarAccount("providerId", 1, 1, internalConfig, internalConfig, new Date(System.currentTimeMillis()));
        cachingCalendarAccess = new TestCachingCalendarAccessImpl(session, account, parameters);

        cachingCalendarAccess.updateCacheIfNeeded();

        assertTrue(cachingCalendarAccess.getCacheUpdated());
    }

    @Test
    public void testUpdateCacheIfNeeded_cacheUpdateRequestedButInBlockingTime_throwException() {
        JSONObject lastUpdate = new JSONObject();
        lastUpdate.putSafe(CachingCalendarAccessConstants.LAST_UPDATE, L(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(60L)));
        JSONObject internalConfig = new JSONObject();
        internalConfig.putSafe(CachingCalendarAccessConstants.CACHING, lastUpdate);
        account = new DefaultCalendarAccount("providerId", 1, 1, internalConfig, internalConfig, new Date(System.currentTimeMillis()));
        Mockito.when(B(parameters.contains(CalendarParameters.PARAMETER_UPDATE_CACHE))).thenReturn(Boolean.TRUE);
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
        lastUpdate.putSafe(CachingCalendarAccessConstants.LAST_UPDATE, L(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(1L)));
        JSONObject internalConfig = new JSONObject();
        internalConfig.putSafe(CachingCalendarAccessConstants.CACHING, lastUpdate);
        account = new DefaultCalendarAccount("providerId", 1, 1, internalConfig, internalConfig, new Date(System.currentTimeMillis()));
        Mockito.when(B(parameters.contains(CalendarParameters.PARAMETER_UPDATE_CACHE))).thenReturn(Boolean.FALSE);
        Mockito.when(parameters.get(CalendarParameters.PARAMETER_UPDATE_CACHE, Boolean.class, Boolean.FALSE)).thenReturn(Boolean.FALSE);
        cachingCalendarAccess = new TestCachingCalendarAccessImpl(session, account, parameters);

        cachingCalendarAccess.updateCacheIfNeeded();

        assertFalse(cachingCalendarAccess.getCacheUpdated());
    }

    @Test
    public void testUpdateCacheIfNeeded_refreshIntervalNotExceededButCacheRefreshForced_update() throws OXException {
        JSONObject lastUpdate = new JSONObject();
        lastUpdate.putSafe(CachingCalendarAccessConstants.LAST_UPDATE, L(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(59L)));
        JSONObject internalConfig = new JSONObject();
        internalConfig.putSafe(CachingCalendarAccessConstants.CACHING, lastUpdate);
        account = new DefaultCalendarAccount("providerId", 1, 1, internalConfig, internalConfig, new Date(System.currentTimeMillis()));
        Mockito.when(B(parameters.contains(CalendarParameters.PARAMETER_UPDATE_CACHE))).thenReturn(Boolean.TRUE);
        Mockito.when(parameters.get(CalendarParameters.PARAMETER_UPDATE_CACHE, Boolean.class, Boolean.FALSE)).thenReturn(Boolean.TRUE);
        cachingCalendarAccess = new TestCachingCalendarAccessImpl(session, account, parameters);

        cachingCalendarAccess.updateCacheIfNeeded();

        assertTrue(cachingCalendarAccess.getCacheUpdated());
    }
}
