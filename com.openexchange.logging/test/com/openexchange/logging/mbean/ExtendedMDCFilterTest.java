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

package com.openexchange.logging.mbean;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
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
