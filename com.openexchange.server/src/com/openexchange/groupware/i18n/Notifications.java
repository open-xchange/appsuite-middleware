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

package com.openexchange.groupware.i18n;

import com.openexchange.i18n.LocalizableStrings;

public class Notifications implements LocalizableStrings {

    /*-
     * CREATE
     */

    public static final String APPOINTMENT_CREATE_MAIL =
            "A new appointment was created by [created_by].\n" +
            "You can check this appointment in your calendar:\n" +
            "[link]\n" +
            "\n" +
            "Appointment\n" +
            "===========\n" +
            "Created by: [created_by]\n" +
            "Created at: [creation_datetime]\n" +
            "[title]\n" +
            "[location]" +
            "[folder_name]\n" +
            "\n" +
            "[start]\n" +
            "[end]\n" +
            "[series]" +
            "[delete_exceptions]" +
            "[change_exceptions]" +
            "\n" +
            "[description]" +
            "\n" +
            "Participants\n" +
            "============\n" +
            "[participants]" +
            "\n" +
            "\n" +
            "Resources\n" +
            "=========\n" +
            "[resources]" +
            "\n" +
            "\n" +
            "========================================== ";

    public static final String APPOINTMENT_CREATE_MAIL_EXT =
        "A new appointment was created by [created_by].\n" +
        "\n" +
        "Appointment\n" +
        "===========\n" +
        "Created by: [created_by]\n" +
        "Created at: [creation_datetime]\n" +
        "[title]\n" +
        "[location]" +
        "\n" +
        "[start]\n" +
        "[end]\n" +
        "[series]" +
        "[delete_exceptions]" +
        "[change_exceptions]" +
        "\n" +
        "[description]" +
        "\n" +
        "Participants\n" +
        "============\n" +
        "[participants]" +
        "\n" +
        "\n" +
        "Resources\n" +
        "=========\n" +
        "[resources]" +
        "\n" +
        "\n" +
        "========================================== ";

    public static final String APPOINTMENT_CREATE_MAIL_ON_BEHALF =
        "A new appointment was created by [created_by] on behalf of [behalf_of].\n" +
        "You can check this appointment in your calendar:\n" +
        "[link]\n" +
        "\n" +
        "Appointment\n" +
        "===========\n" +
        "Created by: [created_by]\n" +
        "Created at: [creation_datetime]\n" +
        "[title]\n" +
        "[location]" +
        "[folder_name]\n" +
        "\n" +
        "[start]\n" +
        "[end]\n" +
        "[series]" +
        "[delete_exceptions]" +
        "[change_exceptions]" +
        "\n" +
        "[description]" +
        "\n" +
        "Participants\n" +
        "============\n" +
        "[participants]" +
        "\n" +
        "\n" +
        "Resources\n" +
        "=========\n" +
        "[resources]" +
        "\n" +
        "\n" +
        "========================================== ";

    public static final String APPOINTMENT_CREATE_MAIL_ON_BEHALF_EXT =
        "A new appointment was created by [created_by] on behalf of [behalf_of].\n" +
        "\n" +
        "Appointment\n" +
        "===========\n" +
        "Created by: [created_by]\n" +
        "Created at: [creation_datetime]\n" +
        "[title]\n" +
        "[location]" +
        "\n" +
        "[start]\n" +
        "[end]\n" +
        "[series]" +
        "[delete_exceptions]" +
        "[change_exceptions]" +
        "\n" +
        "[description]" +
        "\n" +
        "Participants\n" +
        "============\n" +
        "[participants]" +
        "\n" +
        "\n" +
        "Resources\n" +
        "=========\n" +
        "[resources]" +
        "\n" +
        "\n" +
        "========================================== ";
    /**
     * Notification Template for user which has no access to this appointment.
     * We use the same as for external participants, because this has no link to the appointment.
     */
    public static final String APPOINTMENT_CREATE_MAIL_NO_ACCESS = APPOINTMENT_CREATE_MAIL_EXT;

    public static final String APPOINTMENT_CREATE_MAIL_ON_BEHALF_NO_ACCESS = APPOINTMENT_CREATE_MAIL_ON_BEHALF_EXT;

