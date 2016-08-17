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

package com.openexchange.logging.osgi;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.classic.spi.TurboFilterList;
import com.openexchange.logging.filter.RankingAwareTurboFilterList;
import com.openexchange.logging.mbean.IncludeStackTraceServiceImpl;
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

        Mockito.when(loggerContext.getLogger(Mockito.anyString())).thenReturn(logger);
        PowerMockito.when(loggerContext.getProperty(Matchers.anyString())).thenReturn(null);

        Mockito.when(LoggerFactory.getILoggerFactory()).thenReturn(loggerContext);

        MockUtils.injectValueIntoPrivateField(Activator.class, "LOGGER", activatorLogger);
    }

    @Test
    public void testOverrideLoggerLevels_loggerNotAvailable_logWarning() {
        PowerMockito.when(loggerContext.getLogger(Matchers.anyString())).thenReturn(null);

        Mockito.mock(LoggerContext.class);

        activator.overrideLoggerLevels(loggerContext);

        Mockito.verify(loggerContext, Mockito.atLeast(1)).getLogger(Matchers.anyString());
        Mockito.verify(activatorLogger, Mockito.atLeast(1)).warn(Matchers.anyString(), Matchers.any());
    }

    @Test
    public void testOverrideLoggerLevels_configuredLevelWARNTooCoarse_setNewLogLevel() {
        Mockito.when(logger.getLevel()).thenReturn(Level.WARN);

        activator.overrideLoggerLevels(loggerContext);

        Mockito.verify(logger, Mockito.atLeast(1)).setLevel(Level.INFO);
        Mockito.verify(loggerContext, Mockito.atLeast(1)).getLogger(Matchers.anyString());
        Mockito.verify(activatorLogger, Mockito.never()).warn(Matchers.anyString());
    }

    @Test
    public void testOverrideLoggerLevels_configuredLevelNull_setNewLogLevel() {
        Mockito.when(logger.getLevel()).thenReturn(null);

        activator.overrideLoggerLevels(loggerContext);

        Mockito.verify(logger, Mockito.atLeast(1)).setLevel(Level.INFO);
        Mockito.verify(loggerContext, Mockito.atLeast(1)).getLogger(Matchers.anyString());
        Mockito.verify(activatorLogger, Mockito.never()).warn(Matchers.anyString());
    }

    @Test
    public void testOverrideLoggerLevels_configuredLevelOFFTooCoarse_setNewLogLevel() {
        Mockito.when(logger.getLevel()).thenReturn(Level.OFF);

        activator.overrideLoggerLevels(loggerContext);

        Mockito.verify(logger, Mockito.atLeast(1)).setLevel(Level.INFO);
        Mockito.verify(loggerContext, Mockito.atLeast(1)).getLogger(Matchers.anyString());
        Mockito.verify(activatorLogger, Mockito.never()).warn(Matchers.anyString());
    }

    @Test
    public void testOverrideLoggerLevels_configuredLevelINFOAdequate_doNotSetNewLogLevel() {
        Mockito.when(logger.getLevel()).thenReturn(Level.INFO);

        activator.overrideLoggerLevels(loggerContext);

        Mockito.verify(logger, Mockito.never()).setLevel(Level.INFO);
        Mockito.verify(loggerContext, Mockito.atLeast(1)).getLogger(Matchers.anyString());
        Mockito.verify(activatorLogger, Mockito.never()).warn(Matchers.anyString());
    }

    @Test
    public void testOverrideLoggerLevels_configuredLevelALLAdequate_doNotSetNewLogLevel() {
        Mockito.when(logger.getLevel()).thenReturn(Level.ALL);

        activator.overrideLoggerLevels(loggerContext);

        Mockito.verify(logger, Mockito.never()).setLevel(Level.INFO);
        Mockito.verify(loggerContext, Mockito.atLeast(1)).getLogger(Matchers.anyString());
        Mockito.verify(activatorLogger, Mockito.never()).warn(Matchers.anyString());
    }

    @Test
    public void testOverrideLoggerLevels_disableOverrideLogLevels_returnWithOverriding() {
        Mockito.when(logger.getLevel()).thenReturn(Level.OFF);
        PowerMockito.when(loggerContext.getProperty(Matchers.anyString())).thenReturn("true");

        activator.overrideLoggerLevels(loggerContext);

        Mockito.verify(logger, Mockito.never()).setLevel(Level.INFO);
        Mockito.verify(loggerContext, Mockito.never()).getLogger(Matchers.anyString());
        Mockito.verify(activatorLogger, Mockito.never()).warn(Matchers.anyString());
    }

    @Test
    public void testStartBundle_ensureOverrideLoggerLevelsCalled_successfull() throws Exception {
        BundleContext bundleContext = PowerMockito.mock(BundleContext.class);
        Bundle bundle = PowerMockito.mock(Bundle.class);
        Mockito.when(bundleContext.getBundle()).thenReturn(bundle);
        PowerMockito.when(loggerContext.getTurboFilterList()).thenReturn(new TurboFilterList());

        Activator activatorSpy = Mockito.spy(activator);
        Mockito.doNothing().when(activatorSpy).overrideLoggerLevels(loggerContext);
        Mockito.doNothing().when(activatorSpy).configureJavaUtilLogging();
        Mockito.doNothing().when(activatorSpy).installJulLevelChangePropagator(loggerContext);
        Mockito.doNothing().when(activatorSpy).registerLoggingConfigurationMBean(Matchers.eq(bundleContext), Mockito.eq(loggerContext), (RankingAwareTurboFilterList) Mockito.any(), (IncludeStackTraceServiceImpl) Mockito.any());

        activatorSpy.start(bundleContext);

        Mockito.verify(activatorSpy, Mockito.times(1)).overrideLoggerLevels(loggerContext);
    }

    @Test
    public void testStartBundle_ensureInstallJulLevelChangePropagatorCalled_successfull() throws Exception {
        BundleContext bundleContext = PowerMockito.mock(BundleContext.class);
        Bundle bundle = PowerMockito.mock(Bundle.class);
        Mockito.when(bundleContext.getBundle()).thenReturn(bundle);
        PowerMockito.when(loggerContext.getTurboFilterList()).thenReturn(new TurboFilterList());

        Activator activatorSpy = Mockito.spy(activator);
        Mockito.doNothing().when(activatorSpy).overrideLoggerLevels(loggerContext);
        Mockito.doNothing().when(activatorSpy).configureJavaUtilLogging();
        Mockito.doNothing().when(activatorSpy).installJulLevelChangePropagator(loggerContext);
        Mockito.doNothing().when(activatorSpy).registerLoggingConfigurationMBean(Matchers.eq(bundleContext), Mockito.eq(loggerContext), (RankingAwareTurboFilterList) Mockito.any(), (IncludeStackTraceServiceImpl) Mockito.any());

        activatorSpy.start(bundleContext);

        Mockito.verify(activatorSpy, Mockito.times(1)).installJulLevelChangePropagator(loggerContext);
    }

    @Test
    public void testInstallJulLevelChangePropagator_propagatorNotAvailable_addPropagator() {
        Activator activatorSpy = Mockito.spy(activator);
        Mockito.doReturn(false).when(activatorSpy).hasInstanceOf(Matchers.anyCollection(), Matchers.any(Class.class));

        activatorSpy.installJulLevelChangePropagator(loggerContext);

        Mockito.verify(loggerContext, Mockito.atLeast(1)).addListener((LoggerContextListener) Matchers.any());
    }

    @Test
    public void testInstallJulLevelChangePropagator_propagatorAvailable_DoNothing() {
        Activator activatorSpy = Mockito.spy(activator);
        Mockito.doReturn(true).when(activatorSpy).hasInstanceOf(Matchers.anyCollection(), Matchers.any(Class.class));

        activatorSpy.installJulLevelChangePropagator(loggerContext);

        Mockito.verify(loggerContext, Mockito.never()).addListener((LoggerContextListener) Matchers.any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInstallJulLevelChangePropagator_collectionNull_throwException() {
        activator.hasInstanceOf(null, LevelChangePropagator.class);
    }

    @Test
    public void testInstallJulLevelChangePropagator_classNullCollectionEmpty_returnFalse() {
        boolean hasInstanceOf = activator.hasInstanceOf(new ArrayList<LoggerContextListener>(), null);

        Assert.assertFalse(hasInstanceOf);
    }

    @Test
    public void testInstallJulLevelChangePropagator_classNullCollectionIncludesLogger_returnFalse() {
        LoggerContextListener listener = new LevelChangePropagator();
        List<LoggerContextListener> collection = new ArrayList<LoggerContextListener>();
        collection.add(listener);

        boolean hasInstanceOf = activator.hasInstanceOf(collection, null);

        Assert.assertFalse(hasInstanceOf);
    }

    @Test
    public void testInstallJulLevelChangePropagator_everythingFine_returnTrue() {
        LoggerContextListener listener = new LevelChangePropagator();
        List<LoggerContextListener> collection = new ArrayList<LoggerContextListener>();
        collection.add(listener);

        boolean hasInstanceOf = activator.hasInstanceOf(collection, LevelChangePropagator.class);

        Assert.assertTrue(hasInstanceOf);
    }

}
