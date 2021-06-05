/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.chronos.push.impl;

import static com.openexchange.chronos.common.CalendarUtils.filter;
import static com.openexchange.chronos.common.CalendarUtils.getFolderView;
import static com.openexchange.chronos.common.CalendarUtils.isInternal;
import static com.openexchange.chronos.provider.composition.IDMangling.getUniqueFolderId;
import static com.openexchange.chronos.provider.composition.IDMangling.getUniqueFolderIds;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.service.CalendarEvent;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.DeleteResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.common.CollectionUpdate;
import com.openexchange.groupware.tools.mappings.common.ItemUpdate;
import com.openexchange.pns.DefaultPushNotification;
import com.openexchange.pns.KnownTopic;
import com.openexchange.pns.PushNotification;
import com.openexchange.pns.PushNotificationService;
import com.openexchange.pns.PushNotifications;
import com.openexchange.pns.PushNotifications.MessageDataBuilder;
import com.openexchange.pns.PushPriority;

/**
 * {@link PushCalendarHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.1
 */
public class PushCalendarHandler implements CalendarHandler {

    private final PushNotificationService pushNotificationService;

    /**
     * Initializes a new {@link PushCalendarHandler}.
     *
     * @param pushNotificationService A reference to the push notification service
     */
    public PushCalendarHandler(PushNotificationService pushNotificationService) {
        super();
        this.pushNotificationService = pushNotificationService;
    }

    @Override
    public void handle(CalendarEvent event) {
        /*
         * construct push notifications from calendar event
         */
        List<PushNotification> notifications = getNotifications(event.getContextId(), getPushToken(event), getAffectedFoldersPerUser(event), getNeedsActionPerUser(event));
        if (notifications.isEmpty()) {
            return;
        }
        /*
         * pass to notification service
         */
        try {
            pushNotificationService.handle(notifications, PushPriority.MEDIUM);
        } catch (Exception e) {
            getLogger(PushCalendarHandler.class).warn("Unexpected error delivering push notifications", e);
        }
    }

    private static List<PushNotification> getNotifications(int contextId, String pushToken, Map<Integer, List<String>> affectedFoldersPerUser, Map<Integer, List<EventID>> needsActionPerUser) {
        Set<Integer> userIds = new HashSet<Integer>(affectedFoldersPerUser.keySet());
        userIds.addAll(needsActionPerUser.keySet());
        List<PushNotification> notifications = new ArrayList<PushNotification>(userIds.size());
        for (Integer userId : userIds) {
            Map<String, Object> messageData = getMessageData(affectedFoldersPerUser.get(userId), needsActionPerUser.get(userId));
            notifications.add(getNotification(contextId, i(userId), pushToken, messageData));
        }
        return notifications;
    }

    private static PushNotification getNotification(int contextId, int userId, String pushToken, Map<String, Object> messageData) {
        return DefaultPushNotification.builder()
            .contextId(contextId)
            .userId(userId)
            .sourceToken(pushToken)
            .topic(KnownTopic.CALENDAR_UPDATES.getName())
            .messageData(messageData)
        .build();
    }

    private static String getPushToken(CalendarEvent event) {
        return null != event.getCalendarParameters() ? event.getCalendarParameters().get(CalendarParameters.PARAMETER_PUSH_TOKEN, String.class) : null;
    }

