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

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import jcifs.smb.SmbException;
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link CIFSExceptionCodes} - Enumeration of all {@link CIFSException}s.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public enum CIFSExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR("An error occurred: %1$s", CATEGORY_ERROR, null),
    /**
     * A CIFS/SMB error occurred: %1$s
     */
    SMB_ERROR("A CIFS/SMB error occurred: %1$s", CATEGORY_USER_INPUT, CIFSExceptionMessages.SMB_ERROR_MSG),
    /**
     * A CIFS/SMB error occurred: %1$s (status=%2$s)
     */
    SMB_ERROR_WITH_STATUS("A CIFS/SMB error occurred: %1$s (status=%2$s)", CATEGORY_USER_INPUT, CIFSExceptionMessages.SMB_ERROR_MSG),
    /**
     * Invalid CIFS/SMB URL: %1$s
     */
    INVALID_SMB_URL("Invalid CIFS/SMB URL: %1$s", CATEGORY_USER_INPUT, CIFSExceptionMessages.INVALID_SMB_URL_MSG),
    /**
     * CIFS/SMB URL does not denote a directory: %1$s
     */
    NOT_A_FOLDER("CIFS/SMB URL does not denote a directory: %1$s", CATEGORY_USER_INPUT, CIFSExceptionMessages.NOT_A_FOLDER_MSG),
    /**
     * The CIFS/SMB resource does not exist: %1$s
     */
    NOT_FOUND("The CIFS/SMB resource does not exist: %1$s", CATEGORY_USER_INPUT, CIFSExceptionMessages.NOT_FOUND_MSG),
    /**
     * Update denied for CIFS/SMB resource: %1$s
     */
    UPDATE_DENIED("Update denied for CIFS/SMB resource: %1$s", CATEGORY_USER_INPUT, CIFSExceptionMessages.UPDATE_DENIED_MSG),
    /**
     * Delete denied for CIFS/SMB resource: %1$s
     */
    DELETE_DENIED("Delete denied for CIFS/SMB resource: %1$s", CATEGORY_USER_INPUT, CIFSExceptionMessages.DELETE_DENIED_MSG),
    /**
     * CIFS/SMB URL does not denote a file: %1$s
     */
    NOT_A_FILE("CIFS/SMB URL does not denote a file: %1$s", CATEGORY_USER_INPUT, CIFSExceptionMessages.NOT_A_FILE_MSG),
    /**
     * Missing file name.
     */
    MISSING_FILE_NAME("Missing file name.", CATEGORY_USER_INPUT, CIFSExceptionMessages.MISSING_FILE_NAME_MSG),
    /**
     * Versioning not supported by CIFS/SMB file storage.
     */
    VERSIONING_NOT_SUPPORTED("Versioning not supported by CIFS/SMB file storage.", CATEGORY_USER_INPUT,
        CIFSExceptionMessages.VERSIONING_NOT_SUPPORTED_MSG),
    /**
     * The operation completed successfully.
     */
    NT_STATUS_OK("The operation completed successfully.", CATEGORY_USER_INPUT, CIFSExceptionMessages.NT_STATUS_OK),
    /**
     * A device attached to the system is not functioning.
     */
    NT_STATUS_UNSUCCESSFUL("A device attached to the system is not functioning.", CATEGORY_USER_INPUT,
        CIFSExceptionMessages.NT_STATUS_UNSUCCESSFUL),
    /**
     * Incorrect function.
     */
    NT_STATUS_NOT_IMPLEMENTED("Incorrect function.", CATEGORY_ERROR, CIFSExceptionMessages.NT_STATUS_NOT_IMPLEMENTED),
    /**
     * The parameter is incorrect.
     */
    NT_STATUS_INVALID_INFO_CLASS("The parameter is incorrect.", CATEGORY_USER_INPUT, CIFSExceptionMessages.NT_STATUS_INVALID_INFO_CLASS),
    /**
     * Invalid access to memory location.
     */
    NT_STATUS_ACCESS_VIOLATION("Invalid access to memory location.", CATEGORY_USER_INPUT, CIFSExceptionMessages.NT_STATUS_ACCESS_VIOLATION),
    /**
     * The handle is invalid.
     */
    NT_STATUS_INVALID_HANDLE("The handle is invalid.", CATEGORY_USER_INPUT, CIFSExceptionMessages.NT_STATUS_INVALID_HANDLE),
    /**
     * The parameter is incorrect.
     */
    NT_STATUS_INVALID_PARAMETER("The parameter is incorrect.", CATEGORY_USER_INPUT, CIFSExceptionMessages.NT_STATUS_INVALID_PARAMETER),
    /**
     * The system cannot find the file specified.
     */
    NT_STATUS_NO_SUCH_DEVICE("The system cannot find the file specified.", CATEGORY_USER_INPUT,
        CIFSExceptionMessages.NT_STATUS_NO_SUCH_DEVICE),
    /**
     * The system cannot find the file specified.
     */
    NT_STATUS_NO_SUCH_FILE("The system cannot find the file specified.", CATEGORY_USER_INPUT, CIFSExceptionMessages.NT_STATUS_NO_SUCH_FILE),
    /**
     * More data is available.
     */
    NT_STATUS_MORE_PROCESSING_REQUIRED("More data is available.", CATEGORY_USER_INPUT,
        CIFSExceptionMessages.NT_STATUS_MORE_PROCESSING_REQUIRED),
    /**
     * Access is denied.
     */
    NT_STATUS_ACCESS_DENIED("Access is denied.", CATEGORY_USER_INPUT, CIFSExceptionMessages.NT_STATUS_ACCESS_DENIED),
    /**
     * The data area passed to a system call is too small.
     */
    NT_STATUS_BUFFER_TOO_SMALL("The data area passed to a system call is too small.", CATEGORY_USER_INPUT,
        CIFSExceptionMessages.NT_STATUS_BUFFER_TOO_SMALL),
    /**
     * The filename, directory name, or volume label syntax is incorrect.
     */
    NT_STATUS_OBJECT_NAME_INVALID("The filename, directory name, or volume label syntax is incorrect.", CATEGORY_USER_INPUT,
        CIFSExceptionMessages.NT_STATUS_OBJECT_NAME_INVALID),
    /**
     * The system cannot find the file specified.
     */
    NT_STATUS_OBJECT_NAME_NOT_FOUND("The system cannot find the file specified.", CATEGORY_USER_INPUT,
        CIFSExceptionMessages.NT_STATUS_OBJECT_NAME_NOT_FOUND),
    /**
     * Cannot create a file when that file already exists.
     */
    NT_STATUS_OBJECT_NAME_COLLISION("Cannot create a file when that file already exists.", CATEGORY_USER_INPUT,
        CIFSExceptionMessages.NT_STATUS_OBJECT_NAME_COLLISION),
    /**
     * The handle is invalid.
     */
    NT_STATUS_PORT_DISCONNECTED("The handle is invalid.", CATEGORY_USER_INPUT, CIFSExceptionMessages.NT_STATUS_PORT_DISCONNECTED),
    /**
     * The specified path is invalid.
     */
    NT_STATUS_OBJECT_PATH_INVALID("The specified path is invalid.", CATEGORY_USER_INPUT,
        CIFSExceptionMessages.NT_STATUS_OBJECT_PATH_INVALID),
    /**
     * The system cannot find the path specified.
     */
    NT_STATUS_OBJECT_PATH_NOT_FOUND("The system cannot find the path specified.", CATEGORY_USER_INPUT,
        CIFSExceptionMessages.NT_STATUS_OBJECT_PATH_NOT_FOUND),
    /**
     * The specified path is invalid.
     */
    NT_STATUS_OBJECT_PATH_SYNTAX_BAD("The specified path is invalid.", CATEGORY_USER_INPUT,
        CIFSExceptionMessages.NT_STATUS_OBJECT_PATH_SYNTAX_BAD),
    /**
     * The process cannot access the file because it is being used by another process.
     */
    NT_STATUS_SHARING_VIOLATION("The process cannot access the file because it is being used by another process.", CATEGORY_USER_INPUT,
        CIFSExceptionMessages.NT_STATUS_SHARING_VIOLATION),
    /**
     * Access is denied.
     */
    NT_STATUS_DELETE_PENDING("Access is denied.", CATEGORY_USER_INPUT, CIFSExceptionMessages.NT_STATUS_DELETE_PENDING),
    /**
     * There are currently no logon servers available to service the logon request.
     */
    NT_STATUS_NO_LOGON_SERVERS("There are currently no logon servers available to service the logon request.", CATEGORY_USER_INPUT,
        CIFSExceptionMessages.NT_STATUS_NO_LOGON_SERVERS),
    /**
     * The specified user already exists.
     */
    NT_STATUS_USER_EXISTS("The specified user already exists.", CATEGORY_USER_INPUT, CIFSExceptionMessages.NT_STATUS_USER_EXISTS),
    /**
     * The specified user does not exist.
     */
    NT_STATUS_NO_SUCH_USER("The specified user does not exist.", CATEGORY_USER_INPUT, CIFSExceptionMessages.NT_STATUS_NO_SUCH_USER),
    /**
     * The specified network password is not correct.
     */
    NT_STATUS_WRONG_PASSWORD("The specified network password is not correct.", CATEGORY_USER_INPUT,
        CIFSExceptionMessages.NT_STATUS_WRONG_PASSWORD),
    /**
     * Logon failure: unknown user name or bad password.
     */
    NT_STATUS_LOGON_FAILURE("Logon failure: unknown user name or bad password.", CATEGORY_USER_INPUT,
        CIFSExceptionMessages.NT_STATUS_LOGON_FAILURE),
    /**
     * Logon failure: user account restriction.
     */
    NT_STATUS_ACCOUNT_RESTRICTION("Logon failure: user account restriction.", CATEGORY_PERMISSION_DENIED,
        CIFSExceptionMessages.NT_STATUS_ACCOUNT_RESTRICTION),
    /**
     * Logon failure: account logon time restriction violation.
     */
    NT_STATUS_INVALID_LOGON_HOURS("Logon failure: account logon time restriction violation.", CATEGORY_PERMISSION_DENIED,
        CIFSExceptionMessages.NT_STATUS_INVALID_LOGON_HOURS),
    /**
     * Logon failure: user not allowed to log on to this computer.
     */
    NT_STATUS_INVALID_WORKSTATION("Logon failure: user not allowed to log on to this computer.", CATEGORY_PERMISSION_DENIED,
        CIFSExceptionMessages.NT_STATUS_INVALID_WORKSTATION),
    /**
     * Logon failure: the specified account password has expired.
     */
    NT_STATUS_PASSWORD_EXPIRED("Logon failure: the specified account password has expired.", CATEGORY_PERMISSION_DENIED,
        CIFSExceptionMessages.NT_STATUS_PASSWORD_EXPIRED),
    /**
     * Logon failure: account currently disabled.
     */
    NT_STATUS_ACCOUNT_DISABLED("Logon failure: account currently disabled.", CATEGORY_PERMISSION_DENIED,
        CIFSExceptionMessages.NT_STATUS_ACCOUNT_DISABLED),
    /**
     * No mapping between account names and security IDs was done.
     */
    NT_STATUS_NONE_MAPPED("No mapping between account names and security IDs was done.", CATEGORY_ERROR, null),
    /**
     * The security ID structure is invalid.
     */
    NT_STATUS_INVALID_SID("The security ID structure is invalid.", CATEGORY_ERROR, null),
    /**
     * All pipe instances are busy.
     */
    NT_STATUS_INSTANCE_NOT_AVAILABLE("All pipe instances are busy.", CATEGORY_ERROR, null),
    /**
     * All pipe instances are busy.
     */
    NT_STATUS_PIPE_NOT_AVAILABLE("All pipe instances are busy.", CATEGORY_ERROR, null),
    /**
     * The pipe state is invalid.
     */
    NT_STATUS_INVALID_PIPE_STATE("The pipe state is invalid.", CATEGORY_ERROR, null),
    /**
     * All pipe instances are busy.
     */
    NT_STATUS_PIPE_BUSY(CIFSExceptionMessages.NT_STATUS_PIPE_BUSY, CATEGORY_ERROR, null),
    /**
     * No process is on the other end of the pipe.
     */
    NT_STATUS_PIPE_DISCONNECTED("No process is on the other end of the pipe.", CATEGORY_ERROR, null),
    /**
     * The pipe is being closed.
     */
    NT_STATUS_PIPE_CLOSING("The pipe is being closed.", CATEGORY_ERROR, null),
    /**
     * Waiting for a process to open the other end of the pipe.
     */
    NT_STATUS_PIPE_LISTENING("Waiting for a process to open the other end of the pipe.", CATEGORY_TRY_AGAIN,
        CIFSExceptionMessages.NT_STATUS_PIPE_LISTENING),
    /**
     * Access is denied.
     */
    NT_STATUS_FILE_IS_A_DIRECTORY("Access is denied.", CATEGORY_PERMISSION_DENIED, CIFSExceptionMessages.NT_STATUS_FILE_IS_A_DIRECTORY),
    /**
     * A duplicate name exists on the network.
     */
    NT_STATUS_DUPLICATE_NAME("A duplicate name exists on the network.", CATEGORY_USER_INPUT, CIFSExceptionMessages.NT_STATUS_DUPLICATE_NAME),
    /**
     * The specified network name is no longer available.
     */
    NT_STATUS_NETWORK_NAME_DELETED("The specified network name is no longer available.", CATEGORY_USER_INPUT,
        CIFSExceptionMessages.NT_STATUS_NETWORK_NAME_DELETED),
    /**
     * Network access is denied.
     */
    NT_STATUS_NETWORK_ACCESS_DENIED("Network access is denied.", CATEGORY_PERMISSION_DENIED,
        CIFSExceptionMessages.NT_STATUS_NETWORK_ACCESS_DENIED),
    /**
     * The network name cannot be found.
     */
    NT_STATUS_BAD_NETWORK_NAME("The network name cannot be found.", CATEGORY_USER_INPUT, CIFSExceptionMessages.NT_STATUS_BAD_NETWORK_NAME),
    /**
     * No more connections can be made to this remote computer at this time because there are already as many connections as the computer
     * can accept.
     */
    NT_STATUS_REQUEST_NOT_ACCEPTED("No more connections can be made to this remote computer at this time because there are already as "
        + "many connections as the computer can accept.", CATEGORY_TRY_AGAIN, CIFSExceptionMessages.NT_STATUS_REQUEST_NOT_ACCEPTED),
    /**
     * Indicates a Windows NT Server could not be contacted or that objects within the domain are protected such that necessary information
     * could not be retrieved.
     */
    NT_STATUS_CANT_ACCESS_DOMAIN_INFO("Indicates a Windows NT Server could not be contacted or that objects within the domain are "
        + "protected such that necessary information could not be retrieved.", CATEGORY_ERROR, null),
    /**
     * The specified domain did not exist.
     */
    NT_STATUS_NO_SUCH_DOMAIN("The specified domain did not exist.", CATEGORY_USER_INPUT, CIFSExceptionMessages.NT_STATUS_NO_SUCH_DOMAIN),
    /**
     * The directory name is invalid.
     */
    NT_STATUS_NOT_A_DIRECTORY("The directory name is invalid.", CATEGORY_USER_INPUT, CIFSExceptionMessages.NT_STATUS_NOT_A_DIRECTORY),
    /**
     * Access is denied.
     */
    NT_STATUS_CANNOT_DELETE("Access is denied.", CATEGORY_PERMISSION_DENIED, CIFSExceptionMessages.NT_STATUS_CANNOT_DELETE),
    /**
     * The format of the specified computer name is invalid.
     */
    NT_STATUS_INVALID_COMPUTER_NAME("The format of the specified computer name is invalid.", CATEGORY_USER_INPUT,
        CIFSExceptionMessages.NT_STATUS_INVALID_COMPUTER_NAME),
    /**
     * The pipe has been ended.
     */
    NT_STATUS_PIPE_BROKEN("The pipe has been ended.", CATEGORY_ERROR, null),
    /**
     * The specified local group does not exist.
     */
    NT_STATUS_NO_SUCH_ALIAS("The specified local group does not exist.", CATEGORY_USER_INPUT, CIFSExceptionMessages.NT_STATUS_NO_SUCH_ALIAS),
    /**
     * Logon failure: the user has not been granted the requested logon type at this computer.
     */
    NT_STATUS_LOGON_TYPE_NOT_GRANTED("Logon failure: the user has not been granted the requested logon type at this computer.",
        CATEGORY_PERMISSION_DENIED, CIFSExceptionMessages.NT_STATUS_LOGON_TYPE_NOT_GRANTED),
    /**
     * The SAM database on the Windows NT Server does not have a computer account for this workstation trust relationship.
     */
    NT_STATUS_NO_TRUST_SAM_ACCOUNT("The SAM database on the Windows NT Server does not have a computer account for this workstation "
        + "trust relationship.", CATEGORY_ERROR, null),
    /**
     * The trust relationship between the primary domain and the trusted domain failed.
     */
    NT_STATUS_TRUSTED_DOMAIN_FAILURE("The trust relationship between the primary domain and the trusted domain failed.", CATEGORY_ERROR
        , null),
    /**
     * The account used is a Computer Account. Use your global user account or local user account to access this server.
     */
    NT_STATUS_NOLOGON_WORKSTATION_TRUST_ACCOUNT("The account used is a Computer Account. Use your global user account or local user"
        + " account to access this server.", CATEGORY_USER_INPUT, CIFSExceptionMessages.NT_STATUS_NOLOGON_WORKSTATION_TRUST_ACCOUNT),
    /**
     * The user must change his password before he logs on the first time.
     */
    NT_STATUS_PASSWORD_MUST_CHANGE("The user must change his password before he logs on the first time.", CATEGORY_USER_INPUT,
        CIFSExceptionMessages.NT_STATUS_PASSWORD_MUST_CHANGE),
    /**
     * Please use a hostname instead of an IP address in the SMB URL.
     */
    NT_STATUS_NOT_FOUND("Please use a hostname instead of an IP address in the SMB URL.", CATEGORY_USER_INPUT,
        CIFSExceptionMessages.NT_STATUS_NOT_FOUND),
    /**
     * Your account has been blocked. This can have various reasons like having mistyped the password several times.<br>
     * Please contact your system administrator or hoster in case you can no longer log in.
     */
    NT_STATUS_ACCOUNT_LOCKED_OUT("Your account has been blocked. This can have various reasons like having mistyped the password"
        + " several times.\nPlease contact your system administrator or hoster in case you can no longer log in.",
        CATEGORY_PERMISSION_DENIED, CIFSExceptionMessages.NT_STATUS_ACCOUNT_LOCKED_OUT),
    /**
     * The remote system is not reachable by the transport.
     */
    NT_STATUS_PATH_NOT_COVERED("The remote system is not reachable by the transport.", CATEGORY_SERVICE_DOWN, null),
    /**
     * I/O request could not be handled.
     */
    NT_STATUS_IO_REPARSE_TAG_NOT_HANDLED("I/O request could not be handled.", CATEGORY_ERROR, null),

    ;

    private static final TIntObjectMap<CIFSExceptionCodes> CODES;

    static {
        final TIntObjectMap<CIFSExceptionCodes> m = new TIntObjectHashMap<CIFSExceptionCodes>(128);
        m.put(SmbException.NT_STATUS_OK, NT_STATUS_OK);
        m.put(SmbException.NT_STATUS_UNSUCCESSFUL, NT_STATUS_UNSUCCESSFUL);
        m.put(SmbException.NT_STATUS_NOT_IMPLEMENTED, NT_STATUS_NOT_IMPLEMENTED);
        m.put(SmbException.NT_STATUS_INVALID_INFO_CLASS, NT_STATUS_INVALID_INFO_CLASS);
        m.put(SmbException.NT_STATUS_ACCESS_VIOLATION, NT_STATUS_ACCESS_VIOLATION);
        m.put(SmbException.NT_STATUS_INVALID_HANDLE, NT_STATUS_INVALID_HANDLE);
        m.put(SmbException.NT_STATUS_INVALID_PARAMETER, NT_STATUS_INVALID_PARAMETER);
        m.put(SmbException.NT_STATUS_NO_SUCH_DEVICE, NT_STATUS_NO_SUCH_DEVICE);
        m.put(SmbException.NT_STATUS_NO_SUCH_FILE, NT_STATUS_NO_SUCH_FILE);
        m.put(SmbException.NT_STATUS_MORE_PROCESSING_REQUIRED, NT_STATUS_MORE_PROCESSING_REQUIRED);
        m.put(SmbException.NT_STATUS_ACCESS_DENIED, NT_STATUS_ACCESS_DENIED);
        m.put(SmbException.NT_STATUS_BUFFER_TOO_SMALL, NT_STATUS_BUFFER_TOO_SMALL);
        m.put(SmbException.NT_STATUS_OBJECT_NAME_INVALID, NT_STATUS_OBJECT_NAME_INVALID);
        m.put(SmbException.NT_STATUS_OBJECT_NAME_NOT_FOUND, NT_STATUS_OBJECT_NAME_NOT_FOUND);
        m.put(SmbException.NT_STATUS_OBJECT_NAME_COLLISION, NT_STATUS_OBJECT_NAME_COLLISION);
        m.put(SmbException.NT_STATUS_PORT_DISCONNECTED, NT_STATUS_PORT_DISCONNECTED);
        m.put(SmbException.NT_STATUS_OBJECT_PATH_INVALID, NT_STATUS_OBJECT_PATH_INVALID);
        m.put(SmbException.NT_STATUS_OBJECT_PATH_NOT_FOUND, NT_STATUS_OBJECT_PATH_NOT_FOUND);
        m.put(SmbException.NT_STATUS_OBJECT_PATH_SYNTAX_BAD, NT_STATUS_OBJECT_PATH_SYNTAX_BAD);
        m.put(SmbException.NT_STATUS_SHARING_VIOLATION, NT_STATUS_SHARING_VIOLATION);
        m.put(SmbException.NT_STATUS_DELETE_PENDING, NT_STATUS_DELETE_PENDING);
        m.put(SmbException.NT_STATUS_NO_LOGON_SERVERS, NT_STATUS_NO_LOGON_SERVERS);
        m.put(SmbException.NT_STATUS_USER_EXISTS, NT_STATUS_USER_EXISTS);
        m.put(SmbException.NT_STATUS_NO_SUCH_USER, NT_STATUS_NO_SUCH_USER);
        m.put(SmbException.NT_STATUS_WRONG_PASSWORD, NT_STATUS_WRONG_PASSWORD);
        m.put(SmbException.NT_STATUS_LOGON_FAILURE, NT_STATUS_LOGON_FAILURE);
        m.put(SmbException.NT_STATUS_ACCOUNT_RESTRICTION, NT_STATUS_ACCOUNT_RESTRICTION);
        m.put(SmbException.NT_STATUS_INVALID_LOGON_HOURS, NT_STATUS_INVALID_LOGON_HOURS);
        m.put(SmbException.NT_STATUS_INVALID_WORKSTATION, NT_STATUS_INVALID_WORKSTATION);
        m.put(SmbException.NT_STATUS_PASSWORD_EXPIRED, NT_STATUS_PASSWORD_EXPIRED);
        m.put(SmbException.NT_STATUS_ACCOUNT_DISABLED, NT_STATUS_ACCOUNT_DISABLED);
        m.put(SmbException.NT_STATUS_NONE_MAPPED, NT_STATUS_NONE_MAPPED);
        m.put(SmbException.NT_STATUS_INVALID_SID, NT_STATUS_INVALID_SID);
        m.put(SmbException.NT_STATUS_INSTANCE_NOT_AVAILABLE, NT_STATUS_INSTANCE_NOT_AVAILABLE);
        m.put(SmbException.NT_STATUS_PIPE_NOT_AVAILABLE, NT_STATUS_PIPE_NOT_AVAILABLE);
        m.put(SmbException.NT_STATUS_INVALID_PIPE_STATE, NT_STATUS_INVALID_PIPE_STATE);
        m.put(SmbException.NT_STATUS_PIPE_BUSY, NT_STATUS_PIPE_BUSY);
        m.put(SmbException.NT_STATUS_PIPE_DISCONNECTED, NT_STATUS_PIPE_DISCONNECTED);
        m.put(SmbException.NT_STATUS_PIPE_CLOSING, NT_STATUS_PIPE_CLOSING);
        m.put(SmbException.NT_STATUS_PIPE_LISTENING, NT_STATUS_PIPE_LISTENING);
        m.put(SmbException.NT_STATUS_FILE_IS_A_DIRECTORY, NT_STATUS_FILE_IS_A_DIRECTORY);
        m.put(SmbException.NT_STATUS_DUPLICATE_NAME, NT_STATUS_DUPLICATE_NAME);
        m.put(SmbException.NT_STATUS_NETWORK_NAME_DELETED, NT_STATUS_NETWORK_NAME_DELETED);
        m.put(SmbException.NT_STATUS_NETWORK_ACCESS_DENIED, NT_STATUS_NETWORK_ACCESS_DENIED);
        m.put(SmbException.NT_STATUS_BAD_NETWORK_NAME, NT_STATUS_BAD_NETWORK_NAME);
        m.put(SmbException.NT_STATUS_REQUEST_NOT_ACCEPTED, NT_STATUS_REQUEST_NOT_ACCEPTED);
        m.put(SmbException.NT_STATUS_CANT_ACCESS_DOMAIN_INFO, NT_STATUS_CANT_ACCESS_DOMAIN_INFO);
        m.put(SmbException.NT_STATUS_NO_SUCH_DOMAIN, NT_STATUS_NO_SUCH_DOMAIN);
        m.put(SmbException.NT_STATUS_NOT_A_DIRECTORY, NT_STATUS_NOT_A_DIRECTORY);
        m.put(SmbException.NT_STATUS_CANNOT_DELETE, NT_STATUS_CANNOT_DELETE);
        m.put(SmbException.NT_STATUS_INVALID_COMPUTER_NAME, NT_STATUS_INVALID_COMPUTER_NAME);
        m.put(SmbException.NT_STATUS_PIPE_BROKEN, NT_STATUS_PIPE_BROKEN);
        m.put(SmbException.NT_STATUS_NO_SUCH_ALIAS, NT_STATUS_NO_SUCH_ALIAS);
        m.put(SmbException.NT_STATUS_LOGON_TYPE_NOT_GRANTED, NT_STATUS_LOGON_TYPE_NOT_GRANTED);
        m.put(SmbException.NT_STATUS_NO_TRUST_SAM_ACCOUNT, NT_STATUS_NO_TRUST_SAM_ACCOUNT);
        m.put(SmbException.NT_STATUS_TRUSTED_DOMAIN_FAILURE, NT_STATUS_TRUSTED_DOMAIN_FAILURE);
        m.put(SmbException.NT_STATUS_NOLOGON_WORKSTATION_TRUST_ACCOUNT, NT_STATUS_NOLOGON_WORKSTATION_TRUST_ACCOUNT);
        m.put(SmbException.NT_STATUS_PASSWORD_MUST_CHANGE, NT_STATUS_PASSWORD_MUST_CHANGE);
        m.put(SmbException.NT_STATUS_NOT_FOUND, NT_STATUS_NOT_FOUND);
        m.put(SmbException.NT_STATUS_ACCOUNT_LOCKED_OUT, NT_STATUS_ACCOUNT_LOCKED_OUT);
        m.put(SmbException.NT_STATUS_PATH_NOT_COVERED, NT_STATUS_PATH_NOT_COVERED);
        m.put(SmbException.NT_STATUS_IO_REPARSE_TAG_NOT_HANDLED, NT_STATUS_IO_REPARSE_TAG_NOT_HANDLED);
        CODES = m;
    }

    /**
     * Gets the appropriate <code>OXException</code> for given <code>SmbException</code> instance.
     *
     * @param e The SMB exception
     * @return The appropriate <code>OXException</code>
     */
    public static OXException forSmbException(final SmbException e) {
        if (null == e) {
            return UNEXPECTED_ERROR.create("unknown error.");
        }
        final CIFSExceptionCodes errorCode = CODES.get(e.getNtStatus());
        if (null == errorCode) {
            return CIFSExceptionCodes.SMB_ERROR_WITH_STATUS.create(e, e.getMessage(), Integer.valueOf(e.getNtStatus()));
        }
        return errorCode.create(e, new Object[0]);
    }

    private final Category category;
    private final int detailNumber;
    private final String message;
    private String displayMessage;

    private CIFSExceptionCodes(final String message, final Category category, String displayMessage) {
        this.message = message;
        detailNumber = ordinal() + 1;
        this.category = category;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
    }

    private CIFSExceptionCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
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
    public String getDisplayMessage() {
        return displayMessage;
    }

    @Override
    public int getNumber() {
        return detailNumber;
    }

    @Override
    public String getPrefix() {
        return "CIFS";
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
