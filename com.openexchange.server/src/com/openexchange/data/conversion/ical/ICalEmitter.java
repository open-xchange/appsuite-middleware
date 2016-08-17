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

package com.openexchange.data.conversion.ical;

import java.io.OutputStream;
import java.util.List;
import com.openexchange.data.conversion.ical.itip.ITipContainer;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link ICalEmitter}
 *
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
@SingletonService
public interface ICalEmitter {

    /**
     * Creates a new iCal session to use when exporting multiple iCal items.
     *
     * @param mode The operation mode to use.
     * @return A new iCal session
     */
    ICalSession createSession(Mode mode);

    /**
     * Creates a new iCal session for use during export.
     *
     * @return A new iCal session
     */
    ICalSession createSession();

    /**
     * Serializes the iCal session to an output stream.
     *
     * @param session The iCal session to write
     * @param stream The target output stream
     */
    void writeSession(ICalSession session, OutputStream stream) throws ConversionError;

    /**
     * Finishes and flushed the write operation of the iCal session.
     *
     * @param session The iCal session to flush
     * @param stream The target output stream
     */
    void flush(ICalSession session, OutputStream stream) throws ConversionError;

    /**
     * Writes a single appointment to an iCal session.
     *
     * @param session The underlying iCal session
     * @param appointment The appointment to write
     * @param ctx The context
     * @param errors A reference to store any conversion errors
     * @param warnings A reference to store any non-fatal conversion warnings
     * @return A reference to the exported iCal item
     */
    ICalItem writeAppointment(ICalSession session, Appointment appointment, Context context, List<ConversionError> errors, List<ConversionWarning> warnings) throws ConversionError;

    /**
     * Writes a single appointment to an iCal session, optionally considering special iTIP scheduling options.
     *
     * @param session The underlying iCal session
     * @param appointment The appointment to write
     * @param ctx The context
     * @param iTip The associated iTIP container, or <code>null</code> if no iTIP scheduling is involved
     * @param errors A reference to store any conversion errors
     * @param warnings A reference to store any non-fatal conversion warnings
     * @return A reference to the exported iCal item
     */
    ICalItem writeAppointment(ICalSession session, Appointment appointment, Context ctx, ITipContainer iTip, List<ConversionError> errors, List<ConversionWarning> warnings) throws ConversionError;

    /**
     * Writes a single task to an iCal session.
     *
     * @param session The underlying iCal session
     * @param task The task to write
     * @param ctx The context
     * @param iTip The associated iTIP container, or <code>null</code> if no iTIP scheduling is involved
     * @param errors A reference to store any conversion errors
     * @param warnings A reference to store any non-fatal conversion warnings
     * @return A reference to the exported iCal item
     */
    ICalItem writeTask(ICalSession session, Task task, Context context, List<ConversionError> errors, List<ConversionWarning> warnings) throws ConversionError;

    /**
     * Serializes one or more appointments directly as iCal file, i.e. not bound to an iCal session.
     *
     * @param appointments The appointments to write
     * @param ctx The context
     * @param errors A reference to store any conversion errors
     * @param warnings A reference to store any non-fatal conversion warnings
     * @return The exported iCal file as string
     */
    String writeAppointments(List<Appointment> appointments, Context ctx, List<ConversionError> errors, List<ConversionWarning> warnings) throws ConversionError;

    /**
     * Serializes one or more tasks directly as iCal file, i.e. not bound to an iCal session.
     *
     * @param tasks The tasks to write
     * @param ctx The context
     * @param errors A reference to store any conversion errors
     * @param warnings A reference to store any non-fatal conversion warnings
     * @return The exported iCal file as string
     */
    String writeTasks(List<Task> tasks, List<ConversionError> errors, List<ConversionWarning> warnings, Context ctx) throws ConversionError;

    /**
     * Serializes a free/busy-reply directly, using the supplied free/busy information to reflect the free/busy-times and the corresponding attendee.
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
     * Writes a single timezone definition to an iCal session.
     *
     * @param session The underlying iCal session
     * @param timeZoneID The identifier of the timezone
     * @param errors A reference to store any conversion errors
     * @param warnings A reference to store any non-fatal conversion warnings
     * @return <code>true</code> if the timezone has been added, <code>false</code>, if no matching timezone could be found or the timezone definition was already present
     */
    boolean writeTimeZone(ICalSession session, String timeZoneID, List<ConversionError> errors, List<ConversionWarning> warnings) throws ConversionError;

}