    /**
     * Gets the (unique) identifiers of all affected folders mapped to the corresponding user identifier.
     *
     * @param event The event to extract the affected folders data for
     * @return The (unique) identifiers of the affected folders for each user
     */
    private static Map<Integer, List<String>> getAffectedFoldersPerUser(CalendarEvent event) {
        Map<Integer, List<String>> affectedFoldersPerUser = event.getAffectedFoldersPerUser();
        if (null == affectedFoldersPerUser || affectedFoldersPerUser.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Integer, List<String>> uniqueAffectedFoldersPerUser = new HashMap<Integer, List<String>>(affectedFoldersPerUser.size());
        for (Entry<Integer, List<String>> entry : affectedFoldersPerUser.entrySet()) {
            if (0 < event.getCreations().size() || 0 < event.getDeletions().size() || containsSignificantChanges(event.getUpdates(), i(entry.getKey()), entry.getValue())) {
                uniqueAffectedFoldersPerUser.put(entry.getKey(), getUniqueFolderIds(event.getAccountId(), entry.getValue()));
            }
        }
        return uniqueAffectedFoldersPerUser;
    }

    /**
     * Gets a value indicating whether at least one of the supplied update results denotes <i>significant</i> changes, i.e. changes that
     * would directly be visible in the client. Currently, this is only the case whenever the event's sequence number is bumped.
     *
     * @param updates The update results as indicated by the calendar event
     * @param userId The user to indicate the changes for
     * @param folderIds The affected folder identifiers visible to the user to indicate the changes for
     * @return <code>true</code> if there a re significant changes, <code>false</code>, otherwise
     */
    private static boolean containsSignificantChanges(List<UpdateResult> updates, int userId, List<String> folderIds) {
        for (UpdateResult update : updates) {
            if (CalendarUtils.isSignificantChange(update, userId, folderIds)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the (unique) full identifiers of all events where the participation status of the mapped user is newly set to
     * {@link ParticipationStatus#NEEDS_ACTION}.
     *
     * @param event The event to extract the needs action data for
     * @return The (unique) full identifiers of all events needing action per user identifier
     */
    private static Map<Integer, List<EventID>> getNeedsActionPerUser(CalendarEvent event) {
        Map<Integer, List<EventID>> needsActionPerUser = new HashMap<Integer, List<EventID>>();
        for (CreateResult creation : event.getCreations()) {
            Event createdEvent = creation.getCreatedEvent();
            /*
             * collect all internal user attendees with participation status 'needs action' for new events
             */
            for (Attendee userAttendee : filter(createdEvent.getAttendees(), Boolean.TRUE, CalendarUserType.INDIVIDUAL)) {
                if (ParticipationStatus.NEEDS_ACTION.matches(userAttendee.getPartStat())) {
                    EventID eventID = getUniqueEventID(createdEvent, event.getAccountId(), userAttendee.getEntity());
                    com.openexchange.tools.arrays.Collections.put(needsActionPerUser, I(userAttendee.getEntity()), eventID);
                }
            }
        }
        for (UpdateResult update : event.getUpdates()) {
            CollectionUpdate<Attendee, AttendeeField> attendeeUpdates = update.getAttendeeUpdates();
            /*
             * collect all newly added internal user attendees with participation status 'needs action' for updated events
             */
            for (Attendee userAttendee : filter(attendeeUpdates.getAddedItems(), Boolean.TRUE, CalendarUserType.INDIVIDUAL)) {
                if (ParticipationStatus.NEEDS_ACTION.matches(userAttendee.getPartStat())) {
                    EventID eventID = getUniqueEventID(update.getUpdate(), event.getAccountId(), userAttendee.getEntity());
                    com.openexchange.tools.arrays.Collections.put(needsActionPerUser, I(userAttendee.getEntity()), eventID);
                }
            }
            /*
             * collect all updated internal user attendees whose participation status was (re-) set to 'needs action' for updated events
             */
            for (ItemUpdate<Attendee, AttendeeField> attendeeUpdate : attendeeUpdates.getUpdatedItems()) {
                if (isInternal(attendeeUpdate.getUpdate(), CalendarUserType.INDIVIDUAL) &&
                    attendeeUpdate.getUpdatedFields().contains(AttendeeField.PARTSTAT) &&
                    ParticipationStatus.NEEDS_ACTION.matches(attendeeUpdate.getUpdate().getPartStat())) {
                    EventID eventID = getUniqueEventID(update.getUpdate(), event.getAccountId(), attendeeUpdate.getUpdate().getEntity());
                    com.openexchange.tools.arrays.Collections.put(needsActionPerUser, I(attendeeUpdate.getUpdate().getEntity()), eventID);
                }
            }
        }
        for (DeleteResult deletion : event.getDeletions()) {
            Event deletedEvent = deletion.getOriginal();
            /*
             * collect all internal user attendees whose participation status was 'needs action' for deleted events
             */
            for (Attendee userAttendee : filter(deletedEvent.getAttendees(), Boolean.TRUE, CalendarUserType.INDIVIDUAL)) {
                if (ParticipationStatus.NEEDS_ACTION.matches(userAttendee.getPartStat())) {
                    EventID eventID = getUniqueEventID(deletedEvent, event.getAccountId(), userAttendee.getEntity());
                    com.openexchange.tools.arrays.Collections.put(needsActionPerUser, I(userAttendee.getEntity()), eventID);
                }
            }
        }
        return needsActionPerUser;
    }

    private static Map<String, Object> getMessageData(List<String> folders, List<EventID> needsActions) {
        MessageDataBuilder messageDataBuilder = PushNotifications.messageDataBilder();
        if (null != needsActions) {
            try {
                messageDataBuilder.put("needsAction", serializeEventIDs(needsActions));
            } catch (JSONException e) {
                getLogger(PushCalendarHandler.class).warn("Unexpected error serializing event ids", e);
            }
        }
        if (null != folders) {
            messageDataBuilder.put("folders", folders);
        }
        return messageDataBuilder.build();
    }

    private static JSONArray serializeEventIDs(List<EventID> eventIDs) throws JSONException {
        if (null == eventIDs) {
            return null;
        }
        JSONArray jsonArray = new JSONArray(eventIDs.size());
        for (EventID eventID : eventIDs) {
            jsonArray.put(new JSONObject(3).putOpt("folderId", eventID.getFolderID()).putOpt("id", eventID.getObjectID()).putOpt("recurrenceId", eventID.getRecurrenceID()));
        }
        return jsonArray;
    }

    private static EventID getUniqueEventID(Event event, int accountId, int userId) {
        String relativeFolderId = event.getFolderId();
        try {
            relativeFolderId = getFolderView(event, userId);
        } catch (OXException e) {
            getLogger(PushCalendarHandler.class).debug("Error getting folder view on event, falling back to static parent folder", e);
        }
        return new EventID(getUniqueFolderId(accountId, relativeFolderId), event.getId(), event.getRecurrenceId());
    }

}
