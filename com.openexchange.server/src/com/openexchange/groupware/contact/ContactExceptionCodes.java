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

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Generic;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link ContactExceptionCodes}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public enum ContactExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * Found a user contact outside global address book in folder %1$d in
     * context %2$d.
     */
    USER_OUTSIDE_GLOBAL("Found a user contact outside global address book in folder %1$d in context %2$d.", null,
        Category.CATEGORY_ERROR, 1),

    /**
     * Invalid E-Mail address: '%s'. Please correct the E-Mail address.
     */
    INVALID_EMAIL("Invalid E-Mail address: '%s'. Please correct the E-Mail address.", ContactExceptionMessages.INVALID_EMAIL_DISPLAY,
        Category.CATEGORY_USER_INPUT, 100),

    /**
     * Unable to import this contact picture. Either the type is not part of the
     * supported type (JPG, GIF, BMP or PNG) or the size exceed %3$d. Your file
     * type is %1$s and your image size is %2$d.
     */
    IMAGE_SCALE_PROBLEM("Unable to import this contact picture. Either the type is not part of the supported type (JPG, GIF, BMP or PNG)"
        + " or the size exceed %3$d. Your file type is %1$s and your image size is %2$d.",
        ContactExceptionMessages.IMAGE_SCALE_PROBLEM_DISPLAY, Category.CATEGORY_USER_INPUT, 101),

    /**
     * You are not allowed to store this contact in a non-contact folder: folder
     * id %1$d in context %2$d with user %3$d
     */
    NON_CONTACT_FOLDER("You are not allowed to store this contact in a non-contact folder: folder id %1$d in context %2$d with user %3$d",
        ContactExceptionMessages.NON_CONTACT_FOLDER_DISPLAY, Category.CATEGORY_PERMISSION_DENIED, 103),

    /**
     * You do not have the permission to access objects in this
     * folder %1$d in context %2$d with user %3$d
     */
    NO_ACCESS_PERMISSION("You do not have the permission to access objects in the folder %1$d in the context %2$d as user %3$d",
        ContactExceptionMessages.NO_ACCESS_DISPLAY, Category.CATEGORY_PERMISSION_DENIED, 104),

    /** Got a -1 ID from IDGenerator */
    ID_GENERATION_FAILED("Got a -1 ID from IDGenerator", null, Category.CATEGORY_ERROR, 107),

    /** Unable to scale image down. */
    IMAGE_DOWNSCALE_FAILED("Unable to scale image down.", ContactExceptionMessages.IMAGE_DOWNSCALE_FAILED_DISPLAY,
        Category.CATEGORY_ERROR, 108),

    /** Unexpected database error: \"%1$s\" */
    SQL_PROBLEM("Unexpected database error: \"%1$s\"", OXExceptionStrings.SQL_ERROR_MSG, Category.CATEGORY_ERROR, 109),

    /** Invalid SQL Query: %s */
    AGGREGATING_CONTACTS_NOT_ENABLED("This feature has not been enabled", ContactExceptionMessages.AGGREGATING_CONTACTS_DISABLED_DISPLAY,
        Category.CATEGORY_SERVICE_DOWN, 110),

    /**
     * You do not have CATEGORY_PERMISSION_DENIED to create objects in this
     * folder %1$d in context %2$d with user %3$d
     */
    NO_CREATE_PERMISSION("You do not have the permission to create objects in the folder %1$d in context %2$d as user %3$d",
        ContactExceptionMessages.NO_CREATE_DISPLAY, Category.CATEGORY_PERMISSION_DENIED, 112),

    /**
     * Unable to synchronize the old contact with the new changes: Context %1$d
     * Object %2$d
     */
    LOAD_OLD_CONTACT_FAILED("Unable to synchronize the old contact with the new changes: context %1$d object %2$d",
        ContactExceptionMessages.LOAD_OLD_CONTACT_FAILED_DISPLAY, Category.CATEGORY_ERROR, 116),

    /**
     * You are not allowed to mark this contact as private contact: Context %1$d
     * Object %2$d
     */
    MARK_PRIVATE_NOT_ALLOWED("You are not allowed to mark this contact as private contact: context %1$d object %2$d",
        ContactExceptionMessages.MARK_PRIVATE_NOT_ALLOWED_DISPLAY, Category.CATEGORY_PERMISSION_DENIED, 118),

    /**
     * Edit Conflict. Your change cannot be completed because somebody else has
     * made a conflicting change to the same item. Please refresh or synchronize
     * and try again.
     */
    OBJECT_HAS_CHANGED("Edit Conflict. Your change cannot be completed because somebody else has made a conflicting change to the "
        + "same item. Please refresh or synchronize and try again.", ContactExceptionMessages.OBJECT_HAS_CHANGED_DISPLAY,
        Category.CATEGORY_CONFLICT, 119),

    /** An error occurred: Object id is -1 */
    NEGATIVE_OBJECT_ID("An error occurred: object id is -1", null, Category.CATEGORY_ERROR, 121),

    /** No changes found. No update required. Context %1$d Object %2$d */
    NO_CHANGES("No changes found. No update required. context %1$d object %2$d", ContactExceptionMessages.NO_CHANGES_DISPLAY,
        Category.CATEGORY_USER_INPUT, 122),

    /** Contact %1$d not found in context %2$d. */
    CONTACT_NOT_FOUND("Contact %1$d not found in context %2$d.", ContactExceptionMessages.CONTACT_NOT_FOUND_DISPLAY,
        Category.CATEGORY_ERROR, 125),

    /** Unable to save contact image. The image appears to be broken. */
    IMAGE_BROKEN("Unable to save contact image. The image appears to be broken.", ContactExceptionMessages.IMAGE_BROKEN_DISPLAY,
        Category.CATEGORY_USER_INPUT, 136),

    /** Unable to trigger object Events: Context %1$d Folder %2$d */
    TRIGGERING_EVENT_FAILED("Unable to trigger object events: context %1$d folder %2$d", null,
            Category.CATEGORY_ERROR, 146),

    /** Unable to pick up a connection from the DBPool */
    INIT_CONNECTION_FROM_DBPOOL("Unable to pick up a connection from the DBPool", null,
            Category.CATEGORY_SERVICE_DOWN, 151),

    /**
     * Some data entered exceeded the field limit. Please shorten the value for \"%1$s\" (limit: %2$s, current: %3$s) and try again."
     */
    DATA_TRUNCATION("Some data entered exceeded the field limit. Please shorten the value for \"%1$s\" (limit: %2$s, current: %3$s)"
        + " and try again.", ContactExceptionMessages.DATA_TRUNCATION_DISPLAY, Category.CATEGORY_USER_INPUT, 154),

    /**
     * The image you tried to attach is not a valid picture. It may be broken or
     * is not a valid file.
     */
    NOT_VALID_IMAGE("The image you tried to attach is not a valid picture. It may be broken or is not a valid file.",
        ContactExceptionMessages.NOT_VALID_IMAGE_DISPLAY, Category.CATEGORY_TRY_AGAIN, 158),

    /** Your first name is mandatory. Please enter it. */
    FIRST_NAME_MANDATORY("Your first name is mandatory. Please enter it.", ContactExceptionMessages.FIRST_NAME_MANDATORY_DISPLAY,
            Category.CATEGORY_USER_INPUT, 164),

    /**
     * Unable to move this contact because it is marked as private: Context %1$d
     * Object %2$d
     */
    NO_PRIVATE_MOVE("Unable to move this contact because it is marked as private: context %1$d object %2$d",
        ContactExceptionMessages.NO_PRIVATE_MOVE, Category.CATEGORY_PERMISSION_DENIED, 165),

    /** Your display name is mandatory. Please enter it. */
    DISPLAY_NAME_MANDATORY("Your display name is mandatory. Please enter it.", ContactExceptionMessages.DISPLAY_NAME_MANDATORY_DISPLAY,
            Category.CATEGORY_USER_INPUT, 166),

    /**
     * The name you entered is already assigned to another user. Please choose
     * another display name. Context %1$d Object %2$d
     */
    DISPLAY_NAME_IN_USE("The name you entered is already assigned to another user. Please choose another display name."
        + " context %1$d object %2$d", ContactExceptionMessages.DISPLAY_NAME_IN_USE_DISPLAY, Category.CATEGORY_TRY_AGAIN, 167),

    /** Bad character in field %2$s. Error: %1$s */
    BAD_CHARACTER("Bad character in field %2$s. Error: %1$s", ContactExceptionMessages.BAD_CHARACTER_DISPLAY,
        Category.CATEGORY_USER_INPUT, 168),

    /**
     * You do not have CATEGORY_PERMISSION_DENIED to delete objects from folder
     * %1$d in context %2$d with user %3$d
     */
    NO_DELETE_PERMISSION("You do not have the permission to delete objects from folder %1$d in context %2$d as user %3$d",
            ContactExceptionMessages.NO_DELETE_PERMISSION_DISPLAY, Category.CATEGORY_PERMISSION_DENIED, 169),

    /** Mime type is not defined. */
    MIME_TYPE_NOT_DEFINED("Mime type is not defined.", ContactExceptionMessages.MIME_TYPE_NOT_DEFINED_DISPLAY,
            Category.CATEGORY_USER_INPUT, 170),

    /**
     * A contact with private flag cannot be stored in a public folder. Folder:
     * %1$d context %2$d user %3$d
     */
    PFLAG_IN_PUBLIC_FOLDER("A contact with private flag cannot be stored in a public folder. Folder: %1$d context %2$d user %3$d",
            ContactExceptionMessages.PFLAG_IN_PUBLIC_FOLDER_DISPLAY, Category.CATEGORY_USER_INPUT, 171),

    /** Image size too large. Image size: %1$s. Max. size: %2$s. */
    IMAGE_TOO_LARGE("Image size too large. Image size: %1$s. Max. size: %2$s.", ContactExceptionMessages.IMAGE_TOO_LARGE_DISPLAY,
        Category.CATEGORY_USER_INPUT, 172),

    /**
     * Primary email address in system contact must not be edited: Context %1$d
     * Object %2$d User %3$d
     */
    NO_PRIMARY_EMAIL_EDIT("Primary E-Mail address in system contact must not be edited: context %1$d object %2$d user %3$d",
            ContactExceptionMessages.NO_PRIMARY_EMAIL_EDIT_DISPLAY, Category.CATEGORY_PERMISSION_DENIED, 173),

    /** The contact %1$d is not located in folder %2$s (%3$d) */
    NOT_IN_FOLDER("The contact %1$d is not located in folder %2$s (%3$d)", ContactExceptionMessages.NOT_IN_FOLDER_DISPLAY,
        Category.CATEGORY_PERMISSION_DENIED, 174),

    /** Your last name is mandatory. Please enter it. */
    LAST_NAME_MANDATORY("Your last name is mandatory. Please enter it.", ContactExceptionMessages.LAST_NAME_MANDATORY_DISPLAY,
        Category.CATEGORY_USER_INPUT, 175),

    /** You are not allowed to modify contact %1$d in context %2$d. */
    NO_CHANGE_PERMISSION("You are not allowed to modify contact %1$d in context %2$d.", ContactExceptionMessages.NO_CHANGE_PERMISSION_DISPLAY,
            Category.CATEGORY_PERMISSION_DENIED, 176),

    /** An E-Mail address is mandatory for external distribution list members. Please add a valid E-Mail address. */
    EMAIL_MANDATORY_FOR_EXTERNAL_MEMBERS("An E-Mail address is mandatory for external distribution list members. Please add a valid"
        + " E-Mail address.", ContactExceptionMessages.EMAIL_MANDATORY_FOR_EXTERNAL_MEMBERS_DISPLAY, Category.CATEGORY_USER_INPUT, 177),

    /** The object identifier is mandatory for distribution list members referencing existing contacts. */
    OBJECT_ID_MANDATORY_FOR_REFERENCED_MEMBERS("The object identifier is mandatory for distribution list members referencing"
        + " existing contacts.", ContactExceptionMessages.OBJECT_ID_MANDATORY_FOR_REFERENCED_MEMBERS_DISPLAY, Category.CATEGORY_USER_INPUT, 178),

    /** Unable to load objects. Context %1$d User %2$d */
    LOAD_OBJECT_FAILED("Unable to load objects. Context %1$d user %2$d", null, Category.CATEGORY_ERROR, 252),

    /** User contacts can not be deleted. */
    NO_USER_CONTACT_DELETE("User contacts can not be deleted.", ContactExceptionMessages.NO_USER_CONTACT_DELETE_DISPLAY,
            Category.CATEGORY_PERMISSION_DENIED, 260),

    /** The identifier %1$s can't be parsed. */
    ID_PARSING_FAILED("The identifier %1$s can not be parsed.", null, Category.CATEGORY_ERROR, 261),

    /** "The character \"%1$s\" in field \"%2$s\" can't be saved. Please remove the problematic character and try again." */
    INCORRECT_STRING("Field \"%2$s\" contains invalid character: \"%1$s\"", ContactExceptionMessages.INCORRECT_STRING_DISPLAY, Category.CATEGORY_USER_INPUT, 262),

    /**
     * Search aborted for query \"%1$s\" with more than %2$d patterns. <br />
     * The query \"%1$s\" contains too many patterns. Please shorten the query and try again.
     */
    TOO_MANY_PATTERNS("Search aborted for query \"%1$s\" with more than %2$d patterns.",
        ContactExceptionMessages.TOO_MANY_PATTERNS_DISPLAY, Category.CATEGORY_USER_INPUT, 263),

    /**
     * The pattern \"%1$s\" has been ignored during search.
     */
    IGNORED_PATTERN("The pattern \"%1$s\" has been ignored during search.",
        ContactExceptionMessages.IGNORED_PATTERN_DISPLAY, Category.CATEGORY_USER_INPUT, 264),

    /**
     * Number of documents attached to this contact is below zero. You can not
     * remove any more attachments.
     */
    TOO_FEW_ATTACHMENTS("Number of documents attached to this contact is below zero. You can not remove any more attachments.",
        ContactExceptionMessages.TOO_FEW_ATTACHMENTS_DISPLAY, Category.CATEGORY_USER_INPUT, 400),

    /** Need at least a ContactObject and a value to set %s */
    TOO_FEW_ATTRIBUTES("Setting %s requires at least a ContactObject and a value.", ContactExceptionMessages.TOO_FEW_ATTRIBUTES_DISPLAY,
        Category.CATEGORY_ERROR, 500),

    /** Could not convert given string %1$s to a date. */
    DATE_CONVERSION_FAILED("Given string %1$s could not be converted to a date.", ContactExceptionMessages.DATE_CONVERSION_FAILED_DISPLAY,
        Category.CATEGORY_ERROR, 600),

    /** Could not convert given object %1$s to a date when setting %2$s. */
    CONV_OBJ_2_DATE_FAILED("Could not convert given object %s to a date when setting %s.",
        ContactExceptionMessages.CONV_OBJ_2_DATE_FAILED_DISPLAY, Category.CATEGORY_ERROR, 700),

    /** Need at least a ContactObject to get the value of %s */
    CONTACT_OBJECT_MISSING("Getting the value of %s requires at least a ContactObject",
        ContactExceptionMessages.CONTACT_OBJECT_MISSING_DISPLAY, Category.CATEGORY_ERROR, 800),

    /** In order to accomplish the search, %1$d or more characters are required. */
    TOO_FEW_SEARCH_CHARS("In order to accomplish the search, %1$d or more characters are required.",
        ContactExceptionMessages.TOO_FEW_SEARCH_CHARS_DISPLAY, Category.CATEGORY_USER_INPUT, 1000),

    /** An unexpected error occurred: %1$s */
    UNEXPECTED_ERROR("An unexpected error occurred: %1$s", null, Category.CATEGORY_ERROR, 1001),

    ;

    public static final String PREFIX = "CON".intern();

    private String message;
    private String displayMessage;
    private Category category;
    private int number;

    private ContactExceptionCodes(final String message, final String displayMessage, final Category category, final int number) {
        this.message = message;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
        this.category = category;
        this.number = number;
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
    public String getDisplayMessage() {
        return displayMessage;
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
		case CONTACT_NOT_FOUND:
	    case CONTACT_OBJECT_MISSING:
			exc.setGeneric(Generic.NOT_FOUND);
			break;
		case OBJECT_HAS_CHANGED:
			exc.setGeneric(Generic.CONFLICT);
			break;
        default:
            break;
		}
		if (exc.getCategories().contains(Category.CATEGORY_PERMISSION_DENIED)) {
			exc.setGeneric(Generic.NO_PERMISSION);
		}
		return exc;
	}
}
