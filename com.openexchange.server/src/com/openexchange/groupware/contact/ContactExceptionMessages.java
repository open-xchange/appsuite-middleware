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

package com.openexchange.groupware.contact;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link ContactExceptionMessages}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ContactExceptionMessages implements LocalizableStrings {

    public static final String USER_OUTSIDE_GLOBAL_MSG = "Found a user contact outside global address book in folder %1$d in context %2$d.";

    public static final String INVALID_EMAIL_MSG = "Invalid E-Mail address: '%s'. Please correct the E-Mail address.";

    public static final String IMAGE_SCALE_PROBLEM_MSG = "Unable to import this contact picture. Either the type is not part of the supported type (JPG, GIF, BMP or PNG) or the size exceed %3$d. Your file type is %1$s and your image size is %2$d.";

    public static final String NON_CONTACT_FOLDER_MSG = "You are not allowed to store this contact in a non-contact folder: folder id %1$d in context %2$d with user %3$d";

    public static final String NO_ACCESS_PERMISSION_MSG = "You do not have the permission to access objects in the folder %1$d in the context %2$d as user %3$d";

    public static final String ID_GENERATION_FAILED_MSG = "Got a -1 ID from IDGenerator";

    public static final String IMAGE_DOWNSCALE_FAILED_MSG = "Unable to scale image down.";

    public static final String SQL_PROBLEM_MSG = "Invalid SQL query.";

    public static final String NO_CREATE_PERMISSION_MSG = "You do not have the permission to create objects in the folder %1$d in context %2$d as user %3$d";

    public static final String LOAD_OLD_CONTACT_FAILED_MSG = "Unable to synchronize the old contact with the new changes: context %1$d object %2$d";

    public static final String MARK_PRIVATE_NOT_ALLOWED_MSG = "You are not allowed to mark this contact as private contact: context %1$d object %2$d";

    public static final String OBJECT_HAS_CHANGED_MSG = "Edit Conflict. Your change cannot be completed because somebody else" + " has made a conflicting change to the same item. Please refresh or " + "synchronize and try again.";

    public static final String NEGATIVE_OBJECT_ID_MSG = "An error occurred: object id is -1";

    public static final String NO_CHANGES_MSG = "No changes found. No update required. context %1$d object %2$d";

    public static final String CONTACT_NOT_FOUND_MSG = "Contact %1$d not found in context %2$d.";

    public static final String IMAGE_BROKEN_MSG = "Unable to save contact image. The image appears to be broken.";

    public static final String TRIGGERING_EVENT_FAILED_MSG = "Unable to trigger object events: context %1$d folder %2$d";

    public static final String INIT_CONNECTION_FROM_DBPOOL_MSG = "Unable to pick up a connection from the DBPool";

    public static final String DATA_TRUNCATION_MSG = "Some data entered exceeded the field limit. Please shorten the value for \"%1$s\" (limit: %2$s, current: %3$s) and try again.";

    public static final String NOT_VALID_IMAGE_MSG = "The image you tried to attach is not a valid picture. It may be broken or is not a valid file.";

    public static final String FIRST_NAME_MANDATORY_MSG = "Your first name is mandatory. Please enter it.";

    public static final String NO_PRIVATE_MOVE_MSG = "Unable to move this contact because it is marked as private: context %1$d object %2$d";

    public static final String DISPLAY_NAME_MANDATORY = "Your display name is mandatory. Please enter it.";

    public static final String DISPLAY_NAME_IN_USE_MSG = "The name you entered is already assigned to another user. Please choose another display name. context %1$d object %2$d";

    public static final String BAD_CHARACTER_MSG = "Bad character in field %2$s. Error: %1$s";

    public static final String NO_DELETE_PERMISSION_MSG = "You do not have the permission to delete objects from folder %1$d in context %2$d as user %3$d";

    public static final String MIME_TYPE_NOT_DEFINED_MSG = "Mime type is not defined.";

    public static final String PFLAG_IN_PUBLIC_FOLDER_MSG = "A contact with private flag cannot be stored in a public folder. Folder: %1$d context %2$d user %3$d";

    public static final String IMAGE_TOO_LARGE_MSG = "Image size too large. Image size: %1$d. Max. size: %2$d.";

    public static final String NO_PRIMARY_EMAIL_EDIT_MSG = "Primary E-Mail address in system contact must not be edited: context %1$d object %2$d user %3$d";

    public static final String NOT_IN_FOLDER_MSG = "The contact %1$d is not located in folder %2$s (%3$d)";

    public static final String LAST_NAME_MANDATORY_MSG = "Your last name is mandatory. Please enter it.";

    public static final String NO_CHANGE_PERMISSION_MSG = "You are not allowed to modify contact %1$d in context %2$d.";

    public static final String EMAIL_MANDATORY_FOR_EXTERNAL_MEMBERS_MSG = "An E-Mail address is mandatory for external distribution list members. Please add a valid E-Mail address.";

    public static final String LOAD_OBJECT_FAILED_MSG = "Unable to load objects. Context %1$d user %2$d";

    public static final String FEATURE_DISABLED_MSG = "This feature has not been enabled";

    public static final String NO_USER_CONTACT_DELETE_MSG = "User contacts can not be deleted.";

    public static final String ID_PARSING_FAILED_MSG = "The identifier %1$s can not be parsed.";

    public static final String TOO_FEW_ATTACHMENTS_MSG = "Number of documents attached to this contact is below zero. You can not remove any more attachments.";

    public static final String TOO_FEW_ATTRIBUTES_MSG = "Setting %s requires at least a ContactObject and a value.";

    public static final String DATE_CONVERSION_FAILED_MSG = "Given string %1$s could not be converted to a date.";

    public static final String CONV_OBJ_2_DATE_FAILED_MSG = "Could not convert given object %s to a date when setting %s.";

    public static final String CONTACT_OBJECT_MISSING_MSG = "Getting the value of %s requires at least a ContactObject";

    public static final String TOO_FEW_SEARCH_CHARS_MSG = "In order to accomplish the search, %1$d or more characters are required.";

    public static final String UNEXPECTED_ERROR_MSG = "An unexpected error occurred: %1$s";


    private ContactExceptionMessages() {
        super();
    }
}
