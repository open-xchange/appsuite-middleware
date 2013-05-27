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

package com.openexchange.oauth;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link OAuthExceptionMessages} - Exception messages that needs to be translated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public final class OAuthExceptionMessages implements LocalizableStrings {

    // An error occurred: %1$s
    public static final String UNEXPECTED_ERROR_MSG = "An error occurred: %1$s";

    // An I/O error occurred: %1$s
    public static final String IO_ERROR_MSG = "An I/O error occurred: %1$s";

    // A JSON error occurred: %1$s
    public static final String JSON_ERROR_MSG = "A JSON error occurred: %1$s";

    // Response is not of type: %1$s
    public static final String NOT_OF_TYPE_MSG = "Response is not of type: %1$s";

    // A HTTP error occurred: %1$s
    public static final String HTTP_ERROR_MSG = "A HTTP error occurred: %1$s";

    // General error: %1$s
    public static final String GENERAL_ERROR_MSG = "General error: %1$s";

    // Not found: %1$s
    public static final String NOT_FOUND_MSG = "Not found: %1$s";

    // The source and destination URIs are equal: %1$s.
    public static final String EQUAL_NAME_MSG = "The source and destination URIs are equal: %1$s.";

    // Conflict. The resource cannot be created until one or more parental directories have been created: %1$s.
    public static final String CONFLICT_MSG = "Conflict. The resource cannot be created until one or more parental directories have been created: %1$s.";

    // Such a resource already exists: %1$s.
    public static final String PRECONDITION_FAILED_MSG = "Such a resource already exists: %1$s.";

    // Resource limit exceeded.
    public static final String INSUFFICIENT_STORAGE_MSG = "Resource limit exceeded.";

    // Unexpected status %1$s (%2$s)
    public static final String UNEXPECTED_STATUS_MSG = "Unexpected status %1$s (%2$s)";

    // Name contains illegal characters or parent is read-only: %1$s.
    public static final String ILLEGAL_CHARS_OR_READ_ONLY_MSG = "Name contains illegal characters or parent is read-only: %1$s.";

    // Invalid URL: %1$s
    public static final String INVALID_URL_MSG = "Invalid URL: %1$s";

    // Unsupported protocol for SmartDrive server access: %1$s
    public static final String UNSUPPORTED_PROTOCOL_MSG = "Unsupported protocol for SmartDrive server access: %1$s";

    // SmartDrive user "%1$s" is not authenticated for stateful access to SmarTDrive server "%2$s".
    public static final String UNAUTHORIZED_MSG = "SmartDrive user \"%1$s\" is not authenticated for stateful access to SmarTDrive server \"%2$s\".";

    // This resource is not a file
    public static final String NOT_A_FILE_MSG = "This resource is not a file";

    // This resource is not a directory
    public static final String NOT_A_DIRECTORY_MSG = "This resource is not a directory";

    // Unknown OAuth service meta data: %1$s
    public static final String UNKNOWN_OAUTH_SERVICE_META_DATA_MSG = "Unknown OAuth service meta data: %1$s";

    // A SQL error occurred: %1$s
    public static final String SQL_ERROR_MSG = "A SQL error occurred: %1$s";

    // Account not found with identifier %1$s for user %2$s in context %3$s.
    public static final String ACCOUNT_NOT_FOUND_MSG = "Account not found with identifier %1$s for user %2$s in context %3$s.";

    // Unsupported OAuth service: %1$s
    public static final String UNSUPPORTED_SERVICE_MSG = "Unsupported OAuth service: %1$s";

    // Missing argument: %1$s
    public static final String MISSING_ARGUMENT_MSG = "Missing argument: %1$s";

    // Your '%1$s' password changed. You have to authorize the server to use your account with the new password. To do so, go to Configuration -> My Social Configuration -> Accounts. Then try again.
    public static final String TOKEN_EXPIRED_MSG = "Your '%1$s' password changed. You have to authorize the server to use your account with the new password. To do so, go to Configuration -> My Social Configuration -> Accounts. Then try again.";

    // An OAuth error occurred: %1$s
    public static final String OAUTH_ERROR_MSG = "An OAuth error occurred: %1$s";

	public static final String NOT_A_WHITELISTED_URL_MSG = "The address %1$s is not white-listed as for the %2$s OAuth API";

	public static final String MISSING_BODY_MSG = "The request sent was missing its body";

	public static final String INVALID_ACCOUNT_MSG = "The account is invalid, please recreate it.";

	// Please provide a display name.
    public static final String MISSING_DISPLAY_NAME_MSG = "Please provide a display name.";

    // The associated OAuth provider denied the request: %1$s.
    public static final String DENIED_BY_PROVIDER_MSG = "The associated OAuth provider denied the request: %1$s.";

    // The OAuth authentication process has been canceled.
    public static final String CANCELED_BY_USER_MSG = "The OAuth authentication process has been canceled.";

    /**
     * Initializes a new {@link OAuthExceptionMessages}.
     */
    private OAuthExceptionMessages() {
        super();
    }

}
