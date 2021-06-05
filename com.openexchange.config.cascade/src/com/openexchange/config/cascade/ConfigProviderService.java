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

package com.openexchange.config.cascade;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;


/**
 * {@link ConfigProviderService} - Provides access to properties/attributes obeying a certain scope.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Added JavaDoc
 */
public interface ConfigProviderService {

    /** Constant for no context */
    public static final int NO_CONTEXT = -1;

    /** Constant for no user */
    public static final int NO_USER = -1;

    /** Constant for no property */
    public static final BasicProperty NO_PROPERTY = new BasicProperty() {

        @Override
        public String get() throws OXException {
            return null;
        }

        @Override
        public String get(final String metadataName) throws OXException {
            return null;
        }

        @Override
        public boolean isDefined() throws OXException {
            return false;
        }

        @Override
        public void set(final String value) throws OXException {
            // Ignore
        }

        @Override
        public void set(final String metadataName, final String value) throws OXException {
            // Ignore
        }

        @Override
        public List<String> getMetadataNames() throws OXException {
            return Collections.emptyList();
        }

    };

    /**
     * Gets the denoted property.
     *
     * @param propertyName The property name
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The property if found; otherwise {@link #NO_PROPERTY}
     * @throws OXException If returning property fails for any reason
     */
    BasicProperty get(String propertyName, int contextId, int userId) throws OXException;

    /**
     * Gets all property names for specified user.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The property names
     * @throws OXException If returning property names fails for any reason
     */
    Collection<String> getAllPropertyNames(int contextId, int userId) throws OXException;

	/**
	 * Gets the scope of this provider
	 * <p>
	 * Currently known scopes:
	 * <ul>
	 * <li><code>"server"</code></li>
	 * <li><code>"reseller"</code></li>
	 * <li><code>"contextSets"</code></li>
	 * <li><code>"context"</code></li>
	 * <li><code>"user"</code></li>
	 * </ul>
	 *
	 * @return The scope of the provider
	 */
	String getScope();

}
