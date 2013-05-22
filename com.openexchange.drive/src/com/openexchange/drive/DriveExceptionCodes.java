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

package com.openexchange.drive;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link DriveExceptionCodes}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public enum DriveExceptionCodes implements OXExceptionCode {

    /** The directory path \"%1$s\" is invalid */
    INVALID_PATH(DriveExceptionMessages.INVALID_PATH_MSG, Category.CATEGORY_USER_INPUT, 1),

    /** The directory path \"%1$s\" was not found */
    PATH_NOT_FOUND(DriveExceptionMessages.PATH_NOT_FOUND_MSG, Category.CATEGORY_ERROR, 2),

    /** The file \"%1$s\" was not found at \"%2$s\" */
    FILE_NOT_FOUND(DriveExceptionMessages.FILE_NOT_FOUND_MSG, Category.CATEGORY_ERROR, 3),

    /** An I/O error occurred: \"%1$s\" */
    IO_ERROR(DriveExceptionMessages.IO_ERROR_MSG, Category.CATEGORY_ERROR, 4),

    /** The file offset \"%1$d\" is invalid */
    INVALID_FILE_OFFSET(DriveExceptionMessages.INVALID_FILE_OFFSET_MSG, Category.CATEGORY_USER_INPUT, 5),

    /** Unexpected database error: \"%1$s\" */
    DB_ERROR(DriveExceptionMessages.DB_ERROR_MSG, Category.CATEGORY_ERROR, 6),

    /** The file \"%1$s\" with checksum \"%2$s\" was not found at \"%3$s\" */
    FILEVERSION_NOT_FOUND(DriveExceptionMessages.FILEVERSION_NOT_FOUND_MSG, Category.CATEGORY_ERROR, 7),

    /** No checksum for file \"%1$s\" available */
    NO_CHECKSUM_FOR_FILE(DriveExceptionMessages.NO_CHECKSUM_FOR_FILE_MSG, Category.CATEGORY_ERROR, 8),

    /** "Checksum \"%1$s\" for uploaded file \"%2$s\" different from \"%3$s\"" */
    UPLOADED_FILE_CHECKSUM_ERROR(DriveExceptionMessages.UPLOADED_FILE_CHECKSUM_ERROR_MSG, Category.CATEGORY_ERROR, 9),

    /** The directory \"%1$s\" with checksum \"%2$s\" was not found" */
    DIRECTORYVERSION_NOT_FOUND(DriveExceptionMessages.DIRECTORYVERSION_NOT_FOUND_MSG, Category.CATEGORY_ERROR, 10),

    ;

    private static final String PREFIX = "DRV";

    private final Category category;
    private final int number;
    private final String message;

    private DriveExceptionCodes(String message, Category category, int detailNumber) {
        this.message = message;
        number = detailNumber;
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
    public String getMessage() {
        return message;
    }

    @Override
    public int getNumber() {
        return number;
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
        return OXExceptionFactory.getInstance().create(this, new Object[0]);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Object... args) {
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(Throwable cause, Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }

}
