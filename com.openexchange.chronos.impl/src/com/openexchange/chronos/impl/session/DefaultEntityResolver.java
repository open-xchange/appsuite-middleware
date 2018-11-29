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

package com.openexchange.chronos.impl.session;

import static com.openexchange.chronos.common.CalendarUtils.extractEMailAddress;
import static com.openexchange.chronos.common.CalendarUtils.getURI;
import static com.openexchange.chronos.common.CalendarUtils.isInternal;
import static com.openexchange.chronos.common.CalendarUtils.optEMailAddress;
import static com.openexchange.chronos.common.CalendarUtils.optTimeZone;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.java.Autoboxing.i2I;
import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.ResourceId;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tools.alias.UserAliasUtility;
import com.openexchange.java.Strings;
import com.openexchange.java.util.TimeZones;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;
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
    private final Map<Integer, FolderObject> knownFolders;

    /**
     * Initializes a new {@link DefaultEntityResolver}.
     *
     * @param session The underlying session
     * @param services A service lookup reference
     */
    public DefaultEntityResolver(ServerSession session, ServiceLookup services) {
        this(session.getContext(), services);
        knownUsers.put(I(session.getUserId()), session.getUser());
    }

    /**
     * Initializes a new {@link DefaultEntityResolver}.
     *
     * @param contextId The context identifier
     * @param services A service lookup reference
     */
    public DefaultEntityResolver(int contextId, ServiceLookup services) throws OXException {
        this(services.getService(ContextService.class).loadContext(contextId), services);
    }

    /**
     * Initializes a new {@link DefaultEntityResolver}.
     *
     * @param context The context
     * @param services A service lookup reference
     */
    public DefaultEntityResolver(Context context, ServiceLookup services) {
        super();
        this.services = services;
        this.context = context;
        knownUsers = new HashMap<Integer, User>();
        knownGroups = new HashMap<Integer, Group>();
        knownResources = new HashMap<Integer, Resource>();
        knownFolders = new HashMap<Integer, FolderObject>();
    }

    @Override
    public List<Attendee> prepare(List<Attendee> attendees) throws OXException {
        return prepare(attendees, false);
    }

    @Override
    public List<Attendee> prepare(List<Attendee> attendees, boolean resolveResourceIds) throws OXException {
        if (null != attendees) {
            for (Attendee attendee : attendees) {
                /*
                 * try and resolve external calendar user address to internal calendar user, enhance with static properties
                 */
                applyEntityData(resolveExternals(attendee, attendee.getCuType(), resolveResourceIds));
            }
        }
        return attendees;
    }

    @Override
    public <T extends CalendarUser> T prepare(T calendarUser, CalendarUserType cuType) throws OXException {
        return prepare(calendarUser, cuType, false);
    }

    public <T extends CalendarUser> T prepare(T calendarUser, CalendarUserType cuType, boolean resolveResourceIds) throws OXException {
        if (null == calendarUser) {
            return null;
        }
        /*
         * try and resolve external calendar user address to internal calendar user, enhance with static properties
         */
        return applyEntityData(resolveExternals(calendarUser, cuType, resolveResourceIds), cuType);
    }

    @Override
    public int[] getGroupMembers(int groupID) throws OXException {
        return getGroup(groupID).getMember();
    }

    @Override
    public TimeZone getTimeZone(int userID) throws OXException {
        return optTimeZone(getUser(userID).getTimeZone(), TimeZones.UTC);
    }

    @Override
    public Locale getLocale(int userID) throws OXException {
        return getUser(userID).getLocale();
    }

    @Override
    public int getContactId(int userID) throws OXException {
        return getUser(userID).getContactId();
    }

    @Override
    public Attendee prepareUserAttendee(int userID) throws OXException {
        return applyEntityData(new Attendee(), getUser(userID));
    }

    @Override
    public Attendee prepareGroupAttendee(int groupID) throws OXException {
        return applyEntityData(new Attendee(), getGroup(groupID));
    }

    @Override
    public Attendee prepareResourceAttendee(int resourceID) throws OXException {
        return applyEntityData(new Attendee(), getResource(resourceID));
    }

    @Override
    public CalendarUserType probeCUType(int entity) throws OXException {
        if (null != optUser(entity)) {
            return CalendarUserType.INDIVIDUAL;
        }
        if (null != optGroup(entity)) {
            return CalendarUserType.GROUP;
        }
        if (null != optResource(entity)) {
            return CalendarUserType.RESOURCE;
        }
        return null;
    }

    @Override
    public <T extends CalendarUser> T applyEntityData(T calendarUser, int userID) throws OXException {
        User user = getUser(userID);
        calendarUser.setEntity(user.getId());
        calendarUser.setCn(user.getDisplayName());
        calendarUser.setUri(getCalAddress(user));
        calendarUser.setEMail(getEMail(user));
        return calendarUser;
    }

    @Override
    public Attendee applyEntityData(Attendee attendee) throws OXException {
        if (null == attendee) {
            LOG.warn("Ignoring attempt to apply entity data for passed null reference");
            return attendee;
        }
        if (isInternal(attendee)) {
            /*
             * apply known entity data for internal attendees
             */
            if (CalendarUserType.GROUP.equals(attendee.getCuType())) {
                applyEntityData(attendee, getGroup(attendee.getEntity()));
            } else if (CalendarUserType.RESOURCE.equals(attendee.getCuType()) || CalendarUserType.ROOM.equals(attendee.getCuType())) {
                applyEntityData(attendee, getResource(attendee.getEntity()));
            } else {
                applyEntityData(attendee, getUser(attendee.getEntity()));
            }
        } else {
            /*
             * copy over email address for external attendees
             */
            if (null == attendee.getEMail()) {
                attendee.setEMail(optEMailAddress(attendee.getUri()));
            }
        }
        /*
         * do the same with a proxy calendar user in "sent-by"
         */
        if (null != attendee.getSentBy()) {
            try {
                applyEntityData(attendee.getSentBy(), CalendarUserType.INDIVIDUAL);
            } catch (OXException e) {
                if (CalendarExceptionCodes.INVALID_CALENDAR_USER.equals(e)) {
                    LOG.debug("Ignoring invalid proxy {} for SENT-BY property of {}.", attendee.getSentBy(), attendee, e);
                    attendee.setSentBy(null);
                } else {
                    throw e;
                }
            }
        }
        return attendee;
    }

    @Override
    public <T extends CalendarUser> T applyEntityData(T calendarUser, CalendarUserType cuType) throws OXException {
        if (null == calendarUser) {
            LOG.warn("Ignoring attempt to apply entity data for passed null reference");
            return calendarUser;
        }
        if (isInternal(calendarUser, cuType)) {
            /*
             * apply known entity data for internal attendees
             */
            if (CalendarUserType.GROUP.equals(cuType)) {
                Group group = getGroup(calendarUser.getEntity());
                calendarUser.setCn(group.getDisplayName());
                calendarUser.setUri(ResourceId.forGroup(context.getContextId(), group.getIdentifier()));
            } else if (CalendarUserType.RESOURCE.equals(cuType) || CalendarUserType.ROOM.equals(cuType)) {
                Resource resource = getResource(calendarUser.getEntity());
                calendarUser.setCn(resource.getDisplayName());
                calendarUser.setUri(ResourceId.forResource(context.getContextId(), resource.getIdentifier()));
            } else {
                User user = getUser(calendarUser.getEntity());
                calendarUser.setCn(user.getDisplayName());
                calendarUser.setUri(getCalAddress(user));
                calendarUser.setEMail(getEMail(user));
            }
        } else {
            /*
             * copy over email address for external attendees
             */
            if (null == calendarUser.getEMail()) {
                calendarUser.setEMail(optEMailAddress(calendarUser.getUri()));
            }
        }
        /*
         * do the same with a proxy calendar user in "sent-by"
         */
        if (null != calendarUser.getSentBy()) {
            try {
                applyEntityData(calendarUser.getSentBy(), CalendarUserType.INDIVIDUAL);
            } catch (OXException e) {
                if (CalendarExceptionCodes.INVALID_CALENDAR_USER.equals(e)) {
                    LOG.debug("Ignoring invalid proxy {} for SENT-BY property of {}.", calendarUser.getSentBy(), calendarUser, e);
                    calendarUser.setSentBy(null);
                } else {
                    throw e;
                }
            }
        }
        return calendarUser;
    }

    @Override
    public void prefetch(List<Attendee> attendees) {
        Set<Integer> usersToLoad = new HashSet<Integer>();
        Set<Integer> groupsToLoad = new HashSet<Integer>();
        Set<Integer> resourcesToLoad = new HashSet<Integer>();
        for (Attendee attendee : attendees) {
            if (isInternal(attendee)) {
                Integer id = I(attendee.getEntity());
                if (CalendarUserType.GROUP.equals(attendee.getCuType())) {
                    if (false == knownGroups.containsKey(id)) {
                        groupsToLoad.add(id);
                    }
                } else if (CalendarUserType.RESOURCE.equals(attendee.getCuType()) || CalendarUserType.ROOM.equals(attendee.getCuType())) {
                    if (false == knownResources.containsKey(id)) {
                        resourcesToLoad.add(id);
                    }
                } else {
                    if (false == knownUsers.containsKey(id)) {
                        usersToLoad.add(id);
                    }
                }
            }
        }
        if (0 < resourcesToLoad.size()) {
            for (Integer resourceID : resourcesToLoad) {
                try {
                    knownResources.put(resourceID, loadResource(i(resourceID)));
                } catch (OXException e) {
                    LOG.debug("Error loading resource with id {}, skipping during pre-fetch.", resourceID, e);
                }
            }
        }
        if (0 < groupsToLoad.size()) {
            for (Integer groupID : groupsToLoad) {
                try {
                    Group group = loadGroup(i(groupID));
                    knownGroups.put(groupID, group);
                    usersToLoad.addAll(java.util.Arrays.asList(i2I(group.getMember())));
                } catch (OXException e) {
                    LOG.debug("Error loading resource with id {}, skipping during pre-fetch.", groupID, e);
                }
            }
        }
        if (0 < usersToLoad.size()) {
            try {
                User[] users = loadUsers(I2i(usersToLoad));
                for (User user : users) {
                    knownUsers.put(I(user.getId()), user);
                }
            } catch (OXException e) {
                LOG.debug("Error loading users with ids {}, skipping during pre-fetch.", usersToLoad, e);
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
        knownFolders.clear();
    }

    private Group getGroup(int entity) throws OXException {
        Integer id = I(entity);
        Group group = knownGroups.get(id);
        if (null == group) {
            group = loadGroup(entity);
            knownGroups.put(id, group);
        }
        return group;
    }

    private Group optGroup(int entity) throws OXException {
        Integer id = I(entity);
        Group group = knownGroups.get(id);
        if (null == group) {
            try {
                group = loadGroup(entity);
            } catch (OXException e) {
                if (CalendarExceptionCodes.INVALID_CALENDAR_USER.equals(e)) {
                    return null;
                }
                throw e;
            }
            knownGroups.put(id, group);
        }
        return group;
    }

    private User getUser(int entity) throws OXException {
        Integer id = I(entity);
        User user = knownUsers.get(id);
        if (null == user) {
            user = loadUser(entity);
            knownUsers.put(id, user);
        }
        return user;
    }

    private User optUser(int entity) throws OXException {
        Integer id = I(entity);
        User user = knownUsers.get(id);
        if (null == user) {
            try {
                user = loadUser(entity);
            } catch (OXException e) {
                if (CalendarExceptionCodes.INVALID_CALENDAR_USER.equals(e)) {
                    return null;
                }
                throw e;
            }
            knownUsers.put(id, user);
        }
        return user;
    }

    private Resource getResource(int entity) throws OXException {
        Integer id = I(entity);
        Resource resource = knownResources.get(id);
        if (null == resource) {
            resource = loadResource(entity);
            knownResources.put(id, resource);
        }
        return resource;
    }

    private Resource optResource(int entity) throws OXException {
        Integer id = I(entity);
        Resource resource = knownResources.get(id);
        if (null == resource) {
            try {
                resource = loadResource(entity);
            } catch (OXException e) {
                if (CalendarExceptionCodes.INVALID_CALENDAR_USER.equals(e)) {
                    return null;
                }
                throw e;
            }
            knownResources.put(id, resource);
        }
        return resource;
    }


    /**
     * Gets a folder by its identifier.
     *
     * @param id The identifier of the folder to get
     * @return The folder
     */
    public FolderObject getFolder(int id) throws OXException {
        return getFolder(id, null);
    }

    /**
     * Gets a folder by its identifier.
     *
     * @param id The identifier of the folder to get
     * @param optConnection An optional connection to the database to use, or <code>null</code> to acquire one dynamically
     * @return The folder
     */
    public FolderObject getFolder(int id, Connection optConnection) throws OXException {
        Integer iD = I(id);
        FolderObject folder = knownFolders.get(iD);
        if (null == folder) {
            folder = loadFolder(iD, optConnection);
            knownFolders.put(iD, folder);
        }
        return folder;
    }

    /**
     * Optionally gets a folder by its identifier, if it exists.
     *
     * @param id The identifier of the folder to get
     * @return The folder, or <code>null</code> it doesn't exist
     */
    public FolderObject optFolder(int id) throws OXException {
        try {
            return getFolder(id);
        } catch (OXException e) {
            if (CalendarExceptionCodes.FOLDER_NOT_FOUND.equals(e)) {
                return null;
            }
            throw e;
        }
    }

    /**
     * Gets all visible calendar folders for a specific user.
     *
     * @param userId The identifier of the user to get the visible folders for
     * @param optConnection An optional connection to the database to use, or <code>null</code> to acquire one dynamically
     * @return The folders, or an empty list if there are none
     */
    public List<FolderObject> getVisibleFolders(int userId, Connection optConnection) throws OXException {
        List<FolderObject> folders = loadVisibleFolders(userId, optConnection);
        if (null == folders) {
            return Collections.emptyList();
        }
        for (FolderObject folder : folders) {
            knownFolders.put(I(folder.getObjectID()), folder);
        }
        return folders;
    }

    private Attendee applyEntityData(Attendee attendee, User user) throws OXException {
        attendee.setEntity(user.getId());
        attendee.setCuType(CalendarUserType.INDIVIDUAL);
        attendee.setCn(Strings.isNotEmpty(attendee.getCn()) ? attendee.getCn() : user.getDisplayName());
        if (Strings.isEmpty(attendee.getUri())) {
            attendee.setUri(getCalAddress(user));
            attendee.setEMail(getEMail(user));
        } else {
            attendee.setUri(Check.calendarAddressMatches(attendee.getUri(), context.getContextId(), user));
            String email = optEMailAddress(attendee.getUri());
            attendee.setEMail(null != email ? email : getEMail(user));
        }
        return attendee;
    }

    private Attendee applyEntityData(Attendee attendee, Group group) {
        attendee.setEntity(group.getIdentifier());
        attendee.setCuType(CalendarUserType.GROUP);
        attendee.setCn(group.getDisplayName());
        attendee.setPartStat(ParticipationStatus.ACCEPTED);
        attendee.setUri(ResourceId.forGroup(context.getContextId(), group.getIdentifier()));
        return attendee;
    }

    private Attendee applyEntityData(Attendee attendee, Resource resource) {
        attendee.setEntity(resource.getIdentifier());
        attendee.setCuType(CalendarUserType.RESOURCE);
        attendee.setCn(resource.getDisplayName());
        attendee.setComment(resource.getDescription());
        attendee.setPartStat(ParticipationStatus.ACCEPTED);
        attendee.setUri(ResourceId.forResource(context.getContextId(), resource.getIdentifier()));
        attendee.setEMail(resource.getMail());
        return attendee;
    }

    private <T extends CalendarUser> T resolveExternals(T calendarUser, CalendarUserType cuType, boolean resolveResourceIds) throws OXException {
        if (null != calendarUser) {
            if (false == isInternal(calendarUser, cuType)) {
                ResourceId resourceId = resolveExternals(calendarUser.getUri(), resolveResourceIds, true);
                if (null != resourceId) {
                    /*
                     * resolved successfully; cross-check calendar user type, auto-correct if required
                     */
                    if (Attendee.class.isInstance(calendarUser)) {
                        Attendee attendee = (Attendee) calendarUser;
                        if (false == resourceId.getCalendarUserType().matches(attendee.getCuType())) {
                            LOG.warn("Wrong calendar user type {} for internal entity {} ({}), auto-correcting to {}.", 
                                attendee.getCuType(), I(attendee.getEntity()), attendee.getUri(), resourceId.getCalendarUserType());
                            attendee.setCuType(resourceId.getCalendarUserType());
                        }
                    } else if (false == resourceId.getCalendarUserType().matches(cuType)) {
                        throw CalendarExceptionCodes.INVALID_CALENDAR_USER.create(calendarUser.getUri(), I(calendarUser.getEntity()), cuType);
                    }
                    calendarUser.setEntity(resourceId.getEntity());
                }
            }
            resolveExternals(calendarUser.getSentBy(), CalendarUserType.INDIVIDUAL, resolveResourceIds);
        }
        return calendarUser;
    }

    /**
     * Tries to resolve a calendar user address URI to its corresponding internal resource identifier.
     * 
     * @param uri The URI to resolve
     * @param resolveResourceIds <code>true</code> to resolve (internal) resource identifiers, <code>false</code>, otherwise
     * @param considerAliases <code>true</code> to consider user aliases when resolving <code>mailto:</code>-URIs, <code>false</code>, otherwise
     * @return The resolved resource identifier, or <code>null</code> if not found
     */
    private ResourceId resolveExternals(String uri, boolean resolveResourceIds, boolean considerAliases) throws OXException {
        if (Strings.isEmpty(uri)) {
            return null;
        }
        /*
         * try to interpret directly as resource id first
         */
        ResourceId resourceId = ResourceId.parse(uri);
        if (null != resourceId) {
            /*
             * resource id matched, take over if exists and not organized externally, otherwise assume external calendar user
             */
            if (resolveResourceIds && context.getContextId() == resourceId.getContextID()) {
                try {
                    return Check.exists(this, resourceId);
                } catch (OXException e) {
                    LOG.warn("Calendar user {} not found in context {}.", uri, I(context.getContextId()), e);
                }
            }
            return null;
        }
        /*
         * try lookup by e-mail address, otherwise
         */
        String mail = extractEMailAddress(uri);
        for (User knownUser : knownUsers.values()) {
            if (mail.equals(getEMail(knownUser)) || considerAliases && UserAliasUtility.isAlias(mail, knownUser.getAliases())) {
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
                throw CalendarExceptionCodes.INVALID_CALENDAR_USER.create(e, String.valueOf(entity), I(entity), CalendarUserType.RESOURCE);
            }
            throw e;
        }
    }

    private Group loadGroup(int entity) throws OXException {
        try {
            return services.getService(GroupService.class).getGroup(context, entity);
        } catch (OXException e) {
            if ("GRP-0017".equals(e.getErrorCode())) {
                throw CalendarExceptionCodes.INVALID_CALENDAR_USER.create(e, String.valueOf(entity), I(entity), CalendarUserType.GROUP);
            }
            throw e;
        }
    }

    private User loadUser(int entity) throws OXException {
        try {
            return services.getService(UserService.class).getUser(entity, context);
        } catch (OXException e) {
            if ("USR-0010".equals(e.getErrorCode())) {
                throw CalendarExceptionCodes.INVALID_CALENDAR_USER.create(e, String.valueOf(entity), I(entity), CalendarUserType.INDIVIDUAL);
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
                    throw CalendarExceptionCodes.INVALID_CALENDAR_USER.create(e, arg, arg, CalendarUserType.INDIVIDUAL);
                } else {
                    throw CalendarExceptionCodes.INVALID_CALENDAR_USER.create(e, java.util.Arrays.toString(entities), I(0), CalendarUserType.INDIVIDUAL);
                }
            }
            throw e;
        }
    }

    private FolderObject loadFolder(int id, Connection optConnection) throws OXException {
        try {
            return new OXFolderAccess(optConnection, context).getFolderObject(id);
        } catch (OXException e) {
            if ("FLD-0008".equals(e.getErrorCode())) {
                throw CalendarExceptionCodes.FOLDER_NOT_FOUND.create(e, String.valueOf(id));
            }
            throw e;
        }
    }

    private List<FolderObject> loadVisibleFolders(int userId, Connection optConnection) throws OXException {
        SearchIterator<FolderObject> iterator = null;
        try {
            iterator = OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfModule(
                userId, getUser(userId).getGroups(), null, FolderObject.CALENDAR, context, optConnection);
            return SearchIterators.asList(iterator);
        } finally {
            SearchIterators.close(iterator);
        }
    }

    /**
     * Gets the calendar addresses for a user (which includes the user's resource identifier, as well as his e-mail addresses as
     * <code>mailto</code>-URIs), wrapped into a case-insensitive tree-set.
     *
     * @param user The user
     * @return The calendar addresses
     */
    //    private Set<String> getCalAddresses(User user) {
    //        TreeSet<String> addresses = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    //        if (null != user.getMail()) {
    //            addresses.add(getURI(user.getMail()));
    //        }
    //        if (null != user.getAliases()) {
    //            for (String alias : user.getAliases()) {
    //                addresses.add(getURI(alias));
    //            }
    //        }
    //        addresses.add(ResourceId.forUser(getContextID(), user.getId()));
    //        return addresses;
    //    }

    /**
     * Gets the calendar address for a user (as <code>mailto</code>-URI).
     *
     * @param user The user
     * @return The calendar address
     */
    private static String getCalAddress(User user) {
        return getURI(getEMail(user));
    }

    /**
     * Gets the e-mail address for a user.
     *
     * @param user The user
     * @return The e-mail address
     */
    private static String getEMail(User user) {
        return user.getMail();
    }

}
