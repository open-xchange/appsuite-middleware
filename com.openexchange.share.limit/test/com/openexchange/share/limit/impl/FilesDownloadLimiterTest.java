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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Set;
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
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.AJAXRequestResult.ResultType;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.share.limit.FileAccess;
import com.openexchange.share.limit.internal.Services;
import com.openexchange.share.limit.storage.RdbFileAccessStorage;
import com.openexchange.share.limit.util.LimitConfig;
import com.openexchange.tools.servlet.limit.AbstractActionLimitedException;
import com.openexchange.tools.session.ServerSession;

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

    private AJAXRequestData requestData = new AJAXRequestData();

    @Mock
    private DatabaseService databaseService;

    @Mock
    private Connection connection;

    @Mock
    private ServerSession session;

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

        requestData.setModule("files/myFile.txt");
        requestData.setAction("document");
        requestData.setPathInfo("/myFile.txt");
        requestData.setSession(session);
        Mockito.when(session.isAnonymous()).thenReturn(Boolean.FALSE);
        Mockito.when(session.getUser()).thenReturn(linkUser);

        Mockito.when(configView.opt(LimitConfig.LIMIT_ENABLED, Boolean.class, Boolean.FALSE)).thenReturn(Boolean.TRUE);

        limiter = new FilesDownloadLimiter(this.configViewFactory);
    }

    @Test
    public void testGetAction() {
        Set<String> actions = limiter.getActions();

        assertTrue(actions.size() == 3);
        assertTrue(actions.contains("document"));
        assertTrue(actions.contains("zipdocuments"));
        assertTrue(actions.contains("zipfolder"));
    }

    @Test
    public void testGetModule() {
        String module = limiter.getModule();

        assertTrue(module.equalsIgnoreCase("files"));
    }

    @Test
    public void testGetModuleForInfostore() {
        InfostoreDownloadLimiter infostoreLimiter = new InfostoreDownloadLimiter(configViewFactory);
        String module = infostoreLimiter.getModule();

        assertTrue(module.equalsIgnoreCase("infostore"));
    }

    @Test
    public void testDropObsoleteAccesses_isLink_usedConfigCascadeAndCommitedConnection() throws OXException, SQLException {
        Mockito.when(configView.opt(Matchers.anyString(), Matchers.<Class<Integer>> any(), Matchers.<Integer> any())).thenReturn(DEFAULT_TIME_FRAME_LINKS);

        limiter.dropObsoleteAccesses(linkUser, CONTEXT_ID);

        Mockito.verify(configView, Mockito.times(1)).opt(LimitConfig.TIME_FRAME_LINKS, Integer.class, DEFAULT_TIME_FRAME_LINKS);
        Mockito.verify(fileAccessStorage, Mockito.times(1)).removeAccesses(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyLong(), (Connection) Matchers.any());
        Mockito.verify(connection, Mockito.times(1)).commit();
    }

    @Test
    public void testDropObsoleteAccesses_isGuest_usedConfigCascadeAndCommitedConnection() throws OXException, SQLException {
        Mockito.when(configView.opt(Matchers.anyString(), Matchers.<Class<Integer>> any(), Matchers.<Integer> any())).thenReturn(DEFAULT_TIME_FRAME_LINKS);

        limiter.dropObsoleteAccesses(guest, CONTEXT_ID);

        Mockito.verify(configView, Mockito.times(1)).opt(LimitConfig.TIME_FRAME_GUESTS, Integer.class, DEFAULT_TIME_FRAME_LINKS);
        Mockito.verify(fileAccessStorage, Mockito.times(1)).removeAccesses(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyLong(), (Connection) Matchers.any());
        Mockito.verify(connection, Mockito.times(1)).commit();
    }

    @Test
    public void testDropObsoleteAccesses_noGuest_doNothing() throws OXException, SQLException {
        limiter.dropObsoleteAccesses(internal, CONTEXT_ID);

        Mockito.verify(configView, Mockito.never()).opt(LimitConfig.TIME_FRAME_LINKS, Integer.class, DEFAULT_TIME_FRAME_LINKS);
        Mockito.verify(fileAccessStorage, Mockito.never()).removeAccesses(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyLong(), (Connection) Matchers.any());
        Mockito.verify(connection, Mockito.never()).commit();
    }

    @Test
    public void testGetLimit_noGuest_returnNull() throws OXException {
        FileAccess limit = limiter.getLimit(internal, CONTEXT_ID);

        assertNull(limit);
    }

    @Test
    public void testGetLimit_limitDisabled_returnNull() throws OXException {
        Mockito.when(configView.opt(LimitConfig.LIMIT_ENABLED, Boolean.class, Boolean.FALSE)).thenReturn(Boolean.FALSE);

        FileAccess limit = limiter.getLimit(linkUser, CONTEXT_ID);

        assertNull(limit);
    }

    @Test
    public void testGetLimit_noConfigCascadeValueSet_ReturnDefaults() throws OXException {
        Long sizeLimit = LimitConfig.sizeLimitLinks();
        Mockito.when(configView.opt(LimitConfig.SIZE_LIMIT_LINKS, Long.class, sizeLimit)).thenReturn(sizeLimit);
        Integer countLimit = LimitConfig.countLimitLinks();
        Mockito.when(configView.opt(LimitConfig.COUNT_LIMIT_LINKS, Integer.class, countLimit)).thenReturn(countLimit);
        Integer timeFrame = LimitConfig.timeFrameLinks();
        Mockito.when(configView.opt(LimitConfig.TIME_FRAME_LINKS, Integer.class, timeFrame)).thenReturn(timeFrame);
        Mockito.when(configView.opt(LimitConfig.LIMIT_ENABLED, Boolean.class, Boolean.FALSE)).thenReturn(Boolean.TRUE);

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

    @Test
    public void testGetLimit_limitDisabledForGuest_returnNull() throws OXException {
        Mockito.when(configView.opt(LimitConfig.LIMIT_ENABLED, Boolean.class, Boolean.FALSE)).thenReturn(Boolean.FALSE);

        FileAccess limit = limiter.getLimit(guest, CONTEXT_ID);

        assertNull(limit);
    }

    @Test
    public void testGetLimit_noConfigCascadeValueSetForGuest_ReturnDefaults() throws OXException {
        Long sizeLimit = LimitConfig.sizeLimitGuests();
        Mockito.when(configView.opt(LimitConfig.SIZE_LIMIT_GUESTS, Long.class, sizeLimit)).thenReturn(sizeLimit);
        Integer countLimit = LimitConfig.countLimitGuests();
        Mockito.when(configView.opt(LimitConfig.COUNT_LIMIT_GUESTS, Integer.class, countLimit)).thenReturn(countLimit);
        Integer timeFrame = LimitConfig.timeFrameGuests();
        Mockito.when(configView.opt(LimitConfig.TIME_FRAME_GUESTS, Integer.class, timeFrame)).thenReturn(timeFrame);
        Mockito.when(configView.opt(LimitConfig.LIMIT_ENABLED, Boolean.class, Boolean.FALSE)).thenReturn(Boolean.TRUE);

        FileAccess limit = limiter.getLimit(guest, CONTEXT_ID);

        assertNotNull(limit);
        assertEquals(sizeLimit.longValue(), limit.getSize());
        assertEquals(countLimit.intValue(), limit.getCount());
        assertEquals(timeFrame.intValue(), limit.getTimeOfEndInMillis() - limit.getTimeOfStartInMillis());
    }

    @Test
    public void testGetLimit_configCascadeValueSetForGuest_ReturnConfigCascadeValues() throws OXException {
        Long sizeLimit = LimitConfig.sizeLimitGuests();
        Mockito.when(configView.opt(LimitConfig.SIZE_LIMIT_GUESTS, Long.class, sizeLimit)).thenReturn(new Long(22222222222L));
        Integer countLimit = LimitConfig.countLimitGuests();
        Mockito.when(configView.opt(LimitConfig.COUNT_LIMIT_GUESTS, Integer.class, countLimit)).thenReturn(new Integer(111111));
        Integer timeFrame = LimitConfig.timeFrameGuests();
        Mockito.when(configView.opt(LimitConfig.TIME_FRAME_GUESTS, Integer.class, timeFrame)).thenReturn(new Integer(7));

        FileAccess limit = limiter.getLimit(guest, CONTEXT_ID);

        assertNotNull(limit);
        assertEquals(22222222222L, limit.getSize());
        assertEquals(111111, limit.getCount());
        assertEquals(7, limit.getTimeOfEndInMillis() - limit.getTimeOfStartInMillis());
    }

    @Test
    public void testApplicable_wrongModule_notApplicable() throws OXException {
        requestData.setModule("apps/manifests");

        boolean applicable = limiter.applicable(requestData);

        assertFalse(applicable);
    }

    @Test
    public void testApplicable_wrongAction_notApplicable() throws OXException {
        requestData.setAction("all");

        boolean applicable = limiter.applicable(requestData);

        assertFalse(applicable);
    }

    @Test
    public void testApplicable_wrongAction_sessionNotChecked() throws OXException {
        requestData.setAction("all");

        limiter.applicable(requestData);

        Mockito.verify(session, Mockito.never()).isAnonymous();
    }

    @Test
    public void testApplicable_isAnonymousSession_notApplicable() throws OXException {
        Mockito.when(session.isAnonymous()).thenReturn(Boolean.TRUE);

        boolean applicable = limiter.applicable(requestData);

        assertFalse(applicable);
        Mockito.verify(session, Mockito.times(1)).isAnonymous();
    }

    @Test
    public void testApplicable_isNotGuest_notApplicable() throws OXException {
        Mockito.when(session.getUser()).thenReturn(internal);

        boolean applicable = limiter.applicable(requestData);

        assertFalse(applicable);
    }

    @Test
    public void testApplicable_isGuest_applicable() throws OXException {
        Mockito.when(session.getUser()).thenReturn(guest);

        boolean applicable = limiter.applicable(requestData);

        assertTrue(applicable);
    }

    @Test
    public void testApplicable_isGuest_isApplicable() throws OXException {
        boolean applicable = limiter.applicable(requestData);

        assertTrue(applicable);
    }

    @Test
    public void testOnRequestInitialized_isAnonymousSession_return() throws OXException {
        limiter = new FilesDownloadLimiter(configViewFactory) {

            @Override
            protected void removeOldAccesses(ServerSession session, int contextId) {
                fail();
            }
        };
        Mockito.when(session.isAnonymous()).thenReturn(Boolean.TRUE);

        limiter.onRequestInitialized(requestData);
    }

    @Test
    public void testOnRequestInitialized_noLimitAvailable_returnWithoutCheck() throws OXException {
        Mockito.when(session.getContextId()).thenReturn(CONTEXT_ID);

        limiter = new FilesDownloadLimiter(configViewFactory) {

            @Override
            protected void removeOldAccesses(ServerSession session, int contextId) {
                assertEquals(CONTEXT_ID, contextId);
            }

            @Override
            protected FileAccess getLimit(User user, int contextId) {
                return null;
            }

            @Override
            protected FileAccess getUsed(FileAccess limit) {
                fail();
                return null;
            }
        };

        limiter.onRequestInitialized(requestData);
    }

    @Test
    public void testOnRequestInitialized_limitDisabled_returnWithoutCheck() throws OXException {
        Mockito.when(session.getContextId()).thenReturn(CONTEXT_ID);

        limiter = new FilesDownloadLimiter(configViewFactory) {

            @Override
            protected void removeOldAccesses(ServerSession session, int contextId) {
                assertEquals(CONTEXT_ID, contextId);
            }

            @Override
            protected FileAccess getLimit(User user, int contextId) {
                return new FileAccess(contextId, user.getId(), 111L, 111L, 222, 22222L);
            }

            @Override
            protected FileAccess getUsed(FileAccess limit) {
                fail();
                return null;
            }
        };

        limiter.onRequestInitialized(requestData);
    }

    @Test
    public void testOnRequestInitialized_usedNotFound_returnWithoutCheck() throws OXException {
        Mockito.when(session.getContextId()).thenReturn(CONTEXT_ID);

        limiter = new FilesDownloadLimiter(configViewFactory) {

            @Override
            protected void removeOldAccesses(ServerSession session, int contextId) {
                assertEquals(CONTEXT_ID, contextId);
            }

            @Override
            protected FileAccess getLimit(User user, int contextId) {
                long now = new Date().getTime();
                return new FileAccess(contextId, user.getId(), now - 300000, now, 222, 22222L);
            }

            @Override
            protected FileAccess getUsed(FileAccess limit) {
                return null;
            }

            @Override
            protected void throwIfExceeded(FileAccess limit, FileAccess used) throws AbstractActionLimitedException {
                fail();
            }
        };

        limiter.onRequestInitialized(requestData);
    }

    @Test
    public void testOnRequestInitialized_notExceeded_return() throws OXException {
        Mockito.when(session.getContextId()).thenReturn(CONTEXT_ID);

        limiter = new FilesDownloadLimiter(configViewFactory) {

            @Override
            protected void removeOldAccesses(ServerSession session, int contextId) {
                assertEquals(CONTEXT_ID, contextId);
            }

            @Override
            protected FileAccess getLimit(User user, int contextId) {
                long now = new Date().getTime();
                return new FileAccess(contextId, user.getId(), now - 300000, now, 222, 22222L);
            }

            @Override
            protected FileAccess getUsed(FileAccess limit) {
                long now = new Date().getTime();
                return new FileAccess(limit.getContextId(), limit.getUserId(), now - 300000, now, 111, 1111L);
            }
        };

        limiter.onRequestInitialized(requestData);
    }

    @Test(expected = AbstractActionLimitedException.class)
    public void testOnRequestInitialized_sizeExceeded_throwException() throws OXException {
        Mockito.when(session.getContextId()).thenReturn(CONTEXT_ID);

        limiter = new FilesDownloadLimiter(configViewFactory) {

            @Override
            protected void removeOldAccesses(ServerSession session, int contextId) {
                assertEquals(CONTEXT_ID, contextId);
            }

            @Override
            protected FileAccess getLimit(User user, int contextId) {
                long now = new Date().getTime();
                return new FileAccess(contextId, user.getId(), now - 300000, now, 222, 22222L);
            }

            @Override
            protected FileAccess getUsed(FileAccess limit) {
                long now = new Date().getTime();
                return new FileAccess(limit.getContextId(), limit.getUserId(), now - 300000, now, 111, 7777777L);
            }
        };

        limiter.onRequestInitialized(requestData);
    }

    @Test(expected = AbstractActionLimitedException.class)
    public void testOnRequestInitialized_countExceeded_throwException() throws OXException {
        Mockito.when(session.getContextId()).thenReturn(CONTEXT_ID);

        limiter = new FilesDownloadLimiter(configViewFactory) {

            @Override
            protected void removeOldAccesses(ServerSession session, int contextId) {
                assertEquals(CONTEXT_ID, contextId);
            }

            @Override
            protected FileAccess getLimit(User user, int contextId) {
                long now = new Date().getTime();
                return new FileAccess(contextId, user.getId(), now - 300000, now, 222, 22222L);
            }

            @Override
            protected FileAccess getUsed(FileAccess limit) {
                long now = new Date().getTime();
                return new FileAccess(limit.getContextId(), limit.getUserId(), now - 300000, now, 4444, 1111L);
            }
        };

        limiter.onRequestInitialized(requestData);
    }

    @Test
    public void testOnRequestPerformed_exceptionThrown_doNothing() throws OXException {
        limiter.onRequestPerformed(requestData, null, new Exception("buh"));

        Mockito.verify(session, Mockito.never()).isAnonymous();
    }

    @Test
    public void testOnRequestPerformed_sessionAnonymous_doNothing() throws OXException {
        Mockito.when(session.getContextId()).thenReturn(CONTEXT_ID);
        Mockito.when(session.isAnonymous()).thenReturn(Boolean.TRUE);

        limiter = new FilesDownloadLimiter(configViewFactory) {

            @Override
            protected FileAccess getLimit(User user, int contextId) {
                fail();
                return null;
            }
        };

        limiter.onRequestPerformed(requestData, null, null);

        Mockito.verify(session, Mockito.times(1)).isAnonymous();
    }

    @Test
    public void testOnRequestPerformed_limitNull_doNothing() throws OXException {
        Mockito.when(session.getContextId()).thenReturn(CONTEXT_ID);

        limiter = new FilesDownloadLimiter(configViewFactory) {

            @Override
            protected FileAccess getLimit(User user, int contextId) {
                return null;
            }
        };

        limiter.onRequestPerformed(requestData, null, null);

        Mockito.verify(fileAccessStorage, Mockito.never()).addAccess(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyLong(), (Connection) Matchers.any());
    }

    @Test
    public void testOnRequestPerformed_limitDisabled_doNothing() throws OXException {
        Mockito.when(session.getContextId()).thenReturn(CONTEXT_ID);

        limiter = new FilesDownloadLimiter(configViewFactory) {

            @Override
            protected FileAccess getLimit(User user, int contextId) {
                return new FileAccess(contextId, user.getId(), 111L, 111L, 222, 22222L);
            }
        };

        limiter.onRequestPerformed(requestData, null, null);

        Mockito.verify(fileAccessStorage, Mockito.never()).addAccess(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyLong(), (Connection) Matchers.any());
    }

    @Test
    public void testOnRequestPerformed_resultTypeCommon_addAccess() throws OXException {
        Mockito.when(session.getContextId()).thenReturn(CONTEXT_ID);
        Mockito.when(session.getUserId()).thenReturn(USER_ID);

        AJAXRequestResult result = new AJAXRequestResult();
        result.setType(ResultType.COMMON);
        IFileHolder fileHolder = Mockito.mock(IFileHolder.class);
        Long length = 10000L;

        Mockito.when(fileHolder.getLength()).thenReturn(length);
        result.setResultObject(fileHolder);

        limiter = new FilesDownloadLimiter(this.configViewFactory) {

            @Override
            protected FileAccess getLimit(User user, int contextId) {
                long now = new Date().getTime();
                return new FileAccess(contextId, user.getId(), now - 300000, now, 222, 22222L);
            }
        };

        limiter.onRequestPerformed(requestData, result, null);

        Mockito.verify(fileAccessStorage, Mockito.times(1)).addAccess(CONTEXT_ID, USER_ID, length, connection);
    }

    @Test
    public void testOnRequestPerformed_resultTypeDirect_addAccess() throws OXException {
        Mockito.when(session.getContextId()).thenReturn(CONTEXT_ID);
        Mockito.when(session.getUserId()).thenReturn(USER_ID);

        AJAXRequestResult result = new AJAXRequestResult();
        result.setType(ResultType.DIRECT);
        Long length = 10000L;
        result.setResponseProperty("X-Content-Size", length);

        limiter = new FilesDownloadLimiter(this.configViewFactory) {

            @Override
            protected FileAccess getLimit(User user, int contextId) {
                long now = new Date().getTime();
                return new FileAccess(contextId, user.getId(), now - 300000, now, 222, 22222L);
            }
        };

        limiter.onRequestPerformed(requestData, result, null);

        Mockito.verify(fileAccessStorage, Mockito.times(1)).addAccess(CONTEXT_ID, USER_ID, length, connection);
    }
}
