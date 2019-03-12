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

package com.openexchange.chronos.itip.generators;

import static com.openexchange.java.Autoboxing.Coll2i;
import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.itip.ITipIntegrationUtility;
import com.openexchange.chronos.itip.ITipRole;
import com.openexchange.chronos.itip.osgi.Services;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tools.alias.UserAliasUtility;
import com.openexchange.java.Strings;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceService;
import com.openexchange.session.Session;
import com.openexchange.user.UserService;

/**
 * {@link ITipNotificationParticipantResolver}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ITipNotificationParticipantResolver implements NotificationParticipantResolver {

    private final static Logger LOG = LoggerFactory.getLogger(ITipNotificationParticipantResolver.class);

    protected final UserService userService;
    protected final ConfigurationService config;
    protected final ResourceService resources;
    private final ITipIntegrationUtility util;

    public ITipNotificationParticipantResolver(ITipIntegrationUtility util) {
        this(util, Services.getService(ConfigurationService.class), Services.getService(UserService.class), Services.getService(ResourceService.class));
    }

    public ITipNotificationParticipantResolver(ITipIntegrationUtility util, ConfigurationService configurationService, UserService userService, ResourceService resourceService) {
        super();
        this.userService = userService;
        this.resources = resourceService;
        this.config = configurationService;
        this.util = util;
    }


    // TODO: Principal
    @Override
    public List<NotificationParticipant> resolveAllRecipients(Event original, Event update, User user, User onBehalfOf, Context ctx, Session session, CalendarUser principal) throws OXException {
        final NotificationConfiguration defaultConfiguration = getDefaultConfiguration(user, ctx);

        final Map<Integer, Attendee> userIds = new HashMap<Integer, Attendee>();
        final List<Attendee> externalParticipants = new ArrayList<>();
        final Set<String> externalGuardian = new HashSet<String>();

        final Set<Integer> resourceIds = new HashSet<Integer>();

        List<Attendee> attendees = update.getAttendees();
        if (attendees != null) {
            for (final Attendee attendee : attendees) {
                if (CalendarUtils.isInternal(attendee)) {
                    if (CalendarUserType.RESOURCE.equals(attendee.getCuType()) || CalendarUserType.ROOM.equals(attendee.getCuType())) {
                        resourceIds.add(I(attendee.getEntity()));
                    } else if (false == CalendarUserType.GROUP.equals(attendee.getCuType())) {
                        userIds.put(I(attendee.getEntity()), attendee);
                    }
                } else {
                    String mail = CalendarUtils.extractEMailAddress(attendee.getUri());
                    if (!externalGuardian.contains(mail)) {
                        externalParticipants.add(attendee);
                        externalGuardian.add(mail);
                    }
                }
            }
        }

        if (original != null) {
            attendees = original.getAttendees();
            if (attendees != null) {
                for (Attendee attendee : attendees) {
                    if (CalendarUtils.isInternal(attendee)) {
                        if (CalendarUserType.RESOURCE.equals(attendee.getCuType()) || CalendarUserType.ROOM.equals(attendee.getCuType())) {
                            resourceIds.add(I(attendee.getEntity()));
                        } else if (false == CalendarUserType.GROUP.equals(attendee.getCuType())) {
                            userIds.putIfAbsent(I(attendee.getEntity()), attendee);
                        }
                    } else {
                        String mail = CalendarUtils.extractEMailAddress(attendee.getUri());
                        if (!externalGuardian.contains(mail)) {
                            externalParticipants.add(attendee);
                            externalGuardian.add(mail);
                        }
                    }
                }
            }
        }

        final User[] participantUsers = userService.getUser(ctx, Coll2i(userIds.keySet()));
        CalendarUser organizer = determineOrganizer(original, update, ctx, session.getUserId());
        String organizerMail = CalendarUtils.extractEMailAddress(organizer.getEMail());
        if (Strings.isNotEmpty(organizerMail) && organizerMail.toLowerCase().startsWith("mailto:")) {
            organizerMail = organizerMail.substring(7);
        }

        final List<NotificationParticipant> retval = new ArrayList<NotificationParticipant>(participantUsers.length + externalParticipants.size() + 1);

        boolean foundOrganizer = false;
        boolean foundUser = false;
        boolean foundOnBehalfOf = false;
        boolean foundPrincipal = false;

        for (final User u : participantUsers) {
            final int id = u.getId();
            Attendee userParticipant = userIds.get(I(id));
            final String mail = getMailAddress(u, userParticipant);

            final Set<ITipRole> roles = EnumSet.noneOf(ITipRole.class);

            roles.add((id == organizer.getEntity() || mail.equalsIgnoreCase(organizerMail)) ? ITipRole.ORGANIZER : ITipRole.ATTENDEE);
            if (id == onBehalfOf.getId()) {
                roles.add(ITipRole.ON_BEHALF_OF);
            }
            if (principal != null && principal.getEntity() == id) {
                roles.add(ITipRole.PRINCIPAL);
            }

            final NotificationParticipant participant = new NotificationParticipant(roles, false, mail, id);

            foundOrganizer = foundOrganizer || participant.hasRole(ITipRole.ORGANIZER);
            foundUser = foundUser || id == user.getId();
            foundOnBehalfOf = foundOnBehalfOf || id == onBehalfOf.getId();
            foundPrincipal = foundPrincipal || roles.contains(ITipRole.PRINCIPAL);

            participant.setLocale(u.getLocale());
            participant.setTimezone(TimeZone.getTimeZone(u.getTimeZone()));
            if (null != userParticipant) {
                participant.setDisplayName(userParticipant.getCn());
                participant.setConfirmStatus(userParticipant.getPartStat());
                participant.setComment(userParticipant.getComment());
                participant.setHidden(userParticipant.isHidden());
            }
            participant.setUser(u);
            participant.setContext(ctx);

            String folderIdForUser = getFolderIdForUser(update, ctx.getContextId(), u.getId());
            if (folderIdForUser == null) {
                folderIdForUser = update.getFolderId();
            }
            participant.setFolderId(CalendarUtils.prependDefaultAccount(folderIdForUser));

            final NotificationConfiguration configuration = defaultConfiguration.clone();
            configure(u, ctx, configuration, participant.hasRole(ITipRole.ORGANIZER));
            participant.setConfiguration(configuration);
            retval.add(participant);
        }
        // Add special users
        // TODO: Make this DRY
        if (!foundUser) {
            final String mail = user.getMail();
            final int id = user.getId();

            final Set<ITipRole> roles = EnumSet.noneOf(ITipRole.class);

            roles.add((id == organizer.getEntity() || mail.equalsIgnoreCase(organizerMail)) ? ITipRole.ORGANIZER : ITipRole.ATTENDEE);
            if (id == onBehalfOf.getId()) {
                roles.add(ITipRole.ON_BEHALF_OF);
            }

            if (principal != null && principal.getEntity() == id) {
                roles.add(ITipRole.PRINCIPAL);
            }

            final NotificationParticipant participant = new NotificationParticipant(roles, false, mail, id);

            foundOrganizer = foundOrganizer || participant.hasRole(ITipRole.ORGANIZER);
            foundOnBehalfOf = foundOnBehalfOf || id == onBehalfOf.getId();
            foundPrincipal = foundPrincipal || (null != principal && id == principal.getEntity());

            participant.setDisplayName(user.getDisplayName());
            participant.setLocale(user.getLocale());
            participant.setTimezone(TimeZone.getTimeZone(user.getTimeZone()));

            participant.setConfirmStatus(ParticipationStatus.NEEDS_ACTION);

            participant.setUser(user);
            participant.setContext(ctx);
            participant.setFolderId(getFolderIdForUser(update, ctx.getContextId(), user.getId()));

            final NotificationConfiguration configuration = defaultConfiguration.clone();
            configure(user, ctx, configuration, participant.hasRole(ITipRole.ORGANIZER));
            participant.setConfiguration(configuration);
            retval.add(participant);
        }

        if (!foundOnBehalfOf) {
            final String mail = onBehalfOf.getMail();
            final int id = onBehalfOf.getId();

            final Set<ITipRole> roles = EnumSet.noneOf(ITipRole.class);

            roles.add(mail.equalsIgnoreCase(organizerMail) ? ITipRole.ORGANIZER : ITipRole.ATTENDEE);
            if (id == onBehalfOf.getId()) {
                roles.add(ITipRole.ON_BEHALF_OF);
            }
            if (null != principal && principal.getEntity() == id) {
                roles.add(ITipRole.PRINCIPAL);
            }

            final NotificationParticipant participant = new NotificationParticipant(roles, false, mail, id);

            foundOrganizer = foundOrganizer || participant.hasRole(ITipRole.ORGANIZER);
            foundPrincipal = foundPrincipal || roles.contains(ITipRole.PRINCIPAL);

            participant.setDisplayName(onBehalfOf.getDisplayName());
            participant.setLocale(onBehalfOf.getLocale());
            participant.setTimezone(TimeZone.getTimeZone(onBehalfOf.getTimeZone()));

            participant.setConfirmStatus(ParticipationStatus.NEEDS_ACTION);

            participant.setUser(onBehalfOf);
            participant.setContext(ctx);
            participant.setFolderId(getFolderIdForUser(update, ctx.getContextId(), onBehalfOf.getId()));

            final NotificationConfiguration configuration = defaultConfiguration.clone();
            configure(onBehalfOf, ctx, configuration, participant.hasRole(ITipRole.ORGANIZER));
            participant.setConfiguration(configuration);
            retval.add(participant);
        }

        if (!foundPrincipal && null != principal && principal.getEntity() > 0) {
            final User principalUser = userService.getUser(principal.getEntity(), ctx);
            final String mail = principalUser.getMail();
            final int id = principalUser.getId();

            final Set<ITipRole> roles = EnumSet.noneOf(ITipRole.class);

            roles.add(mail.equalsIgnoreCase(organizerMail) ? ITipRole.ORGANIZER : ITipRole.ATTENDEE);
            if (id == principalUser.getId()) {
                roles.add(ITipRole.ON_BEHALF_OF);
            }
            if (principal.getEntity() == id) {
                roles.add(ITipRole.PRINCIPAL);
            }

            final NotificationParticipant participant = new NotificationParticipant(roles, false, mail, id);

            foundOrganizer = foundOrganizer || participant.hasRole(ITipRole.ORGANIZER);

            participant.setDisplayName(principalUser.getDisplayName());
            participant.setLocale(principalUser.getLocale());
            participant.setTimezone(TimeZone.getTimeZone(principalUser.getTimeZone()));

            participant.setConfirmStatus(ParticipationStatus.NEEDS_ACTION);

            participant.setUser(principalUser);
            participant.setContext(ctx);
            participant.setFolderId(getFolderIdForUser(update, ctx.getContextId(), principalUser.getId()));

            final NotificationConfiguration configuration = defaultConfiguration.clone();
            configure(principalUser, ctx, configuration, participant.hasRole(ITipRole.ORGANIZER));

            participant.setConfiguration(configuration);
            retval.add(participant);
        }

        final NotificationConfiguration resourceConfiguration = defaultConfiguration.clone();
        resourceConfiguration.setInterestedInChanges(true);
        resourceConfiguration.setInterestedInStateChanges(true);
        resourceConfiguration.setSendITIP(false);

        for (final Integer resourceId : resourceIds) {
            final Resource resource = resources.getResource(resourceId.intValue(), ctx);
            if (resource.getMail() != null) {
                final NotificationParticipant participant = new NotificationParticipant(ITipRole.ATTENDEE, false, resource.getMail());
                participant.setLocale(user.getLocale());
                participant.setTimezone(TimeZone.getDefault());
                participant.setResource(true);
                participant.setConfiguration(defaultConfiguration.clone());
                participant.setDisplayName(resource.getDisplayName());
                retval.add(participant);
            }

        }

        final Map<String, Attendee> statusMap = new HashMap<>();
        final List<Attendee> confirmations = update.getAttendees();
        if (confirmations != null) {
            for (Attendee p : confirmations) {
                String mail = CalendarUtils.extractEMailAddress(p.getUri());
                if (null != mail && CalendarUtils.isExternalUser(p)) {
                    statusMap.put(mail, p);
                }
            }
        }

        for (Attendee e : externalParticipants) {
            final String mail = CalendarUtils.extractEMailAddress(e.getUri());
            if (null == mail) {
                LOG.warn("The attendee {} has no mail address to write to. Attendees URI is \"{}\" Skipping it.", e.getCn(), e.getUri());
                continue;
            }
            final ITipRole role = (mail.equalsIgnoreCase(organizerMail)) ? ITipRole.ORGANIZER : ITipRole.ATTENDEE;

            foundOrganizer = foundOrganizer || role == ITipRole.ORGANIZER;

            final NotificationParticipant participant = new NotificationParticipant(role, true, mail);
            participant.setDisplayName(e.getCn());
            participant.setTimezone(null == update.getStartDate() ? TimeZone.getDefault() : update.getStartDate().getTimeZone());
            participant.setLocale(user.getLocale());
            Attendee cp = statusMap.get(mail);
            if (cp != null) {
                participant.setConfirmStatus(cp.getPartStat());
                participant.setComment(cp.getComment());
            }
            participant.setConfiguration(defaultConfiguration.clone());
            retval.add(participant);
        }

        if (!foundOrganizer && Strings.isNotEmpty(organizerMail)) {
            /*
             * Organizer does not attend the event. Nevertheless notify the organizer (if email address is available).
             */
            boolean isInternal = CalendarUtils.isInternal(organizer, CalendarUserType.INDIVIDUAL);
            final NotificationParticipant notificationOrganizer = new NotificationParticipant(ITipRole.ORGANIZER, !isInternal, organizerMail, isInternal ? organizer.getEntity() : 0);
            final NotificationConfiguration configuration = defaultConfiguration.clone();
            if (isInternal) {
                User organizerUser = userService.getUser(organizer.getEntity(), ctx);
                configure(organizerUser, ctx, configuration, true);
                notificationOrganizer.setUser(organizerUser);
                notificationOrganizer.setContext(ctx);
                notificationOrganizer.setLocale(organizerUser.getLocale());
                notificationOrganizer.setTimezone(TimeZone.getTimeZone(organizerUser.getTimeZone()));
                notificationOrganizer.setFolderId(getFolderIdForUser(update, ctx.getContextId(), organizer.getEntity()));
            }
            notificationOrganizer.setDisplayName(organizer.getCn());
            notificationOrganizer.setConfiguration(configuration);

            retval.add(notificationOrganizer);
        }

        return retval;
    }

    protected String getMailAddress(User u, Attendee userParticipant) {
        if (CalendarUtils.isInternalUser(userParticipant) && null != userParticipant) {
            String mail = CalendarUtils.extractEMailAddress(userParticipant.getUri());
            if (Strings.isNotEmpty(mail)) {
                if (UserAliasUtility.isAlias(mail, u.getAliases())) {
                    return mail;
                }
            }
        }
        return u.getMail();
    }

    protected CalendarUser determineOrganizer(Event original, Event update, final Context ctx, int userId) throws OXException {
        if (update.containsOrganizer() && null != update.getOrganizer()) {
            return update.getOrganizer();
        } else if (null != original && original.containsOrganizer() && null != original.getOrganizer()) {
            return original.getOrganizer();
        } else if (update.containsCreatedBy() && null != update.getCreatedBy()) {
            return update.getCreatedBy();
        }
        // Use current user as fall back
        LOG.debug("Unable to resolve organizer for appointment: {} in context {}. Using current user as organizer", update.getId(), ctx.getContextId());
        User defaultOrganizer = userService.getUser(userId, ctx);
        CalendarUser cu = new CalendarUser();
        cu.setCn(defaultOrganizer.getDisplayName());
        cu.setEMail(defaultOrganizer.getMail());
        cu.setEntity(defaultOrganizer.getId());
        cu.setUri(CalendarUtils.getURI(defaultOrganizer.getMail()));
        return cu;
    }

    @Override
    public List<NotificationParticipant> getAllParticipants(final List<NotificationParticipant> allRecipients, final Event event) {
        final List<NotificationParticipant> filtered = new ArrayList<NotificationParticipant>();
        final Set<Integer> userIds = new HashSet<Integer>();
        final List<Attendee> users = event.getAttendees();
        final Set<String> externals = new HashSet<String>();
        if (users != null) {
            for (Attendee userParticipant : users) {
                if (CalendarUtils.isInternalUser(userParticipant)) {
                    userIds.add(I(userParticipant.getEntity()));
                } else if (CalendarUtils.isExternalUser(userParticipant)) {
                    externals.add(CalendarUtils.extractEMailAddress(userParticipant.getUri()));
                }
            }
            for (final NotificationParticipant p : allRecipients) {
                if (p.isExternal() && externals.contains(p.getEmail())) {
                    filtered.add(p);
                } else if (!p.isExternal() && !p.isResource() && userIds.contains(I(p.getUser().getId()))) {
                    filtered.add(p);
                }
            }
        }
        return filtered;
    }

    @Override
    public List<NotificationParticipant> getResources(final Event event) {
        final List<Attendee> resources = CalendarUtils.filter(event.getAttendees(), Boolean.TRUE, CalendarUserType.RESOURCE);
        if (resources == null) {
            return Collections.emptyList();
        }
        final List<NotificationParticipant> resourceParticipants = new ArrayList<NotificationParticipant>();
        for (final Attendee resource : resources) {
            String email = CalendarUtils.extractEMailAddress(resource.getUri());
            if (Strings.isNotEmpty(email)) {
                final NotificationParticipant np = new NotificationParticipant(ITipRole.ATTENDEE, false, email);
                np.setDisplayName(resource.getCn());
                resourceParticipants.add(np);
            } else {
                LOG.debug("Resource {} has no mail address.", resource.getCn());
            }
        }
        return resourceParticipants;
    }

    protected NotificationConfiguration getDefaultConfiguration(final User user, final Context ctx) {
        final NotificationConfiguration configuration = new NotificationConfiguration();

        configuration.setIncludeHTML(true); // TODO: pay attention to user
                                           // preferences

        configuration.setInterestedInChanges(true);
        configuration.setInterestedInStateChanges(true);
        configuration.setSendITIP(true);

        configuration.setForceCancelMails(config.getBoolProperty("notify_participants_on_delete", true));

        return configuration;
    }

    protected void configure(final User user, final Context ctx, final NotificationConfiguration config, final boolean isOrganizer) {
        final UserSettingMailStorage usmStorage = UserSettingMailStorage.getInstance();
        final UserSettingMail usm = usmStorage.getUserSettingMail(user.getId(), ctx);

        if (null != usm) {
            config.setInterestedInChanges(usm.isNotifyAppointments());
            if (isOrganizer) {
                config.setInterestedInStateChanges(usm.isNotifyAppointmentsConfirmOwner());
            } else {
                config.setInterestedInStateChanges(usm.isNotifyAppointmentsConfirmParticipant());
            }

            config.setSendITIP(this.config.getBoolProperty("imipForInternalUsers", false));
        } else {
            config.setInterestedInChanges(false);
            config.setInterestedInStateChanges(false);
            config.setSendITIP(false);
        }

    }

    protected String getFolderIdForUser(Event event, int contextId, int userId) throws OXException {
        /*
         * get folder view from event data
         */
        if (null != event) {
            if (false == event.containsAttendees()) {
                throw new UnsupportedOperationException();
            }
            try {
                return CalendarUtils.getFolderView(event, userId);
            } catch (OXException e) {
                if (false == CalendarExceptionCodes.ATTENDEE_NOT_FOUND.equals(e)) {
                    throw e;
                }
            }
        }
        /*
         * fall back to user's default calendar folder
         */
        return util.getPrivateCalendarFolderId(contextId, userId);
    }

}
