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

package com.openexchange.mail.authenticity.impl.core;

import java.util.List;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.mail.authenticity.AllowedAuthServId;
import com.openexchange.mail.dataobjects.MailAuthenticityResult;

/**
 * {@link AuthenticationResultsValidator} - Parses and validates the <code>Authentication-Results</code> headers and <code>From</code> header of an E-Mail.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public interface AuthenticationResultsValidator {

    /**
     * Performs the parsing magic of the specified <code>Authentication-Results</code> headers and <code>From</code> header and
     * returns the overall {@link MailAuthenticityResult}
     *
     * @param authenticationHeaders The <code>Authentication-Results</code> headers
     * @param allowedAuthServIds The allowed authserv-ids
     * @param fromHeader The <code>From</code> header
     * @return The overall {@link MailAuthenticityResult}
     * @throws OXException if the allowed authserv-ids cannot be retrieved from the configuration or any other parsing error occurs
     */
    MailAuthenticityResult parseHeaders(List<String> authenticationHeaders, InternetAddress from, List<AllowedAuthServId> allowedAuthServIds) throws OXException;

}
