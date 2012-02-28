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

package com.openexchange.contact.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.ContactMerger;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.internal.operands.ColumnOperand;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link DefaultContactStorage} - Abstract {@link ContactStorage} implementation.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class DefaultContactStorage implements ContactStorage {
    
    /**
     * An overwriting {@link ContactMerger}.
     */
    private static final ContactMerger MERGER = new ContactMerger(true);
    
    /**
     * Initializes a new {@link DefaultContactStorage}.
     */
    public DefaultContactStorage() {
        super();
    }
    
    @Override
    public Contact get(final Session session, final String folderId, final String id) throws OXException {
        return this.get(session, folderId, id, allFields());
    }

    @Override
    public Collection<Contact> all(final Session session, final String folderId) throws OXException {
        return this.all(session, folderId, allFields());
    }
    
    /**
     * Default implementation that uses <code>ContactStorage.search</code> internally. 
     * Override if applicable for concrete storage implementation.
     */
    @Override
    public Collection<Contact> all(final Session session, final String folderId, final ContactField[] fields) throws OXException {
        return this.search(session, getSearchTermFor(folderId), fields);
    }

    @Override
    public Collection<Contact> list(final Session session, final String folderId, final String[] ids) throws OXException {
        return this.list(session, folderId, ids, allFields());
    }

    @Override
    public Collection<Contact> deleted(final Session session, final String folderId, final Date since) throws OXException {
        return this.deleted(session, folderId, since, allFields());
    }

    /**
     * Default implementation that uses <code>ContactStorage.search</code> internally. 
     * Override if applicable for concrete storage implementation.
     */
    @Override
    public Collection<Contact> list(final Session session, final String folderId, final String[] ids, final ContactField[] fields) 
        throws OXException {
        final CompositeSearchTerm andTerm = new CompositeSearchTerm(CompositeOperation.AND);
        andTerm.addSearchTerm(getSearchTermFor(folderId));
        final CompositeSearchTerm idsTerm = new CompositeSearchTerm(CompositeOperation.OR);
        for (final String id : ids) {
            final SingleSearchTerm idTerm = new SingleSearchTerm(SingleSearchTerm.SingleOperation.EQUALS);
            idTerm.addOperand(new ColumnOperand(ContactField.OBJECT_ID.getAjaxName()));
            idTerm.addOperand(new ConstantOperand<String>(id));
        }
        andTerm.addSearchTerm(idsTerm);
        return this.search(session, andTerm, fields);
    }
    
    @Override
    public <O> Collection<Contact> search(Session session, SearchTerm<O> term) throws OXException {
        return this.search(session, term, allFields());
    }
    
    /**
     * Gets the adapted server session.
     * 
     * @param session the session
     * @return the server session
     * @throws OXException
     */
    protected static ServerSession getServerSession(final Session session) throws OXException {
        if (null == session) {
            throw new IllegalArgumentException("session");
        }
        return ServerSessionAdapter.valueOf(session);        
    }
    
    /**
     * Gets the context the session belongs to.
     *  
     * @param session the session
     * @return the context
     * @throws OXException
     */
    protected static Context getContext(final Session session) throws OXException {
        if (null == session) {
            throw new IllegalArgumentException("session");
        }
        return getServerSession(session).getContext();
    }

    /**
     * Creates a clone from an existing contact and merges the attributes from another contact into that clone.
     * 
     * @param into the original contact that is used as base for the clone
     * @param from the contact containing the changes to be merged into the clone
     * @return the merged contact
     */
    protected static Contact merge(final Contact into, final Contact from) {
        if (null == into) {
            throw new IllegalArgumentException("into");
        } else if (null == from) {
            throw new IllegalArgumentException("from");
        }        
        return MERGER.merge(into, from);
    }
    
    /**
     * 
     * @param into
     * @param from
     * @return
     */
    protected static List<Contact> merge(final List<Contact> into, final List<Contact> from) {
        if (null == into) {
            throw new IllegalArgumentException("into");
        } else if (null == from) {
            throw new IllegalArgumentException("from");
        }
        return MERGER.merge(into, from);
    }

    protected static List<Contact> mergeByID(final List<Contact> into, final List<Contact> from) {
        if (null == into) {
            throw new IllegalArgumentException("into");
        } else if (null == from) {
            throw new IllegalArgumentException("from");
        }        
        for (final Contact fromData : from) {
            final int objectID = fromData.getObjectID();
            for (int i = 0; i < into.size(); i++) {
                final Contact intoData = into.get(i);
                if (objectID == intoData.getObjectID()) {
                    into.set(i, merge(intoData, fromData));
                    break;
                }
            }
        }
        return into;
    }
    
    /**
     * Gets the contact fields that are actually set in the supplied contact, thus the <code>Contact.contains(int field)</code> method 
     * actually return <code>true</code> for.
     *  
     * @param contact the contact
     * @return the assigned contact fields
     */
    protected static ContactField[] getAssignedFields(final Contact contact) {
        if (null == contact) {
            throw new IllegalArgumentException("contact");
        }
        final List<ContactField> setFields = new ArrayList<ContactField>(); 
        for (final ContactField field : allFields()) {
            if (contact.contains(field.getNumber())) {
                setFields.add(field);
            }
        }        
        return setFields.toArray(new ContactField[setFields.size()]);
    }

    /**
     * Gets an array containing the column numbers of all contact fields.
     * 
     * @return the columns
     */
    protected static int[] allColumns() {
        return asColumns(allFields());
    }
    
    /**
     * Gets all contact fields.
     * 
     * @return the fields
     */
    protected static ContactField[] allFields() {
        return ContactField.values();
    }
    
    /**
     * Gets an array containing the column numbers of the supplied contact fields.
     * 
     * @param fields the fields
     * @return the columns, or <code>null</code> if no fields were supplied
     */
    protected static int[] asColumns(final ContactField[] fields) {
        if (null != fields) {
            final int[] columns = new int[fields.length];
            for (int i = 0; i < fields.length; i++) {
                columns[i] = fields[i].getNumber();
            }            
            return columns;
        } else {
            return null;
        }
    }
    
    private static SingleSearchTerm getSearchTermFor(final String folderId) {
        final SingleSearchTerm folderIdTerm = new SingleSearchTerm(SingleSearchTerm.SingleOperation.EQUALS);
        folderIdTerm.addOperand(new ColumnOperand(ContactField.FOLDER_ID.getAjaxName()));
        folderIdTerm.addOperand(new ConstantOperand<String>(folderId));
        return folderIdTerm;
    }
    
}