    public static final String TASK_CREATE_MAIL =
        "A new task was created by [created_by].\n" +
        "You can check this task in your tasks:\n" +
        "[link]\n" +
        "\n" +
        "Task\n" +
        "====\n" +
        "Created by: [created_by]\n" +
        "Created at: [creation_datetime]\n" +
        "[title]\n" +
        "[folder_name]\n" +
        "[priority]\n" +
        "[task_status]\n" +
        "\n" +
        "[start]\n" +
        "[end]\n" +
        "[series]" +
        "\n" +
        "[description]" +
        "\n" +
        "Participants\n" +
        "============\n" +
        "[participants]" +
        "\n" +
        "\n" +
        "Resources\n" +
        "=========\n" +
        "[resources]" +
        "\n" +
        "\n" +
        "========================================== ";

    public static final String TASK_CREATE_MAIL_EXT =
        "A new task was created by [created_by].\n" +
        "\n" +
        "Task\n" +
        "====\n" +
        "Created by: [created_by]\n" +
        "Created at: [creation_datetime]\n" +
        "[title]\n" +
        "[folder_name]\n" +
        "[priority]\n" +
        "[task_status]\n" +
        "\n" +
        "[start]\n" +
        "[end]\n" +
        "[series]" +
        "\n" +
        "[description]" +
        "\n" +
        "Participants\n" +
        "============\n" +
        "[participants]" +
        "\n" +
        "\n" +
        "Resources\n" +
        "=========\n" +
        "[resources]" +
        "\n" +
        "\n" +
        "========================================== ";

    /**
     * Notification Template for user which has no access to this task.
     * We use the same as for external participants, because this has no link to the task.
     */
    public static final String TASK_CREATE_MAIL_NO_ACCESS = TASK_CREATE_MAIL_EXT;

    /*-
     * DELETE
     */

    public static final String APPOINTMENT_DELETE_MAIL =
        "This appointment does not take place.\n" +
        "It was either deleted by [changed_by] or\n" +
        "you have been removed from the list of participants.\n" +
        "\n" +
        "Appointment\n" +
        "===========\n" +
        "Created by:  [created_by]\n" +
        "Created at:  [creation_datetime]\n" +
        "[title]\n" +
        "[location]" +
        "[folder_name]\n" +
        "\n" +
        "[start]\n" +
        "[end]\n" +
        "[series]" +
        "[delete_exceptions]" +
        "[change_exceptions]" +
        "\n" +
        "[description]" +
        "\n" +
        "Participants\n" +
        "============\n" +
        "[participants]" +
        "\n" +
        "\n" +
        "Resources\n" +
        "=========\n" +
        "[resources]" +
        "\n" +
        "\n" +
        "==========================================";

    public static final String TASK_DELETE_MAIL =
        "This task was either deleted by [changed_by] or\n" +
        "you have been removed from the list of participants.\n" +
        "\n" +
        "Task\n" +
        "====\n" +
        "Created by:  [created_by]\n" +
        "Created at:  [creation_datetime]\n" +
        "[title]\n" +
        "[folder_name]\n" +
        "[priority]\n" +
        "[task_status]\n" +
        "\n" +
        "[start]\n" +
        "[end]\n" +
        "[series]" +
        "\n" +
        "[description]" +
        "\n" +
        "Participants\n" +
        "============\n" +
        "[participants]" +
        "\n" +
        "\n" +
        "Resources\n" +
        "=========\n" +
        "[resources]" +
        "\n" +
        "\n" +
        "========================================== ";

    /*-
     * UPDATE
     */

    public static final String APPOINTMENT_UPDATE_MAIL =
        "This appointment was changed by [changed_by].\n" +
        "You can check this appointment in your calendar:\n" +
        "[link]\n" +
        "\n" +
        "Appointment\n" +
        "===========\n" +
        "Created by: [created_by]\n" +
        "Created at: [creation_datetime]\n" +
        "[title]\n" +
        "[location]" +
        "[folder_name]\n" +
        "\n" +
        "[start]\n" +
        "[end]\n" +
        "[series]" +
        "[delete_exceptions]" +
        "[change_exceptions]" +
        "\n" +
        "[description]" +
        "\n" +
        "Participants\n" +
        "============\n" +
        "[participants]" +
        "\n" +
        "\n" +
        "Resources\n" +
        "=========\n" +
        "[resources]" +
        "\n" +
        "\n" +
        "==========================================";

