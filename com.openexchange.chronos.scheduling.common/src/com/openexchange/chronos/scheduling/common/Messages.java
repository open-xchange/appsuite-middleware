///*
// * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
// * @license AGPL-3.0
// *
// * This code is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Affero General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU Affero General Public License for more details.
// *
// * You should have received a copy of the GNU Affero General Public License
// * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
// *
// * Any use of the work other than as authorized under this license or copyright law is prohibited.
// *
// */
//
//package com.openexchange.chronos.scheduling.common;
//
//import com.openexchange.i18n.LocalizableStrings;
//
///**
// *
// * {@link Messages}
// *
// * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a> - Partly moved existing strings
// * @since v7.10.3
// */
//public class Messages implements LocalizableStrings {
//
//    /*
//     * ====================================================
//     * =============== CHANGE DESCRIPTIONS ================
//     * ====================================================
//     */
//    // com.openexchange.chronos.itip.Messages.HAS_CHANGED_NOTE
//    public static final String HAS_CHANGED_SUMMARY = "The appointment description has changed.";
//
//    public static final String HAS_CHANGED_LOCATION = "The appointment takes place in a new location: %1$s.";
//
//    // com.openexchange.chronos.itip.Messages.HAS_CHANGED_TITLE
//    public static final String HAS_CHANGED_SUBJECT = "The appointment has a new subject: %1$s.";
//
//    // com.openexchange.chronos.itip.Messages.HAS_CHANGED_SHOWN_AS
//    public static final String HAS_CHANGED_TRANSPARENCY = "The appointment will now be shown as: %1$s.";
//
//    /**
//     * Example: "This appointment has attachments, please see the appointment at http://ox.superhoster.invalid/index.html#m=calendar&f=bla&i=bla to retrieve them"
//     */
//    public static final String HAS_ATTACHMENTS = "This appointment has attachments. Click on the following link to view the appointment and to retrieve the attachments: %1$s.";
//
//    public static final String HAS_ADDED_ATTACHMENT = "The appointment has a new attachment: %1$s.";
//
//    public static final String HAS_REMOVED_ATTACHMENT = "The attachment %1$s was removed from the appointment.";
//
//    public static final String HAS_CHANGED_RRULE = "The appointments recurrence rule has changed to: %1$s.";
//
//    /*
//     * =============== Attendee changes ===============
//     */
//
//    // Added
//    public static final String HAS_ADDED_PARTICIPANT = "%1$s has been invited to the appointment.";
//
//    public static final String HAS_INVITED_GROUP = "The group %1$s has been invited to the appointment";
//
//    public static final String HAS_ADDED_RESOURCE = "The resource %1$s has been reserved for the appointment";
//
//    // Removed
//    public static final String HAS_REMOVED_PARTICIPANT = "%1$s has been removed from the appointment.";
//
//    public static final String HAS_REMOVED_GROUP = "The group %1$s has been removed from the appointment";
//
//    public static final String HAS_REMOVED_RESOURCE = "The resource %1$s is no longer reserved for the appointment";
//
//    // Status changes
//    public static final String HAS_CHANGED_STATE = "%1$s has %2$s the appointment.";
//
//    /**
//     * The organizer of the appointment has changed. New organizer is: %1$s
//     */
//    public static final String ORGANIZER_CHANGE = "The organizer of the appointment has changed. New organizer is: %1$s";
//
//    /*
//     * =============== DateTime changes ===============
//     */
//
//    public static final String HAS_RESCHEDULED = "The appointment was rescheduled. Original date: %1$s. New date: %2$s";
//
//    public static final String HAS_SPLIT = "The appointment series was updated, beginning at %1$s.";
//
//    public static final String HAS_RESCHEDULED_TIMEZONE = "The timezone of the appointment was changed. Original timezone: %1$s. New timezone: %2$s";
//
//    public static final String HAS_RESCHEDULED_TIMEZONE_START_DATE = "The timezone of the appointments start date was changed. Original timezone: %1$s. New timezone: %2$s";
//
//    public static final String HAS_RESCHEDULED_TIMEZONE_END_DATE = "The timezone of the appointments end date was changed. Original timezone: %1$s. New timezone: %2$s";
//
//    public static final String FULL_TIME = "The entire day";
//
//    public static final String TIMEZONE = "All times will be shown in the timezone %1$s";
//
//    /*
//     * ====================================================
//     * =============== TRANSPARENCY STATUS ================
//     * ====================================================
//     */
//    public static final String RESERVERD = "Reserved";
//
//    public static final String FREE = "Free";
//
//    public static final String WAITING = "waiting";
//
//    /*
//     * ====================================================
//     * ================== Introductions ===================
//     * ====================================================
//     */
//
//    public static final String UPDATE_INTRO = "%1$s has changed an event:";
//    public static final String UPDATE_INSTANCE_INTRO = "%1$s has changed an event of the series %2$s:";
//
//    public static final String COMMENT_INTRO = "%1$s";
//
//    public static final String CREATE_INTRO = "You have been invited to an event by %1$s:";
//
//    public static final String CREATE_EXCEPTION_INTRO = "%1$s created a recurrence exception. Original date: %2$s:";
//
//    public static final String DELETE_INTRO = "%1$s has deleted an appointment, or you have been removed as a participant:";
//    public static final String DELETE_INSTANCE_INTRO = "%1$s has deleted an event of the series %2$s, or you have been removed as a participant:";
//
//    // Unused
//    public static final String DECLINED_COUNTER_PROPOSAL = "The organizer declined your counter proposal for the appointment.";
//
//    public static final String DECLINECOUNTER_INTRO = "%1$s has declined your proposed changes for the appointment %2$s.";
//
//    public static final String REFRESH_INTRO = "%1$s would like to be brought up to date about the appointment %2$s. Please send another invitation.";
//
//    /**
//     * Examples:
//     * <p>"UserXY has accepted the invitation"
//     * <p>"UserXY has declined the invitation"
//     * <p>"UserXY has tentatively accepted the invitation"
//     */
//    public static final String STATUS_CHANGED_INTRO = "%1$s has %2$s the invitation:";
//    public static final String STATUS_CHANGED_INSTANCE_INTRO = "%1$s has %2$s the invitation to an event of the series %3$s:";
//
//    // Used if a participant changed it status to either 'Accepted', 'Denied', or 'Tentatively accepted'
//    public static final String NONE_INTRO = "%1$s sets the status to %2$s for this appointment:";
//    public static final String NONE_INSTANCE_INTRO = "%1$s sets the status to %2$s for an event of the series %3$s:";
//
//    public static final String NONE = "none";
//
//    public static final String COUNTER_ORGANIZER_INTRO = "%1$s would like you to change the appointment:";
//
//    public static final String COUNTER_PARTICIPANT_INTRO = "%1$s has asked %2$s to change the event:";
//
//    /*
//     * ============ On users behalf introductions ============
//     */
//
//    /**
//     * Example: "UserXY has accepted the invitation on your behalf"
//     */
//    public static final String ACCEPT_ON_YOUR_BEHALF_INTRO = "%1$s has %2$s the invitation on your behalf:";
//    public static final String ACCEPT_INSTANCE_ON_YOUR_BEHALF_INTRO = "%1$s has %2$s the invitation to an event of the series %3$s on your behalf:";
//
//    /**
//     * Example: "UserXY has declined the invitation on your behalf"
//     */
//    public static final String DECLINE_ON_YOUR_BEHALF_INTRO = "%1$s has %2$s the invitation on your behalf:";
//    public static final String DECLINE_INSTANCE_ON_YOUR_BEHALF_INTRO = "%1$s has %2$s the invitation to an event of the series %3$s on your behalf:";
//
//    /**
//     * Example: "UserXY has tentatively accepted the invitation on your behalf"
//     */
//    public static final String TENTATIVE_ON_YOUR_BEHALF_INTRO = "%1$s has %2$s the invitation on your behalf:";
//    public static final String TENTATIVE_INSTANCE_ON_YOUR_BEHALF_INTRO = "%1$s has %2$s the invitation to an event of the series %3$s on your behalf:";
//
//    public static final String NONE_ON_YOUR_BEHALF_INTRO =  "On your behalf, %1$s sets your status for this appointment to %2$s:";
//    public static final String NONE_INSTANCE_ON_YOUR_BEHALF_INTRO = "On your behalf, %1$s sets your status for an event of the series %3$s to %2$s:";
//
//    public static final String DELETE_ON_YOUR_BEHALF_INTRO = "%1$s has deleted an appointment on your behalf:";
//    public static final String DELETE_INSTANCE_ON_YOUR_BEHALF_INTRO = "%1$s has deleted an event of the series %2$s on your behalf:";
//
//    public static final String UPDATE_ON_YOUR_BEHALF_INTRO = "%1$s has changed an event on your behalf:";
//    public static final String UPDATE_INSTANCE_ON_YOUR_BEHALF_INTRO = "%1$s has changed an event of the series %2$s on your behalf:";
//
//    public static final String CREATE_ON_YOUR_BEHALF_INTRO = "%1$s is organizing an event on your behalf.";
//
//    /*
//     * =============== On behalf introductions ===============
//     */
//
//    /**
//     * Example: "UserXY has accepted the invitation on behalf of UserAB"
//     */
//    public static final String ACCEPT_ON_BEHALF_INTRO = "%1$s has %2$s the invitation on behalf of %3$s:";
//    public static final String ACCEPT_INSTANCE_ON_BEHALF_INTRO = "%1$s has %2$s the invitation to an event of the series %3$s on behalf of %4$s:";
//
//    /**
//     * Example: "UserXY has declined the invitation on behalf of UserAB"
//     */
//    public static final String DECLINE_ON_BEHALF_INTRO = "%1$s has %2$s the invitation on behalf of %3$s:";
//    public static final String DECLINE_INSTANCE_ON_BEHALF_INTRO = "%1$s has %2$s the invitation to an event of the series %3$s on behalf of %4$s:";
//
//    /**
//     * Example: "UserXY has tentatively accepted the invitation on behalf of UserAB"
//     */
//    public static final String TENTATIVE_ON_BEHALF_INTRO = "%1$s has %2$s the invitation on behalf of %3$s:";
//    public static final String TENTATIVE_INSTANCE_ON_BEHALF_INTRO = "%1$s has %2$s the invitation to an event of the series %3$s on behalf of %4$s:";
//
//    public static final String NONE_ON_BEHALF_INTRO = "On behalf of %3$s, %1$s sets the status for this appointment to %2$s:";
//    public static final String NONE_INSTANCE_ON_BEHALF_INTRO = "On behalf of %4$s, %1$s sets the status for an event of the series %3$s to %2$s:";
//
//    public static final String UPDATE_ON_BEHALF_INTRO = "%1$s has changed an event on behalf of %2$s:";
//    public static final String UPDATE_INSTANCE_ON_BEHALF_INTRO = "%1$s has changed an event of the series %2$s on behalf of %3$s:";
//
//    public static final String DECLINECOUNTER_ON_BEHALF_INTRO = "%1$s has declined your proposed changes on behalf of %2$s for the appointment %3$s.";
//
//    public static final String CREATE_ON_BEHALF_INTRO = "You have been invited to an event organized by %1$s on behalf of %2$s";
//
//    public static final String DELETE_ON_BEHALF_INTRO = "%1$s has deleted an appointment on behalf of %2$s, or you have been removed as a participant:";
//    public static final String DELETE_INSTANCE_ON_BEHALF_INTRO = "%1$s has deleted an appointment of the series %2$s on behalf of %3$s, or you have been removed as a participant:";
//
//    /*
//     * ====================================================
//     * ===================== SUBJECTS =====================
//     * ====================================================
//     */
//
//    public static final String SUBJECT_NEW_APPOINTMENT = "New appointment: %1$s";
//
//    public static final String SUBJECT_CHANGED_APPOINTMENT = "Appointment changed: %1$s";
//
//    public static final String SUBJECT_CANCELLED_APPOINTMENT = "Appointment canceled: %1$s";
//
//    public static final String SUBJECT_COUNTER_APPOINTMENT = "Proposed changes for appointment: %1$s";
//
//    /**
//     * Used to indicate that a participant resetted his status to 'Waiting'; neither 'Accepted, 'Denied' nor 'Tentatively accepted'
//     */
//    public static final String SUBJECT_NONE = "%1$s sets the status to 'none' for: %1$s";
//
//    /**
//     *  Example: Subject: Doe, Jane accepted the invitation: My Appointment with Jane
//     */
//    public static final String SUBJECT_STATE_CHANGED = "%1$s %2$s the invitation: %3$s";
//
//    public static final String SUBJECT_REFRESH = "Resend the invitation for: %1$s";
//
//    public static final String SUBJECT_DECLINECOUNTER = "Change not accepted for: %1$s";
//
//    /*
//     * ====================================================
//     * ====================== LABLES ======================
//     * ====================================================
//     */
//
//    public static final String LABEL_WHEN = "When:";
//
//    public static final String LINK_LABEL = "Direct link:";
//
//    public static final String LABEL_WHERE = "Where:";
//
//    public static final String LABEL_ATTENDEES = "Attendees:";
//    public static final String LABEL_PARTICIPANTS = "Participants:";
//
//    public static final String LABEL_DETAILS = "Details:";
//
//    public static final String LABEL_SHOW_AS = "Show as:";
//
//    public static final String LABEL_CREATED = "Created:";
//
//    public static final String LABEL_MODIFIED = "Modified:";
//
//    public static final String LABEL_RESOURCES = "Resources:";
//
//    /*
//     * ====================================================
//     * =================== JUSTIFICATION ==================
//     * ====================================================
//     */
//
//    public static final String ORGANIZER_JUSTIFICATION = "You have received this E-Mail because you are the organizer of this appointment.";
//
//    public static final String RESOURCE_MANAGER_JUSTIFICATION = "You have received this E-Mail because this appointment contains the resource %1$s which is managed by you.";
//
//}
