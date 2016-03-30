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

package com.openexchange.sessiond;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link SessionExceptionMessages}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class SessionExceptionMessages implements LocalizableStrings {

    public static final String MAX_SESSION_EXCEPTION_MSG = "The maximum number of sessions is exceeded. Please try again later.";
    public static final String PASSWORD_UPDATE_FAILED_MSG = "The password could not be changed in the current session. Please login again.";
    public static final String MAX_SESSION_PER_USER_EXCEPTION_MSG = "The maximum number of sessions is exceeded for your account. Please logout from other clients and try again.";
    public static final String SESSION_EXPIRED_MSG = "Your session expired. Please login again.";
    public static final String SESSION_INVALIDATED_MSG = "Your session was invalidated. Please login again.";
    public static final String CONTEXT_LOCKED_MSG = "The account \"%2$s\" is currently not enabled. Please try again later.";
    public static final String MAX_SESSION_PER_CLIENT_EXCEPTION_MSG = "The maximum number of sessions is exceeded for client %1$s. Please logout from other clients and try again.";
    public static final String NO_SESSION_FOR_TOKENS_MSG = "The session is no longer available. Please try again.";
    public static final String KERBEROS_TICKET_MISSING_MSG = "Kerberos ticket in session is missing. Pleasy try again.";

    private SessionExceptionMessages() {
        super();
    }
}
