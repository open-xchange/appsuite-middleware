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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.groupware.userconfiguration;

import static org.junit.Assert.fail;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.userconfiguration.internal.PermissionConfigurationCheckerImpl;

/**
 * {@link PermissionConfigurationCheckerTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
@RunWith(Parameterized.class)
public class PermissionConfigurationCheckerTest {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {{ Boolean.TRUE }, { Boolean.FALSE}});
    }

    private PermissionConfigurationChecker checker;
    private boolean config;


    /**
     * Initializes a new {@link PermissionConfigurationCheckerTest}.
     */
    public PermissionConfigurationCheckerTest(Boolean config) {
        super();
        ConfigurationService mock = Mockito.mock(ConfigurationService.class, new Answer<Boolean>() {

            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return config;
            }
        });
        this.config = config.booleanValue();
        checker = new PermissionConfigurationCheckerImpl(mock);

    }

    @Test
    public void testChecker() {

        Map<String, String> map = new HashMap<>();
        map.put("com.openexchange.capability.infostore", "true");
        map.put("com.openexchange.capability.contacts", "true");
        try {
            checker.checkAttributes(map);
            if (!config) {
                fail();
            }
        } catch (@SuppressWarnings("unused") OXException e) {
            if (config) {
                fail();
            }
        }

        map = new HashMap<>();
        map.put("com.openexchange.capability.infostore", null);
        try {
            // Should always work
            checker.checkAttributes(map);
        } catch (@SuppressWarnings("unused") OXException e) {
            fail();
        }

        try {
            checker.checkCapabilities(Collections.singleton("infostore"));
            if (!config) {
                fail();
            }
        } catch (@SuppressWarnings("unused") OXException e) {
            if (config) {
                fail();
            }
        }
    }

}
