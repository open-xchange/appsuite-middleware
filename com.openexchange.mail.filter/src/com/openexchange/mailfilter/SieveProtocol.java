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

package com.openexchange.mailfilter;

import java.util.Optional;
import com.openexchange.exception.OXException;

/**
 * {@link SieveProtocol} - Provides low-level access to mail filter/SIEVE server.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public interface SieveProtocol {

    /**
     * Writes given command line to mail filter/SIEVE end-point.
     *
     * @param commandLine The command line
     * @throws OXException If writing command line fails
     */
    default void write(String commandLine) throws OXException {
        write(new String[] {commandLine});
    }

    /**
     * Writes given command lines to mail filter/SIEVE end-point.
     *
     * @param commandLines The command lines
     * @throws OXException If writing command lines fails
     */
    void write(String... commandLines) throws OXException;

    /**
     * Reads response lines from mail filter/SIEVE end-point.
     *
     * @return The response lines
     * @throws OXException If reading fails
     */
    String[] readResponseLines() throws OXException;

    /**
     * Handles the last response line as obtained from {@link #readResponseLines(Optional)} (and {@link #readResponseLines()} respectively).
     *
     * @param responseLine The (last) response line to handle
     * @throws OXException If a SIEVE protocol error happens (e.g. <code>NO</code> response)
     */
    default void handleResponse(String responseLine) throws OXException {
        handleResponse(responseLine, Optional.empty());
    }

    /**
     * Handles the last response line as obtained from {@link #readResponseLines()}.
     * <p>
     * If <code>optionalHandler</code> is absent, a SIEVE <code>NO</code> response is quit with <code>"Command failed"</code> error message.
     *
     * @param responseLine The (last) response line to handle
     * @param The optional <code>NO</code> response handler; if absent a SIEVE <code>NO</code> response is quit with <code>"Command failed"</code> error message.
     * @throws OXException If a SIEVE protocol error happens (e.g. <code>NO</code> response)
     */
    void handleResponse(String responseLine, Optional<NoResponseHandler> optionalHandler) throws OXException;

}
