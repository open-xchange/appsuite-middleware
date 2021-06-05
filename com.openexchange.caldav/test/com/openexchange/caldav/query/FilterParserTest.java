package com.openexchange.caldav.query;

import static com.openexchange.time.TimeTools.D;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.junit.Test;
import com.openexchange.webdav.protocol.WebdavProtocolException;

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

/**
 * {@link FilterParserTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FilterParserTest {

         @Test
     public void testParseTimeRangeQueryStartAndEnd() throws Exception {
        String filterDef =
        "<filter xmlns='urn:ietf:params:xml:ns:caldav'>"+
        "   <comp-filter name='VCALENDAR'>"+
        "       <comp-filter name='VEVENT'>"+
        "           <time-range start='20110911T220000Z' end='20120911T220000Z'/>"+
        "       </comp-filter>"+
        "   </comp-filter>"+
        "</filter>";

        Filter filter = parse(filterDef);
        assertNotNull(filter);

        List<Filter> filters = filter.getFilters();
        assertNotNull(filters);
        assertEquals(1, filters.size());

        CompFilter vCalendarQuery = (CompFilter) filters.get(0);

        assertEquals("VCALENDAR", vCalendarQuery.getName());

        filters = vCalendarQuery.getFilters();
        assertNotNull(filters);
        assertEquals(1, filters.size());

        CompFilter vEventQuery = (CompFilter) filters.get(0);
        assertEquals("VEVENT", vEventQuery.getName());

        filters = vEventQuery.getFilters();
        assertNotNull(filters);
        assertEquals(1, filters.size());

        TimeRange range = (TimeRange) filters.get(0);

        assertEquals(D("11.09.2011 22:00").getTime(), range.getStart());
        assertEquals(D("11.09.2012 22:00").getTime(), range.getEnd());


    }

    private Filter parse(String filterDef) throws JDOMException, IOException, WebdavProtocolException {
        Element rootElement = new SAXBuilder().build(new StringReader(filterDef)).getRootElement();

        return new FilterParser().parse(rootElement);
    }

         @Test
     public void testParseTimeRangeQueryStartOnly() throws Exception {
        String filterDef =
            "<filter xmlns='urn:ietf:params:xml:ns:caldav'>"+
            "   <comp-filter name='VCALENDAR'>"+
            "       <comp-filter name='VEVENT'>"+
            "           <time-range start='20110809T220000Z' />"+
            "       </comp-filter>"+
            "   </comp-filter>"+
            "</filter>";

            Filter filter = parse(filterDef);
            assertNotNull(filter);

            List<Filter> filters = filter.getFilters();
            assertNotNull(filters);
            assertEquals(1, filters.size());

            CompFilter vCalendarQuery = (CompFilter) filters.get(0);

            assertEquals("VCALENDAR", vCalendarQuery.getName());

            filters = vCalendarQuery.getFilters();
            assertNotNull(filters);
            assertEquals(1, filters.size());

            CompFilter vEventQuery = (CompFilter) filters.get(0);
            assertEquals("VEVENT", vEventQuery.getName());

            filters = vEventQuery.getFilters();
            assertNotNull(filters);
            assertEquals(1, filters.size());

            TimeRange range = (TimeRange) filters.get(0);

            assertEquals(D("09.08.2011 22:00").getTime(), range.getStart());
            assertEquals(TimeRange.NOT_SET, range.getEnd());
    }

    // YAGNI
    // We'll build these if and when they are needed
    /*
         @Test
     public void testParseIsNotDefined() {

    }

         @Test
     public void testParsePropFilter() {

    }*/

}
