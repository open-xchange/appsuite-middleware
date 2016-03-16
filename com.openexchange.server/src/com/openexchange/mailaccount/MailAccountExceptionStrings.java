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

package com.openexchange.mailaccount;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link MailAccountExceptionStrings} - The mail account exception strings.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountExceptionStrings implements LocalizableStrings {

    // Cannot find mail account for user "%2$s".
    public static final String NOT_FOUND_MSG_DISPLAY = "Cannot find mail account for user \"%2$s\".";

    // Shown when a mail account that already exists should be created
    public static final String CONFLICT_MSG_DISPLAY = "Found two mail accounts with same identifier %1$s for user %2$s.";

    // Shown when the requested host cannot be resolved
    public static final String UNKNOWN_HOST_ERROR_MSG_DISPLAY = "A host could not be resolved: %1$s.";

    // You do not have the appropriate permissions to delete the default mail account.
    public static final String NO_DEFAULT_DELETE_MSG_DISPLAY = "You do not have the appropriate permissions to delete the default mail account.";

    // You do not have the appropriate permissions to update the default mail account.
    public static final String NO_DEFAULT_UPDATE_MSG_DISPLAY = "You do not have the appropriate permissions to update the default mail account.";

    // The default mail account already exists.
    public static final String NO_DUPLICATE_DEFAULT_MSG_DISPLAY = "The default mail account already exists.";

    // The Unified Mail account already exists
    public static final String DUPLICATE_UNIFIED_INBOX_ACCOUNT_MSG_DISPLAY = "The Unified Mail account already exists.";

    // It is not allowed to create a new mail account for Unified Mail.
    public static final String UNIFIED_INBOX_ACCOUNT_CREATION_FAILED_MSG_DISPLAY = "It is not allowed to create a new mail account for Unified Mail.";

    // Validation for Unified Mail failed.
    public static final String UNIFIED_INBOX_ACCOUNT_VALIDATION_FAILED_MSG_DISPLAY = "Validation for Unified Mail failed.";

    // You do not have the appropriate permissions to create more than one mail account.
    public static final String NOT_ENABLED_MSG_DISPLAY = "You do not have the appropriate permissions to create more than one mail account.";

    // A mail account with the given E-Mail address already exists.
    public static final String CONFLICT_ADDR_MSG_DISPLAY = "A mail account with the given E-Mail address already exists.";

    // The mail account name "%1$s" is not valid.
    public static final String INVALID_NAME_MSG_DISPLAY = "The mail account name \"%1$s\" is not valid.";

    // The selected mail account name already exists.
    public static final String DUPLICATE_MAIL_ACCOUNT_MSG_DISPLAY = "The selected mail account name already exists.";

    // Shown when the transport account already exists
    public static final String DUPLICATE_TRANSPORT_ACCOUNT_MSG_DISPLAY = "Duplicate transport account for user %1$s in context %2$s.";

    // Shown when a connection to the mail server is not possible for the given login
    public static final String VALIDATE_FAILED_MAIL_MSG_DISPLAY = "Could not connect to mail server \"%1$s\" for login %2$s. Please review your settings.";

    // Shown when a connection to the mail transport server is not possible for the given login
    public static final String VALIDATE_FAILED_TRANSPORT_MSG_DISPLAY = "Could not connect to transport server \"%1$s\" for login %2$s. Please review your settings.";

    // Shown when no SSL port was set but SSL enabled
    public static final String DEFAULT_BUT_SECURE_MAIL_MSG_DISPLAY = "Default port specified for mail protocol \"%1$s\", but SSL is enabled. Please review if appropriate.";

    // Shown when SSL port was set but SSL not enabled
    public static final String SECURE_BUT_DEFAULT_MAIL_MSG_DISPLAY = "Secure port specified for mail protocol \"%1$s\", but SSL is not enabled. Please review if appropriate.";

    // Shown when no SSL port was set for transport protocol but SSL enabled
    public static final String DEFAULT_BUT_SECURE_TRANSPORT_MSG_DISPLAY = "Default port specified for transport protocol \"%1$s\", but SSL is enabled. Please review if appropriate.";

    // Shown when SSL port was set for transport protocol but SSL not enabled
    public static final String SECURE_BUT_DEFAULT_TRANSPORT_MSG_DISPLAY = "Secure port specified for transport protocol \"%1$s\", but SSL is not enabled. Please review if appropriate.";

    // The given host name "%1$s" is invalid.
    public static final String INVALID_HOST_NAME_MSG_DISPLAY = "The given host name \"%1$s\" is invalid.";

    // The given host name "%1$s" is not allowed.
    public static final String BLACKLISTED_SERVER_MSG_DISPLAY = "The given host name \"%1$s\" is not allowed.";

    // The protocol must not changed from %1$s to %2$s
    public static final String PROTOCOL_CHANGE_MSG_DISPLAY = "The protocol must not changed from %1$s to %2$s.";

    /**
     * Initializes a new {@link MailAccountExceptionStrings}.
     */
    private MailAccountExceptionStrings() {
        super();
    }

}
