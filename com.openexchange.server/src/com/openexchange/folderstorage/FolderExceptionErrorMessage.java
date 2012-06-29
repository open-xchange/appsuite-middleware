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

package com.openexchange.folderstorage;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Generic;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link FolderExceptionErrorMessage} - Error messages for folder exceptions.
 * <p>
 * Subclasses are supposed to start at <code>1000</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum FolderExceptionErrorMessage implements OXExceptionCode {

    /**
     * Unexpected error: %1$s
     */
    UNEXPECTED_ERROR(FolderExceptionMessages.UNEXPECTED_ERROR_MSG, Category.CATEGORY_ERROR, 1001),
    /**
     * I/O error: %1$s
     */
    IO_ERROR(FolderExceptionMessages.IO_ERROR_MSG, Category.CATEGORY_ERROR, 1002),
    /**
     * Folder "%1$s" is not visible to user "%2$s" in context "%3$s"
     * <p>
     * Folder identifier should be passed as first argument to not injure privacy through publishing folder name.
     */
    FOLDER_NOT_VISIBLE(FolderExceptionMessages.FOLDER_NOT_VISIBLE_MSG, Category.CATEGORY_PERMISSION_DENIED, 3),
    /**
     * No appropriate folder storage for tree identifier "%1$s" and folder identifier "%2$s".
     */
    NO_STORAGE_FOR_ID(FolderExceptionMessages.NO_STORAGE_FOR_ID_MSG, Category.CATEGORY_ERROR, 1004),
    /**
     * No appropriate folder storage for tree identifier "%1$s" and content type "%2$s".
     */
    NO_STORAGE_FOR_CT(FolderExceptionMessages.NO_STORAGE_FOR_CT_MSG, Category.CATEGORY_ERROR, 1005),
    /**
     * Missing session.
     */
    MISSING_SESSION(FolderExceptionMessages.MISSING_SESSION_MSG, Category.CATEGORY_ERROR, 1006),
    /**
     * SQL error: %1$s
     */
    SQL_ERROR(FolderExceptionMessages.SQL_ERROR_MSG, Category.CATEGORY_ERROR, 1007),
    /**
     * Folder "%1$s" could not be found in tree "%2$s".
     */
    NOT_FOUND(FolderExceptionMessages.NOT_FOUND_MSG, Category.CATEGORY_ERROR, 8),
    /**
     * Missing tree identifier.
     */
    MISSING_TREE_ID(FolderExceptionMessages.MISSING_TREE_ID_MSG, Category.CATEGORY_ERROR, 1009),
    /**
     * Missing parent folder identifier.
     */
    MISSING_PARENT_ID(FolderExceptionMessages.MISSING_PARENT_ID_MSG, Category.CATEGORY_ERROR, 1010),
    /**
     * Missing folder identifier.
     */
    MISSING_FOLDER_ID(FolderExceptionMessages.MISSING_FOLDER_ID_MSG, Category.CATEGORY_ERROR, 1011),
    /**
     * Parent folder "%1$s" does not allow folder content type "%2$s" in tree "%3$s" for user %4$s in context %5$s.
     */
    INVALID_CONTENT_TYPE(FolderExceptionMessages.INVALID_CONTENT_TYPE_MSG, Category.CATEGORY_ERROR, 1012),
    /**
     * Move operation not permitted.
     */
    MOVE_NOT_PERMITTED(FolderExceptionMessages.MOVE_NOT_PERMITTED_MSG, Category.CATEGORY_ERROR, 1013),
    /**
     * A folder named "%1$s" already exists below parent folder "%2$s" in tree "%3$s".
     */
    EQUAL_NAME(FolderExceptionMessages.EQUAL_NAME_MSG, Category.CATEGORY_PERMISSION_DENIED, 1014),
    /**
     * Subscribe operation not permitted on tree "%1$s".
     */
    NO_REAL_SUBSCRIBE(FolderExceptionMessages.NO_REAL_SUBSCRIBE_MSG, Category.CATEGORY_PERMISSION_DENIED, 1015),
    /**
     * Unsubscribe operation not permitted on tree "%1$s".
     */
    NO_REAL_UNSUBSCRIBE(FolderExceptionMessages.NO_REAL_UNSUBSCRIBE_MSG, Category.CATEGORY_PERMISSION_DENIED, 1016),
    /**
     * Unsubscribe operation not permitted on folder "%1$s" in tree "%2$s". Unsubscribe subfolders first.
     */
    NO_UNSUBSCRIBE(FolderExceptionMessages.NO_UNSUBSCRIBE_MSG, Category.CATEGORY_PERMISSION_DENIED, 1017),
    /**
     * Unknown content type: %1$s.
     */
    UNKNOWN_CONTENT_TYPE(FolderExceptionMessages.UNKNOWN_CONTENT_TYPE_MSG, Category.CATEGORY_ERROR, 1018),
    /**
     * Missing parameter: %1$s.
     */
    MISSING_PARAMETER(FolderExceptionMessages.MISSING_PARAMETER_MSG, Category.CATEGORY_ERROR, 1019),
    /**
     * Unsupported storage type: %1$s.
     */
    UNSUPPORTED_STORAGE_TYPE(FolderExceptionMessages.UNSUPPORTED_STORAGE_TYPE_MSG, Category.CATEGORY_ERROR, 1020),
    /**
     * Missing property: %1$s.
     */
    MISSING_PROPERTY(FolderExceptionMessages.MISSING_PROPERTY_MSG, Category.CATEGORY_ERROR, 1021),
    /**
     * The object has been changed in the meantime.
     */
    CONCURRENT_MODIFICATION(FolderExceptionMessages.CONCURRENT_MODIFICATION_MSG, Category.CATEGORY_CONFLICT, 1022),
    /**
     * JSON error: %1$s
     */
    JSON_ERROR(FolderExceptionMessages.JSON_ERROR_MSG, Category.CATEGORY_ERROR, 1023),
    /**
     * No default folder available for content type "%1$s" in tree "%2$s".
     */
    NO_DEFAULT_FOLDER(FolderExceptionMessages.NO_DEFAULT_FOLDER_MSG, Category.CATEGORY_ERROR, 1024),
    /**
     * Invalid folder identifier: %1$s.
     */
    INVALID_FOLDER_ID(FolderExceptionMessages.INVALID_FOLDER_ID_MSG, Category.CATEGORY_ERROR, 1025),
    /**
     * Folder "%1$s" must not be deleted by user "%2$s" in context "%3$s".
     * <p>
     * Folder identifier should be passed as first argument to not injure privacy through publishing folder name.
     */
    FOLDER_NOT_DELETEABLE(FolderExceptionMessages.FOLDER_NOT_DELETEABLE_MSG, Category.CATEGORY_PERMISSION_DENIED, 1026),
    /**
     * Folder "%1$s" must not be moved by user "%2$s" in context "%3$s".
     * <p>
     * Folder identifier should be passed as first argument to not injure privacy through publishing folder name.
     */
    FOLDER_NOT_MOVEABLE(FolderExceptionMessages.FOLDER_NOT_MOVEABLE_MSG, Category.CATEGORY_PERMISSION_DENIED, 1027),
    /**
     * A temporary error occurred. Please retry.
     */
    TEMPORARY_ERROR(FolderExceptionMessages.TEMPORARY_ERROR_MSG, Category.CATEGORY_ERROR, 1028),
    /**
     * User "%2$s" must not create subfolders below folder "%2$s" in context "%3$s".
     */
    NO_CREATE_SUBFOLDERS(FolderExceptionMessages.NO_CREATE_SUBFOLDERS_MSG, Category.CATEGORY_PERMISSION_DENIED, 1029),
    /**
     * No mail folder allowed below a public folder.
     */
    NO_PUBLIC_MAIL_FOLDER(FolderExceptionMessages.NO_PUBLIC_MAIL_FOLDER_MSG, Category.CATEGORY_PERMISSION_DENIED, 1030),
    /**
     * No such tree with identifier "%1$s".
     */
    TREE_NOT_FOUND(FolderExceptionMessages.TREE_NOT_FOUND_MSG, Category.CATEGORY_PERMISSION_DENIED, 1031),
    /**
     * A tree with identifier "%1$s" already exists.
     */
    DUPLICATE_TREE(FolderExceptionMessages.DUPLICATE_TREE_MSG, Category.CATEGORY_ERROR, 1032),
    /**
     * The folder name "%1$s" is reserved. Please choose another name.
     */
    RESERVED_NAME(FolderExceptionMessages.RESERVED_NAME_MSG, Category.CATEGORY_PERMISSION_DENIED, 1033),
    /**
     * Found two folders named "%1$s" located below the parent folder "%2$s". Please rename one of the folders. There should be no two folders with the same name.
     */
    DUPLICATE_NAME(FolderExceptionMessages.DUPLICATE_NAME_MSG, Category.CATEGORY_PERMISSION_DENIED, 1034),

    ;

    private static final String PREFIX = "FLD";

    /**
     * The prefix for this error codes.
     */
    public static String prefix() {
        return PREFIX;
    }

    private final Category category;

    private final int detailNumber;

    private final String message;

    private FolderExceptionErrorMessage(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
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
    public int getNumber() {
        return detailNumber;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @return The newly created {@link OXException} instance
     */
    public OXException create() {
        return specials(OXExceptionFactory.getInstance().create(this, new Object[0]));
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Object... args) {
        return specials(OXExceptionFactory.getInstance().create(this, (Throwable) null, args));
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Throwable cause, final Object... args) {
        return specials(OXExceptionFactory.getInstance().create(this, cause, args));
    }

	private OXException specials(final OXException exc) {
		switch(this) {
		case NOT_FOUND:
			exc.setGeneric(Generic.NOT_FOUND);
		}
		
		if (exc.getCategories().contains(Category.CATEGORY_CONFLICT)) {
			exc.setGeneric(Generic.CONFLICT);
		}
		
		if (exc.getCategories().contains(Category.CATEGORY_PERMISSION_DENIED)) {
			exc.setGeneric(Generic.NO_PERMISSION);
		}
		return exc;
	}

}
