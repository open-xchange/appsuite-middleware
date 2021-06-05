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

package com.openexchange.snippet;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;

/**
 * {@link SnippetManagement} - The snippet management for a certain session serving <code>CRUD</code> (<b>c</b>reate, <b>r</b>ead, <b>u</b>pdate, and <b>d</b>elete) operations.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface SnippetManagement {

    /**
     * Gets all available snippets.
     *
     * @param The optional types to filter against
     * @return All available snippets
     * @throws OXException If retrieval operation fails
     */
    List<Snippet> getSnippets(String... types) throws OXException;

    /**
     * Gets all snippets belonging to associated user.
     *
     * @return All user-associated snippets
     * @throws OXException If retrieval operation fails
     */
    List<Snippet> getOwnSnippets() throws OXException;

    /**
     * Gets the number of all snippets belonging to associated user.
     *
     * @return The number of all user-associated snippets
     * @throws OXException If retrieval operation fails
     */
    int getOwnSnippetsCount() throws OXException;

    /**
     * Gets a snippet by specified identifier.
     *
     * @param id The identifier
     * @return The snippet
     * @throws OXException If such a snippet does not exist
     */
    Snippet getSnippet(String id) throws OXException;

    /**
     * Creates specified snippet.
     *
     * @param snippet The snippet to create
     * @return The newly created snippet's identifier
     * @throws OXException If create operation fails
     */
    String createSnippet(Snippet snippet) throws OXException;

    /**
     * Updates specified snippet.
     *
     * @param id The identifier of the snippet to update
     * @param snippet The snippet providing the data to update
     * @param properties The properties to update
     * @param addAttachments The attachments to add
     * @param removeAttachments The attachments to remove
     * @return The updated snippet's identifier
     * @throws OXException If update operation fails
     */
    String updateSnippet(String id, Snippet snippet, Set<Property> properties, Collection<Attachment> addAttachments, Collection<Attachment> removeAttachments) throws OXException;

    /**
     * Deletes specified snippet.
     *
     * @param id The identifier of the snippet to delete
     * @throws OXException If delete operation fails
     */
    void deleteSnippet(String id) throws OXException;

}
