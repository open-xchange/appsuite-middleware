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
import static com.openexchange.chronos.impl.CalendarUtils.i;
import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarSession;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.compat.Appointment2Event;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tools.mappings.Mapping;
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

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AttendeeHelper.class);
    private static final int ATTENDEE_PUBLIC_FOLDER_ID = -2;

    private final CalendarSession session;
    private final UserizedFolder folder;

    private final List<Attendee> attendeesToInsert;
    private final List<Attendee> attendeesToDelete;
    private final List<Attendee> attendeesToUpdate;

    /**
     * Initializes a new {@link AttendeeHelper}.
     *
     * @param session The session
     */
    public AttendeeHelper(CalendarSession session, UserizedFolder folder, Event originalEvent, Event updatedEvent) throws OXException {
        super();
        this.session = session;
        this.folder = folder;
        this.attendeesToInsert = new ArrayList<Attendee>();
        this.attendeesToDelete = new ArrayList<Attendee>();
        this.attendeesToUpdate = new ArrayList<Attendee>();

        if (null == originalEvent) {
            if (null == updatedEvent) {
                // nothing to do
            } else {
                processNewEvent(updatedEvent.getAttendees());
            }
        } else if (null == updatedEvent) {
            processDeletedEvent(originalEvent.getAttendees());
        } else if (updatedEvent.containsAttendees()) {
            processUpdatedEvent(originalEvent.getAttendees(), updatedEvent.getAttendees());
        }
    }

    public List<Attendee> getAttendeesToInsert() throws OXException {
        return attendeesToInsert;
    }

    public List<Attendee> getAttendeesToDelete() {
        return attendeesToDelete;
    }

    public List<Attendee> getAttendeesToUpdate() {
        return attendeesToUpdate;
    }

    private void processUpdatedEvent(List<Attendee> originalAttendees, List<Attendee> updatedAttendees) throws OXException {
        AttendeeDiff attendeeDiff = new AttendeeDiff(originalAttendees, updatedAttendees);
        List<Attendee> attendeeList = new ArrayList<Attendee>(originalAttendees);

        /*
         * delete removed attendees
         */
        for (Attendee removedAttendee : attendeeDiff.getRemovedAttendees()) {
            attendeeList.remove(removedAttendee);
            attendeesToDelete.add(removedAttendee);
        }
        /*
         * apply updated attendee data
         */
        for (Attendee[] updatedAttendee : attendeeDiff.getUpdatedAttendees()) {
            //TODO better use clone / deep-copy of original?
            Attendee originalAttendee = updatedAttendee[0];
            Attendee requestedAttendee = updatedAttendee[1];
            copy(requestedAttendee, originalAttendee, AttendeeField.RSVP, AttendeeField.COMMENT, AttendeeField.PARTSTAT, AttendeeField.ROLE);
            attendeesToUpdate.add(originalAttendee);
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
            attendees.add(getUserAttendeeForInsert(folder, attendee));
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
            Attendee preparedAttendee = getGroupAttendeeForInsert(folder, group, attendee);
            attendees.add(preparedAttendee);
            for (int memberID : group.getMember()) {
                if (contains(existingAttendees, memberID) || contains(attendees, memberID)) {
                    LOG.debug("Skipping explicitly added group member {}", I(memberID));
                    continue;
                }
                Attendee memberAttendee = getAttendeeForInsert(folder, getUser(memberID));
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
            attendees.add(getResourceAttendeeForInsert(folder, getResource(attendee.getEntity()), attendee));
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

    //    /**
    //     * Gets the final list of prepared attendees to use when inserting a new event.
    //     *
    //     * @param folder The folder the event is created in
    //     * @param requestedAttendees The list of attendees as requested by the client
    //     * @return The prepared attendees for insert
    //     */
    //    private List<Attendee> getAttendeesForInsert(UserizedFolder folder, List<Attendee> requestedAttendees) throws OXException {
    //        /*
    //         * always add attendee for default calendar user in folder
    //         */
    //        Attendee defaultAttendee = getDefaultAttendee(folder, requestedAttendees);
    //        if (null == requestedAttendees || 0 == requestedAttendees.size() ||
    //            (1 == requestedAttendees.size() && requestedAttendees.get(0).getEntity() == defaultAttendee.getEntity())) {
    //            return Collections.singletonList(defaultAttendee); // no further attendees
    //        }
    //        List<Attendee> preparedAttendees = new ArrayList<Attendee>();
    //        preparedAttendees.add(defaultAttendee);
    //        /*
    //         * add further internal user attendees
    //         */
    //        for (Attendee userAttendee : filter(requestedAttendees, Boolean.TRUE, CalendarUserType.INDIVIDUAL)) {
    //            if (contains(preparedAttendees, userAttendee)) {
    //                LOG.debug("Skipping duplicate user attendee {}", userAttendee);
    //                continue;
    //            }
    //            preparedAttendees.add(getUserAttendeeForInsert(folder, userAttendee));
    //        }
    //        /*
    //         * resolve & add any internal group attendees
    //         */
    //        for (Attendee groupAttendee : filter(requestedAttendees, Boolean.TRUE, CalendarUserType.GROUP)) {
    //            if (contains(preparedAttendees, groupAttendee)) {
    //                LOG.debug("Skipping duplicate group attendee {}", groupAttendee);
    //                continue;
    //            }
    //            Group group = getGroup(groupAttendee.getEntity());
    //            Attendee preparedAttendee = getGroupAttendeeForInsert(folder, group, groupAttendee);
    //            preparedAttendees.add(preparedAttendee);
    //            for (int memberID : group.getMember()) {
    //                if (contains(preparedAttendees, memberID)) {
    //                    LOG.debug("Skipping explicitly added group member {}", Autoboxing.I(memberID));
    //                    continue;
    //                }
    //                Attendee memberAttendee = getAttendeeForInsert(folder, getUser(memberID));
    //                memberAttendee.setMember(CalendarUtils.getCalAddress(session.getContext().getContextId(), group));
    //                preparedAttendees.add(memberAttendee);
    //            }
    //        }
    //        /*
    //         * resolve & add any internal resource attendees
    //         */
    //        for (Attendee resourceAttendee : filter(requestedAttendees, Boolean.TRUE, CalendarUserType.RESOURCE)) {
    //            if (contains(preparedAttendees, resourceAttendee)) {
    //                LOG.debug("Skipping duplicate resource attendee {}", resourceAttendee);
    //                continue;
    //            }
    //            preparedAttendees.add(getResourceAttendeeForInsert(folder, getResource(resourceAttendee.getEntity()), resourceAttendee));
    //        }
    //        /*
    //         * take over any external attendees
    //         */
    //        for (Attendee externalAttendee : filter(requestedAttendees, Boolean.FALSE, null)) {
    //            if (contains(preparedAttendees, externalAttendee)) {
    //                LOG.debug("Skipping duplicate external attendee {}", externalAttendee);
    //                continue;
    //            }
    //            //TODO checks? resolve to internal? generate synthetic id?
    //            preparedAttendees.add(externalAttendee);
    //        }
    //        return preparedAttendees;
    //    }

    //    private List<Attendee> getAttendeesForUpdate(UserizedFolder folder, List<Attendee> originalAttendees, List<Attendee> updatedAttendees) throws OXException {
    //        AttendeeDiff attendeeDiff = new AttendeeDiff(originalAttendees, updatedAttendees);
    //        for (Attendee addedAttendee : attendeeDiff.getAddedAttendees()) {
    //
    //
    //        }
    //        for (Attendee removedAttendee : attendeeDiff.getRemovedAttendees()) {
    //
    //        }
    //
    //        return null;
    //    }

    private Attendee getGroupAttendeeForInsert(UserizedFolder folder, Group group, Attendee requestedAttendee) throws OXException {
        /*
         * prepare user attendee & apply default properties
         */
        Attendee groupAttendee = getAttendeeForInsert(folder, group);
        /*
         * take over additional properties from corresponding requested attendee
         */
        copy(requestedAttendee, groupAttendee, AttendeeField.ROLE);
        return groupAttendee;
    }

    private Attendee getResourceAttendeeForInsert(UserizedFolder folder, Resource resource, Attendee requestedAttendee) throws OXException {
        /*
         * prepare resource attendee & apply default properties
         */
        Attendee resourceAttendee = getAttendeeForInsert(folder, resource);
        /*
         * take over additional properties from corresponding requested attendee
         */
        copy(requestedAttendee, resourceAttendee, AttendeeField.ROLE);
        return resourceAttendee;
    }

    private Attendee getUserAttendeeForInsert(UserizedFolder folder, Attendee requestedAttendee) throws OXException {
        /*
         * prepare user attendee & apply default properties
         */
        User user = getUser(requestedAttendee.getEntity());
        Attendee userAttendee = getAttendeeForInsert(folder, user);
        /*
         * take over additional properties from corresponding requested attendee
         */
        copy(requestedAttendee, userAttendee, AttendeeField.RSVP, AttendeeField.ROLE);
        return userAttendee;
    }

    private Attendee getAttendeeForInsert(UserizedFolder folder, User user) throws OXException {
        /*
         * prepare user attendee & apply default properties
         */
        Attendee userAttendee = applyProperties(new Attendee(), user);
        userAttendee.setCuType(CalendarUserType.INDIVIDUAL);
        userAttendee.setFolderID(PublicType.getInstance().equals(folder.getType()) ? ATTENDEE_PUBLIC_FOLDER_ID : getDefaultFolderID(user));
        userAttendee.setPartStat(getInitialPartStat(folder, user));
        return userAttendee;
    }

    private Attendee getAttendeeForInsert(UserizedFolder folder, Resource resource) throws OXException {
        Attendee resourceAttendee = new Attendee();
        resourceAttendee.setEntity(resource.getIdentifier());
        resourceAttendee.setCuType(CalendarUserType.RESOURCE);
        resourceAttendee.setPartStat(ParticipationStatus.ACCEPTED);
        resourceAttendee.setUri(getCalAddress(session.getContext().getContextId(), resource));
        resourceAttendee.setCn(resource.getDisplayName());
        resourceAttendee.setComment(resource.getDescription());
        return resourceAttendee;
    }

    private Attendee getAttendeeForInsert(UserizedFolder folder, Group group) throws OXException {
        /*
         * prepare group attendee & apply default properties
         */
        Attendee groupAttendee = new Attendee();
        groupAttendee.setEntity(group.getIdentifier());
        groupAttendee.setCuType(CalendarUserType.GROUP);
        groupAttendee.setPartStat(ParticipationStatus.ACCEPTED);
        groupAttendee.setUri(getCalAddress(session.getContext().getContextId(), group));
        groupAttendee.setCn(group.getDisplayName());
        return groupAttendee;
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
            copy(requestedAttendee, defaultAttendee, AttendeeField.RSVP, AttendeeField.COMMENT, AttendeeField.PARTSTAT, AttendeeField.ROLE);
        }
        return defaultAttendee;
    }

    private ParticipationStatus getInitialPartStat(UserizedFolder folder, User user) throws OXException {
        //TODO hide behind some config facade
        Integer defaultStatus;
        if (PublicType.getInstance().equals(folder.getType())) {
            defaultStatus = ServerUserSetting.getInstance().getDefaultStatusPublic(session.getContext().getContextId(), user.getId());
        } else {
            defaultStatus = ServerUserSetting.getInstance().getDefaultStatusPrivate(session.getContext().getContextId(), user.getId());
        }
        if (null != defaultStatus) {
            return Appointment2Event.getParticipationStatus(defaultStatus.intValue());
        }
        return ParticipationStatus.NEEDS_ACTION;
    }

    /**
     * Gets the actual target calendar user for a specific folder. This is either the current session's user for "private" or "public"
     * folders, or the folder owner for "shared" calendar folders.
     *
     * @param folder The folder to get the calendar user for
     * @return The calendar user
     */
    protected User getCalendarUser(UserizedFolder folder) throws OXException {
        return SharedType.getInstance().equals(folder.getType()) ? getUser(folder.getCreatedBy()) : session.getUser();
    }

    /**
     * Gets the "acting" calendar user for a specific folder, i.e. the proxy user who is acting on behalf of the calendar owner, which is
     * the current session's user in case the folder is a "shared" calendar, otherwise <code>null</code> for "private" or "public" folders.
     *
     * @param folder The folder to determine the proxy user for
     * @return The proxy calendar user, or <code>null</code> if the current session's user is acting on behalf of it's own
     */
    protected User getProxyUser(UserizedFolder folder) throws OXException {
        return SharedType.getInstance().equals(folder.getType()) ? session.getUser() : null;
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

    protected int getDefaultFolderID(User user) throws OXException {
        //TODO: via higher level service?
        return new OXFolderAccess(session.getContext()).getDefaultFolderID(user.getId(), FolderObject.CALENDAR);
    }

    private static void copy(Attendee from, Attendee to, AttendeeField... fields) throws OXException {
        for (AttendeeField field : fields) {
            Mapping<? extends Object, Attendee> mapping = AttendeeMapper.getInstance().get(field);
            if (mapping.isSet(from)) {
                mapping.copy(from, to);
            }
        }
    }

}
