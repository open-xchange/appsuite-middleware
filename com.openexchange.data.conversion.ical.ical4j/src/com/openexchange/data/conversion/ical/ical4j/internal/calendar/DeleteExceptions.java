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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
package com.openexchange.data.conversion.ical.ical4j.internal.calendar;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.property.ExDate;

import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ical4j.internal.AbstractVerifyingAttributeConverter;
import com.openexchange.data.conversion.ical.ical4j.internal.EmitterTools;
import com.openexchange.data.conversion.ical.ical4j.internal.ParserTools;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.contexts.Context;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class DeleteExceptions<T extends CalendarComponent, U extends CalendarObject> extends AbstractVerifyingAttributeConverter<T,U> {

    public boolean isSet(final U calendar) {
        return calendar.containsDeleteExceptions();
    }

    public void emit(final int index, final U calendar, final T t, final List<ConversionWarning> warnings, final Context ctx) throws ConversionError {
        final Date[] dates = calendar.getDeleteException();
        if(null == dates) { return; }
        final DateList deleteExceptions = new DateList(dates.length);
        for(int i = 0, size = dates.length; i < size; i++) {
            deleteExceptions.add(EmitterTools.toDateTime(dates[i]));
        }

        deleteExceptions.setUtc(true);

        final ExDate property = new ExDate(deleteExceptions);
        t.getProperties().add(property);

        return;
    }

    public boolean hasProperty(final T t) {
        return null != t.getProperty("EXDATE");
    }

    public void parse(final int index, final T component, final U cObj, final TimeZone timeZone, final Context ctx, final List<ConversionWarning> warnings) throws ConversionError {
       final PropertyList exdates = component.getProperties("EXDATE");
        for(int i = 0, size = exdates.size(); i < size; i++) {
            final ExDate exdate = (ExDate) exdates.get(0);

            final DateList dates = exdate.getDates();
            for(int j = 0, size2 = dates.size(); j < size2; j++) {
                final net.fortuna.ical4j.model.Date icaldate = (net.fortuna.ical4j.model.Date) dates.get(j);
                final Date date = ParserTools.recalculateAsNeeded(icaldate, exdate, timeZone);
                cObj.addDeleteException(date);
            }
        }
    }

}
