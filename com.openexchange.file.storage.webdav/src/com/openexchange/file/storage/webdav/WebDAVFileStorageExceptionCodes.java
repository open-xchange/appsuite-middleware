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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.file.storage.webdav;

import com.openexchange.exceptions.OXErrorMessage;
import com.openexchange.file.storage.webdav.exception.WebDAVFileStorageExceptionFactory;
import com.openexchange.groupware.AbstractOXException.Category;

/**
 * {@link WebDAVFileStorageExceptionCodes} - Enumeration of all {@link WebDAVFileStorageException}s.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public enum WebDAVFileStorageExceptionCodes implements OXErrorMessage {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR(WebDAVFileStorageExceptionMessages.UNEXPECTED_ERROR_MSG, Category.CODE_ERROR, 1),
    /**
     * A HTTP error occurred: %1$s
     */
    HTTP_ERROR(WebDAVFileStorageExceptionMessages.HTTP_ERROR_MSG, Category.CODE_ERROR, 2),
    /**
     * A DAV error occurred: %1$s
     */
    DAV_ERROR(WebDAVFileStorageExceptionMessages.DAV_ERROR_MSG, Category.CODE_ERROR, 3),
    /**
     * The resource is not a directory: %1$s
     */
    NOT_A_FOLDER(WebDAVFileStorageExceptionMessages.NOT_A_FOLDER_MSG, Category.CODE_ERROR, 4),
    /**
     * Invalid property "%1$s". Should be "%2$s", but is not.
     */
    INVALID_PROPERTY(WebDAVFileStorageExceptionMessages.INVALID_PROPERTY_MSG, Category.CODE_ERROR, 5),
    /**
     * Invalid date property: %1$s
     */
    INVALID_DATE_PROPERTY(WebDAVFileStorageExceptionMessages.INVALID_DATE_PROPERTY_MSG, Category.CODE_ERROR, 6),
    /**
     * Directory "%1$s" must not be deleted.
     */
    DELETE_DENIED(WebDAVFileStorageExceptionMessages.DELETE_DENIED_MSG, Category.CODE_ERROR, 7),
    /**
     * Directory "%1$s" must not be updated.
     */
    UPDATE_DENIED(WebDAVFileStorageExceptionMessages.UPDATE_DENIED_MSG, Category.CODE_ERROR, 8),
    /**
     * Invalid or missing credentials to access WebDAV server "%1$s".
     */
    INVALID_CREDS(WebDAVFileStorageExceptionMessages.INVALID_CREDS_MSG, Category.CODE_ERROR, 9),
    /**
     * The resource is not a file: %1$s
     */
    NOT_A_FILE(WebDAVFileStorageExceptionMessages.NOT_A_FILE_MSG, Category.CODE_ERROR, 10),
    /**
     * Versioning not supported by WebDAV.
     */
    VERSIONING_NOT_SUPPORTED(WebDAVFileStorageExceptionMessages.VERSIONING_NOT_SUPPORTED_MSG, Category.CODE_ERROR, 11),
    /**
     * Missing file name.
     */
    MISSING_FILE_NAME(WebDAVFileStorageExceptionMessages.MISSING_FILE_NAME_MSG, Category.CODE_ERROR, 12);
    

    private final Category category;

    private final int detailNumber;

    private final String message;

    private WebDAVFileStorageExceptionCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
    }

    public Category getCategory() {
        return category;
    }

    public String getMessage() {
        return message;
    }

    public int getDetailNumber() {
        return detailNumber;
    }

    public String getHelp() {
        return null;
    }

    /**
     * Creates a new messaging exception of this error type with specified message arguments.
     * 
     * @param messageArgs The message arguments
     * @return A new twitter exception
     */
    public WebDAVFileStorageException create(final Object... messageArgs) {
        return WebDAVFileStorageExceptionFactory.getInstance().create(this, messageArgs);
    }

    /**
     * Creates a new messaging exception of this error type with specified cause and message arguments.
     * 
     * @param cause The cause
     * @param messageArgs The message arguments
     * @return A new twitter exception
     */
    public WebDAVFileStorageException create(final Throwable cause, final Object... messageArgs) {
        return WebDAVFileStorageExceptionFactory.getInstance().create(this, cause, messageArgs);
    }
}
