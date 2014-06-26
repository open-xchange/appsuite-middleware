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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.mail.smal.impl.adapter;

import java.util.Collection;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.session.Session;

/**
 * {@link IndexAdapter} - The adapter for a search index.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface IndexAdapter {

    /**
     * Starts the index adapter.
     *
     * @throws OXException If start-up fails
     */
    public void start() throws OXException;

    /**
     * Stops the index adapter.
     *
     * @throws OXException If shut-down fails
     */
    public void stop() throws OXException;

    /**
     * Gets the indexable fields.
     *
     * @return The indexable fields
     * @throws OXException If an error occurs
     */
    public MailFields getIndexableFields() throws OXException;

    /**
     * Invoked if a new session is added or restored from log-term container.
     *
     * @param session The session
     * @throws OXException If handling new session fails
     */
    public void onSessionAdd(Session session) throws OXException;

    /**
     * Invoked if a session is dropped or moved to long-term container.
     *
     * @param session The session
     * @throws OXException If handling dropped session fails
     */
    public void onSessionGone(Session session) throws OXException;

    /**
     * Gets all mails in a fast manner. Returned mails only contain identifier and flags.
     *
     * @param optFullName The optional full name to restrict search results to specified folder
     * @param optAccountId The optional account identifier or <code>-1</code> to not restrict to a certain account
     * @param session The session
     * @return All mails
     * @throws OXException If all request fails
     * @throws InterruptedException If processing is interrupted
     */
    public List<MailMessage> all(String optFullName, int optAccountId, Session session) throws OXException, InterruptedException;

    /**
     * Performs the query derived from given search term.
     *
     * @param optFullName The optional full name to restrict search results to specified folder
     * @param searchTerm The search term
     * @param sortField The sort field
     * @param order The order direction
     * @param fields The fields to pre-fill in returned {@link MailMessage} instances; if <code>null</code> all available fields are filled
     * @param indexRange The index range
     * @param optAccountId The optional account identifier or <code>-1</code> to not restrict to a certain account
     * @param session The session
     * @param more Optional <code>boolean</code> array if more results available than requested
     * @return The search result
     * @throws OXException If search fails
     * @throws InterruptedException If processing is interrupted
     */
    public List<MailMessage> search(String optFullName, SearchTerm<?> searchTerm, MailSortField sortField, OrderDirection order, MailField[] fields, IndexRange indexRange, int optAccountId, Session session, boolean[] more) throws OXException, InterruptedException;

    /**
     * Performs specified search by provided query.
     *
     * @param query The query
     * @param fields The fields to set
     * @param session The session
     * @return The resulting mails
     * @throws OXException If search fails
     * @throws InterruptedException If processing is interrupted
     */
    public List<MailMessage> search(String query, MailField[] fields, Session session) throws OXException, InterruptedException;

    /**
     * Gets specified mails located in given folder.
     *
     * @param optMailIds The mail identifiers; pass <code>null</code> to get all messages in folder
     * @param fullName The folder full name
     * @param accountId The account identifier
     * @param session The session
     * @return <code>true</code> if folder is contained; otherwise <code>false</code>
     * @throws OXException If check fails
     * @throws InterruptedException If processing is interrupted
     */
    public List<MailMessage> getMessages(String[] optMailIds, String fullName, MailSortField sortField, OrderDirection order, MailField[] fields, int accountId, Session session) throws OXException, InterruptedException;

    /**
     * Deletes specified mails from index.
     *
     * @param mailIds The mail identifiers
     * @param fullName The folder full name
     * @param accountId The account identifier
     * @param session The session
     * @throws OXException If deletion fails
     * @throws InterruptedException If processing is interrupted
     */
    public void deleteMessages(Collection<String> mailIds, String fullName, int accountId, Session session) throws OXException, InterruptedException;

    /**
     * Deletes all mails from specified folder from index.
     *
     * @param fullName The folder full name
     * @param accountId The account identifier
     * @param session The session
     * @throws OXException If deletion fails
     */
    public void deleteFolder(String fullName, int accountId, Session session) throws OXException;

    /**
     * Checks if index contains mail located in specified folder.
     *
     * @param fullName The folder full name
     * @param accountId The account identifier
     * @param session The session
     * @return <code>true</code> if folder is contained; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    public boolean containsFolder(String fullName, int accountId, Session session) throws OXException;

    /**
     * Changes the flags of specified mails in index.
     *
     * @param mails The mails with changed flags
     * @param session The user session
     * @throws OXException If changing mails in index fails
     */
    public void change(List<MailMessage> mails, Session session) throws OXException, InterruptedException;

    /**
     * Adds specified mail to the index.
     *
     * @param mail The mail to add
     * @param session The session
     * @throws OXException If adding mail to index fails
     */
    public void add(MailMessage mail, Session session) throws OXException;

    /**
     * Adds specified mails to the index.
     *
     * @param mails The mails to add
     * @param session The session
     * @throws OXException If adding mails to index fails
     * @throws InterruptedException If processing is interrupted
     */
    public void add(List<MailMessage> mails, Session session) throws OXException, InterruptedException;

    /**
     * Adds specified mail's content to the index.
     *
     * @param mail The mail from which the content shall be added
     * @param session The session
     * @throws OXException If adding mail's content to index fails
     */
    public void addContent(MailMessage mail, Session session) throws OXException;

    /**
     * Adds contents of previously added mails to index.
     *
     * @throws OXException If flushing contents fails
     */
    public void addContents() throws OXException;

}
