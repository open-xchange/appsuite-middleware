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

package com.openexchange.chronos.impl;

import static com.openexchange.chronos.common.CalendarUtils.contains;
import static com.openexchange.chronos.common.CalendarUtils.filter;
import static com.openexchange.chronos.common.CalendarUtils.filterByMembership;
import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.getAttendeeUpdates;
import static com.openexchange.chronos.common.CalendarUtils.isGroupScheduled;
import static com.openexchange.chronos.common.CalendarUtils.isInternal;
import static com.openexchange.chronos.common.CalendarUtils.isLastUserAttendee;
import static com.openexchange.chronos.common.CalendarUtils.matches;
import static com.openexchange.chronos.impl.Utils.getCalendarUserId;
import static com.openexchange.chronos.impl.Utils.isEnforceDefaultAttendee;
import static com.openexchange.chronos.impl.Utils.isSkipExternalAttendeeURIChecks;
import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.mapping.AbstractCollectionUpdate;
import com.openexchange.chronos.common.mapping.AttendeeMapper;
import com.openexchange.chronos.common.mapping.DefaultItemUpdate;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.service.ItemUpdate;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.type.PublicType;

/**
 * {@link AttendeeHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class AttendeeHelper implements CollectionUpdate<Attendee, AttendeeField> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AttendeeHelper.class);

    private final CalendarSession session;
    private final CalendarFolder folder;
    private final List<Attendee> originalAttendees;
    private final List<Attendee> attendeesToInsert;
    private final List<Attendee> attendeesToDelete;
    private final List<ItemUpdate<Attendee, AttendeeField>> attendeesToUpdate;

    /**
     * Initializes a new {@link AttendeeHelper} for a new event.
     *
     * @param session The calendar session
     * @param folder The parent folder of the event being processed
     * @param event The event data holding the list of attendees, as supplied by the client
     */
    public static AttendeeHelper onNewEvent(CalendarSession session, CalendarFolder folder, Event event) throws OXException {
        AttendeeHelper attendeeHelper = new AttendeeHelper(session, folder, null);
        boolean resolveResourceIds = false == isGroupScheduled(event) || hasInternalOrganizer(session.getEntityResolver(), event);
        attendeeHelper.processNewEvent(emptyForNull(event.getAttendees()), resolveResourceIds);
        return attendeeHelper;
    }

    /**
     * Initializes a new {@link AttendeeHelper} for an updated event.
     *
     * @param session The calendar session
     * @param folder The parent folder of the event being processed
     * @param originalAttendees The original event holding the original attendees
     * @param updatedAttendees The updated event holding the new/updated list of attendees, as supplied by the client
     */
    public static AttendeeHelper onUpdatedEvent(CalendarSession session, CalendarFolder folder, Event originalEvent, Event updatedEvent) throws OXException {
        AttendeeHelper attendeeHelper = new AttendeeHelper(session, folder, originalEvent.getAttendees());
        boolean resolveResourceIds = false == isGroupScheduled(originalEvent) || hasInternalOrganizer(session.getEntityResolver(), originalEvent);
        attendeeHelper.processUpdatedEvent(emptyForNull(updatedEvent.getAttendees()), resolveResourceIds);
        return attendeeHelper;
    }

    /**
     * Initializes a new {@link AttendeeHelper} for a deleted event.
     *
     * @param session The calendar session
     * @param folder The parent folder of the event being processed
     * @param originalAttendees The original list of attendees
     */
    public static AttendeeHelper onDeletedEvent(CalendarSession session, CalendarFolder folder, List<Attendee> originalAttendees) {
        AttendeeHelper attendeeHelper = new AttendeeHelper(session, folder, originalAttendees);
        attendeeHelper.processDeletedEvent();
        return attendeeHelper;
    }

    /**
     * Initializes a new {@link AttendeeHelper}.
     *
     * @param session The calendar session
     * @param folder The parent folder of the event being processed
     * @param originalAttendees The original attendees of the event, or <code>null</code> for new event creations
     */
    private AttendeeHelper(CalendarSession session, CalendarFolder folder, List<Attendee> originalAttendees) {
        super();
        this.session = session;
        this.folder = folder;
        this.originalAttendees = emptyForNull(originalAttendees);
        this.attendeesToInsert = new ArrayList<Attendee>();
        this.attendeesToDelete = new ArrayList<Attendee>();
        this.attendeesToUpdate = new ArrayList<ItemUpdate<Attendee, AttendeeField>>();
    }

    @Override
    public List<Attendee> getAddedItems() {
        return attendeesToInsert;
    }

    @Override
    public List<Attendee> getRemovedItems() {
        return attendeesToDelete;
    }

    @Override
    public boolean isEmpty() {
        return attendeesToInsert.isEmpty() && attendeesToDelete.isEmpty() && attendeesToUpdate.isEmpty();
    }

    @Override
    public List<? extends ItemUpdate<Attendee, AttendeeField>> getUpdatedItems() {
        return attendeesToUpdate;
    }

    /**
     * Gets a "forecast" of the resulting attendee list after all changes are applied to the original list of attendees. No data is
     * actually changed, i.e. the internal list of attendees to insert, update and delete are still intact.
     *
     * @return The changed list of attendees
     */
    public List<Attendee> previewChanges() throws OXException {
        List<Attendee> newAttendees = new ArrayList<Attendee>(originalAttendees);
        newAttendees.removeAll(attendeesToDelete);
        for (ItemUpdate<Attendee, AttendeeField> attendeeToUpdate : attendeesToUpdate) {
            Attendee originalAttendee = attendeeToUpdate.getOriginal();
            newAttendees.remove(originalAttendee);
            Attendee newAttendee = AttendeeMapper.getInstance().copy(originalAttendee, null, (AttendeeField[]) null);
            AttendeeMapper.getInstance().copy(attendeeToUpdate.getUpdate(), newAttendee, AttendeeField.RSVP, AttendeeField.COMMENT, AttendeeField.PARTSTAT, AttendeeField.ROLE, AttendeeField.EXTENDED_PARAMETERS);
            newAttendees.add(newAttendee);
        }
        newAttendees.addAll(attendeesToInsert);
        return newAttendees;
    }

    private void processNewEvent(List<Attendee> requestedAttendees, boolean resolveResourceIds) throws OXException {
        session.getEntityResolver().prefetch(requestedAttendees);
        requestedAttendees = session.getEntityResolver().prepare(requestedAttendees, resolveResourceIds);
        /*
         * always start with attendee for default calendar user in folder
         */
        Attendee defaultAttendee = getDefaultAttendee(session, folder, requestedAttendees);
        attendeesToInsert.add(defaultAttendee);
        if (null != requestedAttendees && 0 < requestedAttendees.size()) {
            /*
             * prepare & add all further attendees
             */
            List<Attendee> attendeeList = new ArrayList<Attendee>();
            attendeeList.add(defaultAttendee);
            attendeesToInsert.addAll(prepareNewAttendees(attendeeList, requestedAttendees));
        }
        /*
         * apply proper default attendee handling afterwards
         */
        handleDefaultAttendee(isEnforceDefaultAttendee(session));
    }

    private void processUpdatedEvent(List<Attendee> updatedAttendees, boolean resolveResourceIds) throws OXException {
        session.getEntityResolver().prefetch(updatedAttendees);
        updatedAttendees = session.getEntityResolver().prepare(updatedAttendees, resolveResourceIds);
        AbstractCollectionUpdate<Attendee, AttendeeField> attendeeDiff = getAttendeeUpdates(originalAttendees, updatedAttendees);
        List<Attendee> attendeeList = new ArrayList<Attendee>(originalAttendees);
        /*
         * delete removed attendees
         */
        List<Attendee> removedAttendees = attendeeDiff.getRemovedItems();
        for (Attendee removedAttendee : removedAttendees) {
            if (isEnforceDefaultAttendee(session) && false == PublicType.getInstance().equals(folder.getType()) && removedAttendee.getEntity() == folder.getCreatedBy()) {
                /*
                 * preserve default calendar user in personal folders
                 */
                LOG.info("Implicitly preserving default calendar user {} in personal folder {}.", I(removedAttendee.getEntity()), folder);
                continue;
            }
            if (CalendarUserType.GROUP.equals(removedAttendee.getCuType())) {
                /*
                 * only remove group attendee in case all originally participating members are also removed
                 */
                if (hasAttendingGroupMembers(removedAttendee.getUri(), originalAttendees, removedAttendees)) {
                    // preserve group attendee
                    LOG.debug("Ignoring removal of group {} as long as there are attending members.", I(removedAttendee.getEntity()));
                    continue;
                }
            }
            if (null != removedAttendee.getMember() && 0 < removedAttendee.getMember().size()) {
                /*
                 * only remove group member attendee in case either the corresponding group attendee itself, or *not all* originally participating members are also removed
                 */
                if (false == containsAllUris(removedAttendees, removedAttendee.getMember())) {
                    boolean attendingOtherMembers = false;
                    for (String groupUri : removedAttendee.getMember()) {
                        if (hasAttendingGroupMembers(groupUri, originalAttendees, removedAttendees)) {
                            attendingOtherMembers = true;
                            break;
                        }
                    }
                    if (false == attendingOtherMembers) {
                        // preserve group member attendee
                        LOG.debug("Ignoring removal of group member attendee {} as long as group unchanged.", I(removedAttendee.getEntity()));
                        continue;
                    }
                }
            }
            attendeeList.remove(removedAttendee);
            attendeesToDelete.add(removedAttendee);
        }
        /*
         * apply updated attendee data
         */
        for (ItemUpdate<Attendee, AttendeeField> attendeeUpdate : attendeeDiff.getUpdatedItems()) {
            Attendee attendee = AttendeeMapper.getInstance().copy(attendeeUpdate.getUpdate(), null, (AttendeeField[]) null);
            attendee = AttendeeMapper.getInstance().copy(attendeeUpdate.getOriginal(), attendee, AttendeeField.ENTITY, AttendeeField.MEMBER, AttendeeField.CU_TYPE, AttendeeField.URI);
            if (attendeeUpdate.getUpdatedFields().contains(AttendeeField.URI)) {
                if (false == isInternal(attendee) && false == isSkipExternalAttendeeURIChecks(session)) {
                    attendee = Check.requireValidEMail(attendee);
                }
            }
            if (attendeeUpdate.getUpdatedFields().contains(AttendeeField.PARTSTAT)) {
                /*
                 * ensure to reset RSVP expectation along with change of participation status
                 */
                attendee.setRsvp(null);
            }
            attendeesToUpdate.add(new DefaultItemUpdate<Attendee, AttendeeField>(AttendeeMapper.getInstance(), attendeeUpdate.getOriginal(), attendee));
        }
        /*
         * prepare & add all new attendees
         */
        attendeesToInsert.addAll(prepareNewAttendees(attendeeList, attendeeDiff.getAddedItems()));
        /*
         * apply proper default attendee handling afterwards
         */
        handleDefaultAttendee(isEnforceDefaultAttendee(session));
    }

    private void processDeletedEvent() {
        attendeesToDelete.addAll(originalAttendees);
    }

    private List<Attendee> prepareNewAttendees(List<Attendee> existingAttendees, List<Attendee> newAttendees) throws OXException {
        List<Attendee> attendees = new ArrayList<Attendee>(newAttendees.size());
        /*
         * add internal user attendees
         */
        boolean inPublicFolder = PublicType.getInstance().equals(folder.getType());
        for (Attendee userAttendee : filter(newAttendees, Boolean.TRUE, CalendarUserType.INDIVIDUAL)) {
            if (contains(existingAttendees, userAttendee) || contains(attendees, userAttendee)) {
                LOG.debug("Skipping duplicate user attendee {}", userAttendee);
                continue;
            }
            userAttendee = session.getEntityResolver().applyEntityData(userAttendee);
            userAttendee.setFolderId(inPublicFolder ? null : session.getConfig().getDefaultFolderId(userAttendee.getEntity()));
            if (false == userAttendee.containsPartStat() || null == userAttendee.getPartStat()) {
                userAttendee.setPartStat(session.getConfig().getInitialPartStat(userAttendee.getEntity(), inPublicFolder));
            }
            attendees.add(userAttendee);
        }
        /*
         * resolve & add any internal group attendees
         */
        boolean resolveGroupAttendees = session.getConfig().isResolveGroupAttendees();
        for (Attendee groupAttendee : filter(newAttendees, Boolean.TRUE, CalendarUserType.GROUP)) {
            if (contains(existingAttendees, groupAttendee) || contains(attendees, groupAttendee)) {
                LOG.debug("Skipping duplicate group attendee {}", groupAttendee);
                continue;
            }
            groupAttendee = session.getEntityResolver().applyEntityData(groupAttendee);
            if (false == resolveGroupAttendees) {
                attendees.add(groupAttendee);
            } else {
                LOG.debug("Skipping group attendee {}; only resolving group members.", groupAttendee);
            }
            for (int memberID : session.getEntityResolver().getGroupMembers(groupAttendee.getEntity())) {
                if (contains(existingAttendees, memberID) || contains(attendees, memberID)) {
                    LOG.debug("Skipping explicitly added group member {}", I(memberID));
                    continue;
                }
                Attendee memberAttendee = session.getEntityResolver().prepareUserAttendee(memberID);
                memberAttendee.setFolderId(PublicType.getInstance().equals(folder.getType()) ? null : session.getConfig().getDefaultFolderId(memberID));
                memberAttendee.setPartStat(session.getConfig().getInitialPartStat(memberID, inPublicFolder));
                if (false == resolveGroupAttendees) {
                    memberAttendee.setMember(Collections.singletonList(groupAttendee.getUri()));
                }
                attendees.add(memberAttendee);
            }
        }
        /*
         * resolve & add any internal resource attendees
         */
        for (Attendee resourceAttendee : filter(newAttendees, Boolean.TRUE, CalendarUserType.RESOURCE)) {
            if (contains(existingAttendees, resourceAttendee) || contains(attendees, resourceAttendee)) {
                LOG.debug("Skipping duplicate resource attendee {}", resourceAttendee);
                continue;
            }
            attendees.add(session.getEntityResolver().applyEntityData(resourceAttendee));
        }
        /*
         * take over any external attendees
         */
        for (Attendee attendee : filter(newAttendees, Boolean.FALSE, (CalendarUserType[]) null)) {
            attendee = session.getEntityResolver().applyEntityData(attendee);
            if (contains(existingAttendees, attendee) || contains(attendees, attendee)) {
                LOG.debug("Skipping duplicate external attendee {}", attendee);
                continue;
            }
            attendees.add(isSkipExternalAttendeeURIChecks(session) ? attendee : Check.requireValidEMail(attendee));
        }
        return attendees;
    }

    /**
     * Gets the <i>default</i> attendee that is always added to a newly inserted event, based on the target folder type.<p/>
     * For <i>public</i> folders, this is an attendee for the current session's user, otherwise (<i>private</i> or <i>shared</i>, an
     * attendee for the folder owner (i.e. the calendar user) is prepared.
     *
     * @param session The calendar session
     * @param folder The folder to get the default attendee for
     * @param requestedAttendees The attendees as supplied by the client, or <code>null</code> if not available
     * @return The default attendee
     */
    public static Attendee getDefaultAttendee(CalendarSession session, CalendarFolder folder, List<Attendee> requestedAttendees) throws OXException {
        /*
         * prepare attendee for default calendar user in folder
         */
        int calendarUserId = getCalendarUserId(folder);
        Attendee defaultAttendee = session.getEntityResolver().prepareUserAttendee(calendarUserId);
        defaultAttendee.setPartStat(ParticipationStatus.ACCEPTED);
        defaultAttendee.setFolderId(PublicType.getInstance().equals(folder.getType()) ? null : folder.getId());
        if (session.getUserId() != calendarUserId) {
            defaultAttendee.setSentBy(session.getEntityResolver().applyEntityData(new CalendarUser(), session.getUserId()));
        }
        /*
         * take over additional properties from corresponding requested attendee
         */
        Attendee requestedAttendee = find(requestedAttendees, defaultAttendee);
        if (null != requestedAttendee) {
            AttendeeMapper.getInstance().copy(requestedAttendee, defaultAttendee,
                AttendeeField.RSVP, AttendeeField.COMMENT, AttendeeField.PARTSTAT, AttendeeField.ROLE, AttendeeField.PARTSTAT, AttendeeField.CN, AttendeeField.URI);
        }
        return defaultAttendee;
    }

    private static List<Attendee> emptyForNull(List<Attendee> attendees) {
        return null == attendees ? Collections.<Attendee> emptyList() : attendees;
    }

    private static ItemUpdate<Attendee, AttendeeField> findUpdate(List<ItemUpdate<Attendee, AttendeeField>> attendeeUpdates, int entity) {
        if (null != attendeeUpdates) {
            for (ItemUpdate<Attendee, AttendeeField> attendeeUpdate : attendeeUpdates) {
                if (matches(attendeeUpdate.getOriginal(), entity)) {
                    return attendeeUpdate;
                }
            }
        }
        return null;
    }

    /**
     * Processes the lists of attendees to update/delete/insert in terms of the configured handling of the implicit attendee for the
     * actual calendar user.
     * <p/>
     * If the default attendee is enforced, this method ensures that the calendar user attendee is always present in
     * personal calendar folders, and there is at least one attendee present for events in public folders. Otherwise, if the actual
     * calendar user would be the last one in the resulting attendee list, this attendee is removed.
     *
     * @param enforceDefaultAttendee <code>true</code> the current calendar user should be added as default attendee to events implicitly,
     *            <code>false</code>, otherwise
     */
    private void handleDefaultAttendee(boolean enforceDefaultAttendee) throws OXException {
        int calendarUserId = getCalendarUserId(folder);
        List<Attendee> attendees = previewChanges();
        /*
         * check if resulting attendees would lead to a "group-scheduled" event or not
         */
        if (false == enforceDefaultAttendee && (attendees.isEmpty() || isLastUserAttendee(attendees, calendarUserId))) {
            /*
             * event is not (or no longer) a group-scheduled one, remove default attendee
             */
            Attendee defaultAttendee = find(attendeesToInsert, calendarUserId);
            if (null != defaultAttendee) {
                attendeesToInsert.remove(defaultAttendee);
            }
            ItemUpdate<Attendee, AttendeeField> defaultAttendeeUpdate = findUpdate(attendeesToUpdate, calendarUserId);
            if (null != defaultAttendeeUpdate) {
                attendeesToUpdate.remove(defaultAttendeeUpdate);
                attendeesToDelete.add(defaultAttendeeUpdate.getOriginal());
            } else {
                defaultAttendee = find(originalAttendees, calendarUserId);
                if (null != defaultAttendee) {
                    attendeesToDelete.add(defaultAttendee);
                }
            }
        } else {
            /*
             * enforce at least the calendar user to be present in public folders
             */
            if (PublicType.getInstance().equals(folder.getType())) {
                if (attendees.isEmpty()) {
                    Attendee defaultAttendee = find(attendeesToDelete, calendarUserId);
                    if (null != defaultAttendee) {
                        LOG.info("Implicitly preserving default calendar user {} in public folder {}.", I(calendarUserId), folder);
                        attendeesToDelete.remove(defaultAttendee);
                    } else {
                        LOG.info("Implicitly adding default calendar user {} in public folder {}.", I(calendarUserId), folder);
                        attendeesToInsert.add(getDefaultAttendee(session, folder, null));
                    }
                }
            } else if (false == contains(attendees, calendarUserId)) {
                /*
                 * ensure the calendar user is always present in personal calendar folders
                 */
                Attendee defaultAttendee = find(attendeesToDelete, calendarUserId);
                if (null != defaultAttendee) {
                    LOG.info("Implicitly preserving default calendar user {} in personal folder {}.", I(calendarUserId), folder);
                    attendeesToDelete.remove(defaultAttendee);
                } else {
                    LOG.info("Implicitly adding default calendar user {} in personal folder {}.", I(calendarUserId), folder);
                    attendeesToInsert.add(getDefaultAttendee(session, folder, null));
                }
            }
        }
    }

    /**
     * Gets a value indicating whether a resulting attendee list is going to contain at least one member of a specific group or not.
     *
     * @param groupUri The uri of the group to check
     * @param originalAttendees The list of original attendees
     * @param removedAttendees The list of removed attendees
     * @return <code>true</code> if there'll be at least one group member afterwards, <code>false</code>, otherwise
     */
    private static boolean hasAttendingGroupMembers(String groupUri, List<Attendee> originalAttendees, List<Attendee> removedAttendees) {
        for (Attendee originalMemberAttendee : filterByMembership(originalAttendees, groupUri)) {
            if (null == find(removedAttendees, originalMemberAttendee)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a value indicating whether the supplied event is organized by an <i>internal</i> entity or not.
     * 
     * @param entityResolver The entity resolver to use
     * @param event The event to check
     * @return <code>true</code> if the event has an <i>internal</i> organizer, false, otherwise
     */
    private static boolean hasInternalOrganizer(EntityResolver entityResolver, Event event) {
        if (null != event.getOrganizer()) {
            try {
                CalendarUser organizer = entityResolver.prepare(event.getOrganizer(), CalendarUserType.INDIVIDUAL);
                return CalendarUtils.isInternal(organizer, CalendarUserType.INDIVIDUAL);
            } catch (OXException e) {
                LOG.warn("Error checking if event has internal organizer", e);
            }
        }
        return false;
    }

    private static boolean containsAllUris(List<Attendee> attendees, List<String> uris) {
        if (null == uris || 0 == uris.size()) {
            return true;
        }
        for (String uri : uris) {
            if (false == containsUri(attendees, uri)) {
                return false;
            }
        }
        return true;
    }

    private static boolean containsUri(List<Attendee> attendees, String uri) {
        if (null == attendees || 0 == attendees.size()) {
            return false;
        }
        for (Attendee attendee : attendees) {
            if (uri.equalsIgnoreCase(attendee.getUri())) {
                return true;
            }
        }
        return false;
    }

}
