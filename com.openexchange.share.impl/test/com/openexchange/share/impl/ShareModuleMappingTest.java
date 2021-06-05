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

package com.openexchange.share.impl;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.config.ConfigurationService;
import com.openexchange.groupware.modules.Module;
import com.openexchange.share.impl.groupware.ShareModuleMapping;


/**
 * {@link ShareModuleMappingTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ConfigurationService.class })
public class ShareModuleMappingTest {

    private final static String MAPPING = "test1=100,test2=101,test3=999";

    private ConfigurationService configService;

    /**
     * Initializes a new {@link ShareModuleMappingTest}.
     */
    public ShareModuleMappingTest() {
        super();
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.configService = PowerMockito.mock(ConfigurationService.class);
        PowerMockito.when(configService.getProperty(ArgumentMatchers.anyString())).thenReturn("");
        PowerMockito.when(configService.getProperty("com.openexchange.share.modulemapping")).thenReturn(MAPPING);
        ShareModuleMapping.init(configService);
    }

     @Test
     public void testShareModuleMappingToInteger() {
        int test1 = ShareModuleMapping.moduleMapping2int("test1");
        int test2 = ShareModuleMapping.moduleMapping2int("test2");
        int test3 = ShareModuleMapping.moduleMapping2int("test3");
        int unbound = ShareModuleMapping.moduleMapping2int("unset_value");
        assertEquals("Unexpected value for test1.", 100, test1);
        assertEquals("Unexpected value for test2.", 101, test2);
        assertEquals("Unexpected value for test3.", 999, test3);
        assertEquals("Unexpected value for unbound.", -1, unbound);
    }

     @Test
     public void testShareModuleMappingToString() {
        String test1 = ShareModuleMapping.moduleMapping2String(100);
        String test2 = ShareModuleMapping.moduleMapping2String(101);
        String test3 = ShareModuleMapping.moduleMapping2String(999);
        String unbound = ShareModuleMapping.moduleMapping2String(Integer.MAX_VALUE);
        assertEquals("Unexpected value for test1.", "test1", test1);
        assertEquals("Unexpected value for test2.", "test2", test2);
        assertEquals("Unexpected value for test3.", "test3", test3);
        assertEquals("Unexpected value for unbound.", Module.UNBOUND.name(), unbound);
    }

}
