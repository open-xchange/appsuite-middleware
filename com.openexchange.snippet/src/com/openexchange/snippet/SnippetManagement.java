/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.snippet;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;

/**
 * {@link SnippetManagement} - The snippet management for <code>CRUD</code> (<b>c</b>reate, <b>r</b>ead, <b>u</b>pdate, and <b>d</b>elete)
 * operations.
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
     * Updates specified snippet.
     *
     * @param id The identifier of the snippet to delete
     * @throws OXException If delete operation fails
     */
    void deleteSnippet(String id) throws OXException;

}
