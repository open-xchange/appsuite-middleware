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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.caldav.Patches;
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
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link AppointmentCollection} - CalDAV collection for appointments.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AppointmentCollection extends CalDAVFolderCollection<Appointment> {

    private static final int[] BASIC_COLUMNS = {
        Appointment.UID, Appointment.FILENAME, Appointment.FOLDER_ID, Appointment.OBJECT_ID, Appointment.LAST_MODIFIED,
        Appointment.RECURRENCE_ID, Appointment.CREATION_DATE, Appointment.CHANGE_EXCEPTIONS
    };

    private List<Appointment> knownAppointments;

    public AppointmentCollection(GroupwareCaldavFactory factory, WebdavPath url, UserizedFolder folder) throws OXException {
        this(factory, url, folder, NO_ORDER);
    }

    public AppointmentCollection(GroupwareCaldavFactory factory, WebdavPath url, UserizedFolder folder, int order) throws OXException {
        super(factory, url, folder, order);
        includeProperties(
            new SupportedCalendarComponentSet(SupportedCalendarComponentSet.VEVENT),
            new SupportedCalendarComponentSets(SupportedCalendarComponentSets.VEVENT),
            new DefaultAlarmVeventDate(),
            new DefaultAlarmVeventDatetime()
        );
    }

    public List<Appointment> getAppointments() throws OXException {
        if (null == this.knownAppointments) {
            this.updateCache();
        }
        return knownAppointments;
    }

    /**
     * Loads all existing change exceptions of a specific recurring appointment.
     *
     * @param recurringMaster The recurring appointment "master"
     * @param applyPatches <code>true</code> to apply patches for the loaded exceptions, <code>false</code>, otherwise
     * @return The loaded change exceptions, or <code>null</code> if there are none
     */
    public CalendarDataObject[] loadChangeExceptions(Appointment recurringMaster, boolean applyPatches) throws OXException {
        CalendarDataObject[] changeExceptions = null;
        if (null != recurringMaster.getChangeException() && 0 < recurringMaster.getChangeException().length) {
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

    @Override
    protected Collection<Appointment> getModifiedObjects(Date since) throws OXException {
        try {
            return filter(factory.getAppointmentInterface().getModifiedAppointmentsInFolder(
                folderID, getIntervalStart(), getIntervalEnd(), BASIC_COLUMNS, since));
        } catch (SQLException e) {
            throw protocolException(e);
        }
    }

    @Override
    protected Collection<Appointment> getDeletedObjects(Date since) throws OXException {
        try {
            return filter(factory.getAppointmentInterface().getDeletedAppointmentsInFolder(folderID, BASIC_COLUMNS, since));
        } catch (SQLException e) {
            throw protocolException(e);
        }
    }

    @Override
    protected Collection<Appointment> getObjects() throws OXException {
        return this.getAppointments();
    }

    @Override
    protected AppointmentResource createResource(Appointment object, WebdavPath url) throws OXException {
        return new AppointmentResource(factory, this, object, url);
    }

    @Override
    protected boolean isSupported(Appointment appointment) throws WebdavProtocolException {
        return null != appointment &&
            (false == appointment.containsRecurrenceID() || appointment.getRecurrenceID() == appointment.getObjectID());
    }

    @Override
    protected List<Appointment> getObjectsInRange(Date from, Date until) throws OXException {
        List<Appointment> appointments = null;
        if (null != this.knownAppointments) {
            appointments = new ArrayList<Appointment>();
            for (Appointment appointment : this.knownAppointments) {
                if (isSupported(appointment) && isInInterval(appointment, from, until)) {
                    appointments.add(appointment);
                }
            }
        } else {
            try {
                appointments = filter(factory.getAppointmentInterface().getAppointmentsBetweenInFolder(this.folderID, BASIC_COLUMNS,
                    getIntervalStart(), getIntervalEnd(), -1, Order.NO_ORDER));
            } catch (SQLException e) {
                throw protocolException(e);
            }
        }
        return appointments;
    }

    @Override
    protected Appointment getObject(String resourceName) throws OXException {
        Appointment object = null;
        /*
         * try from cache first if initialized
         */
        if (null != knownAppointments && 0 < knownAppointments.size()) {
            object = super.getObject(resourceName);
        }
        /*
         * try to resolve object by UID / filename directly if not found
         */
        if (null == object) {
            int objectID = factory.getAppointmentInterface().resolveUid(resourceName);
            if (1 > objectID) {
                objectID = factory.getAppointmentInterface().resolveFilename(resourceName);
            }
            if (0 < objectID) {
                try {
                    object = factory.getAppointmentInterface().getObjectById(objectID, folderID);
                    if (null != knownAppointments) {
                        remember(object);
                    }
                } catch (OXException e) {
                    if ("APP-0059".equals(e.getErrorCode())) {
                        // Got the wrong folder identification. You do not have the appropriate permissions to modify this object
                        // ignore
                    } else {
                        throw e;
                    }
                } catch (SQLException e) {
                    throw protocolException(e);
                }
            }
        }
        return object;
    }

    protected CalendarDataObject load(Appointment appointment, boolean applyPatches) throws OXException {
        try {
            CalendarDataObject cdo = 0 < appointment.getParentFolderID() ?
                factory.getAppointmentInterface().getObjectById(appointment.getObjectID(), appointment.getParentFolderID()) :
                    factory.getAppointmentInterface().getObjectById(appointment.getObjectID());
            if (null != knownAppointments) {
                remember(appointment);
            }
            return applyPatches ? patch(cdo) : cdo;
        } catch (SQLException e) {
            throw super.protocolException(e);
        }
    }

    private void updateCache() throws OXException {
        this.knownAppointments = new ArrayList<Appointment>();
        SearchIterator<Appointment> searchIterator = null;
        try {
            searchIterator = factory.getAppointmentInterface().getAppointmentsBetweenInFolder(folderID, BASIC_COLUMNS,
                getIntervalStart(), getIntervalEnd(), -1, Order.NO_ORDER);
            while (searchIterator.hasNext()) {
                this.remember(searchIterator.next());
            }
        } catch (SQLException e) {
            throw protocolException(e);
        } finally {
            if (null != searchIterator) {
                searchIterator.close();
            }
        }
    }

    /**
     * Adds the supplied appointment to the list of known appointments, implicitly loading recurring appointment exceptions as well if
     * needed.
     *
     * @param appointment The appointment to remember
     * @return <code>true</code>, if it was added to the cache, <code>false</code>, otherwise
     * @throws OXException
     */
    private boolean remember(Appointment appointment) throws OXException {
        if (null == this.knownAppointments) {
            LOG.warn("Appointment cache not initialized, unable to remember appointment.");
            return false;
        } else if (appointment.containsRecurrenceID()) {
            if (appointment.getObjectID() == appointment.getRecurrenceID()) {
                return knownAppointments.add(appointment);
            }
        } else {
            return knownAppointments.add(appointment);
        }
        return false;
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

}
