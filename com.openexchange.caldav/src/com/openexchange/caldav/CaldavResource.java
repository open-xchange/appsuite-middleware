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
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.data.conversion.ical.ICalSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.ldap.User;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.helpers.AbstractResource;

/**
 * A {@link CaldavResource} bridges an OX appointment to a caldav resource.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CaldavResource extends AbstractResource {

    private static final Log LOG = LogFactory.getLog(CaldavResource.class);

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private final GroupwareCaldavFactory factory;

    private Appointment appointment;

    private CaldavCollection parent;

    private final WebdavPath url;

    private boolean exists = false;

    private byte[] fileData;

    private final List<CalendarDataObject> exceptionsToSave = new ArrayList<CalendarDataObject>();

    private final List<CalendarDataObject> deleteExceptionsToSave = new ArrayList<CalendarDataObject>();

    public CaldavResource(final CaldavCollection parent, final Appointment appointment, final GroupwareCaldavFactory factory) throws OXException {
        super();
        if (appointment == null) {
            throw new NullPointerException();
        }
        this.exists = true;
        this.parent = parent;
        this.appointment = appointment;
        this.factory = factory;
        this.url = parent.getUrl().dup().append(appointment.getUid() + ".ics");

        if (!factory.getState().hasBeenPatched(appointment)) {
            factory.getState().markAsPatched(appointment);
            patchGroups();
            patchOrganizer();
            patchOrganizersParticipantState();
            patchSeriesStartAndEnd();
        }
    }

    public CaldavResource(final CaldavCollection parent, final WebdavPath url, final GroupwareCaldavFactory factory) {
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
    public boolean hasBody() throws OXException {
        return true;
    }

    @Override
    protected List<WebdavProperty> internalGetAllProps() throws OXException {
        return Collections.emptyList();
    }

    @Override
    protected WebdavProperty internalGetProperty(final String namespace, final String name) throws OXException {
        if (namespace.equals(CaldavProtocol.CAL_NS.getURI()) && name.equals("calendar-data")) {
            final WebdavProperty property = new WebdavProperty(namespace, name);
            property.setValue(new String(icalFile(), com.openexchange.java.Charsets.UTF_8));
            return property;
        }
        return null;
    }

    @Override
    protected void internalPutProperty(final WebdavProperty prop) throws OXException {

    }

    @Override
    protected void internalRemoveProperty(final String namespace, final String name) throws OXException {

    }

    @Override
    protected boolean isset(final Property p) {
        return true;
    }

    @Override
    public void putBody(final InputStream body, final boolean guessSize) throws OXException {
        fileData = null;
        final ICalParser parser = factory.getIcalParser();
        try {
            final List<CalendarDataObject> appointments = parser.parseAppointments(
                body,
                UTC,
                factory.getContext(),
                new ArrayList<ConversionError>(),
                new ArrayList<ConversionWarning>());
            final Appointment oldAppointment = appointment;
            if (!appointments.isEmpty()) {
                if (appointments.size() == 1) {
                    final CalendarDataObject cdo = appointments.get(0);
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
                    for (final CalendarDataObject cdo : appointments) {
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
        } catch (final ConversionError e) {
            LOG.error(e.getMessage(), e);
            throw WebdavProtocolException.generalError(getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void checkForExplicitRemoves(final Appointment oldAppointment, final CalendarDataObject cdo) {
        if (oldAppointment.getFullTime() && !cdo.getFullTime() && !cdo.containsFullTime()) {
            cdo.setFullTime(false); // Set this explicitly, so the update actually changes it
        }

        if (oldAppointment.getRecurrenceType() != CalendarObject.NO_RECURRENCE && !cdo.containsRecurrenceCount() && !cdo.containsUntil()) {
            cdo.setUntil(cdo.getUntil()); // Again, set this explicitly if this changed.
        }
    }

    private void createNewDeleteExceptions(final Appointment oldAppointment, final CalendarDataObject cdo) {
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
            deleteException.setParentFolderID(parent.getId());
            deleteExceptionsToSave.add(deleteException);
        }

        cdo.removeDeleteExceptions();

    }

    private boolean looksLikeMaster(final CalendarDataObject cdo) {
        return cdo.containsRecurrenceType() && cdo.getRecurrenceType() != CalendarObject.NO_RECURRENCE;
    }

    @Override
    public void setCreationDate(final Date date) throws OXException {

    }

    @Override
    public void create() throws OXException {
        checkRange();
        write(true);
    }

    @Override
    public WebdavResource move(final WebdavPath dest, final boolean noroot, final boolean overwrite) throws OXException {
        final WebdavResource destinationResource = factory.resolveResource(dest);
        CaldavCollection destinationCollection;
        if (destinationResource.isCollection()) {
            destinationCollection = (CaldavCollection) destinationResource;
        } else {
            destinationCollection = (CaldavCollection) factory.resolveCollection(dest.parent());
        }
        final CalendarDataObject moveOp = new CalendarDataObject();
        moveOp.setObjectID(appointment.getObjectID());
        moveOp.setParentFolderID(destinationCollection.getId());
        moveOp.setContext(factory.getContext());
        moveOp.setLastModified(appointment.getLastModified());
        appointment = moveOp;
        write(false);
        parent = destinationCollection;
        return this;
    }

    private void write(final boolean create) throws OXException {
        try {
            final CalendarDataObject toSave = (CalendarDataObject) appointment;
            final AppointmentSQLInterface appointmentSQLInterface = factory.getAppointmentInterface();
            if (create) {
                appointmentSQLInterface.insertAppointmentObject(toSave);
            } else {
                final Appointment oldAppointment = factory.getState().get(appointment.getUid(), appointment.getParentFolderID());
                patchResources(oldAppointment, toSave);
                appointmentSQLInterface.updateAppointmentObject(toSave, parent.getId(), toSave.getLastModified());
            }

            // Exceptions may not change resource participants, so don't patch them here
            for (final Appointment exception : exceptionsToSave) {
                final Appointment matchingException = getMatchingChangeException(exception);
                if (matchingException != null) {
                    exception.setObjectID(matchingException.getObjectID());
                    exception.setLastModified(matchingException.getLastModified());
                } else {
                    exception.setObjectID(appointment.getObjectID());
                }

                exception.removeUid(); // TODO: Needed?
                final CalendarDataObject cdo = (CalendarDataObject) exception;
                factory.getCalendarUtilities().removeRecurringType(cdo);
                appointmentSQLInterface.updateAppointmentObject(cdo, exception.getParentFolderID(), appointment.getLastModified());
            }

            for (final CalendarDataObject deleteException : deleteExceptionsToSave) {
                final Appointment matchingException = getMatchingChangeException(deleteException);
                if (matchingException != null) {
                    deleteException.setObjectID(matchingException.getObjectID());
                    deleteException.setLastModified(matchingException.getLastModified());
                } else {
                    deleteException.setObjectID(appointment.getObjectID());
                }
                appointmentSQLInterface.deleteAppointmentObject(deleteException, parent.getId(), appointment.getLastModified());
            }

        } catch (final ClassCastException e) {
            LOG.error(e.getMessage(), e);
            throw WebdavProtocolException.generalError(getUrl(), 500);
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            throw WebdavProtocolException.generalError(getUrl(), 500);
        } catch (final SQLException e) {
            LOG.error(e.getMessage(), e);
            throw WebdavProtocolException.generalError(getUrl(), 500);
        }
    }

    private void checkRange() throws OXException {
        if (!factory.isInRange(appointment)) {
            throw WebdavProtocolException.generalError(getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private Appointment getMatchingChangeException(final Appointment exception) {
        final List<Appointment> changeExceptions = factory.getState().getChangeExceptions(appointment.getUid(), parent.getId());
        for (final Appointment existingException : changeExceptions) {
           if (existingException.getRecurrenceDatePosition().equals(exception.getRecurrenceDatePosition())) {
                return existingException;
            }
        }
        return null;
    }

    @Override
    public void delete() throws OXException {
        final AppointmentSQLInterface appointments = factory.getAppointmentInterface();
        try {
            appointments.deleteAppointmentObject((CalendarDataObject) appointment, parent.getId(), getLastModified());
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            throw WebdavProtocolException.generalError(getUrl(), 500);
        } catch (final SQLException e) {
            LOG.error(e.getMessage(), e);
            throw WebdavProtocolException.generalError(getUrl(), 500);
        }
    }

    @Override
    public boolean exists() throws OXException {
        return exists;
    }

    @Override
    public InputStream getBody() throws OXException {
        return new ByteArrayInputStream(icalFile());
    }

    private byte[] icalFile() throws OXException {
        if (fileData != null) {
            return fileData;
        }
        if (appointment == null) {
            return new byte[0];
        }
        final ICalEmitter icalEmitter = factory.getIcalEmitter();
        final ICalSession session = icalEmitter.createSession();
        try {
            icalEmitter.writeAppointment(
                session,
                appointment,
                factory.getContext(),
                new ArrayList<ConversionError>(),
                new ArrayList<ConversionWarning>());
            final List<Appointment> changeExceptions = factory.getState().getChangeExceptions(appointment.getUid(), parent.getId());
            for (final Appointment exception : changeExceptions) {
                // exception.removeRecurrenceDatePosition(); // Let the client figure this one out.6
                icalEmitter.writeAppointment(
                    session,
                    exception,
                    factory.getContext(),
                    new ArrayList<ConversionError>(),
                    new ArrayList<ConversionWarning>());
            }
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            icalEmitter.writeSession(session, bytes);
            return fileData = bytes.toByteArray();
        } catch (final ConversionError e) {
            LOG.error(e.getMessage(), e);
            throw WebdavProtocolException.generalError(getUrl(), 500);
        }

    }

    @Override
    public String getContentType() throws OXException {
        return "text/calendar";
    }

    @Override
    public Date getCreationDate() throws OXException {
        return appointment.getCreationDate();
    }

    @Override
    public String getDisplayName() throws OXException {
        return appointment.getTitle();
    }

    @Override
    public String getETag() throws OXException {
        if (!exists) {
            return "";
        }
        return "http://www.open-xchange.com/caldav/etags/" + appointment.getObjectID() + "-" + appointment.getLastModified().getTime();
    }

    @Override
    public String getLanguage() throws OXException {
        return null;
    }

    @Override
    public Date getLastModified() throws OXException {
        return appointment.getLastModified();
    }

    @Override
    public Long getLength() throws OXException {
        return (long) icalFile().length;
    }

    @Override
    public WebdavLock getLock(final String token) throws OXException {
        return null;
    }

    @Override
    public List<WebdavLock> getLocks() throws OXException {
        return Collections.emptyList();
    }

    @Override
    public WebdavLock getOwnLock(final String token) throws OXException {
        return null;
    }

    @Override
    public List<WebdavLock> getOwnLocks() throws OXException {
        return null;
    }

    @Override
    public String getSource() throws OXException {
        return null;
    }

    @Override
    public WebdavPath getUrl() {
        return url;
    }

    @Override
    public void lock(final WebdavLock lock) throws OXException {

    }

    @Override
    public void save() throws OXException {
        write(false);
    }

    @Override
    public void setContentType(final String type) throws OXException {

    }

    @Override
    public void setDisplayName(final String displayName) throws OXException {
        appointment.setTitle(displayName);
    }

    @Override
    public void setLanguage(final String language) throws OXException {

    }

    @Override
    public void setLength(final Long length) throws OXException {

    }

    @Override
    public void setSource(final String source) throws OXException {

    }

    @Override
    public void unlock(final String token) throws OXException {

    }

    // Patching groupware data

    // TODO: Warum ist das nicht gesetzt?
    private void patchOrganizer() throws OXException {
        final String organizer = appointment.getOrganizer();
        if (organizer == null) {
            final int createdBy = appointment.getCreatedBy();
            final User user = factory.resolveUser(createdBy);
            appointment.setOrganizer(user.getMail());
        }
    }

    // TODO: Warum ist das nicht gesetzt?
    private void patchOrganizersParticipantState() {
        final UserParticipant[] users = appointment.getUsers();
        final int createdBy = appointment.getCreatedBy();
        final Map<Integer, UserParticipant> userMap = new HashMap<Integer, UserParticipant>();
        for (final UserParticipant userParticipant : users) {
            final int identifier = userParticipant.getIdentifier();
            if (createdBy == identifier && userParticipant.getConfirm() == 0) {
                userParticipant.setConfirm(CalendarObject.ACCEPT);
            }
            userMap.put(identifier, userParticipant);
        }

        final Participant[] participants = appointment.getParticipants();
        for (final Participant participant : participants) {
            if (UserParticipant.class.isInstance(participant)) {
                final UserParticipant userParticipant = (UserParticipant) participant;
                final int identifier = userParticipant.getIdentifier();
                if (createdBy == identifier && userParticipant.getConfirm() == 0) {
                    userParticipant.setConfirm(CalendarObject.ACCEPT);
                } else {
                    final UserParticipant up = userMap.get(identifier);
                    userParticipant.setConfirm(up.getConfirm());
                    userParticipant.setConfirmMessage(up.getConfirmMessage());
                }
            }
        }
    }

    private void patchSeriesStartAndEnd() {
        final CalendarCollectionService calUtils = factory.getCalendarUtilities();
        calUtils.safelySetStartAndEndDateForRecurringAppointment((CalendarDataObject) appointment);

        if (this.appointment.containsUntil()) {
            this.appointment.setUntil(plusOneDay(appointment.getUntil()));
        }
    }

    private Date plusOneDay(final Date until) {

        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(until);
        calendar.setTimeZone(UTC);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        final Date oneDayLater = calendar.getTime();
        return oneDayLater;
    }

    private void patchGroups() {
        // We want to add all user participants to the participants list and remove all group participants

        final Set<Integer> guardian = new HashSet<Integer>();
        final List<Participant> newParticipants = new ArrayList<Participant>();

        final Participant[] participants = appointment.getParticipants();
        for (final Participant participant : participants) {
            if (UserParticipant.class.isInstance(participant)) {
                final UserParticipant userParticipant = (UserParticipant) participant;
                guardian.add(userParticipant.getIdentifier());
                newParticipants.add(userParticipant);
            } else if (!GroupParticipant.class.isInstance(participant)) {
                newParticipants.add(participant);
            }
        }

        final UserParticipant[] users = appointment.getUsers();
        for (final UserParticipant userParticipant : users) {
            if (!guardian.contains(userParticipant.getIdentifier())) {
                newParticipants.add(userParticipant);
            }
        }

        appointment.setParticipants(newParticipants);
    }

    private void patchResources(final Appointment old, final Appointment update) {
        // We want to add all ResourceParticipants from the oldAppointment to the update, effectively disallowing modification of resources
        final Set<Integer> guardian = new HashSet<Integer>();
        final List<Participant> newParticipants = new ArrayList<Participant>();

        Participant[] participants = update.getParticipants();
        for (final Participant participant : participants) {
            if (ResourceParticipant.class.isInstance(participant)) {
                guardian.add(participant.getIdentifier());
            }
            newParticipants.add(participant);
        }

        participants = old.getParticipants();
        for (final Participant participant : participants) {
            if (ResourceParticipant.class.isInstance(participant) && !guardian.contains(participant.getIdentifier())) {
                newParticipants.add(participant);
            }
        }

        update.setParticipants(newParticipants);
    }

}
