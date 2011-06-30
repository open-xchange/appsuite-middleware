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

package com.openexchange.file.storage.cifs;

import com.openexchange.exceptions.OXErrorMessage;
import com.openexchange.file.storage.cifs.exception.CIFSExceptionFactory;
import com.openexchange.groupware.AbstractOXException.Category;

/**
 * {@link CIFSExceptionCodes} - Enumeration of all {@link CIFSException}s.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public enum CIFSExceptionCodes implements OXErrorMessage {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR(CIFSExceptionMessages.UNEXPECTED_ERROR_MSG, Category.CODE_ERROR, 1),
    /**
     * A CIFS/SMB error occurred: %1$s
     */
    SMB_ERROR(CIFSExceptionMessages.SMB_ERROR_MSG, Category.CODE_ERROR, 2),
    /**
     * Invalid CIFS/SMB URL: %1$s
     */
    INVALID_SMB_URL(CIFSExceptionMessages.INVALID_SMB_URL_MSG, Category.CODE_ERROR, 3),
    /**
     * CIFS/SMB URL does not denote a directory: %1$s
     */
    NOT_A_FOLDER(CIFSExceptionMessages.NOT_A_FOLDER_MSG, Category.CODE_ERROR, 4),
    /**
     * The CIFS/SMB resource does not exist: %1$s
     */
    NOT_FOUND(CIFSExceptionMessages.NOT_FOUND_MSG, Category.CODE_ERROR, 5),
    /**
     * Update denied for CIFS/SMB resource: %1$s
     */
    UPDATE_DENIED(CIFSExceptionMessages.UPDATE_DENIED_MSG, Category.CODE_ERROR, 6),
    /**
     * Delete denied for CIFS/SMB resource: %1$s
     */
    DELETE_DENIED(CIFSExceptionMessages.DELETE_DENIED_MSG, Category.CODE_ERROR, 7),
    /**
     * CIFS/SMB URL does not denote a file: %1$s
     */
    NOT_A_FILE(CIFSExceptionMessages.NOT_A_FILE_MSG, Category.CODE_ERROR, 8),
    /**
     * Missing file name.
     */
    MISSING_FILE_NAME(CIFSExceptionMessages.MISSING_FILE_NAME_MSG, Category.CODE_ERROR, 12),
    /**
     * Versioning not supported by CIFS/SMB file storage.
     */
    VERSIONING_NOT_SUPPORTED(CIFSExceptionMessages.VERSIONING_NOT_SUPPORTED_MSG, Category.CODE_ERROR, 13),
    
    
    
    ;
    

    private final Category category;

    private final int detailNumber;

    private final String message;

    private CIFSExceptionCodes(final String message, final Category category, final int detailNumber) {
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
    public CIFSException create(final Object... messageArgs) {
        return CIFSExceptionFactory.getInstance().create(this, messageArgs);
    }

    /**
     * Creates a new messaging exception of this error type with specified cause and message arguments.
     * 
     * @param cause The cause
     * @param messageArgs The message arguments
     * @return A new twitter exception
     */
    public CIFSException create(final Throwable cause, final Object... messageArgs) {
        return CIFSExceptionFactory.getInstance().create(this, cause, messageArgs);
    }
}
