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

package com.openexchange.multifactor.exceptions;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link MultifactorExceptionMessages}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public class MultifactorExceptionMessages implements LocalizableStrings {

    /** The client did not perform multifactor authentication but it is requried */
    public static final String MISSING_AUTHENTICATION_FACTOR_MESSAGE = "Missing multifactor authentication";

    /** This multifactor authentication was invalid / wrong */
    public static final String  INVALID_AUTHENTICATION_FACTOR_MESSAGE = "Invalid multifactor authentication";

    /** Error occurred when creating the authentication */
    public static final String  ERROR_CREATING_FACTOR_MESSAGE = "An error occured while creating/calculating the authentication factor: %1$s.";

    /** A service is not available */
    public static final String  SERVICE_UNAVAILABLE_MESSAGE = "The required service %1$s is temporary not available. Please try again later.";

    /** Data format error */
    public static final String  JSON_ERROR_MESSAGE = "JSON error: %s";

    /** The type of multifactor provider is not known */
    public static final String  UNKNOWN_PROVIDER_MESSAGE = "The requested provider %1$s is unknown.";

    /** This multifactor provider is not available for the user */
    public static final String  PROVIDER_NOT_AVAILABLE_MESSAGE = "The requested provider %1$s is not available for the user.";

    /** Multifactor is required, but the device identifier is missing */
    public static final String  MISSING_DEVICE_ID_MESSAGE = "Multifactor authentication required but device identifier is missing";

    /** The specified device is not known */
    public static final String  UNKNOWN_DEVICE_ID_MESSAGE = "The requested device is unknown";

    /** Unknown error */
    public static final String  UNKNOWN_ERROR_MESSAGE = "An unknown error occured during multifactor authentication: %s";

    /** The provider type is not specified */
    public static final String  MISSING_PROVIDER_NAME_MESSAGE = "Multifactor authentication required but provider name is missing";

    /** Database error */
    public static final String  SQL_EXCEPTION_MESSAGE = "SQL error: %1$s";

    /** Missing a parameter for this call */
    public static final String  MISSING_PARAMETER_MESSAGE = "Missing parameter: %1$s";

    /** This device is already registered */
    public static final String  DEVICE_ALREADY_REGISTERED_MESSAGE = "The device is already registered";

    /** This action requires a recent multifactor authentication.  Multifactor parameters are missing from the call */
    public static final String  ACTION_REQUIRES_REAUTHENTICATION_MESSAGE = "This action requires recent multifactor authentication.  Missing multifactor parameters for this action";

    /** Specified device is not regisiterd */
    public static final String  MISSING_REGISTRATION_MESSAGE = "This device isn't registered";

    /** No devices found for the provider */
    public static final String  NO_DEVICES_MESSAGE = "No devices in this provider";

    /** Argument exceeded the allowed length */
    public static final String INVALID_ARGUMENT_LENGTH = "The provided agument \"%1$s\" of length %2$s exceeded the allowed length of %3$s";

    /** The registration failed */
    public static final String REGISTRATION_FAILED_MESSAGE = "The registration failed";

    /** The registration failed */
    public static final String DEVICE_REMOVAL_FAILED_MESSAGE = "Unable to remove the device";

    /** The authentication failed */
    public static final String AUTHENTICATION_FAILED = "The authentication failed";

    /** The authentication failed with details */
    public static final String AUTHENTICATION_FAILED_EXT = "The authentication failed: %1$s";

    /** The authentication failed.  Specified attempts allowed before lockout */
    public static final String AUTHENTICATION_WITH_LOCKOUT = "The authentication failed.  A total of %1$d attempts allowed before temporary lockout.";

    private MultifactorExceptionMessages () {
        super();
    }

}
