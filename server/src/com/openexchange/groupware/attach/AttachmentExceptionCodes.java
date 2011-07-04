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

package com.openexchange.groupware.attach;

import static com.openexchange.groupware.attach.AttachmentExceptionMessages.ATTACHMENT_NOT_FOUND_MSG;
import static com.openexchange.groupware.attach.AttachmentExceptionMessages.ATTACHMENT_WITH_FILEID_NOT_FOUND_MSG;
import static com.openexchange.groupware.attach.AttachmentExceptionMessages.ATTACH_FAILED_MSG;
import static com.openexchange.groupware.attach.AttachmentExceptionMessages.DELETE_FAILED_MSG;
import static com.openexchange.groupware.attach.AttachmentExceptionMessages.DETACH_FAILED_MSG;
import static com.openexchange.groupware.attach.AttachmentExceptionMessages.FILESTORE_DOWN_MSG;
import static com.openexchange.groupware.attach.AttachmentExceptionMessages.FILESTORE_WRITE_FAILED_MSG;
import static com.openexchange.groupware.attach.AttachmentExceptionMessages.FILE_DELETE_FAILED_MSG;
import static com.openexchange.groupware.attach.AttachmentExceptionMessages.FILE_MISSING_MSG;
import static com.openexchange.groupware.attach.AttachmentExceptionMessages.GENERATING_ID_FAILED_MSG;
import static com.openexchange.groupware.attach.AttachmentExceptionMessages.INVALID_CHARACTERS_MSG;
import static com.openexchange.groupware.attach.AttachmentExceptionMessages.INVALID_REQUEST_PARAMETER_MSG;
import static com.openexchange.groupware.attach.AttachmentExceptionMessages.READ_FAILED_MSG;
import static com.openexchange.groupware.attach.AttachmentExceptionMessages.SAVE_FAILED_MSG;
import static com.openexchange.groupware.attach.AttachmentExceptionMessages.SEARCH_PROBLEM_MSG;
import static com.openexchange.groupware.attach.AttachmentExceptionMessages.SERVICE_CONFLICT_MSG;
import static com.openexchange.groupware.attach.AttachmentExceptionMessages.SQL_PROBLEM_MSG;
import static com.openexchange.groupware.attach.AttachmentExceptionMessages.UNDONE_FAILED_MSG;
import com.openexchange.exceptions.OXErrorMessage;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.attach.impl.AttachmentExceptionFactory;

/**
 * {@link AttachmentExceptionCodes}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public enum AttachmentExceptionCodes implements OXErrorMessage {

    /** Invalid SQL Query: %s */
    SQL_PROBLEM(SQL_PROBLEM_MSG, Category.CODE_ERROR, 100),
    /** Could not save file to the file store. */
    SAVE_FAILED(SAVE_FAILED_MSG, Category.SUBSYSTEM_OR_SERVICE_DOWN, 400),
    /** Attachments must contain a file. */
    FILE_MISSING(FILE_MISSING_MSG, Category.USER_INPUT, 401),
    /** Cannot generate ID for new attachment: %s */
    GENERATIING_ID_FAILED(GENERATING_ID_FAILED_MSG, Category.CODE_ERROR, 402),
    /** Could not retrieve file: %s */
    READ_FAILED(READ_FAILED_MSG, Category.SUBSYSTEM_OR_SERVICE_DOWN, 404),
    /** The attachment you requested no longer exists. Please refresh the view. */
    ATTACHMENT_NOT_FOUND(ATTACHMENT_NOT_FOUND_MSG, Category.USER_INPUT, 405),
    /** Could not delete attachment. */
    DELETE_FAILED(DELETE_FAILED_MSG, Category.CODE_ERROR, 407),
    /** Could not find an attachment with the file_id %s. Either the file is orphaned or belongs to another module. */
    ATTACHMENT_WITH_FILEID_NOT_FOUND(ATTACHMENT_WITH_FILEID_NOT_FOUND_MSG, Category.CODE_ERROR, 408),
    /** Could not delete files from filestore. Context: %d. */
    FILE_DELETE_FAILED(FILE_DELETE_FAILED_MSG, Category.SUBSYSTEM_OR_SERVICE_DOWN, 416),
    /** Validation failed: %s */
    INVALID_CHARACTERS(INVALID_CHARACTERS_MSG, Category.USER_INPUT, 418),
    /** An error occurred executing the search in the database. */
    SEARCH_PROBLEM(SEARCH_PROBLEM_MSG, Category.CODE_ERROR, 420),
    /** Unable to access the filestore. */
    FILESTORE_DOWN(FILESTORE_DOWN_MSG, Category.SUBSYSTEM_OR_SERVICE_DOWN, 421),
    /** Writing to filestore failed. */
    FILESTORE_WRITE_FAILED(FILESTORE_WRITE_FAILED_MSG, Category.SUBSYSTEM_OR_SERVICE_DOWN, 422),
    /** Changes done to the object this attachment was added to could not be undone. Your database is probably inconsistent, run the consistency tool. */
    UNDONE_FAILED(UNDONE_FAILED_MSG, Category.CODE_ERROR, 600),
    /** An error occurred attaching to the given object. */
    ATTACH_FAILED(ATTACH_FAILED_MSG, Category.CODE_ERROR, 601),
    /** The Object could not be detached because the update to an underlying object failed. */
    DETACH_FAILED(DETACH_FAILED_MSG, Category.CODE_ERROR, 602),
    /** Invalid parameter sent in request. Parameter '%1$s' was '%2$s' which does not look like a number. */
    INVALID_REQUEST_PARAMETER(INVALID_REQUEST_PARAMETER_MSG, Category.USER_INPUT, 701),
    /** Conflicting services registered for context %1$i and folder %2$i */
    SERVICE_CONFLICT(SERVICE_CONFLICT_MSG, Category.SETUP_ERROR, 900),
    ;

    private final String message;
    private final Category category;
    private final int number;

    private AttachmentExceptionCodes(String message, Category category, int number) {
        this.message = message;
        this.category = category;
        this.number = number;
    }

    public int getDetailNumber() {
        return number;
    }

    public String getMessage() {
        return message;
    }

    public String getHelp() {
        // TODO Auto-generated method stub
        return null;
    }

    public Category getCategory() {
        return category;
    }

    public AttachmentException create(Object... args) {
        return AttachmentExceptionFactory.getInstance().create(this, args);
    }

    public AttachmentException create(Throwable cause, Object... args) {
        return AttachmentExceptionFactory.getInstance().create(this, cause, args);
    }
}
