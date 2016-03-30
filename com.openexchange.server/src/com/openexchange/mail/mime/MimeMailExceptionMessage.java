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

package com.openexchange.mail.mime;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link MimeMailExceptionMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class MimeMailExceptionMessage implements LocalizableStrings {

    /**
     * There was an issue while authenticating. This may be due to a recent password change. To continue please log out and log back in with
     * your most current password.
     */
    public final static String LOGIN_FAILED_MSG_DISPLAY = "There was an issue while authenticating. This may be due to a recent password change. To continue please log out and log back in with your most current password.";

    /**
     * The provided login information seem to be wrong. Please try again.
     */
    public final static String INVALID_CREDENTIALS_MSG_DISPLAY = "The provided login information seem to be wrong. Please try again.";

    /**
     * The provided login information to access mail server %1$s seem to be wrong. Please try again.
     */
    public final static String INVALID_CREDENTIALS_EXT_MSG_DISPLAY = "The provided login information to access mail server %1$s seem to be wrong. Please try again.";

    /**
     * Mail folder "%1$s" could not be found.
     */
    public final static String FOLDER_NOT_FOUND_MSG_DISPLAY = "Mail folder \"%1$s\" could not be found.";

    /**
     * Mail folder "%1$s" could not be found on mail server %2$s.
     */
    public final static String FOLDER_NOT_FOUND_EXT_MSG_DISPLAY = "Mail folder \"%1$s\" could not be found on mail server %2$s.";

    /**
     * Folder "%1$s" has been closed. Probably your request took too long.
     */
    public final static String FOLDER_CLOSED_MSG_DISPLAY = "Folder \"%1$s\" has been closed. Probably your request took too long.";

    /**
     * Folder "%1$s" has been closed on mail server %2$s. Probably your request took too long.
     */
    public final static String FOLDER_CLOSED_EXT_MSG_DISPLAY = "Folder \"%1$s\" has been closed on mail server %2$s. Probably your request took too long.";

    /**
     * Mail(s) could not be found in the given folder.
     */
    public final static String MESSAGE_REMOVED_DISPLAY = "Mail(s) could not be found in the given folder.";

    /**
     * The given E-Mail address "%1$s" is invalid.
     */
    public final static String INVALID_EMAIL_ADDRESS_MSG_DISPLAY = "The given E-Mail address \"%1$s\" is invalid.";

    /**
     * You do not have the appropriate permissions to change the content of folder "%1$s".
     */
    public final static String READ_ONLY_FOLDER_MSG_DISPLAY = "You do not have the appropriate permissions to change the content of folder \"%1$s\".";

    /**
     * An attempt was made to open a read-only folder with read-write "%1$s" on mail server %2$s with login %3$s (user=%4$s, context=%5$s)
     */
    public final static String READ_ONLY_FOLDER_EXT_MSG_DISPLAY = "You do not have the appropriate permissions to change the content of folder \"%1$s\" on mail server %2$s.";

    /**
     * Invalid search expression: %1$s
     */
    public final static String SEARCH_ERROR_MSG_DISPLAY = "The search expression \"%1$s\" you entered is invalid.";

    /**
     * Message could not be sent because it is too large.
     */
    public final static String MESSAGE_TOO_LARGE_MSG_DISPLAY = "Message could not be sent because it is too large.";

    /**
     * Message could not be sent because it is too large.
     */
    public final static String MESSAGE_TOO_LARGE_EXT_MSG_DISPLAY = "Message could not be sent because it is too large (%1$s).";

    /**
     * Message could not be sent to following recipients: %1$s.
     */
    public final static String SEND_FAILED_MSG_DISPLAY = "Message could not be sent to the following recipients: %1$s.";

    /**
     * Message could not be sent to following recipients: %1$s (%2$s)
     */
    public final static String SEND_FAILED_EXT_MSG_DISPLAY = "Message could not be sent to the following recipients: %1$s (%2$s)";

    /**
     * Message could not be sent. Error message from mail transport server: %1$s
     */
    public static final String SEND_FAILED_MSG_ERROR_MSG_DISPLAY = "Message could not be sent. Error message from mail transport server: %1$s";

    /**
     * Message could not be sent. Error message from mail transport server: %1$s (%2$s)
     */
    public static final String SEND_FAILED_EXT_MSG_ERROR_MSG_DISPLAY = "Message could not be sent. Error message from mail transport server: %1$s (%2$s)";

    /**
     * Lost connection to mail server.
     */
    public final static String STORE_CLOSED_MSG_DISPLAY = "Lost connection to mail server.";

    /**
     * Connection closed to mail server %1$s.
     */
    public final static String STORE_CLOSED_EXT_MSG_DISPLAY = "Connection closed to mail server %1$s.";

    /**
     * The connection to remote server %1$s was refused or timed out while attempting to connect.
     */
    public final static String CONNECT_ERROR_MSG_DISPLAY = "The connection to remote server %1$s was refused or timed out while attempting to connect.";

    /**
     * The allowed quota on mail server exceeded.
     */
    public final static String QUOTA_EXCEEDED_MSG_DISPLAY = "The allowed quota on mail server exceeded.";

    /**
     * The allowed quota on mail server "%1$s" exceeded.
     */
    public final static String QUOTA_EXCEEDED_EXT_MSG_DISPLAY = "The allowed quota on mail server \"%1$s\" exceeded.";

    /**
     * The mailbox is already in use. Please try again later.
     */
    public static final String IN_USE_ERROR_MSG_DISPLAY = "The mailbox is already in use. Please try again later.";

    /**
     * The mailbox on mail server %1$s for login %2$s is already in use by one of your other processes or clients. Please try again later.
     */
    public static final String IN_USE_ERROR_EXT_MSG_DISPLAY = "The mailbox on mail server %1$s for login %2$s is already in use by one of your other processes or clients. Please try again later.";

    // Wrong or missing login data to access mail transport server %1$s. Please check associated account's settings/credentials.
    public static final String TRANSPORT_INVALID_CREDENTIALS_MSG_DISPLAY = "Wrong or missing login data to access mail transport server %1$s. Please check associated account's settings/credentials.";
    
    /**
     * The message could not be sent due to a mail server temporary failure. Please try again later.
     */
    public static final String TEMPORARY_FAILURE = "The message could not be sent due to a mail server temporary failure. Please try again later.";

    // Timeout while trying to send to the following recipient: %1$s
    public static final String SEND_TIMED_OUT_ERROR_MSG_DISPLAY = "Timeout while trying to send to the following recipient: %1$s";

}
