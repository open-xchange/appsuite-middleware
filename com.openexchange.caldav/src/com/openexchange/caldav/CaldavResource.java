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

package com.openexchange.caldav;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.data.conversion.ical.ICalSession;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.ldap.User;
import com.openexchange.log.LogFactory;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;
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
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CaldavResource extends AbstractResource {

	/**
	 * All appointment fields that may be set in iCal files
	 */
    private static final int[] CALDAV_FIELDS = {
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
    private static final int[] RECURRENCE_FIELDS = {
    	Appointment.INTERVAL,
    	Appointment.DAYS,
    	Appointment.DAY_IN_MONTH,
    	Appointment.MONTH,
    	Appointment.RECURRENCE_COUNT,
    	Appointment.UNTIL
    };

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

    public CaldavResource(final CaldavCollection parent, final Appointment appointment, final GroupwareCaldavFactory factory) throws WebdavProtocolException {
        super();
        if (appointment == null) {
            throw new NullPointerException();
        }
        this.exists = true;
        this.parent = parent;
        this.appointment = appointment;
        this.factory = factory;
        final String uid = appointment.getUid();
        if (null != uid && 0 < uid.length()) {
            this.url = parent.getUrl().dup().append(uid + ".ics");
        } else {
        	//TODO: generate and save missing UUID on demand 
        	this.url = parent.getUrl().dup().append(appointment.getObjectID() + ".ics");
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
    public boolean hasBody() throws WebdavProtocolException {
        return true;
    }

    @Override
    protected List<WebdavProperty> internalGetAllProps() throws WebdavProtocolException {
        return Collections.emptyList();
    }

    @Override
    protected WebdavProperty internalGetProperty(final String namespace, final String name) throws WebdavProtocolException {
        if (namespace.equals(CaldavProtocol.CAL_NS.getURI()) && name.equals("calendar-data")) {
            final WebdavProperty property = new WebdavProperty(namespace, name);
            try {
                property.setValue(new String(icalFile(), "UTF-8"));
            } catch (final UnsupportedEncodingException e) {
                throw WebdavProtocolException.generalError(e, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            return property;
        }
        return null;
    }

    @Override
    protected void internalPutProperty(final WebdavProperty prop) throws WebdavProtocolException {

    }

    @Override
    protected void internalRemoveProperty(final String namespace, final String name) throws WebdavProtocolException {

    }

    @Override
    protected boolean isset(final Property p) {
        return true;
    }

    @Override
    public void putBody(final InputStream body, final boolean guessSize) throws WebdavProtocolException {
        this.loadCompletely();
        this.fileData = null;
        final Appointment oldAppointment = appointment;
        List<CalendarDataObject> appointments = null;
        try {
			appointments = this.parse(body);
		} catch (final ConversionError e) {
			LOG.error(e.getMessage(), e);
            throw WebdavProtocolException.generalError(e, getUrl(), HttpServletResponse.SC_BAD_REQUEST);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
            throw WebdavProtocolException.generalError(e, getUrl(), HttpServletResponse.SC_BAD_REQUEST);
		}
        if (null != appointments && !appointments.isEmpty()) {
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
                    cdo.setShownAs(Appointment.RESERVED);
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
    }

    private void checkForExplicitRemoves(final Appointment oldAppointment, final CalendarDataObject cdo) {
    	/*
    	 * reset previously set appointment fields
    	 */
        for (final int field : CaldavResource.CALDAV_FIELDS) {
        	if (oldAppointment.contains(field) && false == cdo.contains(field)) {
        		cdo.set(field, cdo.get(field)); 
        	}
		}
    	if (CalendarObject.NO_RECURRENCE != oldAppointment.getRecurrenceType() && 
    			CalendarObject.NO_RECURRENCE != cdo.getRecurrenceType()) {
        	/*
        	 * reset previously set recurrence specific fields
        	 */
            for (final int field : CaldavResource.RECURRENCE_FIELDS) {
            	if (oldAppointment.contains(field) && false == cdo.contains(field)) {
            		cdo.set(field, Appointment.UNTIL == field ? null : cdo.get(field)); // getUntil returns 'max until date' if not set 
            	}
    		}
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
    public void setCreationDate(final Date date) throws WebdavProtocolException {

    }

    @Override
	public void create() throws WebdavProtocolException {
        write(true);
    }

    @Override
    public WebdavResource move(final WebdavPath dest, final boolean noroot, final boolean overwrite) throws WebdavProtocolException {
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

    private void write(final boolean create) throws WebdavProtocolException {
        try {
            final CalendarDataObject toSave = (CalendarDataObject) appointment;
            toSave.setIgnoreConflicts(true);
            final AppointmentSQLInterface appointmentSQLInterface = factory.getAppointmentInterface();
            if (create) {
                appointmentSQLInterface.insertAppointmentObject(toSave);
            } else {
                Appointment oldAppointment = factory.getState().get(appointment.getObjectID(), parent.getId());
            	if (false == Patches.Incoming.tryRestoreParticipants(factory.getState().getUnpatched(oldAppointment), toSave)) {
                    patchResources(oldAppointment, toSave);
                    patchParticipantListRemovingAliases(toSave);
                    patchParticipantListRemovingDoubleUsers(toSave);
            	}
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
                cdo.setIgnoreConflicts(true);
                if (create || false == Patches.Incoming.tryRestoreParticipants(null != matchingException ? matchingException :
                	factory.getState().getUnpatched(appointment), exception)) {
                    patchParticipantListRemovingAliases(toSave);
                    patchParticipantListRemovingDoubleUsers(toSave);
                }
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
            throw WebdavProtocolException.generalError(e, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
        	if (Category.CATEGORY_PERMISSION_DENIED.equals(e.getCategory())) {
                throw WebdavProtocolException.generalError(e, getUrl(), HttpServletResponse.SC_FORBIDDEN);
        	} else {
                throw WebdavProtocolException.generalError(e, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        	}
        } catch (final SQLException e) {
            throw WebdavProtocolException.generalError(e, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
    }

    private Appointment getMatchingChangeException(final Appointment exception) throws WebdavProtocolException {
        final List<Appointment> changeExceptions = factory.getState().getChangeExceptionsComplete(I(appointment.getObjectID()), parent.getId());
        for (final Appointment existingException : changeExceptions) {
            if (existingException.getRecurrenceDatePosition().equals(exception.getRecurrenceDatePosition())) {
                return existingException;
            }
        }
        return null;
    }

    @Override
	public void delete() throws WebdavProtocolException {
        final AppointmentSQLInterface appointments = factory.getAppointmentInterface();
        try {
            appointments.deleteAppointmentObject((CalendarDataObject) appointment, parent.getId(), getLastModified());
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            throw WebdavProtocolException.generalError(e, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (final SQLException e) {
            LOG.error(e.getMessage(), e);
            throw WebdavProtocolException.generalError(e, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
    }

    @Override
	public boolean exists() throws WebdavProtocolException {
        return exists;
    }

    @Override
	public InputStream getBody() throws WebdavProtocolException {
        return new ByteArrayInputStream(icalFile());
    }

    private void loadCompletely() throws WebdavProtocolException {
        if (!exists) {
            return;
        }
        appointment = factory.getState().getComplete(appointment.getObjectID(), appointment.getParentFolderID());
        if (!factory.getState().hasBeenPatched(appointment)) {
            factory.getState().markAsPatched(appointment);
            patchAlarmInSharedFolder();
            patchGroups(appointment);
            patchOrganizer(appointment);
            patchOrganizersParticipantState(appointment);
            patchSeriesStartAndEnd();
        }
    }

    private byte[] icalFile() throws WebdavProtocolException {
        loadCompletely();
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
            final List<Appointment> changeExceptions = factory.getState().getChangeExceptionsComplete(I(appointment.getObjectID()), parent.getId());
            for (final Appointment exception : changeExceptions) {
                patchGroups(exception);
                patchOrganizer(exception);
                patchOrganizersParticipantState(exception);

                // exception.removeRecurrenceDatePosition(); // Let the client figure this one out.
                icalEmitter.writeAppointment(
                    session,
                    exception,
                    factory.getContext(),
                    new ArrayList<ConversionError>(),
                    new ArrayList<ConversionWarning>());
            }
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            icalEmitter.writeSession(session, bytes);
            /*
             * apply patches
             */
            String ical = new String(bytes.toByteArray(), "UTF-8");
            ical = Patches.Outgoing.applyAll(ical);
            return fileData = ical.getBytes("UTF-8");
        } catch (final ConversionError e) {
            LOG.error(e.getMessage(), e);
            throw WebdavProtocolException.generalError(e, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (final UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
            throw WebdavProtocolException.generalError(e, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (ParseException e) {
            LOG.error(e.getMessage(), e);
            throw WebdavProtocolException.generalError(e, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
    }

    @Override
	public String getContentType() throws WebdavProtocolException {
        return "text/calendar";
    }

    @Override
	public Date getCreationDate() throws WebdavProtocolException {
        return appointment.getCreationDate();
    }

    @Override
	public String getDisplayName() throws WebdavProtocolException {
        return appointment.getTitle();
    }

    @Override
	public String getETag() throws WebdavProtocolException {
        if (!exists) {
            return "";
        }
        return "http://www.open-xchange.com/caldav/etags/" + appointment.getObjectID() + "-" + appointment.getLastModified().getTime();
    }

    @Override
	public String getLanguage() throws WebdavProtocolException {
        return null;
    }

    @Override
	public Date getLastModified() throws WebdavProtocolException {
        return appointment.getLastModified();
    }

    @Override
	public Long getLength() throws WebdavProtocolException {
        return L(icalFile().length);
    }

    @Override
	public WebdavLock getLock(final String token) throws WebdavProtocolException {
        return null;
    }

    @Override
	public List<WebdavLock> getLocks() throws WebdavProtocolException {
        return Collections.emptyList();
    }

    @Override
	public WebdavLock getOwnLock(final String token) throws WebdavProtocolException {
        return null;
    }

    @Override
	public List<WebdavLock> getOwnLocks() throws WebdavProtocolException {
        return null;
    }

    @Override
	public String getSource() throws WebdavProtocolException {
        return null;
    }

    @Override
	public WebdavPath getUrl() {
        return url;
    }

    @Override
	public void lock(final WebdavLock lock) throws WebdavProtocolException {

    }

    @Override
	public void save() throws WebdavProtocolException {
        write(false);
    }

    @Override
	public void setContentType(final String type) throws WebdavProtocolException {

    }

    @Override
	public void setDisplayName(final String displayName) throws WebdavProtocolException {
        appointment.setTitle(displayName);
    }

    @Override
	public void setLanguage(final String language) throws WebdavProtocolException {

    }

    @Override
	public void setLength(final Long length) throws WebdavProtocolException {

    }

    @Override
	public void setSource(final String source) throws WebdavProtocolException {

    }

    @Override
	public void unlock(final String token) throws WebdavProtocolException {

    }

    // Patching groupware data

    // TODO: Warum ist das nicht gesetzt?
    private void patchOrganizer(Appointment appointment) throws WebdavProtocolException {
        final String organizer = appointment.getOrganizer();
        if (organizer == null) {
            final int createdBy = appointment.getCreatedBy();
            final User user = factory.resolveUser(createdBy);
            appointment.setOrganizer(user.getMail());
        }
    }

    // TODO: Warum ist das nicht gesetzt?
    private void patchOrganizersParticipantState(Appointment appointment) {
        final UserParticipant[] users = appointment.getUsers();
        final int createdBy = appointment.getCreatedBy();
        final TIntObjectMap<UserParticipant> userMap = new TIntObjectHashMap<UserParticipant>();
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
                    if (up != null) {
                        userParticipant.setConfirm(up.getConfirm());
                        userParticipant.setConfirmMessage(up.getConfirmMessage());
                    }
                }
            }
        }
    }

    private void patchSeriesStartAndEnd() {
        final CalendarCollectionService calUtils = factory.getCalendarUtilities();
        calUtils.safelySetStartAndEndDateForRecurringAppointment((CalendarDataObject) appointment);
    }

    private void patchGroups(Appointment appointment) {
        // We want to add all user participants to the participants list and remove all group participants

        final Set<Integer> guardian = new HashSet<Integer>();
        final List<Participant> newParticipants = new ArrayList<Participant>();

        final Participant[] participants = appointment.getParticipants();
        for (final Participant participant : participants) {
            if (UserParticipant.class.isInstance(participant)) {
                final UserParticipant userParticipant = (UserParticipant) participant;
                guardian.add(I(userParticipant.getIdentifier()));
                newParticipants.add(userParticipant);
            } else if (!GroupParticipant.class.isInstance(participant)) {
                newParticipants.add(participant);
            }
        }

        final UserParticipant[] users = appointment.getUsers();
        for (final UserParticipant userParticipant : users) {
            if (!guardian.contains(I(userParticipant.getIdentifier()))) {
                newParticipants.add(userParticipant);
            }
        }

        appointment.setParticipants(newParticipants);
    }

    // Incoming
    private void patchResources(final Appointment old, final Appointment update) {
        // We want to add all ResourceParticipants from the oldAppointment to the update, effectively disallowing modification of resources
        final Set<Integer> guardian = new HashSet<Integer>();
        final List<Participant> newParticipants = new ArrayList<Participant>();

        Participant[] participants = update.getParticipants();
        if (participants == null) {
            return;
        }
        for (final Participant participant : participants) {
            if (ResourceParticipant.class.isInstance(participant)) {
                guardian.add(I(participant.getIdentifier()));
            }
            newParticipants.add(participant);
        }

        participants = old.getParticipants();
        for (final Participant participant : participants) {
            if (ResourceParticipant.class.isInstance(participant) && !guardian.contains(I(participant.getIdentifier()))) {
                newParticipants.add(participant);
            }
        }

        update.setParticipants(newParticipants);
    }

    private void patchAlarmInSharedFolder() {
        if (parent.isShared()) {
            appointment.removeAlarm();
        }
    }

    // Incoming
    private void patchParticipantListRemovingAliases(final Appointment update) throws WebdavProtocolException {
        // Firstly, let's build a Set of all aliases that are already taking part in this appointment
        final Set<String> knownInternalMailAddresses = new HashSet<String>();
        final Participant[] participants = update.getParticipants();
        if (participants == null) {
            return;
        }
        for (final Participant participant : participants) {
            if (UserParticipant.class.isInstance(participant)) {
                final UserParticipant up = (UserParticipant) participant;
                final int userId = up.getIdentifier();
                final User user = factory.resolveUser(userId);
                if (user.getAliases() != null) {
                    knownInternalMailAddresses.addAll(Arrays.asList(user.getAliases()));
                }
                knownInternalMailAddresses.add(user.getMail());
            }
        }
        final List<Participant> prunedParticipants = new ArrayList<Participant>(participants.length);
        for (final Participant participant : participants) {
            if (ExternalUserParticipant.class.isInstance(participant)) {
                final ExternalUserParticipant external = (ExternalUserParticipant) participant;
                final String emailAddress = external.getEmailAddress();
                if (!knownInternalMailAddresses.contains(emailAddress)) {
                    prunedParticipants.add(participant);
                }
            } else {
                prunedParticipants.add(participant);
            }
        }

        update.setParticipants(prunedParticipants);
    }

    private void patchParticipantListRemovingDoubleUsers(final Appointment update) {
        final Set<Integer> users = new HashSet<Integer>();
        final Participant[] participants = update.getParticipants();
        final List<Participant> uniqueParticipants = new ArrayList<Participant>();

        if (participants == null) {
            return;
        }
        for (final Participant participant : participants) {
            if (UserParticipant.class.isInstance(participant)) {
                final UserParticipant up = (UserParticipant) participant;
                if (users.add(I(up.getIdentifier()))) {
                    uniqueParticipants.add(participant);
                }
            } else {
                uniqueParticipants.add(participant);
            }
        }
        update.setParticipants(uniqueParticipants);
    }

    // Incoming
    /*private void patchParticipantListByTurningExternalParticipantsIntoInternalParticipants(Appointment update) {

    }*/
    
    private List<CalendarDataObject> parse(final InputStream body) throws IOException, ConversionError {
    	try {
            final int buflen = 2048;
            final byte[] buf = new byte[buflen];
            final UnsynchronizedByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream(8192);
            for (int read = body.read(buf, 0, buflen); read > 0; read = body.read(buf, 0, buflen)) {
                baos.write(buf, 0, read);
            }
            final String iCal = new String(baos.toByteArray(), "UTF-8");
            return this.parse(iCal);
        } finally {
            try {
                body.close();
            } catch (final IOException e) {
                LOG.error(e);
            }
        }
    }

    private List<CalendarDataObject> parse(final String iCal) throws ConversionError {
    	/*
    	 * apply patches
    	 */
    	final String patchedICal = Patches.Incoming.applyAll(iCal);    	
    	/*
    	 * parse appointments
    	 */
        final ICalParser parser = factory.getIcalParser();
        return parser.parseAppointments(
        		patchedICal, UTC, factory.getContext(), new ArrayList<ConversionError>(), new ArrayList<ConversionWarning>());
    }
    
}
