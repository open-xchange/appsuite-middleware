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

package com.openexchange.groupware.infostore.search;

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.utils.Metadata;


/**
 * {@link SearchTerm}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since 7.6.0
 */
public interface SearchTerm<T> {

    /**
     * Gets the pattern to which the expression should match.
     *
     * @return The pattern
     */
    T getPattern();

    /**
    * Handles given visitor for this search term.
    *
    * @param visitor The visitor
    * @throws OXException If visitor invocation fails
    */
    void visit(SearchTermVisitor visitor) throws OXException;

    /**
     * Adds the addressed field to specified collection
     *
     * @param col The collection which gathers addressed fields
     */
    void addField(Collection<Metadata> col);

    /**
     * Checks if given file matches this search term
     *
     * @param file The file to check
     * @return <code>true</code> if file matches this search term; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    boolean matches(DocumentMetadata file) throws OXException;

}
