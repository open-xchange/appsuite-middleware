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

package com.openexchange.chronos.impl.session;

import static com.openexchange.chronos.common.CalendarUtils.isInternal;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import static com.openexchange.java.Autoboxing.i2I;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.ResourceId;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.arrays.Arrays;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

/**
 * {@link DefaultEntityResolver}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DefaultEntityResolver implements EntityResolver {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultEntityResolver.class);

    private final ServiceLookup services;
    private final Context context;
    private final Map<Integer, Group> knownGroups;
    private final Map<Integer, User> knownUsers;
    private final Map<Integer, Resource> knownResources;

    /**
     * Initializes a new {@link DefaultEntityResolver}.
     *
     * @param session The underlying session
     * @param services A service lookup reference
     */
    public DefaultEntityResolver(ServerSession session, ServiceLookup services) throws OXException {
        super();
        this.services = services;
        this.context = session.getContext();
        knownUsers = new HashMap<Integer, User>();
        knownUsers.put(I(session.getUserId()), session.getUser());
        knownGroups = new HashMap<Integer, Group>();
        knownResources = new HashMap<Integer, Resource>();
    }

    @Override
    public int[] getGroupMembers(int groupID) throws OXException {
        return getGroup(groupID).getMember();
    }

    @Override
    public TimeZone getTimeZone(int userID) throws OXException {
        return TimeZone.getTimeZone(getUser(userID).getTimeZone());
    }

    @Override
    public int getDefaultCalendarID(int userID) throws OXException {
        //TODO: via higher level service?
        return new OXFolderAccess(context).getDefaultFolderID(userID, FolderObject.CALENDAR);
    }

    @Override
    public Attendee prepareUserAttendee(int userID) throws OXException {
        return applyEntityData(new Attendee(), getUser(userID), (AttendeeField[]) null);
    }

    @Override
    public Attendee prepareGroupAttendee(int groupID) throws OXException {
        return applyEntityData(new Attendee(), getGroup(groupID), (AttendeeField[]) null);
    }

    @Override
    public Attendee prepareResourceAttendee(int resourceID) throws OXException {
        return applyEntityData(new Attendee(), getResource(resourceID), (AttendeeField[]) null);
    }

    @Override
    public Attendee applyEntityData(Attendee attendee) throws OXException {
        return applyEntityData(attendee, (AttendeeField[]) null);
    }

    @Override
    public Attendee applyEntityData(Attendee attendee, AttendeeField... fields) throws OXException {
        if (null == attendee || 0 >= attendee.getEntity()) {
            LOG.warn("Ignoring attempt to apply internal entity data for non-internal attendee {}", attendee);
            return attendee;
        }
        switch (attendee.getCuType()) {
            case GROUP:
                return applyEntityData(attendee, getGroup(attendee.getEntity()), fields);
            case RESOURCE:
            case ROOM:
                return applyEntityData(attendee, getResource(attendee.getEntity()), fields);
            default:
                return applyEntityData(attendee, getUser(attendee.getEntity()), fields);
        }
    }

    @Override
    public void prefetch(List<Attendee> attendees) throws OXException {
        Set<Integer> usersToLoad = new HashSet<Integer>();
        Set<Integer> groupsToLoad = new HashSet<Integer>();
        Set<Integer> resourcesToLoad = new HashSet<Integer>();
        for (Attendee attendee : attendees) {
            if (isInternal(attendee)) {
                Integer id = I(attendee.getEntity());
                switch (attendee.getCuType()) {
                    case GROUP:
                        if (false == knownGroups.containsKey(id)) {
                            groupsToLoad.add(id);
                        }
                        break;
                    case RESOURCE:
                    case ROOM:
                        if (false == knownResources.containsKey(id)) {
                            resourcesToLoad.add(id);
                        }
                        break;
                    default:
                        if (false == knownUsers.containsKey(id)) {
                            usersToLoad.add(id);
                        }
                        break;
                }
            }
        }
        if (0 < resourcesToLoad.size()) {
            ResourceService resourceService = services.getService(ResourceService.class);
            for (Integer resourceID : resourcesToLoad) {
                knownResources.put(resourceID, resourceService.getResource(resourceID.intValue(), context));
            }
        }
        if (0 < groupsToLoad.size()) {
            GroupService groupService = services.getService(GroupService.class);
            for (Integer groupID : groupsToLoad) {
                Group group = groupService.getGroup(context, groupID.intValue());
                knownGroups.put(groupID, group);
                usersToLoad.addAll(java.util.Arrays.asList(i2I(group.getMember())));
            }
        }
        if (0 < usersToLoad.size()) {
            UserService userService = services.getService(UserService.class);
            User[] users = userService.getUser(context, I2i(usersToLoad));
            for (User user : users) {
                knownUsers.put(I(user.getId()), user);
            }
        }
    }

    @Override
    public int getContextID() {
        return context.getContextId();
    }

    @Override
    public void invalidate() {
        knownGroups.clear();
        knownResources.clear();
        knownUsers.clear();
    }

    private Group getGroup(int entity) throws OXException {
        int id = I(entity);
        Group group = knownGroups.get(id);
        if (null == group) {
            group = services.getService(GroupService.class).getGroup(context, entity);
            knownGroups.put(id, group);
        }
        return group;
    }

    private User getUser(int entity) throws OXException {
        int id = I(entity);
        User user = knownUsers.get(id);
        if (null == user) {
            user = services.getService(UserService.class).getUser(entity, context);
            knownUsers.put(id, user);
        }
        return user;
    }

    private Resource getResource(int entity) throws OXException {
        int id = I(entity);
        Resource resource = knownResources.get(id);
        if (null == resource) {
            resource = services.getService(ResourceService.class).getResource(entity, context);
            knownResources.put(id, resource);
        }
        return resource;
    }

    private Attendee applyEntityData(Attendee attendee, User user, AttendeeField... fields) {
        if (null == fields || Arrays.contains(fields, AttendeeField.ENTITY)) {
            attendee.setEntity(user.getId());
        }
        if (null == fields || Arrays.contains(fields, AttendeeField.CU_TYPE)) {
            attendee.setCuType(CalendarUserType.INDIVIDUAL);
        }
        if (null == fields || Arrays.contains(fields, AttendeeField.CN)) {
            attendee.setCn(user.getDisplayName());
        }
        if (null == fields || Arrays.contains(fields, AttendeeField.URI)) {
            attendee.setUri(ResourceId.forUser(context.getContextId(), user.getId()));
            // attendee.setUri(Appointment2Event.getURI(user.getMail()));
        }
        return attendee;
    }

    private Attendee applyEntityData(Attendee attendee, Group group, AttendeeField... fields) {
        if (null == fields || Arrays.contains(fields, AttendeeField.ENTITY)) {
            attendee.setEntity(group.getIdentifier());
        }
        if (null == fields || Arrays.contains(fields, AttendeeField.CU_TYPE)) {
            attendee.setCuType(CalendarUserType.GROUP);
        }
        if (null == fields || Arrays.contains(fields, AttendeeField.CN)) {
            attendee.setCn(group.getDisplayName());
        }
        if (null == fields || Arrays.contains(fields, AttendeeField.PARTSTAT)) {
            attendee.setPartStat(ParticipationStatus.ACCEPTED);
        }
        if (null == fields || Arrays.contains(fields, AttendeeField.URI)) {
            attendee.setUri(ResourceId.forGroup(context.getContextId(), group.getIdentifier()));
        }
        return attendee;
    }

    private Attendee applyEntityData(Attendee attendee, Resource resource, AttendeeField... fields) {
        if (null == fields || Arrays.contains(fields, AttendeeField.ENTITY)) {
            attendee.setEntity(resource.getIdentifier());
        }
        if (null == fields || Arrays.contains(fields, AttendeeField.CU_TYPE)) {
            attendee.setCuType(CalendarUserType.RESOURCE);
        }
        if (null == fields || Arrays.contains(fields, AttendeeField.CN)) {
            attendee.setCn(resource.getDisplayName());
        }
        if (null == fields || Arrays.contains(fields, AttendeeField.COMMENT)) {
            attendee.setComment(resource.getDescription());
        }
        if (null == fields || Arrays.contains(fields, AttendeeField.PARTSTAT)) {
            attendee.setPartStat(ParticipationStatus.ACCEPTED);
        }
        if (null == fields || Arrays.contains(fields, AttendeeField.URI)) {
            attendee.setUri(ResourceId.forResource(context.getContextId(), resource.getIdentifier()));
        }
        return attendee;
    }

}
