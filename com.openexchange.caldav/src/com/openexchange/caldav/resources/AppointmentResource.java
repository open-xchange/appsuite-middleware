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

package com.openexchange.caldav.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.caldav.Patches;
import com.openexchange.caldav.Tools;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalSession;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Truncated;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.java.Streams;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;
import com.openexchange.webdav.protocol.WebdavPath;

/**
 * {@link AppointmentResource}
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AppointmentResource extends CalDAVResource<Appointment> {

    /**
     * All appointment fields that may be set in iCal files
     */
    private static int[] CALDAV_FIELDS = {
        Appointment.END_DATE, // DTEND
        Appointment.SHOWN_AS, // TRANSP
        Appointment.LOCATION, // LOCATION
        Appointment.NOTE, // DESCRIPTION
        Appointment.PRIVATE_FLAG, // CLASS
        Appointment.TITLE, // SUMMARY
        Appointment.START_DATE, // DTSTART
        Appointment.PARTICIPANTS, // ATTENDEE
        Appointment.FULL_TIME, // DTSTART/DTEND
        Appointment.ALARM, // VALARM
        Appointment.RECURRENCE_TYPE, // RRULE;FREQ
    };
    
    /**
     * All appointment recurrence fields that may be set in iCal files
     */
    private static int[] RECURRENCE_FIELDS = {
        Appointment.INTERVAL,
        Appointment.DAYS,
        Appointment.DAY_IN_MONTH,
        Appointment.MONTH,
        Appointment.RECURRENCE_COUNT,
        Appointment.UNTIL
    };

    private AppointmentSQLInterface appointmentInterface = null;
    private final AppointmentCollection parent;

    private List<CalendarDataObject> exceptionsToSave = null;
    private List<CalendarDataObject> deleteExceptionsToSave = null;
    private CalendarDataObject appointmentToSave = null;

    public AppointmentResource(final GroupwareCaldavFactory factory, final AppointmentCollection parent, final Appointment object, final WebdavPath url) throws OXException {
        super(factory, parent, object, url);
        this.parent = parent;
    }

    private AppointmentSQLInterface getAppointmentInterface() {
        if (null == this.appointmentInterface) {
            this.appointmentInterface = factory.getAppointmentInterface();
        }
        return this.appointmentInterface;
    }

    @Override
    protected void deleteObject() throws OXException {
        try {
            getAppointmentInterface().deleteAppointmentObject(
                (CalendarDataObject) this.object, object.getParentFolderID(), object.getLastModified());
        } catch (final SQLException e) {
            throw protocolException(e);
        }
    }

    @Override
    protected void saveObject() throws OXException {
        try {
            /*
             * get original data
             */
            final Appointment originalAppointment = parent.load(this.object, false);
            final List<Appointment> originalExceptions = parent.loadChangeExceptions(this.object.getObjectID());
            Date clientLastModified = this.object.getLastModified();
            if (clientLastModified.before(originalAppointment.getLastModified())) {
                throw super.protocolException(HttpServletResponse.SC_CONFLICT);
            }
            /*
             * update appointment
             */
            if (false == Patches.Incoming.tryRestoreParticipants(originalAppointment, appointmentToSave)) {
                Patches.Incoming.patchResources(originalAppointment, appointmentToSave);
                Patches.Incoming.patchParticipantListRemovingAliases(factory, appointmentToSave);
                Patches.Incoming.patchParticipantListRemovingDoubleUsers(appointmentToSave);
            }
            checkForExplicitRemoves(originalAppointment, appointmentToSave);
            getAppointmentInterface().updateAppointmentObject(appointmentToSave, parentFolderID, clientLastModified);
            clientLastModified = appointmentToSave.getLastModified();
            /*
             * update change exceptions
             */
            for (final CalendarDataObject exceptionToSave : exceptionsToSave) {
                final Appointment originalException = getMatchingException(originalExceptions, exceptionToSave.getRecurrenceDatePosition());
                if (null != originalException) {
                    /*
                     * prepare exception update
                     */
                    exceptionToSave.setObjectID(originalException.getObjectID());
                    checkForExplicitRemoves(originalException, exceptionToSave);                    
                    if (false == Patches.Incoming.tryRestoreParticipants(originalException, exceptionToSave)) {
                        Patches.Incoming.patchParticipantListRemovingAliases(factory, exceptionToSave);
                        Patches.Incoming.patchParticipantListRemovingDoubleUsers(exceptionToSave);
                    }
                } else {
                    /*
                     * prepare exception create
                     */
                    exceptionToSave.setObjectID(object.getObjectID());
                    if (false == Patches.Incoming.tryRestoreParticipants(originalAppointment, exceptionToSave)) {
                        Patches.Incoming.patchParticipantListRemovingAliases(factory, exceptionToSave);
                        Patches.Incoming.patchParticipantListRemovingDoubleUsers(exceptionToSave);
                    }
                }
                /*
                 * update exception
                 */
                getAppointmentInterface().updateAppointmentObject(exceptionToSave, parentFolderID, clientLastModified);
                clientLastModified = exceptionToSave.getLastModified();
            }
            /*
             * update delete exceptions
             */
            for (final CalendarDataObject deleteExceptionToSave : deleteExceptionsToSave) {
                final Appointment originalException = getMatchingException(originalExceptions, deleteExceptionToSave.getRecurrenceDatePosition());
                if (null != originalException) {
                    /*
                     * prepare delete of existing exception
                     */
                    deleteExceptionToSave.setObjectID(originalException.getObjectID());
                } else {
                    /*
                     * prepare new delete exception
                     */
                    deleteExceptionToSave.setObjectID(object.getObjectID());
                }
                getAppointmentInterface().deleteAppointmentObject(deleteExceptionToSave, parentFolderID, clientLastModified);
                if (null != deleteExceptionToSave.getLastModified()) {
                    clientLastModified = deleteExceptionToSave.getLastModified();    
                }                
            }
        } catch (final SQLException e) {
            throw protocolException(e);
        }
    }
    
    @Override
    protected void createObject() throws OXException {
        try {
            /*
             * create appointment
             */
            appointmentToSave.removeObjectID(); // in case it's already assigned due to retry operations
            appointmentToSave.setParentFolderID(null != object ? object.getParentFolderID() : parentFolderID);
            getAppointmentInterface().insertAppointmentObject(this.appointmentToSave);
            Date clientLastModified = appointmentToSave.getLastModified();
            /*
             * create change exceptions
             */
            for (final CalendarDataObject exception : exceptionsToSave) {
                exception.removeObjectID(); // in case it's already assigned due to retry operations
                exception.setObjectID(appointmentToSave.getObjectID());
                getAppointmentInterface().updateAppointmentObject(exception, parentFolderID, clientLastModified);
                clientLastModified = exception.getLastModified();
            }
            /*
             * create delete exceptions
             */
            for (final CalendarDataObject exception : deleteExceptionsToSave) {
                exception.setObjectID(appointmentToSave.getObjectID());
                getAppointmentInterface().deleteAppointmentObject(exception, parentFolderID, clientLastModified);
                clientLastModified = exception.getLastModified();
            }
        } catch (final SQLException e) {
            throw protocolException(e);
        }
    }
    
    @Override
    protected void move(final String targetFolderID) throws OXException {
        this.appointmentToSave = new CalendarDataObject();
        appointmentToSave.setObjectID(object.getObjectID());
        appointmentToSave.setParentFolderID(Tools.parse(targetFolderID));
        appointmentToSave.setContext(factory.getContext());
        getAppointmentInterface().updateAppointmentObject(appointmentToSave, parentFolderID, object.getLastModified());
    }

    @Override
    protected String generateICal() throws OXException {
        final ICalEmitter icalEmitter = factory.getIcalEmitter();
        final ICalSession session = icalEmitter.createSession();
        final List<ConversionError> conversionErrors = new LinkedList<ConversionError>();
        final List<ConversionWarning> conversionWarnings = new LinkedList<ConversionWarning>();
        try {
            /*
             * write appointment
             */
            icalEmitter.writeAppointment(session, parent.load(object, true), 
                factory.getContext(), conversionErrors, conversionWarnings);
            if (0 < object.getRecurrenceID()) {
                final List<Appointment> changeExceptions = parent.getChangeExceptions(object.getObjectID());
                if (null != changeExceptions && 0 < changeExceptions.size()) {
                    /*
                     * write exceptions
                     */
                    for (final Appointment changeException : changeExceptions) {
                        icalEmitter.writeAppointment(session, parent.load(changeException, true), 
                            factory.getContext(), conversionErrors, conversionWarnings);
                    }
                }
            }
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            icalEmitter.writeSession(session, bytes);
            /*
             * apply patches
             */
            String iCal = new String(bytes.toByteArray(), "UTF-8");
            iCal = Patches.Outgoing.removeEmptyRDates(iCal);
            return iCal;
        } catch (final UnsupportedEncodingException e) {
            throw protocolException(e);
        }
    }
    
    @Override
    protected void deserialize(final InputStream body) throws OXException, IOException {
        final List<CalendarDataObject> appointments = this.parse(body);
        if (null != appointments && 0 < appointments.size()) {
            this.deleteExceptionsToSave = new ArrayList<CalendarDataObject>();
            this.exceptionsToSave = new ArrayList<CalendarDataObject>();            
            for (final CalendarDataObject cdo : appointments) {
                cdo.setContext(factory.getContext());
                cdo.removeLastModified();
                cdo.setIgnoreConflicts(true);
                if (null != this.object) {
                    cdo.setParentFolderID(this.object.getParentFolderID());
                    cdo.setObjectID(this.object.getObjectID());
                    cdo.removeUid();
                } else {
                    cdo.setParentFolderID(this.parentFolderID);
                }                
                if (1 == appointments.size() || looksLikeMaster(cdo)) {
                    this.appointmentToSave = cdo;
                    createNewDeleteExceptions(this.object, appointmentToSave);
                } else {
                    factory.getCalendarUtilities().removeRecurringType(cdo);
                    exceptionsToSave.add(cdo);
                }
            }
            /*
             * store filename when different from uid
             */
            final String resourceName = super.extractResourceName();
            if (null != resourceName && false == resourceName.equals(appointmentToSave.getUid())) {
                appointmentToSave.setFilename(resourceName);
            }
        }
    }
    
    private List<CalendarDataObject> parse(final InputStream body) throws IOException, ConversionError {
        UnsynchronizedByteArrayOutputStream baos = null;
        try {
            final int buflen = 2048;
            final byte[] buf = new byte[buflen];
            baos = new UnsynchronizedByteArrayOutputStream(8192);
            for (int read = body.read(buf, 0, buflen); read > 0; read = body.read(buf, 0, buflen)) {
                baos.write(buf, 0, read);
            }
            return this.parse(new String(baos.toByteArray(), "UTF-8"));
        } finally {
            Streams.close(baos);
            Streams.close(body);
        }
    }

    private List<CalendarDataObject> parse(final String iCal) throws ConversionError {
        /*
         * apply patches
         */
        final String patchedICal = iCal;
        
        //XXX to make the UserResolver do it's job correctly
        //patchedICal = patchedICal.replace("424242669@devel-mail.netline.de", "@premium");
        /*
         * parse appointments
         */
        return factory.getIcalParser().parseAppointments(
                patchedICal, getTimeZone(), factory.getContext(), new ArrayList<ConversionError>(), new ArrayList<ConversionWarning>());
    }
    
    private void checkForExplicitRemoves(final Appointment oldAppointment, final CalendarDataObject cdo) {
        /*
         * reset previously set appointment fields
         */
        for (final int field : CALDAV_FIELDS) {
            if (oldAppointment.contains(field) && false == cdo.contains(field)) {
                if (CalendarObject.ALARM == field) {
                    // -1 resets alarm
                    cdo.setAlarm(-1);                                                            
                } else {
                    cdo.set(field, cdo.get(field)); 
                }
            }
        }
        /*
         * reset previously set recurrence specific fields
         */
        if (CalendarObject.NO_RECURRENCE != oldAppointment.getRecurrenceType() && 
                CalendarObject.NO_RECURRENCE != cdo.getRecurrenceType()) {
            for (final int field : RECURRENCE_FIELDS) {
                if (oldAppointment.contains(field) && false == cdo.contains(field)) {
                    cdo.set(field, CalendarObject.UNTIL == field ? null : cdo.get(field)); // getUntil returns 'max until date' if not set 
                }
            }
        } 
    }
    
    private void createNewDeleteExceptions(final Appointment oldAppointment, final CalendarDataObject cdo) throws OXException {
        final Date[] wantedDeleteExceptions = cdo.getDeleteException();
        if (wantedDeleteExceptions == null || wantedDeleteExceptions.length == 0) {
            return;
        }
        // Normalize the wanted DelEx to midnight, and add them to our set.
        final Set<Date> wantedSet = new HashSet<Date>(Arrays.asList(wantedDeleteExceptions));

        Date[] knownDeleteExceptions = oldAppointment.getDeleteException();
        if (knownDeleteExceptions == null) {
            knownDeleteExceptions = new Date[0];
        }
        for (final Date date : knownDeleteExceptions) {
            wantedSet.remove(date);
        }

        for (final Date date : wantedSet) {
            final CalendarDataObject deleteException = new CalendarDataObject();
            deleteException.setRecurrenceDatePosition(date);
            deleteException.setContext(factory.getContext());
            deleteException.setParentFolderID(parentFolderID);
            deleteExceptionsToSave.add(deleteException);
        }

        cdo.removeDeleteExceptions();
    }

    @Override
    protected boolean trimTruncatedAttribute(final Truncated truncated) {
        boolean hasTrimmed = false;
        if (null != this.appointmentToSave) {
            hasTrimmed |= trimTruncatedAttribute(truncated, appointmentToSave);
        }
        if (null != this.exceptionsToSave && 0 < this.exceptionsToSave.size()) {
            for (final CalendarDataObject calendarObject : exceptionsToSave) {
                hasTrimmed |= trimTruncatedAttribute(truncated, calendarObject);
            }
        }
        return hasTrimmed;
    }
    
    private static boolean trimTruncatedAttribute(final Truncated truncated, final CalendarDataObject calendarObject) {
        final Object value = calendarObject.get(truncated.getId());
        if (null != value && String.class.isInstance(value)) {
            final String stringValue = (String)value;
            if (stringValue.length() > truncated.getMaxSize()) {
                calendarObject.set(truncated.getId(), stringValue.substring(0, truncated.getMaxSize()));
                return true;
            }
        }
        return false;
    }

    private static boolean looksLikeMaster(final CalendarDataObject cdo) {
        return cdo.containsRecurrenceType() && CalendarObject.NO_RECURRENCE != cdo.getRecurrenceType();
    }

    private static Appointment getMatchingException(final List<Appointment> changeExceptions, final Date recurrenceDatePosition) {
        if (null != changeExceptions) {
            for (final Appointment existingException : changeExceptions) {
                if (existingException.getRecurrenceDatePosition().equals(recurrenceDatePosition)) {
                    return existingException;
                }
            }
        }
        return null;
    }

}
