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

package com.openexchange.subscribe;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link SubscriptionErrorStrings}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SubscriptionErrorStrings implements LocalizableStrings {

    // Please try again later.
    public static final String TRY_AGAIN = "Please try again later.";

    // A SQL Error occurred.
    public static final String SQL_ERROR = "A SQL error occurred.";

    // Provide well-formed HTML.
    public static final String WELL_FORMED = "Provide well-formed HTML.";

    // A parsing error occurred: %1$s.
    public static final String PARSING_ERROR = "A parsing error occurred: %1$s.";

    // Parsing error.
    public static final String PARSING_ERROR2 = "Parsing error.";

    // Do not set a ID when saving a publication
    public static final String DONT_SET_ID = "Do not set a ID when saving a publication";

    // Unable to save a given ID.
    public static final String CANT_SAVE_ID = "Unable to save a given ID.";

    // Provide a valid id.
    public static final String PROVIDE_VALID_ID = "Provide a valid id.";

    // Could not find this Subscription
    public static final String CANT_FIND_SUBSCRIPTION = "Could not find this subscription";

    // Check value to parse.
    public static final String CHECK_VALUE = "Check value to parse.";

    // Please correct the login or password and try again
    public static final String CORRECT_PASSWORD = "Please correct the login or password and try again";

    // The login or password you entered was wrong
    public static final String WRONG_PASSWORD = "The login or password you entered are wrong";

    // Make sure, that the Service is still available, and there are no major changes on the website
    public static final String CHECK_WEBSITE = "Make sure the service is still available and there are no major changes on the website.";

    // Service unavailable
    public static final String SERVICE_UNAVAILABLE = "Service unavailable";

    // Please correct the steps of this workflow so that output of one step and input of the next step match
    public static final String OUTPUT_MUST_MATCH_INPUT = "Please correct the steps of this workflow so that output of one step and input of the next step match";

    // The steps of this crawling workflow do not fit together
    public static final String INCONSISTENT_WORKFLOW = "The steps of this crawling workflow do not fit together";

    // Ask an administrator to check the available subscription sources
    public static final String INACTIVE_SOURCE = "Ask an administrator to check the available subscription sources";

    // The subscription source this subscription belongs to is currently inactive, and cannot provide data.
    public static final String SUBSCRIPTION_SOURCE_CANT_PROVIDE_DATA = "The subscription source this subscription belongs to is currently inactive, and cannot provide data.";

    //Please try using this service again at a later time
    public static final String TRY_AGAIN_LATER = "Please try using this service again at a later time";

    // This service is temporarily unavailable. This may be due to a temporary outage or a permanent change on the 3rd-party-side. Please try using this service again at a later time
    public static final String SERVICE_TEMPORARILY_UNAVAILABLE = "This service is temporarily unavailable. This may be due to a temporary outage or a permanent change on the 3rd-party-side. Please try using this service again at a later time";

    public static final String MISSING_ARGUMENT = "Missing argument. %1$s";

	public static final String PERMISSION_DENIED = "You do not have the permission to complete this operation.";

	public static final String NO_OAUTH_ACCOUNT_GIVEN = "You need to create an OAuth-account first to access this service.";

	// An unexpected error occurred: %1$s.
    public static final String UNEXPECTED_ERROR = "An unexpected error occurred: %1$s.";

    // Please specify your full E-Mail address as login name.
    public static final String EMAIL_ADDR_LOGIN = "Please specify your full E-Mail address as login name.";
}
