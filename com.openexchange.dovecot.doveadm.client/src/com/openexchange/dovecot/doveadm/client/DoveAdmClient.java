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

package com.openexchange.dovecot.doveadm.client;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link DoveAdmClient} - The client for DoveAdm REST API.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
@SingletonService
public interface DoveAdmClient {

    /**
     * Checks specified user identifier and prepares it for being used
     *
     * @param user The user identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The checked user identifier
     * @throws OXException If checking user identifier fails
     */
    String checkUser(String user, int userId, int contextId) throws OXException;

    /**
     * Executes the specified DoveAdm command.
     *
     * @param command The DoveAdm command to execute
     * @return The DoveAdm response
     * @throws OXException If executing the command fails
     */
    DoveAdmResponse executeCommand(DoveAdmCommand command) throws OXException;

    /**
     * Executes the specified DoveAdm commands.
     *
     * @param commands The DoveAdm commands to execute
     * @return The DoveAdm responses
     * @throws OXException If executing the commands fails
     */
    List<DoveAdmResponse> executeCommands(List<DoveAdmCommand> commands) throws OXException;

}