    public static final String APPOINTMENT_UPDATE_MAIL_EXT =
        "This appointment was changed by [changed_by].\n" +
        "\n" +
        "Appointment\n" +
        "===========\n" +
        "Created by: [created_by]\n" +
        "Created at: [creation_datetime]\n" +
        "[title]\n" +
        "[location]" +
        "[folder_name]\n" +
        "\n" +
        "[start]\n" +
        "[end]\n" +
        "[series]" +
        "[delete_exceptions]" +
        "[change_exceptions]" +
        "\n" +
        "[description]" +
        "\n" +
        "Participants\n" +
        "============\n" +
        "[participants]" +
        "\n" +
        "\n" +
        "Resources\n" +
        "=========\n" +
        "[resources]" +
        "\n" +
        "\n" +
        "==========================================";

    /**
     * Notification Template for user which has no access to this appointment.
     * We use the same as for external participants, because this has no link to the appointment.
     */
    public static final String APPOINTMENT_UPDATE_MAIL_NO_ACCESS = APPOINTMENT_UPDATE_MAIL_EXT;

    public static final String TASK_UPDATE_MAIL =
        "This task was changed by [changed_by].\n" +
        "You can check this task in your tasks:\n" +
        "[link]\n" +
        "\n" +
        "Task\n" +
        "====\n" +
        "Created by: [created_by]\n" +
        "Created at: [creation_datetime]\n" +
        "[title]\n" +
        "[folder_name]\n" +
        "[priority]\n" +
        "[task_status]\n" +
        "\n" +
        "[start]\n" +
        "[end]\n" +
        "[series]" +
        "\n" +
        "[description]" +
        "\n" +
        "Participants\n" +
        "============\n" +
        "[participants]" +
        "\n" +
        "\n" +
        "Resources\n" +
        "=========\n" +
        "[resources]" +
        "\n" +
        "\n" +
        "==========================================";

    public static final String TASK_UPDATE_MAIL_EXT =
        "This task was changed by [changed_by].\n" +
        "\n" +
        "Task\n" +
        "====\n" +
        "Created by: [created_by]\n" +
        "Created at: [creation_datetime]\n" +
        "[title]\n" +
        "[folder_name]\n" +
        "[priority]\n" +
        "[task_status]\n" +
        "\n" +
        "[start]\n" +
        "[end]\n" +
        "[series]" +
        "\n" +
        "[description]" +
        "\n" +
        "Participants\n" +
        "============\n" +
        "[participants]" +
        "\n" +
        "\n" +
        "Resources\n" +
        "=========\n" +
        "[resources]" +
        "\n" +
        "\n" +
        "==========================================";

    /**
     * Notification Template for user which has no access to this task.
     * We use the same as for external participants, because this has no link to the task.
     */
    public static final String TASK_UPDATE_MAIL_NO_ACCESS = TASK_UPDATE_MAIL_EXT;

    /*-
     * CONFIRMATION
     */

    public static final String APPOINTMENT_CONFIRMATION_MAIL =
        "[changed_by] has [confirmation_action] this appointment.\n" +
        "You can check this appointment in your calendar:\n" +
        "[link]\n" +
        "\n" +
        "Appointment\n" +
        "===========\n" +
        "Created by: [created_by]\n" +
        "Created at: [creation_datetime]\n" +
        "[title]\n" +
        "[location]" +
        "[folder_name]\n" +
        "\n" +
        "[start]\n" +
        "[end]\n" +
        "[series]" +
        "[delete_exceptions]" +
        "[change_exceptions]" +
        "\n" +
        "[description]" +
        "\n" +
        "Participants\n" +
        "============\n" +
        "[participants]" +
        "\n" +
        "\n" +
        "Resources\n" +
        "=========\n" +
        "[resources]" +
        "\n" +
        "\n" +
        "==========================================";

