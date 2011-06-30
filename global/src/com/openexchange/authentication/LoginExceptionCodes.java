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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.authentication;

import static com.openexchange.authentication.LoginExceptionMessages.*;
import com.openexchange.authentication.exception.LoginExceptionFactory;
import com.openexchange.exceptions.OXErrorMessage;
import com.openexchange.groupware.AbstractOXException.Category;

/**
 * Defines all error messages for the LoginException.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public enum LoginExceptionCodes implements OXErrorMessage {

    /** Account "%s" is locked. */
    ACCOUNT_LOCKED(ACCOUNT_LOCKED_MSG, Category.PERMISSION, 1),
    /** Account "%s" is not ready yet. */
    ACCOUNT_NOT_READY_YET(ACCOUNT_NOT_READY_YET_MSG, Category.TRY_AGAIN, 2),
    /** Unknown problem: "%s". */
    UNKNOWN(UNKNOWN_MSG, Category.CODE_ERROR, 3),
    /** Login not possible at the moment. Please try again later. */
    COMMUNICATION(COMMUNICATION_MSG, Category.SUBSYSTEM_OR_SERVICE_DOWN, 5),
    /** Invalid credentials. */
    INVALID_CREDENTIALS(INVALID_CREDENTIALS_MSG, Category.USER_INPUT, 6),
    /** Instantiating the class failed. */
    @Deprecated
    INSTANTIATION_FAILED(INSTANTIATION_FAILED_MSG, Category.CODE_ERROR, 7),
    /** Class %1$s can not be found. */
    @Deprecated
    CLASS_NOT_FOUND(CLASS_NOT_FOUND_MSG, Category.SETUP_ERROR, 8),
    /** Missing property %1$s. */
    MISSING_PROPERTY(MISSING_PROPERTY_MSG, Category.SETUP_ERROR, 9),
    /** database down. */
    DATABASE_DOWN(DATABASE_DOWN_MSG, Category.SUBSYSTEM_OR_SERVICE_DOWN, 10),
    /** Your password expired. */
    PASSWORD_EXPIRED(PASSWORD_EXPIRED_MSG, Category.PERMISSION, 11),
    /** User %1$s could not be found in context %2$s. */
    USER_NOT_FOUND(USER_NOT_FOUND_MSG, Category.CODE_ERROR, 12),
    /** User is not activated. */
    USER_NOT_ACTIVE(USER_NOT_ACTIVE_MSG, Category.PERMISSION, 13),
    /** Client "%1$s" is not activated. */
    CLIENT_DENIED(CLIENT_DENIED_MSG, Category.USER_CONFIGURATION, 14),
    /** Method "%1$s" in HTTP header authorization is not supported. */
    UNKNOWN_HTTP_AUTHORIZATION(UNKNOWN_HTTP_AUTHORIZATION_MSG, Category.TRY_AGAIN, 15);

    final String message;

    final Category category;

    final int number;

    private LoginExceptionCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.category = category;
        number = detailNumber;
    }

    public Category getCategory() {
        return category;
    }

    public String getMessage() {
        return message;
    }

    public int getDetailNumber() {
        return number;
    }

    public String getHelp() {
        // TODO Auto-generated method stub
        return null;
    }

    public LoginException create(final Object... messageArgs) {
        return LoginExceptionFactory.getInstance().create(this, messageArgs);
    }

    public LoginException create(final Throwable cause, final Object... messageArgs) {
        return LoginExceptionFactory.getInstance().create(this, cause, messageArgs);
    }
}
