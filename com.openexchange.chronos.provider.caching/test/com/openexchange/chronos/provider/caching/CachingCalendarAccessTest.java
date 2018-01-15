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

package com.openexchange.chronos.provider.caching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.DefaultCalendarFolder;
import com.openexchange.chronos.provider.caching.impl.TestCachingCalendarAccessImpl;
import com.openexchange.chronos.provider.caching.internal.CachingCalendarAccessConstants;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.provider.caching.internal.handler.FolderProcessingType;
import com.openexchange.chronos.provider.caching.internal.handler.FolderUpdateState;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link CachingCalendarAccessTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ServerSessionAdapter.class, Services.class })
public class CachingCalendarAccessTest {

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
    public void testMerge_persistedStateNull_throwException() throws OXException {
        Set<FolderUpdateState> type = cachingCalendarAccess.merge(null, Collections.<CalendarFolder> emptyList());

        assertTrue(type.isEmpty());
    }

    @Test
    public void testMerge_visibleFoldersNull_throwException() throws OXException {
        Set<FolderUpdateState> processingInstructions = cachingCalendarAccess.merge(Collections.<FolderUpdateState> emptyList(), null);

        assertTrue(processingInstructions.isEmpty());
    }

    @Test
    public void testMerge_emtpyListsNothingToCompare_returnEmptySet() throws OXException {
        Set<FolderUpdateState> processingInstructions = cachingCalendarAccess.merge(Collections.<FolderUpdateState> emptyList(), Collections.<CalendarFolder> emptyList());

        assertTrue(processingInstructions.isEmpty());
    }

    @Test
    public void testMerge_multipleNewVisibleFolders_returnInstructionsToInsert() throws OXException {
        List<CalendarFolder> visibleFolders = new ArrayList<>();
        visibleFolders.add(new DefaultCalendarFolder("myFolderId", "The name of my folder ids folder"));
        visibleFolders.add(new DefaultCalendarFolder("mySecondFolderId", "The SECOND ONE"));

        Set<FolderUpdateState> processingInstructions = cachingCalendarAccess.merge(Collections.<FolderUpdateState> emptyList(), visibleFolders);

        assertEquals(2, processingInstructions.size());
        for (FolderUpdateState folderUpdateState : processingInstructions) {
            assertEquals(FolderProcessingType.INITIAL_INSERT, folderUpdateState.getType());
        }
    }

    @Test
    public void testMerge_persistedButNoMoreAvailableFolders_returnInstructionsToRemove() throws OXException {
        List<FolderUpdateState> lastFolders = new ArrayList<>();
        lastFolders.add(new FolderUpdateState("myFolderId", new Long(System.currentTimeMillis()), 1, FolderProcessingType.UPDATE));
        lastFolders.add(new FolderUpdateState("mySecondFolderId", new Long(System.currentTimeMillis() - 111111111), 1, FolderProcessingType.UPDATE));

        Set<FolderUpdateState> processingInstructions = cachingCalendarAccess.merge(lastFolders, Collections.<CalendarFolder> emptyList());

        assertEquals(2, processingInstructions.size());
        for (FolderUpdateState folderUpdateState : processingInstructions) {
            assertEquals(FolderProcessingType.DELETE, folderUpdateState.getType());
        }
    }

    @Test
    public void testMerge_mixedMode() throws OXException {
        List<FolderUpdateState> lastFolders = new ArrayList<>();
        lastFolders.add(new FolderUpdateState("myFolderId", new Long(System.currentTimeMillis()), 1, FolderProcessingType.UPDATE));
        lastFolders.add(new FolderUpdateState("mySecondFolderId", new Long(System.currentTimeMillis() - 111111111), 1, FolderProcessingType.UPDATE));

        List<CalendarFolder> visibleFolders = new ArrayList<>();
        visibleFolders.add(new DefaultCalendarFolder("myFolderId", "The name of my folder ids folder"));
        visibleFolders.add(new DefaultCalendarFolder("a new Folder", "The NEW ONE"));

        Set<FolderUpdateState> processingInstructions = cachingCalendarAccess.merge(lastFolders, visibleFolders);

        assertEquals(3, processingInstructions.size());
        for (FolderUpdateState folderUpdateState : processingInstructions) {
            if (folderUpdateState.getFolderId().equals("myFolderId")) {
                assertEquals(FolderProcessingType.UPDATE, folderUpdateState.getType());
            }
            if (folderUpdateState.getFolderId().equals("mySecondFolderId")) {
                assertEquals(FolderProcessingType.DELETE, folderUpdateState.getType());
            }
            if (folderUpdateState.getFolderId().equals("a new Folder")) {
                assertEquals(FolderProcessingType.INITIAL_INSERT, folderUpdateState.getType());
            }
        }
    }

