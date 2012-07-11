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

package com.openexchange.file.storage.webdav;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link WebDAVFileStorageExceptionCodes} - Enumeration of all {@link OXException}s.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public enum WebDAVFileStorageExceptionCodes implements OXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR(WebDAVFileStorageExceptionMessages.UNEXPECTED_ERROR_MSG, CATEGORY_ERROR, 1),
    /**
     * A HTTP error occurred: %1$s
     */
    HTTP_ERROR(WebDAVFileStorageExceptionMessages.HTTP_ERROR_MSG, CATEGORY_ERROR, 2),
    /**
     * A DAV error occurred: %1$s
     */
    DAV_ERROR(WebDAVFileStorageExceptionMessages.DAV_ERROR_MSG, CATEGORY_ERROR, 3),
    /**
     * The resource is not a directory: %1$s
     */
    NOT_A_FOLDER(WebDAVFileStorageExceptionMessages.NOT_A_FOLDER_MSG, CATEGORY_ERROR, 4),
    /**
     * Invalid property "%1$s". Should be "%2$s".
     */
    INVALID_PROPERTY(WebDAVFileStorageExceptionMessages.INVALID_PROPERTY_MSG, CATEGORY_ERROR, 5),
    /**
     * Invalid date property: %1$s
     */
    INVALID_DATE_PROPERTY(WebDAVFileStorageExceptionMessages.INVALID_DATE_PROPERTY_MSG, CATEGORY_ERROR, 6),
    /**
     * Directory "%1$s" must not be deleted.
     */
    DELETE_DENIED(WebDAVFileStorageExceptionMessages.DELETE_DENIED_MSG, CATEGORY_ERROR, 7),
    /**
     * Directory "%1$s" must not be updated.
     */
    UPDATE_DENIED(WebDAVFileStorageExceptionMessages.UPDATE_DENIED_MSG, CATEGORY_ERROR, 8),
    /**
     * Invalid or missing credentials to access WebDAV server "%1$s".
     */
    INVALID_CREDS(WebDAVFileStorageExceptionMessages.INVALID_CREDS_MSG, CATEGORY_ERROR, 9),
    /**
     * The resource is not a file: %1$s
     */
    NOT_A_FILE(WebDAVFileStorageExceptionMessages.NOT_A_FILE_MSG, CATEGORY_ERROR, 10),
    /**
     * Versioning not supported by WebDAV.
     */
    VERSIONING_NOT_SUPPORTED(WebDAVFileStorageExceptionMessages.VERSIONING_NOT_SUPPORTED_MSG, CATEGORY_ERROR, 11),
    /**
     * Missing file name.
     */
    MISSING_FILE_NAME(WebDAVFileStorageExceptionMessages.MISSING_FILE_NAME_MSG, CATEGORY_ERROR, 12);


    private final Category category;

    private final int detailNumber;

    private final String message;

    private WebDAVFileStorageExceptionCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
    }

    @Override
    public String getPrefix() {
        return "WEBDAV_FILE_STORAGE";
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
        return detailNumber;
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
    public OXException create(final Throwable cause, final Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }
}
