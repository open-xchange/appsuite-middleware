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

package com.openexchange.messaging.mail;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link MailMessagingExceptionMessages} - Exception messages for {@link MailMessagingException} that needs to be translated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18
 */
public final class MailMessagingExceptionMessages implements LocalizableStrings {

    // An error occurred: %1$s
    public static final String UNEXPECTED_ERROR_MSG = "An error occurred: %1$s";

    // A SQL error occurred: %1$s
    public static final String SQL_ERROR_MSG = "A SQL error occurred: %1$s";

    // An I/O error occurred: %1$s
    public static final String IO_ERROR_MSG = "An I/O error occurred: %1$s";

    // A JSON error occurred: %1$s
    public static final String JSON_ERROR_MSG = "A JSON error occurred: %1$s";

    // Communication error with facebook service: %1$s
    public static final String COMMUNICATION_ERROR_MSG = "Communication error with facebook service: %1$s";

    // Login to facebook failed for login %1$s.
    public static final String FAILED_LOGIN_MSG = "Login to facebook failed for login %1$s.";

    // Login form not found on page: %1$s
    public static final String LOGIN_FORM_NOT_FOUND_MSG = "Login form not found on page: %1$s";

    // Element with attribute %1$s not found on page %2$s.
    public static final String ELEMENT_NOT_FOUND_MSG = "Element with attribute %1$s not found on page %2$s.";

    // Missing permission "%1$s" in facebook login %2$s. Please copy following URL to your browser, login as %2$s (if not done yet) and grant access: %3$s
    public static final String MISSING_PERMISSION_MSG =
        "Missing permission \"%1$s\" in facebook login %2$s. Please copy the following url to your browser, log in as %2$s (if not done yet) and grant access:\n%3$s";

    // An error occurred during the processing of a script.
    public static final String SCRIPT_ERROR_MSG = "An error occurred while processing a script.";

    // Missing permission for the application associated with configured Facebook API key: %1$s
    // Please grant access for that application in your Facebook account settings.
    public static final String MISSING_APPLICATION_PERMISSION_MSG =
        "Missing permission for the application associated with configured Facebook API key: %1$s\n" + "Please grant access for that application in your Facebook account settings.";

    // FQL query result size (%1$s) does not match requested number of post identifiers (%2$s).
    public static final String FQL_QUERY_RESULT_MISMATCH_MSG = "FQL query result size (%1$s) does not match requested number of post identifiers (%2$s).";

    // Missing configuration parameter: %1$s.
    public static final String MISSING_CONFIGURATION_PARAMETER_MSG = "Missing configuration parameter: %1$s.";

    // Wrong configuration parameter type. Should be %1$s, but is %2$s.
    public static final String WRONG_CONFIGURATION_PARAMETER_MSG = "Wrong configuration parameter type. Should be %1$s, but is %2$s.";

    /**
     * Initializes a new {@link MailMessagingExceptionMessages}.
     */
    private MailMessagingExceptionMessages() {
        super();
    }

}
