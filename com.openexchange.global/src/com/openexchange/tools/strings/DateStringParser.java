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
        if(t != Date.class || s == null) {
            return null;
        }
        final Long parsed = subParser.parse(s, Long.class);
        if(parsed != null) {
            return (T) new Date(parsed.longValue());
        }
        return (T) parseDate(s);
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
                        } catch (final ParseException e) {/*Next*/ }
                }
                final DateFormat sdf = DateFormat.getDateInstance(dateStyle, loc);
                try { return sdf.parse(data);
                    } catch (final ParseException e) {/*Next*/ }
                }
        }
        final DateFormat sdf = DateFormat.getInstance();
        try { return sdf.parse(data);
            } catch (final ParseException e) {/*Next*/ }

        return null;
    }

}
