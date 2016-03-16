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

package com.openexchange.ews;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * {@link DateConverter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DateConverter {

    /**
     * A default date converter instance.
     */
    public static final DateConverter DEFAULT = new DateConverter();

    private static final DatatypeFactory DATATYPE_FACTORY;
    static {
        try {
            DATATYPE_FACTORY = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException("Unable to create a DatatypeFactory instance", e);
        }
    }

    private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {

        @Override
        protected DateFormat initialValue() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return dateFormat;
        }
    };

    private final TimeZone timeZone;

    /**
     * Initializes a new {@link DateConverter}.
     *
     * @param timeZone the timezone to use
     */
    public DateConverter(TimeZone timeZone) {
        super();
        this.timeZone = timeZone;
    }

    /**
     * Initializes a new {@link DateConverter} using the <code>UTC</code> timezone.
     */
    public DateConverter() {
        this(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Gets the XML calendar represented by the supplied date instance.
     *
     * @param date the date
     * @return the calendar, or <code>null</code> if the supplied value was <code>null</code>
     */
    public XMLGregorianCalendar getXMLCalendar(Date date) {
        if (null != date) {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTimeZone(timeZone);
            calendar.setTime(date);
            return DATATYPE_FACTORY.newXMLGregorianCalendar(calendar);
        } else {
            return null;
        }
    }

    /**
     * Gets the date represented by the supplied XML calendar instance.
     *
     * @param xmlCalendar the XML calendar
     * @return the date, or <code>null</code> if the supplied value was <code>null</code>
     */
    public Date getDate(XMLGregorianCalendar xmlCalendar) {
        return null != xmlCalendar ? xmlCalendar.toGregorianCalendar(timeZone, null, null).getTime() : null;
    }

    /**
     * Gets an Exchange representation of the supplied date to be used in
     * <code>MapiPropertyTypeType.SYSTEM_TIME</code> properties.
     *
     * @param date the date
     * @return the formatted system time string
     */
    public String getSystemTimeString(Date date) {
        return DATE_FORMAT.get().format(date);
    }

}