    @Test
    public void testCleanup_removeUndesiredInstructions() {
        Set<FolderUpdateState> lastFolders = new HashSet<>();
        lastFolders.add(new FolderUpdateState("myFolderId", new Long(System.currentTimeMillis()), 1, FolderProcessingType.UPDATE));
        lastFolders.add(new FolderUpdateState("a new Folder", new Long(System.currentTimeMillis()), 1, FolderProcessingType.UPDATE));
        lastFolders.add(new FolderUpdateState("mySecondFolderId-goaway", new Long(System.currentTimeMillis() - 111111111), 1, FolderProcessingType.UPDATE));
        lastFolders.add(new FolderUpdateState("mySecondFolderId-goaway2", new Long(System.currentTimeMillis() - 111111111), 1, FolderProcessingType.UPDATE));
        lastFolders.add(new FolderUpdateState("deleteMeFolderId", new Long(System.currentTimeMillis()), 1, FolderProcessingType.DELETE));
        lastFolders.add(new FolderUpdateState("DeleteMeToo", new Long(System.currentTimeMillis()), 1, FolderProcessingType.DELETE));
        lastFolders.add(new FolderUpdateState("InitialInsertId", new Long(System.currentTimeMillis()), 1, FolderProcessingType.INITIAL_INSERT));

        cachingCalendarAccess.cleanup(lastFolders, new String[] { "myFolderId", "a new Folder" });

        assertEquals(5, lastFolders.size());
        for (FolderUpdateState folderUpdateState : lastFolders) {
            if (folderUpdateState.getFolderId().equals("myFolderId")) {
                assertEquals(FolderProcessingType.UPDATE, folderUpdateState.getType());
            }
            if (folderUpdateState.getFolderId().equals("deleteMeFolderId")) {
                assertEquals(FolderProcessingType.DELETE, folderUpdateState.getType());
            }
            if (folderUpdateState.getFolderId().equals("InitialInsertId")) {
                assertEquals(FolderProcessingType.INITIAL_INSERT, folderUpdateState.getType());
            }
        }
    }

    @Test
    public void testGetLatestUpdateStates_emptyConfiguration_noFolderCachingState() throws OXException {
        List<FolderUpdateState> latestUpdateStates = cachingCalendarAccess.getLatestUpdateStates();

        assertTrue(latestUpdateStates.isEmpty());
    }

    @Test
    public void testGetLatestUpdateStates_noCacheConfiguration_returnInitialInsert() throws OXException, JSONException {
        Map<String, Map<String, Object>> folderCachingConfig = new HashMap<>();
        JSONObject cachingConfig = new JSONObject();
        cachingConfig.put(CachingCalendarAccessConstants.CACHING, folderCachingConfig);

        List<FolderUpdateState> latestUpdateStates = cachingCalendarAccess.getLatestUpdateStates();

        assertTrue(latestUpdateStates.isEmpty());
    }

    @Test
    public void testGetLatestUpdateStates_oneFolderCachedButRequestedInReadTime_returnUpdateState() throws OXException, JSONException {
        Map<String, Map<String, Object>> folderCachingConfig = new HashMap<>();
        Map<String, Object> latestUpdate = new HashMap<>();
        latestUpdate.put(CachingCalendarAccessConstants.LAST_UPDATE, System.currentTimeMillis());
        folderCachingConfig.put("0", latestUpdate);
        JSONObject cachingConfig = new JSONObject();
        cachingConfig.put(CachingCalendarAccessConstants.CACHING, folderCachingConfig);
        Mockito.when(account.getInternalConfiguration()).thenReturn(cachingConfig);

        List<FolderUpdateState> latestUpdateStates = cachingCalendarAccess.getLatestUpdateStates();

        assertEquals(1, latestUpdateStates.size());
        assertEquals(FolderProcessingType.READ_DB, latestUpdateStates.get(0).getType());
    }

