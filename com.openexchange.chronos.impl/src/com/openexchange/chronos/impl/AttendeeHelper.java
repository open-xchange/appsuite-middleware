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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.impl;

import static com.openexchange.chronos.impl.CalendarUtils.applyProperties;
import static com.openexchange.chronos.impl.CalendarUtils.contains;
import static com.openexchange.chronos.impl.CalendarUtils.filter;
import static com.openexchange.chronos.impl.CalendarUtils.find;
import static com.openexchange.chronos.impl.CalendarUtils.getCalAddress;
import static com.openexchange.chronos.impl.CalendarUtils.getCalendarUser;
import static com.openexchange.chronos.impl.CalendarUtils.i;
import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarSession;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.compat.Appointment2Event;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceService;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.user.UserService;

/**
 * {@link AttendeeHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class AttendeeHelper {

    /** The constant used to indicate a common "public" parent folder for internal user attendees */
    static final int ATTENDEE_PUBLIC_FOLDER_ID = -2;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AttendeeHelper.class);


    private final CalendarSession session;
    private final UserizedFolder folder;
    private final List<Attendee> attendeesToInsert;
    private final List<Attendee> attendeesToDelete;
    private final List<Attendee> attendeesToUpdate;

    /**
     * Initializes a new {@link AttendeeHelper}.
     *
     * @param session The calendar session
     * @param folder The parent folder of the event being processed
     * @param originalAttendees The original list of attendees, or <code>null</code> for newly created events
     * @param updatedAttendees The new/updated list of attendees, or <code>null</code> for events being deleted
     */
    public AttendeeHelper(CalendarSession session, UserizedFolder folder, List<Attendee> originalAttendees, List<Attendee> updatedAttendees) throws OXException {
        this.session = session;
        this.folder = folder;
        this.attendeesToInsert = new ArrayList<Attendee>();
        this.attendeesToDelete = new ArrayList<Attendee>();
        this.attendeesToUpdate = new ArrayList<Attendee>();
        if (null == originalAttendees || 0 == originalAttendees.size()) {
            processNewEvent(null == updatedAttendees ? Collections.<Attendee> emptyList() : updatedAttendees);
        } else if (null == updatedAttendees || 0 == updatedAttendees.size()) {
            processDeletedEvent(originalAttendees);
        } else {
            processUpdatedEvent(originalAttendees, updatedAttendees);
        }
    }

    /**
     * Gets a list of newly added attendees that should be inserted.
     *
     * @return The attendees, or an empty list if there are none
     */
    public List<Attendee> getAttendeesToInsert() throws OXException {
        return attendeesToInsert;
    }

    /**
     * Gets a list of removed attendees that should be deleted.
     *
     * @return The attendees, or an empty list if there are none
     */
    public List<Attendee> getAttendeesToDelete() {
        return attendeesToDelete;
    }

    /**
     * Gets a list of modified added attendees that should be updated.
     *
     * @return The attendees, or an empty list if there are none
     */
    public List<Attendee> getAttendeesToUpdate() {
        return attendeesToUpdate;
    }

    private void processNewEvent(List<Attendee> requestedAttendees) throws OXException {
        /*
         * always add attendee for default calendar user in folder
         */
        Attendee defaultAttendee = getDefaultAttendee(folder, requestedAttendees);
        attendeesToInsert.add(defaultAttendee);
        if (null == requestedAttendees || 0 == requestedAttendees.size()) {
            return;// no further attendees
        }
        /*
         * prepare & add all further attendees
         */
        List<Attendee> attendeeList = new ArrayList<Attendee>();
        attendeeList.add(defaultAttendee);
        attendeesToInsert.addAll(prepareNewAttendees(attendeeList, requestedAttendees));
    }

    private void processUpdatedEvent(List<Attendee> originalAttendees, List<Attendee> updatedAttendees) throws OXException {
        AttendeeDiff attendeeDiff = new AttendeeDiff(originalAttendees, updatedAttendees);
        List<Attendee> attendeeList = new ArrayList<Attendee>(originalAttendees);
        /*
         * delete removed attendees
         */
        for (Attendee removedAttendee : attendeeDiff.getRemovedAttendees()) {
            if (false == PublicType.getInstance().equals(folder.getType()) && removedAttendee.getEntity() == folder.getCreatedBy()) {
                // preserve calendar user in personal folders
                LOG.info("Implicitly preserving default calendar user {} in personal folder {}.", I(removedAttendee.getEntity()), folder);
                continue;
            }
            attendeeList.remove(removedAttendee);
            attendeesToDelete.add(removedAttendee);
        }
        /*
         * apply updated attendee data
         */
        for (Attendee[] updatedAttendee : attendeeDiff.getUpdatedAttendees()) {
            Attendee attendeeUpdate = new Attendee();
            AttendeeMapper.getInstance().copy(updatedAttendee[0], attendeeUpdate, AttendeeField.ENTITY, AttendeeField.MEMBER, AttendeeField.CU_TYPE, AttendeeField.URI);
            AttendeeMapper.getInstance().copy(updatedAttendee[1], attendeeUpdate, AttendeeField.RSVP, AttendeeField.COMMENT, AttendeeField.PARTSTAT, AttendeeField.ROLE);
            attendeesToUpdate.add(attendeeUpdate);
        }
        /*
         * prepare & add all new attendees
         */
        attendeesToInsert.addAll(prepareNewAttendees(attendeeList, attendeeDiff.getAddedAttendees()));
    }

    private void processDeletedEvent(List<Attendee> originalAttendees) {
        attendeesToDelete.addAll(originalAttendees);
    }

    private List<Attendee> prepareNewAttendees(List<Attendee> existingAttendees, List<Attendee> newAttendees) throws OXException {
        List<Attendee> attendees = new ArrayList<Attendee>(newAttendees.size());
        /*
         * add internal user attendees
         */
        for (Attendee attendee : filter(newAttendees, Boolean.TRUE, CalendarUserType.INDIVIDUAL)) {
            if (contains(existingAttendees, attendee) || contains(attendees, attendee)) {
                LOG.debug("Skipping duplicate user attendee {}", attendee);
                continue;
            }
            attendees.add(getAttendeeForInsert(getUser(attendee.getEntity()), attendee, folder.getType()));
        }
        /*
         * resolve & add any internal group attendees
         */
        for (Attendee attendee : filter(newAttendees, Boolean.TRUE, CalendarUserType.GROUP)) {
            if (contains(existingAttendees, attendee) || contains(attendees, attendee)) {
                LOG.debug("Skipping duplicate group attendee {}", attendee);
                continue;
            }
            Group group = getGroup(attendee.getEntity());
            Attendee preparedAttendee = getAttendeeForInsert(group, attendee);
            attendees.add(preparedAttendee);
            for (int memberID : group.getMember()) {
                if (contains(existingAttendees, memberID) || contains(attendees, memberID)) {
                    LOG.debug("Skipping explicitly added group member {}", I(memberID));
                    continue;
                }
                Attendee memberAttendee = getAttendeeForInsert(getUser(memberID), null, folder.getType());
                memberAttendee.setMember(CalendarUtils.getCalAddress(session.getContext().getContextId(), group));
                attendees.add(memberAttendee);
            }
        }
        /*
         * resolve & add any internal resource attendees
         */
        for (Attendee attendee : filter(newAttendees, Boolean.TRUE, CalendarUserType.RESOURCE)) {
            if (contains(existingAttendees, attendee) || contains(attendees, attendee)) {
                LOG.debug("Skipping duplicate resource attendee {}", attendee);
                continue;
            }
            attendees.add(getAttendeeForInsert(getResource(attendee.getEntity()), attendee));
        }
        /*
         * take over any external attendees
         */
        for (Attendee attendee : filter(newAttendees, Boolean.FALSE, null)) {
            if (contains(existingAttendees, attendee) || contains(attendees, attendee)) {
                LOG.debug("Skipping duplicate external attendee {}", attendee);
                continue;
            }
            //TODO checks? resolve to internal? generate synthetic id?
            attendees.add(attendee);
        }
        return attendees;
    }

    private Attendee getAttendeeForInsert(Group group, Attendee requestedAttendee) throws OXException {
        /*
         * prepare group attendee & apply default properties
         */
        Attendee groupAttendee = new Attendee();
        groupAttendee.setEntity(group.getIdentifier());
        groupAttendee.setCuType(CalendarUserType.GROUP);
        groupAttendee.setPartStat(ParticipationStatus.ACCEPTED);
        groupAttendee.setUri(getCalAddress(session.getContext().getContextId(), group));
        groupAttendee.setCn(group.getDisplayName());
        /*
         * take over additional properties as requested
         */
        if (null != requestedAttendee) {
            AttendeeMapper.getInstance().copy(requestedAttendee, groupAttendee, AttendeeField.ROLE);
        }
        return groupAttendee;
    }

    private Attendee getAttendeeForInsert(Resource resource, Attendee requestedAttendee) throws OXException {
        /*
         * prepare resource attendee & apply default properties
         */
        Attendee resourceAttendee = new Attendee();
        resourceAttendee.setEntity(resource.getIdentifier());
        resourceAttendee.setCuType(CalendarUserType.RESOURCE);
        resourceAttendee.setPartStat(ParticipationStatus.ACCEPTED);
        resourceAttendee.setUri(getCalAddress(session.getContext().getContextId(), resource));
        resourceAttendee.setCn(resource.getDisplayName());
        resourceAttendee.setComment(resource.getDescription());
        /*
         * take over additional properties as requested
         */
        if (null != requestedAttendee) {
            AttendeeMapper.getInstance().copy(requestedAttendee, resourceAttendee, AttendeeField.ROLE);
        }
        return resourceAttendee;
    }

    private Attendee getAttendeeForInsert(User user, Attendee requestedAttendee, Type folderType) throws OXException {
        /*
         * prepare user attendee & apply default properties
         */
        Attendee userAttendee = applyProperties(new Attendee(), user);
        userAttendee.setCuType(CalendarUserType.INDIVIDUAL);
        userAttendee.setFolderID(PublicType.getInstance().equals(folderType) ? ATTENDEE_PUBLIC_FOLDER_ID : getDefaultFolderID(session.getContext(), user));
        userAttendee.setPartStat(getInitialPartStat(session.getContext().getContextId(), user.getId(), folderType));
        /*
         * take over additional properties as requested
         */
        if (null != requestedAttendee) {
            AttendeeMapper.getInstance().copy(requestedAttendee, userAttendee, AttendeeField.RSVP, AttendeeField.ROLE);
        }
        return userAttendee;
    }

    private Attendee getDefaultAttendee(UserizedFolder folder, List<Attendee> requestedAttendees) throws OXException {
        /*
         * prepare attendee for default calendar user in folder
         */
        User calendarUser = getCalendarUser(folder);
        Attendee defaultAttendee = applyProperties(new Attendee(), calendarUser);
        defaultAttendee.setCuType(CalendarUserType.INDIVIDUAL);
        defaultAttendee.setPartStat(ParticipationStatus.ACCEPTED);
        if (session.getUser().getId() != defaultAttendee.getEntity()) {
            defaultAttendee.setSentBy(getCalAddress(calendarUser));
        }
        defaultAttendee.setFolderID(PublicType.getInstance().equals(folder.getType()) ? ATTENDEE_PUBLIC_FOLDER_ID : i(folder));
        /*
         * take over additional properties from corresponding requested attendee
         */
        Attendee requestedAttendee = find(requestedAttendees, defaultAttendee);
        if (null != requestedAttendee) {
            AttendeeMapper.getInstance().copy(requestedAttendee, defaultAttendee, AttendeeField.RSVP, AttendeeField.COMMENT, AttendeeField.PARTSTAT, AttendeeField.ROLE);
        }
        return defaultAttendee;
    }

    /**
     * Gets the initial participation status to use for new events in a specific folder.
     *
     * @param contextID The context identifier
     * @param userID The identifier of the user to get the participation status for
     * @param folderType The folder type where the new event is located in
     * @return The initial participation status, or {@link ParticipationStatus#NEEDS_ACTION} if not defined
     */
    private static ParticipationStatus getInitialPartStat(int contextID, int userID, Type folderType) throws OXException {
        Integer defaultStatus;
        if (PublicType.getInstance().equals(folderType)) {
            defaultStatus = ServerUserSetting.getInstance().getDefaultStatusPublic(contextID, userID);
        } else {
            defaultStatus = ServerUserSetting.getInstance().getDefaultStatusPrivate(contextID, userID);
        }
        return null != defaultStatus ? Appointment2Event.getParticipationStatus(defaultStatus.intValue()) : ParticipationStatus.NEEDS_ACTION;
    }

    protected User getUser(int userID) throws OXException {
        UserService userService = Services.getService(UserService.class);
        return userService.getUser(userID, session.getContext());
    }

    protected Group getGroup(int groupID) throws OXException {
        return Services.getService(GroupService.class).getGroup(session.getContext(), groupID);
    }

    protected Resource getResource(int resourceID) throws OXException {
        return Services.getService(ResourceService.class).getResource(resourceID, session.getContext());
    }

    static int getDefaultFolderID(Context context, User user) throws OXException {
        //TODO: via higher level service?
        return new OXFolderAccess(context).getDefaultFolderID(user.getId(), FolderObject.CALENDAR);
    }

}
