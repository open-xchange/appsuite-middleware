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

package com.openexchange.groupware.contact;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link ContactExceptionMessages}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ContactExceptionMessages implements LocalizableStrings {

    public final static String INVALID_EMAIL_DISPLAY = "Invalid E-Mail address: '%s'. Please correct the E-Mail address.";

    public final static String IMAGE_SCALE_PROBLEM_DISPLAY = "Unable to import this contact picture.";

    public final static String NON_CONTACT_FOLDER_DISPLAY = "Folder is not of type Contact.";

    public final static String NO_ACCESS_DISPLAY = "You do not have the appropriate permission to access objects in the folder \"%1$s\".";

    public final static String IMAGE_DOWNSCALE_FAILED_DISPLAY = "Unable to scale image down.";

    public final static String AGGREGATING_CONTACTS_DISABLED_DISPLAY = "This feature has not been enabled";

    public final static String NO_CREATE_DISPLAY = "You do not have the appropriate permission to create objects in the folder \"%1$s\".";

    public final static String LOAD_OLD_CONTACT_FAILED_DISPLAY = "Unable to synchronize the old contact with the new changes.";

    public final static String MARK_PRIVATE_NOT_ALLOWED_DISPLAY = "You are not allowed to mark this contact as private contact.";

    // Somebody else modified the same object just before the actual change should be saved. Actual change is denied and user should refresh
    // his object.
    public final static String OBJECT_HAS_CHANGED_DISPLAY = "An edit conflict occurred. To edit the contact please reload it.";

    public final static String NO_CHANGES_DISPLAY = "No changes found. No update required.";

    public final static String CONTACT_NOT_FOUND_DISPLAY = "Contact \"%1$s\" not found.";

    public final static String IMAGE_BROKEN_DISPLAY = "The image appears to be broken.";

    public final static String DATA_TRUNCATION_DISPLAY = "Some data entered exceeded the field limit. Please shorten the value for \"%1$s\" (limit: %2$s, current: %3$s) and try again.";

    public final static String NOT_VALID_IMAGE_DISPLAY = "The image you tried to attach is not a valid picture. It may be broken or is not a valid file.";

    public final static String FIRST_NAME_MANDATORY_DISPLAY = "Required  value \"first name\" was not supplied.";

    public final static String DISPLAY_NAME_MANDATORY_DISPLAY = "Required  value \"display name\" was not supplied.";

    public final static String NO_PRIVATE_MOVE = "Unable to move this contact because it is marked as private.";

    public final static String DISPLAY_NAME_IN_USE_DISPLAY = "The name you entered is already assigned to another user. Please choose another display name.";

    public final static String BAD_CHARACTER_DISPLAY = "Bad character in field \"%2$s\".";

    public final static String NO_DELETE_PERMISSION_DISPLAY = "You do not have the appropriate permission to delete objects from the folder \"%1$s\".";

    public final static String MIME_TYPE_NOT_DEFINED_DISPLAY = "Mime type is not defined.";

    public final static String PFLAG_IN_PUBLIC_FOLDER_DISPLAY = "Storing a contact with private flag in a shared folder is not allowed.";

    public final static String IMAGE_TOO_LARGE_DISPLAY = "Image size too large. Image size: %1$s. Max. size: %2$s.";

    public final static String NO_PRIMARY_EMAIL_EDIT_DISPLAY = "Primary E-Mail address in system contact must not be edited.";

    public final static String NOT_IN_FOLDER_DISPLAY = "The contact %1$d is not located in folder %2$s (%3$d).";

    public final static String LAST_NAME_MANDATORY_DISPLAY = "Required  value \"last name\" was not supplied.";

    public final static String NO_CHANGE_PERMISSION_DISPLAY = "You are not allowed to modify that contact";

    public final static String EMAIL_MANDATORY_FOR_EXTERNAL_MEMBERS_DISPLAY = "An E-Mail address is mandatory for external distribution list members. Please add a valid E-Mail address to the contact \"%1$s\".";

    public final static String OBJECT_ID_MANDATORY_FOR_REFERENCED_MEMBERS_DISPLAY = "The object identifier is mandatory for distribution list members referencing existing contacts.";

    public final static String NO_USER_CONTACT_DELETE_DISPLAY = "User contacts can not be deleted.";

    public final static String TOO_FEW_ATTACHMENTS_DISPLAY = "Number of documents attached to this contact is below zero. You can not remove any more attachments.";

    public final static String TOO_FEW_ATTRIBUTES_DISPLAY = "Setting %s requires at least a contact and a value.";

    public final static String DATE_CONVERSION_FAILED_DISPLAY = "Given string %1$s could not be converted to a date.";

    public final static String CONV_OBJ_2_DATE_FAILED_DISPLAY = "Could not convert given object %s to a date when setting %s.";

    public final static String CONTACT_OBJECT_MISSING_DISPLAY = "Getting the value of %s requires at least a ContactObject";

    public final static String TOO_FEW_SEARCH_CHARS_DISPLAY = "In order to accomplish the search, %1$d or more characters are required.";

    public final static String INCORRECT_STRING_DISPLAY = "The character \"%1$s\" in field \"%2$s\" can't be saved. Please remove the problematic character and try again.";

    public final static String TOO_MANY_PATTERNS_DISPLAY = "The query \"%1$s\" contains too many patterns. Please shorten the query and try again.";

    public final static String IGNORED_PATTERN_DISPLAY = "The pattern \"%1$s\" has been ignored during search.";

    private ContactExceptionMessages() {
        super();
    }
}
