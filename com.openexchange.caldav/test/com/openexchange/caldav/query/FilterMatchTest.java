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

package com.openexchange.caldav.query;

import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.time.TimeTools.D;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Date;
import org.junit.Test;


/**
 * {@link FilterMatchTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FilterMatchTest {
         @Test
     public void testMatcherForTimeSpanQuery() {
        FilterAnalyzer analyzer = new FilterAnalyzerBuilder()
            .compFilter("VCALENDAR")
              .compFilter("VEVENT")
                .timeRange().capture().end()
              .end()
            .end()
        .build();

        Filter filter = new Filter().addFilter(
            compFilter("VCALENDAR").addFilter(
                compFilter("VEVENT").addFilter(
                    timeSpan(D("08.09.2011 22:00"))
                )
            )
        );

        ArrayList<Object> captured = new ArrayList<Object>();
        boolean matches = analyzer.match(filter, captured);
        assertTrue(matches);
        assertEquals(2, captured.size());
        assertEquals(L(D("08.09.2011 22:00").getTime()), captured.get(0));
        assertEquals(L(-1L), captured.get(1));

        filter = new Filter().addFilter(
            compFilter("VCALENDAR").addFilter(
                compFilter("VBLUPP").addFilter(
                    timeSpan(D("08.09.2011 22:00"))
                )
            )
        );

        assertFalse(analyzer.match(filter, new ArrayList<Object>()));

    }

    private Filter timeSpan(Date start) {
        TimeRange tr = new TimeRange();
        tr.setStart(start.getTime());
        return tr;
    }

    private Filter compFilter(String string) {
        CompFilter compFilter = new CompFilter();
        compFilter.setName(string);
        return compFilter;
    }

}
