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

package com.openexchange.groupware.importexport.importers;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.api2.OXException;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.server.services.ServerServiceRegistry;


/**
 * {@link Bug12380RecoveryParser}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class Bug12380RecoveryParser implements ICalParser {

    private ICalParser delegate;
    private ServerServiceRegistry registry;
    
    public Bug12380RecoveryParser(ICalParser delegate, ServerServiceRegistry registry) {
        this.delegate = delegate;
        this.registry = registry;
    }

    public List<CalendarDataObject> parseAppointments(InputStream ical, TimeZone defaultTZ, Context ctx, List<ConversionError> errors, List<ConversionWarning> warnings) throws ConversionError {
        return splitIfNeeded(delegate.parseAppointments(ical, defaultTZ, ctx, errors, warnings));
    }

    public List<CalendarDataObject> parseAppointments(String icalText, TimeZone defaultTZ, Context ctx, List<ConversionError> errors, List<ConversionWarning> warnings) throws ConversionError {
        return splitIfNeeded(delegate.parseAppointments(icalText, defaultTZ, ctx, errors, warnings));
    }

    private List<CalendarDataObject> splitIfNeeded(List<CalendarDataObject> appointments) throws ConversionError {
        CalendarCollectionService tools = registry.getService(CalendarCollectionService.class);
        int index = 0;
        LinkedList<CalendarDataObject> copy = new LinkedList<CalendarDataObject>(appointments);
        for (CalendarDataObject appointment : appointments) {
            try {
                if(appointment.isSequence() && !tools.isOccurrenceDate(appointment.getStartDate().getTime(), -1, appointment, new long[0])) {
                    CalendarDataObject clone = (CalendarDataObject) appointment.clone();
                    tools.removeRecurringType(appointment);
                    copy.add(clone);
                }
            } catch (OXException e) {
                throw new ConversionError(index, e);
            }
            index++;
        }
        
        return copy;
    }

    public List<Task> parseTasks(InputStream ical, TimeZone defaultTZ, Context context, List<ConversionError> errors, List<ConversionWarning> warnings) throws ConversionError {
        return delegate.parseTasks(ical, defaultTZ, context, errors, warnings);
    }

    public List<Task> parseTasks(String icalText, TimeZone defaultTZ, Context context, List<ConversionError> errors, List<ConversionWarning> warnings) throws ConversionError {
        return delegate.parseTasks(icalText, defaultTZ, context, errors, warnings);
    }

    
    
}
