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

package com.openexchange.advertisement.impl.services;

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
    
    private int userId = 1;
    private int contextId = 1;
    
    @Before
    public void setUp() throws Exception{
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Services.class);
        PowerMockito.when(session.getContextId()).thenReturn(contextId);
        PowerMockito.when(session.getUserId()).thenReturn(userId);
        
        PowerMockito.when(Services.getService(CacheService.class)).thenReturn(cacheService);
        PowerMockito.when(cacheService.getCache(configService.CACHING_REGION)).thenReturn(userCache);
    }
    
    private AbstractAdvertisementConfigService configService = new AbstractAdvertisementConfigService() {
        
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
        PowerMockito.stub(PowerMockito.method(AbstractAdvertisementConfigService.class , "getConfigByUserInternal", Session.class)).toReturn(result);;
        JSONValue config = configService.getConfig(session);
        assertTrue(config != null);
    }
    
    @Test
    public void getConfigTest_withoutCacheInternal() throws Exception {
        PowerMockito.when(Services.getService(CacheService.class)).thenReturn(null);
        JSONValue result = new JSONObject();
        PowerMockito.stub(PowerMockito.method(AbstractAdvertisementConfigService.class , "getConfigByUserInternal", Session.class)).toReturn(null);;
        PowerMockito.stub(PowerMockito.method(AbstractAdvertisementConfigService.class , "getConfigInternal", Session.class, String.class, String.class)).toReturn(result);;
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
        PowerMockito.stub(PowerMockito.method(AbstractAdvertisementConfigService.class , "getConfigByUserInternal", Session.class)).toReturn(result);;
        JSONValue config = configService.getConfig(session);
        assertTrue(config != null);
    }
    
    @Test
    public void getConfigTest_normalCache() throws Exception {
        PowerMockito.when(userCache.newCacheKey(contextId, userId)).thenReturn(cacheKey);
        PowerMockito.when(userCache.newCacheKey(-1, configService.RESELLER_ALL, configService.PACKAGE_ALL)).thenReturn(cacheKey);
        JSONValue result = new JSONObject();
        CacheKey cacheKey2 = Mockito.mock(CacheKey.class);
        PowerMockito.when(userCache.get(cacheKey)).thenReturn(null);
        PowerMockito.when(userCache.get(cacheKey2)).thenReturn(result);
        PowerMockito.stub(PowerMockito.method(AbstractAdvertisementConfigService.class , "getConfigByUserInternal", Session.class)).toReturn(null);
        PowerMockito.stub(PowerMockito.method(AbstractAdvertisementConfigService.class , "getConfigInternal", Session.class, String.class, String.class)).toReturn(result);;
        JSONValue config = configService.getConfig(session);
        assertTrue(config != null);
    }
}
