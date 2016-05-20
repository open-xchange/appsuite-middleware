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
package com.openexchange.share.limit.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.sql.Connection;
import java.sql.SQLException;
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
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.share.limit.FileAccess;
import com.openexchange.share.limit.internal.Services;
import com.openexchange.share.limit.storage.RdbFileAccessStorage;
import com.openexchange.share.limit.util.LimitConfig;

/**
 * {@link AnonymousGuestDownloadLimiterTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Services.class, RdbFileAccessStorage.class })
public class FilesDownloadLimiterTest {

    private static int CONTEXT_ID = 1;
    private static int USER_ID = 77;
    private static int DEFAULT_TIME_FRAME_LINKS = 0;

    private FilesDownloadLimiter limiter;

    @Mock
    private ConfigViewFactory configViewFactory;

    @Mock
    private RdbFileAccessStorage fileAccessStorage;

    @Mock
    private ConfigView configView;

    private UserImpl linkUser = new UserImpl();

    private UserImpl guest = new UserImpl();

    private UserImpl internal = new UserImpl();

    @Mock
    private DatabaseService databaseService;

    @Mock
    private Connection connection;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(Services.class);
        Mockito.when(Services.getService(DatabaseService.class, true)).thenReturn(databaseService);
        Mockito.when(databaseService.getReadOnly(CONTEXT_ID)).thenReturn(connection);
        Mockito.when(databaseService.getWritable(CONTEXT_ID)).thenReturn(connection);

        Mockito.when(configViewFactory.getView(0, CONTEXT_ID)).thenReturn(configView);

        linkUser.setCreatedBy(USER_ID);
        guest.setCreatedBy(USER_ID);
        guest.setMail("guest@example.org");
        internal.setMail("internal@internal.de");

        PowerMockito.mockStatic(RdbFileAccessStorage.class);
        PowerMockito.when(RdbFileAccessStorage.getInstance()).thenReturn(fileAccessStorage);

        limiter = new FilesDownloadLimiter(this.configViewFactory);
    }

    @Test
    public void testDropObsoleteAccesses_usedConfigCascadeAndCommitedConnection() throws OXException, SQLException {
        Mockito.when(configView.opt(Matchers.anyString(), Matchers.<Class<Integer>> any(), Matchers.<Integer> any())).thenReturn(DEFAULT_TIME_FRAME_LINKS);

        limiter.dropObsoleteAccesses(linkUser, CONTEXT_ID);

        Mockito.verify(configView, Mockito.times(1)).opt(LimitConfig.TIME_FRAME_LINKS, Integer.class, DEFAULT_TIME_FRAME_LINKS);
        Mockito.verify(fileAccessStorage, Mockito.times(1)).removeAccesses(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyLong(), (Connection) Matchers.any());
        Mockito.verify(connection, Mockito.times(1)).commit();
    }

    @Test
    public void testGetLimit_noConfigCascadeValueSet_ReturnDefaults() throws OXException {
        Long sizeLimit = LimitConfig.sizeLimitLinks();
        Mockito.when(configView.opt(LimitConfig.SIZE_LIMIT_LINKS, Long.class, sizeLimit)).thenReturn(sizeLimit);
        Integer countLimit = LimitConfig.countLimitLinks();
        Mockito.when(configView.opt(LimitConfig.COUNT_LIMIT_LINKS, Integer.class, countLimit)).thenReturn(countLimit);
        Integer timeFrame = LimitConfig.timeFrameLinks();
        Mockito.when(configView.opt(LimitConfig.TIME_FRAME_LINKS, Integer.class, timeFrame)).thenReturn(timeFrame);

        FileAccess limit = limiter.getLimit(linkUser, CONTEXT_ID);

        assertNotNull(limit);
        assertEquals(sizeLimit.longValue(), limit.getSize());
        assertEquals(countLimit.intValue(), limit.getCount());
        assertEquals(timeFrame.intValue(), limit.getTimeOfEndInMillis() - limit.getTimeOfStartInMillis());
    }

    @Test
    public void testGetLimit_configCascadeValueSet_ReturnConfigCascadeValues() throws OXException {
        Long sizeLimit = LimitConfig.sizeLimitLinks();
        Mockito.when(configView.opt(LimitConfig.SIZE_LIMIT_LINKS, Long.class, sizeLimit)).thenReturn(new Long(22222222222L));
        Integer countLimit = LimitConfig.countLimitLinks();
        Mockito.when(configView.opt(LimitConfig.COUNT_LIMIT_LINKS, Integer.class, countLimit)).thenReturn(new Integer(111111));
        Integer timeFrame = LimitConfig.timeFrameLinks();
        Mockito.when(configView.opt(LimitConfig.TIME_FRAME_LINKS, Integer.class, timeFrame)).thenReturn(new Integer(7));

        FileAccess limit = limiter.getLimit(linkUser, CONTEXT_ID);

        assertNotNull(limit);
        assertEquals(22222222222L, limit.getSize());
        assertEquals(111111, limit.getCount());
        assertEquals(7, limit.getTimeOfEndInMillis() - limit.getTimeOfStartInMillis());
    }
}
