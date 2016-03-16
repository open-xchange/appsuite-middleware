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

package com.openexchange.kerberos;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link KerberosExceptionMessages}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
final class KerberosExceptionMessages implements LocalizableStrings {

    // The client send a ticket within the browser request and the verification of this ticket failed.
    static final String TICKET_WRONG_MSG = "Verification of client ticket failed.";

    // The Kerberos ticket granting service denied to issue a delegate ticket for the client.
    static final String DELEGATE_FAILED_MSG = "Failed to get a delegate ticket.";

    // Some problem occurred while talking to Kerberos server.
    static final String COMM_FAILED_MSG = "Communication to Kerberos server failed.";

    // Initial login of this service on the Kerberos server failed.
    static final String LOGIN_FAILED_MSG = "Authenticating this service against the Kerberos server failed.";

    // When shutting down this service the termination of the service ticket failed.
    static final String LOGOUT_FAILED_MSG = "Problem while terminating service ticket.";

    // Written to the log file if the session contains a Kerberos subject that does not contain any ticket that needs a renewal.
    // %1$s is replaced with the subjects principal name.
    static final String NO_CREDENTIALS_MSG = "Can not find credentials in subject %1$s that need a renewal.";

    // Written to the log file if the session does not contain a Kerberos subject for the delegation.
    static final String TICKET_MISSING_MSG = "No Kerberos delegation ticket found in session.";

    private KerberosExceptionMessages() {
        super();
    }
}
