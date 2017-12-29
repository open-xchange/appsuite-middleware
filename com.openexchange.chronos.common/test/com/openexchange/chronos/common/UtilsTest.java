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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.common;

import static com.openexchange.chronos.common.CalendarUtils.getDuration;
import static org.junit.Assert.assertEquals;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import org.junit.Test;

/**
 * {@link UtilsTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class UtilsTest  {

    @Test
    public void testGetDurationDate() throws Exception {
        assertEquals("P0D", getDuration(DateTime.parse("20171205"), DateTime.parse("20171205"), null).toString());
        assertEquals("P1D", getDuration(DateTime.parse("20171205"), DateTime.parse("20171206"), null).toString());
        assertEquals("-P1D", getDuration(DateTime.parse("20171206"), DateTime.parse("20171205"), null).toString());
        assertEquals("P365D", getDuration(DateTime.parse("20161206"), DateTime.parse("20171206"), null).toString());
        assertEquals("-P365D", getDuration(DateTime.parse("20171206"), DateTime.parse("20161206"), null).toString());
        assertEquals("P731D", getDuration(DateTime.parse("20151206"), DateTime.parse("20171206"), null).toString());
        assertEquals("-P731D", getDuration(DateTime.parse("20171206"), DateTime.parse("20151206"), null).toString());
    }

    @Test
    public void testGetDurationDateTime() throws Exception {
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");
        assertEquals("P0D", getDuration(DateTime.parse("20171205T160000"), DateTime.parse("20171205T160000"), timeZone).toString());
        assertEquals("PT1M", getDuration(DateTime.parse("20171205T160000"), DateTime.parse("20171205T160100"), timeZone).toString());
        assertEquals("-PT1M", getDuration(DateTime.parse("20171205T160100"), DateTime.parse("20171205T160000"), timeZone).toString());
        assertEquals("PT3H30M", getDuration(DateTime.parse("20171205T160000"), DateTime.parse("20171205T193000"), timeZone).toString());
        assertEquals("-PT3H30M", getDuration(DateTime.parse("20171205T193000"), DateTime.parse("20171205T160000"), timeZone).toString());
        assertEquals("PT8H", getDuration(DateTime.parse("20171205T160000"), DateTime.parse("20171206T000000"), timeZone).toString());
        assertEquals("-PT8H", getDuration(DateTime.parse("20171206T000000"), DateTime.parse("20171205T160000"), timeZone).toString());
        assertEquals("PT11H", getDuration(DateTime.parse("20171205T160000"), DateTime.parse("20171206T030000"), timeZone).toString());
        assertEquals("-PT11H", getDuration(DateTime.parse("20171206T030000"), DateTime.parse("20171205T160000"), timeZone).toString());
    }

}