    public static final String APPOINTMENT_CONFIRMATION_MAIL_EXT =
        "[changed_by] has [confirmation_action] this appointment.\n" +
        "\n" +
        "Appointment\n" +
        "===========\n" +
        "Created by: [created_by]\n" +
        "Created at: [creation_datetime]\n" +
        "[title]\n" +
        "[location]" +
        "[folder_name]\n" +
        "\n" +
        "[start]\n" +
        "[end]\n" +
        "[series]" +
        "[delete_exceptions]" +
        "[change_exceptions]" +
        "\n" +
        "[description]" +
        "\n" +
        "Participants\n" +
        "============\n" +
        "[participants]" +
        "\n" +
        "\n" +
        "Resources\n" +
        "=========\n" +
        "[resources]" +
        "\n" +
        "\n" +
        "==========================================";

    public static final String TASK_CONFIRMATION_MAIL =
        "[changed_by] has [confirmation_action] this task.\n" +
        "You can check this task in your tasks:\n" +
        "[link]\n" +
        "\n" +
        "Task\n" +
        "====\n" +
        "Created by: [created_by]\n" +
        "Created at: [creation_datetime]\n" +
        "[title]\n" +
        "[folder_name]\n" +
        "[priority]\n" +
        "[task_status]\n" +
        "\n" +
        "[start]\n" +
        "[end]\n" +
        "[series]" +
        "\n" +
        "[description]" +
        "\n" +
        "Participants\n" +
        "============\n" +
        "[participants]" +
        "\n" +
        "\n" +
        "Resources\n" +
        "=========\n" +
        "[resources]" +
        "\n" +
        "\n" +
        "==========================================";

    public static final String TASK_CONFIRMATION_MAIL_EXT =
        "[changed_by] has [confirmation_action] this task.\n" +
        "\n" +
        "Task\n" +
        "====\n" +
        "Created by: [created_by]\n" +
        "Created at: [creation_datetime]\n" +
        "[title]\n" +
        "[folder_name]\n" +
        "[priority]\n" +
        "[task_status]\n" +
        "\n" +
        "[start]\n" +
        "[end]\n" +
        "[series]" +
        "\n" +
        "[description]" +
        "\n" +
        "Participants\n" +
        "============\n" +
        "[participants]" +
        "\n" +
        "\n" +
        "Resources\n" +
        "=========\n" +
        "[resources]" +
        "\n" +
        "\n" +
        "==========================================";

    /*-
     * MESSAGE SUBJECT PREFIXES
     */

    public static final String APPOINTMENT_CREATE_TITLE = "New appointment";

    public static final String APPOINTMENT_UPDATE_TITLE = "Appointment changed";

    public static final String APPOINTMENT_DELETE_TITLE = "Appointment deleted";

    public static final String APPOINTMENT_ACCEPTED_TITLE = "Appointment (accepted)";

    public static final String APPOINTMENT_DECLINED_TITLE = "Appointment (declined)";

    public static final String APPOINTMENT_TENTATIVE_TITLE = "Appointment (tentative)";

    public static final String APPOINTMENT_NONE_TITLE = "Appointment (none)";

    public static final String TASK_CREATE_TITLE = "New task";

    public static final String TASK_UPDATE_TITLE = "Task changed";

    public static final String TASK_DELETE_TITLE = "Task deleted";

    public static final String TASK_ACCEPTED_TITLE = "Task (accepted)";

    public static final String TASK_DECLINED_TITLE = "Task (declined)";

    public static final String TASK_TENTATIVE_TITLE = "Task (tentative)";

    /*-
     * MISC
     */

    // Reminder
    public static final String REMINDER = "Reminder";

    // No resources have been scheduled.
    public static final String NO_RESOURCES = "No resources have been scheduled.";

    // No series
    public static final String NO_SERIES = "No series";

    // No start date
    public static final String NO_START_DATE = "No start date";

    // No end date
    public static final String NO_END_DATE = "No end date";

