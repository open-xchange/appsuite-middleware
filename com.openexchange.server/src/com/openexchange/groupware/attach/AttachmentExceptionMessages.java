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

package com.openexchange.groupware.attach;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link AttachmentExceptionMessages}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class AttachmentExceptionMessages implements LocalizableStrings {

    public static final String OVER_LIMIT_MSG = "Attachment cannot be saved. File store limit is exceeded.";

    public static final String SQL_PROBLEM_MSG = "Invalid SQL Query: %s";

    public static final String SAVE_FAILED_MSG = "Could not save file to the file store.";

    public static final String FILE_MISSING_MSG = "Attachments must contain a file.";

    public static final String GENERATING_ID_FAILED_MSG = "Cannot generate ID for new attachment: %s";

    public static final String READ_FAILED_MSG = "Could not retrieve file: %s";

    public static final String ATTACHMENT_NOT_FOUND_MSG = "The attachment you requested no longer exists. Please refresh the view.";

    public static final String DELETE_FAILED_MSG = "Could not delete attachment.";

    public static final String ATTACHMENT_WITH_FILEID_NOT_FOUND_MSG = "Could not find an attachment with the file id %s. Either the file is orphaned or belongs to another module.";

    public static final String FILE_DELETE_FAILED_MSG = "Could not delete files from file store. Context: %d.";

    public static final String INVALID_CHARACTERS_MSG = "Validation failed: %s";

    public static final String SEARCH_PROBLEM_MSG = "An error occurred executing the search in the database.";

    public static final String FILESTORE_DOWN_MSG = "Unable to access the file store.";

    public static final String FILESTORE_WRITE_FAILED_MSG = "Writing to file store failed.";

    public static final String UNDONE_FAILED_MSG = "Changes done to the object this attachment was added to could not be undone. Your database is probably inconsistent, run the consistency tool.";

    public static final String ATTACH_FAILED_MSG = "An error occurred attaching to the given object.";

    public static final String DETACH_FAILED_MSG = "The object could not be detached because the update to an underlying object failed.";

    public static final String INVALID_REQUEST_PARAMETER_MSG = "Invalid parameter sent in request. Parameter '%1$s' was '%2$s' which does not look like a number.";

    public static final String SERVICE_CONFLICT_MSG = "Conflicting services registered for context %1$i and folder %2$i";

    private AttachmentExceptionMessages() {
        super();
    }
}
