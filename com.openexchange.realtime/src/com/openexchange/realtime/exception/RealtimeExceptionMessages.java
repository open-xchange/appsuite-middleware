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

package com.openexchange.realtime.exception;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link RealtimeExceptionMessages} - Translatable error messages.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 */
public class RealtimeExceptionMessages implements LocalizableStrings {

    // XMPP
    public static final String STANZA_BAD_REQUEST_MSG = "";
    public static final String STANZA_CONFILCT_MSG = "";
    public static final String STANZA_FEATURE_NOT_IMPLEMENTED_MSG = "";
    public static final String STANZA_FORBIDDEN_MSG = "";
    public static final String STANZA_GONE_MSG = "";
    public static final String STANZA_INTERNAL_SERVER_ERROR_MSG = "";
    public static final String STANZA_ITEM_NOT_FOUND_MSG = "";
    public static final String STANZA_JID_MALFORMED_MSG = "";
    public static final String STANZA_NOT_ACCEPTABLE_MSG = "";
    public static final String STANZA_NOT_AUTHORIZED_MSG = "";
    public static final String STANZA_NOT_ALLOWED_MSG = "";
    public static final String STANZA_PAYMENT_REQUIRED_MSG = "";
    public static final String STANZA_POLICY_VIOLATION_MSG = "";
    public static final String STANZA_RECIPIENT_UNAVAILABLE_MSG = "";
    public static final String STANZA_REDIRECT_MSG = "";
    public static final String STANZA_REGISTRATION_REQUIRED_MSG = "";
    public static final String STANZA_REMOTE_SERVER_NOT_FOUND_MSG = "";
    public static final String STANZA_REMOTE_SERVER_TIMEOUT_MSG = "";
    public static final String STANZA_RESOURCE_CONSTRAINT_MSG = "";
    public static final String STANZA_SERVICE_UNAVAILABLE_MSG = "";
    public static final String STANZA_SUBSCRIPTION_REQUIRED_MSG = "";
    public static final String STANZA_UNDEFINED_CONDITION_MSG = "";
    public static final String STANZA_UNEXPECTED_REQUEST_MSG = "";
    
    // Atmosphere
    
    // Generic (start with code 1000)
    /** No appropriate channel found for recipient %1$s with payload namespace %2$s */
    public static final String NO_APPROPRIATE_CHANNEL = "No appropriate channel found for recipient %1$s with payload namespace %2$s";

    /** The following needed service is missing: \"%1$s\" */
    public static final String NEEDED_SERVICE_MISSING_MSG = "The following needed service is missing: \"%1$s\"";

    // Unexpected error: %1$s
    public static final String UNEXPECTED_ERROR_MSG = "Unexpected error: %1$s";

    /** Invalid ID. Resource identifier is missing. */
    public static final String INVALID_ID = "Invalid ID. Resource identifier is missing.";

    /** Resource not available. */
    public static final String RESOURCE_NOT_AVAILABLE_MSG = "Resource not available.";
    
    /** You session is invlaid.*/
    public static final String SESSION_INVALID_MSG = "Your session is invalid.";
    

}
