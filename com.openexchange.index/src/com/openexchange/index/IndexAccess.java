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
    void addEnvelopeData(Collection<IndexDocument<V>> documents) throws OXException, InterruptedException;

    /**
     * Adds specified document's content to associated index.
     * 
     * @param document The document to add
     * @throws OXException If add operation fails
     */
    void addContent(IndexDocument<V> document) throws OXException;

    /**
     * Adds specified documents' contents to associated index.
     * 
     * @param documents The documents to add
     * @throws OXException If add operation fails
     * @throws InterruptedException If interrupted while adding
     */
    void addContent(Collection<IndexDocument<V>> documents) throws OXException, InterruptedException;

    /**
     * Adds specified document's attachments to associated index.
     * 
     * @param document The document to add
     * @throws OXException If add operation fails
     */
    void addAttachments(IndexDocument<V> document) throws OXException;

    /**
     * Adds specified documents' attachments to associated index.
     * 
     * @param documents The documents to add
     * @throws OXException If add operation fails
     * @throws InterruptedException If interrupted while adding
     */
    void addAttachments(Collection<IndexDocument<V>> documents) throws OXException, InterruptedException;

    /**
     * Changes the denoted fields of already existing document according to specified input document.
     * 
     * @param document The document providing changes
     * @param fields The fields denoting the changes or <code>null</code> to indicate usage of all possible fields.
     * @throws OXException If change operation fails
     * @throws InterruptedException If operation has been interrupted
     */
    void change(IndexDocument<V> document, Set<? extends IndexField> fields) throws OXException, InterruptedException;

    /**
     * Changes the denoted fields of already existing documents according to specified input documents.
     * 
     * @param documents The documents providing changes
     * @param The fields denoting the changes or <code>null</code> to indicate usage of all possible fields.
     * @throws OXException If change operation fails
     * @throws InterruptedException If operation has been interrupted
     */
    void change(Collection<IndexDocument<V>> documents, Set<? extends IndexField> fields) throws OXException, InterruptedException;

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
     * 
     * @param parameters The query parameters
     * @return The query result
     * @throws OXException If query fails
     * @throws InterruptedException If interrupted while retrieving results
     */
    IndexResult<V> query(QueryParameters parameters) throws OXException, InterruptedException;

    /**
     * Gets the trigger type for this access.
     * 
     * @return he trigger type.
     */
    TriggerType getTriggerType();

}
