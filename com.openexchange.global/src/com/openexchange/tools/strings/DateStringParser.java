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

package com.openexchange.tools.strings;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * {@link DateStringParser}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DateStringParser implements StringParser {

    private StringParser subParser;

    public DateStringParser() {

    }

    public DateStringParser(final StringParser subParser) {
        this.subParser = subParser;
    }

    public void setSubParser(final StringParser subParser) {
        this.subParser = subParser;
    }

    public StringParser getSubParser() {
        return subParser;
    }

    @Override
    public <T> T parse(final String s, final Class<T> t) {
        if (t != Date.class || s == null) {
            return null;
        }
        final Long parsed = subParser.parse(s, Long.class);
        if (parsed != null) {
            @SuppressWarnings("unchecked") T date = (T) new Date(parsed.longValue());
            return date;
        }
        @SuppressWarnings("unchecked") T parsedDate = (T) parseDate(s);
        return parsedDate;
    }

    // Plucked from HCalendarParser
    private static Date parseDate(final String data){
        final List<Locale> locales = Arrays.asList(Locale.US, Locale.UK, Locale.CANADA, Locale.ENGLISH, Locale.FRENCH, Locale.GERMAN, Locale.CHINA);
        final int[] styles = new int [] {DateFormat.FULL, DateFormat.LONG, DateFormat.MEDIUM, DateFormat.SHORT };
        for(final Locale loc: locales){
            for(final int dateStyle: styles){
                for(final int timeStyle: styles){
                    final DateFormat sdf = DateFormat.getDateTimeInstance(dateStyle, timeStyle, loc);
                    try { return sdf.parse(data);
                        } catch (ParseException e) {/*Next*/ }
                }
                final DateFormat sdf = DateFormat.getDateInstance(dateStyle, loc);
                try { return sdf.parse(data);
                    } catch (ParseException e) {/*Next*/ }
                }
        }
        final DateFormat sdf = DateFormat.getInstance();
        try { return sdf.parse(data);
            } catch (ParseException e) {/*Next*/ }

        return null;
    }

}
