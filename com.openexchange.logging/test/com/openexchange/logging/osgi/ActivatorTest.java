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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.logging.osgi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.openexchange.test.mock.MockUtils;
/**
 * Unit tests for {@link com.openexchange.logging.osgi.Activator}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4.2
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ LoggerFactory.class, LoggerContext.class, Logger.class })
public class ActivatorTest {

    @InjectMocks
    private Activator activator;

    @Mock
    private LoggerContext loggerContext;

    @Mock
    private Logger logger;

    @Mock
    private Logger activatorLogger;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(LoggerFactory.class);

        PowerMockito.when(loggerContext.getLogger(Matchers.anyString())).thenReturn(logger);

        MockUtils.injectValueIntoPrivateField(Activator.class, "logger", activatorLogger);
        MockUtils.injectValueIntoPrivateField(this.activator, "loggerContext", loggerContext);
    }

    @Test
    public void testOverrideLoggerLevels_loggerNotAvailable_LogWarning() {
        PowerMockito.when(loggerContext.getLogger(Matchers.anyString())).thenReturn(null);

        activator.overrideLoggerLevels();

        Mockito.verify(activatorLogger, Mockito.atLeast(1)).warn(Matchers.anyString());
        Mockito.verify(loggerContext, Mockito.atLeast(1)).getLogger(Matchers.anyString());
    }

    @Test
    public void testOverrideLoggerLevels_contextAndLoggerAvailable_SetNewLogLevel() {
        activator.overrideLoggerLevels();

        Mockito.verify(logger, Mockito.atLeast(1)).setLevel(Level.INFO);
        Mockito.verify(loggerContext, Mockito.atLeast(1)).getLogger(Matchers.anyString());
        Mockito.verify(activatorLogger, Mockito.never()).warn(Matchers.anyString());
    }

    @Test
    public void testStartBundle_ensureOverrideLoggerLevelsCalled_Successfull() throws Exception {
        Activator activatorSpy = Mockito.spy(new Activator());
        Mockito.doNothing().when(activatorSpy).overrideLoggerLevels();
        Mockito.doNothing().when(activatorSpy).configureJavaUtilLogging();
        Mockito.doNothing().when(activatorSpy).registerLoggingConfigurationMBean();
        Mockito.doNothing().when(activatorSpy).addExceptionCategoryFilter();

        activatorSpy.startBundle();

        Mockito.verify(activatorSpy, Mockito.times(1)).overrideLoggerLevels();
    }
}
