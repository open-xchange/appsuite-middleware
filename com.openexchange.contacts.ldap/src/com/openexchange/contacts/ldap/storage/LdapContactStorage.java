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

package com.openexchange.contacts.ldap.storage;

import java.util.Date;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.storage.DefaultContactStorage;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.search.SearchTerm;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;


/**
 * {@link LdapContactStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 *
 */
public class LdapContactStorage extends DefaultContactStorage {

    @Override
    public boolean supports(final Session session, final String folderId) throws OXException {
        return Integer.toString(FolderObject.SYSTEM_LDAP_FOLDER_ID).equals(folderId);
    }

    @Override
    public Contact get(final Session session, final String folderId, final String id, final ContactField[] fields) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SearchIterator<Contact> all(final Session session, final String folderId, final ContactField[] fields, final SortOptions sortOptions) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SearchIterator<Contact> list(final Session session, final String folderId, final String[] ids, final ContactField[] fields, final SortOptions sortOptions) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SearchIterator<Contact> deleted(final Session session, final String folderId, final Date since, final ContactField[] fields) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SearchIterator<Contact> deleted(final Session session, final String folderId, final Date since, final ContactField[] fields, final SortOptions sortOptions) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SearchIterator<Contact> modified(final Session session, final String folderId, final Date since, final ContactField[] fields) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SearchIterator<Contact> modified(final Session session, final String folderID, final Date since, final ContactField[] fields, final SortOptions sortOptions) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <O> SearchIterator<Contact> search(final Session session, final SearchTerm<O> term, final ContactField[] fields, final SortOptions sortOptions) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SearchIterator<Contact> search(final Session session, final ContactSearchObject contactSearch, final ContactField[] fields, final SortOptions sortOptions) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void create(final Session session, final String folderId, final Contact contact) throws OXException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void update(final Session session, final String folderId, final String id, final Contact contact, final Date lastRead) throws OXException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateReferences(final Session session, final Contact contact) throws OXException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void delete(final Session session, final int userID, final String folderId, final String id, final Date lastRead) throws OXException {
        // TODO Auto-generated method stub
        
    }

}
