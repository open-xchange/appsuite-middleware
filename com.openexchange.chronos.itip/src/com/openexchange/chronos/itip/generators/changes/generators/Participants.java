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

package com.openexchange.chronos.itip.generators.changes.generators;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.chronos.itip.generators.ArgumentType;
import com.openexchange.chronos.itip.generators.Sentence;
import com.openexchange.chronos.itip.generators.changes.ChangeDescriptionGenerator;
import com.openexchange.chronos.itip.osgi.Services;
import com.openexchange.chronos.itip.tools.ITipEventUpdate;
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.service.ItemUpdate;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceService;
import com.openexchange.user.UserService;

/**
 * {@link Participants}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class Participants implements ChangeDescriptionGenerator {

    private static final EventField[] FIELDS = new EventField[] { EventField.ATTENDEES };

    protected static enum ChangeType {
        ADD, REMOVE, ACCEPT, DECLINE, TENTATIVE, NEEDS_ACTION
    }

    private static final Map<ChangeType, String> PARTICIPANT_MESSAGE_MAP = new HashMap<ChangeType, String>(6, 0.9f);

    static {
        PARTICIPANT_MESSAGE_MAP.put(ChangeType.ADD, Messages.HAS_ADDED_PARTICIPANT);

        PARTICIPANT_MESSAGE_MAP.put(ChangeType.REMOVE, Messages.HAS_REMOVED_PARTICIPANT);

        PARTICIPANT_MESSAGE_MAP.put(ChangeType.ACCEPT, Messages.HAS_CHANGED_STATE);
        PARTICIPANT_MESSAGE_MAP.put(ChangeType.DECLINE, Messages.HAS_CHANGED_STATE);
        PARTICIPANT_MESSAGE_MAP.put(ChangeType.TENTATIVE, Messages.HAS_CHANGED_STATE);

    }

    private static final Map<ChangeType, String> GROUP_MESSAGE_MAP = new HashMap<ChangeType, String>(3, 0.9f);

    static {
        GROUP_MESSAGE_MAP.put(ChangeType.ADD, Messages.HAS_INVITED_GROUP);
        GROUP_MESSAGE_MAP.put(ChangeType.REMOVE, Messages.HAS_REMOVED_GROUP);
    }

    private static final Map<ChangeType, String> RESOURCE_MESSAGE_MAP = new HashMap<ChangeType, String>(3, 0.9f);

    static {
        RESOURCE_MESSAGE_MAP.put(ChangeType.ADD, Messages.HAS_ADDED_RESOURCE);
        RESOURCE_MESSAGE_MAP.put(ChangeType.REMOVE, Messages.HAS_REMOVED_RESOURCE);
    }

    public Participants() {
        super();
    }

    @Override
    public List<Sentence> getDescriptions(Context ctx, Event original, Event updated, ITipEventUpdate diff, Locale locale, TimeZone timezone) throws OXException {
        Set<Integer> attendeeIds = new HashSet<>();
        Set<Integer> groupAttendeeIds = new HashSet<>();
        Set<Integer> resourceAttendeeIds = new HashSet<>();

        Map<Integer, ChangeType> attendeeChange = new HashMap<>();
        Map<Integer, ChangeType> groupChange = new HashMap<>();
        Map<Integer, ChangeType> resourceChange = new HashMap<>();
        Map<String, ChangeType> externalChange = new HashMap<>();

        if (diff != null && diff.getUpdatedFields() != null && diff.getUpdatedFields().contains(EventField.ATTENDEES) && diff.getAttendeeUpdates() != null) {
            CollectionUpdate<Attendee, AttendeeField> attendeeUpdates = diff.getAttendeeUpdates();
            investigateSetOperation(attendeeUpdates, attendeeIds, groupAttendeeIds, resourceAttendeeIds, attendeeChange, resourceChange, groupChange, externalChange, ChangeType.ADD, attendeeUpdates.getAddedItems());
            investigateSetOperation(attendeeUpdates, attendeeIds, groupAttendeeIds, resourceAttendeeIds, attendeeChange, resourceChange, groupChange, externalChange, ChangeType.REMOVE, attendeeUpdates.getRemovedItems());
            investigateChanges(attendeeUpdates, attendeeIds, attendeeChange, externalChange);
        }

        List<Sentence> changes = new ArrayList<Sentence>();

        UserService userService = Services.getService(UserService.class, true);
        for (Integer attendeeId : attendeeIds) {
            User u = userService.getUser(i(attendeeId), ctx);
            ChangeType changeType = attendeeChange.get(attendeeId);
            writeChange(changes, u.getDisplayName(), changeType);
        }

        List<String> externalMails = new ArrayList<String>(externalChange.keySet());
        Collections.sort(externalMails);
        for (String mail : externalMails) {
            ChangeType changeType = externalChange.get(mail);
            writeChange(changes, mail, changeType);
        }

        GroupService groupService = Services.getService(GroupService.class, true);
        for (Entry<Integer, ChangeType> change : groupChange.entrySet()) {
            Group group = groupService.getGroup(ctx, i(change.getKey()));
            ChangeType changeType = change.getValue();
            if (changeType == null) {
                continue;
            }
            switch (changeType) {
                case ADD:
                case REMOVE:
                    changes.add(new Sentence(GROUP_MESSAGE_MAP.get(changeType)).add(group.getDisplayName(), ArgumentType.PARTICIPANT));
                    break;
                default: // Skip
            }
        }

        ResourceService resources = Services.getService(ResourceService.class, true);
        for (Entry<Integer, ChangeType> entry : resourceChange.entrySet()) {
            Resource resource = resources.getResource(i(entry.getKey()), ctx);
            ChangeType changeType = entry.getValue();
            if (changeType == null) {
                continue;
            }
            switch (changeType) {
                case ADD:
                case REMOVE:
                    changes.add(new Sentence(RESOURCE_MESSAGE_MAP.get(changeType)).add(resource.getDisplayName(), ArgumentType.PARTICIPANT));
                    break;
                default: // Skip
            }
        }

        return changes;
    }

    private void writeChange(List<Sentence> changes, String name, ChangeType changeType) {
        if (null == changeType) {
            return;
        }
        Sentence s = new Sentence(PARTICIPANT_MESSAGE_MAP.get(changeType)).add(name, ArgumentType.PARTICIPANT);
        switch (changeType) {
            case ADD: /* fall-through */
            case REMOVE:
                break;
            case ACCEPT:
                s.addStatus(ParticipationStatus.ACCEPTED);
                break;
            case DECLINE:
                s.addStatus(ParticipationStatus.DECLINED);
                break;
            case TENTATIVE:
                s.addStatus(ParticipationStatus.TENTATIVE);
                break;
            default:
                s = null;
                break;
        }
        if (null != s) {
            changes.add(s);
        }
    }

    private void investigateChanges(CollectionUpdate<Attendee, AttendeeField> difference, Set<Integer> userIds, Map<Integer, ChangeType> userChange, Map<String, ChangeType> externalChange) {

        List<? extends ItemUpdate<Attendee, AttendeeField>> updatedItems = difference.getUpdatedItems();
        for (ItemUpdate<Attendee, AttendeeField> itemUpdate : updatedItems) {
            ChangeType changeType = null;
            if (itemUpdate.getUpdatedFields().contains(AttendeeField.PARTSTAT)) {
                changeType = getChangeType(itemUpdate.getUpdate().getPartStat());
            }
            ParticipationStatus partStat = itemUpdate.getOriginal().getPartStat();
            if (partStat == null || !getChangeType(partStat).equals(changeType)) {
                Attendee original = itemUpdate.getOriginal();
                if (CalendarUtils.isInternal(original)) {
                    userIds.add(I(original.getEntity()));
                    userChange.put(I(original.getEntity()), changeType);
                } else {
                    externalChange.put(original.getEMail(), changeType);
                }
            }
        }

        if (difference.getAddedItems() != null && !difference.getAddedItems().isEmpty()) {
            for (Attendee added : difference.getAddedItems()) {
                if (CalendarUserType.INDIVIDUAL.equals(added.getCuType())) {
                    if (CalendarUtils.isInternal(added)) {
                        userIds.add(I(added.getEntity()));
                        userChange.put(I(added.getEntity()), ChangeType.ADD);
                    } else {
                        externalChange.put(added.getEMail(), ChangeType.ADD);
                    }
                }
            }
        }

        if (difference.getRemovedItems() != null && !difference.getRemovedItems().isEmpty()) {
            for (Attendee removed : difference.getRemovedItems()) {
                if (CalendarUserType.INDIVIDUAL.equals(removed.getCuType())) {
                    if (CalendarUtils.isInternal(removed)) {
                        userIds.add(I(removed.getEntity()));
                        userChange.put(I(removed.getEntity()), ChangeType.REMOVE);
                    } else {
                        externalChange.put(removed.getEMail(), ChangeType.REMOVE);
                    }
                }
            }
        }
    }

    /**
     * Get the {@link ChangeType} to the corresponding {@link ParticipationStatus}
     *
     * @param newPartStat The status
     * @return The {@link ChangeType}
     */
    private ChangeType getChangeType(ParticipationStatus newPartStat) {
        if (ParticipationStatus.DECLINED.equals(newPartStat)) {
            return ChangeType.DECLINE;
        } else if (ParticipationStatus.TENTATIVE.equals(newPartStat)) {
            return ChangeType.TENTATIVE;
        } else if (ParticipationStatus.NEEDS_ACTION.equals(newPartStat)) {
            return ChangeType.NEEDS_ACTION;
        }
        return ChangeType.ACCEPT;
    }

    private void investigateSetOperation(CollectionUpdate<Attendee, AttendeeField> difference, Set<Integer> userIds, Set<Integer> groupIds, Set<Integer> resourceIds, Map<Integer, ChangeType> userChange, Map<Integer, ChangeType> resourceChange, Map<Integer, ChangeType> groupChange, Map<String, ChangeType> externalChange, ChangeType changeType, List<Attendee> list) {
        for (Attendee added : list) {
            if (null != added) {
                CalendarUserType cuType = added.getCuType();
                if (null != cuType) {
                    if (cuType.equals(CalendarUserType.INDIVIDUAL)) {
                        if (CalendarUtils.isInternal(added)) {
                            userIds.add(I(added.getEntity()));
                            userChange.put(I(added.getEntity()), changeType);
                        } else {
                            externalChange.put(added.getEMail(), changeType);
                        }
                    } else if (cuType.equals(CalendarUserType.RESOURCE)) {
                        resourceIds.add(I(added.getEntity()));
                        resourceChange.put(I(added.getEntity()), changeType);
                    } else if (cuType.equals(CalendarUserType.GROUP)) {
                        groupIds.add(I(added.getEntity()));
                        groupChange.put(I(added.getEntity()), changeType);
                    }
                }
            }
        }
    }

    @Override
    public EventField[] getFields() {
        return FIELDS;
    }

}
