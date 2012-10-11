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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.index;

import java.util.Collection;
import java.util.Set;
import com.openexchange.exception.OXException;

/**
 * {@link IndexAccess} - Provides access to an index.
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface IndexAccess<V> {

    /**
     * Checks if a folder within an account is already indexed. If folder is <code>null</code>, the whole account is checked.
     * 
     * @param accountId The account id.
     * @param folderId The folder id or <code>null</code>.
     * @return <code>true</code> if a folder or account is indexed; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    boolean isIndexed(String accountId, String folderId) throws OXException;

    /**
     * Gets the fields indexed by this access.
     * 
     * @return The indexed fields
     */
    Set<? extends IndexField> getIndexedFields();

    /**
     * Adds specified document's headers to associated index.
     * 
     * @param document The document to add
     * @throws OXException If add operation fails
     */
    void addEnvelopeData(IndexDocument<V> document) throws OXException;

    /**
     * Adds specified documents' headers to associated index.
     * 
     * @param documents The documents to add
     * @throws OXException If add operation fails
     * @throws InterruptedException If interrupted while adding
     */
    void addEnvelopeData(Collection<IndexDocument<V>> documents) throws OXException;

    /**
     * Adds specified document's content to associated index.
     * 
     * @param document The document to add
     * @param full If <code>true</code> the document will be added as a whole. Otherwise it will be loaded from the index and only the
     *            content will be added to the existing document.
     * @throws OXException If parameter full is false but the document could not be found in the index or if an index error occurs.
     */
    void addContent(IndexDocument<V> document, boolean full) throws OXException;

    /**
     * Adds specified documents' contents to associated index.
     * 
     * @param documents The documents to add
     * @param full If <code>true</code> the document will be added as a whole. Otherwise it will be loaded from the index and only the
     *            content will be added to the existing document.
     * @throws OXException If parameter full is false but one of the documents could not be found in the index or if an index error occurs.
     */
    void addContent(Collection<IndexDocument<V>> documents, boolean full) throws OXException;

    /**
     * Adds specified document's attachments to associated index.
     * 
     * @param document The document to add
     * @param full If <code>true</code> the document will be added as a whole. Otherwise it will be loaded from the index and only the
     *            attachment will be added to the existing document.
     * @throws OXException If parameter full is false but the document could not be found in the index or if an index error occurs.
     */
    void addAttachments(IndexDocument<V> document, boolean full) throws OXException;

    /**
     * Adds specified documents' attachments to associated index.
     * 
     * @param documents The documents to add
     * @param full If <code>true</code> the document will be added as a whole. Otherwise it will be loaded from the index and only the
     *            attachment will be added to the existing document.
     * @throws OXException If parameter full is false but one of the documents could not be found in the index or if an index error occurs.
     */
    void addAttachments(Collection<IndexDocument<V>> documents, boolean full) throws OXException;

    /**
     * Changes the denoted fields of already existing document according to specified input document.
     * 
     * @param document The document providing changes
     * @param fields The fields denoting the changes or <code>null</code> to indicate usage of all possible fields.
     * @throws OXException If change operation fails
     * @throws InterruptedException If operation has been interrupted
     */
    void change(IndexDocument<V> document, Set<? extends IndexField> fields) throws OXException;

    /**
     * Changes the denoted fields of already existing documents according to specified input documents.
     * 
     * @param documents The documents providing changes
     * @param fields The fields denoting the changes or <code>null</code> to indicate usage of all possible fields.
     * @throws OXException If change operation fails
     * @throws InterruptedException If operation has been interrupted
     */
    void change(Collection<IndexDocument<V>> documents, Set<? extends IndexField> fields) throws OXException;

    /**
     * Deletes a document by identifier.
     * 
     * @param id The document identifier
     * @throws OXException If delete operation fails
     */
    void deleteById(String id) throws OXException;

    /**
     * Deletes documents by query.
     * 
     * @param query The query string
     * @throws OXException
     */
    void deleteByQuery(QueryParameters parameters) throws OXException;

    /**
     * Queries indexed documents by specified query parameters.
     * <p>
     * Actually a convenience method for {@link #query(QueryParameters, FacetParameters, Set)} with 2nd argument set to <code>null</code>.
     * 
     * @param parameters The query parameters
     * @param fields The fields to be filled within the returned documents. If set to <code>null</code> all known fields will be filled.
     * @return The query result
     * @throws OXException If query fails
     * @throws InterruptedException If interrupted while retrieving results
     */
    IndexResult<V> query(QueryParameters parameters, Set<? extends IndexField> fields) throws OXException;

    /**
     * Queries indexed documents by specified query parameters.
     * 
     * @param parameters The query parameters
     * @param facetParameters The <i>optional</i> facet parameters (pass <code>null</code> to not perform any facets)
     * @param fields The fields to be filled within the returned documents. If set to <code>null</code> all known fields will be filled.
     * @return The query result
     * @throws OXException If query fails
     * @throws InterruptedException If interrupted while retrieving results
     */
    IndexResult<V> query(QueryParameters parameters, FacetParameters facetParameters, Set<? extends IndexField> fields) throws OXException;

}
