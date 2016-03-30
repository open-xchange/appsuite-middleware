package com.openexchange.caldav.query;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import junit.framework.TestCase;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import static com.openexchange.time.TimeTools.D;

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

/**
 * {@link FilterParserTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FilterParserTest extends TestCase {


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
    public void testParseIsNotDefined() {

    }

    public void testParsePropFilter() {

    }*/

}
