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

package com.openexchange.sessiond;

/**
 * {@link InvalidatedLookupService} - Tests whether the denoted user's sessions have been invalidated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface InvalidatedLookupService {

    /**
     * Tests whether the denoted user's sessions have been invalidated. The <i>invalidated status</i> of the user is cleared by this method.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if invalidated; otherwise <code>false</code>
     */
    boolean invalidated(int userId, int contextId);

}