    // No due date
    public static final String NO_DUE_DATE = "No due date";

    // No delete exceptions
    public static final String NO_DELETE_EXCEPTIONS = "No delete exceptions";

    // No change exceptions
    public static final String NO_CHANGE_EXCEPTIONS = "No change exceptions";

    // Mail to resource %1$s
    public static final String RESOURCE_PREFIX = "Mail to resource %1$s";

    // Resource
    public static final String RESOURCE_TITLE_PREFIX = "Resource";

    // Not set
    public static final String NOT_SET = "Not set";

    /*-
     * Confirmation actions
     */

    public static final String CA_ACCEPTED = "accepted";

    public static final String CA_DECLINED = "declined";

    public static final String CA_TENTATIVELY_ACCEPTED = "tentatively accepted";

    /*-
     * TASK PRIORITIES
     */

    // low
    public static final String PRIORITY_LOW = "Low";

    // normal
    public static final String PRIORITY_NORMAL = "Normal";

    // high
    public static final String PRIORITY_HIGH = "High";

    /*-
     * Confirmation statuses
     */

    // waiting
    public static final String STATUS_WAITING = "waiting";

    // accepted
    public static final String STATUS_ACCEPTED = "accepted";

    // declined
    public static final String STATUS_DECLINED = "declined";

    // tentative
    public static final String STATUS_TENTATIVE = "tentative";

    // external
    public static final String STATUS_EXTERNAL = "external";

    /*-
     * Line patterns
     */

    // Location: %1$s
    public static final String FORMAT_LOCATION = "Location: %1$s";

    // Description: %1$s
    public static final String FORMAT_DESCRIPTION = "Description: %1$s";

    // Folder: %1$s
    public static final String FORMAT_FOLDER = "Folder: %1$s";

    // Start date: %1$s
    public static final String FORMAT_START_DATE = "Start date: %1$s";

    // End date: %1$s
    public static final String FORMAT_END_DATE = "End date: %1$s";

    // Due date: %1$s
    public static final String FORMAT_DUE_DATE = "Due date: %1$s";

    // Series: %1$s
    public static final String FORMAT_SERIES = "Series: %1$s";

    // Comments:
    // %1$s: Placeholder for comments
    public static final String FORMAT_COMMENTS = "Comments:\n%1$s";

    // Priority: %1$s
    public static final String FORMAT_PRIORITY = "Priority: %1$s";

    // Status: %1$s
    public static final String FORMAT_STATUS = "Status: %1$s";

    // Delete exceptions: %1$s
    public static final String FORMAT_DELETE_EXCEPTIONS = "Delete exceptions: %1$s";

    // Change exceptions: %1$s
    public static final String FORMAT_CHANGE_EXCEPTIONS = "Change exceptions: %1$s";

    // Change exception of %1$s on %2$s
    // %1$s: Placeholder for recurrence title
    // %2$s: Placeholder for change exception's date
    // Example: My Recurring Appointment on 2009-12-06
    public static final String FORMAT_CHANGE_EXCEPTION_OF = "Change exception of %1$s on %2$s";

    /*-
     * Task statuses
     */

    // Not started
    public static final String TASK_STATUS_NOT_STARTED = "Not started";

    // In progress
    public static final String TASK_STATUS_IN_PROGRESS = "In progress";

    // Done
    public static final String TASK_STATUS_DONE = "Done";

    // Waiting
    public static final String TASK_STATUS_WAITING = "Waiting";

    // Deferred
    public static final String TASK_STATUS_DEFERRED = "Deferred";

    /*-
     * Added/Removed participant
     */

    public static final String ADDED = "Added";

    public static final String REMOVED = "Removed";

    /*-
     * Numeric recurrence templates
     */

    // The appendix to indicate the first ordinal; e.g for EN it would be "1st", for DE it would be "1.".
    public static final String REC_1ST_ORDINAL_APPENDIX = "st";

    // The appendix to indicate the second ordinal; e.g for EN it would be "2nd", for DE it would be "2.".
    public static final String REC_2ND_ORDINAL_APPENDIX = "nd";

