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

package com.openexchange.groupware.ldap;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import java.io.Serializable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * Verifies that the IMAP server attribute of a {@link User} can be updated through {@link UserService}.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @since 7.6.1
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ServerServiceRegistry.class)
public class Bug33891Test {

    CacheKey mockedCacheKey;
    Cache mockedCache;
    CacheService mockedCacheService;
    ServerServiceRegistry mockedServiceRegistry;
    private RdbUserStorage mockedUserStorage;
    private CachingUserStorage cachingUserStorage;
    private User mockedUser;

    @Before
    public void setUp() throws OXException {
        mockedCacheKey = mock(CacheKey.class);
        doAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) {
                return Integer.valueOf(1);
            }
        }).when(mockedCacheKey).getContextId();
        doAnswer(new Answer<String[]>() {
            @Override
            public String[] answer(InvocationOnMock invocation) {
                return new String[] { String.valueOf(1) };
            }
        }).when(mockedCacheKey).getKeys();
        mockedCache = mock(Cache.class);
        doAnswer(new Answer<CacheKey>() {
            @Override
            public CacheKey answer(InvocationOnMock invocation) {
                return mockedCacheKey;
            }
        }).when(mockedCache).newCacheKey(1, 1);
        doAnswer(new Answer<User>() {
            @Override
            public User answer(InvocationOnMock invocation) {
                UserImpl retval = new UserImpl();
                retval.setImapServer("oldValue");
                return retval;
            }
        }).when(mockedCache).get((Serializable) ArgumentMatchers.any());
        mockedCacheService = mock(CacheService.class);
        doAnswer(new Answer<Cache>() {
            @Override
            public Cache answer(InvocationOnMock invocation) {
                return mockedCache;
            }
        }).when(mockedCacheService).getCache(CachingUserStorage.REGION_NAME);
        PowerMockito.mockStatic(ServerServiceRegistry.class);
        mockedServiceRegistry = mock(ServerServiceRegistry.class);
        PowerMockito.when(ServerServiceRegistry.getInstance()).thenReturn(mockedServiceRegistry);
        doAnswer(new Answer<CacheService>() {
            @Override
            public CacheService answer(InvocationOnMock invocation) {
                return mockedCacheService;
            }
        }).when(mockedServiceRegistry).getService(CacheService.class);
        mockedUserStorage = mock(RdbUserStorage.class);
        cachingUserStorage = new CachingUserStorage(mockedUserStorage);
        mockedUser = mock(User.class);
        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return "newValue";
            }
        }).when(mockedUser).getImapServer();
    }

    @After
    public void tearDown()
 {
        cachingUserStorage = null;
        mockedUserStorage = null;
        mockedCacheService = null;
        mockedCache = null;
        mockedCacheKey = null;
    }

     @Test
     public void test() throws OXException {
        SimContext simContext = new SimContext(1);
        cachingUserStorage.updateUserInternal(null, mockedUser, simContext);
        Mockito.verify(mockedUserStorage).updateUser(mockedUser, simContext);
    }
}
