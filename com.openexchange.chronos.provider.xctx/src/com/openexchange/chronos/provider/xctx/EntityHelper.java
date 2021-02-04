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

package com.openexchange.chronos.provider.xctx;

import static com.openexchange.chronos.common.CalendarUtils.extractEMailAddress;
import static com.openexchange.chronos.common.CalendarUtils.getURI;
import static com.openexchange.chronos.common.CalendarUtils.isInternal;
import static com.openexchange.chronos.common.CalendarUtils.removeExtendedParameter;
import static com.openexchange.chronos.common.CalendarUtils.setExtendedParameter;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Conference;
import com.openexchange.chronos.ConferenceField;
import com.openexchange.chronos.DelegatingEvent;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.mapping.AttendeeMapper;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarPermission;
import com.openexchange.chronos.provider.DefaultCalendarPermission;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.DeleteResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.EventsResult;
import com.openexchange.chronos.service.ImportResult;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.chronos.service.UpdatesResult;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.groupware.tools.mappings.common.CollectionUpdate;
import com.openexchange.groupware.tools.mappings.common.ItemUpdate;
import com.openexchange.groupware.tools.mappings.common.SimpleCollectionUpdate;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Pair;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.ShareService;
import com.openexchange.share.core.subscription.XctxEntityHelper;
import com.openexchange.tools.id.IDMangler;
import com.openexchange.user.UserService;

