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

package com.openexchange.chronos.service;

import java.util.List;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Available;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.exception.OXException;

/**
 * {@link CalendarConfig}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface CalendarConfig {

    /**
     * Gets the identifier of a specific user's default personal calendar folder.
     *
     * @param userId The identifier of the user to retrieve the default calendar identifier for
     * @return The default calendar folder identifier
     */
    String getDefaultFolderId(int userId) throws OXException;

    /**
     * Gets the initial participation status to use for new events in a specific folder.
     *
     * @param userId The identifier of the user to get the participation status for
     * @param inPublicFolder <code>true</code> if the event is located in a <i>public</i> folder, <code>false</code>, otherwise
     * @return The initial participation status, or {@link ParticipationStatus#NEEDS_ACTION} if not defined
     */
    ParticipationStatus getInitialPartStat(int userId, boolean inPublicFolder);

    /**
     * Gets the default alarms to be applied to events whose start-date is of type <i>date</i> from the underlying user configuration.
     *
     * @param userId The identifier of the user to get the default alarm for
     * @return The default alarms, or <code>null</code> if not defined
     */
    List<Alarm> getDefaultAlarmDate(int userId) throws OXException;

    /**
     * Gets the default alarms to be applied to events whose start-date is of type <i>date-time</i> from the underlying user configuration.
     *
     * @param userId The identifier of the user to get the default alarm for
     * @return The default alarms, or <code>null</code> if not defined
     */
    List<Alarm> getDefaultAlarmDateTime(int userId) throws OXException;

    /**
     * Gets the defined availability (in form of one or more available definitions) from the underlying user configuration.
     *
     * @param userId The identifier of the user to get the availability for
     * @return The availability, or <code>null</code> if not defined
     */
    Available[] getAvailability(int userId) throws OXException;

    /**
     * Gets a value indicating whether notifications for newly created / scheduled events are enabled or not.
     * <p/>
     * This setting is either used for internal user attendees when the operation is performed by the organizer, or for the organizer or
     * calendar owner in case the operation is performed by another user on his behalf.
     *
     * @param userId The identifier of the user to get the notification preference for
     * @return <code>true</code> of notifications are enabled, <code>false</code>, otherwise
     * @see com.openexchange.mail.usersetting.UserSettingMail#isNotifyAppointments()
     */
    boolean isNotifyOnCreate(int userId);

    /**
     * Gets a value indicating whether notifications for updated / re-scheduled events are enabled or not.
     * <p/>
     * This setting is either used for internal user attendees when the operation is performed by the organizer, or for the organizer or
     * calendar owner in case the operation is performed by another user on his behalf.
     * <p/>
     * This setting is <b>not</b> used for changes towards a user's participation status (reply operations).
     *
     * @param userId The identifier of the user to get the notification preference for
     * @return <code>true</code> of notifications are enabled, <code>false</code>, otherwise
     * @see com.openexchange.mail.usersetting.UserSettingMail#isNotifyAppointments()
     */
    boolean isNotifyOnUpdate(int userId);

    /**
     * Gets a value indicating whether notifications for deleted (cancelled) events are enabled or not.
     * <p/>
     * This setting is either used for internal user attendees when the operation is performed by the organizer, or for the organizer or
     * calendar owner in case the operation is performed by another user on his behalf.
     *
     * @param userId The identifier of the user to get the notification preference for
     * @return <code>true</code> of notifications are enabled, <code>false</code>, otherwise
     * @see com.openexchange.mail.usersetting.UserSettingMail#isNotifyAppointments()
     */
    boolean isNotifyOnDelete(int userId);

    /**
     * Gets a value indicating whether notifications for replies of attendees are enabled or not.
     * <p/>
     * This setting used if the user is the event organizer and the operation is performed by an invited attendee.
     *
     * @param userId The identifier of the user to get the notification preference for
     * @return <code>true</code> of notifications are enabled, <code>false</code>, otherwise
     * @see com.openexchange.mail.usersetting.UserSettingMail#isNotifyAppointmentsConfirmOwner()
     */
    boolean isNotifyOnReply(int userId);

    /**
     * Gets a value indicating whether notifications for replies of attendees are enabled or not.
     * <p/>
     * This setting used if the user is an attendee and the operation is performed by another invited attendee.
     *
     * @param userId The identifier of the user to get the notification preference for
     * @return <code>true</code> of notifications are enabled, <code>false</code>, otherwise
     * @see com.openexchange.mail.usersetting.UserSettingMail#isNotifyAppointmentsConfirmParticipant()
     */
    boolean isNotifyOnReplyAsAttendee(int userId);

    /**
     * Gets a value indicating the preferred message format of notification mails.
     * <p>
     * The returned <code>int</code> value is wither <code>1</code> (text only), <code>2</code> (HTML only), or <code>3</code> (both).
     *
     * @param userId The identifier of the user to get the notification preference for
     * @return The desired message format
     * @see com.openexchange.mail.usersetting.UserSettingMail#getMsgFormat()
     */
    int getMsgFormat(int userId);

    /**
     * Gets a value indicating whether newly added group attendees should be resolved to their individual members, without preserving the
     * group reference, or not.
     *
     * @return <code>true</code> if group attendees should be resolved, <code>false</code>, otherwise
     */
    boolean isResolveGroupAttendees();

    /**
     * Gets a value indicating whether notifications to <i>internal</i> resource attendees are are enabled or not.
     * <p/>
     * <i>External</i> attendees of calendar user type <code>RESOURCE</code> are always considered for scheduling messages, independently
     * of this setting.
     *
     * @return <code>true</code> of notifications are enabled, <code>false</code>, otherwise
     */
    boolean isNotifyResourceAttendees();

    /**
     * Gets the configured minimum search pattern length.
     *
     * @return The minimum search pattern length, or <code>0</code> for no limitation
     */
    int getMinimumSearchPatternLength() throws OXException;

    /**
     * Gets the configured maximum number of conflicts between two recurring event series.
     *
     * @return The maximum conflicts per recurrence
     */
    int getMaxConflictsPerRecurrence();

    /**
     * Gets the configured maximum number of attendees to indicate per conflict.
     *
     * @return The the maximum number of attendees to indicate per conflict
     */
    int getMaxAttendeesPerConflict();

    /**
     * Gets the overall maximum number of conflicts to return.
     *
     * @return The the maximum number of conflicts to return
     */
    int getMaxConflicts();

    /**
     * Gets the maximum number of considered occurrences when checking conflicts for an event series. A value of or smaller than 0
     * disables the limit.
     *
     * @return The maximum number of considered occurrences when checking conflicts for an event series
     */
    int getMaxOccurrencesForConflicts();

    /**
     * Gets a value indicating for how many years into the future occurrences of an event series are considered when checking conflicts.
     * A value of or smaller than 0 disables the limit.
     *
     * @return How many years into the future occurrences of an event series are considered when checking conflicts
     */
    int getMaxSeriesUntilForConflicts();

    /**
     * Gets a value indicating whether the checks of (external) attendee URIs are generally disabled or not.
     *
     * @return <code>true</code> if the URI checks are disabled, <code>false</code>, otherwise
     */
    boolean isSkipExternalAttendeeURIChecks();

    /**
     * Gets a value indicating whether attendee changes to events located in <i>private</i> or <i>shared</i> folders should be restricted
     * according to <a href="https://tools.ietf.org/html/rfc6638#section-3.2.2.1">RFC 6638, section 3.2.2.1</a> or not, which effectively
     * restricts any changes to the calendar scheduling resource to be performed by the organizer only.
     *
     * @return <code>true</code> if allowed attendee changes should be restricted, <code>false</code>, otherwise
     */
    boolean isRestrictAllowedAttendeeChanges();

    /**
     * Gets a value indicating whether it is allowed to change the organizer of an event or not.
     *
     * @return <code>true</code> if organizer changes are allowed, <code>false</code> otherwise
     */
    boolean isOrganizerChangeAllowed();

    /**
     * Gets a value indicating whether organizer changes of the participation status property of other attendees are allowed beyond the
     * recommended restrictions mentioned in <a href="https://tools.ietf.org/html/rfc6638#section-3.2.1">RFC 6638, section 3.2.1</a>.
     * Otherwise, only transitions to <code>NEEDS-ACTION</code> are possible.
     *
     * @return <code>true</code> if changing <code>PARTSTAT</code>s of other attendees is allowed, <code>false</code>, otherwise
     */
    boolean isAllowOrganizerPartStatChanges();

    /**
     * Attendee data from copies of a group-scheduled event organized by an external calendar user is dynamically looked up in calendar
     * folders of other internal users. This flag indicates whether the lookup is only attempted for attendees that share the same mail domain
     * as the current calendar user, or if the lookup is always performed.
     *
     * @return <code>true</code> if lookup should only be done if the mail domain matches, <code>false</code>, otherwise
     */
    boolean isLookupPeerAttendeesForSameMailDomainOnly();

}
