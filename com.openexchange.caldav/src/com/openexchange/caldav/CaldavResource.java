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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.caldav;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.data.conversion.ical.ICalSession;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.ldap.User;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.helpers.AbstractResource;

/**
 * A {@link CaldavResource} bridges an OX appointment to a caldav resource.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CaldavResource extends AbstractResource {

    private static final Log LOG = LogFactory.getLog(CaldavResource.class);

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private GroupwareCaldavFactory factory;

    private Appointment appointment;

    private CaldavCollection parent;

    private WebdavPath url;

    private boolean exists = false;

    private byte[] fileData;

    private List<CalendarDataObject> exceptionsToSave = new ArrayList<CalendarDataObject>();

    private List<CalendarDataObject> deleteExceptionsToSave = new ArrayList<CalendarDataObject>();

    public CaldavResource(CaldavCollection parent, Appointment appointment, GroupwareCaldavFactory factory) throws WebdavProtocolException {
        super();
        if (appointment == null) {
            throw new NullPointerException();
        }
        this.exists = true;
        this.parent = parent;
        this.appointment = appointment;
        this.factory = factory;
        this.url = parent.getUrl().dup().append(appointment.getUid() + ".ics");

        if (! factory.getState().hasBeenPatched(appointment)) {
            factory.getState().markAsPatched(appointment);
            patchOrganizer();
            patchOrganizersParticipantState();
            patchSeriesStartAndEnd();
            patchGroups();
        }
    }

    public CaldavResource(CaldavCollection parent, WebdavPath url, GroupwareCaldavFactory factory) {
        super();
        this.exists = false;
        this.parent = parent;
        this.factory = factory;
        this.url = url;
    }

    @Override
    protected WebdavFactory getFactory() {
        return factory;
    }

    @Override
    public boolean hasBody() throws WebdavProtocolException {
        return true;
    }

    @Override
    protected List<WebdavProperty> internalGetAllProps() throws WebdavProtocolException {
        return Collections.emptyList();
    }

    @Override
    protected WebdavProperty internalGetProperty(String namespace, String name) throws WebdavProtocolException {
        if (namespace.equals(CaldavProtocol.CAL_NS.getURI()) && name.equals("calendar-data")) {
            WebdavProperty property = new WebdavProperty(namespace, name);
            try {
                property.setValue(new String(icalFile(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new WebdavProtocolException(getUrl(), 500);
            }
            return property;
        }
        return null;
    }

    @Override
    protected void internalPutProperty(WebdavProperty prop) throws WebdavProtocolException {

    }

    @Override
    protected void internalRemoveProperty(String namespace, String name) throws WebdavProtocolException {

    }

    @Override
    protected boolean isset(Property p) {
        return true;
    }

    @Override
    public void putBody(InputStream body, boolean guessSize) throws WebdavProtocolException {
        fileData = null;
        ICalParser parser = factory.getIcalParser();
        try {
            List<CalendarDataObject> appointments = parser.parseAppointments(
                body,
                UTC,
                factory.getContext(),
                new ArrayList<ConversionError>(),
                new ArrayList<ConversionWarning>());
            Appointment oldAppointment = appointment;
            if (!appointments.isEmpty()) {
                if (appointments.size() == 1) {
                    CalendarDataObject cdo = appointments.get(0);
                    cdo.setContext(factory.getContext());
                    if (oldAppointment != null) {
                        checkForExplicitRemoves(oldAppointment, cdo);
                        createNewDeleteExceptions(oldAppointment, cdo);
                        cdo.setObjectID(oldAppointment.getObjectID());
                        cdo.setParentFolderID(oldAppointment.getParentFolderID());
                        cdo.setLastModified(oldAppointment.getLastModified());
                    } else {
                        cdo.setParentFolderID(parent.getId());
                    }
                    appointment = cdo;
                } else {
                    for (CalendarDataObject cdo : appointments) {
                        cdo.setContext(factory.getContext());

                        if (oldAppointment != null) {
                            checkForExplicitRemoves(oldAppointment, cdo);
                            cdo.setParentFolderID(oldAppointment.getParentFolderID());
                            cdo.setLastModified(oldAppointment.getLastModified());
                        } else {
                            cdo.setParentFolderID(parent.getId());
                        }
                        if (looksLikeMaster(cdo)) {
                            if (oldAppointment != null) {
                                createNewDeleteExceptions(oldAppointment, cdo);
                                cdo.setObjectID(oldAppointment.getObjectID());
                            }
                            appointment = cdo;
                        } else {
                            exceptionsToSave.add(cdo);
                        }
                    }
                }

            }
        } catch (ConversionError e) {
            LOG.error(e.getMessage(), e);
            throw new WebdavProtocolException(getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void checkForExplicitRemoves(Appointment oldAppointment, CalendarDataObject cdo) {
        if (oldAppointment.getFullTime() && !cdo.getFullTime() && !cdo.containsFullTime()) {
            cdo.setFullTime(false); // Set this explicitly, so the update actually changes it
        }

        if (oldAppointment.getRecurrenceType() != CalendarObject.NO_RECURRENCE && !cdo.containsRecurrenceCount() && !cdo.containsUntil()) {
            cdo.setUntil(cdo.getUntil()); // Again, set this explicitly if this changed.
        }
    }

    private void createNewDeleteExceptions(Appointment oldAppointment, CalendarDataObject cdo) {
        Date[] wantedDeleteExceptions = cdo.getDeleteException();
        if (wantedDeleteExceptions == null || wantedDeleteExceptions.length == 0) {
            return;
        }
        // Normalize the wanted DelEx to midnight, and add them to our set.
        Set<Date> wantedSet = new HashSet<Date>(Arrays.asList(wantedDeleteExceptions));

        Date[] knownDeleteExceptions = oldAppointment.getDeleteException();
        if (knownDeleteExceptions == null) {
            knownDeleteExceptions = new Date[0];
        }
        for (Date date : knownDeleteExceptions) {
            wantedSet.remove(date);
        }

        for (Date date : wantedSet) {
            CalendarDataObject deleteException = new CalendarDataObject();
            deleteException.setRecurrenceDatePosition(date);
            deleteException.setContext(factory.getContext());
            deleteException.setParentFolderID(parent.getId());
            deleteExceptionsToSave.add(deleteException);
        }

        cdo.removeDeleteExceptions();

    }

    private boolean looksLikeMaster(CalendarDataObject cdo) {
        return cdo.containsRecurrenceType() && cdo.getRecurrenceType() != CalendarObject.NO_RECURRENCE;
    }

    @Override
    public void setCreationDate(Date date) throws WebdavProtocolException {

    }

    public void create() throws WebdavProtocolException {
        checkRange();
        write(true);
    }

    @Override
    public WebdavResource move(WebdavPath dest, boolean noroot, boolean overwrite) throws WebdavProtocolException {
        WebdavResource destinationResource = factory.resolveResource(dest);
        CaldavCollection destinationCollection;
        if (destinationResource.isCollection()) {
            destinationCollection = (CaldavCollection) destinationResource;
        } else {
            destinationCollection = (CaldavCollection) factory.resolveCollection(dest.parent());
        }
        CalendarDataObject moveOp = new CalendarDataObject();
        moveOp.setObjectID(appointment.getObjectID());
        moveOp.setParentFolderID(destinationCollection.getId());
        moveOp.setContext(factory.getContext());
        moveOp.setLastModified(appointment.getLastModified());
        appointment = moveOp;
        write(false);
        parent = destinationCollection;
        return this;
    }

    private void write(boolean create) throws WebdavProtocolException {
        try {
            CalendarDataObject toSave = (CalendarDataObject) appointment;
            AppointmentSQLInterface appointmentSQLInterface = factory.getAppointmentInterface();
            if (create) {
                appointmentSQLInterface.insertAppointmentObject(toSave);
            } else {
                Appointment oldAppointment = factory.getState().get(appointment.getUid(), appointment.getParentFolderID());
                
                patchGroupsBeforeSave(oldAppointment, toSave);
                appointmentSQLInterface.updateAppointmentObject(toSave, parent.getId(), toSave.getLastModified());
            }

            for (Appointment exception : exceptionsToSave) {
                Appointment matchingException = getMatchingChangeException(exception);
                if (matchingException != null) {
                    patchGroupsBeforeSave(matchingException, exception);
                    exception.setObjectID(matchingException.getObjectID());
                    exception.setLastModified(matchingException.getLastModified());
                } else {
                    patchGroupsBeforeSave(appointment, exception);
                    exception.setObjectID(appointment.getObjectID());
                }
                
                exception.removeUid(); // TODO: Needed?
                CalendarDataObject cdo = (CalendarDataObject) exception;
                factory.getCalendarUtilities().removeRecurringType(cdo);
                appointmentSQLInterface.updateAppointmentObject(
                    cdo,
                    exception.getParentFolderID(),
                    appointment.getLastModified());
            }

            for (CalendarDataObject deleteException : deleteExceptionsToSave) {
                Appointment matchingException = getMatchingChangeException(deleteException);
                if (matchingException != null) {
                    deleteException.setObjectID(matchingException.getObjectID());
                    deleteException.setLastModified(matchingException.getLastModified());
                } else {
                    deleteException.setObjectID(appointment.getObjectID());
                }
                appointmentSQLInterface.deleteAppointmentObject(deleteException, parent.getId(), appointment.getLastModified());
            }

        } catch (ClassCastException e) {
            LOG.error(e.getMessage(), e);
            throw new WebdavProtocolException(getUrl(), 500);
        } catch (OXPermissionException e) {
            LOG.error(e.getMessage(), e);
            throw new WebdavProtocolException(getUrl(), 403);
        } catch (OXException e) {
            LOG.error(e.getMessage(), e);
            throw new WebdavProtocolException(getUrl(), 500);
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw new WebdavProtocolException(getUrl(), 500);
        }
    }

    private void checkRange() throws WebdavProtocolException {
        if (!factory.isInRange(appointment)) {
            throw new WebdavProtocolException(getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private Appointment getMatchingChangeException(Appointment exception) {
        List<Appointment> changeExceptions = factory.getState().getChangeExceptions(appointment.getUid(), parent.getId());
        for (Appointment existingException : changeExceptions) {
            if (existingException.getRecurrenceDatePosition().equals(exception.getRecurrenceDatePosition())) {
                return existingException;
            }
        }
        return null;
    }

    public void delete() throws WebdavProtocolException {
        AppointmentSQLInterface appointments = factory.getAppointmentInterface();
        try {
            appointments.deleteAppointmentObject((CalendarDataObject) appointment, parent.getId(), getLastModified());
        } catch (OXException e) {
            LOG.error(e.getMessage(), e);
            throw new WebdavProtocolException(getUrl(), 500);
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw new WebdavProtocolException(getUrl(), 500);
        }
    }

    public boolean exists() throws WebdavProtocolException {
        return exists;
    }

    public InputStream getBody() throws WebdavProtocolException {
        return new ByteArrayInputStream(icalFile());
    }

    private byte[] icalFile() throws WebdavProtocolException {
        if (fileData != null) {
            return fileData;
        }
        if (appointment == null) {
            return new byte[0];
        }
        ICalEmitter icalEmitter = factory.getIcalEmitter();
        ICalSession session = icalEmitter.createSession();
        try {
            icalEmitter.writeAppointment(
                session,
                appointment,
                factory.getContext(),
                new ArrayList<ConversionError>(),
                new ArrayList<ConversionWarning>());
            List<Appointment> changeExceptions = factory.getState().getChangeExceptions(appointment.getUid(), parent.getId());
            for (Appointment exception : changeExceptions) {
                // exception.removeRecurrenceDatePosition(); // Let the client figure this one out.6
                icalEmitter.writeAppointment(
                    session,
                    exception,
                    factory.getContext(),
                    new ArrayList<ConversionError>(),
                    new ArrayList<ConversionWarning>());
            }
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            icalEmitter.writeSession(session, bytes);
            return fileData = bytes.toByteArray();
        } catch (ConversionError e) {
            LOG.error(e.getMessage(), e);
            throw new WebdavProtocolException(getUrl(), 500);
        }

    }

    public String getContentType() throws WebdavProtocolException {
        return "text/calendar";
    }

    public Date getCreationDate() throws WebdavProtocolException {
        return appointment.getCreationDate();
    }

    public String getDisplayName() throws WebdavProtocolException {
        return appointment.getTitle();
    }

    public String getETag() throws WebdavProtocolException {
        if (!exists) {
            return "";
        }
        return "http://www.open-xchange.com/caldav/etags/" + appointment.getObjectID() + "-" + appointment.getLastModified().getTime();
    }

    public String getLanguage() throws WebdavProtocolException {
        return null;
    }

    public Date getLastModified() throws WebdavProtocolException {
        return appointment.getLastModified();
    }

    public Long getLength() throws WebdavProtocolException {
        return (long) icalFile().length;
    }

    public WebdavLock getLock(String token) throws WebdavProtocolException {
        return null;
    }

    public List<WebdavLock> getLocks() throws WebdavProtocolException {
        return Collections.emptyList();
    }

    public WebdavLock getOwnLock(String token) throws WebdavProtocolException {
        return null;
    }

    public List<WebdavLock> getOwnLocks() throws WebdavProtocolException {
        return null;
    }

    public String getSource() throws WebdavProtocolException {
        return null;
    }

    public WebdavPath getUrl() {
        return url;
    }

    public void lock(WebdavLock lock) throws WebdavProtocolException {

    }

    public void save() throws WebdavProtocolException {
        write(false);
    }

    public void setContentType(String type) throws WebdavProtocolException {

    }

    public void setDisplayName(String displayName) throws WebdavProtocolException {
        appointment.setTitle(displayName);
    }

    public void setLanguage(String language) throws WebdavProtocolException {

    }

    public void setLength(Long length) throws WebdavProtocolException {

    }

    public void setSource(String source) throws WebdavProtocolException {

    }

    public void unlock(String token) throws WebdavProtocolException {

    }

    // Patching groupware data

    // TODO: Warum ist das nicht gesetzt?
    private void patchOrganizer() throws WebdavProtocolException {
        String organizer = appointment.getOrganizer();
        if (organizer == null) {
            int createdBy = appointment.getCreatedBy();
            User user = factory.resolveUser(createdBy);
            appointment.setOrganizer(user.getMail());
        }
    }

    // TODO: Warum ist das nicht gesetzt?
    private void patchOrganizersParticipantState() {
        UserParticipant[] users = appointment.getUsers();
        int createdBy = appointment.getCreatedBy();
        Map<Integer, UserParticipant> userMap = new HashMap<Integer, UserParticipant>();
        for (UserParticipant userParticipant : users) {
            int identifier = userParticipant.getIdentifier();
            if (createdBy == identifier && userParticipant.getConfirm() == 0) {
                userParticipant.setConfirm(CalendarObject.ACCEPT);
            }
            userMap.put(identifier, userParticipant);
        }

        Participant[] participants = appointment.getParticipants();
        for (Participant participant : participants) {
            if (UserParticipant.class.isInstance(participant)) {
                UserParticipant userParticipant = (UserParticipant) participant;
                int identifier = userParticipant.getIdentifier();
                if (createdBy == identifier && userParticipant.getConfirm() == 0) {
                    userParticipant.setConfirm(CalendarObject.ACCEPT);
                } else {
                    UserParticipant up = userMap.get(identifier);
                    userParticipant.setConfirm(up.getConfirm());
                    userParticipant.setConfirmMessage(up.getConfirmMessage());
                }
            }
        }
    }

    private void patchSeriesStartAndEnd() {
        if (this.appointment.isMaster()) {
            CalendarCollectionService calUtils = factory.getCalendarUtilities();
            calUtils.safelySetStartAndEndDateForRecurringAppointment((CalendarDataObject) appointment);
            
            if (this.appointment.containsUntil()) {
                this.appointment.setUntil(plusOneDay(appointment.getUntil()));
            }
        }
    }
    
    private Date plusOneDay(Date until) {
        
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(until);
        calendar.setTimeZone(UTC);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        Date oneDayLater = calendar.getTime();
        return oneDayLater;
    }

    
    private void patchGroups() {
        // TODO
        
    }
    
    private void patchGroupsBeforeSave(Appointment old, Appointment update) {
        // TODO
    }

}
