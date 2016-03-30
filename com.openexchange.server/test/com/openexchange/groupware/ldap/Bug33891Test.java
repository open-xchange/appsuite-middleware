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

package com.openexchange.groupware.ldap;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import java.io.Serializable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
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
        }).when(mockedCache).get((Serializable) Matchers.any());
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
    public void tearDown() {
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
