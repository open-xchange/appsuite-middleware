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
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.caching.impl.TestCachingCalendarAccessImpl;
import com.openexchange.chronos.provider.caching.internal.CachingCalendarAccessConstants;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.provider.caching.internal.handler.ProcessingType;
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

    @Mock
    protected CalendarAccount account;

    @Mock
    protected CalendarParameters parameters;

    @Mock
    private CalendarStorageFactory calendarStorageFactory;

    @Mock
    private DatabaseService databaseService;

    @Mock
    private Connection connection;

    private final Map<String, Object> cachingConfigMap = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(ServerSessionAdapter.class);
        PowerMockito.when(ServerSessionAdapter.valueOf((com.openexchange.session.Session) Matchers.any())).thenReturn(serverSession);

        PowerMockito.mockStatic(Services.class);
        PowerMockito.when(Services.getService(CalendarStorageFactory.class)).thenReturn(calendarStorageFactory);
        PowerMockito.when(Services.getService(DatabaseService.class)).thenReturn(databaseService);
        PowerMockito.when(databaseService.getWritable((Context) Matchers.any())).thenReturn(connection);

        cachingCalendarAccess = new TestCachingCalendarAccessImpl(session, account, parameters);

        Mockito.when(account.getInternalConfiguration()).thenReturn(new JSONObject());
    }

    @Test
    public void testGetLatestUpdateStates_emptyConfiguration_noCachingState() throws OXException {
        ProcessingType latestUpdateStates = cachingCalendarAccess.getProcessingType();

        assertEquals(ProcessingType.INITIAL_INSERT, latestUpdateStates);
    }

    @Test
    public void testGetLatestUpdateStates_noCacheConfiguration_returnInitialInsert() throws OXException, JSONException {
        JSONObject cachingConfig = new JSONObject();
        cachingConfig.put(CachingCalendarAccessConstants.CACHING, cachingConfig);

        ProcessingType latestUpdateStates = cachingCalendarAccess.getProcessingType();

        assertEquals(ProcessingType.INITIAL_INSERT, latestUpdateStates);
    }

    @Test
    public void testGetLatestUpdateStates_cchedButRequestedInReadTime_returnUpdateState() throws OXException, JSONException {
        Map<String, Object> latestUpdate = new HashMap<>();
        latestUpdate.put(CachingCalendarAccessConstants.LAST_UPDATE, System.currentTimeMillis());
        JSONObject cachingConfig = new JSONObject();
        cachingConfig.put(CachingCalendarAccessConstants.CACHING, latestUpdate);
        Mockito.when(account.getInternalConfiguration()).thenReturn(cachingConfig);

        ProcessingType latestUpdateStates = cachingCalendarAccess.getProcessingType();

        assertEquals(ProcessingType.READ_DB, latestUpdateStates);
    }

    @Test
    public void testGetLatestUpdateStates_cachedAndRefreshPeriodExceeded_returnUpdateState() throws OXException, JSONException {
        Map<String, Object> latestUpdate = new HashMap<>();
        latestUpdate.put(CachingCalendarAccessConstants.LAST_UPDATE, System.currentTimeMillis() - TimeUnit.DAYS.toMillis(14L));
        JSONObject cachingConfig = new JSONObject();
        cachingConfig.put(CachingCalendarAccessConstants.CACHING, latestUpdate);
        Mockito.when(account.getInternalConfiguration()).thenReturn(cachingConfig);

        ProcessingType latestUpdateStates = cachingCalendarAccess.getProcessingType();

        assertEquals(ProcessingType.UPDATE, latestUpdateStates);
    }

    @Test
    public void testGetLatestUpdateStates_knownWithoutLastUpdateTimestamp_shouldNotHappenButReturnInitialUpdate() throws OXException, JSONException {
        Map<String, Object> latestUpdate = new HashMap<>();
        JSONObject cachingConfig = new JSONObject();
        cachingConfig.put(CachingCalendarAccessConstants.CACHING, latestUpdate);
        Mockito.when(account.getInternalConfiguration()).thenReturn(cachingConfig);

        ProcessingType latestUpdateStates = cachingCalendarAccess.getProcessingType();

        assertEquals(ProcessingType.INITIAL_INSERT, latestUpdateStates);
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
}
