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

package com.openexchange.logback.extensions;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import ch.qos.logback.classic.PatternLayout;
import com.openexchange.test.mock.MockUtils;


/**
 * {@link SyslogPatternLayoutActivatorTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PatternLayout.class })
public class SyslogPatternLayoutActivatorTest {

    private SyslogPatternLayoutActivator syslogPatternLayoutActivator;


    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testAddPatternLayout_notInMap_addToMap() {
        Map<String, String> defaultConverterMap = new HashMap<String, String>();

        MockUtils.injectValueIntoPrivateField(PatternLayout.class, "defaultConverterMap", defaultConverterMap);

        syslogPatternLayoutActivator = new SyslogPatternLayoutActivator();

        Assert.assertTrue(defaultConverterMap.containsKey(ExtendedPatternLayoutEncoder.EREPLACE));
        Assert.assertTrue(defaultConverterMap.containsKey(ExtendedPatternLayoutEncoder.TID));
        Assert.assertTrue(defaultConverterMap.containsKey(ExtendedPatternLayoutEncoder.LMDC));
        Assert.assertTrue(defaultConverterMap.containsKey(ExtendedPatternLayoutEncoder.SAN));
    }

    @Test
    public void testAddPatternLayout_notInMap_addToMapWithCorrectInstance() {
        Map<String, String> defaultConverterMap = new HashMap<String, String>();

        MockUtils.injectValueIntoPrivateField(PatternLayout.class, "defaultConverterMap", defaultConverterMap);

        syslogPatternLayoutActivator = new SyslogPatternLayoutActivator();

        Assert.assertEquals("com.openexchange.logback.extensions.ExtendedReplacingCompositeConverter", defaultConverterMap.get(ExtendedPatternLayoutEncoder.EREPLACE));
        Assert.assertEquals("com.openexchange.logback.extensions.ThreadIdConverter", defaultConverterMap.get(ExtendedPatternLayoutEncoder.TID));
        Assert.assertEquals("com.openexchange.logback.extensions.LineMDCConverter", defaultConverterMap.get(ExtendedPatternLayoutEncoder.LMDC));
        Assert.assertEquals("com.openexchange.logback.extensions.LogSanitisingConverter", defaultConverterMap.get(ExtendedPatternLayoutEncoder.SAN));
    }

    @Test
    public void testAddPatternLayout_alreadyInMap_nothingToAdd() {
        Map<String, String> defaultConverterMap = new HashMap<String, String>();
        defaultConverterMap.put(ExtendedPatternLayoutEncoder.LMDC, LineMDCConverter.class.getName());
        defaultConverterMap.put(ExtendedPatternLayoutEncoder.EREPLACE, ExtendedReplacingCompositeConverter.class.getName());
        defaultConverterMap.put(ExtendedPatternLayoutEncoder.TID, ThreadIdConverter.class.getName());
        defaultConverterMap.put(ExtendedPatternLayoutEncoder.SAN, LogSanitisingConverter.class.getName());
        Map<String, String> spy = PowerMockito.spy(defaultConverterMap);

        MockUtils.injectValueIntoPrivateField(PatternLayout.class, "defaultConverterMap", spy);

        syslogPatternLayoutActivator = new SyslogPatternLayoutActivator();

        Mockito.verify(spy, Mockito.never()).put(Matchers.anyString(), Matchers.anyString());
    }
}