    // The appendix to indicate the third ordinal; e.g for EN it would be "3rd", for DE it would be "3.".
    public static final String REC_3RD_ORDINAL_APPENDIX = "rd";

    // The appendix to indicate an ordinal greater than 3rd; e.g for EN it would be "4th", for DE it would be "4.".
    public static final String REC_ORDINAL_APPENDIX = "th";

    /*-
     * Recurrence templates
     */

    // and
    public static final String REC_AND = "and";

    // Each day
    public static final String REC_DAILY1 = "Each day";

    // Each %1$s days; e.g. "Each 2 days"
    public static final String REC_DAILY2 = "Each %1$s days";

    // Each week on %1$s; e.g. "Each week on Monday"
    public static final String REC_WEEKLY1 = "Each week on %1$s";

    // Each %1$s weeks on %2$s; e.g. "Each 2 weeks on Monday and Tuesday"
    public static final String REC_WEEKLY2 = "Each %1$s weeks on %2$s";

    // On %1$s day every month; e.g. "On 1st day every month"
    public static final String REC_MONTHLY1_1 = "On %1$s day every month";

    // On %1$s day every %2$s month; e.g. "On 1st day every 2nd month"
    public static final String REC_MONTHLY1_2 = "On %1$s day every %2$s month";

    // On %1$s %2$s each month; e.g. "On first Monday each month"
    public static final String REC_MONTHLY2_1 = "On %1$s %2$s each month";

    // On %1$s %2$s each %3$s month; e.g. "On first Monday each 3rd month"
    public static final String REC_MONTHLY2_2 = "On %1$s %2$s each %3$s month";

    // Each %1$s %2$s; e.g. "Each 10th October"
    public static final String REC_YEARLY1 = "Each %1$s %2$s";

    // On %1$s %2$s in %3$s; e.g "On third Tuesday in April"
    public static final String REC_YEARLY2 = "On %1$s %2$s in %3$s";

    // ", ends after %1$s appointment(s)"
    public static final String REC_ENDS_APPOINTMENT = ", ends after %1$s appointment(s)";

    // ", ends after %1$s task(s)"
    public static final String REC_ENDS_TASK = ", ends after %1$s task(s)";

    // ", ends on %1$s"
    public static final String REC_ENDS_UNTIL = ", ends on %1$s";

    /*-
     * Months
     */

    // January
    public static final String REC_JAN = "January";

    // February
    public static final String REC_FEB = "February";

    // March
    public static final String REC_MARCH = "March";

    // April
    public static final String REC_APRIL = "April";

    // May
    public static final String REC_MAY = "May";

    // June
    public static final String REC_JUNE = "June";

    // July
    public static final String REC_JULY = "July";

    // August
    public static final String REC_AUG = "August";

    // September
    public static final String REC_SEP = "September";

    // October
    public static final String REC_OCT = "October";

    // November
    public static final String REC_NOV = "November";

    // December
    public static final String REC_DEC = "December";

    /*-
     * Weekdays
     */

    // Monday
    public static final String REC_MONDAY = "Monday";

    // Tuesday
    public static final String REC_TUESDAY = "Tuesday";

    // Wednesday
    public static final String REC_WEDNESDAY = "Wednesday";

    // Thursday
    public static final String REC_THURSDAY = "Thursday";

    // Friday
    public static final String REC_FRIDAY = "Friday";

    // Saturday
    public static final String REC_SATURDAY = "Saturday";

    // Sunday
    public static final String REC_SUNDAY = "Sunday";

    // Sunday
    public static final String REC_DAY = "Sunday";

    // Weekday
    public static final String REC_WEEKDAY = "Weekday";

    // Weekend day
    public static final String REC_WEEKENDDAY = "Weekend day";

    /*-
     * Monthly1 constants
     */

    // first
    public static final String REC_FIRST = "first";

    // second
    public static final String REC_SECOND = "second";

    // third
    public static final String REC_THIRD = "third";

    // fourth
    public static final String REC_FOURTH = "fourth";

    // last
    public static final String REC_LAST = "last";
}
