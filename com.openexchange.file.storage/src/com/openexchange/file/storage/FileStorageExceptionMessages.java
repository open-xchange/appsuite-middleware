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

package com.openexchange.file.storage;

import com.openexchange.exception.OXException;
import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link OXExceptionMessages} - Exception messages for {@link OXException} that needs to be translated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since Open-Xchange v6.18.2
 */
public final class FileStorageExceptionMessages implements LocalizableStrings {

    // The folder you requested does not exist.
    public static final String FOLDER_NOT_EXISTS_MSG = "The folder you requested does not exist.";

    // The file you requested does not exist.
    public static final String FILE_NOT_EXISTS_MSG = "The file you requested does not exist.";

    // The file version you requested does not exist.
    public static final String FILE_VERSION_NOT_EXISTS_MSG = "The file version you requested does not exist.";

    // A folder named "%1$s" already exists below the parent folder "%2$s".
    public static final String DUPLICATE_FOLDER_MSG = "A folder named \"%1$s\" already exists below the parent folder \"%2$s\".";

    // You do not have the appropriate permissions to create a subfolder below the folder "%1$s".
    public static final String NO_CREATE_ACCESS_MSG = "You do not have the appropriate permissions to create a subfolder below the folder \"%1$s\".";

    // In order to accomplish the search, %1$d or more characters are required.
    public static final String PATTERN_NEEDS_MORE_CHARACTERS_MSG = "In order to accomplish the search, %1$d or more characters are required.";

    // Invalid URL \"%1$s\". Please correct the value and try again.
    public static final String INVALID_URL_MSG = "Invalid URL \"%1$s\". Please correct the value and try again.";

    // The allowed quota is reached. Please delete some items in order to store new ones.
    public static final String QUOTA_REACHED_MSG = "The allowed quota is reached. Please delete some items in order to store new ones.";

    // ZIP archive exceeds max. allowed size of %1$s
    public static final String ARCHIVE_MAX_SIZE_EXCEEDED_MSG = "ZIP archive exceeds max. allowed size of %1$s";

    // The file storage account is currently not accessible.
    public static final String ACCOUNT_NOT_ACCESSIBLE_MSG = "The file storage account is currently not accessible.";

    // The folder \"%1$s\" cannot be moved to \"%2$s\".
    public static final String FOLDER_MOVE_NOT_SUPPORTED_MSG = "The folder \"%1$s\" cannot be moved to \"%2$s\".";

    // The file \"%1$s\" cannot be moved to \"%2$s\".
    public static final String FILE_MOVE_NOT_SUPPORTED_MSG = "The file \"%1$s\" cannot be moved to \"%2$s\".";

    // The notes of the file \"%1$s\" in folder \"%2$s\" are lost when moving it into the account \"%3$s\".
    public static final String LOSS_OF_NOTES_MSG = "The notes of the file \"%1$s\" in folder \"%2$s\" are lost when moving it into the account \"%3$s\".";

    // All previous versions of the file \"%1$s\" in folder \"%2$s\" are lost when moving it into the account \"%3$s\".
    public static final String LOSS_OF_VERSIONS_MSG = "All previous versions of the file \"%1$s\" in folder \"%2$s\" are lost when moving it into the account \"%3$s\".";

    // All previous versions of the file \"%1$s\" in folder \"%2$s\" are lost when moving it into the account \"%3$s\".
    public static final String LOSS_OF_CATEGORIES_MSG = "The assigned categories of the file \"%1$s\" in folder \"%2$s\" are lost when moving it into the account \"%3$s\".";

    // The shared file \"%1$s\" in folder \"%2$s\" is no longer accessible by other users when moving it into the account \"%3$s\".
    public static final String LOSS_OF_FILE_SHARES_MSG = "The shared file \"%1$s\" in folder \"%2$s\" is no longer accessible by other users when moving it into the account \"%3$s\".";

    // The shared folder \"%1$s\" is no longer accessible by other users when moving it into the account \"%2$s\".
    public static final String LOSS_OF_FOLDER_SHARES_MSG = "The shared folder \"%1$s\" is no longer accessible by other users when moving it into the account \"%2$s\".";

