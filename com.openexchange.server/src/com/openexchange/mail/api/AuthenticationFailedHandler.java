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

package com.openexchange.mail.api;

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link AuthenticationFailedHandler} - Handles failed authentications that occurred while attempting to connect/log-in to a certain service.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public interface AuthenticationFailedHandler {

    /** The result for a handled authentication error */
    public static enum Result {
        /**
         * Possible further handlers are supposed to be invoked.
         */
        NEUTRAL,
        /**
         * Possible further handlers are <b>not</b> supposed to be invoked.
         */
        ABORT;
    }

    /** The type of service that yielded the failed authentication */
    public static enum Service {
        /**
         * The mail access service; e.g. IMAP
         */
        MAIL,
        /**
         * The mail transport service; e.g. SMTP
         */
        TRANSPORT,
        /**
         * The mail filter service; e.g. SIEVE
         */
        MAIL_FILTER;
    }

    /**
     * This method is called in case the authentication has failed.
     *
     * @param failedAuthentication The optional {@code OXException} instance that reflects the failed authentication
     * @param service The type of service that yielded the failed authentication
     * @param mailConfig The effective mail configuration for affected user
     * @param session The user which couln't be authenticated.
     * @return The result that controls whether to proceed in invocation chain
     * @throws OXException If handling the failed authentication is supported being aborted with an error
     */
    Result handleAuthenticationFailed(OXException failedAuthentication, Service service, MailConfig mailConfig, Session session) throws OXException;

}
