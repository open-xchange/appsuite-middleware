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

package com.openexchange.imap.commandexecutor;

import java.util.Optional;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.ResponseEvent.StatusResponse;

/**
 * {@link ExecutedCommand} - Represents an executed IMAP command.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class ExecutedCommand {

    /** The determined status response */
    public final Optional<StatusResponse> optionalStatusResponse;

    /** The response array */
    public final Response[] responses;

    /**
     * Initializes a new {@link ExecutedCommand}.
     *
     * @param responses The response array
     */
    public ExecutedCommand(Response[] responses) {
        this(null, responses);
    }

    /**
     * Initializes a new {@link ExecutedCommand}.
     *
     * @param statusResponse The status response or <code>null</code>
     * @param responses The response array
     */
    public ExecutedCommand(StatusResponse statusResponse, Response[] responses) {
        super();
        this.optionalStatusResponse = Optional.ofNullable(statusResponse);
        this.responses = responses;
    }

}
