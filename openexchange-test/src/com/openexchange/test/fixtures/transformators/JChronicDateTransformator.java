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

package com.openexchange.test.fixtures.transformators;

import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.exception.OXException;
import com.openexchange.test.common.groupware.calendar.TimeTools;
import com.openexchange.test.fixtures.FixtureLoader;
import com.openexchange.test.fixtures.SimpleCredentials;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class JChronicDateTransformator implements Transformator {

    private static final Pattern parenthesesRegex = Pattern.compile("(\\{|\\[|\\(.+\\)|\\]|\\})");
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
                throw OXException.general("unable to parse user / timezone from '" + (match == null ? null : match.toString()) + "'.");
            }
            value = matcher.replaceFirst("").trim();
        }

        Date date = TimeTools.D(value, timeZone);

        if (date == null) {
            throw OXException.general("Can't parse date '" + value + "'");
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
