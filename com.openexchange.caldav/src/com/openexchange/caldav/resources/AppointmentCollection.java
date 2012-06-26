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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.caldav.Patches;
import com.openexchange.caldav.mixins.SupportedCalendarComponentSet;
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
        Appointment.RECURRENCE_ID, Appointment.CREATION_DATE
    };
    
    private final GroupwareCaldavFactory factory;
    private AppointmentSQLInterface appointmentInterface = null;
    private List<Appointment> knownAppointments = null;
    private Map<Integer, ArrayList<Appointment>> knownExceptions = null;

    public AppointmentCollection(GroupwareCaldavFactory factory, WebdavPath url, UserizedFolder folder) throws OXException {
        this(factory, url, folder, NO_ORDER);
    }
    
    public AppointmentCollection(GroupwareCaldavFactory factory, WebdavPath url, UserizedFolder folder, int order) throws OXException {
        super(factory, url, folder, order);
        this.factory = factory;
        includeProperties(new SupportedCalendarComponentSet(SupportedCalendarComponentSet.VEVENT));
    }
    
    private AppointmentSQLInterface getAppointmentInterface() {
        if (null == this.appointmentInterface) {
            this.appointmentInterface = factory.getAppointmentInterface();
        }
        return this.appointmentInterface;
    }
    
    private void updateCache() throws OXException {
        this.knownAppointments = new ArrayList<Appointment>();
        this.knownExceptions = new HashMap<Integer, ArrayList<Appointment>>();
        SearchIterator<Appointment> searchIterator = null;
        try {
            searchIterator = getAppointmentInterface().getAppointmentsBetweenInFolder(this.folderID, BASIC_COLUMNS, 
                getIntervalStart(), getIntervalEnd(), -1, Order.NO_ORDER);
            while (searchIterator.hasNext()) {
                Appointment appointment = searchIterator.next();
                if (appointment.containsRecurrenceID() && appointment.getObjectID() != appointment.getRecurrenceID()) {
                    int recurrenceID = appointment.getRecurrenceID();
                    ArrayList<Appointment> changeExceptions = knownExceptions.get(recurrenceID);
                    if (null == changeExceptions) {
                        changeExceptions = new ArrayList<Appointment>(); 
                        knownExceptions.put(Integer.valueOf(recurrenceID), changeExceptions);
                    }
                    changeExceptions.add(appointment);
                } else {
                    knownAppointments.add(appointment);
                }
            }
        } catch (SQLException e) {
            throw protocolException(e);
        } finally {
            searchIterator.close();
        }
    }
    
    public List<Appointment> getAppointments() throws OXException {
        if (null == this.knownAppointments) {
            this.updateCache();
        }
        return knownAppointments;
    }

    public ArrayList<Appointment> getChangeExceptions(int recurrenceID) throws OXException {
        if (null == this.knownExceptions) {
            this.updateCache();
        }
        return this.knownExceptions.get(Integer.valueOf(recurrenceID));
    }
    
    public ArrayList<Appointment> loadChangeExceptions(int recurrenceID) throws OXException {
        ArrayList<Appointment> changeExceptions = getChangeExceptions(recurrenceID);
        if (null != changeExceptions && 0 < changeExceptions.size()) {
            for (int i = 0; i < changeExceptions.size(); i++) {
                Appointment changeException = changeExceptions.get(i);
                if (false == changeException.containsRecurrenceDatePosition()) {
                    changeExceptions.set(i, this.load(changeException, false));
                }
            }
        }
        return changeExceptions;
    }
    
    @Override
    protected Collection<Appointment> getModifiedObjects(Date since) throws OXException {
        try {
            return filter(getAppointmentInterface().getModifiedAppointmentsInFolder(
                folderID, factory.start(), factory.end(), BASIC_COLUMNS, since));
        } catch (SQLException e) {
            throw protocolException(e);
        }
    }

    @Override
    protected Collection<Appointment> getDeletedObjects(Date since) throws OXException {
        try {
            return filter(getAppointmentInterface().getDeletedAppointmentsInFolder(folderID, BASIC_COLUMNS, since));
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
                appointments = filter(getAppointmentInterface().getAppointmentsBetweenInFolder(this.folderID, BASIC_COLUMNS, 
                    getIntervalStart(), getIntervalEnd(), -1, Order.NO_ORDER));
            } catch (SQLException e) {
                throw protocolException(e);
            }
        }
        return appointments;
    }

    protected Appointment load(Appointment appointment, boolean applyPatches) throws OXException {
        try {
            CalendarDataObject cdo = getAppointmentInterface().getObjectById(appointment.getObjectID(), appointment.getParentFolderID());
            return applyPatches ? patch(cdo) : cdo;
        } catch (SQLException e) {
            throw super.protocolException(e);
        }
    }
    
    private Appointment patch(CalendarDataObject appointment) throws WebdavProtocolException {
        if (null != appointment) {
            Patches.Outgoing.removeAlarmInSharedFolder(getFolder(), appointment);
            Patches.Outgoing.resolveGroupParticipants(appointment);
            Patches.Outgoing.setOrganizerInformation(factory, appointment);
            Patches.Outgoing.setOrganizersParticipantStatus(appointment);
            Patches.Outgoing.setSeriesStartAndEnd(factory, appointment);
        }
        return appointment;
    }
    
}