    // The file \"%1$s\" was not updated due to possible data loss. Please review the warnings for details.
    public static final String FILE_UPDATE_ABORTED_MSG = "The file \"%1$s\" was not updated due to possible data loss. Please review the warnings for details.";

    // The %1$s URL does not denote a file: %2$s
    public static final String NOT_A_FILE_MSG = "The %1$s URL does not denote a file: %1$s";

    // The %1$s URL does not denote a directory: %2$s
    public static final String NOT_A_FOLDER_MSG = "The %1$s URL does not denote a directory: %2$s";

    // Missing file name.
    public static final String MISSING_FILE_NAME_MSG = "Missing file name.";

    // Update denied for %1$s resource: %2$s
    public static final String UPDATE_DENIED_MSG = "Update denied for \"%1$s\" resource: %2$s";

    // Delete denied for \"%1$s\" resource: %2$s
    public static final String DELETE_DENIED_MSG = "Delete denied for \"%1$s\" resource: %2$s";

    // Missing configuration for %1$s account "%2$s".
    // The first placeholder is filled with the name of file storage provider (e.g. "boxcom"), the latter one is filled with numeric identifier of the account.
    // E.g. 'Missing configuration for boxcom account "3".'
    public static final String MISSING_CONFIG_MSG = "Missing configuration for %1$s account \"%2$s\".";

    // The provided %1$s resource does not exist: %2$s
    public static final String NOT_FOUND_MSG = "The provided %1$s resource does not exist: %2$s";

    // The file %1$s doesn't have any content.
    public static final String NO_CONTENT_MSG = "The file %1$s doesn't have any content.";

    // The associated %2$s account does no more exist
    public static final String ACCOUNT_NOT_FOUND_MSG = "The associated %2$s account does no more exist";

    // Individual permissions are not supported in account \"%1$s\".
    public static final String NO_PERMISSION_SUPPORT_MSG = "Individual permissions are not supported in account \"%1$s\".";

    // Saving notes for file \"%1$s\" is not supported in account \"%2$s\".
    public static final String NO_NOTES_SUPPORT_MSG = "Saving notes for file \"%1$s\" is not supported in account \"%2$s\".";

    // Assigning categories to file \"%1$s\" is not supported in account \"%2$s\".
    public static final String NO_CATEGORIES_SUPPORT_MSG = "Assigning categories to file \"%1$s\" is not supported in account \"%2$s\".";

    // The file \"%1$s\" was not saved due to possible data loss. Please review the warnings for details.
    public static final String FILE_SAVE_ABORTED_MSG = "The file \"%1$s\" was not saved due to possible data loss. Please review the warnings for details.";

    // Bad or expired access token. Need to re-authenticate user.
    public static final String UNLINKED_ERROR_MSG = "Bad or expired access token. Need to re-authenticate user.";

    // Your rate limit has been exceeded and further access is not possible at the moment.
    public static final String STORAGE_RATE_LIMIT_MSG = "Your rate limit has been exceeded and further access is not possible at the moment.";

    // You need to get sufficient permissions to perform the operation on the specified entity.
    public static final String INVALID_OBJECT_PERMISSIONS_SIMPLE_MSG = "You need to get sufficient permissions to perform the operation on the specified entity.";

    // File name contains not allowed characters: \"%1$s\"
    public static final String ILLEGAL_CHARACTERS_MSG = "File name contains illegal characters: \"%1$s\"";

    // File name is a reserved name: \"%1$s\"
    public static final String RESERVED_NAME_MSG = "File name is a reserved name: \"%1$s\"";

    // File name must not be \".\" or \"..\".
    public static final String ONLY_DOTS_MSG = "File name must not be \".\" or \"..\".";

    // File name must not end with a dot or whitespace.
    public static final String WHITESPACE_END_MSG = "File name must not end with a dot or whitespace.";
    
    // A file with that name already exists.
    public static final String FILE_ALREADY_EXISTS = "A file with that name already exists.";

    /**
     * Initializes a new {@link OXExceptionMessages}.
     */
    private FileStorageExceptionMessages() {
        super();
    }

}
