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
package com.openexchange.test.fixtures.transformators;

import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.test.fixtures.FixtureLoader;
import com.openexchange.test.fixtures.SimpleCredentials;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class JChronicDateTransformator implements Transformator{

	private static final Pattern parenthesesRegex = Pattern.compile("(\\{|\\[|\\(.+\\)|\\]|\\})");
	private static final String fallbackPattern = "dd.MM.yy HH:mm";
	private final FixtureLoader fixtureLoader;

    public JChronicDateTransformator(FixtureLoader fixtureLoader) {
		super();
		this.fixtureLoader = fixtureLoader;
	}

	@Override
    public Object transform(String value) throws OXException {
    	if (null == value) {
    		return null;
    	}

    	TimeZone timeZone = null;
    	final Matcher matcher = parenthesesRegex.matcher(value);
    	if (matcher.find()) {
    		String match = matcher.group();
    		if (null != match) {
    			match = match.substring(1, match.length() - 1);
    			if (match.startsWith("users:") && 6 < match.length()) {
	    			final String user = match.substring(6);
	    			final SimpleCredentials credentials = fixtureLoader.getFixtures("users", SimpleCredentials.class).getEntry(user).getEntry();
	    			timeZone = credentials.getTimeZone();
        		} else {
        			final String tzID = match.replace(' ', '_');
        			if (isAvailable(tzID)) {
        				timeZone = TimeZone.getTimeZone(match);
        			}
        		}
    		}
    		if (null == timeZone) {
    			throw OXException.general("unable to parse user / timezone from '" + match.toString() + "'.");
    		}
    		value = matcher.replaceFirst("").trim();
    	}

    	Date date = TimeTools.D(value, timeZone);

    	if(date == null) {
    	    throw OXException.general("Can't parse date '"+value+"'");
    	}

    	return date;
    }


    private static boolean isAvailable(final String timeZoneID) {
		final String[] availableIDs = TimeZone.getAvailableIDs();
		for (int i = 0; i < availableIDs.length; i++) {
			if (availableIDs[i].equals(timeZoneID)) {
				return true;
			}
		}
		return false;
	}
}
