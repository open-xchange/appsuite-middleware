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

package com.openexchange.subscribe;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link SubscriptionErrorStrings}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SubscriptionErrorStrings implements LocalizableStrings {

    // The message displayed if the requested cannot be found.
    public static final String CANT_FIND_SUBSCRIPTION_DISPLAY = "Cannot find the requested subscription.";

    // The message displayed if the server is not able to access the 3rd party service with the given credentials.
    public static final String WRONG_PASSWORD_DISPLAY = "The login/password combination you entered is wrong.";

    // The message displayed if a required service (e. g. for adding a subscription) is currently not available.
    public static final String SERVICE_UNAVAILABLE_DISPLAY = "A required service is currently not available. Please try again later.";

    // The message displayed if an argument is missing. %1$s defines the missing argument.
    public static final String MISSING_ARGUMENT_DISPLAY = "The argument %1$s is missing.";

    // The message displayed if the user misses some permissions.
    public static final String PERMISSION_DENIED_DISPLAY = "You do not have the appropriate permissions to complete this operation.";

    // The message displayed if a non valid email address is inserted.
    public static final String EMAIL_ADDR_LOGIN_DISPLAY = "Please specify your full E-Mail address as login name.";

    // The message displayed if a required service (e. g. for adding a subscription) is currently not available.
    public static final String INACTIVE_SOURCE_DISPLAY = "The source the subscription belongs to is currently inactive, and cannot provide data.";

    // The message displayed if the user would like to access the service but does not have an OAuth account.
    public static final String NO_OAUTH_ACCOUNT_GIVEN_DISPLAY = "You need to create an OAuth-account first to access this service.";

    public static final String NEED_VERIFICATION_DISPLAY = "Your account needs to be verified: %1$s";

    // The service provider asked for an identity confirmation. This happens for some accounts and cannot fixed by us.
    // It is in the provider's responsibility. For this reason, the subscription cannot be completed.
    public static final String ABORT_IDENTITY_CONFIRMATION_DISPLAY = "The service provider asked for an identity confirmation. This happens for some accounts and cannot fixed by us. It is in the provider's responsibility. For this reason, the subscription cannot be completed.";

    // You already have such a subscription.
    public static final String DUPLICATE_SUBSCRIPTION_DISPLAY = "You already have such a subscription.";
}
