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

package com.openexchange.download.limit.limiter;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
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
import org.mockito.ArgumentMatchers;
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
import com.openexchange.download.limit.FileAccess;
import com.openexchange.download.limit.internal.Services;
import com.openexchange.download.limit.storage.RdbFileAccessStorage;
import com.openexchange.download.limit.util.LimitConfig;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.java.Autoboxing;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

/**
 * {@link AnonymousGuestDownloadLimiterTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Services.class, RdbFileAccessStorage.class, LimitConfig.class })
public class FilesDownloadLimiterTest {

    static int CONTEXT_ID = 1;
    private static int USER_ID = 77;
    private static int DEFAULT_TIME_FRAME_LINKS = 0;

    private FilesDownloadLimiter limiter;

    @Mock
    private ConfigViewFactory configViewFactory;

    @Mock
    private RdbFileAccessStorage fileAccessStorage;

    @Mock
    private LimitConfig config;

    @Mock
    private ConfigView configView;

    private final UserImpl linkUser = new UserImpl();

    private final UserImpl guest = new UserImpl();

    private final UserImpl internal = new UserImpl();

    private final AJAXRequestData requestData = new AJAXRequestData();

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
        Mockito.when(Autoboxing.valueOf(session.isAnonymous())).thenReturn(Boolean.FALSE);
        Mockito.when(session.getUser()).thenReturn(linkUser);

        PowerMockito.mockStatic(LimitConfig.class);
        PowerMockito.when(LimitConfig.getInstance()).thenReturn(config);
        Mockito.when(Autoboxing.valueOf(config.isEnabled())).thenReturn(Boolean.TRUE);

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
        Mockito.when(configView.opt(ArgumentMatchers.anyString(), ArgumentMatchers.<Class<Integer>> any(), ArgumentMatchers.<Integer> any())).thenReturn(I(DEFAULT_TIME_FRAME_LINKS));

        limiter.dropObsoleteAccesses(linkUser, CONTEXT_ID);

        Mockito.verify(configView, Mockito.times(1)).opt(LimitConfig.TIME_FRAME_LINKS, Integer.class, I(DEFAULT_TIME_FRAME_LINKS));
        Mockito.verify(fileAccessStorage, Mockito.times(1)).removeAccesses(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyLong(), (Connection) ArgumentMatchers.any());
        Mockito.verify(connection, Mockito.times(1)).commit();
    }

    @Test
    public void testDropObsoleteAccesses_isGuest_usedConfigCascadeAndCommitedConnection() throws OXException, SQLException {
        Mockito.when(configView.opt(ArgumentMatchers.anyString(), ArgumentMatchers.<Class<Integer>> any(), ArgumentMatchers.<Integer> any())).thenReturn(I(DEFAULT_TIME_FRAME_LINKS));

        limiter.dropObsoleteAccesses(guest, CONTEXT_ID);

        Mockito.verify(configView, Mockito.times(1)).opt(LimitConfig.TIME_FRAME_GUESTS, Integer.class, I(DEFAULT_TIME_FRAME_LINKS));
        Mockito.verify(fileAccessStorage, Mockito.times(1)).removeAccesses(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyLong(), (Connection) ArgumentMatchers.any());
        Mockito.verify(connection, Mockito.times(1)).commit();
    }

    @Test
    public void testDropObsoleteAccesses_noGuest_doNothing() throws OXException, SQLException {
        limiter.dropObsoleteAccesses(internal, CONTEXT_ID);

        Mockito.verify(configView, Mockito.never()).opt(LimitConfig.TIME_FRAME_LINKS, Integer.class, I(DEFAULT_TIME_FRAME_LINKS));
        Mockito.verify(fileAccessStorage, Mockito.never()).removeAccesses(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyLong(), (Connection) ArgumentMatchers.any());
        Mockito.verify(connection, Mockito.never()).commit();
    }

    @Test
    public void testGetLimit_noGuest_returnNull() {
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
        Long sizeLimit = L(LimitConfig.getInstance().sizeLimitLinks());
        Mockito.when(configView.opt(LimitConfig.SIZE_LIMIT_LINKS, Long.class, sizeLimit)).thenReturn(sizeLimit);
        Integer countLimit = I(LimitConfig.getInstance().countLimitLinks());
        Mockito.when(configView.opt(LimitConfig.COUNT_LIMIT_LINKS, Integer.class, countLimit)).thenReturn(countLimit);
        Integer timeFrame = I(LimitConfig.getInstance().timeFrameLinks());
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
        Long sizeLimit = L(LimitConfig.getInstance().sizeLimitLinks());
        Mockito.when(configView.opt(LimitConfig.SIZE_LIMIT_LINKS, Long.class, sizeLimit)).thenReturn(new Long(22222222222L));
        Integer countLimit = I(LimitConfig.getInstance().countLimitLinks());
        Mockito.when(configView.opt(LimitConfig.COUNT_LIMIT_LINKS, Integer.class, countLimit)).thenReturn(new Integer(111111));
        Integer timeFrame = I(LimitConfig.getInstance().timeFrameLinks());
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
        Long sizeLimit = L(LimitConfig.getInstance().sizeLimitGuests());
        Mockito.when(configView.opt(LimitConfig.SIZE_LIMIT_GUESTS, Long.class, sizeLimit)).thenReturn(sizeLimit);
        Integer countLimit = I(LimitConfig.getInstance().countLimitGuests());
        Mockito.when(configView.opt(LimitConfig.COUNT_LIMIT_GUESTS, Integer.class, countLimit)).thenReturn(countLimit);
        Integer timeFrame = I(LimitConfig.getInstance().timeFrameGuests());
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
        Long sizeLimit = L(LimitConfig.getInstance().sizeLimitGuests());
        Mockito.when(configView.opt(LimitConfig.SIZE_LIMIT_GUESTS, Long.class, sizeLimit)).thenReturn(new Long(22222222222L));
        Integer countLimit = I(LimitConfig.getInstance().countLimitGuests());
        Mockito.when(configView.opt(LimitConfig.COUNT_LIMIT_GUESTS, Integer.class, countLimit)).thenReturn(new Integer(111111));
        Integer timeFrame = I(LimitConfig.getInstance().timeFrameGuests());
        Mockito.when(configView.opt(LimitConfig.TIME_FRAME_GUESTS, Integer.class, timeFrame)).thenReturn(new Integer(7));

        FileAccess limit = limiter.getLimit(guest, CONTEXT_ID);

        assertNotNull(limit);
        assertEquals(22222222222L, limit.getSize());
        assertEquals(111111, limit.getCount());
        assertEquals(7, limit.getTimeOfEndInMillis() - limit.getTimeOfStartInMillis());
    }

    @Test
    public void testApplicable_wrongModule_notApplicable() {
        requestData.setModule("apps/manifests");

        boolean applicable = limiter.applicable(requestData);

        assertFalse(applicable);
    }

    @Test
    public void testApplicable_wrongAction_notApplicable() {
        requestData.setAction("all");

        boolean applicable = limiter.applicable(requestData);

        assertFalse(applicable);
    }

    @Test
    public void testApplicable_isAnonymousSession_notApplicable() {
        Mockito.when(Autoboxing.valueOf(session.isAnonymous())).thenReturn(Boolean.TRUE);

        boolean applicable = limiter.applicable(requestData);

        assertFalse(applicable);
        Mockito.verify(session, Mockito.times(1)).isAnonymous();
    }

    @Test
    public void testApplicable_isNotGuest_notApplicable() {
        Mockito.when(session.getUser()).thenReturn(internal);

        boolean applicable = limiter.applicable(requestData);

        assertFalse(applicable);
    }

    @Test
    public void testApplicableWithDocumentAction_isGuestButNoDelivery_notApplicable() {
        Mockito.when(session.getUser()).thenReturn(guest);

        boolean applicable = limiter.applicable(requestData);

        assertFalse(applicable);
    }

    @Test
    public void testApplicableWithDocumentAction_isGuestAndDelivery_applicable() {
        Mockito.when(session.getUser()).thenReturn(guest);
        requestData.putParameter("delivery", "download");

        boolean applicable = limiter.applicable(requestData);

        assertTrue(applicable);
    }

    @Test
    public void testApplicableWithDocumentAction_isGuestAndDL_applicable() {
        Mockito.when(session.getUser()).thenReturn(guest);
        requestData.putParameter("dl", "1");

        boolean applicable = limiter.applicable(requestData);

        assertTrue(applicable);
    }

    @Test
    public void testApplicable_isGuest_isApplicable() {
        requestData.setAction("zipdocuments");

        boolean applicable = limiter.applicable(requestData);

        assertTrue(applicable);
    }

    @Test
    public void testOnRequestInitialized_noLimitAvailable_returnWithoutCheck() throws OXException {
        Mockito.when(I(session.getContextId())).thenReturn(I(CONTEXT_ID));

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
        Mockito.when(I(session.getContextId())).thenReturn(I(CONTEXT_ID));

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
        Mockito.when(I(session.getContextId())).thenReturn(I(CONTEXT_ID));

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
            protected void throwIfExceeded(FileAccess limit, FileAccess used) throws OXException {
                fail();
            }
        };

        limiter.onRequestInitialized(requestData);
    }

    @Test
    public void testOnRequestInitialized_notExceeded_return() throws OXException {
        Mockito.when(I(session.getContextId())).thenReturn(I(CONTEXT_ID));

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

    @Test(expected = OXException.class)
    public void testOnRequestInitialized_sizeExceeded_throwException() throws OXException {
        Mockito.when(I(session.getContextId())).thenReturn(I(CONTEXT_ID));

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

    @Test(expected = OXException.class)
    public void testOnRequestInitialized_countExceeded_throwException() throws OXException {
        Mockito.when(I(session.getContextId())).thenReturn(I(CONTEXT_ID));

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
    public void testOnRequestPerformed_exceptionThrown_doNothing() {
        limiter.onRequestPerformed(requestData, null, new Exception("buh"));

        Mockito.verify(session, Mockito.never()).isAnonymous();
    }

    @Test
    public void testOnRequestPerformed_limitNull_doNothing() throws OXException {
        Mockito.when(I(session.getContextId())).thenReturn(I(CONTEXT_ID));

        limiter = new FilesDownloadLimiter(configViewFactory) {

            @Override
            protected FileAccess getLimit(User user, int contextId) {
                return null;
            }
        };

        limiter.onRequestPerformed(requestData, null, null);

        Mockito.verify(fileAccessStorage, Mockito.never()).addAccess(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyLong(), (Connection) ArgumentMatchers.any());
    }

    @Test
    public void testOnRequestPerformed_limitDisabled_doNothing() throws OXException {
        Mockito.when(I(session.getContextId())).thenReturn(I(CONTEXT_ID));

        limiter = new FilesDownloadLimiter(configViewFactory) {

            @Override
            protected FileAccess getLimit(User user, int contextId) {
                return new FileAccess(contextId, user.getId(), 111L, 111L, 222, 22222L);
            }
        };

        limiter.onRequestPerformed(requestData, null, null);

        Mockito.verify(fileAccessStorage, Mockito.never()).addAccess(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyLong(), (Connection) ArgumentMatchers.any());
    }

    @Test
    public void testOnRequestPerformed_resultTypeCommon_addAccess() throws OXException {
        Mockito.when(I(session.getContextId())).thenReturn(I(CONTEXT_ID));
        Mockito.when(I(session.getUserId())).thenReturn(I(USER_ID));

        AJAXRequestResult result = new AJAXRequestResult();
        result.setType(ResultType.COMMON);
        IFileHolder fileHolder = Mockito.mock(IFileHolder.class);
        Long length = L(10000L);

        Mockito.when(L(fileHolder.getLength())).thenReturn(length);
        result.setResultObject(fileHolder);

        limiter = new FilesDownloadLimiter(this.configViewFactory) {

            @Override
            protected FileAccess getLimit(User user, int contextId) {
                long now = new Date().getTime();
                return new FileAccess(contextId, user.getId(), now - 300000, now, 222, 22222L);
            }
        };

        limiter.onRequestPerformed(requestData, result, null);

        Mockito.verify(fileAccessStorage, Mockito.times(1)).addAccess(CONTEXT_ID, USER_ID, length.longValue(), connection);
    }

    @Test
    public void testOnRequestPerformed_resultTypeDirect_addAccess() throws OXException {
        Mockito.when(I(session.getContextId())).thenReturn(I(CONTEXT_ID));
        Mockito.when(I(session.getUserId())).thenReturn(I(USER_ID));

        AJAXRequestResult result = new AJAXRequestResult();
        result.setType(ResultType.DIRECT);
        Long length = L(10000L);
        result.setResponseProperty("X-Content-Size", length);

        limiter = new FilesDownloadLimiter(this.configViewFactory) {

            @Override
            protected FileAccess getLimit(User user, int contextId) {
                long now = new Date().getTime();
                return new FileAccess(contextId, user.getId(), now - 300000, now, 222, 22222L);
            }
        };

        limiter.onRequestPerformed(requestData, result, null);

        Mockito.verify(fileAccessStorage, Mockito.times(1)).addAccess(CONTEXT_ID, USER_ID, length.longValue(), connection);
    }
}
