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

package com.openexchange.passwordchange.history;

import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 *
 * {@link PasswordChangeRecorderRegistryService} - Registry to get available {@link PasswordChangeRecorder}s from
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
@SingletonService
public interface PasswordChangeRecorderRegistryService {

    /**
     * Gets all available {@link PasswordChangeRecorder recorders}
     *
     * @return The registered recorders
     */
    Map<String, PasswordChangeRecorder> getRecorders();

    /**
     * Gets a specific {@link PasswordChangeRecorder recorder} for given name
     *
     * @param symbolicName The name of the recorders
     * @return The recorder or <code>null</code>
     */
    PasswordChangeRecorder getRecorder(String symbolicName);

    /**
     * Gets the suitable {@link PasswordChangeRecorder recorder} for given user.
     * <p>
     * Throws {@link PasswordChangeRecorderException#DENIED_FOR_GUESTS} in case specified user appears to be a guest user.<br>
     * Throws {@link PasswordChangeRecorderException#DISABLED} in case password change recording is disabled for given user.
     *
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The recorder
     * @throws OXException If there is no suitable recorder for given user
     */
    PasswordChangeRecorder getRecorderForUser(int userId, int contextId) throws OXException;

}
