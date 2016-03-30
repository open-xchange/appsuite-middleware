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

package com.openexchange.share.impl;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
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
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.configService = PowerMockito.mock(ConfigurationService.class);
        PowerMockito.when(configService.getProperty(Matchers.anyString())).thenReturn("");
        PowerMockito.when(configService.getProperty("com.openexchange.share.modulemapping")).thenReturn(MAPPING);
        ShareModuleMapping.init(configService);
    }

    @Test
    public void testShareModuleMappingToInteger() throws Exception {
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
    public void testShareModuleMappingToString() throws Exception {
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