    @Test
    public void testGetLatestUpdateStates_oneFolderCachedAndRefreshPeriodExceeded_returnUpdateState() throws OXException, JSONException {
        Map<String, Map<String, Object>> folderCachingConfig = new HashMap<>();
        Map<String, Object> latestUpdate = new HashMap<>();
        latestUpdate.put(CachingCalendarAccessConstants.LAST_UPDATE, System.currentTimeMillis() - TimeUnit.DAYS.toMillis(14L));
        folderCachingConfig.put("0", latestUpdate);
        JSONObject cachingConfig = new JSONObject();
        cachingConfig.put(CachingCalendarAccessConstants.CACHING, folderCachingConfig);
        Mockito.when(account.getInternalConfiguration()).thenReturn(cachingConfig);

        List<FolderUpdateState> latestUpdateStates = cachingCalendarAccess.getLatestUpdateStates();

        assertEquals(1, latestUpdateStates.size());
        assertEquals(FolderProcessingType.UPDATE, latestUpdateStates.get(0).getType());
    }

    @Test
    public void testGetLatestUpdateStates_multipleFolderCached_returnUpdateState() throws OXException, JSONException {
        Map<String, Map<String, Object>> folderCachingConfig = new HashMap<>();
        Map<String, Object> latestUpdate = new HashMap<>();
        latestUpdate.put(CachingCalendarAccessConstants.LAST_UPDATE, System.currentTimeMillis() - TimeUnit.DAYS.toMillis(14L));
        folderCachingConfig.put("0", latestUpdate);

        Map<String, Object> latestUpdate2 = new HashMap<>();
        latestUpdate2.put(CachingCalendarAccessConstants.LAST_UPDATE, System.currentTimeMillis());
        folderCachingConfig.put("2", latestUpdate2);
        JSONObject cachingConfig = new JSONObject();
        cachingConfig.put(CachingCalendarAccessConstants.CACHING, folderCachingConfig);
        Mockito.when(account.getInternalConfiguration()).thenReturn(cachingConfig);

        List<FolderUpdateState> latestUpdateStates = cachingCalendarAccess.getLatestUpdateStates();

        assertEquals(2, latestUpdateStates.size());
        for (FolderUpdateState folderUpdateState : latestUpdateStates) {
            if (folderUpdateState.getFolderId().equals("0")) {
                assertEquals(FolderProcessingType.UPDATE, folderUpdateState.getType());
            }
            if (folderUpdateState.getFolderId().equals("2")) {
                assertEquals(FolderProcessingType.READ_DB, folderUpdateState.getType());
            }
        }
    }

    @Test
    public void testGetLatestUpdateStates_folderKnownWithoutLastUpdateTimestamp_shouldNotHappenButReturnInitialUpdate() throws OXException, JSONException {
        Map<String, Map<String, Object>> folderCachingConfig = new HashMap<>();
        Map<String, Object> latestUpdate = new HashMap<>();
        folderCachingConfig.put("0", latestUpdate);
        JSONObject cachingConfig = new JSONObject();
        cachingConfig.put(CachingCalendarAccessConstants.CACHING, folderCachingConfig);
        Mockito.when(account.getInternalConfiguration()).thenReturn(cachingConfig);

        List<FolderUpdateState> latestUpdateStates = cachingCalendarAccess.getLatestUpdateStates();

        assertEquals(1, latestUpdateStates.size());
        assertEquals(FolderProcessingType.INITIAL_INSERT, latestUpdateStates.get(0).getType());
    }

    @Test
    public void testGetLatestUpdateStates_folderConfigNullAndProviderIntervalBiggerThanOneDay_useFromProvider() throws OXException {
        final long refreshInterval = 600000;
        cachingCalendarAccess = new TestCachingCalendarAccessImpl(session, account, parameters) {

            @Override
            public long getRefreshInterval(String folderId) {
                return refreshInterval;
            }
        };
        long cascadedRefreshInterval = cachingCalendarAccess.getCascadedRefreshInterval("0");

        assertEquals(refreshInterval, cascadedRefreshInterval);
    }

    @Test
    public void testGetLatestUpdateStates_folderConfigEmptyAndProviderIntervalBiggerThanOneDay_useFromProvider() throws OXException {
        final long refreshInterval = 600000;
        cachingCalendarAccess = new TestCachingCalendarAccessImpl(session, account, parameters) {

            @Override
            public long getRefreshInterval(String folderId) {
                return refreshInterval;
            }
        };
        long cascadedRefreshInterval = cachingCalendarAccess.getCascadedRefreshInterval("0");

        assertEquals(refreshInterval, cascadedRefreshInterval);
    }
}