/**
 * {@link EntityHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class EntityHelper extends XctxEntityHelper {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link EntityHelper}.
     * 
     * @param services A service lookup reference
     * @param account The calendar account
     */
    public EntityHelper(ServiceLookup services, CalendarAccount account) {
        super(account.getProviderId(), String.valueOf(account.getAccountId()), account.getUserConfiguration().optString("url", null));
        this.services = services;
    }

    @Override
    protected UserService getUserService() throws OXException {
        return services.getServiceSafe(UserService.class);
    }

    @Override
    protected GroupService getGroupService() throws OXException {
        return services.getServiceSafe(GroupService.class);
    }

    @Override
    protected ShareService getShareService() throws OXException {
        return services.getServiceSafe(ShareService.class);
    }

    @Override
    protected DispatcherPrefixService getDispatcherPrefixService() throws OXException {
        return services.getServiceSafe(DispatcherPrefixService.class);
    }

    /**
     * <i>Unmangles</i> the identifiers in any entities and calendar users found in the supplied event from the <i>local</i> context, so
     * that it can be used within the guest session of the foreign context.
     * <p/>
     * This includes restoring previously mangled entities for the remote context, as well turning <i>internal</i> calendar users (from
     * the <i>local</i> context into <i>external</i>s, so that they do not interfere there.
     * 
     * @param event The event to unmangle the entities and calendar users in
     * @return A new delegating event with unmangled entity identifiers to be used in the remote context
     */
    public Event unmangleLocalEvent(Event event) {
        return new DelegatingEvent(event) {

            @Override
            public CalendarUser getCreatedBy() {
                return unmangleLocalUser(super.getCreatedBy(), CalendarUserType.INDIVIDUAL);
            }

            @Override
            public CalendarUser getModifiedBy() {
                return unmangleLocalUser(super.getModifiedBy(), CalendarUserType.INDIVIDUAL);
            }

            @Override
            public CalendarUser getCalendarUser() {
                return unmangleLocalUser(super.getCalendarUser(), CalendarUserType.INDIVIDUAL);
            }

            @Override
            public Organizer getOrganizer() {
                return unmangleLocalOrganizer(super.getOrganizer());
            }

            @Override
            public List<Attendee> getAttendees() {
                return unmangleLocalAttendees(super.getAttendees());
            }
        };
    }

    /**
     * <i>Mangles</i> the identifiers found in the supplied folder permissions from the <i>remote</i> context, so that they can be used
     * within the local session of the calendar account's context.
     * <p/>
     * For each permission entry, the mangled permission's <code>identifier</code> will be constructed based on the remote entity id,
     * while the <code>entity</code> itself will no longer be set in the resulting permission. The same is done with a potentially set
     * {@link EntityInfo} in the supplied foreign permission.
     * 
     * @param permissions The permissions to mangle the identifiers in
     * @return A list with new permissions with qualified remote entity identifiers
     */
    public List<CalendarPermission> mangleRemoteCalendarPermissions(List<CalendarPermission> permissions) {
        if (null == permissions) {
            return null;
        }
        List<CalendarPermission> mangledPermissions = new ArrayList<CalendarPermission>(permissions.size());
        for (CalendarPermission permission : permissions) {
            mangledPermissions.add(mangleRemoteCalendarPermission(permission));
        }
        return mangledPermissions;
    }

    /**
     * <i>Mangles</i> the identifiers found in the supplied folder permission from the <i>remote</i> context, so that it can be used
     * within the local session of the calendar account's context.
     * <p/>
     * The mangled permission's <code>identifier</code> will be constructed based on the remote entity id, while the <code>entity</code>
     * itself will no longer be set in the resulting permission. The same is done with a potentially set {@link EntityInfo} in the
     * supplied foreign permission.
     * 
     * @param permission The permission to mangle the identifiers in
     * @return A new permission with qualified remote entity identifiers
     */
    public CalendarPermission mangleRemoteCalendarPermission(CalendarPermission permission) {
        if (null == permission) {
            return null;
        }
        DefaultCalendarPermission mangledPermission = new DefaultCalendarPermission(permission);
        mangledPermission.setIdentifier(mangleRemoteEntity(permission.getEntity()));
        mangledPermission.setEntity(NOT_SET);
        mangledPermission.setEntityInfo(mangleRemoteEntity(permission.getEntityInfo()));
        return mangledPermission;
    }

    /**
     * Resolves and builds additional entity info for the users and groups referenced by the supplied permissions under perspective of
     * the passed session's user, and returns a list of new permissions enriched by these entity info.
     * 
     * @param session The session to use to resolve the entities in
     * @param permissions The permissions to enhance
     * @return A list with new permissions, enhanced with additional details of the underlying entity
     */
    public List<CalendarPermission> addPermissionEntityInfos(CalendarSession session, List<CalendarPermission> permissions) {
        if (null == permissions) {
            return null;
        }
        List<CalendarPermission> enhancedPermissions = new ArrayList<CalendarPermission>(permissions.size());
        for (CalendarPermission permission : permissions) {
            enhancedPermissions.add(addPermissionEntityInfo(session, permission));
        }
        return enhancedPermissions;
    }

    /**
     * Resolves and builds additional entity info for a certain user or group under perspective of the passed session's user, and returns
     * a new permission enriched by these entity info.
     * 
     * @param session The session to use to resolve the entity in
     * @param permission The permission to enhance
     * @return A new permission, enhanced with additional details of the underlying entity, or the passed permission as is if no
     *         info could be resolved
     */
    public CalendarPermission addPermissionEntityInfo(CalendarSession session, CalendarPermission permission) {
        if (null == permission) {
            return null;
        }
        EntityInfo entityInfo = lookupEntity(session.getSession(), permission.getEntity(), permission.isGroup());
        if (null == entityInfo) {
            return permission;
        }
        CalendarPermission enhancedPermission = new DefaultCalendarPermission(permission);
        enhancedPermission.setEntityInfo(entityInfo);
        return enhancedPermission;
    }

    /**
     * Looks up and returns a new {@link EntityInfo} object in case the supplied one only carries an identifier.
     * 
     * @param session The session under which perspective the entity infos should be looked up
     * @param entityInfo The entity info to enrich as needed
     * @return The (possibly replaced) entity info, or the passed one if not needed
     */
    public EntityInfo addEntityInfo(Session session, EntityInfo entityInfo) {
        if (null != entityInfo && null == entityInfo.getDisplayName() && null == entityInfo.getEmail1() && NOT_SET != entityInfo.getEntity()) {
            EntityInfo lookedUpEntityInfo = lookupEntity(session, entityInfo.getEntity(), EntityInfo.Type.GROUP.equals(entityInfo.getType()));
            if (null != lookedUpEntityInfo) {
                return lookedUpEntityInfo;
            }
        }
        return entityInfo;
    }

    public List<ImportResult> mangleRemoteImportResults(List<ImportResult> importResults) {
        if (null == importResults || importResults.isEmpty()) {
            return importResults;
        }
        List<ImportResult> mangledImportResults = new ArrayList<ImportResult>(importResults.size());
        for (ImportResult importResult : importResults) {
            mangledImportResults.add(mangleRemoteImportResult(importResult));
        }
        return mangledImportResults;
    }

    public ImportResult mangleRemoteImportResult(ImportResult importResult) {
        if (null == importResult) {
            return null;
        }
        return new ImportResult() {

            @Override
            public long getTimestamp() {
                return importResult.getTimestamp();
            }

            @Override
            public List<UpdateResult> getUpdates() {
                return mangleRemoteUpdateResults(importResult.getUpdates());
            }

            @Override
            public Session getSession() {
                return null; // TODO
            }

            @Override
            public String getFolderID() {
                return importResult.getFolderID();
            }

            @Override
            public List<DeleteResult> getDeletions() {
                return mangleRemoteDeleteResults(importResult.getDeletions());
            }

            @Override
            public List<CreateResult> getCreations() {
                return mangleRemoteCreateResults(importResult.getCreations());
            }

            @Override
            public int getCalendarUser() {
                return 0; // TODO
            }

            @Override
            public List<OXException> getWarnings() {
                return importResult.getWarnings();
            }

            @Override
            public OXException getError() {
                return importResult.getError();
            }

            @Override
            public int getIndex() {
                return importResult.getIndex();
            }

            @Override
            public EventID getId() {
                return importResult.getId();
            }
        };
    }

    public CalendarResult mangleRemoteCalendarResult(CalendarResult calendarResult) {
        if (null == calendarResult) {
            return null;
        }
        return new CalendarResult() {

            @Override
            public long getTimestamp() {
                return calendarResult.getTimestamp();
            }

            @Override
            public List<UpdateResult> getUpdates() {
                return mangleRemoteUpdateResults(calendarResult.getUpdates());
            }

            @Override
            public Session getSession() {
                return null; //TODO: which session?
            }

            @Override
            public String getFolderID() {
                return calendarResult.getFolderID();
            }

            @Override
            public List<DeleteResult> getDeletions() {
                return mangleRemoteDeleteResults(calendarResult.getDeletions());
            }

            @Override
            public List<CreateResult> getCreations() {
                return mangleRemoteCreateResults(calendarResult.getCreations());
            }

            @Override
            public int getCalendarUser() {
                return 0; //TODO: which user?
            }
        };

    }

    public UpdatesResult mangleRemoteUpdatesResult(UpdatesResult updatesResult) {
        if (null == updatesResult || updatesResult.isEmpty()) {
            return updatesResult;
        }
        return new UpdatesResult() {

            @Override
            public long getTimestamp() {
                return updatesResult.getTimestamp();
            }

            @Override
            public boolean isTruncated() {
                return updatesResult.isTruncated();
            }

            @Override
            public boolean isEmpty() {
                return updatesResult.isEmpty();
            }

            @Override
            public List<Event> getNewAndModifiedEvents() {
                return mangleRemoteEvents(updatesResult.getNewAndModifiedEvents());
            }

            @Override
            public List<Event> getDeletedEvents() {
                return mangleRemoteEvents(updatesResult.getDeletedEvents());
            }
        };
    }

    public <K> Map<K, EventsResult> mangleMappedEventsResults(Map<K, EventsResult> mappedEventsResults) {
        if (null == mappedEventsResults || mappedEventsResults.isEmpty()) {
            return mappedEventsResults;
        }
        Map<K, EventsResult> mangledMappedEventsResults = new HashMap<K, EventsResult>(mappedEventsResults.size());
        for (Entry<K, EventsResult> entry : mappedEventsResults.entrySet()) {
            mangledMappedEventsResults.put(entry.getKey(), mangleRemoteEventsResult(entry.getValue()));
        }
        return mangledMappedEventsResults;
    }

    public List<CreateResult> mangleRemoteCreateResults(List<CreateResult> createResults) {
        if (null == createResults || createResults.isEmpty()) {
            return createResults;
        }
        List<CreateResult> mangledCreateResults = new ArrayList<CreateResult>(createResults.size());
        for (CreateResult createResult : createResults) {
            mangledCreateResults.add(mangleRemoteCreateResult(createResult));
        }
        return mangledCreateResults;
    }

    public CreateResult mangleRemoteCreateResult(CreateResult createResult) {
        if (null == createResult) {
            return null;
        }
        return new CreateResult() {

            @Override
            public long getTimestamp() {
                return createResult.getTimestamp();
            }

            @Override
            public Event getCreatedEvent() {
                return mangleRemoteEvent(createResult.getCreatedEvent());
            }
        };
    }

    public List<UpdateResult> mangleRemoteUpdateResults(List<UpdateResult> updateResults) {
        if (null == updateResults || updateResults.isEmpty()) {
            return updateResults;
        }
        List<UpdateResult> mangledUpdateResults = new ArrayList<UpdateResult>(updateResults.size());
        for (UpdateResult updateResult : updateResults) {
            mangledUpdateResults.add(mangleRemoteUpdateResult(updateResult));
        }
        return mangledUpdateResults;
    }

    public UpdateResult mangleRemoteUpdateResult(UpdateResult updateResult) {
        if (null == updateResult) {
            return null;
        }
        return new UpdateResult() {

            @Override
            public long getTimestamp() {
                return updateResult.getTimestamp();
            }

            @Override
            public CollectionUpdate<Attendee, AttendeeField> getAttendeeUpdates() {
                return mangleRemoteAttendeeUpdates(updateResult.getAttendeeUpdates());
            }

            @Override
            public CollectionUpdate<Conference, ConferenceField> getConferenceUpdates() {
                return updateResult.getConferenceUpdates();
            }

            @Override
            public CollectionUpdate<Alarm, AlarmField> getAlarmUpdates() {
                return updateResult.getAlarmUpdates();
            }

            @Override
            public SimpleCollectionUpdate<Attachment> getAttachmentUpdates() {
                return updateResult.getAttachmentUpdates();
            }

            @Override
            public Event getOriginal() {
                return mangleRemoteEvent(updateResult.getOriginal());
            }

            @Override
            public Event getUpdate() {
                return mangleRemoteEvent(updateResult.getUpdate());
            }

            @Override
            public Set<EventField> getUpdatedFields() {
                return updateResult.getUpdatedFields();
            }

            @Override
            public boolean containsAnyChangeOf(EventField[] fields) {
                return updateResult.containsAnyChangeOf(fields);
            }
        };
    }

    public List<DeleteResult> mangleRemoteDeleteResults(List<DeleteResult> deleteResults) {
        if (null == deleteResults || deleteResults.isEmpty()) {
            return deleteResults;
        }
        List<DeleteResult> mangledDeleteResults = new ArrayList<DeleteResult>(deleteResults.size());
        for (DeleteResult deleteResult : deleteResults) {
            mangledDeleteResults.add(mangleRemoteDeleteResult(deleteResult));
        }
        return mangledDeleteResults;
    }

    public DeleteResult mangleRemoteDeleteResult(DeleteResult deleteResult) {
        if (null == deleteResult) {
            return null;
        }
        return new DeleteResult() {

            @Override
            public long getTimestamp() {
                return deleteResult.getTimestamp();
            }

            @Override
            public EventID getEventID() {
                return deleteResult.getEventID();
            }

            @Override
            public Event getOriginal() {
                return mangleRemoteEvent(deleteResult.getOriginal());
            }
        };
    }

    public CollectionUpdate<Attendee, AttendeeField> mangleRemoteAttendeeUpdates(CollectionUpdate<Attendee, AttendeeField> attendeeUpdates) {
        if (null == attendeeUpdates || attendeeUpdates.isEmpty()) {
            return attendeeUpdates;
        }
        return new CollectionUpdate<Attendee, AttendeeField>() {

            @Override
            public boolean isEmpty() {
                return attendeeUpdates.isEmpty();
            }

            @Override
            public List<Attendee> getRemovedItems() {
                return mangleRemoteAttendees(attendeeUpdates.getRemovedItems());
            }

            @Override
            public List<Attendee> getAddedItems() {
                return mangleRemoteAttendees(attendeeUpdates.getAddedItems());
            }

            @Override
            public List<? extends ItemUpdate<Attendee, AttendeeField>> getUpdatedItems() {
                return mangleRemoteAttendeeUpdates(attendeeUpdates.getUpdatedItems());
            }
        };
    }

    public List<? extends ItemUpdate<Attendee, AttendeeField>> mangleRemoteAttendeeUpdates(List<? extends ItemUpdate<Attendee, AttendeeField>> attendeeUpdates) {
        if (null == attendeeUpdates || attendeeUpdates.isEmpty()) {
            return attendeeUpdates;
        }
        List<ItemUpdate<Attendee, AttendeeField>> mangledAttendeeUpdates = new ArrayList<ItemUpdate<Attendee, AttendeeField>>(attendeeUpdates.size());
        for (ItemUpdate<Attendee, AttendeeField> attendeeUpdate : attendeeUpdates) {
            mangledAttendeeUpdates.add(mangleRemoteAttendeeUpdate(attendeeUpdate));
        }
        return attendeeUpdates;
    }

    public ItemUpdate<Attendee, AttendeeField> mangleRemoteAttendeeUpdate(ItemUpdate<Attendee, AttendeeField> attendeeUpdate) {
        if (null == attendeeUpdate) {
            return null;
        }
        return new ItemUpdate<Attendee, AttendeeField>() {

            @Override
            public Set<AttendeeField> getUpdatedFields() {
                return attendeeUpdate.getUpdatedFields();
            }

            @Override
            public Attendee getUpdate() {
                return mangleRemoteAttendee(attendeeUpdate.getUpdate());
            }

            @Override
            public Attendee getOriginal() {
                return mangleRemoteAttendee(attendeeUpdate.getOriginal());
            }

            @Override
            public boolean containsAnyChangeOf(AttendeeField[] fields) {
                return attendeeUpdate.containsAnyChangeOf(fields);
            }
        };
    }

    public EventsResult mangleRemoteEventsResult(EventsResult eventsResult) {
        if (null == eventsResult) {
            return null;
        }
        return new EventsResult() {

            @Override
            public long getTimestamp() {
                return eventsResult.getTimestamp();
            }

            @Override
            public List<Event> getEvents() {
                return mangleRemoteEvents(eventsResult.getEvents());
            }

            @Override
            public OXException getError() {
                return eventsResult.getError();
            }
        };
    }

    public List<Event> mangleRemoteEvents(List<Event> events) {
        if (null == events || events.isEmpty()) {
            return events;
        }
        List<Event> mangledEvents = new ArrayList<Event>(events.size());
        for (Event event : events) {
            mangledEvents.add(mangleRemoteEvent(event));
        }
        return events;
    }

    public Event mangleRemoteEvent(Event event) {
        return new DelegatingEvent(event) {

            @Override
            public CalendarUser getCreatedBy() {
                return mangleRemoteUser(super.getCreatedBy(), CalendarUserType.INDIVIDUAL);
            }

            @Override
            public CalendarUser getModifiedBy() {
                return mangleRemoteUser(super.getModifiedBy(), CalendarUserType.INDIVIDUAL);
            }

            @Override
            public CalendarUser getCalendarUser() {
                return mangleRemoteUser(super.getCalendarUser(), CalendarUserType.INDIVIDUAL);
            }

            @Override
            public Organizer getOrganizer() {
                return mangleRemoteOrganizer(super.getOrganizer());
            }

            @Override
            public List<Attendee> getAttendees() {
                return mangleRemoteAttendees(super.getAttendees());
            }
        };
    }

    List<Attendee> mangleRemoteAttendees(List<Attendee> attendees) {
        if (null == attendees || attendees.isEmpty()) {
            return attendees;
        }
        List<Attendee> mangledAttendees = new ArrayList<Attendee>(attendees.size());
        for (Attendee attendee : attendees) {
            mangledAttendees.add(mangleRemoteAttendee(attendee));
        }
        return mangledAttendees;
    }

    List<Attendee> unmangleLocalAttendees(List<Attendee> attendees) {
        if (null == attendees || attendees.isEmpty()) {
            return attendees;
        }
        List<Attendee> unmangledAttendees = new ArrayList<Attendee>(attendees.size());
        for (Attendee attendee : attendees) {
            unmangledAttendees.add(unmangleLocalAttendee(attendee));
        }
        return unmangledAttendees;
    }

    Attendee mangleRemoteAttendee(Attendee attendee) {
        if (null == attendee || false == isInternal(attendee)) {
            return attendee;
        }
        Attendee mangledAttendee;
        try {
            mangledAttendee = AttendeeMapper.getInstance().copy(attendee, null, (AttendeeField[]) null);
        } catch (OXException e) {
            getLogger(EntityHelper.class).warn("Unexpected error copying attendee {} for id mangling, keeping as-is.", attendee, e);
            return attendee;
        }
        mangleRemoteEntities(mangledAttendee, attendee.getCuType());
        String identifier = mangleRemoteEntity(attendee.getEntity());
        mangledAttendee.setExtendedParameters(setExtendedParameter(mangledAttendee.getExtendedParameters(), "X-OX-IDENTIFIER", identifier));
        return mangledAttendee;
    }

    Attendee unmangleLocalAttendee(Attendee attendee) {
        if (null == attendee) {
            return attendee;
        }
        Attendee unmangledAttendee;
        try {
            unmangledAttendee = AttendeeMapper.getInstance().copy(attendee, null, (AttendeeField[]) null);
        } catch (OXException e) {
            getLogger(EntityHelper.class).warn("Unexpected error copying attendee {} for id mangling, keeping as-is.", attendee, e);
            return attendee;
        }
        unmangleLocalEntities(unmangledAttendee, attendee.getCuType());
        unmangledAttendee.setExtendedParameters(removeExtendedParameter(unmangledAttendee.getExtendedParameters(), "X-OX-IDENTIFIER"));
        return unmangledAttendee;
    }

    Organizer mangleRemoteOrganizer(Organizer organizer) {
        if (null == organizer || false == isInternal(organizer, CalendarUserType.INDIVIDUAL)) {
            return organizer;
        }
        Organizer mangledOrganizer = new Organizer(organizer);
        mangleRemoteEntities(mangledOrganizer, CalendarUserType.INDIVIDUAL);
        return mangledOrganizer;
    }

    Organizer unmangleLocalOrganizer(Organizer organizer) {
        if (null == organizer) {
            return organizer;
        }
        Organizer unmangledOrganizer = new Organizer(organizer);
        unmangleLocalEntities(unmangledOrganizer, CalendarUserType.INDIVIDUAL);
        return unmangledOrganizer;
    }

    CalendarUser mangleRemoteUser(CalendarUser calendarUser, CalendarUserType cuType) {
        if (null == calendarUser || false == CalendarUtils.isInternal(calendarUser, cuType)) {
            return calendarUser;
        }
        CalendarUser mangledCalendarUser = new CalendarUser(calendarUser);
        mangleRemoteEntities(mangledCalendarUser, cuType);
        return mangledCalendarUser;
    }

    CalendarUser unmangleLocalUser(CalendarUser calendarUser, CalendarUserType cuType) {
        if (null == calendarUser) {
            return calendarUser;
        }
        CalendarUser unmangledCalendarUser = new CalendarUser(calendarUser);
        unmangleLocalEntities(unmangledCalendarUser, cuType);
        return unmangledCalendarUser;
    }

    private <T extends CalendarUser> void mangleRemoteEntities(T calendarUser, CalendarUserType cuType) {
        if (null != calendarUser) {
            if (isInternal(calendarUser, cuType)) {
                calendarUser.setUri(mangleRemoteUri(calendarUser.getUri(), calendarUser.getEntity()));
                calendarUser.setEntity(NOT_SET);
            }
            if (null != calendarUser.getSentBy()) {
                mangleRemoteEntities(calendarUser.getSentBy(), CalendarUserType.INDIVIDUAL);
            }
        }
    }

    private <T extends CalendarUser> void unmangleLocalEntities(T calendarUser, CalendarUserType cuType) {
        if (null != calendarUser) {
            if (isInternal(calendarUser, cuType)) {
                /*
                 * internal entity of local context - turn into external calendar user from perspective of foreign context
                 */
                String email = extractEMailAddress(calendarUser.getUri());
                if (Strings.isEmpty(email)) {
                    email = calendarUser.getEMail();
                    if (Strings.isEmpty(email)) {
                        throw new IllegalArgumentException("Cannot use " + calendarUser + " in foreign account");
                    }
                }
                calendarUser.setUri(getURI(email));
                calendarUser.setEntity(NOT_SET);
            } else {
                /*
                 * previously mangled, or external entity - unmangle if possible, otherwise use as-is
                 */
                try {
                    Pair<String, Integer> unmangledUri = unmangleLocalUri(calendarUser.getUri());
                    calendarUser.setUri(unmangledUri.getFirst());
                    calendarUser.setEntity(i(unmangledUri.getSecond()));
                } catch (IllegalArgumentException e) {
                    getLogger(EntityHelper.class).debug("Can't unmangle {}, using as-is.", calendarUser.getUri(), e);
                }
            }
            if (null != calendarUser.getSentBy()) {
                unmangleLocalEntities(calendarUser.getSentBy(), CalendarUserType.INDIVIDUAL);
            }
        }
    }

    private String mangleRemoteUri(String uri, int entity) {
        return IDMangler.mangle(serviceId, accountId, String.valueOf(entity), null == uri ? "" : uri);
    }

    private Pair<String, Integer> unmangleLocalUri(String uri) {
        if (null != uri) {
            List<String> components = IDMangler.unmangle(uri);
            if (false == matchesAccount(components)) {
                throw new IllegalArgumentException("Cannot unmangle URI " + uri + " from foreign account");
            }
            return new Pair<String, Integer>(components.get(3), Integer.valueOf(components.get(2)));
        }
        return new Pair<String, Integer>(null, I(-1));
    }

}
