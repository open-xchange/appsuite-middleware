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

package com.openexchange.mailaccount;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link MailAccountExceptionStrings} - The mail account exception strings.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountExceptionStrings implements LocalizableStrings {

    // Unexpected error: %1$s.
    public static final String UNEXPECTED_ERROR_MSG = "Unexpected error: %1$s.";

    // Cannot find mail account with identifier %1$s for user %2$s in context %3$s.
    public static final String NOT_FOUND_MSG = "Cannot find mail account with identifier %1$s for user %2$s in context %3$s.";

    // Found two mail accounts with same identifier %1$s for user %2$s in context %3$s.
    public static final String CONFLICT_MSG = "Found two mail accounts with same identifier %1$s for user %2$s in context %3$s.";

    // Found two mail accounts with same email address %1$s for user %2$s in context %3$s.
    public static final String CONFLICT_ADDR_MSG = "Found two mail accounts with same E-Mail address %1$s for user %2$s in context %3$s.";

    // A SQL error occurred: %1$s.
    public static final String SQL_ERROR_MSG = "A SQL error occurred: %1$s.";

    // A host could not be resolved: %1$s.
    public static final String UNKNOWN_HOST_ERROR_MSG = "A host could not be resolved: %1$s.";

    // Denied deletion of default mail account of user %1$s in context %2$s.
    public static final String NO_DEFAULT_DELETE_MSG = "Denied deletion of default mail account of user %1$s in context %2$s.";

    // Denied update of default mail account of user %1$s in context %2$s.
    public static final String NO_DEFAULT_UPDATE_MSG = "Denied update of default mail account of user %1$s in context %2$s.";

    // No duplicate default account allowed.
    public static final String NO_DUPLICATE_DEFAULT_MSG = "No duplicate default account allowed.";

    // Password encryption failed for login %1$s on server %2$s (user=%3$s, context=%4$s).
    public static final String PASSWORD_ENCRYPTION_FAILED_MSG = "Password encryption failed for login %1$s on server %2$s (user=%3$s, context=%4$s).";

    // Password decryption failed for login %1$s on server %2$s (user=%3$s, context=%4$s).
    public static final String PASSWORD_DECRYPTION_FAILED_MSG = "Password decryption failed for login %1$s on server %2$s (user=%3$s, context=%4$s).";

    // The Unified Mail account already exists for user %1$s in context %2$s.
    public static final String DUPLICATE_UNIFIED_INBOX_ACCOUNT_MSG = "The Unified Mail account already exists for user %1$s in context %2$s.";

    // Mail account creation failed.
    public static final String CREATION_FAILED_MSG = "Mail account creation failed.";

    // Mail account validation failed.
    public static final String VALIDATION_FAILED_MSG = "Mail account validation failed.";

    // Multiple mail accounts not enabled for user %1$s in context %2$s.
    public static final String NOT_ENABLED_MSG = "Multiple mail accounts not enabled for user %1$s in context %2$s.";

    // Invalid mail account name: %1$s
    public static final String INVALID_NAME_MSG = "Invalid mail account name: %1$s";

    // Duplicate mail account for user %1$s in context %2$s.
    public static final String DUPLICATE_MAIL_ACCOUNT_MSG = "Duplicate mail account for user %1$s in context %2$s.";

    // Duplicate transport account for user %1$s in context %2$s.
    public static final String DUPLICATE_TRANSPORT_ACCOUNT_MSG = "Duplicate transport account for user %1$s in context %2$s.";

    // Is thrown if the URI to the mail server can not be understood.
    // %1$s is replaced with the not understandable mail server URI.
    public static final String URI_PARSE_FAILED_MSG = "Unable to parse mail server URI \"%1$s\".";

    // Invalid host name: %1$s
    public static final String INVALID_HOST_NAME_MSG = "Invalid host name: %1$s";

    // Could not connect to mail server "%1$s" for login %2$s
    public static final String VALIDATE_FAILED_MAIL_MSG = "Could not connect to mail server \"%1$s\" for login %2$s";

    // Could not connect to transport server "%1$s" for login %2$s
    public static final String VALIDATE_FAILED_TRANSPORT_MSG = "Could not connect to transport server \"%1$s\" for login %2$s";

    // Default port specified for mail protocol "%1$s", but SSL is enabled. Please review if appropriate.
    public static final String DEFAULT_BUT_SECURE_MAIL_MSG = "Default port specified for mail protocol \"%1$s\", but SSL is enabled. Please review if appropriate.";

    // Secure port specified for mail protocol "%1$s", but SSL is not enabled. Please review if appropriate.
    public static final String SECURE_BUT_DEFAULT_MAIL_MSG = "Secure port specified for mail protocol \"%1$s\", but SSL is not enabled. Please review if appropriate.";

    // Default port specified for transport protocol "%1$s", but SSL is enabled. Please review if appropriate.
    public static final String DEFAULT_BUT_SECURE_TRANSPORT_MSG = "Default port specified for transport protocol \"%1$s\", but SSL is enabled. Please review if appropriate.";

    // Secure port specified for transport protocol "%1$s", but SSL is not enabled. Please review if appropriate.
    public static final String SECURE_BUT_DEFAULT_TRANSPORT_MSG = "Secure port specified for transport protocol \"%1$s\", but SSL is not enabled. Please review if appropriate.";

    /**
     * Initializes a new {@link MailAccountExceptionStrings}.
     */
    private MailAccountExceptionStrings() {
        super();
    }

}
