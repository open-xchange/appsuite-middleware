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
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
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
import com.openexchange.chronos.provider.caching.impl.CachingCalendarAccessImpl;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.provider.caching.internal.handler.ProcessingType;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Autoboxing;
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

    private CachingCalendarAccessImpl cachingCalendarAccess;

    @Mock
    private Session session;

    @Mock
    private ServerSession serverSession;

    @Mock
    private CalendarAccount account;

    @Mock
    private CalendarParameters parameters;

    @Mock
    private CalendarStorageFactory calendarStorageFactory;

    @Mock
    private DatabaseService databaseService;

    @Mock
    private Connection connection;

    private Map<String, Object> calendarConfig = new HashMap<String, Object>();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(ServerSessionAdapter.class);
        PowerMockito.when(ServerSessionAdapter.valueOf((com.openexchange.session.Session) Matchers.any())).thenReturn(serverSession);

        PowerMockito.mockStatic(Services.class);
        PowerMockito.when(Services.getService(CalendarStorageFactory.class)).thenReturn(calendarStorageFactory);
        PowerMockito.when(Services.getService(DatabaseService.class)).thenReturn(databaseService);
        PowerMockito.when(databaseService.getWritable((Context)Matchers.any())).thenReturn(connection);

        cachingCalendarAccess = new CachingCalendarAccessImpl(session, account, parameters);

        Mockito.when(account.getConfiguration()).thenReturn(calendarConfig);
    }

    @Test
    public void testGetType_noEntryForLastUpdateFound_returnInitialInsert() throws OXException {
        ProcessingType type = cachingCalendarAccess.getType();

        assertEquals(ProcessingType.INITIAL_INSERT, type);
    }

    @Test
    public void testGetType_lastUpdateToSmall_returnInitialInsert() throws OXException {
        calendarConfig.put(CachingCalendarAccess.LAST_UPDATE, Autoboxing.L(0));

        ProcessingType type = cachingCalendarAccess.getType();

        assertEquals(ProcessingType.INITIAL_INSERT, type);
    }

    @Test
    public void testGetType_lastUpdateToSmall2_returnInitialInsert() throws OXException {
        calendarConfig.put(CachingCalendarAccess.LAST_UPDATE, Autoboxing.L(-111111111));

        ProcessingType type = cachingCalendarAccess.getType();

        assertEquals(ProcessingType.INITIAL_INSERT, type);
    }

    @Test
    public void testGetType_lastUpdateFarAway_returnUpdate() throws OXException {
        calendarConfig.put(CachingCalendarAccess.LAST_UPDATE, Autoboxing.L(111111111));

        ProcessingType type = cachingCalendarAccess.getType();

        assertEquals(ProcessingType.UPDATE, type);
    }

    @Test
    public void testGetType_lastInDefinedRange_returnReadDB() throws OXException {
        long currentTimeMillis = System.currentTimeMillis();
        calendarConfig.put(CachingCalendarAccess.LAST_UPDATE, Autoboxing.L(currentTimeMillis));

        ProcessingType type = cachingCalendarAccess.getType();

        assertEquals(ProcessingType.READ_DB, type);
    }
}
