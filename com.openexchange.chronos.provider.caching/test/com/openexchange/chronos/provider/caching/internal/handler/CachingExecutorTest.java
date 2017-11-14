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

package com.openexchange.chronos.provider.caching.internal.handler;

import static org.junit.Assert.assertFalse;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.dmfs.rfc5545.DateTime;
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
import com.openexchange.chronos.Event;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.caching.ExternalCalendarResult;
import com.openexchange.chronos.provider.caching.impl.TestCachingCalendarAccessImpl;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link CachingExecutorTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ CachingHandlerFactory.class, ServerSessionAdapter.class, Services.class })
public class CachingExecutorTest {

    protected TestCachingCalendarAccessImpl cachingCalendarAccess;

    private CachingExecutor executor;

    @Mock
    private CachingHandler handler;

    @Mock
    private CachingHandlerFactory factory;

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

    private List<Event> existingEvents = new ArrayList<>();

    private ExternalCalendarResult externalCalendarResult = new ExternalCalendarResult(true, Collections.emptyList());
    private List<Event> externalEvents = new ArrayList<>();

    private Set<FolderUpdateState> lastFolderStates = new HashSet<>();

    private List<OXException> warnings = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(CachingHandlerFactory.class);
        Mockito.when(CachingHandlerFactory.getInstance()).thenReturn(factory);
        Mockito.when(factory.get(Matchers.any(), Matchers.any())).thenReturn(handler);
        
        PowerMockito.mockStatic(ServerSessionAdapter.class);
        PowerMockito.when(ServerSessionAdapter.valueOf((com.openexchange.session.Session) Matchers.any())).thenReturn(serverSession);

        PowerMockito.mockStatic(Services.class);
        PowerMockito.when(Services.getService(CalendarStorageFactory.class)).thenReturn(calendarStorageFactory);
        PowerMockito.when(Services.getService(DatabaseService.class)).thenReturn(databaseService);
        PowerMockito.when(databaseService.getWritable((Context) Matchers.any())).thenReturn(connection);


        Mockito.when(handler.getExistingEvents(Matchers.anyString())).thenReturn(existingEvents);
        Mockito.when(handler.getExternalEvents(Matchers.anyString())).thenReturn(externalCalendarResult);

        Mockito.when(account.getInternalConfiguration()).thenReturn(new JSONObject());
        cachingCalendarAccess = new TestCachingCalendarAccessImpl(session, account, parameters);

        lastFolderStates.add(new FolderUpdateState("myFolderId", new Long(System.currentTimeMillis()), 1, FolderProcessingType.UPDATE));
    }

    @Test
    public void testCache_executionSetNotAvailable_nothingTodo() {
        executor = new CachingExecutor(cachingCalendarAccess, null);

        executor.cache(warnings);

        assertFalse(cachingCalendarAccess.isConfigSaved());
        Mockito.verify(factory, Mockito.never()).get(Matchers.any(), Matchers.any());
    }

    @Test
    public void testCache_emptyExecutionSet_nothingTodo() {
        executor = new CachingExecutor(cachingCalendarAccess, Collections.<FolderUpdateState> emptySet());

        executor.cache(warnings);

        assertFalse(cachingCalendarAccess.isConfigSaved());
        Mockito.verify(factory, Mockito.never()).get(Matchers.any(), Matchers.any());
    }

    @Test
    public void testCache_existingAndExternalEmpty_nothingToPersist() throws OXException {
        executor = new CachingExecutor(cachingCalendarAccess, lastFolderStates);

        executor.cache(warnings);

        assertFalse(cachingCalendarAccess.isConfigSaved());
        Mockito.verify(factory, Mockito.times(1)).get(Matchers.any(), Matchers.any());
        Mockito.verify(handler, Mockito.never()).persist(Matchers.anyString(), Matchers.any());
    }

    @Test
    public void testCache_externalHasNewEvents_persist() throws OXException {
        executor = new CachingExecutor(cachingCalendarAccess, lastFolderStates);

        Event e = new Event();
        e.setUid("available");
        e.setStartDate(new DateTime(System.currentTimeMillis()));
        e.setTimestamp(System.currentTimeMillis());
        externalEvents.add(e);
        externalCalendarResult = new ExternalCalendarResult(true, externalEvents);
        Mockito.when(handler.getExternalEvents(Matchers.anyString())).thenReturn(externalCalendarResult);

        executor.cache(warnings);

        Mockito.verify(factory, Mockito.times(1)).get(Matchers.any(), Matchers.any());
        Mockito.verify(handler, Mockito.times(1)).persist(Matchers.anyString(), Matchers.any());
    }

    @Test
    public void testCache_existingHasEventsButExternalNot_persist() throws OXException {
        executor = new CachingExecutor(cachingCalendarAccess, lastFolderStates);

        Event e = new Event();
        e.setUid("available");
        e.setStartDate(new DateTime(System.currentTimeMillis()));
        e.setTimestamp(System.currentTimeMillis());
        existingEvents.add(e);
        Mockito.when(handler.getExistingEvents(Matchers.anyString())).thenReturn(existingEvents);

        executor.cache(warnings);

        Mockito.verify(factory, Mockito.times(1)).get(Matchers.any(), Matchers.any());
        Mockito.verify(handler, Mockito.times(1)).persist(Matchers.anyString(), Matchers.any());
    }

}
