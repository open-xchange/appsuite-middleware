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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link CIFSExceptionMessages} - Exception messages for {@link CIFSException} that needs to be translated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class CIFSExceptionMessages implements LocalizableStrings {

    // A CIFS/SMB error occurred.
    public static final String SMB_ERROR_MSG = "A CIFS/SMB error occurred.";

    // Invalid CIFS/SMB URL.
    public static final String INVALID_SMB_URL_MSG = "Invalid CIFS/SMB URL.";

    // CIFS/SMB URL does not denote a directory: %1$s
    public static final String NOT_A_FOLDER_MSG = "CIFS/SMB URL does not denote a directory: %1$s";

    // The CIFS/SMB resource does not exist: %1$s
    public static final String NOT_FOUND_MSG = "The CIFS/SMB resource does not exist: %1$s";

    // Update denied for CIFS/SMB resource: %1$s
    public static final String UPDATE_DENIED_MSG = "Update denied for CIFS/SMB resource: %1$s";

    // Delete denied for CIFS/SMB resource: %1$s
    public static final String DELETE_DENIED_MSG = "Delete denied for CIFS/SMB resource: %1$s";

    // CIFS/SMB URL does not denote a file: %1$s
    public static final String NOT_A_FILE_MSG = "CIFS/SMB URL does not denote a file: %1$s";

    // Missing file name.
    public static final String MISSING_FILE_NAME_MSG = "Missing file name.";

    // Versioning not supported by CIFS/SMB file storage.
    public static final String VERSIONING_NOT_SUPPORTED_MSG = "Versioning not supported by CIFS/SMB file storage.";

    // The operation completed successfully.
    public static final String NT_STATUS_OK = "The operation completed successfully.";

    // A device attached to the system is not functioning.
    public static final String NT_STATUS_UNSUCCESSFUL = "A device attached to the system is not functioning.";

    // Incorrect function.
    public static final String NT_STATUS_NOT_IMPLEMENTED = "Incorrect function.";

    // The parameter is incorrect.
    public static final String NT_STATUS_INVALID_INFO_CLASS = "The parameter is incorrect.";

    // Invalid access to memory location.
    public static final String NT_STATUS_ACCESS_VIOLATION = "Invalid access to memory location.";

    // The handle is invalid.
    public static final String NT_STATUS_INVALID_HANDLE = "The handle is invalid.";

    // The parameter is incorrect.
    public static final String NT_STATUS_INVALID_PARAMETER = "The parameter is incorrect.";

    // The system cannot find the file specified.
    public static final String NT_STATUS_NO_SUCH_DEVICE = "The system cannot find the file specified.";

    // The system cannot find the file specified.
    public static final String NT_STATUS_NO_SUCH_FILE = "The system cannot find the file specified.";

    // More data is available.
    public static final String NT_STATUS_MORE_PROCESSING_REQUIRED = "More data is available.";

    // Access is denied.
    public static final String NT_STATUS_ACCESS_DENIED = "Access is denied.";

    // The data area passed to a system call is too small.
    public static final String NT_STATUS_BUFFER_TOO_SMALL = "The data area passed to a system call is too small.";

    // The filename, directory name, or volume label syntax is incorrect.
    public static final String NT_STATUS_OBJECT_NAME_INVALID = "The filename, directory name, or volume label syntax is incorrect.";

    // The system cannot find the file specified.
    public static final String NT_STATUS_OBJECT_NAME_NOT_FOUND = "The system cannot find the file specified.";

    // Cannot create a file when that file already exists.
    public static final String NT_STATUS_OBJECT_NAME_COLLISION = "Cannot create a file when that file already exists.";

    // The handle is invalid.
    public static final String NT_STATUS_PORT_DISCONNECTED = "The handle is invalid.";

    // The specified path is invalid.
    public static final String NT_STATUS_OBJECT_PATH_INVALID = "The specified path is invalid.";

    // The system cannot find the path specified.
    public static final String NT_STATUS_OBJECT_PATH_NOT_FOUND = "The system cannot find the path specified.";

    // The specified path is invalid.
    public static final String NT_STATUS_OBJECT_PATH_SYNTAX_BAD = "The specified path is invalid.";

    // The process cannot access the file because it is being used by another process.
    public static final String NT_STATUS_SHARING_VIOLATION = "The process cannot access the file because it is being used by another process.";

    // Access is denied.
    public static final String NT_STATUS_DELETE_PENDING = "Access is denied.";

    // There are currently no logon servers available to service the logon request.
    public static final String NT_STATUS_NO_LOGON_SERVERS = "There are currently no logon servers available to service the logon request.";

    // The specified user already exists.
    public static final String NT_STATUS_USER_EXISTS = "The specified user already exists.";

    // The specified user does not exist.
    public static final String NT_STATUS_NO_SUCH_USER = "The specified user does not exist.";

    // The specified network password is not correct.
    public static final String NT_STATUS_WRONG_PASSWORD = "The specified network password is not correct.";

    // Logon failure: unknown user name or bad password.
    public static final String NT_STATUS_LOGON_FAILURE = "Logon failure: unknown user name or bad password.";

    // Logon failure: user account restriction.
    public static final String NT_STATUS_ACCOUNT_RESTRICTION = "Logon failure: user account restriction.";

    // Logon failure: account logon time restriction violation.
    public static final String NT_STATUS_INVALID_LOGON_HOURS = "Logon failure: account logon time restriction violation.";

    // Logon failure: user not allowed to log on to this computer.
    public static final String NT_STATUS_INVALID_WORKSTATION = "Logon failure: user not allowed to log on to this computer.";

    // Logon failure: the specified account password has expired.
    public static final String NT_STATUS_PASSWORD_EXPIRED = "Logon failure: the specified account password has expired.";

    // Logon failure: account currently disabled.
    public static final String NT_STATUS_ACCOUNT_DISABLED = "Logon failure: account currently disabled.";

    // No mapping between account names and security IDs was done.
    public static final String NT_STATUS_NONE_MAPPED = "No mapping between account names and security IDs was done.";

    // All pipe instances are busy.
    public static final String NT_STATUS_PIPE_BUSY = "All pipe instances are busy.";

    // Waiting for a process to open the other end of the pipe.
    public static final String NT_STATUS_PIPE_LISTENING = "Waiting for a process to open the other end of the pipe.";

    // Access is denied.
    public static final String NT_STATUS_FILE_IS_A_DIRECTORY = "Access is denied.";

    // A duplicate name exists on the network.
    public static final String NT_STATUS_DUPLICATE_NAME = "A duplicate name exists on the network.";

    // The specified network name is no longer available.
    public static final String NT_STATUS_NETWORK_NAME_DELETED = "The specified network name is no longer available.";

    // Network access is denied.
    public static final String NT_STATUS_NETWORK_ACCESS_DENIED = "Network access is denied.";

    // The network name cannot be found.
    public static final String NT_STATUS_BAD_NETWORK_NAME = "The network name cannot be found.";

    // No more connections can be made to this remote computer at this time because there are already as many connections as the computer can accept.
    public static final String NT_STATUS_REQUEST_NOT_ACCEPTED = "No more connections can be made to this remote computer at this time because there are already as many connections as the computer can accept.";

    // Indicates a Windows NT Server could not be contacted or that objects within the domain are protected such that necessary information could not be retrieved.
    public static final String NT_STATUS_CANT_ACCESS_DOMAIN_INFO = "Indicates a Windows NT Server could not be contacted or that objects within the domain are protected such that necessary information could not be retrieved.";

    // The specified domain did not exist.
    public static final String NT_STATUS_NO_SUCH_DOMAIN = "The specified domain did not exist.";

    // The directory name is invalid.
    public static final String NT_STATUS_NOT_A_DIRECTORY = "The directory name is invalid.";

    // Access is denied.
    public static final String NT_STATUS_CANNOT_DELETE = "Access is denied.";

    // The format of the specified computer name is invalid.
    public static final String NT_STATUS_INVALID_COMPUTER_NAME = "The format of the specified computer name is invalid.";

    // The specified local group does not exist.
    public static final String NT_STATUS_NO_SUCH_ALIAS = "The specified local group does not exist.";

    // Logon failure: the user has not been granted the requested logon type at this computer.
    public static final String NT_STATUS_LOGON_TYPE_NOT_GRANTED = "Logon failure: the user has not been granted the requested logon type at this computer.";

    // The account used is a Computer Account. Use your global user account or local user account to access this server.
    public static final String NT_STATUS_NOLOGON_WORKSTATION_TRUST_ACCOUNT = "The account used is a Computer Account. Use your global user account or local user account to access this server.";

    // The user must change his password before he logs on the first time.
    public static final String NT_STATUS_PASSWORD_MUST_CHANGE = "The user must change his password before he logs on the first time.";

    // Please use a hostname instead of an IP address in the SMB URL.
    public static final String NT_STATUS_NOT_FOUND = "Please use a hostname instead of an IP address in the SMB URL.";

    // Your account has been blocked. This can have various reasons like having mistyped the password several times.
    // Please contact your system administrator or hoster in case you can no longer log in.
    public static final String NT_STATUS_ACCOUNT_LOCKED_OUT = "Your account has been blocked. This can have various reasons like having mistyped the password several times.\nPlease contact your system administrator or hoster in case you can no longer log in.";

    /**
     * Initializes a new {@link CIFSExceptionMessages}.
     */
    private CIFSExceptionMessages() {
        super();
    }

}
