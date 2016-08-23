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

package com.openexchange.caldav.resources;

import static com.openexchange.dav.DAVProtocol.protocolException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import com.google.common.io.BaseEncoding;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.caldav.Patches;
import com.openexchange.caldav.Tools;
import com.openexchange.caldav.mixins.DefaultAlarmVeventDate;
import com.openexchange.caldav.mixins.DefaultAlarmVeventDatetime;
import com.openexchange.caldav.mixins.SupportedCalendarComponentSet;
import com.openexchange.caldav.mixins.SupportedCalendarComponentSets;
import com.openexchange.calendar.CalendarSql;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.search.Order;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.helpers.AbstractResource;

/**
 * {@link AppointmentCollection} - CalDAV collection for appointments.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AppointmentCollection extends CalDAVFolderCollection<Appointment> {

    /** A custom marker used as constant prefix for the resource name of single recurrences */
    private static final String RECURRENCE_MARKER = "WX8ZQ";

    /** A basic set of columns used to retrieve from the appointment service in list requests */
    private static final int[] BASIC_COLUMNS = {
        Appointment.UID, Appointment.FILENAME, Appointment.FOLDER_ID, Appointment.OBJECT_ID, Appointment.LAST_MODIFIED,
        Appointment.RECURRENCE_ID, Appointment.CREATION_DATE, Appointment.CHANGE_EXCEPTIONS
    };

    private Date lastModified;

    /**
     * Initializes a new {@link AppointmentCollection}.
     *
     * @param factory The factory
     * @param url The WebDAV path to use
     * @param folder The underlying calendar folder
     */
    public AppointmentCollection(GroupwareCaldavFactory factory, WebdavPath url, UserizedFolder folder) throws OXException {
        this(factory, url, folder, NO_ORDER);
    }

    /**
     * Initializes a new {@link AppointmentCollection}.
     *
     * @param factory The factory
     * @param url The WebDAV path to use
     * @param folder The underlying calendar folder
     * @param order The indicated calendar order for the collection
     */
    public AppointmentCollection(GroupwareCaldavFactory factory, WebdavPath url, UserizedFolder folder, int order) throws OXException {
        super(factory, url, folder, order);
        includeProperties(
            new SupportedCalendarComponentSet(SupportedCalendarComponentSet.VEVENT),
            new SupportedCalendarComponentSets(SupportedCalendarComponentSets.VEVENT),
            new DefaultAlarmVeventDate(),
            new DefaultAlarmVeventDatetime()
        );
    }

    /**
     * Loads all existing change exceptions of a specific recurring appointment.
     *
     * @param recurringMaster The recurring appointment "master"
     * @param applyPatches <code>true</code> to apply patches for the loaded exceptions, <code>false</code>, otherwise
     * @return The loaded change exceptions, or <code>null</code> if there are none
     */
    protected CalendarDataObject[] loadChangeExceptions(Appointment recurringMaster, boolean applyPatches) throws OXException {
        CalendarDataObject[] changeExceptions = null;
        if (0 < recurringMaster.getRecurrenceID() && recurringMaster.getRecurrenceID() == recurringMaster.getObjectID() &&
            null != recurringMaster.getChangeException() && 0 < recurringMaster.getChangeException().length) {
            changeExceptions = factory.getCalendarUtilities().getChangeExceptionsByRecurrence(
                recurringMaster.getRecurrenceID(), CalendarSql.EXCEPTION_FIELDS, factory.getSession());
            if (applyPatches && null != changeExceptions && 0 < changeExceptions.length) {
                for (int i = 0; i < changeExceptions.length; i++) {
                    changeExceptions[i] = patch(changeExceptions[i]);
                }
            }
        }
        return changeExceptions;
    }

    /**
     * Loads an appointment with all available data.
     *
     * @param appointment The appointment to load
     * @param applyPatches <code>true</code> to apply patches for the loaded appointment data, <code>false</code>, otherwise
     * @return The loaded appointment
     */
    protected CalendarDataObject load(Appointment appointment, boolean applyPatches) throws OXException {
        try {
            CalendarDataObject cdo = 0 < appointment.getParentFolderID() ?
                factory.getAppointmentInterface().getObjectById(appointment.getObjectID(), appointment.getParentFolderID()) :
                    factory.getAppointmentInterface().getObjectById(appointment.getObjectID());
            return applyPatches ? patch(cdo) : cdo;
        } catch (SQLException e) {
            throw protocolException(getUrl(), e);
        }
    }

    @Override
    public Date getLastModified() throws WebdavProtocolException {
        if (null == this.lastModified) {
            try {
                lastModified = Tools.getLatestModified(new Date(factory.getAppointmentInterface().getSequenceNumber(folderID)), folder);
            } catch (OXException e) {
                throw protocolException(getUrl(), e);
            }
        }
        return lastModified;
    }

    @Override
    protected WebdavPath constructPathForChildResource(Appointment appointment) {
        if (0 < appointment.getRecurrenceID() && appointment.getRecurrenceID() != appointment.getObjectID()) {
            /*
             * construct special resource name to directly access this appointment exceptions
             */
            String name = factory.getContext().getContextId() + "-" + folderID + "-" + appointment.getObjectID();
            String encodedName = BaseEncoding.base64Url().omitPadding().encode(name.getBytes(Charsets.UTF_8));
            return constructPathForChildResource(RECURRENCE_MARKER + encodedName + getFileExtension());
        }
        return super.constructPathForChildResource(appointment);
    }

    @Override
    protected Appointment getObject(String resourceName) throws OXException {
        if (Strings.isEmpty(resourceName)) {
            return null;
        }
        /*
         * check resource name for directly targeted appointment (as used by change exceptions without recurrence master)
         */
        Appointment appointment = getByRecurrenceMarker(resourceName);
        if (null != appointment) {
            return appointment;
        }
        /*
         * by default, try to resolve object by UID and filename
         */
        int objectID = factory.getAppointmentInterface().resolveUid(resourceName);
        if (1 > objectID) {
            objectID = factory.getAppointmentInterface().resolveFilename(resourceName);
        }
        if (0 < objectID) {
            try {
                return factory.getAppointmentInterface().getObjectById(objectID, folderID);
            } catch (OXException e) {
                if ("APP-0059".equals(e.getErrorCode())) {
                    // Got the wrong folder identification. You do not have the appropriate permissions to modify this object
                    // ignore
                } else {
                    throw e;
                }
            } catch (SQLException e) {
                throw protocolException(getUrl(), e);
            }
        }
        return null;
    }

    @Override
    protected boolean isSupported(Appointment object) throws OXException {
        return true;
    }

    @Override
    protected List<Appointment> getObjectsInRange(Date from, Date until) throws OXException {
        Date intervalStart = getIntervalStart();
        if (intervalStart.before(from)) {
            intervalStart = from;
        }
        Date intervalEnd = getIntervalEnd();
        if (intervalEnd.after(until)) {
            intervalEnd = until;
        }
        if (intervalStart.after(intervalEnd)) {
            return Collections.emptyList();
        }
        SearchIterator<Appointment> searchIterator = null;
        try {
            searchIterator = factory.getAppointmentInterface().getAppointmentsBetweenInFolder(
                folderID, BASIC_COLUMNS, intervalStart, intervalEnd, -1, Order.NO_ORDER);
            return getSignificantAppointments(searchIterator);
        } catch (SQLException e) {
            throw protocolException(getUrl(), e);
        } finally {
            SearchIterators.close(searchIterator);
        }
    }

    @Override
    protected Collection<Appointment> getModifiedObjects(Date since) throws OXException {
        SearchIterator<Appointment> searchIterator = null;
        try {
            searchIterator = factory.getAppointmentInterface().getModifiedAppointmentsInFolder(
                folderID, getIntervalStart(), getIntervalEnd(), BASIC_COLUMNS, since);
            return getSignificantAppointments(searchIterator);
        } catch (SQLException e) {
            throw protocolException(getUrl(), e);
        } finally {
            SearchIterators.close(searchIterator);
        }
    }

    @Override
    protected Collection<Appointment> getDeletedObjects(Date since) throws OXException {
        SearchIterator<Appointment> searchIterator = null;
        try {
            searchIterator = factory.getAppointmentInterface().getDeletedAppointmentsInFolder(folderID, BASIC_COLUMNS, since);
            return getSignificantAppointments(searchIterator);
        } catch (SQLException e) {
            throw protocolException(getUrl(), e);
        } finally {
            SearchIterators.close(searchIterator);
        }
    }

    @Override
    protected Collection<Appointment> getObjects() throws OXException {
        SearchIterator<Appointment> searchIterator = null;
        try {
            searchIterator = factory.getAppointmentInterface().getAppointmentsBetweenInFolder(
                folderID, BASIC_COLUMNS, getIntervalStart(), getIntervalEnd(), -1, Order.NO_ORDER);
            return getSignificantAppointments(searchIterator);
        } catch (SQLException e) {
            throw protocolException(getUrl(), e);
        } finally {
            SearchIterators.close(searchIterator);
        }
    }

    @Override
    protected AbstractResource createResource(Appointment object, WebdavPath url) throws OXException {
        return new AppointmentResource(factory, this, object, url);
    }

    private CalendarDataObject patch(CalendarDataObject appointment) throws OXException {
        if (null != appointment) {
            Patches.Outgoing.adjustAlarm(getFolder(), appointment);
            Patches.Outgoing.resolveGroupParticipants(appointment);
            Patches.Outgoing.setOrganizerInformation(factory, appointment);
            Patches.Outgoing.setOrganizersParticipantStatus(appointment);
            Patches.Outgoing.setSeriesStartAndEnd(factory, appointment);
            Patches.Outgoing.removeImplicitParticipant(getFolder(), appointment);
        }
        return appointment;
    }

    /**
     * Gets a specific recurring appointment occurrence based on it's resource name.
     *
     * @param resourceName The resource name to get the appointment recurrence for
     * @return The recurring appointment occurrence, or <code>null</code> if not found
     */
    private Appointment getByRecurrenceMarker(String resourceName) throws OXException {
        if (resourceName.startsWith(RECURRENCE_MARKER)) {
            /*
             * check resource name for directly targeted appointment (as used by change exceptions without recurrence master)
             */
            try {
                byte[] decodedName = BaseEncoding.base64Url().omitPadding().decode(resourceName.substring(RECURRENCE_MARKER.length()));
                String[] splitted = Strings.splitByDelimNotInQuotes(new String(decodedName, Charsets.UTF_8), '-');
                if (null == splitted || 3 != splitted.length) {
                    throw new IllegalArgumentException(resourceName);
                }
                int contextId = Integer.parseInt(splitted[0]);
                int folderId = Integer.parseInt(splitted[1]);
                int objectId = Integer.parseInt(splitted[2]);
                if (contextId == factory.getContext().getContextId() && folderId == this.folderID) {
                    try {
                        return factory.getAppointmentInterface().getObjectById(objectId, folderId);
                    } catch (SQLException e) {
                        throw protocolException(getUrl(), e);
                    } catch (OXException e) {
                        if ("APP-0059".equals(e.getErrorCode()) || "OX-0001".equals(e.getErrorCode())) {
                            throw protocolException(getUrl(), e, HttpServletResponse.SC_NOT_FOUND);
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                // no directly targeted appointment, continue by resolving the resource name as usual
            }
        }
        return null;
    }

    /**
     * Gets a list of appointments that are 'significant' for iCal synchronization, i.e. all non-recurring appointments, all recurring
     * appointment masters, as well as all recurring appointment exceptions where no corresponding master appointment is available.
     *
     * @param searchIterator The search iterator to process
     * @return The significant appointments
     */
    private static List<Appointment> getSignificantAppointments(SearchIterator<Appointment> searchIterator) throws OXException {
        Map<String, List<Appointment>> appointmentsByUids;
        try {
            appointmentsByUids = mapAppointmentsByUids(searchIterator);
        } finally {
            SearchIterators.close(searchIterator);
        }
        return getSignificantAppointments(appointmentsByUids);
    }

    /**
     * Gets a list of appointments that are 'significant' for iCal synchronization, i.e. all non-recurring appointments, all recurring
     * appointment masters, as well as all recurring appointment exceptions where no corresponding master appointment is available.
     *
     * @param appointmentsByUids The appointments mapped by their UID
     * @return The significant appointments
     */
    private static List<Appointment> getSignificantAppointments(Map<String, List<Appointment>> appointmentsByUids) {
        if (null == appointmentsByUids || 0 == appointmentsByUids.size()) {
            return Collections.emptyList();
        }
        List<Appointment> appointments = new ArrayList<Appointment>(appointmentsByUids.size());
        for (List<Appointment> appointmentsWithUid : appointmentsByUids.values()) {
             if (1 == appointmentsWithUid.size()) {
                 appointments.add(appointmentsWithUid.get(0));
             } else {
                 Appointment recurringMaster = null;
                 for (Appointment appointment : appointmentsWithUid) {
                     if (false == appointment.containsRecurrenceID() || appointment.getRecurrenceID() == appointment.getObjectID()) {
                         recurringMaster = appointment;
                         break;
                     }
                 }
                 if (null != recurringMaster) {
                     appointments.add(recurringMaster);
                 } else {
                     appointments.addAll(appointmentsWithUid);
                 }
             }
        }
        return appointments;
    }

    /**
     * Reads all appointments from the supplied search iterator and maps them by their UID.
     *
     * @param searchIterator The search iterator to process
     * @return The appointments, mapped by their UID
     */
    private static Map<String, List<Appointment>> mapAppointmentsByUids(SearchIterator<Appointment> searchIterator) throws OXException {
        Map<String, List<Appointment>> appointmentsByUid = new HashMap<String, List<Appointment>>();
        while (searchIterator.hasNext()) {
            Appointment appointment = searchIterator.next();
            String uid = appointment.getUid();
            if (Strings.isEmpty(uid)) {
                LOG.warn("Skipping appointment without UID: {}", appointment);
                continue;
            }
            List<Appointment> appointments = appointmentsByUid.get(uid);
            if (null == appointments) {
                appointments = new ArrayList<Appointment>();
                appointmentsByUid.put(uid, appointments);
            }
            appointments.add(appointment);
        }
        return appointmentsByUid;
    }

}
