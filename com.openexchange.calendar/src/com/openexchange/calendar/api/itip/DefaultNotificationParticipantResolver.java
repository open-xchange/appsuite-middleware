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

package com.openexchange.calendar.api.itip;

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
import com.openexchange.calendar.itip.ITipIntegrationUtility;
import com.openexchange.calendar.itip.ITipRole;
import com.openexchange.calendar.itip.generators.NotificationConfiguration;
import com.openexchange.calendar.itip.generators.NotificationParticipant;
import com.openexchange.calendar.itip.generators.NotificationParticipantResolver;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmStatus;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Autoboxing;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceService;
import com.openexchange.user.UserService;

/**
 * {@link DefaultNotificationParticipantResolver}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco
 *         Laguna</a>
 */
public class DefaultNotificationParticipantResolver implements
		NotificationParticipantResolver {
    
    Logger LOG = LoggerFactory.getLogger(DefaultNotificationParticipantResolver.class);

	private final UserService userService;
	private final GroupService groupService;
	private final ConfigurationService config;
	private final ResourceService resources;
	private final ITipIntegrationUtility util;

	public DefaultNotificationParticipantResolver(final GroupService groupService,
			final UserService userService, final ResourceService resources,
			final ConfigurationService config, final ITipIntegrationUtility util) {
		super();
		this.groupService = groupService;
		this.userService = userService;
		this.resources = resources;
		this.config = config;
		this.util = util;
	}

	// TODO: Principal
	@Override
    public List<NotificationParticipant> resolveAllRecipients(
			final Appointment original, final Appointment appointment, final User user,
			final User onBehalfOf, final Context ctx) throws OXException {
		final NotificationConfiguration defaultConfiguration = getDefaultConfiguration(
				user, ctx);

		final Map<Integer, UserParticipant> userIds = new HashMap<Integer, UserParticipant>();
		final List<ExternalUserParticipant> externalParticipants = new ArrayList<ExternalUserParticipant>(
				appointment.getParticipants().length);
		final Set<String> externalGuardian = new HashSet<String>();

		final Set<Integer> resourceIds = new HashSet<Integer>();

		Participant[] participants = appointment.getParticipants();
		if (participants != null) {
			for (final Participant participant : participants) {
				if (participant instanceof UserParticipant) {
					final UserParticipant userParticipant = (UserParticipant) participant;
					userIds.put(userParticipant.getIdentifier(),
							userParticipant);
				} else if (participant instanceof ExternalUserParticipant) {
					final ExternalUserParticipant ep = (ExternalUserParticipant) participant;
					if (!externalGuardian.contains(ep.getEmailAddress()
							.toLowerCase())) {
						externalParticipants.add(ep);
						externalGuardian
								.add(ep.getEmailAddress().toLowerCase());
					}
				} else if (participant instanceof ResourceParticipant) {
					final ResourceParticipant rp = (ResourceParticipant) participant;
					resourceIds.add(rp.getIdentifier());
				}
			}
		}

		UserParticipant[] users = appointment.getUsers();
		if (users != null) {
			for (final UserParticipant userParticipant : users) {
				userIds.put(userParticipant.getIdentifier(), userParticipant);
			}
		}

		if (original != null) {
			users = original.getUsers();
			if (users != null) {
				for (final UserParticipant userParticipant : users) {
					if (!userIds.containsKey(userParticipant.getIdentifier())) {
						userIds.put(userParticipant.getIdentifier(),
								userParticipant);
					}
				}
			}

			participants = original.getParticipants();
			if (participants != null) {
				for (final Participant participant : participants) {
					if (participant instanceof UserParticipant) {
						final UserParticipant userParticipant = (UserParticipant) participant;
						if (!userIds.containsKey(userParticipant
								.getIdentifier())) {
							userIds.put(userParticipant.getIdentifier(),
									userParticipant);
						}
					} else if (participant instanceof ExternalUserParticipant) {
						final ExternalUserParticipant ep = (ExternalUserParticipant) participant;
						if (!externalGuardian.contains(ep.getEmailAddress()
								.toLowerCase())) {
							externalParticipants.add(ep);
							externalGuardian.add(ep.getEmailAddress()
									.toLowerCase());
						}
					}
				}
			}
		}

		final User[] participantUsers = userService.getUser(ctx,
				Autoboxing.Coll2i(userIds.keySet()));
		String organizer = determineOrganizer(original, appointment, ctx);
		if (organizer.startsWith("mailto:")) {
			organizer = organizer.substring(7);
		}

		final List<NotificationParticipant> retval = new ArrayList<NotificationParticipant>(
				participantUsers.length + externalParticipants.size() + 1);

		boolean foundOrganizer = false;
		boolean foundUser = false;
		boolean foundOnBehalfOf = false;
		boolean foundPrincipal = false;

		final int appId = (appointment.getObjectID() <= 0 && original != null) ? original.getObjectID() : appointment.getObjectID();

		for (final User u : participantUsers) {
			final String mail = u.getMail();
			final int id = u.getId();

			final Set<ITipRole> roles = EnumSet.noneOf(ITipRole.class);

			roles.add((mail.equalsIgnoreCase(organizer) || id == appointment
					.getOrganizerId()) ? ITipRole.ORGANIZER : ITipRole.ATTENDEE);
			if (id == onBehalfOf.getId()) {
				roles.add(ITipRole.ON_BEHALF_OF);
			}
			if (id == appointment.getPrincipalId()) {
				roles.add(ITipRole.PRINCIPAL);
			}

			final NotificationParticipant participant = new NotificationParticipant(
					roles, false, mail, id);

			foundOrganizer = foundOrganizer
					|| participant.hasRole(ITipRole.ORGANIZER);
			foundUser = foundUser || id == user.getId();
			foundOnBehalfOf = foundOnBehalfOf || id == onBehalfOf.getId();
			foundPrincipal = foundPrincipal
					|| id == appointment.getPrincipalId();

			participant.setDisplayName(u.getDisplayName());
			participant.setLocale(u.getLocale());
			participant.setTimezone(TimeZone.getTimeZone(u.getTimeZone()));
			final UserParticipant userParticipant = userIds.get(id);
			if (userParticipant != null) {
				participant.setConfirmStatus(ConfirmStatus.byId(userParticipant
						.getConfirm()));
				participant.setComment(userParticipant.getConfirmMessage());
			}
			participant.setUser(u);
			participant.setContext(ctx);

			int folderIdForUser = util.getFolderIdForUser(appId, u.getId(), ctx.getContextId());
			if (folderIdForUser <= 0) {
			    folderIdForUser = appointment.getParentFolderID();
			}
            participant.setFolderId(folderIdForUser);

			final NotificationConfiguration configuration = defaultConfiguration
					.clone();
			configure(u, ctx, configuration,
					participant.hasRole(ITipRole.ORGANIZER));
			participant.setConfiguration(configuration);
			retval.add(participant);
		}
		// Add special users
		// TODO: Make this DRY
		if (!foundUser) {
			final String mail = user.getMail();
			final int id = user.getId();

			final Set<ITipRole> roles = EnumSet.noneOf(ITipRole.class);

			roles.add((mail.equalsIgnoreCase(organizer) || id == appointment
					.getOrganizerId()) ? ITipRole.ORGANIZER : ITipRole.ATTENDEE);
			if (id == onBehalfOf.getId()) {
				roles.add(ITipRole.ON_BEHALF_OF);
			}

			if (id == appointment.getPrincipalId()) {
				roles.add(ITipRole.PRINCIPAL);
			}

			final NotificationParticipant participant = new NotificationParticipant(
					roles, false, mail, id);

			foundOrganizer = foundOrganizer
					|| participant.hasRole(ITipRole.ORGANIZER);
			foundOnBehalfOf = foundOnBehalfOf || id == onBehalfOf.getId();
			foundPrincipal = foundPrincipal
					|| id == appointment.getPrincipalId();

			participant.setDisplayName(user.getDisplayName());
			participant.setLocale(user.getLocale());
			participant.setTimezone(TimeZone.getTimeZone(user.getTimeZone()));

			participant.setConfirmStatus(ConfirmStatus.NONE);

			participant.setUser(user);
			participant.setContext(ctx);
			participant.setFolderId(util.getFolderIdForUser(appId, user.getId(), ctx.getContextId()));


			final NotificationConfiguration configuration = defaultConfiguration
					.clone();
			configure(user, ctx, configuration,
					participant.hasRole(ITipRole.ORGANIZER));
			participant.setConfiguration(configuration);
			retval.add(participant);
		}

		if (!foundOnBehalfOf) {
			final String mail = onBehalfOf.getMail();
			final int id = onBehalfOf.getId();

			final Set<ITipRole> roles = EnumSet.noneOf(ITipRole.class);

			roles.add(mail.equalsIgnoreCase(organizer) ? ITipRole.ORGANIZER
					: ITipRole.ATTENDEE);
			if (id == onBehalfOf.getId()) {
				roles.add(ITipRole.ON_BEHALF_OF);
			}
			if (id == appointment.getPrincipalId()) {
				roles.add(ITipRole.PRINCIPAL);
			}

			final NotificationParticipant participant = new NotificationParticipant(
					roles, false, mail, id);

			foundOrganizer = foundOrganizer
					|| participant.hasRole(ITipRole.ORGANIZER);
			foundPrincipal = foundPrincipal
					|| id == appointment.getPrincipalId();

			participant.setDisplayName(onBehalfOf.getDisplayName());
			participant.setLocale(onBehalfOf.getLocale());
			participant.setTimezone(TimeZone.getTimeZone(onBehalfOf
					.getTimeZone()));

			participant.setConfirmStatus(ConfirmStatus.NONE);

			participant.setUser(onBehalfOf);
			participant.setContext(ctx);
			participant.setFolderId(util.getFolderIdForUser(appId, onBehalfOf.getId(), ctx.getContextId()));

			final NotificationConfiguration configuration = defaultConfiguration
					.clone();
			configure(onBehalfOf, ctx, configuration,
					participant.hasRole(ITipRole.ORGANIZER));
			participant.setConfiguration(configuration);
			retval.add(participant);
		}

		if (!foundPrincipal && appointment.getPrincipalId() > 0) {
			final User principalUser = userService.getUser(
					appointment.getPrincipalId(), ctx);
			final String mail = principalUser.getMail();
			final int id = principalUser.getId();

			final Set<ITipRole> roles = EnumSet.noneOf(ITipRole.class);

			roles.add(mail.equalsIgnoreCase(organizer) ? ITipRole.ORGANIZER
					: ITipRole.ATTENDEE);
			if (id == principalUser.getId()) {
				roles.add(ITipRole.ON_BEHALF_OF);
			}
			if (id == appointment.getPrincipalId()) {
				roles.add(ITipRole.PRINCIPAL);
			}

			final NotificationParticipant participant = new NotificationParticipant(
					roles, false, mail, id);

			foundOrganizer = foundOrganizer
					|| participant.hasRole(ITipRole.ORGANIZER);

			participant.setDisplayName(principalUser.getDisplayName());
			participant.setLocale(principalUser.getLocale());
			participant.setTimezone(TimeZone.getTimeZone(principalUser
					.getTimeZone()));

			participant.setConfirmStatus(ConfirmStatus.NONE);

			participant.setUser(principalUser);
			participant.setContext(ctx);
			participant.setFolderId(util.getFolderIdForUser(appId, principalUser.getId(), ctx.getContextId()));


			final NotificationConfiguration configuration = defaultConfiguration
					.clone();
			configure(principalUser, ctx, configuration,
					participant.hasRole(ITipRole.ORGANIZER));

			participant.setConfiguration(configuration);
			retval.add(participant);
		}

		final NotificationConfiguration resourceConfiguration = defaultConfiguration
				.clone();
		resourceConfiguration.setInterestedInChanges(true);
		resourceConfiguration.setInterestedInStateChanges(true);
		resourceConfiguration.setSendITIP(false);

		for (final Integer resourceId : resourceIds) {
			final Resource resource = resources.getResource(resourceId.intValue(), ctx);
			if (resource.getMail() != null) {
				final NotificationParticipant participant = new NotificationParticipant(
						ITipRole.ATTENDEE, false, resource.getMail());
				participant.setLocale(user.getLocale());
				participant.setTimezone(TimeZone.getDefault());
				participant.setResource(true);
				participant.setConfiguration(defaultConfiguration);
				retval.add(participant);
			}

		}

		final Map<String, ConfirmableParticipant> statusMap = new HashMap<String, ConfirmableParticipant>();
		final ConfirmableParticipant[] confirmations = appointment.getConfirmations();
		if (confirmations != null) {
			for (final ConfirmableParticipant p : confirmations) {
				statusMap.put(p.getEmailAddress(), p);
			}
		}

		for (final ExternalUserParticipant e : externalParticipants) {
			final String mail = e.getEmailAddress();
			final ITipRole role = (mail.equalsIgnoreCase(organizer)) ? ITipRole.ORGANIZER
					: ITipRole.ATTENDEE;

			foundOrganizer = foundOrganizer || role == ITipRole.ORGANIZER;

			final NotificationParticipant participant = new NotificationParticipant(
					role, true, mail);
			participant.setDisplayName(e.getDisplayName());
            participant.setTimezone(TimeZone.getTimeZone(appointment.getTimezone()));
			participant.setLocale(user.getLocale());
			final ConfirmableParticipant cp = statusMap.get(e.getEmailAddress());
			if (cp != null) {
				participant.setConfirmStatus(cp.getStatus());
				participant.setComment(cp.getMessage());
			}
			participant.setConfiguration(defaultConfiguration);
			retval.add(participant);
		}

		if (!foundOrganizer) {
			final User organizerUser = discoverOrganizer(appointment, ctx);
			final NotificationParticipant notificationOrganizer = new NotificationParticipant(
					ITipRole.ORGANIZER, organizerUser == null, organizer);
			final NotificationConfiguration configuration = defaultConfiguration.clone();
			if (organizerUser == null) {
			    LOG.warn("Unable to resolve Organizer for appointment: " + appointment.getObjectID() + " in context " + ctx.getContextId());
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

	private User discoverOrganizer(final Appointment appointment, final Context ctx) throws OXException {
		if (appointment.getOrganizerId() > 0) {
			return userService.getUser(appointment.getOrganizerId(), ctx);
		} else {
			String organizer = appointment.getOrganizer();
			if (organizer == null) {
				return userService.getUser(appointment.getCreatedBy(), ctx);
			}
			if (organizer.startsWith("mailto:")) {
				organizer = organizer.substring(7).toLowerCase();
			}
			try {
				return userService.searchUser(organizer, ctx);
			} catch (final OXException x) {
				return null;
			}
		}
	}

	private String determineOrganizer(final Appointment original,
			final Appointment appointment, final Context ctx) throws OXException {
		String organizer = appointment.getOrganizer();
		if (organizer == null && original != null) {
			organizer = original.getOrganizer();
		}
		if (organizer == null) {
			final User owner = userService.getUser(appointment.getCreatedBy(), ctx);
			organizer = owner.getMail();
		}
		if (organizer == null) {
			return "unknown";
		}
		return organizer.toLowerCase();
	}

	@Override
    public List<NotificationParticipant> getAllParticipants(
			final List<NotificationParticipant> allRecipients,
			final Appointment appointment, final User user, final Context ctx) {
		final Set<Integer> userIds = new HashSet<Integer>();
		final UserParticipant[] users = appointment.getUsers();
		if (users != null) {
			for (final UserParticipant userParticipant : users) {
				userIds.add(userParticipant.getIdentifier());
			}
		}

		final Set<String> externals = new HashSet<String>();
		final Participant[] participants = appointment.getParticipants();
		if (participants != null) {
			for (final Participant p : participants) {
				if (p instanceof UserParticipant) {
					final UserParticipant up = (UserParticipant) p;
					userIds.add(up.getIdentifier());
				}
				if (p instanceof ExternalUserParticipant) {
					final ExternalUserParticipant ep = (ExternalUserParticipant) p;
					externals.add(ep.getEmailAddress().toLowerCase());
				}
			}
		}

		final List<NotificationParticipant> filtered = new ArrayList<NotificationParticipant>();
		for (final NotificationParticipant p : allRecipients) {
			if (p.isExternal()
					&& externals.contains(p.getEmail().toLowerCase())) {
				filtered.add(p);
			} else if (!p.isExternal() && !p.isResource()
					&& userIds.contains(p.getUser().getId())) {
				filtered.add(p);
			}
		}

		return filtered;

	}

	@Override
    public List<NotificationParticipant> getResources(final Appointment appointment,
			final Context ctx) throws OXException {
		final Participant[] participants = appointment.getParticipants();
		if (participants == null) {
			return Collections.emptyList();
		}
		final List<NotificationParticipant> resourceParticipants = new ArrayList<NotificationParticipant>();
		for (final Participant participant : participants) {
			if (participant instanceof ResourceParticipant) {
				final ResourceParticipant rp = (ResourceParticipant) participant;
				final Resource resource = resources.getResource(rp.getIdentifier(),
						ctx);

				final NotificationParticipant np = new NotificationParticipant(
						ITipRole.ATTENDEE, false, resource.getMail());
				np.setDisplayName(resource.getDisplayName());
				resourceParticipants.add(np);
			}
		}
		return resourceParticipants;
	}

	private NotificationConfiguration getDefaultConfiguration(final User user,
			final Context ctx) {
		final NotificationConfiguration configuration = new NotificationConfiguration();

		configuration.setIncludeHTML(true); // TODO: pay attention to user
											// preferences

		configuration.setInterestedInChanges(true);
		configuration.setInterestedInStateChanges(true);
		configuration.setSendITIP(true);

		configuration.setForceCancelMails(config.getBoolProperty(
				"notify_participants_on_delete", true));

		return configuration;
	}

	private void configure(final User user, final Context ctx,
			final NotificationConfiguration config, final boolean isOrganizer) {
		final UserSettingMailStorage usmStorage = UserSettingMailStorage
				.getInstance();
		final UserSettingMail usm = usmStorage.getUserSettingMail(user.getId(), ctx);

		if (null != usm) {
    		config.setInterestedInChanges(usm.isNotifyAppointments());
    		if (isOrganizer) {
    			config.setInterestedInStateChanges(usm
    					.isNotifyAppointmentsConfirmOwner());
    		} else {
    			config.setInterestedInStateChanges(usm
    					.isNotifyAppointmentsConfirmParticipant());
    		}

    		config.setSendITIP(this.config.getBoolProperty("imipForInternalUsers",
    				false));
        } else {
            config.setInterestedInChanges(false);
            config.setInterestedInStateChanges(false);
            config.setSendITIP(false);
        }

	}

}
