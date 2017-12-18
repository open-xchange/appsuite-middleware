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
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.CalendarUtils;
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
 * {@link DefaultNotificationParticipantResolver}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DefaultNotificationParticipantResolver implements NotificationParticipantResolver {

    Logger LOG = LoggerFactory.getLogger(DefaultNotificationParticipantResolver.class);

    private final UserService            userService;
    private final ConfigurationService   config;
    private final ResourceService        resources;
    private final ITipIntegrationUtility util;

    public DefaultNotificationParticipantResolver(ITipIntegrationUtility util) {
        super();
        this.userService = Services.getService(UserService.class);
        this.resources = Services.getService(ResourceService.class);
        this.config = Services.getService(ConfigurationService.class);
        this.util = util;
    }

    // TODO: Principal
    @Override
    public List<NotificationParticipant> resolveAllRecipients(Event original, Event appointment, User user, User onBehalfOf, Context ctx, Session session, CalendarUser principal) throws OXException {
        final NotificationConfiguration defaultConfiguration = getDefaultConfiguration(user, ctx);

        final Map<Integer, Attendee> userIds = new HashMap<Integer, Attendee>();
        final List<Attendee> externalParticipants = new ArrayList<>();
        final Set<String> externalGuardian = new HashSet<String>();

        final Set<Integer> resourceIds = new HashSet<Integer>();

        List<Attendee> participants = appointment.getAttendees();
        if (participants != null) {
            for (final Attendee participant : participants) {
                if (CalendarUtils.isInternalUser(participant)) {
                    userIds.put(I(participant.getEntity()), participant);
                } else if (CalendarUtils.isExternalUser(participant)) {
                    String mail = CalendarUtils.extractEMailAddress(participant.getUri());
                    if (!externalGuardian.contains(mail)) {
                        externalParticipants.add(participant);
                        externalGuardian.add(mail);
                    }
                } else if (CalendarUserType.RESOURCE.equals(participant.getCuType())) {
                    resourceIds.add(I(participant.getEntity()));
                }
            }
        }

        if (original != null) {
            participants = original.getAttendees();
            if (participants != null) {
                for (Attendee participant : participants) {
                    if (CalendarUtils.isInternalUser(participant)) {
                        if (!userIds.containsKey(I(participant.getEntity()))) {
                            userIds.put(I(participant.getEntity()), participant);
                        }
                    } else if (CalendarUtils.isExternalUser(participant)) {
                        String mail = CalendarUtils.extractEMailAddress(participant.getUri());
                        if (!externalGuardian.contains(mail)) {
                            externalParticipants.add(participant);
                            externalGuardian.add(mail);
                        }
                    }
                }
            }
        }

        final User[] participantUsers = userService.getUser(ctx, Coll2i(userIds.keySet()));
        String organizer = determineOrganizer(original, appointment, ctx);
        if (organizer.startsWith("mailto:")) {
            organizer = organizer.substring(7);
        }

        final List<NotificationParticipant> retval = new ArrayList<NotificationParticipant>(participantUsers.length + externalParticipants.size() + 1);

        boolean foundOrganizer = false;
        boolean foundUser = false;
        boolean foundOnBehalfOf = false;
        boolean foundPrincipal = false;

        final String appId = (appointment.getId() == null && original != null) ? original.getId() : appointment.getId();

        for (final User u : participantUsers) {
            final int id = u.getId();
            Attendee userParticipant = userIds.get(I(id));
            final String mail = getMailAddress(u, userParticipant);

            final Set<ITipRole> roles = EnumSet.noneOf(ITipRole.class);

            roles.add((mail.equalsIgnoreCase(organizer) || id == appointment.getOrganizer().getEntity()) ? ITipRole.ORGANIZER : ITipRole.ATTENDEE);
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
            }
            participant.setUser(u);
            participant.setContext(ctx);

            String folderIdForUser = util.getFolderIdForUser(session, appId);
            if (folderIdForUser == null) {
                folderIdForUser = appointment.getFolderId();
            }
            participant.setFolderId(folderIdForUser);

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

            roles.add((mail.equalsIgnoreCase(organizer) || id == appointment.getOrganizer().getEntity()) ? ITipRole.ORGANIZER : ITipRole.ATTENDEE);
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
            participant.setFolderId(util.getFolderIdForUser(session, appId));

            final NotificationConfiguration configuration = defaultConfiguration.clone();
            configure(user, ctx, configuration, participant.hasRole(ITipRole.ORGANIZER));
            participant.setConfiguration(configuration);
            retval.add(participant);
        }

        if (!foundOnBehalfOf) {
            final String mail = onBehalfOf.getMail();
            final int id = onBehalfOf.getId();

            final Set<ITipRole> roles = EnumSet.noneOf(ITipRole.class);

            roles.add(mail.equalsIgnoreCase(organizer) ? ITipRole.ORGANIZER : ITipRole.ATTENDEE);
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
            participant.setFolderId(util.getFolderIdForUser(session, appId));

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

            roles.add(mail.equalsIgnoreCase(organizer) ? ITipRole.ORGANIZER : ITipRole.ATTENDEE);
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
            participant.setFolderId(util.getFolderIdForUser(session, appId));

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
                participant.setConfiguration(defaultConfiguration);
                retval.add(participant);
            }

        }

        final Map<String, Attendee> statusMap = new HashMap<>();
        final List<Attendee> confirmations = appointment.getAttendees();
        if (confirmations != null) {
            for (Attendee p : confirmations) {
                String mail = CalendarUtils.extractEMailAddress(p.getUri());
                if (null != mail && CalendarUtils.isExternalUser(p))
                    statusMap.put(mail, p);
            }
        }

        for (Attendee e : externalParticipants) {
            final String mail = CalendarUtils.extractEMailAddress(e.getUri());
            if (null == mail) {
                LOG.warn("The attendee {} has no mail address to write to. Attendees URI is \"{}\" Skipping it.", e.getCn(), e.getUri());
                continue;
            }
            final ITipRole role = (mail.equalsIgnoreCase(organizer)) ? ITipRole.ORGANIZER : ITipRole.ATTENDEE;

            foundOrganizer = foundOrganizer || role == ITipRole.ORGANIZER;

            final NotificationParticipant participant = new NotificationParticipant(role, true, mail);
            participant.setDisplayName(e.getCn());
            participant.setTimezone(null == appointment.getStartDate() ? TimeZone.getDefault() : appointment.getStartDate().getTimeZone());
            participant.setLocale(user.getLocale());
            Attendee cp = statusMap.get(mail);
            if (cp != null) {
                participant.setConfirmStatus(cp.getPartStat());
                participant.setComment(cp.getComment());
            }
            participant.setConfiguration(defaultConfiguration);
            retval.add(participant);
        }

        if (!foundOrganizer) {
            final User organizerUser = discoverOrganizer(appointment, ctx);
            final NotificationParticipant notificationOrganizer = new NotificationParticipant(ITipRole.ORGANIZER, organizerUser == null, organizer, organizerUser == null ? -1 : organizerUser.getId());
            final NotificationConfiguration configuration = defaultConfiguration.clone();
            if (organizerUser == null) {
                LOG.warn("Unable to resolve Organizer for appointment: " + appointment.getId() + " in context " + ctx.getContextId());
            } else {
                configure(organizerUser, ctx, configuration, true);
                notificationOrganizer.setUser(organizerUser);
                notificationOrganizer.setContext(ctx);
                notificationOrganizer.setDisplayName(organizerUser.getDisplayName());
                notificationOrganizer.setLocale(organizerUser.getLocale());
                notificationOrganizer.setTimezone(TimeZone.getTimeZone(organizerUser.getTimeZone()));
            }
            notificationOrganizer.setConfiguration(configuration);

            retval.add(notificationOrganizer);
        }

        return retval;
    }

    private String getMailAddress(User u, Attendee userParticipant) {
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

    private User discoverOrganizer(final Event appointment, final Context ctx) throws OXException {
        if (appointment.getOrganizer() != null && appointment.getOrganizer().getEntity() > 0) {
            return userService.getUser(appointment.getOrganizer().getEntity(), ctx);
        } else {
            Organizer organizer = appointment.getOrganizer();
            if (organizer == null) {
                return userService.getUser(appointment.getCreatedBy().getEntity(), ctx);
            }
            String organizerMail = CalendarUtils.extractEMailAddress(organizer.getUri());
            if (null != organizerMail) {
                try {
                    return userService.searchUser(organizerMail, ctx);
                } catch (final OXException x) {
                    // Fall through
                    LOG.debug("", x);
                }
            }
        }
        return null;
    }

    private String determineOrganizer(Event original, Event appointment, final Context ctx) throws OXException {
        final Organizer organizer;
        if (appointment.getOrganizer() != null) {
            organizer = appointment.getOrganizer();
        } else {
            organizer = original.getOrganizer();
        }
        if (organizer == null) {
            final User owner = userService.getUser(appointment.getCreatedBy().getEntity(), ctx);
            return owner == null ? "unknown" : owner.getMail();
        }
        return CalendarUtils.extractEMailAddress(organizer.getUri());
    }

    @Override
    public List<NotificationParticipant> getAllParticipants(final List<NotificationParticipant> allRecipients, final Event appointment, final User user, final Context ctx) {
        final List<NotificationParticipant> filtered = new ArrayList<NotificationParticipant>();
        final Set<Integer> userIds = new HashSet<Integer>();
        final List<Attendee> users = appointment.getAttendees();
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
    public List<NotificationParticipant> getResources(final Event appointment, final Context ctx) throws OXException {
        final List<Attendee> resources = CalendarUtils.filter(appointment.getAttendees(), Boolean.TRUE, CalendarUserType.RESOURCE);
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

    private NotificationConfiguration getDefaultConfiguration(final User user, final Context ctx) {
        final NotificationConfiguration configuration = new NotificationConfiguration();

        configuration.setIncludeHTML(true); // TODO: pay attention to user
                                           // preferences

        configuration.setInterestedInChanges(true);
        configuration.setInterestedInStateChanges(true);
        configuration.setSendITIP(true);

        configuration.setForceCancelMails(config.getBoolProperty("notify_participants_on_delete", true));

        return configuration;
    }

    private void configure(final User user, final Context ctx, final NotificationConfiguration config, final boolean isOrganizer) {
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

}
