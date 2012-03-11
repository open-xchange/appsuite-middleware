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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
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

	private UserService userService;
	private GroupService groupService;
	private ConfigurationService config;
	private ResourceService resources;
	private ITipIntegrationUtility util;

	public DefaultNotificationParticipantResolver(GroupService groupService,
			UserService userService, ResourceService resources,
			ConfigurationService config, ITipIntegrationUtility util) {
		super();
		this.groupService = groupService;
		this.userService = userService;
		this.resources = resources;
		this.config = config;
		this.util = util;
	}

	// TODO: Principal
	public List<NotificationParticipant> resolveAllRecipients(
			Appointment original, Appointment appointment, User user,
			User onBehalfOf, Context ctx) throws OXException {
		NotificationConfiguration defaultConfiguration = getDefaultConfiguration(
				user, ctx);

		Map<Integer, UserParticipant> userIds = new HashMap<Integer, UserParticipant>();
		List<ExternalUserParticipant> externalParticipants = new ArrayList<ExternalUserParticipant>(
				appointment.getParticipants().length);
		Set<String> externalGuardian = new HashSet<String>();

		Set<Integer> groupIds = new HashSet<Integer>();
		Set<Integer> resourceIds = new HashSet<Integer>();

		Participant[] participants = appointment.getParticipants();
		if (participants != null) {
			for (Participant participant : participants) {
				if (participant instanceof UserParticipant) {
					UserParticipant userParticipant = (UserParticipant) participant;
					userIds.put(userParticipant.getIdentifier(),
							userParticipant);
				} else if (participant instanceof ExternalUserParticipant) {
					ExternalUserParticipant ep = (ExternalUserParticipant) participant;
					if (!externalGuardian.contains(ep.getEmailAddress()
							.toLowerCase())) {
						externalParticipants.add(ep);
						externalGuardian
								.add(ep.getEmailAddress().toLowerCase());
					}
				} else if (participant instanceof GroupParticipant) {
					GroupParticipant gp = (GroupParticipant) participant;
					groupIds.add(gp.getIdentifier());
				} else if (participant instanceof ResourceParticipant) {
					ResourceParticipant rp = (ResourceParticipant) participant;
					resourceIds.add(rp.getIdentifier());
				}
			}
		}

		UserParticipant[] users = appointment.getUsers();
		if (users != null) {
			for (UserParticipant userParticipant : users) {
				userIds.put(userParticipant.getIdentifier(), userParticipant);
			}
		}

		if (original != null) {
			users = original.getUsers();
			if (users != null) {
				for (UserParticipant userParticipant : users) {
					if (!userIds.containsKey(userParticipant.getIdentifier())) {
						userIds.put(userParticipant.getIdentifier(),
								userParticipant);
					}
				}
			}

			participants = original.getParticipants();
			if (participants != null) {
				for (Participant participant : participants) {
					if (participant instanceof UserParticipant) {
						UserParticipant userParticipant = (UserParticipant) participant;
						if (!userIds.containsKey(userParticipant
								.getIdentifier())) {
							userIds.put(userParticipant.getIdentifier(),
									userParticipant);
						}
					} else if (participant instanceof ExternalUserParticipant) {
						ExternalUserParticipant ep = (ExternalUserParticipant) participant;
						if (!externalGuardian.contains(ep.getEmailAddress()
								.toLowerCase())) {
							externalParticipants.add(ep);
							externalGuardian.add(ep.getEmailAddress()
									.toLowerCase());
						}
					} else if (participant instanceof GroupParticipant) {
						GroupParticipant gp = (GroupParticipant) participant;
						groupIds.add(gp.getIdentifier());
					}
				}
			}
		}

		for (int id : groupIds) {
			int[] member = groupService.getGroup(ctx, id).getMember();
			for (int i : member) {
				if (!userIds.containsKey(i)) {
					userIds.put(i, new UserParticipant(i));
				}
			}
		}

		User[] participantUsers = userService.getUser(ctx,
				Autoboxing.Coll2i(userIds.keySet()));
		String organizer = determineOrganizer(original, appointment, ctx);
		if (organizer.startsWith("mailto:")) {
			organizer = organizer.substring(7);
		}

		List<NotificationParticipant> retval = new ArrayList<NotificationParticipant>(
				participantUsers.length + externalParticipants.size() + 1);

		boolean foundOrganizer = false;
		boolean foundUser = false;
		boolean foundOnBehalfOf = false;
		boolean foundPrincipal = false;
		
		int appId = (appointment.getObjectID() <= 0 && original != null) ? original.getObjectID() : appointment.getObjectID();

		for (User u : participantUsers) {
			String mail = u.getMail();
			int id = u.getId();

			Set<ITipRole> roles = EnumSet.noneOf(ITipRole.class);

			roles.add((mail.equalsIgnoreCase(organizer) || id == appointment
					.getOrganizerId()) ? ITipRole.ORGANIZER : ITipRole.ATTENDEE);
			if (id == onBehalfOf.getId()) {
				roles.add(ITipRole.ON_BEHALF_OF);
			}
			if (id == appointment.getPrincipalId()) {
				roles.add(ITipRole.PRINCIPAL);
			}

			NotificationParticipant participant = new NotificationParticipant(
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
			UserParticipant userParticipant = userIds.get(id);
			if (userParticipant != null) {
				participant.setConfirmStatus(ConfirmStatus.byId(userParticipant
						.getConfirm()));
				participant.setComment(userParticipant.getConfirmMessage());
			}
			participant.setUser(u);
			participant.setContext(ctx);
			
			participant.setFolderId(util.getFolderIdForUser(appId, u.getId(), ctx.getContextId()));

			NotificationConfiguration configuration = defaultConfiguration
					.clone();
			configure(u, ctx, configuration,
					participant.hasRole(ITipRole.ORGANIZER));
			participant.setConfiguration(configuration);
			retval.add(participant);
		}
		// Add special users
		// TODO: Make this DRY
		if (!foundUser) {
			String mail = user.getMail();
			int id = user.getId();

			Set<ITipRole> roles = EnumSet.noneOf(ITipRole.class);

			roles.add((mail.equalsIgnoreCase(organizer) || id == appointment
					.getOrganizerId()) ? ITipRole.ORGANIZER : ITipRole.ATTENDEE);
			if (id == onBehalfOf.getId()) {
				roles.add(ITipRole.ON_BEHALF_OF);
			}

			if (id == appointment.getPrincipalId()) {
				roles.add(ITipRole.PRINCIPAL);
			}

			NotificationParticipant participant = new NotificationParticipant(
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


			NotificationConfiguration configuration = defaultConfiguration
					.clone();
			configure(user, ctx, configuration,
					participant.hasRole(ITipRole.ORGANIZER));
			participant.setConfiguration(configuration);
			retval.add(participant);
		}

		if (!foundOnBehalfOf) {
			String mail = onBehalfOf.getMail();
			int id = onBehalfOf.getId();

			Set<ITipRole> roles = EnumSet.noneOf(ITipRole.class);

			roles.add(mail.equalsIgnoreCase(organizer) ? ITipRole.ORGANIZER
					: ITipRole.ATTENDEE);
			if (id == onBehalfOf.getId()) {
				roles.add(ITipRole.ON_BEHALF_OF);
			}
			if (id == appointment.getPrincipalId()) {
				roles.add(ITipRole.PRINCIPAL);
			}

			NotificationParticipant participant = new NotificationParticipant(
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

			NotificationConfiguration configuration = defaultConfiguration
					.clone();
			configure(onBehalfOf, ctx, configuration,
					participant.hasRole(ITipRole.ORGANIZER));
			participant.setConfiguration(configuration);
			retval.add(participant);
		}

		if (!foundPrincipal && appointment.getPrincipalId() > 0) {
			User principalUser = userService.getUser(
					appointment.getPrincipalId(), ctx);
			String mail = principalUser.getMail();
			int id = principalUser.getId();

			Set<ITipRole> roles = EnumSet.noneOf(ITipRole.class);

			roles.add(mail.equalsIgnoreCase(organizer) ? ITipRole.ORGANIZER
					: ITipRole.ATTENDEE);
			if (id == principalUser.getId()) {
				roles.add(ITipRole.ON_BEHALF_OF);
			}
			if (id == appointment.getPrincipalId()) {
				roles.add(ITipRole.PRINCIPAL);
			}

			NotificationParticipant participant = new NotificationParticipant(
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


			NotificationConfiguration configuration = defaultConfiguration
					.clone();
			configure(principalUser, ctx, configuration,
					participant.hasRole(ITipRole.ORGANIZER));

			participant.setConfiguration(configuration);
			retval.add(participant);
		}

		NotificationConfiguration resourceConfiguration = defaultConfiguration
				.clone();
		resourceConfiguration.setInterestedInChanges(true);
		resourceConfiguration.setInterestedInStateChanges(true);
		resourceConfiguration.setSendITIP(false);

		for (Integer resourceId : resourceIds) {
			Resource resource = resources.getResource(resourceId, ctx);
			if (resource.getMail() != null) {
				NotificationParticipant participant = new NotificationParticipant(
						ITipRole.ATTENDEE, false, resource.getMail());
				participant.setLocale(user.getLocale());
				participant.setTimezone(TimeZone.getDefault());
				participant.setResource(true);
				participant.setConfiguration(defaultConfiguration);
				retval.add(participant);
			}

		}

		Map<String, ConfirmableParticipant> statusMap = new HashMap<String, ConfirmableParticipant>();
		ConfirmableParticipant[] confirmations = appointment.getConfirmations();
		if (confirmations != null) {
			for (ConfirmableParticipant p : confirmations) {
				statusMap.put(p.getEmailAddress(), p);
			}
		}

		for (ExternalUserParticipant e : externalParticipants) {
			String mail = e.getEmailAddress();
			ITipRole role = (mail.equalsIgnoreCase(organizer)) ? ITipRole.ORGANIZER
					: ITipRole.ATTENDEE;

			foundOrganizer = foundOrganizer || role == ITipRole.ORGANIZER;

			NotificationParticipant participant = new NotificationParticipant(
					role, true, mail);
			participant.setDisplayName(e.getDisplayName());
			participant.setTimezone(TimeZone.getDefault());
			participant.setLocale(user.getLocale());
			ConfirmableParticipant cp = statusMap.get(e.getEmailAddress());
			if (cp != null) {
				participant.setConfirmStatus(cp.getStatus());
				participant.setComment(cp.getMessage());
			}
			participant.setConfiguration(defaultConfiguration);
			retval.add(participant);
		}

		if (!foundOrganizer) {
			User organizerUser = discoverOrganizer(appointment, ctx);
			NotificationParticipant notificationOrganizer = new NotificationParticipant(
					ITipRole.ORGANIZER, organizerUser == null, organizer);
			NotificationConfiguration configuration = defaultConfiguration.clone();
			if (organizerUser != null) {
				configure(organizerUser, ctx, configuration, true);
				notificationOrganizer.setUser(organizerUser);
				notificationOrganizer.setContext(ctx);
			}
			notificationOrganizer.setConfiguration(configuration);
			
			retval.add(notificationOrganizer);
		}

		return retval;
	}

	private User discoverOrganizer(Appointment appointment, Context ctx) throws OXException {
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
			} catch (OXException x) {
				return null;
			}
		}
	}

	private String determineOrganizer(Appointment original,
			Appointment appointment, Context ctx) throws OXException {
		String organizer = appointment.getOrganizer();
		if (organizer == null && original != null) {
			organizer = original.getOrganizer();
		}
		if (organizer == null) {
			User owner = userService.getUser(appointment.getCreatedBy(), ctx);
			organizer = owner.getMail();
		}
		if (organizer == null) {
			return "unknown";
		}
		return organizer.toLowerCase();
	}

	public List<NotificationParticipant> getAllParticipants(
			List<NotificationParticipant> allRecipients,
			Appointment appointment, User user, Context ctx) {
		Set<Integer> userIds = new HashSet<Integer>();
		UserParticipant[] users = appointment.getUsers();
		if (users != null) {
			for (UserParticipant userParticipant : users) {
				userIds.add(userParticipant.getIdentifier());
			}
		}

		Set<String> externals = new HashSet<String>();
		Participant[] participants = appointment.getParticipants();
		if (participants != null) {
			for (Participant p : participants) {
				if (p instanceof UserParticipant) {
					UserParticipant up = (UserParticipant) p;
					userIds.add(up.getIdentifier());
				}
				if (p instanceof ExternalUserParticipant) {
					ExternalUserParticipant ep = (ExternalUserParticipant) p;
					externals.add(ep.getEmailAddress().toLowerCase());
				}
			}
		}

		List<NotificationParticipant> filtered = new ArrayList<NotificationParticipant>();
		for (NotificationParticipant p : allRecipients) {
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

	public List<NotificationParticipant> getResources(Appointment appointment,
			Context ctx) throws OXException {
		Participant[] participants = appointment.getParticipants();
		if (participants == null) {
			return Collections.emptyList();
		}
		List<NotificationParticipant> resourceParticipants = new ArrayList<NotificationParticipant>();
		for (Participant participant : participants) {
			if (participant instanceof ResourceParticipant) {
				ResourceParticipant rp = (ResourceParticipant) participant;
				Resource resource = resources.getResource(rp.getIdentifier(),
						ctx);

				NotificationParticipant np = new NotificationParticipant(
						ITipRole.ATTENDEE, false, resource.getMail());
				np.setDisplayName(resource.getDisplayName());
				resourceParticipants.add(np);
			}
		}
		return resourceParticipants;
	}

	private NotificationConfiguration getDefaultConfiguration(User user,
			Context ctx) {
		NotificationConfiguration configuration = new NotificationConfiguration();

		configuration.setIncludeHTML(true); // TODO: pay attention to user
											// preferences

		configuration.setInterestedInChanges(true);
		configuration.setInterestedInStateChanges(true);
		configuration.setSendITIP(true);

		configuration.setForceCancelMails(config.getBoolProperty(
				"notify_participants_on_delete", true));

		return configuration;
	}

	private void configure(User user, Context ctx,
			NotificationConfiguration config, boolean isOrganizer) {
		UserSettingMailStorage usmStorage = UserSettingMailStorage
				.getInstance();
		UserSettingMail usm = usmStorage.getUserSettingMail(user.getId(), ctx);

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

	}

}
