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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.data.conversion.ical;

import java.io.OutputStream;
import java.util.List;

import com.openexchange.data.conversion.ical.itip.ITipContainer;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.Task;

/**
 * {@link ICalEmitter}
 *
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public interface ICalEmitter {

    // TODO: What about mixed exports?Tasks and Appointments
    public String writeAppointments(List<Appointment> appointmentObjects, Context ctx, List<ConversionError> errors, List<ConversionWarning> warnings) throws ConversionError;

    public String writeAppointmentRequest(Appointment appointment, Context ctx, List<ConversionError> errors, List<ConversionWarning> warnings) throws ConversionError;

    public String writeTasks(List<Task> tasks, List<ConversionError> errors, List<ConversionWarning> warnings, Context ctx) throws ConversionError;

    /**
     * Writes a free/busy-reply, using the supplied free/busy information to
     * reflect the free/busy-times and the corresponding attendee.
     * 
     * @param freeBusyRequest the free/busy-information 
     * @param ctx the context
     * @param errors the list of conversion errors
     * @param warnings the list of conversion warnings
     * @return the free/busy-reply
     * @throws ConversionError
     */
    String writeFreeBusyReply(FreeBusyInformation freeBusyInfo, Context ctx, List<ConversionError> errors, List<ConversionWarning> warnings) throws ConversionError;

    /**
     * Creates a new {@link ICalSession} to collect the iCal information.
     * @param mode Operation mode to use.
     * @return a newly generated {@link ICalSession}.
     */
    public ICalSession createSession(Mode mode);

    ICalSession createSession();

    public ICalItem writeAppointment(ICalSession session, Appointment appointment, Context ctx, ITipContainer iTip, List<ConversionError> errors, List<ConversionWarning> warnings) throws ConversionError;

    /**
     * @throws ConversionError if a wrong session is given that is not created with this implementations {@link #createSession()} method.
     */
    public ICalItem writeAppointment(ICalSession session, Appointment appointment, Context context, List<ConversionError> errors, List<ConversionWarning> warnings) throws ConversionError;

    /**
     * @throws ConversionError if a wrong session is given that is not created with this implementations {@link #createSession()} method.
     */
    public ICalItem writeTask(ICalSession session, Task task, Context context, List<ConversionError> errors, List<ConversionWarning> warnings) throws ConversionError;

    /**
     * @throws ConversionError if a wrong session is given that is not created with this implementations {@link #createSession()} method.
     */
    public void writeSession(ICalSession session, OutputStream stream) throws ConversionError;

	public void flush(ICalSession session, OutputStream stream) throws ConversionError;

}
