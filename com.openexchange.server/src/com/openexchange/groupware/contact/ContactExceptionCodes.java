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

import static com.openexchange.groupware.contact.ContactExceptionMessages.BAD_CHARACTER_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.CONTACT_NOT_FOUND_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.CONTACT_OBJECT_MISSING_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.CONV_OBJ_2_DATE_FAILED_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.DATA_TRUNCATION_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.DATE_CONVERSION_FAILED_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.DISPLAY_NAME_IN_USE_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.EMAIL_MANDATORY_FOR_EXTERNAL_MEMBERS_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.FEATURE_DISABLED_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.FIRST_NAME_MANDATORY_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.ID_GENERATION_FAILED_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.ID_PARSING_FAILED_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.IMAGE_BROKEN_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.IMAGE_DOWNSCALE_FAILED_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.IMAGE_SCALE_PROBLEM_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.IMAGE_TOO_LARGE_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.INIT_CONNECTION_FROM_DBPOOL_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.INVALID_EMAIL_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.LAST_NAME_MANDATORY_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.LOAD_OBJECT_FAILED_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.LOAD_OLD_CONTACT_FAILED_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.MARK_PRIVATE_NOT_ALLOWED_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.MIME_TYPE_NOT_DEFINED_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.NEGATIVE_OBJECT_ID_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.NON_CONTACT_FOLDER_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.NOT_IN_FOLDER_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.NOT_VALID_IMAGE_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.NO_ACCESS_PERMISSION_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.NO_CHANGES_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.NO_CHANGE_PERMISSION_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.NO_CREATE_PERMISSION_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.NO_DELETE_PERMISSION_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.NO_PRIMARY_EMAIL_EDIT_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.NO_PRIVATE_MOVE_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.NO_USER_CONTACT_DELETE_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.OBJECT_HAS_CHANGED_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.PFLAG_IN_PUBLIC_FOLDER_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.SQL_PROBLEM_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.TOO_FEW_ATTACHMENTS_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.TOO_FEW_ATTRIBUTES_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.TOO_FEW_SEARCH_CHARS_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.TRIGGERING_EVENT_FAILED_MSG;
import static com.openexchange.groupware.contact.ContactExceptionMessages.USER_OUTSIDE_GLOBAL_MSG;
import com.openexchange.exception.Category;
import com.openexchange.exception.LogLevel;
import com.openexchange.exception.LogLevelAwareOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Generic;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link ContactExceptionCodes}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public enum ContactExceptionCodes implements LogLevelAwareOXExceptionCode {

    /**
     * Found a user contact outside global address book in folder %1$d in
     * context %2$d.
     */
    USER_OUTSIDE_GLOBAL(USER_OUTSIDE_GLOBAL_MSG, Category.CATEGORY_ERROR, 1),
    /**
     * Invalid E-Mail address: '%s'. Please correct the E-Mail address.
     */
    INVALID_EMAIL(INVALID_EMAIL_MSG, Category.CATEGORY_USER_INPUT, 100, LogLevel.ERROR),
    /**
     * Unable to import this contact picture. Either the type is not part of the
     * supported type (JPG, GIF, BMP or PNG) or the size exceed %3$d. Your file
     * type is %1$s and your image size is %2$d.
     */
    IMAGE_SCALE_PROBLEM(IMAGE_SCALE_PROBLEM_MSG, Category.CATEGORY_USER_INPUT,
            101),
    /**
     * You are not allowed to store this contact in a non-contact folder: folder
     * id %1$d in context %2$d with user %3$d
     */
    NON_CONTACT_FOLDER(NON_CONTACT_FOLDER_MSG,
            Category.CATEGORY_PERMISSION_DENIED, 103),
    /**
     * You do not have CATEGORY_PERMISSION_DENIED to access objects in this
     * folder %1$d in context %2$d with user %3$d
     */
    NO_ACCESS_PERMISSION(NO_ACCESS_PERMISSION_MSG,
            Category.CATEGORY_PERMISSION_DENIED, 104),
    /** Got a -1 ID from IDGenerator */
    ID_GENERATION_FAILED(ID_GENERATION_FAILED_MSG, Category.CATEGORY_ERROR, 107),
    /** Unable to scale image down. */
    IMAGE_DOWNSCALE_FAILED(IMAGE_DOWNSCALE_FAILED_MSG, Category.CATEGORY_ERROR,
            108),
    /** Invalid SQL Query. */
    SQL_PROBLEM(SQL_PROBLEM_MSG, Category.CATEGORY_ERROR, 109),
    /** Invalid SQL Query: %s */
    AGGREGATING_CONTACTS_NOT_ENABLED(FEATURE_DISABLED_MSG,
            Category.CATEGORY_SERVICE_DOWN, 110),
    /**
     * You do not have CATEGORY_PERMISSION_DENIED to create objects in this
     * folder %1$d in context %2$d with user %3$d
     */
    NO_CREATE_PERMISSION(NO_CREATE_PERMISSION_MSG,
            Category.CATEGORY_PERMISSION_DENIED, 112),
    /**
     * Unable to synchronize the old contact with the new changes: Context %1$d
     * Object %2$d
     */
    LOAD_OLD_CONTACT_FAILED(LOAD_OLD_CONTACT_FAILED_MSG,
            Category.CATEGORY_ERROR, 116),
    /**
     * You are not allowed to mark this contact as private contact: Context %1$d
     * Object %2$d
     */
    MARK_PRIVATE_NOT_ALLOWED(MARK_PRIVATE_NOT_ALLOWED_MSG,
            Category.CATEGORY_PERMISSION_DENIED, 118),
    /**
     * Edit Conflict. Your change cannot be completed because somebody else has
     * made a conflicting change to the same item. Please refresh or synchronize
     * and try again.
     */
    OBJECT_HAS_CHANGED(OBJECT_HAS_CHANGED_MSG, Category.CATEGORY_CONFLICT, 119),
    /** An error occurred: Object id is -1 */
    NEGATIVE_OBJECT_ID(NEGATIVE_OBJECT_ID_MSG, Category.CATEGORY_ERROR, 121),
    /** No changes found. No update required. Context %1$d Object %2$d */
    NO_CHANGES(NO_CHANGES_MSG, Category.CATEGORY_USER_INPUT, 122),
    /** Contact %1$d not found in context %2$d. */
    CONTACT_NOT_FOUND(CONTACT_NOT_FOUND_MSG, Category.CATEGORY_ERROR, 125),
    /** Unable to save contact image. The image appears to be broken. */
    IMAGE_BROKEN(IMAGE_BROKEN_MSG, Category.CATEGORY_USER_INPUT, 136),
    /** Unable to trigger object Events: Context %1$d Folder %2$d */
    TRIGGERING_EVENT_FAILED(TRIGGERING_EVENT_FAILED_MSG,
            Category.CATEGORY_ERROR, 146),
    /** Unable to pick up a connection from the DBPool */
    INIT_CONNECTION_FROM_DBPOOL(INIT_CONNECTION_FROM_DBPOOL_MSG,
            Category.CATEGORY_SERVICE_DOWN, 151),
    /**
     * Some data entered exceeded the field limit. Please shorten the value for \"%1$s\" (limit: %2$s, current: %3$s) and try again."
     */
    DATA_TRUNCATION(DATA_TRUNCATION_MSG, Category.CATEGORY_USER_INPUT, 154),
    /**
     * The image you tried to attach is not a valid picture. It may be broken or
     * is not a valid file.
     */
    NOT_VALID_IMAGE(NOT_VALID_IMAGE_MSG, Category.CATEGORY_TRY_AGAIN, 158),
    /** Your first name is mandatory. Please enter it. */
    FIRST_NAME_MANDATORY(FIRST_NAME_MANDATORY_MSG,
            Category.CATEGORY_USER_INPUT, 164),
    /**
     * Unable to move this contact because it is marked as private: Context %1$d
     * Object %2$d
     */
    NO_PRIVATE_MOVE(NO_PRIVATE_MOVE_MSG, Category.CATEGORY_PERMISSION_DENIED,
            165),
    /** Your display name is mandatory. Please enter it. */
    DISPLAY_NAME_MANDATORY(ContactExceptionMessages.DISPLAY_NAME_MANDATORY,
            Category.CATEGORY_USER_INPUT, 166),
    /**
     * The name you entered is already assigned to another user. Please choose
     * another display name. Context %1$d Object %2$d
     */
    DISPLAY_NAME_IN_USE(DISPLAY_NAME_IN_USE_MSG, Category.CATEGORY_TRY_AGAIN,
            167),
    /** Bad character in field %2$s. Error: %1$s */
    BAD_CHARACTER(BAD_CHARACTER_MSG, Category.CATEGORY_USER_INPUT, 168),
    /**
     * You do not have CATEGORY_PERMISSION_DENIED to delete objects from folder
     * %1$d in context %2$d with user %3$d
     */
    NO_DELETE_PERMISSION(NO_DELETE_PERMISSION_MSG,
            Category.CATEGORY_PERMISSION_DENIED, 169),
    /** Mime type is not defined. */
    MIME_TYPE_NOT_DEFINED(MIME_TYPE_NOT_DEFINED_MSG,
            Category.CATEGORY_USER_INPUT, 170),
    /**
     * A contact with private flag cannot be stored in a public folder. Folder:
     * %1$d context %2$d user %3$d
     */
    PFLAG_IN_PUBLIC_FOLDER(PFLAG_IN_PUBLIC_FOLDER_MSG,
            Category.CATEGORY_USER_INPUT, 171),
    /** Image size too large. Image size: %1$d. Max. size: %2$d. */
    IMAGE_TOO_LARGE(IMAGE_TOO_LARGE_MSG, Category.CATEGORY_USER_INPUT, 172),
    /**
     * Primary email address in system contact must not be edited: Context %1$d
     * Object %2$d User %3$d
     */
    NO_PRIMARY_EMAIL_EDIT(NO_PRIMARY_EMAIL_EDIT_MSG,
            Category.CATEGORY_PERMISSION_DENIED, 173),
    /** The contact %1$d is not located in folder %2$s (%3$d) */
    NOT_IN_FOLDER(NOT_IN_FOLDER_MSG, Category.CATEGORY_PERMISSION_DENIED, 174),
    /** Your last name is mandatory. Please enter it. */
    LAST_NAME_MANDATORY(LAST_NAME_MANDATORY_MSG, Category.CATEGORY_USER_INPUT,
            175),
    /** You are not allowed to modify contact %1$d in context %2$d. */
    NO_CHANGE_PERMISSION(NO_CHANGE_PERMISSION_MSG,
            Category.CATEGORY_PERMISSION_DENIED, 176),
    /**
     * An E-Mail address is mandatory for external distribution list members.
     * Please add a valid E-Mail address.
     */
    EMAIL_MANDATORY_FOR_EXTERNAL_MEMBERS(
            EMAIL_MANDATORY_FOR_EXTERNAL_MEMBERS_MSG,
            Category.CATEGORY_USER_INPUT, 177),
    /** Unable to load objects. Context %1$d User %2$d */
    LOAD_OBJECT_FAILED(LOAD_OBJECT_FAILED_MSG, Category.CATEGORY_ERROR, 252),
    /** User contacts can not be deleted. */
    NO_USER_CONTACT_DELETE(NO_USER_CONTACT_DELETE_MSG,
            Category.CATEGORY_PERMISSION_DENIED, 260),
    /** The identifier %1$s can't be parsed. */
    ID_PARSING_FAILED(ID_PARSING_FAILED_MSG, Category.CATEGORY_ERROR, 261),
    /**
     * Number of documents attached to this contact is below zero. You can not
     * remove any more attachments.
     */
    TOO_FEW_ATTACHMENTS(TOO_FEW_ATTACHMENTS_MSG, Category.CATEGORY_USER_INPUT,
            400),
    /** Need at least a ContactObject and a value to set %s */
    TOO_FEW_ATTRIBUTES(TOO_FEW_ATTRIBUTES_MSG, Category.CATEGORY_ERROR, 500),
    /** Could not convert given string %1$s to a date. */
    DATE_CONVERSION_FAILED(DATE_CONVERSION_FAILED_MSG, Category.CATEGORY_ERROR,
            600),
    /** Could not convert given object %1$s to a date when setting %2$s. */
    CONV_OBJ_2_DATE_FAILED(CONV_OBJ_2_DATE_FAILED_MSG, Category.CATEGORY_ERROR,
            700),
    /** Need at least a ContactObject to get the value of %s */
    CONTACT_OBJECT_MISSING(CONTACT_OBJECT_MISSING_MSG, Category.CATEGORY_ERROR,
            800),
    /** In order to accomplish the search, %1$d or more characters are required. */
    TOO_FEW_SEARCH_CHARS(TOO_FEW_SEARCH_CHARS_MSG,
            Category.CATEGORY_USER_INPUT, 1000),
    /** An unexpected error occurred: %1$s */
    UNEXPECTED_ERROR(ContactExceptionMessages.UNEXPECTED_ERROR_MSG,
            Category.CATEGORY_USER_INPUT, 1001),

    ;

    public static final String PREFIX = "CON".intern();

    private String message;
    private Category category;
    private int number;
    private LogLevel logLevel;

    private ContactExceptionCodes(final String message,
        final Category category, final int number) {
        this(message, category, number, null);
    }

    private ContactExceptionCodes(final String message,
            final Category category, final int number, final LogLevel logLevel) {
        this.message = message;
        this.category = category;
        this.number = number;
        this.logLevel = logLevel;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public LogLevel getLogLevel() {
        return logLevel;
    }

    @Override
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's
     * attributes.
     *
     * @return The newly created {@link OXException} instance
     */
    public OXException create() {
        return specials(OXExceptionFactory.getInstance().create(this,
                new Object[0]));
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's
     * attributes.
     *
     * @param args
     *            The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Object... args) {
        return specials(OXExceptionFactory.getInstance().create(this,
                (Throwable) null, args));
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's
     * attributes.
     *
     * @param cause
     *            The optional initial cause
     * @param args
     *            The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Throwable cause, final Object... args) {
        return specials(OXExceptionFactory.getInstance().create(this, cause,
                args));
    }

	private OXException specials(OXException exc) {
		switch(this) {
		case CONTACT_NOT_FOUND: case CONTACT_OBJECT_MISSING:
			exc.setGeneric(Generic.NOT_FOUND);
			break;
		case OBJECT_HAS_CHANGED:
			exc.setGeneric(Generic.CONFLICT);
			break;
		}
		if (exc.getCategories().contains(Category.CATEGORY_PERMISSION_DENIED)) {
			exc.setGeneric(Generic.NO_PERMISSION);
		}
		return exc;
	}
}
