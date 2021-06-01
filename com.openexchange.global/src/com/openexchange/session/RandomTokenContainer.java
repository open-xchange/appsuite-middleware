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

package com.openexchange.session;

/**
 * A {@link RandomTokenContainer} maintains an association of a random token string to a certain value. The lifetime of the association is
 * scoped by the lifetime of a session.
 *
 * @see SessionSpecificContainerRetrievalService
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface RandomTokenContainer<T> {

    /**
     * Generates a new random token an associates the value with it. The lifetime of this association is bound by the lifetime of the given
     * session.
     *
     * @param session The session that the lifetime of this association is bound to.
     * @param value The value to store.
     * @return The newly created random token
     */
    public String rememberForSession(Session session, T value);

    /**
     * Retrieves a previously stored value for a given token.
     * @param token The token to try to retrieve the value for.
     * @return The previously stored value, or <code>null</code> if the association expired or no value was stored for this token.
     */
    public T get(String token);

    /**
     * Removes a value from this token container. If the value is found the clean up operation will be used on it.
     * @param token The token used to store the value.
     * @return The value after having run clean up on it.
     */
    public T remove(String token);

}
