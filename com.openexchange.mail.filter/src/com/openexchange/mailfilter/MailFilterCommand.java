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

import com.openexchange.exception.OXException;

/**
 * {@link MailFilterCommand} - The interface to perform an arbitrary mail filter commands.
 * <p>
 * Example:
 * <pre>
 * MailFilterCommand myCommand = new MailFilterCommand() {
 *
 *     &#64;Override
 *     public void execute(SieveProtocol sieveProtocol) throws OXException {
 *         // Issue SIEVE command
 *         sieveProtocol.write("MY.CUSTOM.SIEVE.COMMAND1 argument1");
 *
 *         String[] lines = sieveProtocol.readResponseLines();
 *         String responseLine = lines[lines.length - 1];
 *
 *         // Handle lines/response line...
 *
 *         sieveProtocol.handleResponse(responseLine);
 *
 *         // Issue another SIEVE command
 *         sieveProtocol.write("MY.CUSTOM.SIEVE.COMMAND2 argument2");
 *
 *         lines = sieveProtocol.readResponseLines();
 *         responseLine = lines[lines.length - 1];
 *
 *         // Handle lines/response line...
 *
 *         sieveProtocol.handleResponse(responseLine);
 *     }
 * };
 * </pre>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public interface MailFilterCommand {

    /**
     * Executes this command.
     *
     * @param sieveProtocol The SIEVE protocol
     * @throws OXException If command fails
     */
    void execute(SieveProtocol sieveProtocol) throws OXException;

}
