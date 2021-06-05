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

package com.openexchange.advertisement.impl.services;

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertTrue;
import org.json.JSONObject;
import org.json.JSONValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.advertisement.AdvertisementConfigService;
import com.openexchange.advertisement.impl.osgi.Services;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 *
 * {@link AbstractAdvertisementConfigServiceTest}
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.8.4
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Session.class, Services.class, CacheService.class, AbstractAdvertisementConfigService.class} )
public class AbstractAdvertisementConfigServiceTest {

    @Mock
    Session session;
    @Mock
    CacheService cacheService;
    @Mock
    Cache userCache;
    @Mock
    CacheKey cacheKey;

    private final int userId = 1;
    private final int contextId = 1;

    @Before
    public void setUp() throws Exception{
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Services.class);
        PowerMockito.when(I(session.getContextId())).thenReturn(I(contextId));
        PowerMockito.when(I(session.getUserId())).thenReturn(I(userId));

        PowerMockito.when(Services.getService(CacheService.class)).thenReturn(cacheService);
        PowerMockito.when(cacheService.getCache(AbstractAdvertisementConfigService.CACHING_REGION)).thenReturn(userCache);
    }

    private final AbstractAdvertisementConfigService configService = new AbstractAdvertisementConfigService() {

        @Override
        public String getSchemeId() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        protected String getReseller(int contextId) throws OXException {
            return RESELLER_ALL;
        }

        @Override
        protected String getPackage(Session session) throws OXException {
            return PACKAGE_ALL;
        }
    };

    @Test
    public void getConfigTest_withoutCacheUserInternal() throws Exception {
        PowerMockito.when(Services.getService(CacheService.class)).thenReturn(null);
        JSONValue result = new JSONObject();
        PowerMockito.stub(PowerMockito.method(AbstractAdvertisementConfigService.class , "getConfigByUserInternal", Session.class)).toReturn(result);
        JSONValue config = configService.getConfig(session);
        assertTrue(config != null);
    }

    @Test
    public void getConfigTest_withoutCacheInternal() throws Exception {
        PowerMockito.when(Services.getService(CacheService.class)).thenReturn(null);
        JSONValue result = new JSONObject();
        PowerMockito.stub(PowerMockito.method(AbstractAdvertisementConfigService.class , "getConfigByUserInternal", Session.class)).toReturn(null);
        PowerMockito.stub(PowerMockito.method(AbstractAdvertisementConfigService.class , "getConfigInternal", Session.class, String.class, String.class)).toReturn(result);
        JSONValue config = configService.getConfig(session);
        assertTrue(config != null);
    }

    @Test
    public void getConfigTest_userCache() throws Exception {
        PowerMockito.when(userCache.newCacheKey(contextId, userId)).thenReturn(cacheKey);
        JSONValue result = new JSONObject();
        PowerMockito.when(userCache.get(cacheKey)).thenReturn(result);
        JSONValue config = configService.getConfig(session);
        assertTrue(config != null);
    }

    @Test
    public void getConfigTest_fromDatabase() throws Exception {
        PowerMockito.when(userCache.newCacheKey(contextId, userId)).thenReturn(cacheKey);
        JSONValue result = new JSONObject();
        PowerMockito.when(userCache.get(cacheKey)).thenReturn(null);
        PowerMockito.stub(PowerMockito.method(AbstractAdvertisementConfigService.class , "getConfigByUserInternal", Session.class)).toReturn(result);
        JSONValue config = configService.getConfig(session);
        assertTrue(config != null);
    }

    @Test
    public void getConfigTest_normalCache() throws Exception {
        PowerMockito.when(userCache.newCacheKey(contextId, userId)).thenReturn(cacheKey);
        PowerMockito.when(userCache.newCacheKey(-1, AdvertisementConfigService.RESELLER_ALL, AdvertisementConfigService.PACKAGE_ALL)).thenReturn(cacheKey);
        JSONValue result = new JSONObject();
        CacheKey cacheKey2 = Mockito.mock(CacheKey.class);
        PowerMockito.when(userCache.get(cacheKey)).thenReturn(null);
        PowerMockito.when(userCache.get(cacheKey2)).thenReturn(result);
        PowerMockito.stub(PowerMockito.method(AbstractAdvertisementConfigService.class , "getConfigByUserInternal", Session.class)).toReturn(null);
        PowerMockito.stub(PowerMockito.method(AbstractAdvertisementConfigService.class , "getConfigInternal", Session.class, String.class, String.class)).toReturn(result);
        JSONValue config = configService.getConfig(session);
        assertTrue(config != null);
    }
}
