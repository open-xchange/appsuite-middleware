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

package com.openexchange.groupware.importexport.importers;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.FreeBusyInformation;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.server.services.ServerServiceRegistry;


/**
 * {@link ExtraneousSeriesMasterRecoveryParser}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class ExtraneousSeriesMasterRecoveryParser implements ICalParser {

    private final ICalParser delegate;
    private final ServerServiceRegistry registry;

    public ExtraneousSeriesMasterRecoveryParser(final ICalParser delegate, final ServerServiceRegistry registry) {
        this.delegate = delegate;
        this.registry = registry;
    }

    @Override
    public List<CalendarDataObject> parseAppointments(final InputStream ical, final TimeZone defaultTZ, final Context ctx, final List<ConversionError> errors, final List<ConversionWarning> warnings) throws ConversionError {
        return splitIfNeeded(delegate.parseAppointments(ical, defaultTZ, ctx, errors, warnings));
    }

    @Override
    public List<CalendarDataObject> parseAppointments(final String icalText, final TimeZone defaultTZ, final Context ctx, final List<ConversionError> errors, final List<ConversionWarning> warnings) throws ConversionError {
        return splitIfNeeded(delegate.parseAppointments(icalText, defaultTZ, ctx, errors, warnings));
    }

    private List<CalendarDataObject> splitIfNeeded(final List<CalendarDataObject> appointments) throws ConversionError {
        if (appointments == null) {
            return null;
        }
        
        if (appointments.size() == 0) {
            return appointments;
        }
        final CalendarCollectionService tools = registry.getService(CalendarCollectionService.class);
        int index = 0;
        final LinkedList<CalendarDataObject> copy = new LinkedList<CalendarDataObject>(appointments);
        for (final CalendarDataObject appointment : appointments) {
            try {
                if(appointment.isSequence() && !tools.isOccurrenceDate(appointment.getStartDate().getTime(), -1, appointment, new long[0])) {
                    final CalendarDataObject clone = appointment.clone();
                    tools.removeRecurringType(appointment);
                    copy.add(clone);
                }
            } catch (final OXException e) {
                throw new ConversionError(index, e);
            }
            index++;
        }

        return copy;
    }

    @Override
    public List<Task> parseTasks(final InputStream ical, final TimeZone defaultTZ, final Context context, final List<ConversionError> errors, final List<ConversionWarning> warnings) throws ConversionError {
        return delegate.parseTasks(ical, defaultTZ, context, errors, warnings);
    }

    @Override
    public List<Task> parseTasks(final String icalText, final TimeZone defaultTZ, final Context context, final List<ConversionError> errors, final List<ConversionWarning> warnings) throws ConversionError {
        return delegate.parseTasks(icalText, defaultTZ, context, errors, warnings);
    }

    @Override
    public String parseProperty(final String propertyName, final InputStream ical) {
        return delegate.parseProperty(propertyName, ical);
    }

	@Override
	public List<FreeBusyInformation> parseFreeBusy(String icalText, TimeZone defaultTZ, Context ctx, List<ConversionError> errors, List<ConversionWarning> warnings) throws ConversionError {
		return delegate.parseFreeBusy(icalText, defaultTZ, ctx, errors, warnings);
	}

	@Override
	public List<FreeBusyInformation> parseFreeBusy(InputStream ical, TimeZone defaultTZ, Context ctx, List<ConversionError> errors, List<ConversionWarning> warnings) throws ConversionError {
		return delegate.parseFreeBusy(ical, defaultTZ, ctx, errors, warnings);
	}

	@Override
	public void setLimit(int limit) {
		delegate.setLimit(limit);
	}

}
