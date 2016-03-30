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

package com.openexchange.dav.caldav.ical;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.dav.caldav.ical.SimpleICal.Property;



/**
 * {@link ICalUtils}
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class ICalUtils {
	
    public static String formatAsUTC(final Date date) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmm'00Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

	public static String fold(String content) {
    	return content.replaceAll(".{75}", "$0\r\n ");
    }

    public static String unfold(String content) {
    	return content.replaceAll("(?:\\r\\n?|\\n)[ \t]", "");
    }
	    
	public static Date parseDate(Property property) throws ParseException {
	    if (null == property || null == property.getValue()) {
	        return null;
	    }
		SimpleDateFormat dateFormat = new SimpleDateFormat();
		if ("DATE".equals(property.getAttribute("VALUE"))) {
			dateFormat.applyPattern("yyyyMMdd");
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		} else {
			String tzid = property.getAttribute("TZID");
			if (null != tzid) {
				dateFormat.setTimeZone(TimeZone.getTimeZone(tzid));
				dateFormat.applyPattern("yyyyMMdd'T'HHmmss");
			} else {
				dateFormat.applyPattern("yyyyMMdd'T'HHmmss'Z'");
				dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			}
		}
		return dateFormat.parse(property.getValue());
	}
	
	public static String format(Date date, TimeZone timeZone) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmm'00'");
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(date);
    }
    
    public static String format(Date date, String timeZoneID) {
        return format(date, TimeZone.getTimeZone(timeZoneID));
    }
    
	public static List<Date[]> parsePeriods(Property property) throws ParseException {
		List<Date[]> periods = new ArrayList<Date[]>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		String[] splitted =  property.getValue().split(",");
		for (String value : splitted) {
			if (0 < value.trim().length()) {
				String from = value.substring(0, value.indexOf('/'));
				String until = value.substring(value.indexOf('/') + 1);
				if (until.startsWith("PT")) {
					throw new UnsupportedOperationException("durations not implemented");
				}
				Date[] period = new Date[2];
				period[0] = dateFormat.parse(from);
				period[1] = dateFormat.parse(until);
				periods.add(period);
			}
		}
		return periods;
	}

    private ICalUtils() {
    	// 
    }

}
