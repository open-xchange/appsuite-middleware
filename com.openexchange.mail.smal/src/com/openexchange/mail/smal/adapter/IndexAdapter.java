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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.mail.smal.adapter;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailField;
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
     * Performs the query derived from given search term.
     * 
     * @param searchTerm The search term
     * @param sortField The sort field
     * @param order The order direction
     * @param session The session
     * @return The search result
     * @throws OXException If search fails
     */
    public List<MailMessage> search(SearchTerm<?> searchTerm, MailSortField sortField, OrderDirection order, Session session) throws OXException;

    /**
     * Checks if index contains mail located in specified folder.
     * 
     * @param fullName The folder full name
     * @param accountId The account identifier
     * @param session The session
     * @return <code>true</code> if folder is contained; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    public List<MailMessage> getMessages(String fullName, MailSortField sortField, OrderDirection order, MailField[] fields, int accountId, Session session) throws OXException;

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
     */
    public void add(MailMessage[] mails, Session session) throws OXException;

    /**
     * Synchronizes mails contained given folder with the index.
     * 
     * @param fullName The folder full name
     * @param The account identifier
     * @param session The session
     * @return <code>true</code> if invocation triggered sync; otherwise <code>false</code>
     * @throws OXException If synchronizing mails with index fails
     */
    public boolean sync(String fullName, int accountId, Session session) throws OXException;

}
