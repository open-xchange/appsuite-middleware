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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.ResourceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
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
    public Attendee prepare(Attendee attendee) throws OXException {
        return prepare(attendee, attendee.getCuType());
    }

    @Override
    public List<Attendee> prepare(List<Attendee> attendees) throws OXException {
        if (null != attendees) {
            for (Attendee attendee : attendees) {
                prepare(attendee);
            }
        }
        return attendees;
    }

    @Override
    public <T extends CalendarUser> T prepare(T calendarUser, CalendarUserType cuType) throws OXException {
        if (null == calendarUser) {
            return null;
        }
        calendarUser = resolveExternals(calendarUser, cuType);
        if (0 < calendarUser.getEntity() || 0 == calendarUser.getEntity() && CalendarUserType.GROUP.equals(cuType)) {
            /*
             * internal entity, ensure it exists & enhance with static properties
             */
            checkExistence(calendarUser.getEntity(), cuType);
            applyEntityData(calendarUser, cuType);
            return calendarUser;
        }
        /*
         * external entity otherwise, take over as-is
         */
        return calendarUser;
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
    public <T extends CalendarUser> T applyEntityData(T calendarUser, int userID) throws OXException {
        User user = getUser(userID);
        calendarUser.setEntity(user.getId());
        calendarUser.setCn(user.getDisplayName());
        calendarUser.setUri(getCalAddress(user));
        return calendarUser;
    }

    @Override
    public Attendee applyEntityData(Attendee attendee) throws OXException {
        return applyEntityData(attendee, (AttendeeField[]) null);
    }

    @Override
    public Attendee applyEntityData(Attendee attendee, AttendeeField... fields) throws OXException {
        if (null == attendee || false == CalendarUtils.isInternal(attendee)) {
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

    private <T extends CalendarUser> T applyEntityData(T calendarUser, CalendarUserType cuType) throws OXException {
        switch (cuType) {
            case GROUP:
                Group group = getGroup(calendarUser.getEntity());
                calendarUser.setCn(group.getDisplayName());
                calendarUser.setUri(ResourceId.forGroup(context.getContextId(), group.getIdentifier()));
                break;
            case RESOURCE:
            case ROOM:
                Resource resource = getResource(calendarUser.getEntity());
                calendarUser.setCn(resource.getDisplayName());
                calendarUser.setUri(ResourceId.forResource(context.getContextId(), resource.getIdentifier()));
                break;
            default:
                User user = getUser(calendarUser.getEntity());
                calendarUser.setCn(user.getDisplayName());
                calendarUser.setUri(getCalAddress(user));
                break;
        }
        return calendarUser;
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
            for (Integer resourceID : resourcesToLoad) {
                knownResources.put(resourceID, loadResource(resourceID.intValue()));
            }
        }
        if (0 < groupsToLoad.size()) {
            for (Integer groupID : groupsToLoad) {
                Group group = loadGroup(groupID.intValue());
                knownGroups.put(groupID, group);
                usersToLoad.addAll(java.util.Arrays.asList(i2I(group.getMember())));
            }
        }
        if (0 < usersToLoad.size()) {
            User[] users = loadUsers(I2i(usersToLoad));
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
            group = loadGroup(entity);
            knownGroups.put(id, group);
        }
        return group;
    }

    private User getUser(int entity) throws OXException {
        int id = I(entity);
        User user = knownUsers.get(id);
        if (null == user) {
            user = loadUser(entity);
            knownUsers.put(id, user);
        }
        return user;
    }

    private Resource getResource(int entity) throws OXException {
        int id = I(entity);
        Resource resource = knownResources.get(id);
        if (null == resource) {
            resource = loadResource(entity);
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
            attendee.setUri(getCalAddress(user));
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

    private <T extends CalendarUser> T resolveExternals(T calendarUser, CalendarUserType cuType) throws OXException {
        if (null != calendarUser) {
            if (0 < calendarUser.getEntity() || 0 == calendarUser.getEntity() && CalendarUserType.GROUP.equals(cuType)) {
                // already resolved
            } else {
                ResourceId resourceId = resolve(calendarUser.getUri());
                if (null != resourceId) {
                    if (false == resourceId.getCalendarUserType().equals(cuType)) {
                        throw CalendarExceptionCodes.INVALID_CALENDAR_USER.create(calendarUser.getUri(), I(calendarUser.getEntity()), cuType);
                    }
                    calendarUser.setEntity(resourceId.getEntity());
                }
            }
            resolveExternals(calendarUser.getSentBy(), CalendarUserType.INDIVIDUAL);
        }
        return calendarUser;
    }

    private void checkExistence(int entity, CalendarUserType type) throws OXException {
        switch (type) {
            case GROUP:
                getGroup(entity);
                break;
            case RESOURCE:
            case ROOM:
                getResource(entity);
                break;
            default:
                getUser(entity);
                break;
        }
    }

    private ResourceId resolve(String uri) throws OXException {
        return resolve(uri, true);
    }

    private ResourceId resolve(String uri, boolean considerAliases) throws OXException {
        if (Strings.isEmpty(uri)) {
            return null;
        }
        /*
         * try to interpret directly as resource id first
         */
        ResourceId resourceId = ResourceId.parse(uri);
        if (null != resourceId) {
            return resourceId;
        }
        /*
         * try lookup by e-mail address, otherwise
         */
        String mail = extractMail(uri);
        for (User knownUser : knownUsers.values()) {
            if (mail.equals(knownUser.getMail()) || considerAliases && Arrays.contains(knownUser.getAliases(), mail)) {
                return new ResourceId(context.getContextId(), knownUser.getId(), CalendarUserType.INDIVIDUAL);
            }
        }
        User user;
        try {
            user = services.getService(UserService.class).searchUser(mail, context, considerAliases);
        } catch (OXException e) {
            if ("USR-0014".equals(e.getErrorCode())) {
                user = null;
            } else {
                throw e;
            }
        }
        if (null != user) {
            knownUsers.put(I(user.getId()), user);
            return new ResourceId(context.getContextId(), user.getId(), CalendarUserType.INDIVIDUAL);
        }
        return null;
    }

    private Resource loadResource(int entity) throws OXException {
        try {
            return services.getService(ResourceService.class).getResource(entity, context);
        } catch (OXException e) {
            if ("RES-0012".equals(e.getErrorCode())) {
                throw CalendarExceptionCodes.INVALID_CALENDAR_USER.create(String.valueOf(entity), I(entity), CalendarUserType.RESOURCE);
            }
            throw e;
        }
    }

    private Group loadGroup(int entity) throws OXException {
        try {
            return services.getService(GroupService.class).getGroup(context, entity);
        } catch (OXException e) {
            if ("GRP-0017".equals(e.getErrorCode())) {
                throw CalendarExceptionCodes.INVALID_CALENDAR_USER.create(String.valueOf(entity), I(entity), CalendarUserType.GROUP);
            }
            throw e;
        }
    }

    private User loadUser(int entity) throws OXException {
        try {
            return services.getService(UserService.class).getUser(entity, context);
        } catch (OXException e) {
            if ("USR-0010".equals(e.getErrorCode())) {
                throw CalendarExceptionCodes.INVALID_CALENDAR_USER.create(String.valueOf(entity), I(entity), CalendarUserType.GROUP);
            }
            throw e;
        }
    }

    private User[] loadUsers(int[] entities) throws OXException {
        try {
            return services.getService(UserService.class).getUser(context, entities);
        } catch (OXException e) {
            if ("USR-0010".equals(e.getErrorCode())) {
                if (null != e.getLogArgs() && 0 < e.getLogArgs().length) {
                    Object arg = e.getLogArgs()[0];
                    throw CalendarExceptionCodes.INVALID_CALENDAR_USER.create(arg, arg, CalendarUserType.INDIVIDUAL);
                } else {
                    throw CalendarExceptionCodes.INVALID_CALENDAR_USER.create(java.util.Arrays.toString(entities), I(0), CalendarUserType.INDIVIDUAL);
                }
            }
            throw e;
        }
    }

    /**
     * Gets the calendar address for a user (as mailto URI).
     *
     * @param user The user
     * @return The calendar address
     */
    private static String getCalAddress(User user) {
        return "mailto:" + user.getMail(); // TODO: IDNA.toACE?
    }

    private static String extractMail(String value) {
        URI uri;
        try {
            uri = new URI(value);
        } catch (URISyntaxException e) {
            LOG.debug("Error parsing URI \"{}\", assuming \"mailto:\" protocol as fallback.", value, e);
            try {
                uri = new URI("mailto", value, null);
            } catch (URISyntaxException e2) {
                LOG.debug("Error constructing \"mailto:\" URI for \"{}\", interpreting directly as fallback.", value, e2);
                return value;
            }
        }
        String specificPart = uri.getSchemeSpecificPart();
        if (Strings.isNotEmpty(specificPart) && "mailto".equalsIgnoreCase(uri.getScheme())) {
            return uri.getSchemeSpecificPart();
        }
        return value;
    }

}
