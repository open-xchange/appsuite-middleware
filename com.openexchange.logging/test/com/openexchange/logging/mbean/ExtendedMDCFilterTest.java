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

package com.openexchange.logging.mbean;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.MDC;
import com.openexchange.logging.filter.ExtendedMDCFilter;
import static org.powermock.api.mockito.PowerMockito.*;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.spi.FilterReply;

/**
 * {@link ExtendedMDCFilterTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Logger.class)
public class ExtendedMDCFilterTest {

    @Test
    public void testWhitelistBasedFiltering() throws Exception {
        Set<String> whitelist = new HashSet<String>();
        whitelist.add("com.openexchange.a");
        whitelist.add("com.openexchange.b");
        ExtendedMDCFilter filter = new ExtendedMDCFilter(whitelist);
        filter.addTuple("context", "3");
        filter.addLogger("com.openexchange.b", Level.TRACE);
        Logger logger = mock(Logger.class);
        when(logger.getName()).thenReturn("org.apache.some.Logger", "com.openexchange.a.some.Logger", "com.openexchange.b.some.Logger");

        Assert.assertEquals(FilterReply.NEUTRAL, filter.decide(null, logger, Level.TRACE, "Some message", null, null));
        try {
            MDC.put("user", "5");
            MDC.put("context", "3");
            Assert.assertEquals(FilterReply.ACCEPT, filter.decide(null, logger, Level.TRACE, "Some message", null, null));
            Assert.assertEquals(FilterReply.ACCEPT, filter.decide(null, logger, Level.TRACE, "Some message", null, null));
        } finally {
            MDC.clear();
        }
    }

    @Test
    public void testMultipleTupleBasedFiltering() throws Exception {
        ExtendedMDCFilter filter = new ExtendedMDCFilter(Collections.singleton("com.openexchange"));
        filter.addTuple("user", "5");
        filter.addTuple("context", "3");
        filter.addLogger("com.openexchange.b", Level.TRACE);
        Logger logger = mock(Logger.class);
        when(logger.getName()).thenReturn("com.openexchange.b.some.Logger");
        Assert.assertEquals(FilterReply.NEUTRAL, filter.decide(null, logger, Level.TRACE, "Some message", null, null));

        try {
            MDC.put("user", "5");
            MDC.put("context", "3");
            Assert.assertEquals(FilterReply.ACCEPT, filter.decide(null, logger, Level.TRACE, "Some message", null, null));
        } finally {
            MDC.clear();
        }
    }

    @Test
    public void testSingleTupleBasedFiltering() throws Exception {
        ExtendedMDCFilter filter = new ExtendedMDCFilter(Collections.singleton("com.openexchange"));
        filter.addTuple("context", "3");
        filter.addLogger("com.openexchange.a", Level.TRACE);
        Logger logger = mock(Logger.class);
        when(logger.getName()).thenReturn("com.openexchange.a.some.logger");
        Assert.assertEquals(FilterReply.NEUTRAL, filter.decide(null, logger, Level.TRACE, "Some message", null, null));

        try {
            MDC.put("user", "5");
            MDC.put("context", "3");
            Assert.assertEquals(FilterReply.ACCEPT, filter.decide(null, logger, Level.TRACE, "Some message", null, null));
        } finally {
            MDC.clear();
        }
    }

    @Test
    public void testUserFilterWithLoggingLevel() {
        ExtendedMDCFilter filter = new ExtendedMDCFilter(Collections.singleton("com.openexchange"));
        filter.addTuple("context", "314");
        filter.addTuple("user", "1618");
        filter.addLogger("com.openexchange.a", Level.DEBUG);
        Logger loggerA = mock(Logger.class);
        when(loggerA.getName()).thenReturn("com.openexchange.a.some.logger");

        Logger loggerB = mock(Logger.class);
        when(loggerB.getName()).thenReturn("com.openexchange.b.some.logger");

        Assert.assertEquals(FilterReply.NEUTRAL, filter.decide(null, loggerA, Level.TRACE, "Some message", null, null));

        try {
            MDC.put("user", "1618");
            MDC.put("context", "314");
            Assert.assertEquals(FilterReply.ACCEPT, filter.decide(null, loggerA, Level.DEBUG, "Some message", null, null));
            Assert.assertEquals(FilterReply.NEUTRAL, filter.decide(null, loggerB, Level.TRACE, "Some message", null, null));
        } finally {
            MDC.clear();
        }
    }
}
