/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
