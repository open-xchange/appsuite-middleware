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

package com.openexchange.chronos.alarm.mail.notification;

import static com.openexchange.java.Autoboxing.Coll2i;
import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
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
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.itip.ITipRole;
import com.openexchange.chronos.itip.generators.ITipNotificationParticipantResolver;
import com.openexchange.chronos.itip.generators.NotificationConfiguration;
import com.openexchange.chronos.itip.generators.NotificationParticipant;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceService;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.user.UserService;

/**
 * {@link MailAlarmNotificationParticipantResolver}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.1
 */
public class MailAlarmNotificationParticipantResolver extends ITipNotificationParticipantResolver {

    private static final Logger LOG = LoggerFactory.getLogger(MailAlarmNotificationParticipantResolver.class.getName());
    private ContextService contextService;

    public MailAlarmNotificationParticipantResolver(ConfigurationService configurationService, UserService userService, ResourceService resourceService, ContextService contextService) {
        super(null, configurationService, userService, resourceService);
        this.contextService = contextService;
    }

    @Override
    public List<NotificationParticipant> resolveAllRecipients(Event original, Event event, User user, User onBehalfOf, Context ctx, Session session, CalendarUser principal) throws OXException {
        return resolveAllRecipients(event, user, ctx);
    }

    private List<NotificationParticipant> resolveAllRecipients(Event event, User user, Context ctx) throws OXException {
        final NotificationConfiguration defaultConfiguration = getDefaultConfiguration(user, ctx);

        final Map<Integer, Attendee> userIds = new HashMap<Integer, Attendee>();
        final List<Attendee> externalParticipants = new ArrayList<>();
        final Set<String> externalGuardian = new HashSet<String>();
        final Set<Integer> resourceIds = new HashSet<Integer>();

        List<Attendee> participants = event.getAttendees();
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

        final User[] participantUsers = userService.getUser(ctx, Coll2i(userIds.keySet()));
        CalendarUser organizer = determineOrganizer(null, event, ctx, user.getId());
        String organizerMail = CalendarUtils.extractEMailAddress(organizer.getEMail());
        if (organizerMail.toLowerCase().startsWith("mailto:")) {
            organizerMail = organizerMail.substring(7);
        }

        final List<NotificationParticipant> retval = new ArrayList<NotificationParticipant>(participantUsers.length + externalParticipants.size() + 1);

        for (final User u : participantUsers) {
            final int id = u.getId();
            Attendee userParticipant = userIds.get(I(id));
            final String mail = getMailAddress(u, userParticipant);

            final Set<ITipRole> roles = EnumSet.noneOf(ITipRole.class);

            roles.add((id == organizer.getEntity() || mail.equalsIgnoreCase(organizerMail)) ? ITipRole.ORGANIZER : ITipRole.ATTENDEE);

            final NotificationParticipant participant = new NotificationParticipant(roles, false, mail, id);

            participant.setLocale(u.getLocale());
            participant.setTimezone(TimeZone.getTimeZone(u.getTimeZone()));
            if (null != userParticipant) {
                participant.setDisplayName(userParticipant.getCn());
                participant.setConfirmStatus(userParticipant.getPartStat());
                participant.setComment(userParticipant.getComment());
            }
            participant.setUser(u);
            participant.setContext(ctx);

            String folderIdForUser = getFolderIdForUser(event, ctx.getContextId(), u.getId());
            if (folderIdForUser == null) {
                folderIdForUser = event.getFolderId();
            }
            participant.setFolderId(CalendarUtils.prependDefaultAccount(folderIdForUser));

            final NotificationConfiguration configuration = defaultConfiguration.clone();
            configure(u, ctx, configuration, participant.hasRole(ITipRole.ORGANIZER));
            participant.setConfiguration(configuration);
            retval.add(participant);
        }

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
        final List<Attendee> confirmations = event.getAttendees();
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
            final ITipRole role = (mail.equalsIgnoreCase(organizerMail)) ? ITipRole.ORGANIZER : ITipRole.ATTENDEE;

            final NotificationParticipant participant = new NotificationParticipant(role, true, mail);
            participant.setDisplayName(e.getCn());
            participant.setTimezone(null == event.getStartDate() ? TimeZone.getDefault() : event.getStartDate().getTimeZone());
            participant.setLocale(user.getLocale());
            Attendee cp = statusMap.get(mail);
            if (cp != null) {
                participant.setConfirmStatus(cp.getPartStat());
                participant.setComment(cp.getComment());
            }
            participant.setConfiguration(defaultConfiguration.clone());
            retval.add(participant);
        }
        return retval;
    }

    @Override
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
        return getPrivateCalendarFolderId(contextId, userId);
    }

    private String getPrivateCalendarFolderId(int cid, int userId) throws OXException {
        final Context ctx = this.contextService.getContext(cid);
        final OXFolderAccess acc = new OXFolderAccess(ctx);
        return String.valueOf(acc.getDefaultFolderID(userId, FolderObject.CALENDAR));
    }
}
