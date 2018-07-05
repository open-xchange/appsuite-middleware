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

package com.openexchange.chronos.ical.ical4j.extensions;

import static org.slf4j.LoggerFactory.getLogger;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.StringTokenizer;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyFactory;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.property.ExDate;

/**
 * {@link LenientExDate}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class LenientExDate extends ExDate {

    public static final PropertyFactory FACTORY = new Factory();
    public static final String PROPERTY_NAME = Property.EXDATE;

    private static final long serialVersionUID = -4174897483634416534L;

    public LenientExDate() {
        super();
    }

    public LenientExDate(ParameterList aList, String aValue) throws ParseException {
        super(aList, aValue);
    }

    @Override
    public void setValue(String value) throws ParseException {
        DateList dateList = getDates();
        TimeZone timeZone = getTimeZone();
        StringTokenizer tokenizer = new StringTokenizer(value, ",");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            DateTime dateTime;
            try {
                dateTime = new DateTime(token, timeZone);
            } catch (ParseException e) {
                getLogger(LenientExDate.class).warn("Error parsing EXDATE as DATE-TIME, trying DATE as fallback", e);
                try {
                    dateTime = new DateTime(new Date(token));
                    dateTime.setTimeZone(timeZone);
                } catch (Exception x) {
                    throw e;
                }
            }
            dateList.add(dateTime);
        }
    }

    private static class Factory implements PropertyFactory {

        private static final long serialVersionUID = 1010126682318456760L;

        /**
         * Initializes a new {@link Factory}.
         */
        Factory() {
            super();
        }

        @Override
        public Property createProperty(String name, ParameterList parameters, String value) throws IOException, URISyntaxException, ParseException {
            return new LenientExDate(parameters, value);
        }

        @Override
        public Property createProperty(String name) {
            return new LenientExDate();
        }

    }

}
