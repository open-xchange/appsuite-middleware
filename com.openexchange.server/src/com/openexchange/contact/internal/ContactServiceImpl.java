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

package com.openexchange.contact.internal;

import java.util.Collection;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.contact.ContactService;
import com.openexchange.contact.storage.ContactStorage;
import com.openexchange.contact.storage.SortOptions;
import com.openexchange.contact.storage.registry.ContactStorageRegistry;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.search.SearchTerm;

/**
 * {@link ContactServiceImpl} - {@link ContactService} implementation.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ContactServiceImpl implements ContactService {
	
    private final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ContactServiceImpl.class));
    
    public ContactServiceImpl() {
    	
    }	

	@Override
	public Contact get(int contextID, int userID, String folderId, String id) throws OXException {
		
		
		

		/*
		 * pass through to storage layer
		 */
		return getStorage(contextID, folderId).get(contextID, folderId, id);
	}

	@Override
	public Contact get(int contextID, int userID, String folderId, String id, ContactField[] fields) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Contact> all(int contextID, int userID, String folderId) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Contact> all(int contextID, int userID, String folderId, SortOptions sortOptions) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Contact> all(int contextID, int userID, String folderId, ContactField[] fields) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Contact> all(int contextID, int userID, String folderId, ContactField[] fields, SortOptions sortOptions) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Contact> list(int contextID, int userID, String folderId,
			String[] ids) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Contact> list(int contextID, int userID, String folderId,
			String[] ids, SortOptions sortOptions) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Contact> list(int contextID, int userID, String folderId,
			String[] ids, ContactField[] fields) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Contact> list(int contextID, int userID, String folderId,
			String[] ids, ContactField[] fields, SortOptions sortOptions)
			throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Contact> deleted(int contextID, int userID,
			String folderId, Date since) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Contact> deleted(int contextID, int userID,
			String folderId, Date since, ContactField[] fields)
			throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <O> Collection<Contact> search(int contextID, int userID, SearchTerm<O> term) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <O> Collection<Contact> search(int contextID, int userID, SearchTerm<O> term, SortOptions sortOptions) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <O> Collection<Contact> search(int contextID, int userID, SearchTerm<O> term, ContactField[] fields) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <O> Collection<Contact> search(int contextID, int userID, SearchTerm<O> term, ContactField[] fields, SortOptions sortOptions)
			throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void create(int contextID, int userID, String folderId, Contact contact) throws OXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(int contextID, int userID, String folderId, Contact contact, Date lastRead) throws OXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(int contextID, int userID, String folderId, String id, Date lastRead) throws OXException {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Gets the contact storage. 
	 * 
	 * @param contextID the current context ID
	 * @param folderId the folder ID
	 * @return the contact storage
	 * @throws OXException
	 */
	protected ContactStorage getStorage(final int contextID, final String folderId) throws OXException {
		return ContactServiceLookup.getService(ContactStorageRegistry.class, true).getStorage(contextID, folderId);
	}
    
}
