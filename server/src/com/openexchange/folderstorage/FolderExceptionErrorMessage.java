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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import com.openexchange.exceptions.OXErrorMessage;
import com.openexchange.groupware.AbstractOXException.Category;

/**
 * {@link FolderExceptionErrorMessage} - Error messages for folder exceptions.
 * <p>
 * Subclasses are supposed to start at <code>1000</code>.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum FolderExceptionErrorMessage implements OXErrorMessage {

    /**
     * Unexpected error: %1$s
     */
    UNEXPECTED_ERROR(FolderExceptionMessages.UNEXPECTED_ERROR_MSG, Category.CODE_ERROR, 1),
    /**
     * I/O error: %1$s
     */
    IO_ERROR(FolderExceptionMessages.IO_ERROR_MSG, Category.CODE_ERROR, 2),
    /**
     * SQL error: %1$s
     */
    SQL_ERROR(FolderExceptionMessages.SQL_ERROR_MSG, Category.CODE_ERROR, 3),
    /**
     * No appropriate folder storage for tree identifier "%1$s" and folder identifier "%2$s".
     */
    NO_STORAGE_FOR_ID(FolderExceptionMessages.NO_STORAGE_FOR_ID_MSG, Category.CODE_ERROR, 4),
    /**
     * No appropriate folder storage for tree identifier "%1$s" and content type "%2$s".
     */
    NO_STORAGE_FOR_CT(FolderExceptionMessages.NO_STORAGE_FOR_CT_MSG, Category.CODE_ERROR, 5),
    /**
     * Missing session.
     */
    MISSING_SESSION(FolderExceptionMessages.MISSING_SESSION_MSG, Category.CODE_ERROR, 6),
    /**
     * Folder "%1$s" is not visible to user "%2$s" in context "%3$s"
     * <p>
     * Folder identifier should be passed as first argument to not injure privacy through publishing folder name.
     */
    FOLDER_NOT_VISIBLE(FolderExceptionMessages.FOLDER_NOT_VISIBLE_MSG, Category.PERMISSION, 7),
    /**
     * JSON error: %1$s
     */
    JSON_ERROR(FolderExceptionMessages.JSON_ERROR_MSG, Category.CODE_ERROR, 8),
    /**
     * Missing tree identifier.
     */
    MISSING_TREE_ID(FolderExceptionMessages.MISSING_TREE_ID_MSG, Category.CODE_ERROR, 9),
    /**
     * Missing parent folder identifier.
     */
    MISSING_PARENT_ID(FolderExceptionMessages.MISSING_PARENT_ID_MSG, Category.CODE_ERROR, 10),
    /**
     * Missing folder identifier.
     */
    MISSING_FOLDER_ID(FolderExceptionMessages.MISSING_FOLDER_ID_MSG, Category.CODE_ERROR, 11),
    /**
     * Parent folder "%1$s" does not allow folder content type "%2$s" in tree "%3$s" for user %4$s in context %5$s.
     */
    INVALID_CONTENT_TYPE(FolderExceptionMessages.INVALID_CONTENT_TYPE_MSG, Category.CODE_ERROR, 12),
    /**
     * Move operation not permitted.
     */
    MOVE_NOT_PERMITTED(FolderExceptionMessages.MOVE_NOT_PERMITTED_MSG, Category.CODE_ERROR, 13),
    /**
     * A folder named "%1$s" already exists below parent folder "%2$s" in tree "%3$s".
     */
    EQUAL_NAME(FolderExceptionMessages.EQUAL_NAME_MSG, Category.PERMISSION, 14),
    /**
     * Subscribe operation not permitted on tree "%1$s".
     */
    NO_REAL_SUBSCRIBE(FolderExceptionMessages.NO_REAL_SUBSCRIBE_MSG, Category.PERMISSION, 15),
    /**
     * Un-Subscribe operation not permitted on tree "%1$s".
     */
    NO_REAL_UNSUBSCRIBE(FolderExceptionMessages.NO_REAL_UNSUBSCRIBE_MSG, Category.PERMISSION, 16),
    /**
     * Un-Subscribe operation not permitted on folder "%1$s" in tree "%2$s".
     */
    NO_UNSUBSCRIBE(FolderExceptionMessages.NO_UNSUBSCRIBE_MSG, Category.PERMISSION, 17),
    /**
     * Unknown content type: %1$s.
     */
    UNKNOWN_CONTENT_TYPE(FolderExceptionMessages.UNKNOWN_CONTENT_TYPE_MSG, Category.CODE_ERROR, 18),
    /**
     * Missing parameter: %1$s.
     */
    MISSING_PARAMETER(FolderExceptionMessages.MISSING_PARAMETER_MSG, Category.CODE_ERROR, 19),
    /**
     * Unsupported storage type: %1$s.
     */
    UNSUPPORTED_STORAGE_TYPE(FolderExceptionMessages.UNSUPPORTED_STORAGE_TYPE_MSG, Category.CODE_ERROR, 20),
    /**
     * Missing property: %1$s.
     */
    MISSING_PROPERTY(FolderExceptionMessages.MISSING_PROPERTY_MSG, Category.CODE_ERROR, 21),
    /**
     * The object has been changed in the meantime.
     */
    CONCURRENT_MODIFICATION(FolderExceptionMessages.CONCURRENT_MODIFICATION_MSG, Category.CONCURRENT_MODIFICATION, 22);

    private final Category category;

    private final int detailNumber;

    private final String message;

    private FolderExceptionErrorMessage(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
    }

    public Category getCategory() {
        return category;
    }

    public int getDetailNumber() {
        return detailNumber;
    }

    public String getHelp() {
        return null;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Creates a {@link FolderException} carrying this error message.
     * 
     * @return A newly created {@link FolderException} carrying this error message.
     */
    public FolderException create() {
        return FolderExceptionFactory.getInstance().create(this, null, new Object[0]);
    }

    /**
     * Creates a {@link FolderException} carrying this error message.
     * 
     * @param messageArguments The (optional) message arguments; pass <code>null</code> to ignore
     * @return A newly created {@link FolderException} carrying this error message.
     */
    public FolderException create(final Object... messageArguments) {
        return FolderExceptionFactory.getInstance().create(this, null, messageArguments);
    }

    /**
     * Creates a {@link FolderException} carrying this error message.
     * 
     * @param cause The (optional) cause; pass <code>null</code> to ignore
     * @param messageArguments The (optional) message arguments; pass <code>null</code> to ignore
     * @return A newly created {@link FolderException} carrying this error message.
     */
    public FolderException create(final Throwable cause, final Object... messageArguments) {
        return FolderExceptionFactory.getInstance().create(this, cause, messageArguments);
    }

}
