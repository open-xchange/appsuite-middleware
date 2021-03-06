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

package com.openexchange.groupware.calendar;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link OXCalendarExceptionMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class OXCalendarExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link OXCalendarExceptionMessage}.
     */
    private OXCalendarExceptionMessage() {
        super();
    }

    /**
     * Appointments must have at least one participant.
     */
    public static final String UPDATE_WITHOUT_PARTICIPANTS_DISPLAY = "Appointments must have at least one participant.";

    /**
     * You do not have the appropriate permissions to read appointments in that folder.
     */
    public static final String NO_PERMISSION_MSG = "You do not have the appropriate permissions to read appointments in that folder.";

    /**
     * End date must not be before start date.
     */
    public static final String END_DATE_BEFORE_START_DATE_DISPLAY = "End date must not be before start date.";

    /**
     * This label (%d) is not supported.
     */
    public static final String UNSUPPORTED_LABEL_DISPLAY = "This label (%d) is not supported.";

    /**
     * Private flag is only allowed inside of a private folder.
     */
    public static final String PRIVATE_FLAG_IN_PRIVATE_FOLDER_MSG = "Private flag is only allowed inside of a private folder.";

    /**
     * Appointments marked as 'Private' can only be scheduled for the respective user (or owner of the calendar). Please remove additional
     * participants or remove the \"Private\" mark.
     */
    public static final String PRIVATE_FLAG_AND_PARTICIPANTS_MSG = "Appointments marked as 'Private' can only be scheduled for the respective user (or owner of the calendar). Please remove additional participants or remove the \"Private\" mark.";

    /**
     * Unsupported \"shown as\" value %d.
     */
    public static final String UNSUPPORTED_SHOWN_AS_MSG = "Unsupported \"shown as\" value %d.";

    /**
     * Required value \"Start Date\" was not supplied.
     */
    public static final String MANDATORY_FIELD_START_DATE_MSG = "Required  value \"Start Date\" was not supplied.";

    /**
     * Required value \"End Date\" was not supplied.
     */
    public static final String MANDATORY_FIELD_END_DATE_MSG = "Required value \"End Date\" was not supplied.";

    /**
     * Required value \"Title\" was not supplied.
     */
    public static final String MANDATORY_FIELD_TITLE_MSG = "Required value \"Title\" was not supplied.";

    /**
     * Move not supported. You cannot move an appointment into that folder.
     */
    public static final String MOVE_NOT_SUPPORTED_MSG = "Move not supported. You cannot move an appointment into that folder.";

    /**
     * Move not allowed from shared folders.
     */
    public static final String SHARED_FOLDER_MOVE_NOT_SUPPORTED_MSG = "Move not allowed from shared folders.";

    /**
     * No permissions for attachments granted.
     */
    public static final String NO_PERMISSIONS_TO_ATTACH_DETACH_DISPLAY = "No permissions for attachments granted.";

    /**
     * Insufficient read rights for this folder.
     */
    public static final String NO_PERMISSIONS_TO_READ_MSG = "Insufficient read rights for this folder.";

    /**
     * Folder is not of type Calendar.
     */
    public static final String NON_CALENDAR_FOLDER_MSG = "Folder is not of type Calendar.";

    /**
     * Moving an appointment with private flag to a public folder is not allowed.
     */
    public static final String PRIVATE_MOVE_TO_PUBLIC_MSG = "Moving an appointment with private flag to a public folder is not allowed.";

    /**
     * You do not have the appropriate permissions to modify this object.
     */
    public static final String LOAD_PERMISSION_EXCEPTION_1_MSG = "You do not have the appropriate permissions to modify this object.";

    /**
     * You do not have the appropriate permissions to move this object.
     */
    public static final String LOAD_PERMISSION_EXCEPTION_4_MSG = "You do not have the appropriate permissions to move this object.";

    /**
     * You do not have the appropriate permissions to read this object.
     */
    public static final String LOAD_PERMISSION_EXCEPTION_5_DISPLAY = "You do not have the appropriate permissions to read this object.";

    /**
     * You do not have the appropriate permissions to create an object.
     */
    public static final String LOAD_PERMISSION_EXCEPTION_6_MSG = "You do not have the appropriate permissions to create an object.";

    /**
     * You are trying to create a new recurring appointment from an exception. This is not possible.
     */
    public static final String RECURRING_ALREADY_EXCEPTION_MSG = "You are trying to create a new recurring appointment from an exception. This is not possible.";

    /**
     * Moving an instance of a recurring appointment into another folder is not allowed.
     */
    public static final String RECURRING_EXCEPTION_MOVE_EXCEPTION_MSG = "Moving an instance of a recurring appointment into another folder is not allowed.";

    /**
     * Moving an appointment with private flag to a shared folder is not allowed.
     */
    public static final String MOVE_TO_SHARED_FOLDER_NOT_SUPPORTED_DISPLAY = "Moving an appointment with private flag to a shared folder is not allowed.";

    /**
     * You can not use different private flags for one element of a recurring appointment.
     */
    public static final String RECURRING_EXCEPTION_PRIVATE_FLAG_MSG = "You can not use different private flags for one element of a recurring appointment.";

    /**
     * You can not use the private flag in a non private folder.
     */
    public static final String PIVATE_FLAG_ONLY_IN_PRIVATE_FOLDER_MSG = "You can not use the private flag in a non private folder.";

    /**
     * Invalid characters (%2$s) in field %1$s.
     */
    public static final String INVALID_CHARACTER_DISPLAY = "Invalid characters (%2$s) in field %1$s.";

    /**
     * Series end is before start date.
     */
    public static final String UNTIL_BEFORE_START_DATE_MSG = "Series end is before start date.";

    /**
     * An external participant with email address %1$s is already contained. Please remove duplicate participant and retry.
     */
    public static final String DUPLICATE_EXTERNAL_PARTICIPANT_MSG = "An external participant with the E-Mail address %1$s is already included. Please remove participant duplicate and retry.";

    /**
     * Moving a recurring appointment to another folder is not supported.
     */
    public static final String RECURRING_FOLDER_MOVE_MSG = "Moving a recurring appointment to another folder is not supported.";

    /**
     * Cannot create that appointment. An appointment having the same unique identifier already exists.
     */
    public static final String UID_ALREDY_EXISTS_MSG = "Cannot create that appointment. An appointment having the same unique identifier already exists.";

    /**
     * Cannot create that task. A task having the same unique identifier already exists.
     */
    public static final String TASK_UID_ALREDY_EXISTS_MSG = "Cannot create that task. A task having the same unique identifier already exists.";

    //Sequence number is outdated.
    public static final String OUTDATED_SEQUENCE = "Sequence number is outdated.";

    /**
     * The calendar is currently under maintenance. Please try again later.
     */
    public static final String CALENDAR_MAINTENANCE_MSG = "The calendar is currently under maintenance. Please try again later.";

}
