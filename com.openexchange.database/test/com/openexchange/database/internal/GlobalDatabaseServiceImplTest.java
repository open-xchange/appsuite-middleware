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

package com.openexchange.database.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.test.mock.MockUtils;

/**
 * {@link GlobalDatabaseServiceImplTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ GlobalDbInit.class })
public class GlobalDatabaseServiceImplTest {

    @InjectMocks
    private GlobalDatabaseServiceImpl globalDatabaseServiceImpl;

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private final GlobalDbConfig config1 = new GlobalDbConfig("schema1", 11, 111);
    @Mock
    private final GlobalDbConfig config2 = new GlobalDbConfig("schema2", 22, 222);
    @Mock
    private final GlobalDbConfig config3 = new GlobalDbConfig("schema3", 33, 333);
    @Mock
    private final GlobalDbConfig config4 = new GlobalDbConfig("schema4", 44, 444);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(GlobalDbInit.class);
    }

     @Test
     public void testLoadGlobalDbConfigs_emptyCollectionBefore_readCompleteNewConfig() throws OXException {
        MockUtils.injectValueIntoPrivateField(globalDatabaseServiceImpl, "globalDbConfigs", new ConcurrentHashMap<String, GlobalDbConfig>());
        Map<String, GlobalDbConfig> newConfig = new ConcurrentHashMap<String, GlobalDbConfig>();
        newConfig.put("default", config1);
        newConfig.put("de", config2);
        newConfig.put("fr", config3);
        newConfig.put("es", config4);
        PowerMockito.when(GlobalDbInit.init((ConfigurationService) ArgumentMatchers.any(), (ConfigDatabaseServiceImpl) ArgumentMatchers.any(), (Pools) ArgumentMatchers.any(), (ReplicationMonitor) ArgumentMatchers.any())).thenReturn(newConfig);

        Map<String, GlobalDbConfig> reloadedConfig = globalDatabaseServiceImpl.loadGlobalDbConfigs(configurationService);

        Assert.assertEquals(newConfig.size(), reloadedConfig.size());
    }

     @Test
     public void testLoadGlobalDbConfigs_emptyCollectionBefore_verifyCorrectOnesReloaded() throws OXException {
        MockUtils.injectValueIntoPrivateField(globalDatabaseServiceImpl, "globalDbConfigs", new ConcurrentHashMap<String, GlobalDbConfig>());
        Map<String, GlobalDbConfig> newConfig = new ConcurrentHashMap<String, GlobalDbConfig>();
        newConfig.put("default", config1);
        newConfig.put("de", config2);
        newConfig.put("fr", config3);
        newConfig.put("es", config4);
        PowerMockito.when(GlobalDbInit.init((ConfigurationService) ArgumentMatchers.any(), (ConfigDatabaseServiceImpl) ArgumentMatchers.any(), (Pools) ArgumentMatchers.any(), (ReplicationMonitor) ArgumentMatchers.any())).thenReturn(newConfig);

        Map<String, GlobalDbConfig> reloadedConfig = globalDatabaseServiceImpl.loadGlobalDbConfigs(configurationService);

        verifyResult(reloadedConfig, newConfig);
    }

     @Test
     public void testLoadGlobalDbConfigs_twoInCollection_readAdditionalConfigsConfig() throws OXException {
        Map<String, GlobalDbConfig> existingConfig = new ConcurrentHashMap<String, GlobalDbConfig>();
        existingConfig.put("de", config2);
        existingConfig.put("fr", config3);
        MockUtils.injectValueIntoPrivateField(globalDatabaseServiceImpl, "globalDbConfigs", existingConfig);

        Map<String, GlobalDbConfig> newConfig = new ConcurrentHashMap<String, GlobalDbConfig>();
        newConfig.put("default", config1);
        newConfig.put("de", config2);
        newConfig.put("fr", config3);
        newConfig.put("es", config4);
        PowerMockito.when(GlobalDbInit.init((ConfigurationService) ArgumentMatchers.any(), (ConfigDatabaseServiceImpl) ArgumentMatchers.any(), (Pools) ArgumentMatchers.any(), (ReplicationMonitor) ArgumentMatchers.any())).thenReturn(newConfig);

        Map<String, GlobalDbConfig> reloadedConfig = globalDatabaseServiceImpl.loadGlobalDbConfigs(configurationService);

        Assert.assertEquals(newConfig.size(), reloadedConfig.size());
    }

     @Test
     public void testLoadGlobalDbConfigs_twoInCollection_verifyCorrectOnesReloaded() throws OXException {
        Map<String, GlobalDbConfig> existingConfig = new ConcurrentHashMap<String, GlobalDbConfig>();
        existingConfig.put("de", config2);
        existingConfig.put("fr", config3);
        MockUtils.injectValueIntoPrivateField(globalDatabaseServiceImpl, "globalDbConfigs", existingConfig);

        Map<String, GlobalDbConfig> newConfig = new ConcurrentHashMap<String, GlobalDbConfig>();
        newConfig.put("default", config1);
        newConfig.put("de", config2);
        newConfig.put("fr", config3);
        newConfig.put("es", config4);
        PowerMockito.when(GlobalDbInit.init((ConfigurationService) ArgumentMatchers.any(), (ConfigDatabaseServiceImpl) ArgumentMatchers.any(), (Pools) ArgumentMatchers.any(), (ReplicationMonitor) ArgumentMatchers.any())).thenReturn(newConfig);

        Map<String, GlobalDbConfig> reloadedConfig = globalDatabaseServiceImpl.loadGlobalDbConfigs(configurationService);

        verifyResult(reloadedConfig, newConfig);
    }

     @Test
     public void testLoadGlobalDbConfigs_fourInOriginCollection_readAndRemoveAdditionalConfigs() throws OXException {
        Map<String, GlobalDbConfig> existingConfig = new ConcurrentHashMap<String, GlobalDbConfig>();
        existingConfig.put("default", config1);
        existingConfig.put("de", config2);
        existingConfig.put("fr", config3);
        existingConfig.put("es", config4);
        MockUtils.injectValueIntoPrivateField(globalDatabaseServiceImpl, "globalDbConfigs", existingConfig);

        Map<String, GlobalDbConfig> newConfig = new ConcurrentHashMap<String, GlobalDbConfig>();
        newConfig.put("default", config1);
        newConfig.put("es", config4);
        PowerMockito.when(GlobalDbInit.init((ConfigurationService) ArgumentMatchers.any(), (ConfigDatabaseServiceImpl) ArgumentMatchers.any(), (Pools) ArgumentMatchers.any(), (ReplicationMonitor) ArgumentMatchers.any())).thenReturn(newConfig);

        Map<String, GlobalDbConfig> reloadedConfig =  globalDatabaseServiceImpl.loadGlobalDbConfigs(configurationService);

        Assert.assertEquals(newConfig.size(), reloadedConfig.size());
    }

     @Test
     public void testLoadGlobalDbConfigs_fourInOriginCollection_verifyCorrectOnesReloaded() throws OXException {
        Map<String, GlobalDbConfig> existingConfig = new ConcurrentHashMap<String, GlobalDbConfig>();
        existingConfig.put("default", config1);
        existingConfig.put("de", config2);
        existingConfig.put("fr", config3);
        existingConfig.put("es", config4);
        MockUtils.injectValueIntoPrivateField(globalDatabaseServiceImpl, "globalDbConfigs", existingConfig);

        Map<String, GlobalDbConfig> newConfig = new ConcurrentHashMap<String, GlobalDbConfig>();
        newConfig.put("default", config1);
        newConfig.put("es", config4);
        PowerMockito.when(GlobalDbInit.init((ConfigurationService) ArgumentMatchers.any(), (ConfigDatabaseServiceImpl) ArgumentMatchers.any(), (Pools) ArgumentMatchers.any(), (ReplicationMonitor) ArgumentMatchers.any())).thenReturn(newConfig);

        Map<String, GlobalDbConfig> reloadedConfig = globalDatabaseServiceImpl.loadGlobalDbConfigs(configurationService);

        verifyResult(reloadedConfig, newConfig);
    }

     @Test
     public void testLoadGlobalDbConfigs_fourInOriginCollection_removeOtherConfigs() throws OXException {
        Map<String, GlobalDbConfig> existingConfig = new ConcurrentHashMap<String, GlobalDbConfig>();
        existingConfig.put("default", config1);
        existingConfig.put("de", config2);
        existingConfig.put("fr", config3);
        existingConfig.put("es", config4);
        MockUtils.injectValueIntoPrivateField(globalDatabaseServiceImpl, "globalDbConfigs", existingConfig);

        Map<String, GlobalDbConfig> newConfig = new ConcurrentHashMap<String, GlobalDbConfig>();
        PowerMockito.when(GlobalDbInit.init((ConfigurationService) ArgumentMatchers.any(), (ConfigDatabaseServiceImpl) ArgumentMatchers.any(), (Pools) ArgumentMatchers.any(), (ReplicationMonitor) ArgumentMatchers.any())).thenReturn(newConfig);

        Map<String, GlobalDbConfig> reloadedConfig = globalDatabaseServiceImpl.loadGlobalDbConfigs(configurationService);

        Assert.assertEquals(newConfig.size(), reloadedConfig.size());
    }

     @Test
     public void testLoadGlobalDbConfigs_fourInOriginCollection_verifyNoOneReloaded() throws OXException {
        Map<String, GlobalDbConfig> existingConfig = new ConcurrentHashMap<String, GlobalDbConfig>();
        existingConfig.put("default", config1);
        existingConfig.put("de", config2);
        existingConfig.put("fr", config3);
        existingConfig.put("es", config4);
        MockUtils.injectValueIntoPrivateField(globalDatabaseServiceImpl, "globalDbConfigs", existingConfig);

        Map<String, GlobalDbConfig> newConfig = new ConcurrentHashMap<String, GlobalDbConfig>();
        PowerMockito.when(GlobalDbInit.init((ConfigurationService) ArgumentMatchers.any(), (ConfigDatabaseServiceImpl) ArgumentMatchers.any(), (Pools) ArgumentMatchers.any(), (ReplicationMonitor) ArgumentMatchers.any())).thenReturn(newConfig);

        Map<String, GlobalDbConfig> reloadedConfig = globalDatabaseServiceImpl.loadGlobalDbConfigs(configurationService);

        verifyResult(reloadedConfig, newConfig);
    }

    private void verifyResult(Map<String, GlobalDbConfig> reloadedConfig, Map<String, GlobalDbConfig> newConfig) throws OXException {
        for (String configName : newConfig.keySet()) {
            GlobalDbConfig globalDbConfig = reloadedConfig.get(configName);
            Assert.assertNotNull(globalDbConfig);
            Assert.assertEquals(newConfig.get(configName).toString(), globalDbConfig.toString());
        }
    }
}
